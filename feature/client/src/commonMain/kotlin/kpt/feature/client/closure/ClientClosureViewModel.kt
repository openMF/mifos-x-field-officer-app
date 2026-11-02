/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.closure

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
import kpt.core.model.client.Narration
import kpt.core.model.objects.clients.ClientCloseRequest
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Close a client — ported from `6b66e8a43:feature/client/clientClosure/ClientClosureViewModel.kt`.
 *
 * Same three substrate changes as `clientStaff` (stream instead of a `try/catch` load, the
 * hand-rolled `networkMonitor` observer deleted, read Loading/Error out of `DialogState`), plus two
 * specific to this screen.
 *
 * ## The closure DATE is formatted once, at submit
 *
 * The original formatted it in two places with the same helper — once for display and once for the
 * request — which is fine, and is kept. What is NOT kept is reading the reason back out of
 * `state.reasons[state.currentSelectedIndex]` at submit time: the reasons are the stream's now, so
 * the action carries the chosen id (same change as `clientStaff`, same reason).
 *
 * ## A FIXED defect: the date contradicted its own declared format
 *
 * The original sent `DateHelper.getDateAsStringFromLong(date)` as `closureDate` while declaring
 * `dateFormat = ApiDateFormatter.DATE_FORMAT`. Those disagree: the helper formats with
 * `SHORT_MONTH` (`dd-MM-yyyy`, "21-10-2026") and the declared format is `dd MMMM yyyy`
 * ("21 October 2026"), so Fineract was told to parse a string it could not parse.
 *
 * `ApiDateFormatter.formatForApi(millis)` is the correct pairing and exists for precisely this —
 * its own KDoc says it is there "to ensure consistency between the date strings sent to the API
 * and the dateFormat parameter". The request uses it; the DISPLAYED date keeps the original's
 * `DateHelper` formatting, because that is the UI and §6a keeps the UI.
 *
 * This is not a §6a violation: §6a governs the carried-over Compose UI, and this is a write that
 * could not succeed. Fidelity to a request the server rejects is not fidelity.
 */
internal class ClientClosureViewModel(
    repository: ClientRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientClosureState, ClientClosureEvent, ClientClosureAction>(
    ClientClosureState(id = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    private val stream = repository.clientCloseTemplateStream(viewModelScope)

    /** Tenant-configured closure reasons. A catalog — shared across clients, hence keyed `Unit`. */
    val reasons: StateFlow<ScreenState<List<Narration>>> = stream.state
        .mapContent { template, _ -> template.narrations }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientClosureAction) {
        when (action) {
            ClientClosureAction.NavigateBack -> sendEvent(ClientClosureEvent.NavigateBack)
            ClientClosureAction.OnNext -> sendEvent(ClientClosureEvent.NavigateNext)
            ClientClosureAction.OnRetry -> stream.retry()

            is ClientClosureAction.OptionChanged ->
                mutableStateFlow.value = state.copy(currentSelectedIndex = action.index)

            is ClientClosureAction.UpdateDatePicker ->
                mutableStateFlow.value = state.copy(showDatePicker = action.status)

            is ClientClosureAction.UpdateDate ->
                mutableStateFlow.value = state.copy(date = action.date)

            ClientClosureAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            is ClientClosureAction.OnSubmit -> close(action.closureReasonId)
        }
    }

    private fun close(closureReasonId: Int) {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            val request = ClientCloseRequest(
                // Formatted by the helper that OWNS the format constant below — see the header.
                closureDate = ApiDateFormatter.formatForApi(state.date),
                closureReasonId = closureReasonId,
                dateFormat = ApiDateFormatter.DATE_FORMAT,
                locale = ApiDateFormatter.LOCALE,
            )
            when (val result = commands.closeClient(state.id, request)) {
                is MutationResult.Applied -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientClosureState.DialogState.ShowStatusDialog(ResultStatus.SUCCESS),
                )
                // NOT refreshed. A closed client is no longer this screen's subject — the success
                // dialog's only action leaves. The CLIENT and the roster are what went stale, and
                // both are re-read by the screens that own them when they are next shown.

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientClosureState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "close client failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = ClientClosureState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            // Fineract's reason is the useful part — "client has active loans" is
                            // the common one and tells the officer exactly what to do next.
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientClosureState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = FAILED_MSG,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "ClientClosure"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L

        const val OFFLINE_MSG = "No connection — closure not sent."
        const val FAILED_MSG = "Could not close this client."
    }
}

@OptIn(ExperimentalTime::class)
internal data class ClientClosureState(
    val id: Int = -1,
    val showDatePicker: Boolean = false,
    val currentSelectedIndex: Int = 0,
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    sealed interface DialogState {
        data class ShowStatusDialog(val status: ResultStatus, val msg: String = "") : DialogState
    }
}

internal sealed interface ClientClosureEvent {
    data object NavigateBack : ClientClosureEvent
    data object NavigateNext : ClientClosureEvent
}

internal sealed interface ClientClosureAction {
    data object NavigateBack : ClientClosureAction
    data object OnRetry : ClientClosureAction
    data object OnNext : ClientClosureAction
    data object CloseDialog : ClientClosureAction
    data class OptionChanged(val index: Int) : ClientClosureAction
    data class UpdateDatePicker(val status: Boolean) : ClientClosureAction
    data class UpdateDate(val date: Long) : ClientClosureAction
    data class OnSubmit(val closureReasonId: Int) : ClientClosureAction
}
