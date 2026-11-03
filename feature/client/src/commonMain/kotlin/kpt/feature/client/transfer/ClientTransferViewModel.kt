/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.transfer

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
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.office.OfficeRepository
import kpt.core.model.objects.clients.ProposeTransferRequest
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.office.Office
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Propose transferring a client to another office — ported from
 * `6b66e8a43:feature/client/clientTransfer/ClientTransferViewModel.kt`.
 *
 * Same substrate changes as the other two S3c action screens. Two things specific to this one:
 *
 * ## One repository, not two
 *
 * The original injected `ClientDetailsRepository` for the write AND `CreateNewGroupRepository`
 * for the office list — the group feature's repository, borrowed for its `offices()` method. The
 * office list is not group data; it is office data, and `OfficeRepository.officesStream` already
 * exists on this substrate for the screens that need it.
 *
 * ## The same date-format defect as `clientClosure`, fixed the same way
 *
 * `transferDate` was `DateHelper.getDateAsStringFromLong` (`dd-MM-yyyy`) while the request declared
 * `ApiDateFormatter.DATE_FORMAT` (`dd MMMM yyyy`) — a string Fineract was told to parse and could
 * not. The request uses [ApiDateFormatter.formatForApi]; the displayed date keeps the original's
 * format, because that is UI.
 */
internal class ClientTransferViewModel(
    offices: OfficeRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientTransferState, ClientTransferEvent, ClientTransferAction>(
    ClientTransferState(id = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    private val stream = offices.officesStream(viewModelScope)

    /** Destination offices. A tenant-level catalog, shared across clients. */
    val offices: StateFlow<ScreenState<List<Office>>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientTransferAction) {
        when (action) {
            ClientTransferAction.NavigateBack -> sendEvent(ClientTransferEvent.NavigateBack)
            ClientTransferAction.OnNext -> sendEvent(ClientTransferEvent.NavigateNext)
            ClientTransferAction.OnRetry -> stream.retry()

            is ClientTransferAction.OptionChanged ->
                mutableStateFlow.value = state.copy(currentSelectedIndex = action.index)

            is ClientTransferAction.NoteChanged ->
                mutableStateFlow.value = state.copy(note = action.note)

            is ClientTransferAction.UpdateDatePicker ->
                mutableStateFlow.value = state.copy(showDatePicker = action.status)

            is ClientTransferAction.UpdateDate ->
                mutableStateFlow.value = state.copy(date = action.date)

            ClientTransferAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            is ClientTransferAction.OnSubmit -> transfer(action.destinationOfficeId)
        }
    }

    private fun transfer(destinationOfficeId: Int) {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            val request = ProposeTransferRequest(
                destinationOfficeId = destinationOfficeId,
                transferDate = ApiDateFormatter.formatForApi(state.date),
                note = state.note,
                dateFormat = ApiDateFormatter.DATE_FORMAT,
                locale = ApiDateFormatter.LOCALE,
            )
            when (val result = commands.proposeTransfer(state.id, request)) {
                is MutationResult.Applied -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientTransferState.DialogState.ShowStatusDialog(ResultStatus.SUCCESS),
                )
                // The OFFICE list did not change, so the stream is not refreshed. What changed is
                // the CLIENT's transfer status, and the detail screen re-reads that when shown.

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientTransferState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "propose transfer failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = ClientTransferState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientTransferState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = FAILED_MSG,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "ClientTransfer"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val OFFLINE_MSG = "No connection — transfer request not sent."
        const val FAILED_MSG = "Could not propose this transfer."
    }
}

@OptIn(ExperimentalTime::class)
internal data class ClientTransferState(
    val id: Int = -1,
    val showDatePicker: Boolean = false,
    val currentSelectedIndex: Int = 0,
    val note: String = "",
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    /** The original's rule, kept: a transfer needs a note. */
    val isEnabled: Boolean get() = note.isNotEmpty() && !isSubmitting

    sealed interface DialogState {
        data class ShowStatusDialog(val status: ResultStatus, val msg: String = "") : DialogState
    }
}

internal sealed interface ClientTransferEvent {
    data object NavigateBack : ClientTransferEvent
    data object NavigateNext : ClientTransferEvent
}

internal sealed interface ClientTransferAction {
    data object NavigateBack : ClientTransferAction
    data object OnRetry : ClientTransferAction
    data object OnNext : ClientTransferAction
    data object CloseDialog : ClientTransferAction
    data class OptionChanged(val index: Int) : ClientTransferAction
    data class NoteChanged(val note: String) : ClientTransferAction
    data class UpdateDatePicker(val status: Boolean) : ClientTransferAction
    data class UpdateDate(val date: Long) : ClientTransferAction
    data class OnSubmit(val destinationOfficeId: Int) : ClientTransferAction
}
