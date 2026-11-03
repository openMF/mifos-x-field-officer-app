/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.staff

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
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.client.ClientRepository
import kpt.core.model.client.StaffOption
import kpt.core.model.objects.clients.AssignStaffRequest
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.ui.components.ResultStatus

/**
 * Assign a loan officer to a client — ported from
 * `6b66e8a43:feature/client/clientStaff/ClientStaffViewModel.kt`.
 *
 * The original was already on the `BaseViewModel<State, Event, Action>` triad, so the MVI shape
 * carries over unchanged. Three things underneath it do not.
 *
 * ## 1. The read is a stream, and it is the CLIENT TEMPLATE
 *
 * `getClientStaffOptions(clientId)` looked like its own endpoint and was not: it called
 * `getClientTemplate(clientId)` and kept the `staffOptions` field. Two sibling methods did the
 * same for `savingAccountOptions` and `shareAccounts` — three uncached requests for one response.
 * It is one `clientTemplateStream` here, projected with [mapContent], so the screens that need the
 * other fields share the cache rather than re-fetching it.
 *
 * ## 2. The hand-rolled connectivity observer is gone
 *
 * The original ran `networkMonitor.isOnline.collect { … networkConnection = it }` for the sole
 * purpose of telling `AppErrorComponent` whether to blame the network. `ScreenState.NoNetwork`
 * is decided by the stream for every screen, so both the observer and the `networkConnection`
 * field go. Fifth screen in this port where that collapse happened.
 *
 * ## 3. `try/catch` around the read is gone, and `DialogState` loses two of its three cases
 *
 * `Loading` and `Error` described the READ and were rendered as dialogs over an empty form;
 * they are states of the content area now. `ShowStatusDialog` stays — a submit result the officer
 * must acknowledge before moving on genuinely is a dialog, and it is the one the original used to
 * drive navigation.
 *
 * ## Carried over verbatim, including a defect
 *
 * `currentSelectedIndex` defaults to 0 and indexes [staffOptions] directly, so the form opens with
 * the first officer pre-selected and submitting without touching the dropdown assigns them. That is
 * the original's behaviour and §6a keeps it; recorded in CAPABILITY_GAPS rather than fixed here.
 * The index is range-checked before use — the original's `state.staffOptions[index]` would throw if
 * the list ever shrank under a held index, and a crash is not UI fidelity.
 */
internal class ClientStaffViewModel(
    repository: ClientRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientStaffState, ClientStaffEvent, ClientStaffAction>(
    ClientStaffState(id = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    private val stream = repository.clientTemplateStream(state.id, viewModelScope)

    /** The officers this client may be assigned to. Projected from the template, not its own read. */
    val staffOptions: StateFlow<ScreenState<List<StaffOption>>> = stream.state
        .mapContent { template, _ -> template.staffOptions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientStaffAction) {
        when (action) {
            ClientStaffAction.NavigateBack -> sendEvent(ClientStaffEvent.NavigateBack)
            ClientStaffAction.OnNext -> sendEvent(ClientStaffEvent.NavigateNext)
            ClientStaffAction.OnRetry -> stream.retry()

            is ClientStaffAction.OptionChanged ->
                mutableStateFlow.value = state.copy(currentSelectedIndex = action.index)

            is ClientStaffAction.OnSubmit -> assign(action.staffId)

            ClientStaffAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)
        }
    }

    /**
     * The staff id comes from the ACTION, not from re-indexing the list in here.
     *
     * The original read `state.staffOptions[state.currentSelectedIndex]` at submit time — a
     * second lookup into a list the ViewModel no longer owns, against an index chosen against
     * whatever the list held when the user tapped. The screen has the selected option in hand; it
     * passes the id.
     */
    private fun assign(staffId: Int) {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            when (val result = commands.assignStaff(state.id, AssignStaffRequest(staffId = staffId))) {
                is MutationResult.Applied -> {
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = ClientStaffState.DialogState.ShowStatusDialog(ResultStatus.SUCCESS),
                    )
                    // The assignment changes the client's template AND the client itself. The
                    // template is what this screen reads, so it is refreshed; without this a back
                    // navigation and return would show the pre-assignment option set from cache.
                    stream.refresh(forceFresh = true)
                }

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientStaffState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "assign staff failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = ClientStaffState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            // The original surfaced `e.message` here — the reason is kept, the
                            // request line + raw JSON around it are not: a Fineract rejection
                            // ("staff not in client's office") is the only useful thing the
                            // officer gets. See `serverErrorMessage` in core/model.
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientStaffState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = FAILED_MSG,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "ClientStaff"

        // "clientId", NOT "id". The SavedStateHandle key for a type-safe route is the
        // PROPERTY NAME on the route class, and `ClientStaffRoute` declares `val clientId: Int`.
        // Reading "id" always missed, so the `?: -1` below it always won and this screen asked
        // Fineract for client -1 on every open — a 404 whose body was then rendered to the officer
        // as the error message. Found on device 2026-10-23; the other 25 client ViewModels all
        // had it right, so this was a single typo, not a pattern.
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val OFFLINE_MSG = "No connection — assignment not sent."
        const val FAILED_MSG = "Could not assign staff."
    }
}

/**
 * Screen state.
 *
 * [staffOptions] is NOT here — it is on the stream. The original held the list beside a
 * `DialogState`, which is what let a loading dialog sit over a populated form and an error dialog
 * over a stale option set.
 */
internal data class ClientStaffState(
    val id: Int = -1,
    val currentSelectedIndex: Int = 0,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    /** One case, where the original had three — the other two described the read. */
    sealed interface DialogState {
        data class ShowStatusDialog(val status: ResultStatus, val msg: String = "") : DialogState
    }
}

internal sealed interface ClientStaffEvent {
    data object NavigateBack : ClientStaffEvent
    data object NavigateNext : ClientStaffEvent
}

internal sealed interface ClientStaffAction {
    data object NavigateBack : ClientStaffAction
    data object OnRetry : ClientStaffAction
    data object OnNext : ClientStaffAction
    data object CloseDialog : ClientStaffAction
    data class OptionChanged(val index: Int) : ClientStaffAction
    data class OnSubmit(val staffId: Int) : ClientStaffAction
}
