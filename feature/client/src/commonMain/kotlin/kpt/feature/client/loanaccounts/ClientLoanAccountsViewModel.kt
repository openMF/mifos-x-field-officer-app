/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.loanaccounts

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
import kpt.core.model.loan.LoanAccount
import kpt.core.model.loan.LoanStatus

/**
 * A client's loan accounts — ported from
 * `6b66e8a43:feature/client/clientLoanAccounts/{ClientLoanAccountsViewModel,Screen}.kt`.
 *
 * ## This screen is what makes the loan vertical reachable from the CLIENT side
 *
 * It is the only caller of the loan ACCOUNT PROFILE in the original
 * (`clientLoanAccountsDestination(navigateToViewAccount = navigateToLoanAccountProfileScreen)`).
 * Until it landed, `feature/loan`'s profile had no caller — see
 * `loan-vertical-unreachable` in CAPABILITY_GAPS and the spec's
 * "Verify the PATH into the screen" rule, which this port is the first to follow.
 *
 * It is reached from the client GENERAL screen's "Loan Account" row, which was `null`-gated (so
 * not drawn) until now.
 *
 * ## FIXED: the filters destroyed the list they filter
 *
 * The original stored the SAME list in both fields:
 *
 * ```kotlin
 * val loanAccounts = repository.getClientAccounts(clientId).loanAccounts
 *     .filter { it.accountNo?.contains(state.searchText.trim()) == true }
 * …copy(loanAccounts = loanAccounts, unfilteredLoanAccounts = loanAccounts)
 * ```
 *
 * `unfilteredLoanAccounts` was already search-filtered, so "clear filters" restored the SEARCH
 * result rather than the full list, and a status filter applied on top of a search could never be
 * widened back. Here the stream is the single source and BOTH the query and the status set are
 * applied as a derivation over it — the codebase's established `ScreenState` filter idiom. Nothing
 * is stored twice, so nothing can disagree.
 *
 * ## FIXED: a refresh silently cleared the officer's status filter
 *
 * Every load did `selectedStatus = emptySet()`. With a stream that re-reads on reconnect and on
 * pull-to-refresh, that would wipe the filter repeatedly and apparently at random. Because the
 * filter is now a derivation, a refresh re-applies it instead.
 *
 * ## The `networkMonitor` gate is gone, as on every other ported screen
 *
 * `networkMonitor.isOnline.first()` refused to even TRY when it believed itself offline, showing
 * "No internet connection, Try Again" over an empty screen while a perfectly good cached copy of
 * the accounts sat in the database. The stream serves cache and reports its own freshness.
 */
