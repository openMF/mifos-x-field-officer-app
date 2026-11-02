/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.depositaccounts

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
import kpt.core.model.savings.SavingAccountDepositType
import kpt.core.model.savings.SavingsAccount

/**
 * A client's deposit accounts of ONE kind — savings, fixed deposit, or recurring deposit.
 *
 * Ported from THREE original screens at `6b66e8a43`:
 *  - `feature/client/savingsAccounts/{SavingsAccountsScreen,ViewModel}.kt` (299 + 156 lines)
 *  - `feature/client/fixedDepositAccount/{FixedDepositAccountScreen,ViewModel}.kt` (317 + 203)
 *  - `feature/client/recurringDepositAccount/{RecurringDepositAccountScreen,ViewModel}.kt` (315 + 195)
 *
 * ## Why ONE screen and not three
 *
 * The three originals are the same screen. Each reads the SAME field —
 * `clientAccounts.savingsAccounts` — applies the SAME `status?.closed == false` guard, and differs
 * only in which `SavingAccountDepositType.ServerTypes` it keeps. Fineract models all three as
 * deposit accounts on one list; the split exists in the UI layer alone.
 *
 * Porting them as three files would reproduce ~1,300 lines whose only distinct token is an enum
 * constant, and would land exactly the duplicate-scaffolding defect `G-IMPL-DUP`
 * (RULE-IMPL-DUPLICATION-001) exists to catch. The kind travels as a route argument instead, which
 * is also what the destination already expects downstream: `navigateToSavingsSummary` takes an
 * `accountType` endpoint precisely so "a caller that knows the account's deposit type (the client's
 * savings list, group detail) passes `account.depositType.endpoint`".
 *
 * This is a de-duplication, NOT a redesign — every behaviour below is traceable to one of the three
 * originals, and the two places where they genuinely disagreed are called out at [DepositAccountKind].
 *
 * ## FIXED: "Approve" approved the wrong thing
 *
 * The original's approve action passed the CLIENT id where an ACCOUNT id was required:
 *
 * ```kotlin
 * is Actions.ApproveAccount -> onAction.invoke(
 *     SavingsAccountAction.ApproveAccount(state.clientId),   // <- clientId
 * )
 * ```
 *
 * which reached `navigateToApproveAccount(event.accountId)`. The sibling `ViewAccount` branch two
 * lines above correctly passes `savings.id`, so this is a slip rather than an intention. The effect
 * is that approving a pending deposit account opened the approval screen for whatever account
 * happens to carry the client's id — a wrong-target write on a financial record, and invisible
 * because the screen it opens looks normal. [ClientDepositAccountsAction.ApproveAccount] carries
 * the account id.
 *
 * ## The filter button is NOT drawn
 *
 * The original renders a filter `IconButton` whose action flips `isFilterDialogOpen`, but its only
 * dialog composable handles `DialogState.Error` — there is no filter dialog anywhere in the three
 * files. The control was inert in every one of them. Per the migration spec's §6a rule an unported
 * or non-functional affordance is **absent, not inert**, so the button is omitted rather than drawn
 * and ignored. Recorded as a gap; adding real filtering is a product decision, not a port.
 *
 * ## Search is local, and case-insensitive
 *
 * The original re-fetched from the server on every search tap (`OnSearchClick -> getSavingsAccount()`)
 * and matched the account number case-SENSITIVELY, under its own `// todo implement search bar
 * functionality`. The query now filters the stream locally — the same treatment the loan-accounts
 * port applied — so the only reason left to re-read is wanting fresher data.
 */
