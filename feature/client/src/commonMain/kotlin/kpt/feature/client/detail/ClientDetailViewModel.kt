/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientRepository
import kpt.core.database.client.entity.ClientAccounts
import kpt.core.database.client.entity.ClientEntity

/**
 * Client detail — ported from `6b66e8a43:feature/client/clientDetails/ClientDetailsViewModel.kt`.
 *
 * The original carried SIX independent `StateFlow`s for one screen — `clientDetailsUiState`,
 * `loanAccount`, `savingsAccounts`, `profileImage`, `client`, `showLoading` — which could disagree
 * about what was on screen. They become one [ClientDetailState] whose parts are `ScreenState`s, so
 * identity can render while accounts are still loading without either half lying about the other.
 *
 * Four defects in the original, fixed here:
 *
 *  1. `init` called `getUserProfile()` using `clientId.value` from `SavedStateHandle` — still `0` at
 *     construction, so the first profile-image fetch was for client 0. The id is now read ONCE, up
 *     front, and the streams are opened from it.
 *  2. `loadClientDetailsAndClientAccounts()` was PUBLIC and never called from `init`; the screen had
 *     to remember to invoke it, and nothing loaded if it did not. Loading is owned here.
 *  3. `getUserProfile()` swallowed `DataState.Error` into an empty `{}` branch, so a failed photo was
 *     silent. A missing photo is now [ScreenState.Empty] and a failed one is `Error` — the ledger's
 *     §5 decision that "no photo" and "could not load photo" are different facts.
 *  4. `_showLoading` was written by two concurrent collectors, so whichever finished first cleared
 *     the spinner while the other was still running. There is no shared loading flag now; each
 *     stream carries its own state.
 */
internal class ClientDetailViewModel(
    private val repository: ClientRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientDetailState, ClientDetailEvent, ClientDetailAction>(ClientDetailState()) {

    /**
     * Read once, at construction, from the route argument.
     *
     * `getStateFlow(…, initialValue = 0)` is what let the original fire its first request for client
     * 0; a type-safe route argument is present by the time the ViewModel exists.
     */
    private val clientId: Int = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: 0

    /**
     * Held, not just collected — the error state's Retry has to reach the stream that failed.
     * Collecting `.state` inline would leave nothing to call `retry()` on, and a Retry button wired
     * to `{}` is exactly the dead-clickable defect RULE-IMPL-DEAD-CLICKABLE-001 names.
     */
    private val clientStream = repository.clientStream(clientId, viewModelScope)
    private val accountsStream = repository.clientAccountsStream(clientId, viewModelScope)

    init {
        clientStream.state
            .onEach { mutableStateFlow.value = state.copy(client = it) }
            .launchIn(viewModelScope)

        accountsStream.state
            .onEach { mutableStateFlow.value = state.copy(accounts = it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: ClientDetailAction) {
        when (action) {
            is ClientDetailAction.LoanClicked -> sendEvent(ClientDetailEvent.OpenLoan(action.loanId))
            is ClientDetailAction.SavingsClicked -> sendEvent(ClientDetailEvent.OpenSavings(action.savingsId))
            ClientDetailAction.NotesClicked -> sendEvent(ClientDetailEvent.OpenNotes(clientId))
            ClientDetailAction.DocumentsClicked -> sendEvent(ClientDetailEvent.OpenDocuments(clientId))
            ClientDetailAction.ChargesClicked -> sendEvent(ClientDetailEvent.OpenCharges(clientId))
            ClientDetailAction.RetryClient -> clientStream.retry()
            ClientDetailAction.RetryAccounts -> accountsStream.retry()
        }
    }

    private companion object {
        /** Matches the route argument name in `ClientNavigation`. */
        const val CLIENT_ID_KEY = "clientId"
    }
}

/**
 * One state for the screen, with each read carrying its own [ScreenState].
 *
 * Identity and accounts load independently on purpose: a slow accounts call must not hold back the
 * client's name, which is already cached.
 */
internal data class ClientDetailState(
    val client: ScreenState<ClientEntity> = ScreenState.Loading,
    val accounts: ScreenState<ClientAccounts> = ScreenState.Loading,
)

internal sealed interface ClientDetailAction {
    data class LoanClicked(val loanId: Int) : ClientDetailAction
    data class SavingsClicked(val savingsId: Int) : ClientDetailAction
    data object NotesClicked : ClientDetailAction
    data object DocumentsClicked : ClientDetailAction
    data object ChargesClicked : ClientDetailAction

    /** Per-section retry: each read failed on its own, so each recovers on its own. */
    data object RetryClient : ClientDetailAction
    data object RetryAccounts : ClientDetailAction
}

internal sealed interface ClientDetailEvent {
    data class OpenLoan(val loanId: Int) : ClientDetailEvent
    data class OpenSavings(val savingsId: Int) : ClientDetailEvent
    data class OpenNotes(val clientId: Int) : ClientDetailEvent
    data class OpenDocuments(val clientId: Int) : ClientDetailEvent
    data class OpenCharges(val clientId: Int) : ClientDetailEvent
}
