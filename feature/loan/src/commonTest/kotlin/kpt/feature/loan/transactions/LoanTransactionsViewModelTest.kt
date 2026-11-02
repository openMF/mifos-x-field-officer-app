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

package kpt.feature.loan.transactions

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeLoanRepository
import kpt.core.model.objects.account.loan.Transaction
import kpt.core.model.objects.account.loan.Type
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.loanWithAssociations.SavingAccountCurrency
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LoanTransactionsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val loanState = MutableStateFlow<ScreenState<LoanWithAssociations>>(ScreenState.Loading)
    private val repository = FakeLoanRepository(loan = loanState)

    private fun viewModel(loanId: Int = 7) =
        LoanTransactionsViewModel(repository, SavedStateHandle(mapOf("loanId" to loanId)))

    private fun loan(vararg transactions: Transaction) = LoanWithAssociations(
        id = 7,
        currency = SavingAccountCurrency(code = "INR", decimalPlaces = 2, displaySymbol = "₹"),
        transactions = transactions.toList(),
    )

    private fun transaction(
        id: Int,
        type: String? = "Repayment",
        amount: Double? = 1_000.0,
        reversed: Boolean? = null,
        date: List<Int> = listOf(2026, 3, 14),
    ) = Transaction(
        id = id,
        officeName = "Head Office",
        date = date,
        amount = amount,
        principalPortion = 900.0,
        interestPortion = 100.0,
        outstandingLoanBalance = 9_000.0,
        type = type?.let { Type(value = it) },
        manuallyReversed = reversed,
    )

    private suspend fun rows(vm: LoanTransactionsViewModel): List<TransactionRow> {
        var result: List<TransactionRow>? = null
        vm.transactions.test {
            assertIs<ScreenState.Loading>(awaitItem())
            result = assertIs<ScreenState.Content<List<TransactionRow>>>(awaitItem()).data
            cancelAndIgnoreRemainingEvents()
        }
        return assertNotNull(result)
    }

    @Test
    fun theRouteArgumentReachesTheRead() = runTest(dispatcher) {
        viewModel(loanId = 42)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf<Any>(42), repository.requestedKeys["loan"]?.toList())
    }

    @Test
    fun rowsAreNumberedFromOneInTheOrderFineractSentThem() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(transaction(11), transaction(12), transaction(13)),
        )

        val result = rows(vm)

        assertEquals(listOf("1", "2", "3"), result.map { it.number })
        // The ID column is the transaction's own id, which is NOT the row number — the original
        // showed both columns for exactly this reason.
        assertEquals(listOf("11", "12", "13"), result.map { it.id })
    }

    @Test
    fun aReversedTransactionIsKeptAndFlagged() = runTest(dispatcher) {
        // Kept, not filtered: an officer reconciling an account has to see that a payment was
        // undone. The screen strikes the row through in the error colour.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(transaction(1), transaction(2, reversed = true)),
        )

        val result = rows(vm)

        assertEquals(2, result.size)
        assertTrue(result[1].manuallyReversed)
        // A null flag is NOT reversed — `== true`, not `!!`.
        assertTrue(!result[0].manuallyReversed)
    }

    @Test
    fun aLoanWithNoTransactionsIsEmptyNotAnEmptyTable() = runTest(dispatcher) {
        // The original rendered its empty state from a check in the screen body. Moving the
        // decision to the stream means the screen has one branch and the state is the one the
        // framework already models.
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan())

        vm.transactions.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Empty>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun anUnmappedTransactionTypeRendersAsTheCatchAll() = runTest(dispatcher) {
        // Fineract has many more types than the three the table names (waiver, write-off,
        // recovery, refund …). All of them read "-", which is the original's behaviour.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(transaction(1, type = "Waive Interest"), transaction(2, type = null)),
        )

        val result = rows(vm)

        assertEquals(TransactionType.UNKNOWN, result[0].transactionType)
        assertEquals(TransactionType.UNKNOWN, result[1].transactionType)
        assertEquals("-", result[0].transactionType.value)
    }

    @Test
    fun theThreeNamedTypesMapByTheirFineractLabel() {
        assertEquals(TransactionType.ACCRUAL, TransactionType.fromValue("Accrual"))
        assertEquals(TransactionType.DISBURSEMENT, TransactionType.fromValue("Disbursement"))
        assertEquals(TransactionType.REPAYMENT, TransactionType.fromValue("Repayment"))
        assertEquals(null, TransactionType.fromValue("repayment"), "the match is case-sensitive")
    }

    @Test
    fun aTransactionWithNoUsableDateRendersAPlaceholder() = runTest(dispatcher) {
        // `getDateAsString` indexes `[2]`; `Transaction.date` defaults to an EMPTY list, so an
        // unguarded call would throw on any transaction Fineract sent without one.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(transaction(1, date = emptyList()), transaction(2, date = listOf(2026))),
        )

        val result = rows(vm)

        assertEquals("-", result[0].transactionDate)
        assertEquals("-", result[1].transactionDate)
    }

    @Test
    fun amountsAreFormattedWithTheLoansOwnCurrency() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(transaction(1, amount = 1_234.5)))

        val row = rows(vm).single()

        assertTrue(row.amount.contains("1,234"), "amount was ${row.amount}")
        assertTrue(row.loanBalance.contains("9,000"), "balance was ${row.loanBalance}")
    }

    @Test
    fun retryReachesTheRead() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(transaction(1)))
        testScheduler.advanceUntilIdle()

        repository.refreshes.test {
            vm.actionChannel.trySend(LoanTransactionsAction.OnRetry)
            testScheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun backNavigationIsAnEvent() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(LoanTransactionsAction.NavigateBack)
            assertEquals(LoanTransactionsEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