internal class ClientDepositAccountsViewModel(
    repository: ClientRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientDepositAccountsState, ClientDepositAccountsEvent, ClientDepositAccountsAction>(
    ClientDepositAccountsState(
        clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1,
        kind = savedStateHandle.get<String>(KIND_KEY)
            ?.let { runCatching { DepositAccountKind.valueOf(it) }.getOrNull() }
            ?: DepositAccountKind.SAVINGS,
    ),
) {

    private val stream = repository.clientAccountsStream(state.clientId, viewModelScope)

    /**
     * This client's accounts of this kind, with the query applied.
     *
     * `combineContent` is the substrate's filter/sort seam, so Loading, NoNetwork, Error and
     * Unauthenticated pass through untouched and only Content is narrowed. `emptyIfContent` runs
     * AFTER the query so an over-narrow search shows the Empty state rather than a header counting
     * zero above nothing.
     */
    val accounts: StateFlow<ScreenState<List<SavingsAccount>>> = stream.state
        .combineContent(stateFlow) { clientAccounts, ui, _ ->
            clientAccounts.savingsAccounts.ofKind(ui.kind).matching(ui.searchText)
        }
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /**
     * How many accounts of this kind the client has, before the query.
     *
     * Counted off the UNFILTERED list. The original's header counted the filtered one, so the one
     * number that could have told an officer their search was too narrow reported the search's own
     * result — the same defect the loan-accounts port fixed.
     */
    val totalCount: StateFlow<Int> = stream.state
        .map { screen ->
            (screen as? ScreenState.Content)?.data?.savingsAccounts?.ofKind(state.kind)?.size ?: 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), 0)

    override fun handleAction(action: ClientDepositAccountsAction) {
        when (action) {
            ClientDepositAccountsAction.NavigateBack ->
                sendEvent(ClientDepositAccountsEvent.NavigateBack)

            ClientDepositAccountsAction.Refresh -> stream.refresh(forceFresh = true)

            ClientDepositAccountsAction.ToggleSearch -> mutableStateFlow.value = state.copy(
                isSearchBarActive = !state.isSearchBarActive,
                // Closing the search clears the query, so a hidden field can never go on filtering
                // the list with nothing on screen to explain why.
                searchText = if (state.isSearchBarActive) "" else state.searchText,
            )

            is ClientDepositAccountsAction.UpdateSearchValue ->
                mutableStateFlow.value = state.copy(searchText = action.query)

            is ClientDepositAccountsAction.ViewAccount -> sendEvent(
                ClientDepositAccountsEvent.ViewAccount(
                    accountId = action.accountId,
                    // The ROW's own deposit type, not the screen's kind: they agree today because
                    // the list is filtered by kind, but the endpoint belongs to the account being
                    // opened and reading it from the row keeps that true if the filter ever widens.
                    accountEndpoint = action.accountEndpoint,
                ),
            )

            is ClientDepositAccountsAction.ApproveAccount ->
                sendEvent(ClientDepositAccountsEvent.ApproveAccount(action.accountId))

            ClientDepositAccountsAction.AddAccount ->
                sendEvent(ClientDepositAccountsEvent.AddAccount(state.clientId, state.kind))
        }
    }

    internal companion object {
        const val CLIENT_ID_KEY = "clientId"
        const val KIND_KEY = "kind"
        private const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * Which deposit product this screen lists.
 *
 * [serverType] is the discriminator the three originals differed by, and the ONLY thing that
 * differed between them.
 */
enum class DepositAccountKind(
    val serverType: SavingAccountDepositType.ServerTypes,
) {
    SAVINGS(SavingAccountDepositType.ServerTypes.SAVINGS),
    FIXED(SavingAccountDepositType.ServerTypes.FIXED),
    RECURRING(SavingAccountDepositType.ServerTypes.RECURRING),
}

/**
 * Keep only this client's accounts of one kind that are still open.
 *
 * `status?.closed == false` is the originals' guard, kept verbatim including its quirk: an account
 * whose `status` is null is EXCLUDED, because `null == false` is false. That silently hides an
 * account the server returned without a status block rather than showing it as unknown. Kept
 * because changing which accounts an officer can see is a product decision, and logged as a gap.
 */
private fun List<SavingsAccount>.ofKind(kind: DepositAccountKind): List<SavingsAccount> =
    filter { it.depositType?.serverType == kind.serverType && it.status?.closed == false }

/**
 * Apply the search query to the account NUMBER.
 *
 * Account number only — which is what all three originals did (`accountNo?.contains(...)`), under
 * their own "todo modify search accordingly". Widening it to the product name is a product
 * decision. Unlike the originals this is case-insensitive, because Fineract account numbers can
 * carry a letter prefix and a case-sensitive match on one is a search that silently finds nothing.
 */
private fun List<SavingsAccount>.matching(query: String): List<SavingsAccount> {
    val q = query.trim()
    return if (q.isEmpty()) this else filter { it.accountNo?.contains(q, ignoreCase = true) == true }
}

internal data class ClientDepositAccountsState(
    val clientId: Int = -1,
    val kind: DepositAccountKind = DepositAccountKind.SAVINGS,
    val isSearchBarActive: Boolean = false,
    val searchText: String = "",
) {
    /**
     * Whether a query is narrowing the list.
     *
     * The screen needs this to tell its two empty states apart: "this client has no accounts of
     * this kind" offers to open one, "nothing matches your search" must not — opening an account
     * would not reveal the ones the query is hiding. The originals had a single empty state and
     * offered the create button in both.
     */
    val isFiltering: Boolean get() = searchText.isNotBlank()
}

internal sealed interface ClientDepositAccountsEvent {
    data object NavigateBack : ClientDepositAccountsEvent

    /** Open the account summary. [accountEndpoint] addresses the right Fineract collection. */
    data class ViewAccount(val accountId: Int, val accountEndpoint: String) :
        ClientDepositAccountsEvent

    /** Approve a submitted-and-pending account. Carries the ACCOUNT id — see the class KDoc. */
    data class ApproveAccount(val accountId: Int) : ClientDepositAccountsEvent

    data class AddAccount(val clientId: Int, val kind: DepositAccountKind) :
        ClientDepositAccountsEvent
}

internal sealed interface ClientDepositAccountsAction {
    data object NavigateBack : ClientDepositAccountsAction

    /**
     * Re-read, from EITHER the pull gesture or the error state's retry.
     *
     * ONE member: `RefreshableScreenContent` routes both to the same callback, and they are the
     * same call anyway — `ScreenDataStream.retry()` is declared `= refresh(forceFresh = true)`.
     */
    data object Refresh : ClientDepositAccountsAction
    data object ToggleSearch : ClientDepositAccountsAction
    data object AddAccount : ClientDepositAccountsAction
    data class UpdateSearchValue(val query: String) : ClientDepositAccountsAction
    data class ViewAccount(val accountId: Int, val accountEndpoint: String) :
        ClientDepositAccountsAction
    data class ApproveAccount(val accountId: Int) : ClientDepositAccountsAction
}
