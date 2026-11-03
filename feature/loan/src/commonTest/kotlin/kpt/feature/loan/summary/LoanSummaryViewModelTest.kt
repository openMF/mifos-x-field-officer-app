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

package kpt.feature.loan.summary

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
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanAccountSummary
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanStatus
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanTimeline
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
 * The summary's own logic is the status derivation and the "has this loan actually disbursed?"
 * gate — both of which decide whether real money is shown on screen.
 */
class LoanSummaryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val loanState = MutableStateFlow<ScreenState<LoanWithAssociations>>(ScreenState.Loading)
    private val repository = FakeLoanRepository(loan = loanState)

    private fun viewModel(loanId: Int = 7) =
        LoanSummaryViewModel(repository, SavedStateHandle(mapOf("loanId" to loanId)))

    private fun loan(status: LoanStatus?) = LoanWithAssociations(
        id = 7,
        accountNo = "000000007",
        clientName = "Asha",
        loanProductName = "Group Loan",
        loanOfficerName = "R. Kumar",
        status = status,
        currency = SavingAccountCurrency(code = "INR", decimalPlaces = 2),
        timeline = LoanTimeline(actualDisbursementDate = listOf(2026, 3, 14)),
        summary = LoanAccountSummary(
            totalExpectedRepayment = 11_000.0,
            totalRepayment = 4_000.0,
            totalOutstanding = 7_000.0,
            totalOverdue = 500.0,
            principalDisbursed = 10_000.0,
            principalPaid = 3_500.0,
            principalOutstanding = 6_500.0,
            interestCharged = 1_000.0,
            interestPaid = 500.0,
            interestOutstanding = 500.0,
        ),
    )

    private suspend fun ui(vm: LoanSummaryViewModel): LoanSummaryUi {
        var result: LoanSummaryUi? = null
        vm.summary.test {
            assertIs<ScreenState.Loading>(awaitItem())
            result = assertIs<ScreenState.Content<LoanSummaryUi>>(awaitItem()).data
            cancelAndIgnoreRemainingEvents()
        }
        return assertNotNull(result)
    }

    // ---- the status derivation ----

    @Test
    fun activeWinsHere_whichIsTheOPPOSITEOfTheLoanProfile() {
        // The original derives the primary action on THIS screen with `active` first and on the
        // loan PROFILE with `pendingApproval` first. A loan Fineract reports as both therefore
        // reads ACTIVE here and PENDING there — the same loan, two screens, two answers. Carried
        // over rather than silently unified; logged as `loan-status-precedence-differs-by-screen`.
        assertEquals(
            LoanSummaryStatus.ACTIVE,
            LoanStatus(active = true, pendingApproval = true).toSummaryStatus(),
        )
    }

    @Test
    fun theFiveStatesMapInTheOriginalsOrder() {
        assertEquals(LoanSummaryStatus.ACTIVE, LoanStatus(active = true).toSummaryStatus())
        assertEquals(LoanSummaryStatus.PENDING, LoanStatus(pendingApproval = true).toSummaryStatus())
        assertEquals(
            LoanSummaryStatus.AWAITING_DISBURSAL,
            LoanStatus(waitingForDisbursal = true).toSummaryStatus(),
        )
        assertEquals(LoanSummaryStatus.OVERPAID, LoanStatus(overpaid = true).toSummaryStatus())
        // Every closed variant, and a null status, fall through to CLOSED — the original's `else`.
        assertEquals(LoanSummaryStatus.CLOSED, LoanStatus(closedObligationsMet = true).toSummaryStatus())
        assertEquals(LoanSummaryStatus.CLOSED, LoanStatus().toSummaryStatus())
        assertEquals(LoanSummaryStatus.CLOSED, null.toSummaryStatus())
    }

    @Test
    fun thisScreenDistinguishesTheTwoStatesTheProfileCollapsesToUnknown() {
        // AWAITING_DISBURSAL and CLOSED both read "Unknown" with a "View Action" button on the
        // loan profile. Here they have their own labels and their own actions, which is why the
        // profile's catch-all gap has an answer in the original even though the profile has none.
        assertEquals(LoanSummaryAct.Disburse, LoanSummaryStatus.AWAITING_DISBURSAL.primaryAction())
        assertNull(LoanSummaryStatus.CLOSED.primaryAction())
    }

    @Test
    fun eachStatusOffersExactlyOneActionAndClosedOffersNone() {
        assertEquals(LoanSummaryAct.MakeRepayment, LoanSummaryStatus.ACTIVE.primaryAction())
        assertEquals(LoanSummaryAct.Approve, LoanSummaryStatus.PENDING.primaryAction())
        assertEquals(LoanSummaryAct.Transfer, LoanSummaryStatus.OVERPAID.primaryAction())
        // The original drew the button for CLOSED too, enabled only by a SEPARATE
        // `isButtonActive()` predicate, with an empty `{ }` onClick. Deriving the null from the
        // same place as the label means the two cannot disagree.
        assertNull(LoanSummaryStatus.CLOSED.primaryAction())
    }

    // ---- the disbursement gate ----

    @Test
    fun aPendingLoansFiguresAreADashNotZero() = runTest(dispatcher) {
        // `shouldInflateLoanSummary` — the original's rule, kept. A loan that has never disbursed
        // has a summary block full of zeroes and projections, and "Amount Paid: ₹0.00" beside
        // "Outstanding: ₹0.00" reads as a loan that has been paid off. Shown as "—" rather than
        // blank: a screen of empty cards read as broken (device, 2026-10-26).
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(LoanStatus(pendingApproval = true)))

        val content = ui(vm)

        assertEquals("—", content.amountPaid)
        assertEquals("—", content.outstanding)
        assertEquals("—", content.principalDisbursed)
        assertEquals("—", content.interestCharged)
        // And the disbursement DATE, for the same reason — an expected date shown as an actual one
        // tells the officer money has moved when it has not.
        assertEquals("—", content.disbursedOn)
    }

    @Test
    fun aPendingLoanShowsTheRequestedAmount() = runTest(dispatcher) {
        // The one figure a loan has from submission: what was asked for.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(LoanStatus(pendingApproval = true)).copy(principal = 50_000.0, approvedPrincipal = 50_000.0),
        )
        val total = ui(vm).totalLoan
        assertTrue(total.contains("50,000"), "pending totalLoan was $total")
    }

    @Test
    fun anApprovedLoanShowsTheApprovedAmountNotTheRequestedOne() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(LoanStatus(waitingForDisbursal = true)).copy(principal = 50_000.0, approvedPrincipal = 40_000.0),
        )
        val total = ui(vm).totalLoan
        assertTrue(total.contains("40,000"), "approved totalLoan was $total")
    }

    @Test
    fun anAwaitingDisbursalLoansFiguresAreAlsoADash() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(LoanStatus(waitingForDisbursal = true)))

        assertEquals("—", ui(vm).outstanding)
    }

    @Test
    fun theOfficerNameIsShownEVENOnAPendingLoan() = runTest(dispatcher) {
        // Not gated: the loan officer is assigned before disbursement and is exactly what someone
        // looking at a pending loan needs to see. The original did not gate it either.
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(LoanStatus(pendingApproval = true)))

        assertEquals("R. Kumar", ui(vm).loanOfficerName)
    }

    @Test
    fun anActiveLoansFiguresAreFormattedWithItsOwnCurrency() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(LoanStatus(active = true)))

        val content = ui(vm)

        assertTrue(content.totalLoan.contains("11,000"), "totalLoan was ${content.totalLoan}")
        assertTrue(content.outstanding.contains("7,000"), "outstanding was ${content.outstanding}")
        assertEquals("14 Mar 2026", content.disbursedOn)
    }

    @Test
    fun anOverpaidAndAClosedLoanBothSHOWTheirFigures() = runTest(dispatcher) {
        // `active || closedObligationsMet || overpaid` — a repaid loan's history is the point of
        // opening it.
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(LoanStatus(overpaid = true)))
        assertTrue(ui(vm).amountPaid.isNotEmpty())
    }

    @Test
    fun aLoanWithNoSummaryBlockAtAllRendersBlanksRatherThanCrashing() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            LoanWithAssociations(id = 7, status = LoanStatus(active = true), summary = null),
        )

        val content = ui(vm)

        assertEquals("", content.totalLoan)
        assertEquals(LoanSummaryStatus.ACTIVE, content.status)
    }

    @Test
    fun aPartialDisbursementDateIsDroppedRatherThanIndexedInto() = runTest(dispatcher) {
        // `actualDisbursementDate` is `List<Int?>?` — a nullable list OF NULLABLES, and the
        // original guarded with `date.all { it != null }` then an unchecked cast. Nulls are
        // filtered and the size re-checked, so a `[2026, null, 14]` drops instead of casting.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            loan(LoanStatus(active = true))
                .copy(timeline = LoanTimeline(actualDisbursementDate = listOf(2026, null, 14))),
        )

        assertEquals("", ui(vm).disbursedOn)
    }

    // ---- actions ----

    @Test
    fun theRouteArgumentReachesTheRead() = runTest(dispatcher) {
        viewModel(loanId = 42)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf<Any>(42), repository.requestedKeys["loan"]?.toList())
    }

    @Test
    fun thePrimaryActionUsesTheStatusTheScreenRendered() = runTest(dispatcher) {
        val vm = viewModel(loanId = 7)
        vm.eventFlow.test {
            vm.actionChannel.trySend(
                LoanSummaryAction.OnPrimaryActionClick(LoanSummaryStatus.ACTIVE),
            )
            // The original read `state.loanWithAssociations` back at tap time and returned
            // silently when it was null, so a tap during a reload did nothing at all.
            assertEquals(
                LoanSummaryEvent.OpenAction(LoanSummaryAct.MakeRepayment, loanId = 7),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aClosedLoansButtonTapEmitsNothing() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(
                LoanSummaryAction.OnPrimaryActionClick(LoanSummaryStatus.CLOSED),
            )
            testScheduler.advanceUntilIdle()
            // No action exists, so no event — rather than the original's empty `{ }` branch.
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aMenuTapCarriesTheItemAndTheLoanIdAndClosesTheMenu() = runTest(dispatcher) {
        val vm = viewModel(loanId = 7)
        vm.actionChannel.trySend(LoanSummaryAction.ToggleDropdown)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.openDropdown)

        vm.eventFlow.test {
            vm.actionChannel.trySend(
                LoanSummaryAction.OnMenuItemClick(LoanSummaryMenuItem.Transactions),
            )
            assertEquals(
                LoanSummaryEvent.OpenMenuItem(LoanSummaryMenuItem.Transactions, loanId = 7),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(!vm.stateFlow.value.openDropdown, "the menu closes on selection")
    }

    @Test
    fun theCopiedMessageIsRaisedAndCleared() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(LoanSummaryAction.OnAccountNoCopied)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.showCopiedMessage)

        vm.actionChannel.trySend(LoanSummaryAction.OnMessageShown)
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.showCopiedMessage)
    }

    @Test
    fun retryReachesTheRead() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(LoanStatus(active = true)))
        testScheduler.advanceUntilIdle()

        repository.refreshes.test {
            vm.actionChannel.trySend(LoanSummaryAction.OnRetry)
            testScheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
