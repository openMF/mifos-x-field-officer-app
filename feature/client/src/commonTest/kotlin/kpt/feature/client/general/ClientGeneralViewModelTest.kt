/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(
    kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class,
    kpt.core.data.testing.ExperimentalDataTestingApi::class,
)

package kpt.feature.client.general

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
import kpt.core.model.savings.SavingAccountCurrency
import kpt.core.model.savings.SavingAccountDepositType
import kpt.core.model.savings.SavingsAccount
import kpt.core.model.savings.SavingsAccountStatus
import kpt.feature.client.general.components.ClientGeneralActionItem
import kpt.feature.client.general.components.clientGeneralActions
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Pins the performance-history arithmetic, which is the only real logic on this screen — and two
 * of its five figures do not mean what their labels say. Both rules are the original's.
 */
class ClientGeneralViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val accountsState = MutableStateFlow<ScreenState<ClientAccounts>>(ScreenState.Loading)
    private val repository = FakeClientRepository(accounts = accountsState)

    private fun viewModel(clientId: Int = 42) =
        ClientGeneralViewModel(repository, SavedStateHandle(mapOf("clientId" to clientId)))

    private fun loan(active: Boolean, cycle: Int) = LoanAccount(
        id = 1,
        loanCycle = cycle,
        status = LoanStatus(active = active),
    )

    private fun savings(
        active: Boolean,
        balance: Double,
        closed: Boolean = false,
        recurring: Boolean = false,
        symbol: String? = null,
    ) = SavingsAccount(
        id = 1,
        accountBalance = balance,
        status = SavingsAccountStatus(active = active, closed = closed),
        depositType = SavingAccountDepositType(
            id = if (recurring) {
                SavingAccountDepositType.ServerTypes.RECURRING.id
            } else {
                SavingAccountDepositType.ServerTypes.SAVINGS.id
            },
        ),
        currency = symbol?.let { SavingAccountCurrency(displaySymbol = it) },
    )

    // ---- the arithmetic ----

    @Test
    fun loanCyclesSumsTheCycleFieldAcrossActiveLoansRatherThanCountingLoans() {
        // "No. Of Loan Cycles" is a SUM of `loanCycle`, not a count. Two active loans on their 3rd
        // and 2nd cycles report 5, not 2. Kept from the original — changing it would change a
        // reported number under the officer.
        val history = performanceHistory(
            ClientAccounts(loanAccounts = listOf(loan(active = true, cycle = 3), loan(active = true, cycle = 2))),
        )
        assertEquals(5, history.loanCyclesCount)
        assertEquals(2, history.activeLoans)
    }

    @Test
    fun closedLoansCountTowardsNeitherFigure() {
        val history = performanceHistory(
            ClientAccounts(loanAccounts = listOf(loan(active = true, cycle = 1), loan(active = false, cycle = 9))),
        )
        assertEquals(1, history.loanCyclesCount)
        assertEquals(1, history.activeLoans)
    }

    @Test
    fun totalSavingsExcludesRecurringDepositsAndClosedAccounts() {
        // So "Total Savings" deliberately does NOT equal the sum of the balances the accounts list
        // shows. The original's filter; kept.
        val history = performanceHistory(
            ClientAccounts(
                savingsAccounts = listOf(
                    savings(active = true, balance = 100.0),
                    savings(active = true, balance = 500.0, recurring = true),
                    savings(active = true, balance = 700.0, closed = true),
                    savings(active = false, balance = 900.0),
                ),
            ),
        )
        assertEquals(100.0, history.totalSaving)
        // activeSavingsCount counts ALL active accounts, recurring included — a different rule
        // from totalSaving's, in the same card.
        assertEquals(3, history.activeSavingsCount)
    }

    @Test
    fun lastLoanAmountIsAlwaysNull() {
        // The original's own comment: "No function yet created for calculating this value." The row
        // renders "Not Available". Kept null rather than invented — a fabricated figure on a loan
        // screen is worse than an honest blank.
        assertNull(performanceHistory(ClientAccounts(loanAccounts = listOf(loan(true, 1)))).lastLoanAmount)
    }

    @Test
    fun theCurrencyComesFromTheFirstSavingsAccountAndFallsBackToDollar() {
        assertEquals(
            "₹",
            performanceHistory(
                ClientAccounts(savingsAccounts = listOf(savings(true, 1.0, symbol = "₹"))),
            ).currency,
        )
        // The original's fallback, for a client with no savings account at all.
        assertEquals("$", performanceHistory(ClientAccounts()).currency)
    }

    @Test
    fun noAccountsIsZeroesRatherThanAnErrorOrABlankCard() {
        val history = performanceHistory(ClientAccounts())
        assertEquals(0, history.loanCyclesCount)
        assertEquals(0, history.activeLoans)
        assertEquals(0, history.activeSavingsCount)
        assertEquals(0.0, history.totalSaving)
    }

    // ---- the ViewModel ----

    @Test
    fun theRouteArgumentReachesTheAccountsRead() = runTest(dispatcher) {
        viewModel(clientId = 7)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf<Any>(7), repository.requestedKeys["accounts"]?.toList())
    }

    @Test
    fun theHistoryIsDerivedFromTheAccountsStream() = runTest(dispatcher) {
        val vm = viewModel()
        accountsState.value = ScreenState.Content(
            ClientAccounts(loanAccounts = listOf(loan(active = true, cycle = 4))),
        )

        vm.performance.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<PerformanceHistory>>(awaitItem())
            // Derived, not stored — so it cannot fall out of step with the accounts it summarises.
            assertEquals(4, content.data.loanCyclesCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun actionClicksCarryTheClientIdRatherThanLettingTheScreenReadItMidLoad() = runTest(dispatcher) {
        val vm = viewModel(clientId = 42)
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientGeneralAction.OnActionClick(ClientGeneralActionItem.LoanAccount))
            val event = assertIs<ClientGeneralEvent.OnActionClick>(awaitItem())
            // The original read `state.client?.id ?: -1` at all SEVEN call sites.
            assertEquals(42, event.clientId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun retryReachesTheAccountsStream() = runTest(dispatcher) {
        val vm = viewModel()
        repository.refreshesFor("accounts").test {
            vm.actionChannel.trySend(ClientGeneralAction.OnRetry)
            testScheduler.advanceUntilIdle()
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theActionListIsTheOriginalsSevenInTheOriginalsOrder() {
        assertEquals(
            listOf(
                ClientGeneralActionItem.UpcomingCharges,
                ClientGeneralActionItem.LoanAccount,
                ClientGeneralActionItem.SavingAccounts,
                ClientGeneralActionItem.FixedDepositAccounts,
                ClientGeneralActionItem.RecurringDepositAccounts,
                ClientGeneralActionItem.SharesAccounts,
                ClientGeneralActionItem.CollateralData,
            ),
            clientGeneralActions.toList(),
        )
    }
}
