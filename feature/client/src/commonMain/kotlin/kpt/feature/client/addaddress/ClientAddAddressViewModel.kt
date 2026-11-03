/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.addaddress

import androidx.lifecycle.SavedStateHandle
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
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.FormDraftSlot
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.store.submit.draft.failureReason
import kpt.core.base.store.submit.draft.isOffline
import kpt.core.base.store.submit.draft.offlineWhenUnsent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.client.ClientRepository
import kpt.core.model.client.AddressTemplate
import kpt.core.model.client.PostClientAddressRequest
import kpt.core.model.draft.AddressDraftPayload
import kpt.core.model.draft.CreateForm
import kpt.core.model.objects.error.serverErrorMessage

/**
 * Add a postal address to a client — ported from
 * `6b66e8a43:feature/client/clientAddress/addAddress/AddAddressScreen.kt` (610 lines, no ViewModel
 * of its own).
 *
 * Reached from the client ADDRESS screen's "+", which was `null`-gated (so not drawn) until this
 * landed.
 *
 * ## The form had no ViewModel, and shared the LIST's
 *
 * The original holds eleven `rememberSaveable` fields and six error strings inside the composable,
 * and reaches the write by calling `viewModel.createClientAddress(...)` directly on the ADDRESS
 * LIST's ViewModel — a public method bypassing the action channel entirely, the same shape the
 * photo screen's `deleteClientImage()` had.
 *
 * Both move here, per §6a adaptation 2 (dispatch becomes `actionChannel` sends). The list's
 * ViewModel stays read-only; this form owns its own state, which is also what makes the validation
 * below testable without a composition.
 *
 * ## The six error messages are UNREACHABLE, and are kept that way
 *
 * The original gates its submit button on `areRequiredAddressFieldsFilled(...)`:
 *
 * ```kotlin
 * addressTypeId > 0 && addressLine1.isNotBlank() && city.isNotBlank() &&
 *     stateProvinceId > 0 && countryId > 0 && postalCode.isNotBlank()
 * ```
 *
 * and validates on click with `validateAddressFields(...)`, whose six conditions are
 * `addressTypeId <= 0`, `addressLine1.isBlank()`, `city.isBlank()`, `stateProvinceId <= 0`,
 * `countryId <= 0`, `postalCode.isBlank()` — the exact negation of the same six terms. The button
 * is therefore enabled if and only if validation would pass, so the validator always returns six
 * nulls and **not one of its error strings can ever render**.
 *
 * The consequence for an officer is a dead end: leave a required field empty and Submit is simply
 * dim, with nothing saying which field is missing.
 *
 * KEPT as found. §6a is explicit that a carried-over screen keeps everything "including anything
 * the agent believes is wrong", recorded rather than fixed — the same call the signature and photo
 * sheets' dead "More" option gets. Replacing the gate with submit-then-show-errors is a UI
 * redesign, which is Stitch's job. Tracked as `add-address-required-field-errors-unreachable`.
 *
 * The validator is still ported in full, and [validate] is tested, because the moment the gate
 * changes those messages become live and they should be right when they do.
 */
