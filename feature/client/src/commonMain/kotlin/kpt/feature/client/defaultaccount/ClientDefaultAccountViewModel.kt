/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.defaultaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.FormDraftSlot
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.store.submit.draft.isOffline
import kpt.core.base.store.submit.draft.offlineWhenUnsent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.client.ClientRepository
import kpt.core.model.client.SavingAccountOption
import kpt.core.model.draft.CreateForm
import kpt.core.model.objects.clients.UpdateSavingsAccountRequest
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.ui.components.ResultStatus

/**
 * Choose which of a client's savings accounts is their DEFAULT — ported from
 * `6b66e8a43:feature/client/clientUpdateDefaultAccount/UpdateDefaultAccountViewModel.kt`.
 *
 * The original was already on the `BaseViewModel<State, Event, Action>` triad, so the MVI shape,
 * the action set, the dialog-driven navigation and the copy all carry over. Four things underneath
 * it do not.
 *
 * ## 1. Both reads are streams, and the account list is the CLIENT TEMPLATE
 *
 * `repo.getSavingsAccounts(clientId)` looked like its own endpoint and was not — it called
 * `getClientTemplate(clientId)` and kept the `savingAccountOptions` field, exactly as its two
 * siblings did for `staffOptions` and `shareAccounts`. It is one [ClientRepository.clientTemplateStream]
 * here, projected with [mapContent], so this screen shares the cache with the assign-staff screen
 * instead of issuing a third identical uncached request.
 *
 * The client itself — needed only for [ClientRepository.clientStream]'s `savingsAccountId`, i.e.
 * which account is ALREADY the default — is the second stream.
 *
 * ## 2. The two reads are deliberately NOT combined
 *
 * `combineScreenStates` would be the obvious move and would be wrong. The original fetched the
 * client inside its OWN nested `try/catch` that did nothing but `Logger.e(...)`:
 *
 * ```kotlin
 * try { getClientSavingsAccountId() } catch (e: Exception) { Logger.e(...) }
 * ```
 *
 * so a client read that failed cost the officer the pre-selection and nothing else — the form
 * still worked. Combining the streams would promote that tolerated failure into a full-screen
 * error on a screen that does not need the client to function. [defaultAccountId] therefore reads
 * the client stream through a plain `map` to a nullable: no `Content` yet, or an error, means "no
 * pre-selection", which is precisely the original's degraded behaviour.
 *
 * ## 3. The selection is addressed by ACCOUNT ID, not by list position — FIXED
 *
 * The original tracked the choice as `currentSelectedIndex: Int` into a list it held in state, and
 * re-read `state.accounts[state.currentSelectedIndex].id` at submit time. Two faults fall out of
 * that, and this screen writes to a financial record:
 *
 *  - **Wrong-target write.** The index is a position in whatever list was loaded when the officer
 *    opened the dropdown. The list is a cached server read that can re-emit reordered or shorter
 *    (an account closed, a refresh landing mid-form). A held index then designates a DIFFERENT
 *    account, and Submit makes THAT one the client's default — silently, because the dropdown
 *    re-renders showing the newly-indexed account's number as if the officer had picked it.
 *  - **Crash.** `state.accounts[state.currentSelectedIndex]` is also read by the Screen's dropdown
 *    on every recomposition, with no bound check. A list that shrank under a held index throws
 *    `IndexOutOfBoundsException` on the way to drawing the form.
 *
 * [ClientDefaultAccountState.selectedAccountId] holds the id instead, and [selectionFor] resolves
 * it against the list actually on screen. An account that disappears falls back to the server's
 * current default and then to the first row, so there is always a coherent selection and never a
 * stale one. The submit id comes from the ACTION, so the account named on screen is the account
 * written.
 *
 * ## 4. The hand-rolled connectivity observer is gone, and `DialogState` loses two of three cases
 *
 * `networkMonitor.isOnline.collect { … networkConnection = it }` existed only to tell the error
 * component whether to blame the network; `ScreenState.NoNetwork` is decided by the stream for
 * every screen. Worse, the original re-subscribed it from `OnRetry` — `getAccountsAndObserveNetwork()`
 * launched a FRESH never-cancelled collector per retry, so the observers accumulated for the life
 * of the screen. Both the observer and the `networkConnection` field go.
 *
 * `DialogState.Loading` and `.Error` described the READ and were rendered as a full-screen spinner
 * and an error panel that replaced the form entirely; they are states of the content area now.
 * `ShowStatusDialog` stays — a submit result the officer must acknowledge before navigating really
 * is a dialog, and it is the one the original used to drive navigation.
 *
 * ## Checked and NOT a defect
 *
 * The write passes the client id as the path argument and the ACCOUNT id in the payload
 * (`updateDefaultSavingsAccount(route.clientId, accountId)` → `UpdateSavingsAccountRequest(accountId)`),
 * which is correct — unlike the deposit-accounts screens, where the approve action passed the
 * client id where an account id was required. The original's failure dialog also already routed
 * its button to `Dismiss` rather than to `OnNext`, so acknowledging a failure did not navigate
 * forward here. Both carry over unchanged.
 */
