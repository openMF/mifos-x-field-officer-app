/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.charge.ChargeCommandRepository
import kpt.core.data.charge.ChargeRepository
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.error.serverErrorMessage
import kpt.feature.charge.ui.ChargeUi
import kpt.feature.charge.ui.chargeResource
import kpt.feature.charge.ui.chargeResourceId
import kpt.feature.charge.ui.toUi

/**
 * The charges levied on ONE client, loan or savings account — the list half of
 * `6b66e8a43:feature/client/charges/{ChargesScreen,ChargesViewModel,ShowClientCharge}.kt` (whose
 * route already took `resourceType` + `resourceId`) merged with the loan's separate
 * `loanCharge/LoanChargeViewModel.kt`.
 *
 * ## One stream instead of three loaders
 *
 * The original branched `if (resourceType == clients) clientLoadCharges() else
 * loadOtherAccountCharges()` and, after a delete, re-ran that loader to re-open its bottom sheet. The
 * store owns the endpoint difference now, and a delete clears the row in the table the stream
 * observes — so nothing here re-reads.
 *
 * ## Edit is offered only where Fineract can do it
 *
 * The original offered Edit on a CLIENT charge and then answered it with "Updating client charges is
 * not supported" after the officer had filled the form in. [canEdit] keeps the action off the row.
 */
internal class ChargeListViewModel(
    repository: ChargeRepository,
    private val commands: ChargeCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ChargeListState, ChargeListEvent, ChargeListAction>(ChargeListState()) {

    private val resource: ChargeResource = savedStateHandle.chargeResource()
    private val resourceId: Int = savedStateHandle.chargeResourceId()

    val canEdit: Boolean = resource.canUpdate

    /** Held, not collected inline — the pull gesture and the error's retry reach it. */
    private val stream = repository.chargesStream(resource, resourceId, viewModelScope)

    val charges: StateFlow<ScreenState<List<ChargeUi>>> = stream.state
        .mapContent { rows, _ -> rows.map { it.toUi() } }
        // An owner with no charges is EMPTY, not a list with no rows — one branch in the screen.
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ChargeListAction) {
        when (action) {
            ChargeListAction.NavigateBack -> sendEvent(ChargeListEvent.NavigateBack)
            // ONE member for the pull gesture and the error retry: `RefreshableScreenContent` routes
            // both to the same callback, and `retry()` is `refresh(forceFresh = true)` anyway.
            ChargeListAction.OnRefresh -> stream.refresh(forceFresh = true)
            ChargeListAction.OnAddCharge -> sendEvent(ChargeListEvent.AddCharge(resource.path, resourceId))

            is ChargeListAction.ToggleExpanded -> mutableStateFlow.value = state.copy(
                expandedId = if (state.expandedId == action.id) null else action.id,
            )

            is ChargeListAction.OnViewCharge ->
                sendEvent(ChargeListEvent.OpenCharge(resource.path, resourceId, action.id))

            is ChargeListAction.OnEditCharge -> if (canEdit) {
                sendEvent(ChargeListEvent.EditCharge(resource.path, resourceId, action.id))
            }

            // Destructive and irreversible, so it is always confirmed — the original deleted on the
            // first tap of the row's menu.
            is ChargeListAction.OnDeleteCharge ->
                mutableStateFlow.value = state.copy(pendingDeleteId = action.id)

            ChargeListAction.DismissDialog ->
                mutableStateFlow.value = state.copy(pendingDeleteId = null, writeError = null)

            ChargeListAction.ConfirmDelete -> confirmDelete()
        }
    }

    private fun confirmDelete() {
        val id = state.pendingDeleteId ?: return
        if (state.isDeleting) return
        mutableStateFlow.value = state.copy(isDeleting = true, pendingDeleteId = null)
        viewModelScope.launch {
            val result = commands.deleteCharge(resource, resourceId, id)
            mutableStateFlow.value = when (result) {
                is MutationResult.Applied ->
                    state.copy(isDeleting = false, expandedId = null, writeError = null)

                // Offline is not a failure the officer caused, and moving fixes it — so it is told
                // apart from a refusal. The original showed every failure as a full-screen error
                // that REPLACED the still-correct list.
                is MutationResult.Blocked -> state.copy(
                    isDeleting = false,
                    writeError = if (result.reason == BlockReason.OFFLINE) {
                        ChargeWriteError.Offline
                    } else {
                        ChargeWriteError.Failed()
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "delete charge failed" }
                    // Fineract's reason ("cannot be deleted as the loan is not pending approval")
                    // is what the officer can act on.
                    state.copy(
                        isDeleting = false,
                        writeError = ChargeWriteError.Failed(result.cause.serverErrorMessage()),
                    )
                }

                is MutationResult.Conflicted -> state.copy(isDeleting = false, writeError = ChargeWriteError.Failed())
            }
        }
    }

    private companion object {
        const val TAG = "ChargeListViewModel"

        /** Template default — long enough to survive a rotation or a brief tab switch. */
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

internal data class ChargeListState(
    val expandedId: Int? = null,
    /** The charge awaiting the delete confirmation; `null` when no dialog is up. */
    val pendingDeleteId: Int? = null,
    val isDeleting: Boolean = false,
    val writeError: ChargeWriteError? = null,
)

/** A failed WRITE, kept apart from the read's [ScreenState] so it never blanks the list. */
internal sealed interface ChargeWriteError {
    data object Offline : ChargeWriteError

    /** [serverMessage] is Fineract's own sentence when it sent one; else the generic copy shows. */
    data class Failed(val serverMessage: String? = null) : ChargeWriteError
}

internal sealed interface ChargeListEvent {
    data object NavigateBack : ChargeListEvent
    data class AddCharge(val resourceType: String, val resourceId: Int) : ChargeListEvent
    data class OpenCharge(val resourceType: String, val resourceId: Int, val chargeId: Int) : ChargeListEvent
    data class EditCharge(val resourceType: String, val resourceId: Int, val chargeId: Int) : ChargeListEvent
}

internal sealed interface ChargeListAction {
    data object NavigateBack : ChargeListAction
    data object OnRefresh : ChargeListAction
    data object OnAddCharge : ChargeListAction
    data object ConfirmDelete : ChargeListAction
    data object DismissDialog : ChargeListAction
    data class ToggleExpanded(val id: Int) : ChargeListAction
    data class OnViewCharge(val id: Int) : ChargeListAction
    data class OnEditCharge(val id: Int) : ChargeListAction
    data class OnDeleteCharge(val id: Int) : ChargeListAction
}
