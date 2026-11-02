/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.collateral

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.FormDraftSlot
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.store.submit.draft.isOffline
import kpt.core.base.store.submit.draft.offlineWhenUnsent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.client.ClientRepository
import kpt.core.model.draft.CreateForm
import kpt.core.model.objects.clients.CollateralPayload
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.shared.CollateralItem
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kpt.core.ui.util.TextFieldsValidator
import org.jetbrains.compose.resources.StringResource

/**
 * Pledge collateral against a client — ported from
 * `6b66e8a43:feature/client/clientCollateral/ClientCollateralViewModel.kt`.
 *
 * Same substrate changes as the other S3c action screens. Two defects are FIXED rather than
 * carried, both crashes rather than UI choices:
 *
 * ## 1. `quantity.toInt()` on every keystroke
 *
 * `OnQuantityChange` computed `currentCollateral.basePrice * action.quantity.toInt()` with no
 * guard. The field is free text: the first keystroke after clearing it, or any paste containing a
 * non-digit, threw `NumberFormatException` inside `handleAction`. The `isEmpty()` checks on the
 * next two lines look like they cover it and do not — `toInt()` has already thrown by then. The
 * computation uses `toIntOrNull()` here and treats un-parseable input as zero, which is what those
 * `isEmpty()` branches were reaching for.
 *
 * ## 2. The totals were derived state stored as fields
 *
 * `total` and `totalCollateral` were written into the state on every keystroke from the selected
 * item and the quantity — so changing the SELECTED TYPE left both showing the previous type's
 * figures until the quantity was edited again. (The screen half-knew: its dropdown's
 * `onValueChanged` re-sent `OnQuantityChange(state.quantity)` to force a recompute.) They are
 * computed from `(item, quantity)` here, so they cannot disagree with the selection.
 *
 * ## Carried over: the quantity VALIDATION, unchanged
 *
 * `TextFieldsValidator.numberValidator` on submit, with the error cleared on edit. The original's
 * behaviour, its strings, its timing.
 */