internal class ClientDefaultAccountViewModel(
    repository: ClientRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<ClientDefaultAccountState, ClientDefaultAccountEvent, ClientDefaultAccountAction>(
    ClientDefaultAccountState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    /** This client's ClientDefaultAccount write that did not land — resumable here, listed in Sync & Drafts. */
    private val formDraft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.DEFAULT_ACCOUNT.key,
        ownerId = state.clientId.toLong(),
        ClientDefaultAccountState.serializer(),
        UpdateSavingsAccountRequest.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered

    private var lastPayload: UpdateSavingsAccountRequest? = null

    private fun keepDraft(payload: UpdateSavingsAccountRequest, failure: String?) =
        formDraft.keep("Default savings account", state.copy(dialogState = null), payload, failure)

    private val templateStream = repository.clientTemplateStream(state.clientId, viewModelScope)
    private val clientStream = repository.clientStream(state.clientId, viewModelScope)

    /**
     * The savings accounts this client may nominate as their default.
     *
     * Projected from the template, not its own read. No `emptyIfContent`: a client with no savings
     * account is a real answer from the server rather than an absence of data, and the original
     * says so with its own inline message — so it stays `Content(emptyList())` and the screen
     * renders that copy, which is also what the assign-staff port does with an empty option set.
     */
    val accounts: StateFlow<ScreenState<List<SavingAccountOption>>> = templateStream.state
        .mapContent { template, _ -> template.savingAccountOptions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /**
     * The account that is the client's default RIGHT NOW, or `null` when it is unknown.
     *
     * Nullable rather than a `ScreenState`, because every non-`Content` case means the same thing
     * to this screen — pre-select nothing — and none of them should stop the officer choosing. See
     * point 2 of the class KDoc for why that matches the original rather than relaxing it.
     */
    val defaultAccountId: StateFlow<Long?> = clientStream.state
        .map { (it as? ScreenState.Content)?.data?.savingsAccountId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), null)

    override fun handleAction(action: ClientDefaultAccountAction) {
        when (action) {
            ClientDefaultAccountAction.ResumeDraft -> formDraft.resume()?.let { mutableStateFlow.value = it }
            ClientDefaultAccountAction.DiscardDraft -> formDraft.discard()
            ClientDefaultAccountAction.SaveDraft -> lastPayload?.let { payload ->
                val failed = state.dialogState as? ClientDefaultAccountState.DialogState.ShowStatusDialog
                keepDraft(payload, failure = failed?.msg ?: "Not accepted by the server.")
                mutableStateFlow.value = state.copy(dialogState = failed?.copy(draftSaved = true))
            }
            ClientDefaultAccountAction.NavigateBack ->
                sendEvent(ClientDefaultAccountEvent.NavigateBack)

            ClientDefaultAccountAction.OnNext -> sendEvent(ClientDefaultAccountEvent.NavigateNext)

            // Both reads, because the original's retry re-ran both the account load and the
            // client fetch. Unlike the original it does NOT also re-subscribe a network observer.
            ClientDefaultAccountAction.OnRetry -> {
                templateStream.retry()
                clientStream.retry()
            }

            is ClientDefaultAccountAction.OptionChanged ->
                mutableStateFlow.value = state.copy(selectedAccountId = action.accountId)

            is ClientDefaultAccountAction.OnSave -> save(action.accountId)

            ClientDefaultAccountAction.Dismiss ->
                mutableStateFlow.value = state.copy(dialogState = null)
        }
    }

    /**
     * Make [accountId] the client's default savings account.
     *
     * The id arrives on the action rather than being re-derived here. The original looked the
     * account up a second time — `state.accounts[state.currentSelectedIndex].id` — at the moment
     * of submit, against a list that may have re-emitted since the officer chose; see point 3 of
     * the class KDoc. The screen has the chosen account in hand and passes its id.
     */
    private fun save(accountId: Long) {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            val payload = UpdateSavingsAccountRequest(savingsAccountId = accountId)
            lastPayload = payload
            val result = commands.updateSavingsAccount(clientId = state.clientId, payload = payload).offlineWhenUnsent()
            when (result) {
                is MutationResult.Applied -> {
                    formDraft.clear()
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = ClientDefaultAccountState.DialogState.ShowStatusDialog(
                            ResultStatus.SUCCESS,
                        ),
                    )
                    // The default account is a field ON THE CLIENT (`savingsAccountId`), so the
                    // client is re-read. Without this, dismissing the dialog and coming back shows
                    // the PREVIOUS default pre-selected from cache — the screen would contradict
                    // the write it just reported as successful.
                    clientStream.refresh(forceFresh = true)
                }

                is MutationResult.Blocked -> {
                    if (result.isOffline) keepDraft(payload, failure = null)
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = ClientDefaultAccountState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                            offline = result.isOffline,
                            draftSaved = result.isOffline,
                        ),
                    )
                }

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "update default account failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = ClientDefaultAccountState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            // The original surfaced the server's own message as the dialog body
                            // (it extracted the Fineract error body in the repository). Kept: a
                            // rejection reason is the only actionable thing the officer gets.
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientDefaultAccountState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = FAILED_MSG,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "ClientDefaultAccount"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val OFFLINE_MSG = "No connection — the default account was not changed."
        const val FAILED_MSG = "Could not update the default account."
    }
}

