/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.create

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.FormDraftSlot
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.store.submit.draft.failureReason
import kpt.core.base.store.submit.draft.isOffline
import kpt.core.base.store.submit.draft.offlineWhenUnsent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.DateFormatPattern
import kpt.core.data.group.GroupCommandRepository
import kpt.core.data.office.OfficeRepository
import kpt.core.model.draft.CreateForm
import kpt.core.model.group.GroupPayload
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.office.Office
import kpt.core.model.utils.ApiDateFormatter

/**
 * Create a group — ported from
 * `6b66e8a43:feature/groups/createNewGroup/{CreateNewGroupScreen,CreateNewGroupViewModel,
 * CreateNewGroupUiState}.kt`.
 *
 * Reached from the groups list's FAB, which this port's list did not have at all — the original
 * draws `MifosFAB(icon = MifosIcons.Add, onClick = onAddGroupClick)` and the ported
 * `GroupsListScreen` took no create callback, so the affordance was simply missing rather than
 * null-gated.
 *
 * ## The original is pre-MVI, so the adaptation is larger than usual
 *
 * `CreateNewGroupViewModel` is a plain `ViewModel` with a hand-rolled
 * `MutableStateFlow<CreateNewGroupUiState>` and the `Loading / ShowOffices / ShowFetchingError /
 * ShowGroupCreatedSuccessfully` sealed class that §6a's adaptation 1 replaces. The offices read
 * becomes a [ScreenState] stream and the write becomes an action on the `BaseViewModel` triad;
 * the eleven `rememberSaveable` fields in the screen move here with them.
 *
 * ## FIXED: the success message was decided by a flag, not by what happened
 *
 * ```kotlin
 * fun getResponse(): String = when (userStatus.value) {
 *     false -> "created successfully"
 *     true  -> "Saved into DB Successfully"
 * }
 * ```
 *
 * `userStatus` is a stored PREFERENCE, so the screen announced "created successfully" or "saved
 * locally" according to a setting read at composition — never according to whether the request
 * actually reached Fineract. An officer who went offline after that preference was written was
 * told the group had been created.
 *
 * The substrate already answers this correctly: `MutationResult.Applied` carries `synced`, which
 * is the truth of THIS write. [CreateGroupEvent.Created] carries it so the screen can say which
 * of the two actually happened.
 *
 * ## The date and its declared format come from one value
 *
 * As on the share and fixed-deposit wizards — see [SUBMIT_DATE_FORMAT].
 */
internal class CreateGroupViewModel(
    officeRepository: OfficeRepository,
    private val commands: GroupCommandRepository,
    drafts: FormDraftRepository,
) : BaseViewModel<CreateGroupState, CreateGroupEvent, CreateGroupAction>(CreateGroupState()) {

    /** A create that did not land is kept here — resumable on this form, listed in Sync & Drafts. */
    private val draft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.GROUP.key,
        ownerId = null,
        CreateGroupState.serializer(),
        GroupPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = draft.offered

    /** The offices a group may be opened in. */
    val offices: StateFlow<ScreenState<List<Office>>> =
        officeRepository.officesStream(viewModelScope).state
            // No offices is Empty (the screen's empty slot); the stream reports it as empty Content.
            .emptyIfContent { it.isEmpty() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: CreateGroupAction) {
        when (action) {
            CreateGroupAction.NavigateBack -> sendEvent(CreateGroupEvent.NavigateBack)

            is CreateGroupAction.UpdateName ->
                mutableStateFlow.value = state.copy(name = action.value, nameError = false)

            is CreateGroupAction.UpdateOffice ->
                mutableStateFlow.value = state.copy(office = action.option, officeError = false)

            is CreateGroupAction.UpdateExternalId ->
                mutableStateFlow.value = state.copy(externalId = action.value)

            is CreateGroupAction.UpdateSubmissionDate ->
                mutableStateFlow.value = state.copy(submissionDateMillis = action.millis)

            is CreateGroupAction.UpdateActivationDate ->
                mutableStateFlow.value = state.copy(activationDateMillis = action.millis)

            is CreateGroupAction.UpdateActive -> mutableStateFlow.value = withActive(action.value)

            CreateGroupAction.Submit -> submit()

            CreateGroupAction.DismissError ->
                mutableStateFlow.value = state.copy(error = null)

            CreateGroupAction.ResumeDraft -> draft.resume()?.let { mutableStateFlow.value = it }
            CreateGroupAction.DiscardDraft -> draft.discard()
            CreateGroupAction.SaveDraft -> {
                draft.keep(state.name, state, state.toPayload(), failure = state.failureReason)
                mutableStateFlow.value = state.copy(draftSaved = true)
            }
        }
    }

    /**
     * An inactive group has no activation date to send; clearing it here means the payload cannot
     * carry a date for a group that is not being activated.
     */
    private fun withActive(active: Boolean): CreateGroupState =
        state.copy(isActive = active, activationDateMillis = if (active) state.activationDateMillis else 0L)

    private fun failedState(result: MutationResult<*>): CreateGroupState = state.copy(
        isSubmitting = false,
        error = CreateGroupError.Failed,
        failureReason = result.failureReason { it.serverErrorMessage() },
    )

    private fun submit() {
        val errors = state.validate()
        if (!errors.isValid || state.isSubmitting) {
            mutableStateFlow.value =
                state.copy(nameError = errors.name, officeError = errors.office)
            return
        }

        mutableStateFlow.value = state.copy(isSubmitting = true, error = null)
        viewModelScope.launch {
            when (val result = commands.createGroup(state.toPayload()).offlineWhenUnsent()) {
                is MutationResult.Applied -> {
                    draft.clear()
                    mutableStateFlow.value = state.copy(isSubmitting = false)
                    // `synced` is the truth of THIS write, not a stored preference — see the
                    // class KDoc.
                    sendEvent(
                        CreateGroupEvent.Created(
                            synced = result.synced,
                            groupId = result.value.groupId ?: result.value.resourceId,
                        ),
                    )
                }

                // Offline: kept automatically and sent on reconnect — the officer already chose to submit.
                is MutationResult.Blocked -> if (result.isOffline) {
                    draft.keep(state.name, state.copy(isSubmitting = false), state.toPayload(), failure = null)
                    mutableStateFlow.value =
                        state.copy(isSubmitting = false, error = CreateGroupError.Offline, draftSaved = true)
                } else {
                    mutableStateFlow.value = failedState(result)
                }

                // Refused: the server's reason, and the choice to keep it (Save as draft) — never automatic,
                // since resending a rejected create would only fail again.
                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create group failed" }
                    mutableStateFlow.value = failedState(result)
                }

                is MutationResult.Conflicted ->
                    mutableStateFlow.value = failedState(result)
            }
        }
    }

    internal companion object {
        /** One pattern formats the dates AND declares `dateFormat`. */
        val SUBMIT_DATE_FORMAT = DateFormatPattern.NUMERIC_DASH
        const val LOCALE = "en"
        private const val STATE_TIMEOUT_MS = 5_000L
        private const val TAG = "CreateGroup"
    }
}

