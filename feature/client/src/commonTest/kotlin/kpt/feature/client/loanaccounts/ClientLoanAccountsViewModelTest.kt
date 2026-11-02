/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.data.testing.ExperimentalDataTestingApi::class)

package kpt.feature.client.loanaccounts

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeClientRepository
import kpt.core.model.client.ClientAccounts
import kpt.core.model.loan.LoanAccount
import kpt.core.model.loan.LoanStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The filters are this screen's logic, and the original's version destroyed the list it filtered.
 * Most of these tests exist to keep that from coming back.
 */
class ClientLoanAccountsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun loan(
        id: Int,
        accountNo: String,
        status: LoanStatus = LoanStatus(active = true),
    ) = LoanAccount(id = id, accountNo = accountNo, productName = "Group Loan", status = status)

    private val allAccounts = listOf(
        loan(1, "LN0001", LoanStatus(active = true)),
        loan(2, "LN0002", LoanStatus(pendingApproval = true)),
        loan(3, "AB0003", LoanStatus(closedObligationsMet = true)),
        loan(4, "LN0004", LoanStatus(overpaid = true)),
        loan(5, "LN0005", LoanStatus(waitingForDisbursal = true)),
    )

    private val accountsState = MutableStateFlow<ScreenState<ClientAccounts>>(
        ScreenState.Content(ClientAccounts(loanAccounts = allAccounts)),
    )
    private val repository = FakeClientRepository(accounts = accountsState)

    private fun viewModel(clientId: Int = 53) =
        ClientLoanAccountsViewModel(repository, SavedStateHandle(mapOf("clientId" to clientId)))

    private suspend fun visible(vm: ClientLoanAccountsViewModel): List<Int> {
        var ids: List<Int> = emptyList()
        vm.accounts.test {
            // Skip states until Content or Empty settles.
            var item = awaitItem()
            while (item is ScreenState.Loading) item = awaitItem()
            ids = when (item) {
                is ScreenState.Content -> item.data.mapNotNull { it.id }
                is ScreenState.Empty -> emptyList()
                else -> error("unexpected state $item")
            }
            cancelAndIgnoreRemainingEvents()
        }
        return ids
    }

    @Test
    fun theRouteArgumentReachesTheRead() = runTest(dispatcher) {
        viewModel(clientId = 42)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf<Any>(42), repository.requestedKeys["accounts"]?.toList())
    }

    @Test
    fun everyAccountIsVisibleWithNoFilter() = runTest(dispatcher) {
        assertEquals(listOf(1, 2, 3, 4, 5), visible(viewModel()))
    }

    // ---- THE defect: the filter destroyed its own source ----

    @Test
    fun clearingTheFilterRestoresTheFULLListNotTheSearchResult() = runTest(dispatcher) {
        // THE REGRESSION. The original stored the search-filtered list in BOTH `loanAccounts` and
        // `unfilteredLoanAccounts`, so "clear filters" restored the SEARCH result and a status
        // filter applied on top of a search could never be widened back.
        val vm = viewModel()
        vm.actionChannel.trySend(ClientLoanAccountsAction.UpdateSearchValue("LN000"))
        vm.actionChannel.trySend(
            ClientLoanAccountsAction.HandleFilterClick(LoanStatusFilter.ACTIVE),
        )
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(1), visible(vm))

        vm.actionChannel.trySend(ClientLoanAccountsAction.ClearFilters)
        testScheduler.advanceUntilIdle()
        // The STATUS filter is cleared; the search is still "LN000", so AB0003 stays out. Both
        // are derived from the one stream, so each can be lifted independently.
        assertEquals(listOf(1, 2, 4, 5), visible(vm))

        vm.actionChannel.trySend(ClientLoanAccountsAction.UpdateSearchValue(""))
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(1, 2, 3, 4, 5), visible(vm))
    }

    @Test
    fun aRefreshDoesNotCLEARTheOfficersStatusFilter() = runTest(dispatcher) {
        // The original reset `selectedStatus = emptySet()` on EVERY load. With a stream that
        // re-reads on reconnect and on pull-to-refresh, the filter would be wiped repeatedly and
        // apparently at random.
        val vm = viewModel()
        vm.actionChannel.trySend(
            ClientLoanAccountsAction.HandleFilterClick(LoanStatusFilter.PENDING),
        )
        testScheduler.advanceUntilIdle()

        // A fresh emission from the stream, exactly as a refresh produces.
        accountsState.value = ScreenState.Content(ClientAccounts(loanAccounts = allAccounts))
        testScheduler.advanceUntilIdle()

        assertEquals(setOf(LoanStatusFilter.PENDING), vm.stateFlow.value.selectedStatus)
        assertEquals(listOf(2), visible(vm))
    }

    @Test
    fun theHeaderCountIsTheUNFILTEREDTotal() = runTest(dispatcher) {
        // The original counted the FILTERED list, so the one number that could have told the
        // officer their filter was too narrow reported the filter's own result: "0 item".
        val vm = viewModel()
        vm.actionChannel.trySend(ClientLoanAccountsAction.UpdateSearchValue("nothing matches this"))
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList(), visible(vm))

        // COLLECTED, not read off `.value`. `totalCount` is `WhileSubscribed` — correct for a
        // render-only value — which means its `.value` sits at the initial 0 when nothing is
        // collecting. (The same shape bit the repayment screen for real, where the WRITE read two
        // such flows; those two are `Eagerly` now. Here the screen is the only reader, so the
        // test has to behave like the screen.)
        vm.totalCount.test {
            assertEquals(0, awaitItem())
            assertEquals(5, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- the filters themselves ----

    @Test
    fun eachStatusFilterSelectsItsOwnBucket() = runTest(dispatcher) {
        val vm = viewModel()

        suspend fun only(filter: LoanStatusFilter): List<Int> {
            vm.actionChannel.trySend(ClientLoanAccountsAction.ClearFilters)
            vm.actionChannel.trySend(ClientLoanAccountsAction.HandleFilterClick(filter))
            testScheduler.advanceUntilIdle()
            return visible(vm)
        }

        assertEquals(listOf(1), only(LoanStatusFilter.ACTIVE))
        assertEquals(listOf(2), only(LoanStatusFilter.PENDING))
        assertEquals(listOf(3), only(LoanStatusFilter.CLOSED))
        assertEquals(listOf(4), only(LoanStatusFilter.OVERPAID))
    }

    @Test
    fun theFiltersAreADISJUNCTIONNotAConjunction() = runTest(dispatcher) {
        // Two statuses selected means "either", not "both" — the original's `any { }`, and the
        // only reading that makes sense for mutually-exclusive statuses.
        val vm = viewModel()
        vm.actionChannel.trySend(ClientLoanAccountsAction.HandleFilterClick(LoanStatusFilter.ACTIVE))
        vm.actionChannel.trySend(ClientLoanAccountsAction.HandleFilterClick(LoanStatusFilter.OVERPAID))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(1, 4), visible(vm))
    }

    @Test
    fun anAwaitingDisbursalLoanMatchesNONEOfTheFourFilters() = runTest(dispatcher) {
        // Carried over deliberately: the four buckets do not partition `LoanStatus`, so an
        // approved-but-undisbursed loan disappears under ANY active filter. Adding a fifth bucket
        // is a product decision; logged as
        // `client-loan-accounts-filters-do-not-cover-awaiting-disbursal`.
        val vm = viewModel()
        LoanStatusFilter.entries.forEach {
            vm.actionChannel.trySend(ClientLoanAccountsAction.HandleFilterClick(it))
        }
        testScheduler.advanceUntilIdle()

        assertTrue(5 !in visible(vm), "loan 5 is waitingForDisbursal and matches no bucket")
    }

    @Test
    fun tappingASelectedFilterUnselectsIt() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(ClientLoanAccountsAction.HandleFilterClick(LoanStatusFilter.ACTIVE))
        testScheduler.advanceUntilIdle()
        assertEquals(setOf(LoanStatusFilter.ACTIVE), vm.stateFlow.value.selectedStatus)

        vm.actionChannel.trySend(ClientLoanAccountsAction.HandleFilterClick(LoanStatusFilter.ACTIVE))
        testScheduler.advanceUntilIdle()
        assertEquals(emptySet(), vm.stateFlow.value.selectedStatus)
    }

    @Test
    fun aLoanWithNoStatusMatchesNoFilterButSHOWSWithNoneSelected() = runTest(dispatcher) {
        accountsState.value = ScreenState.Content(
            ClientAccounts(loanAccounts = listOf(LoanAccount(id = 9, accountNo = "LN0009"))),
        )
        val vm = viewModel()
        assertEquals(listOf(9), visible(vm))

        vm.actionChannel.trySend(ClientLoanAccountsAction.HandleFilterClick(LoanStatusFilter.ACTIVE))
        testScheduler.advanceUntilIdle()
        assertEquals(emptyList(), visible(vm))
    }

    // ---- search ----

    @Test
    fun theSearchMatchesTheAccountNumberCaseINSENSITIVELY() = runTest(dispatcher) {
        // The original matched case-SENSITIVELY, which matters because Fineract account numbers
        // can carry a letter prefix.
        val vm = viewModel()
        vm.actionChannel.trySend(ClientLoanAccountsAction.UpdateSearchValue("ab000"))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(3), visible(vm))
    }

    @Test
    fun theSearchIgnoresSurroundingWhitespace() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(ClientLoanAccountsAction.UpdateSearchValue("  LN0002  "))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(2), visible(vm))
    }

    @Test
    fun closingTheSearchBarCLEARSTheQuery() = runTest(dispatcher) {
        // The original hid the field and left the text in place, so a hidden query kept filtering
        // the list with nothing on screen to explain why.
        val vm = viewModel()
        vm.actionChannel.trySend(ClientLoanAccountsAction.ToggleSearch)
        vm.actionChannel.trySend(ClientLoanAccountsAction.UpdateSearchValue("LN0001"))
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(1), visible(vm))

        vm.actionChannel.trySend(ClientLoanAccountsAction.ToggleSearch)
        testScheduler.advanceUntilIdle()
        assertEquals("", vm.stateFlow.value.searchText)
        assertEquals(listOf(1, 2, 3, 4, 5), visible(vm))
    }

    @Test
    fun anOverNarrowFilterYieldsEMPTYNotAnEmptyContentList() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(ClientLoanAccountsAction.UpdateSearchValue("zzz"))
        testScheduler.advanceUntilIdle()

        vm.accounts.test {
            var item = awaitItem()
            while (item is ScreenState.Loading) item = awaitItem()
            // Empty, so the screen renders its empty state rather than a list with no rows.
            assertIs<ScreenState.Empty>(item)
            cancelAndIgnoreRemainingEvents()
        }
        // And `isFiltering` is what tells the screen WHICH empty state — the "no accounts" copy
        // offers to create one, the "nothing matches" copy must not.
        assertTrue(vm.stateFlow.value.isFiltering)
    }

    @Test
    fun isFilteringIsFalseWithNoQueryAndNoStatus() {
        assertTrue(!ClientLoanAccountsState(clientId = 1).isFiltering)
        assertTrue(ClientLoanAccountsState(clientId = 1, searchText = "x").isFiltering)
        assertTrue(
            ClientLoanAccountsState(
                clientId = 1,
                selectedStatus = setOf(LoanStatusFilter.ACTIVE),
            ).isFiltering,
        )
        // Whitespace alone is not a filter — it is what `filtered` trims away.
        assertTrue(!ClientLoanAccountsState(clientId = 1, searchText = "   ").isFiltering)
    }

    // ---- navigation ----

    @Test
    fun viewAccountAndMakeRepaymentCarryTheLoanId() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientLoanAccountsAction.ViewAccount(7))
            assertEquals(ClientLoanAccountsEvent.ViewAccount(7), awaitItem())
            vm.actionChannel.trySend(ClientLoanAccountsAction.MakeRepayment(7))
            assertEquals(ClientLoanAccountsEvent.MakeRepayment(7), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addAccountCarriesTheClientIdAndMakesNOExtraRequest() = runTest(dispatcher) {
        // The original fetched the client a SECOND time at tap time just to read its `accountNo`
        // into the event — un-guarded, so a tap while offline threw out of `handleAction`.
        val vm = viewModel(clientId = 53)
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientLoanAccountsAction.AddAccount)
            assertEquals(ClientLoanAccountsEvent.AddAccount(53), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // The `client` stream was never supplied to the fake, so reading it would have thrown.
        assertTrue("client" !in repository.requestedKeys)
    }

    @Test
    fun refreshReachesTheRead() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        repository.refreshes.test {
            vm.actionChannel.trySend(ClientLoanAccountsAction.Refresh)
            testScheduler.advanceUntilIdle()
            awaitItem()
            // ONE member for both the pull gesture and the error-state retry — see
            // `RefreshableScreenContent`, and note `retry()` is `refresh(forceFresh = true)`.
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun backNavigationIsAnEvent() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientLoanAccountsAction.NavigateBack)
            assertEquals(ClientLoanAccountsEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