/**
 * Which account the dropdown shows, given what the officer picked and what the server has.
 *
 * Replaces the original's `state.accounts[state.currentSelectedIndex]`. Three tiers, in order:
 * the officer's own choice, then the client's existing default (the original's
 * `getClientSavingsAccountId()` pre-selection), then the first row (its `currentSelectedIndex = 0`
 * fallback when no default matched). `null` only when the list is empty, which the screen renders
 * as the original's "no savings accounts" message.
 *
 * Addressing by id is what makes the selection survive a list that re-emits reordered: an index
 * would silently move to another account, and this screen's Submit writes whatever is selected.
 * An account that has disappeared from the list falls back rather than crashing.
 *
 * Pure and `internal` so the tiering is tested directly, without a ViewModel or Compose.
 */
internal fun List<SavingAccountOption>.selectionFor(
    chosenId: Long?,
    defaultId: Long?,
): SavingAccountOption? = firstOrNull { it.id == chosenId }
    ?: firstOrNull { it.id == defaultId }
    ?: firstOrNull()

/**
 * Screen state.
 *
 * `accounts` is NOT here — it is on the stream, and neither is the original's `networkConnection`.
 * [selectedAccountId] replaces `currentSelectedIndex`; `null` means "the officer has not chosen
 * yet", which is how the server's current default gets to win the first render.
 */
/** Serializable so a draft restores the form as left; the transient fields are per-visit. */
@Serializable
internal data class ClientDefaultAccountState(
    val clientId: Int = -1,
    val selectedAccountId: Long? = null,
    @Transient val isSubmitting: Boolean = false,
    @Transient val dialogState: DialogState? = null,
) {
    /** One case, where the original had three — the other two described the read. */
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

internal sealed interface ClientDefaultAccountEvent {
    data object NavigateBack : ClientDefaultAccountEvent

    /** Acknowledging the SUCCESS dialog leaves the form. Carries no id — the host knows the client. */
    data object NavigateNext : ClientDefaultAccountEvent
}

internal sealed interface ClientDefaultAccountAction {
    data object ResumeDraft : ClientDefaultAccountAction
    data object DiscardDraft : ClientDefaultAccountAction
    data object SaveDraft : ClientDefaultAccountAction
    data object NavigateBack : ClientDefaultAccountAction
    data object OnRetry : ClientDefaultAccountAction
    data object OnNext : ClientDefaultAccountAction
    data object Dismiss : ClientDefaultAccountAction

    /** The officer picked a row. Carries the account's ID, where the original carried its INDEX. */
    data class OptionChanged(val accountId: Long) : ClientDefaultAccountAction

    /** Submit. Carries the id of the account shown as selected, so the write targets that one. */
    data class OnSave(val accountId: Long) : ClientDefaultAccountAction
}
