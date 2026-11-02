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

package kpt.feature.loan.schedule

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
import kpt.core.model.objects.account.loan.Period
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.RepaymentScheduleTableData
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.loanWithAssociations.SavingAccountCurrency
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The schedule's whole job is the projection: which periods are rows, which is the disbursement,
 * and what the totals and the three counters run over. Every one of those is off-by-one-able.
 */
class LoanScheduleViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val loanState = MutableStateFlow<ScreenState<LoanWithAssociations>>(ScreenState.Loading)
    private val repository = FakeLoanRepository(loan = loanState)

    private fun viewModel(loanId: Int = 7) =
        LoanScheduleViewModel(repository, SavedStateHandle(mapOf("loanId" to loanId)))

    /** Period 0 — Fineract's disbursement row. `period == null` is what marks it. */
    private fun disbursement() = Period(
        period = null,
        dueDate = listOf(2026, 1, 1),
        principalLoanBalanceOutstanding = 10_000.0,
        complete = true,
    )

    private fun installment(
        number: Int,
        due: Double,
        paid: Double,
        complete: Boolean?,
        overdue: Double? = null,
    ) = Period(
        period = number,
        daysInPeriod = 30,
        dueDate = listOf(2026, number + 1, 1),
        obligationsMetOnDate = if (complete == true) listOf(2026, number + 1, 2) else null,
        principalDue = due,
        interestDue = 50.0,
        totalDueForPeriod = due,
        totalPaidForPeriod = paid,
        totalOutstandingForPeriod = due - paid,
        totalOverdue = overdue,
        complete = complete,
    )

    private fun loan(vararg periods: Period) = LoanWithAssociations(
        id = 7,
        currency = SavingAccountCurrency(code = "INR", decimalPlaces = 2, displaySymbol = "₹"),
        repaymentSchedule = RepaymentSchedule(periods = periods.toList()),
    )

    private suspend fun table(vm: LoanScheduleViewModel): RepaymentScheduleTableData {
        var result: RepaymentScheduleTableData? = null
        vm.schedule.test {
            assertIs<ScreenState.Loading>(awaitItem())
            result = assertIs<ScreenState.Content<RepaymentScheduleTableData>>(awaitItem()).data
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
    fun theDisbursementIsItsOwnRowAndIsNotAnInstallment() = runTest(dispatcher) {
        // THE off-by-one. `getListOfActualPeriods()` drops period 0 because Fineract returns the
        // disbursement there — it has no due amount and is not a repayment. Counting it would
        // inflate the installment count by one on every loan in the portfolio.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(
                disbursement(),
                installment(1, due = 1_000.0, paid = 1_000.0, complete = true),
                installment(2, due = 1_000.0, paid = 0.0, complete = false),
            ),
        )

        val data = table(vm)

        assertNotNull(data.disbursementRow)
        assertEquals(2, data.rows.size)
        // Renumbered from 1 over the ACTUAL periods, so row numbers match what the officer counts.
        assertEquals(listOf("1", "2"), data.rows.map { it.number })
        // The disbursement row carries no number — the original left its cell blank.
        assertEquals("", data.disbursementRow?.number)
    }

    @Test
    fun aScheduleWithNoDisbursementRowStillProjectsItsInstallments() = runTest(dispatcher) {
        // `getListOfActualPeriods()` drops the first period UNCONDITIONALLY, so a schedule whose
        // period 0 is a real installment loses it. That is the original's behaviour and Fineract
        // always sends the disbursement first — this pins the interaction so a future change to
        // the helper cannot quietly swallow a row without a test turning red.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(
                installment(1, due = 1_000.0, paid = 0.0, complete = false),
                installment(2, due = 1_000.0, paid = 0.0, complete = false),
            ),
        )

        val data = table(vm)

        assertNull(data.disbursementRow, "period 0 had a `period` value, so it is not a disbursement")
        assertEquals(1, data.rows.size)
    }

    @Test
    fun theTotalsSumTheInstallmentsOnlyNotTheDisbursement() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(
                disbursement(),
                installment(1, due = 1_000.0, paid = 1_000.0, complete = true),
                installment(2, due = 1_500.0, paid = 500.0, complete = false),
            ),
        )

        val data = table(vm)

        // 1000 + 1500 — the disbursement's 10,000 outstanding balance is not a due amount.
        assertTrue(data.totals.due.contains("2,500"), "due totalled ${data.totals.due}")
        assertTrue(data.totals.paid.contains("1,500"), "paid totalled ${data.totals.paid}")
    }

    @Test
    fun theThreeCountersRunOverTheInstallments() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(
                disbursement(),
                installment(1, due = 1_000.0, paid = 1_000.0, complete = true),
                installment(2, due = 1_000.0, paid = 0.0, complete = false, overdue = 1_000.0),
                installment(3, due = 1_000.0, paid = 0.0, complete = false),
            ),
        )

        val data = table(vm)

        assertEquals(1, data.completeCount)
        // Overdue is a SUBSET of pending — an overdue installment is also not complete, so the
        // two counters deliberately overlap. The original's bottom bar printed both.
        assertEquals(1, data.overdueCount)
        assertEquals(2, data.pendingCount)
    }

    @Test
    fun aPeriodWithNoCompleteFlagDoesNotCrashTheScreen() = runTest(dispatcher) {
        // REGRESSION. `getNumberOfRepaymentsPending` was `if (!period.complete!!)` and
        // `getNumberOfRepaymentsOverDue` dereferenced it too, so a period Fineract sent without
        // the flag took the whole schedule screen down with an NPE.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(
                disbursement(),
                installment(1, due = 1_000.0, paid = 0.0, complete = null, overdue = 500.0),
            ),
        )

        val data = table(vm)

        assertEquals(0, data.completeCount)
        // An absent flag counts as not-complete, which is what the `!!` meant before it threw.
        assertEquals(1, data.pendingCount)
        assertEquals(1, data.overdueCount)
    }

    @Test
    fun aNullAmountIsAnEmptyCellNotZero() = runTest(dispatcher) {
        // The distinction matters on the fee and penalty columns: "0.00" asserts a charge was
        // computed and came to nothing; blank says Fineract reported none. The original kept it.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(
                disbursement(),
                Period(period = 1, dueDate = listOf(2026, 2, 1), complete = false),
            ),
        )

        val data = table(vm)

        assertEquals("", data.rows.single().fees)
        assertEquals("", data.rows.single().penalties)
    }

    @Test
    fun anUnpaidPeriodRendersABlankPaidDateRatherThanCrashing() = runTest(dispatcher) {
        // `DateHelper.getDateAsString` indexes `[2]` directly. Fineract omits
        // `obligationsMetOnDate` on an unpaid period, and a truncated list would throw.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(
                disbursement(),
                installment(1, due = 1_000.0, paid = 0.0, complete = false)
                    .also { it.obligationsMetOnDate = listOf(2026) },
            ),
        )

        assertEquals("", table(vm).rows.single().paidDate)
    }

    @Test
    fun theCurrencyTravelsWithThePeriods() = runTest(dispatcher) {
        // Both come from the SAME emission. In the original the schedule and the loan were
        // separate requests, so a refresh landing between them could format one request's amounts
        // with the other's currency.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(disbursement(), installment(1, due = 1_000.0, paid = 0.0, complete = false)),
        )

        val data = table(vm)

        assertTrue(
            data.rows.single().due.contains("1,000"),
            "formatted with the loan's own currency: ${data.rows.single().due}",
        )
    }

    @Test
    fun aLoanWithNoScheduleAtAllProjectsAnEmptyTable() = runTest(dispatcher) {
        // `associations=all` can come back without a repaymentSchedule on a loan that has not been
        // approved. An empty table beats a crash, and the counters read zero.
        val vm = viewModel()
        loanState.value = ScreenState.Content(LoanWithAssociations(id = 7))

        val data = table(vm)

        assertNull(data.disbursementRow)
        assertTrue(data.rows.isEmpty())
        assertEquals(0, data.pendingCount)
    }

    @Test
    fun retryReachesTheRead() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(disbursement()))
        testScheduler.advanceUntilIdle()

        repository.refreshes.test {
            vm.actionChannel.trySend(LoanScheduleAction.OnRetry)
            testScheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun backNavigationIsAnEvent() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(LoanScheduleAction.NavigateBack)
            assertEquals(LoanScheduleEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