/** One selectable office. The CHOICE carries its id, so no index can drift against a re-read. */
@Serializable
internal data class OfficeOption(val id: Int, val label: String)

internal data class CreateGroupFieldErrors(
    val name: Boolean = false,
    val office: Boolean = false,
) {
    val isValid: Boolean get() = !name && !office
}

/**
 * The original's two required fields.
 *
 * `name.isBlank()` rather than `isEmpty()`: a whitespace-only name passed the original's check and
 * reached Fineract, the same defect the client forms carried.
 */
internal fun CreateGroupState.validate(): CreateGroupFieldErrors = CreateGroupFieldErrors(
    name = name.isBlank(),
    office = office == null,
)

internal fun CreateGroupState.toPayload(): GroupPayload = GroupPayload(
    name = name.trim(),
    officeId = office?.id ?: 0,
    externalId = externalId.trim().ifEmpty { null },
    active = isActive,
    submittedOnDate = ApiDateFormatter.formatForApi(
        submissionDateMillis,
        CreateGroupViewModel.SUBMIT_DATE_FORMAT,
    ),
    // Only an ACTIVE group carries an activation date; sending one for an inactive group would
    // describe a state the payload is not asking for.
    activationDate = if (isActive) {
        ApiDateFormatter.formatForApi(
            activationDateMillis,
            CreateGroupViewModel.SUBMIT_DATE_FORMAT,
        )
    } else {
        null
    },
    dateFormat = CreateGroupViewModel.SUBMIT_DATE_FORMAT.pattern,
    locale = CreateGroupViewModel.LOCALE,
)

internal enum class CreateGroupError {
    Offline,
    Failed,
}

/** Serializable so a draft can restore the form as left; the transient fields are per-visit. */
@Serializable
internal data class CreateGroupState(
    val name: String = "",
    val office: OfficeOption? = null,
    val externalId: String = "",
    val submissionDateMillis: Long = 0L,
    val activationDateMillis: Long = 0L,
    val isActive: Boolean = false,
    @Transient val nameError: Boolean = false,
    @Transient val officeError: Boolean = false,
    @Transient val isSubmitting: Boolean = false,
    @Transient val error: CreateGroupError? = null,
    /** The server's reason when it refused the create — shown, and kept with a saved draft. */
    @Transient val failureReason: String? = null,
    @Transient val draftSaved: Boolean = false,
) {
    val canSubmit: Boolean get() = !isSubmitting
}

internal sealed interface CreateGroupEvent {
    data object NavigateBack : CreateGroupEvent

    /**
     * The group was created.
     *
     * [synced] says whether it reached Fineract or was queued locally — the distinction the
     * original decided from a stored preference instead of from the write.
     */
    /** [groupId] is the server's id — null when the write was queued offline and has none yet. */
    data class Created(val synced: Boolean, val groupId: Int? = null) : CreateGroupEvent
}

internal sealed interface CreateGroupAction {
    data object NavigateBack : CreateGroupAction
    data object Submit : CreateGroupAction
    data object DismissError : CreateGroupAction
    data object ResumeDraft : CreateGroupAction
    data object DiscardDraft : CreateGroupAction
    data object SaveDraft : CreateGroupAction
    data class UpdateName(val value: String) : CreateGroupAction
    data class UpdateOffice(val option: OfficeOption) : CreateGroupAction
    data class UpdateExternalId(val value: String) : CreateGroupAction
    data class UpdateSubmissionDate(val millis: Long) : CreateGroupAction
    data class UpdateActivationDate(val millis: Long) : CreateGroupAction
    data class UpdateActive(val value: Boolean) : CreateGroupAction
}