internal class ClientAddAddressViewModel(
    repository: ClientRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<ClientAddAddressState, ClientAddAddressEvent, ClientAddAddressAction>(
    ClientAddAddressState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    /** This client's address create that did not land — resumable here, listed in Sync & Drafts. */
    private val draft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.ADDRESS.key,
        ownerId = state.clientId.toLong(),
        ClientAddAddressState.serializer(),
        AddressDraftPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = draft.offered

    private fun keepDraft(failure: String?) = draft.keep(
        title = listOf(state.addressLine1, state.city).filter { it.isNotBlank() }.joinToString(", "),
        state = state.copy(isSubmitting = false),
        payload = AddressDraftPayload(state.clientId, state.addressTypeId, state.toPayload()),
        failure = failure,
    )

    /**
     * Address type, state/province and country option lists.
     *
     * `addressTemplateStream` already existed on the repository and had NO consumer before this
     * screen — the three dropdowns are its first readers.
     */
    val template: StateFlow<ScreenState<AddressTemplate>> =
        repository.addressTemplateStream(viewModelScope).state
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientAddAddressAction) {
        when (action) {
            ClientAddAddressAction.ResumeDraft -> draft.resume()?.let { mutableStateFlow.value = it }
            ClientAddAddressAction.DiscardDraft -> draft.discard()
            ClientAddAddressAction.SaveDraft -> {
                keepDraft(failure = state.failureReason ?: "Not accepted by the server.")
                mutableStateFlow.value = state.copy(draftSaved = true)
            }
            ClientAddAddressAction.NavigateBack -> sendEvent(ClientAddAddressEvent.NavigateBack)
            ClientAddAddressAction.Submit -> submit()
            ClientAddAddressAction.DismissError ->
                mutableStateFlow.value = state.copy(writeError = null)
            else -> updateField(action)
        }
    }

    private fun updateField(action: ClientAddAddressAction) {
        when (action) {
            is ClientAddAddressAction.UpdateAddressType ->
                mutableStateFlow.value =
                    state.copy(addressTypeId = action.id, addressTypeLabel = action.label)

            is ClientAddAddressAction.UpdateStateProvince ->
                mutableStateFlow.value =
                    state.copy(stateProvinceId = action.id, stateProvinceLabel = action.label)

            is ClientAddAddressAction.UpdateCountry ->
                mutableStateFlow.value =
                    state.copy(countryId = action.id, countryLabel = action.label)

            is ClientAddAddressAction.UpdateAddressLine1 ->
                mutableStateFlow.value = state.copy(addressLine1 = action.value)

            is ClientAddAddressAction.UpdateAddressLine2 ->
                mutableStateFlow.value = state.copy(addressLine2 = action.value)

            is ClientAddAddressAction.UpdateAddressLine3 ->
                mutableStateFlow.value = state.copy(addressLine3 = action.value)

            is ClientAddAddressAction.UpdateCity ->
                mutableStateFlow.value = state.copy(city = action.value)

            is ClientAddAddressAction.UpdatePostalCode ->
                mutableStateFlow.value = state.copy(postalCode = action.value)

            else -> Unit
        }
    }

    private fun failSubmit(reason: String?) {
        mutableStateFlow.value = state.copy(
            isSubmitting = false,
            writeError = AddAddressError.Failed,
            failureReason = reason,
        )
    }

    private fun submit() {
        val errors = state.validate()
        mutableStateFlow.value = state.copy(errors = errors)
        if (!errors.isValid || state.isSubmitting) return

        mutableStateFlow.value = state.copy(isSubmitting = true, writeError = null)
        viewModelScope.launch {
            val result = commands.createClientAddress(
                clientId = state.clientId,
                addressTypeId = state.addressTypeId,
                addressPayload = state.toPayload(),
            ).offlineWhenUnsent()
            when (result) {
                is MutationResult.Applied -> {
                    draft.clear()
                    mutableStateFlow.value = state.copy(isSubmitting = false)
                    sendEvent(ClientAddAddressEvent.Saved)
                }

                // Offline: kept automatically and sent on reconnect.
                is MutationResult.Blocked -> if (result.isOffline) {
                    keepDraft(failure = null)
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        writeError = AddAddressError.Offline,
                        draftSaved = true,
                    )
                } else {
                    failSubmit(result.failureReason { it.serverErrorMessage() })
                }

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create client address failed" }
                    failSubmit(result.failureReason { it.serverErrorMessage() })
                }

                is MutationResult.Conflicted -> failSubmit(result.failureReason())
            }
        }
    }

    internal companion object {
        const val CLIENT_ID_KEY = "clientId"
        private const val STATE_TIMEOUT_MS = 5_000L
        private const val TAG = "ClientAddAddress"
    }
}

/**
 * The original's six required-field rules, verbatim.
 *
 * `addressLine2` and `addressLine3` are optional in the original and stay optional here.
 */
