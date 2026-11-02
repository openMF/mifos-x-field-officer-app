/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.checkerinbox.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.dataOrNull
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeCheckerInboxCommandRepository
import kpt.core.data.testing.FakeCheckerInboxRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.model.objects.checkerinboxtask.RescheduleLoansTask
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalDataTestingApi::class, ExperimentalTime::class)
class CheckerInboxViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    /** Noon local time, so the day is the same in any time zone the test runs in. */
    private fun on(date: String) =
        LocalDate.parse(date).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds() + 12 * 3_600_000L

    private val tasks = listOf(
        CheckerTask(1, on("2026-10-01"), "", "asha", "CREATE", "CLIENT", "11"),
        CheckerTask(2, on("2026-10-10"), "", "bimal", "APPROVE", "LOAN", "22"),
        CheckerTask(3, on("2026-10-20"), "", "asha", "CREATE", "LOAN", "33"),
    )

    private fun TestScope.started(
        inbox: FakeCheckerInboxRepository = FakeCheckerInboxRepository(tasks),
        commands: FakeCheckerInboxCommandRepository = FakeCheckerInboxCommandRepository(inbox),
    ) = CheckerInboxViewModel(inbox, commands).also { vm ->
        backgroundScope.launch { vm.inbox.collect {} }
        backgroundScope.launch { vm.reschedules.collect {} }
        runCurrent()
    }

    private val CheckerInboxViewModel.shown get() = inbox.value.dataOrNull!!.tasks.map { it.id }

    @Test
    fun newestFirstAndSearchMatchesTheMaker() = runTest(dispatcher) {
        val vm = started()
        assertEquals(listOf(3, 2, 1), vm.shown)
        vm.trySendAction(CheckerInboxAction.QueryChanged("ASH"))
        runCurrent()
        assertEquals(listOf(3, 1), vm.shown)
    }

    @Test
    fun eachFilterCriterionAppliesOnItsOwn() = runTest(dispatcher) {
        val vm = started()
        fun apply(filter: InboxFilter) {
            vm.trySendAction(CheckerInboxAction.FilterOpened)
            vm.trySendAction(CheckerInboxAction.DraftChanged { filter })
            vm.trySendAction(CheckerInboxAction.FilterApplied)
            runCurrent()
        }
        // The original ignored entity unless a date was set.
        apply(InboxFilter(entity = "loan"))
        assertEquals(listOf(3, 2), vm.shown)
        // The original returned NOTHING for a From date alone.
        apply(InboxFilter(from = LocalDate.parse("2026-10-10")))
        assertEquals(listOf(3, 2), vm.shown)
        // Both ends inclusive.
        apply(InboxFilter(from = LocalDate.parse("2026-10-01"), to = LocalDate.parse("2026-10-10")))
        assertEquals(listOf(2, 1), vm.shown)
        apply(InboxFilter(resourceId = " 33 "))
        assertEquals(listOf(3), vm.shown)
        // Nothing matches → an empty RESULT, not an empty inbox.
        apply(InboxFilter(action = "DELETE"))
        assertEquals(emptyList(), vm.shown)
        assertEquals(3, vm.inbox.value.dataOrNull!!.total)
    }

    @Test
    fun anInvertedDateRangeIsNotApplied() = runTest(dispatcher) {
        val vm = started()
        vm.trySendAction(CheckerInboxAction.FilterOpened)
        vm.trySendAction(
            CheckerInboxAction.DraftChanged {
                it.copy(from = LocalDate.parse("2026-10-20"), to = LocalDate.parse("2026-10-01"))
            },
        )
        vm.trySendAction(CheckerInboxAction.FilterApplied)
        runCurrent()
        assertEquals(InboxFilter(), vm.stateFlow.value.filter)
        assertEquals(listOf(3, 2, 1), vm.shown)
    }

    @Test
    fun aBulkDecisionReportsEachFailureAndKeepsItSelected() = runTest(dispatcher) {
        val inbox = FakeCheckerInboxRepository(tasks)
        val commands = FakeCheckerInboxCommandRepository(inbox) { id ->
            if (id == 2) {
                MutationResult.Failed(fineractRejection("Maker cannot check own task"), rolledBack = false)
            } else {
                MutationResult.Applied(kpt.core.model.shared.GenericResponse(), synced = true)
            }
        }
        val vm = started(inbox, commands)
        vm.trySendAction(CheckerInboxAction.TaskLongPressed(1))
        vm.trySendAction(CheckerInboxAction.TaskClicked(2)) // a tap while selecting toggles
        vm.trySendAction(CheckerInboxAction.TaskClicked(3))
        vm.trySendAction(CheckerInboxAction.DecideRequested(Decision.APPROVE))
        vm.trySendAction(CheckerInboxAction.DecideConfirmed)
        runCurrent()

        assertEquals(listOf("approve" to 1, "approve" to 2, "approve" to 3), commands.decisions)
        val outcome = vm.stateFlow.value.outcome!!
        assertEquals(2, outcome.done)
        assertEquals(listOf(DecisionFailure("#2 APPROVE LOAN", "Maker cannot check own task")), outcome.failures)
        // Decided tasks left the list through the store; the failure stays selected to retry.
        assertEquals(listOf(2), vm.shown)
        assertEquals(setOf(2), vm.stateFlow.value.selected)
    }

    @Test
    fun goingOfflineStopsTheBatchAndSaysSo() = runTest(dispatcher) {
        val inbox = FakeCheckerInboxRepository(tasks)
        val commands = FakeCheckerInboxCommandRepository(inbox) { MutationResult.Blocked(BlockReason.OFFLINE) }
        val vm = started(inbox, commands)
        vm.trySendAction(CheckerInboxAction.TaskLongPressed(1))
        vm.trySendAction(CheckerInboxAction.TaskClicked(2))
        vm.trySendAction(CheckerInboxAction.DecideRequested(Decision.REJECT))
        vm.trySendAction(CheckerInboxAction.DecideConfirmed)
        runCurrent()

        assertEquals(1, commands.decisions.size)
        assertTrue(vm.stateFlow.value.outcome!!.offline)
        assertEquals(setOf(1, 2), vm.stateFlow.value.selected)
    }

    @Test
    fun oneTasksOwnButtonDecidesOnlyThatTask() = runTest(dispatcher) {
        val inbox = FakeCheckerInboxRepository(tasks)
        val commands = FakeCheckerInboxCommandRepository(inbox)
        val vm = started(inbox, commands)
        vm.trySendAction(CheckerInboxAction.TaskClicked(3))
        runCurrent()
        assertEquals(3, vm.stateFlow.value.expanded)
        vm.trySendAction(CheckerInboxAction.DecideRequested(Decision.DELETE, id = 3))
        runCurrent()
        assertEquals(PendingDecision(Decision.DELETE, setOf(3)), vm.stateFlow.value.confirm)
        vm.trySendAction(CheckerInboxAction.DecideCancelled)
        runCurrent()
        assertTrue(commands.decisions.isEmpty())
        assertNull(vm.stateFlow.value.confirm)
    }

    @Test
    fun aRescheduleOpensItsLoan() = runTest(dispatcher) {
        val request = RescheduleLoansTask(id = 4, loanId = 77, clientName = "Asha")
        val vm = started(FakeCheckerInboxRepository(reschedules = listOf(request)))
        vm.eventFlow.test {
            vm.trySendAction(CheckerInboxAction.RescheduleClicked(request))
            runCurrent()
            assertEquals(CheckerInboxEvent.OpenLoanReschedules(77), awaitItem())
        }
    }
}
