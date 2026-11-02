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

package kpt.feature.loan.reschedules

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeLoanCommandRepository
import kpt.core.data.testing.FakeLoanRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse
import kpt.core.model.objects.account.loan.reschedules.RescheduleReasonOption
import kpt.core.model.objects.account.loan.reschedules.RescheduleStatus
import kpt.core.model.objects.account.loan.reschedules.RescheduleTimeline
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The PENDING precondition is this screen's logic, and the original checked it TWICE on purpose —
 * once on the icon tap and again at confirm. Both checks are tested.
 */
class LoanReschedulesViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun status(
        pending: Boolean = false,
        approved: Boolean = false,
        rejected: Boolean = false,
    ) = RescheduleStatus(
        id = 1,
        code = "c",
        value = "v",
        pendingApproval = pending,
        approved = approved,
        rejected = rejected,
    )

    private fun reschedule(
        id: Int,
        status: RescheduleStatus? = status(pending = true),
        from: List<Int>? = listOf(2026, 4, 1),
    ) = LoanRescheduleResponse(
        id = id,
        loanId = 7,
        rescheduleFromDate = from,
        rescheduleReasonCodeValue = RescheduleReasonOption(id = 3, name = "Crop failure"),
        statusEnum = status,
        timeline = RescheduleTimeline(
            submittedOnDate = listOf(2026, 3, 14),
            submittedByUsername = "r.kumar",
        ),
    )

    private val historyState =
        MutableStateFlow<ScreenState<List<LoanRescheduleResponse>>>(ScreenState.Loading)

    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        repository: FakeLoanRepository = FakeLoanRepository(loanReschedules = historyState),
        loanId: Int = 7,
    ) = LoanReschedulesViewModel(
        repository,
        commands,
        SavedStateHandle(mapOf("loanId" to loanId)),
    )

    private suspend fun rows(vm: LoanReschedulesViewModel): List<RescheduleRow> {
        var result: List<RescheduleRow>? = null
        vm.history.test {
            var item = awaitItem()
            while (item is ScreenState.Loading) item = awaitItem()
            result = (item as? ScreenState.Content)?.data ?: emptyList()
            cancelAndIgnoreRemainingEvents()
        }
        return assertNotNull(result)
    }

    // ---- the status derivation ----

    @Test
    fun theFourStatusesMapInTheOriginalsOrder() {
        assertEquals(RescheduleStatusCode.PENDING, status(pending = true).toStatusCode())
        assertEquals(RescheduleStatusCode.APPROVED, status(approved = true).toStatusCode())
        assertEquals(RescheduleStatusCode.REJECTED, status(rejected = true).toStatusCode())
        // All three false, and a null status object, both fall through.
        assertEquals(RescheduleStatusCode.UNKNOWN, status().toStatusCode())
        assertEquals(RescheduleStatusCode.UNKNOWN, null.toStatusCode())
    }

    @Test
    fun pendingWinsWhenTheServerSetsMoreThanOneFlag() {
        assertEquals(
            RescheduleStatusCode.PENDING,
            status(pending = true, approved = true).toStatusCode(),
        )
    }

    // ---- the rows ----

    @Test
    fun theRouteArgumentReachesTheRead() = runTest(dispatcher) {
        val repository = FakeLoanRepository(loanReschedules = historyState)
        viewModel(repository = repository)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf<Any>(7), repository.requestedKeys["loanReschedules"]?.toList())
    }

    @Test
    fun aRowCarriesItsDatesReasonAndSubmitter() = runTest(dispatcher) {
        val vm = viewModel()
        historyState.value = ScreenState.Content(listOf(reschedule(1)))

        val row = rows(vm).single()

        assertEquals(RescheduleStatusCode.PENDING, row.status)
        assertEquals("1 Apr 2026", row.rescheduleFrom)
        assertEquals("Crop failure", row.reason)
        assertEquals("14 Mar 2026", row.submittedOn)
        assertEquals("r.kumar", row.submittedBy)
    }

    @Test
    fun aPartialOrMissingDateRendersAPlaceholder() = runTest(dispatcher) {
        // `getDateAsString` indexes `[2]` directly, and every one of a reschedule's dates is
        // optional on the wire.
        val vm = viewModel()
        historyState.value = ScreenState.Content(
            listOf(reschedule(1, from = null), reschedule(2, from = listOf(2026))),
        )

        assertEquals(listOf("—", "—"), rows(vm).map { it.rescheduleFrom })
    }

    @Test
    fun onlyAPendingRowIsActionable() = runTest(dispatcher) {
        val vm = viewModel()
        historyState.value = ScreenState.Content(
            listOf(
                reschedule(1, status(pending = true)),
                reschedule(2, status(approved = true)),
                reschedule(3, status(rejected = true)),
                reschedule(4, status = null),
            ),
        )

        assertEquals(listOf(true, false, false, false), rows(vm).map { it.isActionable })
    }

    @Test
    fun aLoanWithNoReschedulesIsEmpty() = runTest(dispatcher) {
        val vm = viewModel()
        historyState.value = ScreenState.Content(emptyList())

        vm.history.test {
            assertIs<ScreenState.Loading>(awaitItem())
            // The STREAM reports Empty (its `isEmpty` predicate), not an empty Content list.
            assertIs<ScreenState.Empty>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- the double precondition check ----

    @Test
    fun tappingApproveOnANonPendingRowOffersNoConfirmation() = runTest(dispatcher) {
        // The FIRST check. The original's handlers `return@update it` for a non-pending row, so
        // the icons were drawn and inert; here they are not drawn at all AND the action is a no-op
        // if one somehow arrives.
        val vm = viewModel()
        historyState.value = ScreenState.Content(listOf(reschedule(1, status(approved = true))))
        testScheduler.advanceUntilIdle()

        val approved = rows(vm).single()
        vm.actionChannel.trySend(LoanReschedulesAction.OnApproveClick(approved))
        testScheduler.advanceUntilIdle()

        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun confirmingAnActionOnARowThatIsNoLongerPendingIsREFUSED() = runTest(dispatcher) {
        // The SECOND check, and the reason it exists: the list re-reads between the icon tap and
        // the confirmation, so a row can change status in between. The original checked again too.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        historyState.value = ScreenState.Content(listOf(reschedule(1, status(pending = true))))
        testScheduler.advanceUntilIdle()

        val pending = rows(vm).single()
        vm.actionChannel.trySend(LoanReschedulesAction.OnApproveClick(pending))
        testScheduler.advanceUntilIdle()
        assertIs<LoanReschedulesState.DialogState.ConfirmApprove>(vm.stateFlow.value.dialogState)

        // Someone else approved it while the confirmation was on screen.
        historyState.value = ScreenState.Content(listOf(reschedule(1, status(approved = true))))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanReschedulesAction.ConfirmApprove(1))
        testScheduler.advanceUntilIdle()

        assertTrue(commands.approvedReschedules.isEmpty(), "nothing is sent for a decided row")
        val dialog = assertIs<LoanReschedulesState.DialogState.Failed>(vm.stateFlow.value.dialogState)
        assertTrue(dialog.msg.contains("no longer pending"), "message was ${dialog.msg}")
    }

    // ---- approve / reject ----

    @Test
    fun approvingSendsTodayPairedWithItsDeclaredFormat() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        historyState.value = ScreenState.Content(listOf(reschedule(1)))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanReschedulesAction.ConfirmApprove(1))
        testScheduler.advanceUntilIdle()

        val (id, request) = commands.approvedReschedules.single()
        assertEquals(1, id)
        // This VM was one of the few in the original already using `ApiDateFormatter` correctly.
        assertEquals(ApiDateFormatter.DATE_FORMAT, request.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, request.locale)
        assertTrue(request.approvedOnDate.isNotBlank())
    }

    @Test
    fun rejectingSendsAREJECTIONNotADeletion() = runTest(dispatcher) {
        // The original named this "delete" throughout — icon, action and repository method — but
        // the wire command is `?command=reject` with a rejection payload carrying a date and a
        // user. Named for what it does.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        historyState.value = ScreenState.Content(listOf(reschedule(1)))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanReschedulesAction.ConfirmReject(1))
        testScheduler.advanceUntilIdle()

        val (id, request) = commands.rejectedReschedules.single()
        assertEquals(1, id)
        assertTrue(request.rejectedOnDate.isNotBlank())
        assertEquals(ApiDateFormatter.DATE_FORMAT, request.dateFormat)
    }

    @Test
    fun aSuccessfulActionDoesNotReReadTheList() = runTest(dispatcher) {
        val repository = FakeLoanRepository(loanReschedules = historyState)
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands, repository)
        historyState.value = ScreenState.Content(listOf(reschedule(1)))
        testScheduler.advanceUntilIdle()

        repository.refreshes.test {
            vm.actionChannel.trySend(LoanReschedulesAction.ConfirmApprove(1))
            testScheduler.advanceUntilIdle()
            // The command records the new status through the reschedules store, which this
            // list observes — a re-read here is the post-write refresh the write door replaced.
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.stateFlow.value.actingOn)
    }

    @Test
    fun aSecondActionWhileOneIsInFlightIsIgnored() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeLoanCommandRepository(gate = gate)
        val vm = viewModel(commands)
        historyState.value = ScreenState.Content(listOf(reschedule(1), reschedule(2)))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanReschedulesAction.ConfirmApprove(1))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanReschedulesAction.ConfirmApprove(2))
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.approvedReschedules.size)
        assertEquals(1, vm.stateFlow.value.actingOn)
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.actingOn)
    }

    @Test
    fun offlineSaysNothingWasSent() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            rescheduleResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        historyState.value = ScreenState.Content(listOf(reschedule(1)))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanReschedulesAction.ConfirmApprove(1))
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanReschedulesState.DialogState.Failed>(vm.stateFlow.value.dialogState)
        assertTrue(dialog.msg.contains("No connection"), "message was ${dialog.msg}")
        assertNull(vm.stateFlow.value.actingOn)
    }

    @Test
    fun aRejectionSurfacesFineractsOwnReason() = runTest(dispatcher) {
        // The original showed one fixed string per action — "could not approve" — discarding the
        // server's explanation.
        val commands = FakeLoanCommandRepository(
            rescheduleResult = {
                MutationResult.Failed(
                    fineractRejection("loan is not active"),
                    rolledBack = false,
                )
            },
        )
        val vm = viewModel(commands)
        historyState.value = ScreenState.Content(listOf(reschedule(1)))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanReschedulesAction.ConfirmApprove(1))
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanReschedulesState.DialogState.Failed>(vm.stateFlow.value.dialogState)
        assertEquals("loan is not active", dialog.msg)
    }

    @Test
    fun theConfirmationCanBeDismissedWithoutActing() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        historyState.value = ScreenState.Content(listOf(reschedule(1)))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanReschedulesAction.OnRejectClick(rows(vm).single()))
        testScheduler.advanceUntilIdle()
        assertIs<LoanReschedulesState.DialogState.ConfirmReject>(vm.stateFlow.value.dialogState)

        vm.actionChannel.trySend(LoanReschedulesAction.DismissDialog)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.dialogState)
        assertTrue(commands.rejectedReschedules.isEmpty())
    }

    @Test
    fun retryAndRefreshBothReachTheRead() = runTest(dispatcher) {
        val repository = FakeLoanRepository(loanReschedules = historyState)
        val vm = viewModel(repository = repository)
        historyState.value = ScreenState.Content(listOf(reschedule(1)))
        testScheduler.advanceUntilIdle()

        repository.refreshes.test {
            vm.actionChannel.trySend(LoanReschedulesAction.OnRefresh)
            testScheduler.advanceUntilIdle()
            awaitItem()
            // ONE member for both the pull gesture and the error-state retry — see
            // `RefreshableScreenContent`, and note `retry()` is `refresh(forceFresh = true)`.
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun proposeCarriesTheLoanId() = runTest(dispatcher) {
        val vm = viewModel(loanId = 7)
        vm.eventFlow.test {
            vm.actionChannel.trySend(LoanReschedulesAction.OnPropose)
            assertEquals(LoanReschedulesEvent.Propose(loanId = 7), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun backNavigationIsAnEvent() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(LoanReschedulesAction.NavigateBack)
            assertEquals(LoanReschedulesEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