internal class ClientCollateralViewModel(
    repository: ClientRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<ClientCollateralState, ClientCollateralEvent, ClientCollateralAction>(
    ClientCollateralState(id = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    /** This client's ClientCollateral write that did not land — resumable here, listed in Sync & Drafts. */
    private val formDraft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.COLLATERAL.key,
        ownerId = state.id.toLong(),
        ClientCollateralState.serializer(),
        CollateralPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered

    private var lastPayload: CollateralPayload? = null

    private fun keepDraft(payload: CollateralPayload, failure: String?) =
        formDraft.keep("Collateral × ${state.quantity}", state.copy(dialogState = null), payload, failure)

    private val stream = repository.collateralItemsStream(viewModelScope)

    /** The collateral TYPES the tenant offers. A catalog, shared across clients. */
    val collaterals: StateFlow<ScreenState<List<CollateralItem>>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientCollateralAction) {
        when (action) {
            ClientCollateralAction.ResumeDraft -> formDraft.resume()?.let { mutableStateFlow.value = it }
            ClientCollateralAction.DiscardDraft -> formDraft.discard()
            ClientCollateralAction.SaveDraft -> lastPayload?.let { payload ->
                val failed = state.dialogState as? ClientCollateralState.DialogState.ShowStatusDialog
                keepDraft(payload, failure = failed?.msg ?: "Not accepted by the server.")
                mutableStateFlow.value = state.copy(dialogState = failed?.copy(draftSaved = true))
            }
            ClientCollateralAction.NavigateBack -> sendEvent(ClientCollateralEvent.NavigateBack)
            ClientCollateralAction.OnNext -> sendEvent(ClientCollateralEvent.NavigateNext)
            ClientCollateralAction.OnRetry -> stream.retry()

            is ClientCollateralAction.OptionChanged ->
                mutableStateFlow.value = state.copy(currentSelectedIndex = action.index)

            // Error cleared on edit, like the original. The totals are NOT written here — see the
            // header; they are derived by the screen from (item, quantity).
            is ClientCollateralAction.OnQuantityChange ->
                mutableStateFlow.value = state.copy(quantity = action.quantity, quantityError = null)

            ClientCollateralAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            is ClientCollateralAction.OnSave -> save(action.collateralId)
        }
    }

    private fun save(collateralId: Int) {
        val quantityError = TextFieldsValidator.numberValidator(state.quantity)
        if (quantityError != null) {
            mutableStateFlow.value = state.copy(quantityError = quantityError)
            return
        }
        if (state.isOverlayLoading) return

        mutableStateFlow.value = state.copy(isOverlayLoading = true, dialogState = null)
        viewModelScope.launch {
            val payload = CollateralPayload(
                collateralId = collateralId,
                quantity = state.quantity,
                // `CollateralPayload` carries no date, but it does carry a locale, and Fineract
                // parses the quantity against it. Taken from the same place every other request
                // takes it so the two cannot drift.
                locale = ApiDateFormatter.LOCALE,
            )
            lastPayload = payload
            when (val result = commands.createCollateral(state.id, payload).offlineWhenUnsent()) {
                is MutationResult.Applied -> {
                    formDraft.clear()
                    mutableStateFlow.value = state.copy(
                        isOverlayLoading = false,
                        dialogState = ClientCollateralState.DialogState.ShowStatusDialog(ResultStatus.SUCCESS),
                    )
                }
                // The TYPE catalog did not change, so this stream is not refreshed. What changed is
                // the client's pledged collateral, which `clientCollateralStream` owns and the
                // collateral-DETAIL screen re-reads when shown.

                is MutationResult.Blocked -> {
                    if (result.isOffline) keepDraft(payload, failure = null)
                    mutableStateFlow.value = state.copy(
                        isOverlayLoading = false,
                        dialogState = ClientCollateralState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                            offline = result.isOffline,
                            draftSaved = result.isOffline,
                        ),
                    )
                }

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create collateral failed" }
                    mutableStateFlow.value = state.copy(
                        isOverlayLoading = false,
                        dialogState = ClientCollateralState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isOverlayLoading = false,
                    dialogState = ClientCollateralState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = FAILED_MSG,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "ClientCollateral"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val OFFLINE_MSG = "No connection — collateral not saved."
        const val FAILED_MSG = "Could not save this collateral."
    }
}

/** Serializable so a draft restores the form as left; the transient fields are per-visit. */
@Serializable
internal data class ClientCollateralState(
    val id: Int = -1,
    val quantity: String = "",
    @Transient val quantityError: StringResource? = null,
    val currentSelectedIndex: Int = 0,
    @Transient val isOverlayLoading: Boolean = false,
    @Transient val dialogState: DialogState? = null,
) {
    sealed interface DialogState {
        /** [draftSaved] = kept (offline automatically, or "Save as draft"), in Sync & Drafts. */
        data class ShowStatusDialog(
            val status: ResultStatus,
            val msg: String = "",
            val offline: Boolean = false,
            val draftSaved: Boolean = false,
        ) : DialogState
    }
}

/**
 * The two derived figures the form shows, computed from the SELECTED item and the typed quantity.
 *
 * A function, not state. The original stored both and recomputed them only on a quantity keystroke,
 * so switching the collateral TYPE left the previous type's figures on screen — which the screen
 * worked around by re-firing `OnQuantityChange` from the dropdown's `onValueChanged`.
 *
 * `toIntOrNull() ?: 0` is the crash fix: the original's `toInt()` threw on an empty or non-digit
 * field, on every keystroke.
 */
internal fun collateralTotals(item: CollateralItem, quantity: String): Pair<Double, Double> {
    val qty = quantity.toIntOrNull() ?: return 0.0 to 0.0
    val total = item.basePrice * qty
    return total to (total * item.pctToBase) / 100
}

internal sealed interface ClientCollateralEvent {
    data object NavigateBack : ClientCollateralEvent
    data object NavigateNext : ClientCollateralEvent
}

internal sealed interface ClientCollateralAction {
    data object ResumeDraft : ClientCollateralAction
    data object DiscardDraft : ClientCollateralAction
    data object SaveDraft : ClientCollateralAction
    data object NavigateBack : ClientCollateralAction
    data object OnRetry : ClientCollateralAction
    data object OnNext : ClientCollateralAction
    data object CloseDialog : ClientCollateralAction
    data class OptionChanged(val index: Int) : ClientCollateralAction
    data class OnQuantityChange(val quantity: String) : ClientCollateralAction
    data class OnSave(val collateralId: Int) : ClientCollateralAction
}
