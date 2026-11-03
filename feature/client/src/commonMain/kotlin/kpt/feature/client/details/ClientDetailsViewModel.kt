/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.details

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
import kpt.core.data.client.ClientRepository
import kpt.core.model.client.Client
import kpt.core.model.objects.clients.AssignStaffRequest
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.ui.components.ResultStatus
import kpt.core.ui.util.imageToByteArray
import kpt.feature.client.details.components.ClientDetailsActionItem

/**
 * The client's DETAIL fields plus its eight actions — what the profile screen's `ProfileCard` taps
 * through to. Ported from
 * `6b66e8a43:feature/client/clientDetailsProfile/ClientProfileDetailsViewModel.kt`.
 *
 * This is the screen that hosts the S3c action forms: Closure, Transfer, Assign Staff and Create
 * Collateral all route from here, so porting it is what makes those four reachable.
 *
 * ## Substrate changes
 *
 * `GetClientDetailsUseCase` → `clientStream`; the `networkMonitor` observer deleted; the read's
 * Loading/Error out of `DialogState`. Same three as every other screen in this slice.
 *
 * The photo is a one-shot blob, as on the profile screen.
 *
 * ## The AssignStaff row does two different things
 *
 * This is the one piece of real behaviour on the screen and it is carried over exactly: if the
 * client ALREADY has a staff member, tapping "Assign Staff" does not navigate — it raises the
 * unassign confirmation, and confirming sends `unassignStaff`. Only an unstaffed client goes to
 * the assign form. The dialog's copy says why (Fineract needs a few minutes before a new
 * assignment takes).
 *
 * ## Details rows are DERIVED
 *
 * `buildClientDetails` wrote three `Map<StringResource, String>` into the state on every load. They
 * are computed from the client here, so they cannot disagree with the record they describe. The
 * original resolved the "N/A" fallback with a suspending `getString` inside the ViewModel; the
 * fallback is applied in the SCREEN instead, where resolving a string resource is free — which is
 * also why `buildClientDetails` had to be `suspend` at all.
 */