internal fun ClientAddAddressState.validate(): AddressFieldErrors = AddressFieldErrors(
    addressType = addressTypeId <= 0,
    addressLine1 = addressLine1.isBlank(),
    city = city.isBlank(),
    stateProvince = stateProvinceId <= 0,
    country = countryId <= 0,
    postalCode = postalCode.isBlank(),
)

/**
 * The original's submit gate: `areRequiredAddressFieldsFilled`.
 *
 * Kept as a SEPARATE predicate from [validate] even though the two are logically identical,
 * because that identity is precisely the defect recorded in the ViewModel's KDoc — collapsing them
 * into one would hide it, and the two are meant to diverge whenever the gate is reconsidered.
 */
internal val ClientAddAddressState.areRequiredFieldsFilled: Boolean
    get() = addressTypeId > 0 &&
        addressLine1.isNotBlank() &&
        city.isNotBlank() &&
        stateProvinceId > 0 &&
        countryId > 0 &&
        postalCode.isNotBlank()

internal fun ClientAddAddressState.toPayload(): PostClientAddressRequest = PostClientAddressRequest(
    addressLine1 = addressLine1.trim(),
    addressLine2 = addressLine2.trim(),
    addressLine3 = addressLine3.trim(),
    city = city.trim(),
    stateProvinceId = stateProvinceId,
    countryId = countryId,
    postalCode = postalCode.trim(),
)

/** Which required fields are missing. All false is the only submittable shape. */
internal data class AddressFieldErrors(
    val addressType: Boolean = false,
    val addressLine1: Boolean = false,
    val city: Boolean = false,
    val stateProvince: Boolean = false,
    val country: Boolean = false,
    val postalCode: Boolean = false,
) {
    val isValid: Boolean
        get() = !addressType && !addressLine1 && !city && !stateProvince && !country && !postalCode
}

/** What went wrong on the WRITE, as a value the screen renders. */
internal enum class AddAddressError {
    Offline,
    Failed,
}

/** Serializable so a draft restores the form as left; the transient fields are per-visit. */
@Serializable
internal data class ClientAddAddressState(
    val clientId: Int = -1,
    val addressTypeId: Int = 0,
    val addressTypeLabel: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val addressLine3: String = "",
    val city: String = "",
    val stateProvinceId: Int = 0,
    val stateProvinceLabel: String = "",
    val countryId: Int = 0,
    val countryLabel: String = "",
    val postalCode: String = "",
    @Transient val errors: AddressFieldErrors = AddressFieldErrors(),
    @Transient val isSubmitting: Boolean = false,
    @Transient val writeError: AddAddressError? = null,
    /** The server's reason when it refused the create — shown, and kept with a saved draft. */
    @Transient val failureReason: String? = null,
    @Transient val draftSaved: Boolean = false,
) {
    /** The original's `enabled = isSubmitEnabled`, and off while a write is in flight. */
    val canSubmit: Boolean get() = areRequiredFieldsFilled && !isSubmitting
}

internal sealed interface ClientAddAddressEvent {
    data object NavigateBack : ClientAddAddressEvent
    data object Saved : ClientAddAddressEvent
}

internal sealed interface ClientAddAddressAction {
    data object NavigateBack : ClientAddAddressAction
    data object Submit : ClientAddAddressAction
    data object DismissError : ClientAddAddressAction
    data object ResumeDraft : ClientAddAddressAction
    data object DiscardDraft : ClientAddAddressAction
    data object SaveDraft : ClientAddAddressAction
    data class UpdateAddressType(val id: Int, val label: String) : ClientAddAddressAction
    data class UpdateStateProvince(val id: Int, val label: String) : ClientAddAddressAction
    data class UpdateCountry(val id: Int, val label: String) : ClientAddAddressAction
    data class UpdateAddressLine1(val value: String) : ClientAddAddressAction
    data class UpdateAddressLine2(val value: String) : ClientAddAddressAction
    data class UpdateAddressLine3(val value: String) : ClientAddAddressAction
    data class UpdateCity(val value: String) : ClientAddAddressAction
    data class UpdatePostalCode(val value: String) : ClientAddAddressAction
}
