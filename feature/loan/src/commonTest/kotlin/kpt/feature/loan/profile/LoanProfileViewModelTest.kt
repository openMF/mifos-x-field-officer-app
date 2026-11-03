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

package kpt.feature.loan.profile

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
import kpt.core.data.testing.FakeRecentRecordsRepository
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanStatus
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.feature.loan.profile.components.LoanProfileActionItem
import kpt.feature.loan.profile.components.loanProfileActionItems
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * The status derivation is this screen's logic — it decides the chip, its colour AND which action
 * the primary button offers, so all three must come from one answer.
 */
class LoanProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val loanState = MutableStateFlow<ScreenState<LoanWithAssociations>>(ScreenState.Loading)

    /**
     * The SHARED fake, not an anonymous `object : LoanRepository`.
     *
     * This test used to hand-roll one, listing every member — and that is exactly the maintenance
     * cost `FakeLoanRepository` was introduced to remove. It broke when `loanChargesStream` was
     * added in S4f, in a test that has nothing to say about loan charges. One fake, one place to
     * add a member.
     */
    private val repository = FakeLoanRepository(loan = loanState)

    private fun viewModel(loanId: Int = 7) =
        LoanProfileViewModel(repository, SavedStateHandle(mapOf("loanId" to loanId)), FakeRecentRecordsRepository())

    private fun loan(status: LoanStatus?) =
        LoanWithAssociations(id = 7, clientName = "Asha", status = status)

    // ---- the status derivation ----

    @Test
    fun pendingApprovalWins() {
        assertEquals(
            LoanProfileStatus.PENDING,
            LoanStatus(pendingApproval = true, active = true, overpaid = true).toProfileStatus(),
        )
    }

    @Test
    fun overpaidBeatsActive() {
        // The precedence is load-bearing: Fineract can report a loan BOTH active and overpaid, and
        // the original tested `overpaid` first — so it showed OVERPAID with a Transfer button, not
        // ACTIVE with a Repayment one.
        assertEquals(
            LoanProfileStatus.OVERPAID,
            LoanStatus(active = true, overpaid = true).toProfileStatus(),
        )
    }

    @Test
    fun activeIsTheFallbackAmongTheThree() {
        assertEquals(LoanProfileStatus.ACTIVE, LoanStatus(active = true).toProfileStatus())
    }

    @Test
    fun aNullStatusAndEveryOtherFineractStateAreUNKNOWN() {
        assertEquals(LoanProfileStatus.UNKNOWN, null.toProfileStatus())
        // Carried over deliberately: `waitingForDisbursal` and the three `closed*` flags all fall
        // through to UNKNOWN, so a closed loan reads "Unknown" with a "View Action" button. That
        // is the original's behaviour; widening it is a product question.
        assertEquals(
            LoanProfileStatus.UNKNOWN,
            LoanStatus(waitingForDisbursal = true).toProfileStatus(),
        )
        assertEquals(LoanProfileStatus.UNKNOWN, LoanStatus(closed = true).toProfileStatus())
        assertEquals(
            LoanProfileStatus.UNKNOWN,
            LoanStatus(closedObligationsMet = true).toProfileStatus(),
        )
    }

    @Test
    fun eachStatusOffersExactlyOnePrimaryActionAndUnknownOffersNone() {
        assertEquals(LoanPrimaryAction.Approve, LoanProfileStatus.PENDING.primaryAction())
        assertEquals(LoanPrimaryAction.Transfer, LoanProfileStatus.OVERPAID.primaryAction())
        assertEquals(LoanPrimaryAction.Repayment, LoanProfileStatus.ACTIVE.primaryAction())
        // UNKNOWN falls through to the account-details screen rather than offering an action.
        assertNull(LoanProfileStatus.UNKNOWN.primaryAction())
    }

    // ---- the ViewModel ----

    @Test
    fun theRouteArgumentReachesTheRead() = runTest(dispatcher) {
        viewModel(loanId = 42)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf<Any>(42), repository.requestedKeys["loan"]?.toList())
    }

    @Test
    fun theStatusIsDerivedFromTheSameEmissionTheDetailRenders() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(LoanStatus(active = true)))

        vm.status.test {
            assertIs<ScreenState.Loading>(awaitItem())
            // Derived, so the chip, its colour and the button cannot disagree about the loan's
            // state — the class of bug that shipped on the group screen.
            assertEquals(
                LoanProfileStatus.ACTIVE,
                assertIs<ScreenState.Content<LoanProfileStatus>>(awaitItem()).data,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun retryAlwaysReachesTheReadEvenWhenOffline() = runTest(dispatcher) {
        // THE REGRESSION. The original's retry checked its own `networkConnection` flag and, when
        // false, showed "details not found" instead of retrying. Worse, its loader was gated on
        // `loanAccount == null`, so once a loan had loaded it was NEVER re-read — not on
        // reconnect, not on Retry.
        val vm = viewModel()
        loanState.value = ScreenState.Content(loan(LoanStatus(active = true)))
        testScheduler.advanceUntilIdle()

        repository.refreshes.test {
            vm.actionChannel.trySend(LoanProfileAction.OnRetry)
            testScheduler.advanceUntilIdle()
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun thePrimaryActionUsesTheStatusTheScreenRenderedNotAReRead() = runTest(dispatcher) {
        val vm = viewModel(loanId = 7)
        vm.eventFlow.test {
            vm.actionChannel.trySend(
                LoanProfileAction.OnPrimaryActionClick(LoanProfileStatus.PENDING),
            )
            // The original read `state.loanAccount` at tap time and returned silently if it was
            // null, so a tap during load did nothing at all.
            assertEquals(
                LoanProfileEvent.OpenAction(LoanPrimaryAction.Approve, loanId = 7),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun anUnknownStatusTapFallsThroughToAccountDetails() = runTest(dispatcher) {
        val vm = viewModel(loanId = 7)
        vm.eventFlow.test {
            vm.actionChannel.trySend(
                LoanProfileAction.OnPrimaryActionClick(LoanProfileStatus.UNKNOWN),
            )
            assertEquals(LoanProfileEvent.OpenAccountDetails(loanId = 7), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aDetailTapCarriesBothTheItemAndTheLoanId() = runTest(dispatcher) {
        val vm = viewModel(loanId = 7)
        vm.eventFlow.test {
            vm.actionChannel.trySend(
                LoanProfileAction.OnDetailItemClick(LoanProfileActionItem.Transactions),
            )
            assertEquals(
                LoanProfileEvent.OpenDetail(LoanProfileActionItem.Transactions, loanId = 7),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun allThirteenDetailItemsAreDeclaredInTheOriginalsOrder() {
        assertEquals(13, loanProfileActionItems.size)
        assertEquals(LoanProfileActionItem.General, loanProfileActionItems.first())
        assertEquals(LoanProfileActionItem.StandingInstructions, loanProfileActionItems.last())
    }
}