internal class ClientDetailsViewModel(
    private val repository: ClientRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientDetailsState, ClientDetailsEvent, ClientDetailsAction>(
    ClientDetailsState(),
) {

    private val clientId: Int = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1

    private val stream = repository.clientStream(clientId, viewModelScope)

    val client: StateFlow<ScreenState<Client>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    init {
        loadImage()
    }

    private fun loadImage() {
        viewModelScope.launch {
            mutableStateFlow.value = state.copy(profileImage = imageToByteArray(repository.clientImage(clientId)))
        }
    }

    override fun handleAction(action: ClientDetailsAction) {
        when (action) {
            ClientDetailsAction.NavigateBack -> sendEvent(ClientDetailsEvent.NavigateBack)

            is ClientDetailsAction.OnActionClick -> {
                // The original's branch, kept: an already-staffed client raises the UNASSIGN
                // confirmation rather than navigating to the assign form.
                if (action.action == ClientDetailsActionItem.AssignStaff && action.hasStaff) {
                    mutableStateFlow.value =
                        state.copy(dialogState = ClientDetailsState.DialogState.UnAssignStaff(action.staffId))
                } else {
                    sendEvent(ClientDetailsEvent.OnActionClick(action.action, clientId))
                }
            }

            ClientDetailsAction.OnUpdateDetailsClick ->
                sendEvent(ClientDetailsEvent.NavigateToUpdateDetails(clientId))

            is ClientDetailsAction.OnUpdatePhotoClick ->
                sendEvent(ClientDetailsEvent.NavigateToUpdatePhoto(clientId, action.name, action.accountNo))

            is ClientDetailsAction.OnUpdateSignatureClick ->
                sendEvent(ClientDetailsEvent.NavigateToUpdateSignature(clientId, action.name, action.accountNo))

            ClientDetailsAction.OnActivateClick -> sendEvent(ClientDetailsEvent.NavigateToActivate(clientId))

            ClientDetailsAction.DismissDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            // The original's OnNext re-ran the whole load. The staff change is what went stale, and
            // it lives on the client record this screen reads.
            ClientDetailsAction.OnNext -> {
                mutableStateFlow.value = state.copy(dialogState = null)
                stream.refresh(forceFresh = true)
            }

            ClientDetailsAction.OnRetry -> {
                stream.retry()
                loadImage()
            }

            is ClientDetailsAction.ConfirmUnAssignStaff -> unassign(action.staffId)
        }
    }

    private fun unassign(staffId: Int) {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            when (val result = commands.unassignStaff(clientId, AssignStaffRequest(staffId = staffId))) {
                is MutationResult.Applied -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientDetailsState.DialogState.ShowStatusDialog(ResultStatus.SUCCESS),
                )
                // NOT refreshed here — the success dialog's button is `OnNext`, which refreshes.
                // Refreshing twice would re-read while the dialog is still up.

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientDetailsState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "unassign staff failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = ClientDetailsState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientDetailsState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = FAILED_MSG,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "ClientDetails"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val OFFLINE_MSG = "No connection — the staff member was not unassigned."
        const val FAILED_MSG = "Could not unassign this staff member."
    }
}

internal data class ClientDetailsState(
    val profileImage: ByteArray? = null,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    /**
     * Two cases, where the original had four. `Loading` and `Error` described the READ and are the
     * content area's now; these two are genuinely dialogs — a destructive confirmation and a
     * submit result the officer acknowledges.
     */
    sealed interface DialogState {
        data class UnAssignStaff(val staffId: Int) : DialogState
        data class ShowStatusDialog(val status: ResultStatus, val msg: String = "") : DialogState
    }

    // ByteArray in a data class compares by identity — see ClientProfileState.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientDetailsState) return false
        return profileImage.contentEquals(other.profileImage) &&
            isSubmitting == other.isSubmitting &&
            dialogState == other.dialogState
    }

    override fun hashCode(): Int {
        var result = profileImage?.contentHashCode() ?: 0
        result = 31 * result + isSubmitting.hashCode()
        result = 31 * result + (dialogState?.hashCode() ?: 0)
        return result
    }
}

internal sealed interface ClientDetailsEvent {
    data object NavigateBack : ClientDetailsEvent
    data class OnActionClick(val action: ClientDetailsActionItem, val clientId: Int) : ClientDetailsEvent
    data class NavigateToUpdateDetails(val clientId: Int) : ClientDetailsEvent
    data class NavigateToUpdatePhoto(val clientId: Int, val name: String, val accountNo: String) : ClientDetailsEvent
    data class NavigateToUpdateSignature(
        val clientId: Int,
        val name: String,
        val accountNo: String,
    ) : ClientDetailsEvent
    data class NavigateToActivate(val clientId: Int) : ClientDetailsEvent
}

internal sealed interface ClientDetailsAction {
    data object NavigateBack : ClientDetailsAction
    data object OnRetry : ClientDetailsAction
    data object OnNext : ClientDetailsAction
    data object DismissDialog : ClientDetailsAction
    data object OnUpdateDetailsClick : ClientDetailsAction

    /** The original's bottom-bar "Activate Client", shown only while the client is not active. */
    data object OnActivateClick : ClientDetailsAction

    /**
     * `hasStaff` and `staffId` come from the SCREEN, which holds the client the stream emitted.
     * The original read `state.client?.staffName` and `state.client!!.staffId` inside the
     * ViewModel — the `!!` on a nullable field of a record it did not own.
     */
    data class OnActionClick(
        val action: ClientDetailsActionItem,
        val hasStaff: Boolean = false,
        val staffId: Int = 0,
    ) : ClientDetailsAction

    data class ConfirmUnAssignStaff(val staffId: Int) : ClientDetailsAction
    data class OnUpdatePhotoClick(val name: String, val accountNo: String) : ClientDetailsAction
    data class OnUpdateSignatureClick(val name: String, val accountNo: String) : ClientDetailsAction
}
