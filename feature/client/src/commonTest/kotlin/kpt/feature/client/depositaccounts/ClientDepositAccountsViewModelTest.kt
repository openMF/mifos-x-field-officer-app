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
import kpt.core.model.savings.SavingAccountDepositType
import kpt.core.model.savings.SavingsAccount
import kpt.core.model.savings.SavingsAccountStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The deposit-accounts list, across all three kinds.
 *
 * The cases that matter are the ones the three originals got wrong — the approve action's target
 * and the header's count — plus the kind discriminator that is now the ONLY thing separating what
 * used to be three files.
 */
class ClientDepositAccountsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun account(
        id: Int,
        accountNo: String,
        serverType: SavingAccountDepositType.ServerTypes,
        closed: Boolean = false,
        pending: Boolean = false,
    ) = SavingsAccount(
        id = id,
        accountNo = accountNo,
        productName = "Product $id",
        depositType = SavingAccountDepositType(id = serverType.id, code = serverType.code),
        status = SavingsAccountStatus(
            closed = closed,
            submittedAndPendingApproval = pending,
        ),
    )

    private val accounts = listOf(
        account(1, "S-001", SavingAccountDepositType.ServerTypes.SAVINGS),
        account(2, "S-002", SavingAccountDepositType.ServerTypes.SAVINGS, pending = true),
        account(3, "S-003", SavingAccountDepositType.ServerTypes.SAVINGS, closed = true),
        account(4, "F-001", SavingAccountDepositType.ServerTypes.FIXED),
        account(5, "R-001", SavingAccountDepositType.ServerTypes.RECURRING),
    )

    private val accountsState = MutableStateFlow<ScreenState<ClientAccounts>>(
        ScreenState.Content(ClientAccounts(savingsAccounts = accounts)),
    )

    private fun viewModel(
        kind: DepositAccountKind,
        clientId: Int = 7,
    ) = ClientDepositAccountsViewModel(
        repository = FakeClientRepository(accounts = accountsState),
        savedStateHandle = SavedStateHandle(
            mapOf(
                ClientDepositAccountsViewModel.CLIENT_ID_KEY to clientId,
                ClientDepositAccountsViewModel.KIND_KEY to kind.name,
            ),
        ),
    )

    private suspend fun contentOf(vm: ClientDepositAccountsViewModel): List<SavingsAccount> {
        var result: List<SavingsAccount> = emptyList()
        vm.accounts.test {
            var item = awaitItem()
            while (item is ScreenState.Loading) item = awaitItem()
            result = (item as? ScreenState.Content)?.data.orEmpty()
            cancelAndIgnoreRemainingEvents()
        }
        return result
    }

    /**
     * The kind discriminator — the one thing that differed between the three original files.
     *
     * Each kind sees only its own accounts, so collapsing them into one screen cannot leak a fixed
     * deposit into the savings list.
     */
    @Test
    fun eachKindSeesOnlyItsOwnAccounts() = runTest {
        assertEquals(
            listOf("S-001", "S-002"),
            contentOf(viewModel(DepositAccountKind.SAVINGS)).map { it.accountNo },
        )
        assertEquals(
            listOf("F-001"),
            contentOf(viewModel(DepositAccountKind.FIXED)).map { it.accountNo },
        )
        assertEquals(
            listOf("R-001"),
            contentOf(viewModel(DepositAccountKind.RECURRING)).map { it.accountNo },
        )
    }

    /** `status?.closed == false` — the originals' guard, kept. S-003 is closed and must not show. */
    @Test
    fun closedAccountsAreExcluded() = runTest {
        val shown = contentOf(viewModel(DepositAccountKind.SAVINGS)).map { it.accountNo }
        assertTrue("S-003" !in shown, "a closed account must not be listed")
    }

    /**
     * The defect this port exists to fix.
     *
     * The originals raised the approve action with `state.clientId` where an ACCOUNT id belongs, so
     * approving a pending deposit account opened the approval screen for whatever record carried
     * the client's id. The event must carry the account's own id — here 2, never the client id 7.
     */
    @Test
    fun approveCarriesTheAccountIdNotTheClientId() = runTest {
        val vm = viewModel(DepositAccountKind.SAVINGS, clientId = 7)
        vm.eventFlow.test {
            vm.trySendAction(ClientDepositAccountsAction.ApproveAccount(accountId = 2))
            val event = awaitItem()
            assertEquals(ClientDepositAccountsEvent.ApproveAccount(accountId = 2), event)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * The header counts the UNFILTERED list.
     *
     * The originals counted the filtered one, so the number that should warn an officer their
     * search is too narrow reported the search's own result — it read 0 next to an empty list.
     */
    @Test
    fun totalCountIgnoresTheSearchQuery() = runTest {
        val vm = viewModel(DepositAccountKind.SAVINGS)
        vm.totalCount.test {
            var count = awaitItem()
            while (count == 0) count = awaitItem()
            assertEquals(2, count, "two open savings accounts before any query")

            vm.trySendAction(ClientDepositAccountsAction.UpdateSearchValue("nothing-matches"))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        // …while the LIST does narrow.
        val vm2 = viewModel(DepositAccountKind.SAVINGS)
        vm2.trySendAction(ClientDepositAccountsAction.UpdateSearchValue("nothing-matches"))
        assertTrue(contentOf(vm2).isEmpty(), "an unmatched query empties the list")
    }

    /** Case-insensitive, unlike the originals: Fineract account numbers can carry a letter prefix. */
    @Test
    fun searchMatchesAccountNumberCaseInsensitively() = runTest {
        val vm = viewModel(DepositAccountKind.SAVINGS)
        vm.trySendAction(ClientDepositAccountsAction.UpdateSearchValue("s-001"))
        assertEquals(listOf("S-001"), contentOf(vm).map { it.accountNo })
    }

    /** Closing the search clears the query, so a hidden field cannot go on filtering silently. */
    @Test
    fun closingTheSearchClearsTheQuery() = runTest(dispatcher) {
        val vm = viewModel(DepositAccountKind.SAVINGS)
        vm.trySendAction(ClientDepositAccountsAction.ToggleSearch)
        vm.trySendAction(ClientDepositAccountsAction.UpdateSearchValue("S-001"))
        // `trySendAction` queues onto the ViewModel's scope; nothing is applied until it runs.
        testScheduler.advanceUntilIdle()
        assertEquals("S-001", vm.stateFlow.value.searchText)

        vm.trySendAction(ClientDepositAccountsAction.ToggleSearch)
        testScheduler.advanceUntilIdle()
        assertEquals("", vm.stateFlow.value.searchText)
        assertTrue(!vm.stateFlow.value.isSearchBarActive)
    }

    /** View carries the ROW's endpoint, so a recurring row addresses `recurringdepositaccounts`. */
    @Test
    fun viewCarriesTheRowsOwnEndpoint() = runTest {
        val vm = viewModel(DepositAccountKind.RECURRING)
        vm.eventFlow.test {
            vm.trySendAction(
                ClientDepositAccountsAction.ViewAccount(
                    accountId = 5,
                    accountEndpoint =
                    SavingAccountDepositType.ServerTypes.RECURRING.endpoint,
                ),
            )
            assertEquals(
                ClientDepositAccountsEvent.ViewAccount(5, "recurringdepositaccounts"),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
