/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.shareaccounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientRepository
import kpt.core.model.objects.account.share.ShareAccounts

/**
 * A client's SHARE accounts — ported from
 * `6b66e8a43:feature/client/shareAccounts/{ShareAccountsScreen,ShareAccountsViewModel}.kt`.
 *
 * Reached from the client GENERAL screen's "Share Accounts" row, which was `null`-gated (so not
 * drawn) until this landed. It is the last of that screen's five account rows to go live.
 *
 * ## Why this is NOT folded into the deposit-accounts screen
 *
 * Its three siblings — savings, fixed deposit, recurring deposit — collapsed into one
 * parameterised destination because they read the same `clientAccounts.savingsAccounts` list and
 * differed only by a `ServerTypes` constant. Share accounts are a different ENTITY
 * ([ShareAccounts], not `SavingsAccountEntity`) with different fields: approved and
 * pending-for-approval share COUNTS rather than a balance, and no deposit type at all. There is no
 * discriminator that would make it the same screen, so it is its own.
 *
 * ## One request fewer than the original
 *
 * The original called `repository.getShareAccounts(clientId)`, a third endpoint alongside the
 * client read and the savings read. `clientAccountsStream` already carries `shareAccounts` on the
 * SAME `ClientAccounts` payload the deposit screens read, so this screen adds no request at all —
 * and it shares their cache, so arriving here after visiting savings is instant and works offline.
 *
 * ## "View Account" does nothing, and that is the original's behaviour
 *
 * The original's graph wires `shareAccountsDestination(navigateToViewAccount = {})` — an EMPTY
 * LAMBDA at the call site. There is no share-account detail screen in the original to open, so the
 * action was dead there too.
 *
 * It is KEPT, drawn and inert, rather than removed. §6a's `null`-the-callback rule covers
 * destinations THIS PORT has not reached yet; a control that is dead in the original is a
 * pre-existing UI defect, which §6a keeps verbatim and records. Tracked as
 * `share-account-view-and-pay-are-dead-in-the-original`.
 */
internal class ClientShareAccountsViewModel(
    repository: ClientRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientShareAccountsState, ClientShareAccountsEvent, ClientShareAccountsAction>(
    ClientShareAccountsState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    private val stream = repository.clientAccountsStream(state.clientId, viewModelScope)

    /** This client's share accounts, with the query applied. */
    val accounts: StateFlow<ScreenState<List<ShareAccounts>>> = stream.state
        .combineContent(stateFlow) { clientAccounts, ui, _ ->
            clientAccounts.shareAccounts.matching(ui.searchText)
        }
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /**
     * How many share accounts the client has, before the query.
     *
     * Off the UNFILTERED list, for the reason the deposit-accounts port documents: a header that
     * counts the filtered list reports the search's own result, so the number that should warn an
     * officer their search is too narrow reads "0" beside an empty screen.
     */
    val totalCount: StateFlow<Int> = stream.state
        .map { screen -> (screen as? ScreenState.Content)?.data?.shareAccounts?.size ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), 0)

    override fun handleAction(action: ClientShareAccountsAction) {
        when (action) {
            ClientShareAccountsAction.NavigateBack ->
                sendEvent(ClientShareAccountsEvent.NavigateBack)

            ClientShareAccountsAction.Refresh -> stream.refresh(forceFresh = true)

            ClientShareAccountsAction.ToggleSearch -> mutableStateFlow.value = state.copy(
                isSearchBarActive = !state.isSearchBarActive,
                searchText = if (state.isSearchBarActive) "" else state.searchText,
            )

            is ClientShareAccountsAction.UpdateSearchValue ->
                mutableStateFlow.value = state.copy(searchText = action.query)

            is ClientShareAccountsAction.ViewAccount ->
                sendEvent(ClientShareAccountsEvent.ViewAccount(action.accountId))

            ClientShareAccountsAction.AddAccount ->
                sendEvent(ClientShareAccountsEvent.AddAccount(state.clientId))
        }
    }

    internal companion object {
        const val CLIENT_ID_KEY = "clientId"
        private const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * Apply the query to the account NUMBER, case-insensitively.
 *
 * The original had no search over this list at all — its search bar toggled but filtered nothing
 * (`toggleSearchBar()` flips a flag no read consults). Filtering the stream is the same derivation
 * the loan- and deposit-account lists use, so the control now does what it appears to do rather
 * than being removed.
 */
private fun List<ShareAccounts>.matching(query: String): List<ShareAccounts> {
    val q = query.trim()
    return if (q.isEmpty()) this else filter { it.accountNo?.contains(q, ignoreCase = true) == true }
}

internal data class ClientShareAccountsState(
    val clientId: Int = -1,
    val isSearchBarActive: Boolean = false,
    val searchText: String = "",
) {
    val isFiltering: Boolean get() = searchText.isNotBlank()
}

internal sealed interface ClientShareAccountsEvent {
    data object NavigateBack : ClientShareAccountsEvent

    /** Dead in the original — see the class KDoc. */
    data class ViewAccount(val accountId: Int) : ClientShareAccountsEvent
    data class AddAccount(val clientId: Int) : ClientShareAccountsEvent
}

internal sealed interface ClientShareAccountsAction {
    data object NavigateBack : ClientShareAccountsAction
    data object Refresh : ClientShareAccountsAction
    data object ToggleSearch : ClientShareAccountsAction
    data object AddAccount : ClientShareAccountsAction
    data class UpdateSearchValue(val query: String) : ClientShareAccountsAction
    data class ViewAccount(val accountId: Int) : ClientShareAccountsAction
}