internal class ClientLoanAccountsViewModel(
    repository: ClientRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientLoanAccountsState, ClientLoanAccountsEvent, ClientLoanAccountsAction>(
    ClientLoanAccountsState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    private val stream = repository.clientAccountsStream(state.clientId, viewModelScope)

    /**
     * The accounts, with the query and the status filters applied.
     *
     * Derived from the stream and the UI state together, so the list on screen is always the
     * current read filtered by the current selection — there is no second copy to go stale.
     * `emptyIfContent` AFTER filtering, so an over-narrow filter shows the Empty state rather than
     * a header reading "0 items" above nothing.
     */
    val accounts: StateFlow<ScreenState<List<LoanAccount>>> = stream.state
        // `combineContent` is the substrate's own filter/sort seam — "Combines ScreenState with a
        // local state flow for reactive filter/sort/preferences" — so Loading, NoNetwork, Error and
        // Unauthenticated pass through untouched and only Content is filtered.
        .combineContent(stateFlow) { accounts, ui, _ -> accounts.loanAccounts.filtered(ui) }
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /**
     * How many accounts the client has in total, regardless of filtering.
     *
     * The header's count came off the FILTERED list in the original, which made it read "0 items"
     * whenever a filter matched nothing — so the one number that could have told the officer the
     * filter was too narrow reported the filter's own result.
     */
    val totalCount: StateFlow<Int> = stream.state
        .map { state -> (state as? ScreenState.Content)?.data?.loanAccounts?.size ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), 0)

    override fun handleAction(action: ClientLoanAccountsAction) {
        when (action) {
            ClientLoanAccountsAction.NavigateBack -> sendEvent(ClientLoanAccountsEvent.NavigateBack)

            // Both the original's `Refresh` and its `OnSearchClick` re-fetched from the server.
            // Searching no longer needs to: the query filters the stream locally, so the only
            // reason to re-read is to get fresher data.

            ClientLoanAccountsAction.Refresh -> stream.refresh(forceFresh = true)

            ClientLoanAccountsAction.ToggleSearch -> mutableStateFlow.value = state.copy(
                isSearchBarActive = !state.isSearchBarActive,
                // Closing the search clears the query. The original left the text in place while
                // hiding the field, so a hidden query kept filtering the list with nothing on
                // screen to explain why.
                searchText = if (state.isSearchBarActive) "" else state.searchText,
            )

            is ClientLoanAccountsAction.UpdateSearchValue ->
                mutableStateFlow.value = state.copy(searchText = action.query)

            ClientLoanAccountsAction.ToggleFilter ->
                mutableStateFlow.value = state.copy(isFilterDialogOpen = !state.isFilterDialogOpen)

            is ClientLoanAccountsAction.HandleFilterClick -> mutableStateFlow.value = state.copy(
                selectedStatus = state.selectedStatus.toggle(action.status),
            )

            ClientLoanAccountsAction.ClearFilters ->
                mutableStateFlow.value = state.copy(selectedStatus = emptySet())

            is ClientLoanAccountsAction.ViewAccount ->
                sendEvent(ClientLoanAccountsEvent.ViewAccount(action.loanId))

            is ClientLoanAccountsAction.MakeRepayment ->
                sendEvent(ClientLoanAccountsEvent.MakeRepayment(action.loanId))

            ClientLoanAccountsAction.AddAccount ->
                sendEvent(ClientLoanAccountsEvent.AddAccount(state.clientId))
        }
    }

    private companion object {
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

/**
 * Apply the query and the status filters.
 *
 * The query matches the ACCOUNT NUMBER only, case-insensitively. The original matched account
 * number only too (its own `// Todo modify search accordingly` says so) but case-SENSITIVELY, which
 * matters because Fineract account numbers can carry a letter prefix. Widening it to the product
 * name is a product decision, not a porting one — logged as
 * `client-loan-accounts-search-is-account-no-only`.
 */
internal fun List<LoanAccount>.filtered(ui: ClientLoanAccountsState): List<LoanAccount> {
    val query = ui.searchText.trim()
    return asSequence()
        .filter { query.isEmpty() || it.accountNo?.contains(query, ignoreCase = true) == true }
        .filter { loan ->
            // An EMPTY selection means "every status", not "no statuses" — the original's
            // behaviour, and the reason the set is the right shape for this rather than a nullable
            // single value.
            ui.selectedStatus.isEmpty() || ui.selectedStatus.any { it.matches(loan.status) }
        }
        .toList()
}

/**
 * The four status buckets the filter sheet offers, with the original's predicates.
 *
 * Note that these do NOT partition `LoanStatus`: `waitingForDisbursal` matches none of the
 * four, so an approved-but-undisbursed loan disappears from the list under ANY active filter. That
 * is the original's behaviour and it is kept — adding a fifth bucket is a product decision. Logged
 * as `client-loan-accounts-filters-do-not-cover-awaiting-disbursal`.
 */
internal enum class LoanStatusFilter {
    ACTIVE,
    PENDING,
    CLOSED,
    OVERPAID,
    ;

    fun matches(status: LoanStatus?): Boolean {
        status ?: return false
        return when (this) {
            ACTIVE -> status.active == true
            PENDING -> status.pendingApproval == true
            OVERPAID -> status.overpaid == true
            // All four closed variants, as the original's `isActuallyClosed` had them.
            CLOSED ->
                status.closed == true ||
                    status.closedObligationsMet == true ||
                    status.closedRescheduled == true ||
                    status.closedWrittenOff == true
        }
    }
}

internal data class ClientLoanAccountsState(
    val clientId: Int = -1,
    val isSearchBarActive: Boolean = false,
    val searchText: String = "",
    val selectedStatus: Set<LoanStatusFilter> = emptySet(),
    val isFilterDialogOpen: Boolean = false,
) {
    /**
     * Whether anything is narrowing the list right now.
     *
     * The screen needs this to tell its two empty states apart: "this client has no loan accounts"
     * offers to create one, "nothing matches your filter" must not — creating an account would not
     * reveal the accounts the filter is hiding. The original had one empty state for both and
     * offered the create button in each.
     */
    val isFiltering: Boolean get() = searchText.isNotBlank() || selectedStatus.isNotEmpty()
}

internal sealed interface ClientLoanAccountsEvent {
    data object NavigateBack : ClientLoanAccountsEvent
    data class ViewAccount(val loanId: Int) : ClientLoanAccountsEvent
    data class MakeRepayment(val loanId: Int) : ClientLoanAccountsEvent

    /**
     * Create a loan account for this client.
     *
     * Carries the client id ONLY. The original's event also carried the client's `accountNo`, which
     * it fetched with a SECOND request at tap time (`repository.getClient(clientId)`) — un-guarded,
     * so a tap while offline threw out of `handleAction`. The create-account screen takes a client
     * id; whatever it needs beyond that, it can read itself.
     */
    data class AddAccount(val clientId: Int) : ClientLoanAccountsEvent
}

internal sealed interface ClientLoanAccountsAction {
    data object NavigateBack : ClientLoanAccountsAction

    /**
     * Re-read, from EITHER the pull gesture or the error state's retry.
     *
     * ONE member, because `RefreshableScreenContent` routes both to the same callback — and
     * because they are the same call anyway: `ScreenDataStream.retry()` is declared
     * `= refresh(forceFresh = true)`. A separate `OnRetry` would be a second name for one
     * operation, and whichever the wrapper did not route would sit undispatched.
     */
    data object Refresh : ClientLoanAccountsAction
    data object ToggleSearch : ClientLoanAccountsAction
    data object ToggleFilter : ClientLoanAccountsAction
    data object ClearFilters : ClientLoanAccountsAction
    data object AddAccount : ClientLoanAccountsAction
    data class UpdateSearchValue(val query: String) : ClientLoanAccountsAction
    data class HandleFilterClick(val status: LoanStatusFilter) : ClientLoanAccountsAction
    data class ViewAccount(val loanId: Int) : ClientLoanAccountsAction
    data class MakeRepayment(val loanId: Int) : ClientLoanAccountsAction
}
