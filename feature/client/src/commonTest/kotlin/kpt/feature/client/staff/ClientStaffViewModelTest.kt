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

package kpt.feature.client.staff

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeClientCommandRepository
import kpt.core.data.testing.FakeClientRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.client.ClientTemplateOptions
import kpt.core.model.client.StaffOption
import kpt.core.ui.components.ResultStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Locks what the port changed in `clientStaff`, and the one defect it carried over deliberately.
 */
class ClientStaffViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val template = MutableStateFlow<ScreenState<ClientTemplateOptions>>(ScreenState.Loading)

    private val repository = FakeClientRepository(template = template)

    private fun viewModel(
        commands: FakeClientCommandRepository = FakeClientCommandRepository(),
        clientId: Int = 42,
        // The key is "clientId" — the property name on ClientStaffRoute — not "id". This helper
        // seeded "id", which matched the ViewModel's (wrong) constant, so the two agreed with each
        // other and disagreed with the route. Both tests below passed while the shipped screen read
        // nothing and fell through to its `?: -1` default on every real open.
    ) = ClientStaffViewModel(repository, commands, SavedStateHandle(mapOf("clientId" to clientId)))

    private fun options(vararg names: String) = ClientTemplateOptions(
        staffOptions = names.mapIndexed { i, n -> StaffOption(id = i + 1, displayName = n) },
    )

    @Test
    fun theRouteArgumentReachesTheRead() = runTest(dispatcher) {
        viewModel(clientId = 7)
        testScheduler.advanceUntilIdle()
        // The original's route defaulted `id` to -1, so a missing key asked Fineract for client -1.
        assertEquals(listOf<Any>(7), repository.requestedKeys["template"]?.toList())
    }

    @Test
    fun theStaffListIsProjectedFromTheTemplateStream() = runTest(dispatcher) {
        val vm = viewModel()
        template.value = ScreenState.Content(options("Asha", "Bimal"))

        vm.staffOptions.test {
            // `awaitItem`, not `expectMostRecentItem`: WhileSubscribed means nothing flows until
            // this collector attaches, so the first item is still Loading and the Content arrives
            // only once the scheduler runs. `expectMostRecentItem` does not wait for it and reads
            // the initial value — a test that passes for the wrong reason, which bit this port
            // once already.
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<List<StaffOption>>>(awaitItem())
            // The screen reads STAFF, not the template envelope — that projection is what keeps
            // `core:network` off this module's classpath.
            assertEquals(listOf("Asha", "Bimal"), content.data.map { it.displayName })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailedReadStaysAReadStateRatherThanBecomingADialog() = runTest(dispatcher) {
        val vm = viewModel()
        template.value = ScreenState.Error(RuntimeException("boom"))
        testScheduler.advanceUntilIdle()

        vm.staffOptions.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Error>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // The original raised `DialogState.Error` over an empty form. Loading and Error are states
        // of the content area now; only the submit RESULT is still a dialog.
        assertEquals(null, vm.stateFlow.value.dialogState)
    }

    @Test
    fun retryReachesTheStream() = runTest(dispatcher) {
        val vm = viewModel()
        repository.refreshesFor("template").test {
            vm.actionChannel.trySend(ClientStaffAction.OnRetry)
            testScheduler.advanceUntilIdle()
            // The original's retry re-ran a `try/catch` load AND re-subscribed the network observer.
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun submitSendsTheStaffIdTheScreenChoseAndReReadsTheTemplate() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository()
        val vm = viewModel(commands)
        template.value = ScreenState.Content(options("Asha", "Bimal"))
        testScheduler.advanceUntilIdle()

        repository.refreshesFor("template").test {
            vm.actionChannel.trySend(ClientStaffAction.OnSubmit(staffId = 2))
            testScheduler.advanceUntilIdle()

            // The id comes from the ACTION. The original re-indexed `staffOptions[index]` at submit
            // time, against a list the ViewModel no longer owns.
            assertEquals(listOf(42 to 2), commands.assignedStaff)
            // The assignment changes the template this screen reads, so it is re-read — without
            // this, going back and returning shows the pre-assignment option set from cache.
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }

        val dialog = assertIs<ClientStaffState.DialogState.ShowStatusDialog>(vm.stateFlow.value.dialogState)
        assertEquals(ResultStatus.SUCCESS, dialog.status)
    }

    @Test
    fun aSecondSubmitInsideTheInFlightWindowIsDropped() = runTest(dispatcher) {
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val commands = FakeClientCommandRepository(gate = gate)
        val vm = viewModel(commands)
        template.value = ScreenState.Content(options("Asha"))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientStaffAction.OnSubmit(staffId = 1))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(ClientStaffAction.OnSubmit(staffId = 1))
        testScheduler.advanceUntilIdle()

        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        // Two POSTs would assign twice and surface a spurious second result. Same guard every
        // other write on this substrate carries.
        assertEquals(1, commands.assignedStaff.size)
    }

    @Test
    fun anOfflineSubmitSaysSoRatherThanReportingAGenericFailure() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository(
            unitResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        template.value = ScreenState.Content(options("Asha"))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientStaffAction.OnSubmit(staffId = 1))
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<ClientStaffState.DialogState.ShowStatusDialog>(vm.stateFlow.value.dialogState)
        assertEquals(ResultStatus.FAILURE, dialog.status)
        assertTrue(dialog.msg.contains("No connection"), "offline should be named: ${dialog.msg}")
    }

    @Test
    fun aRejectionKeepsTheServersReason() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository(
            unitResult = { MutationResult.Failed(fineractRejection("staff not in client office"), rolledBack = false) },
        )
        val vm = viewModel(commands)
        template.value = ScreenState.Content(options("Asha"))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientStaffAction.OnSubmit(staffId = 1))
        testScheduler.advanceUntilIdle()

        // A Fineract rejection reason is the only actionable thing the officer gets; replacing it
        // with a generic string loses it. The original surfaced it and so does this.
        val dialog = assertIs<ClientStaffState.DialogState.ShowStatusDialog>(vm.stateFlow.value.dialogState)
        assertEquals("staff not in client office", dialog.msg)
    }

    @Test
    fun closingTheFailureDialogDoesNotNavigateForward() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(ClientStaffAction.CloseDialog)
        testScheduler.advanceUntilIdle()
        assertEquals(null, vm.stateFlow.value.dialogState)
    }
}
