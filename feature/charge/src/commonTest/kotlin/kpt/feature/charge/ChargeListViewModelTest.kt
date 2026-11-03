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

package kpt.feature.charge

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeChargeCommandRepository
import kpt.core.data.testing.FakeChargeRepository
import kpt.core.model.charge.Charges
import kpt.core.model.objects.charge.ChargeResource
import kpt.feature.charge.list.ChargeListAction
import kpt.feature.charge.list.ChargeListEvent
import kpt.feature.charge.list.ChargeListViewModel
import kpt.feature.charge.list.ChargeWriteError
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChargeListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun handle(resource: ChargeResource, id: Int) =
        SavedStateHandle(mapOf("resourceType" to resource.path, "resourceId" to id))

    private fun viewModel(
        repository: FakeChargeRepository = FakeChargeRepository(),
        commands: FakeChargeCommandRepository = FakeChargeCommandRepository(),
        resource: ChargeResource = ChargeResource.LOAN,
        id: Int = 7,
    ) = ChargeListViewModel(repository, commands, handle(resource, id))

    @Test
    fun theRouteSelectsTheResourceThatIsRead() = runTest(dispatcher) {
        for (resource in ChargeResource.entries) {
            val repository = FakeChargeRepository()
            viewModel(repository = repository, resource = resource, id = 3)
            assertEquals(listOf(resource to 3), repository.streamRequests.toList())
        }
    }

    @Test
    fun rowsAreFormattedAndAMissingValueIsADashNotNull() = runTest(dispatcher) {
        val repository = FakeChargeRepository(
            ScreenState.Content(listOf(Charges(id = 5, name = "Fee", amount = 150.0, dueDate = listOf(2026, 3, 14)))),
        )
        val vm = viewModel(repository = repository)

        vm.charges.test {
            val first = awaitItem()
            val row = ((if (first is ScreenState.Loading) awaitItem() else first) as ScreenState.Content).data.single()
            assertEquals(5, row.id)
            assertEquals("—", row.definitionId)
            assertEquals("—", row.outstanding)
            assertTrue(row.dueDate!!.contains("2026"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun anOwnerWithNoChargesIsEmpty() = runTest(dispatcher) {
        val vm = viewModel(repository = FakeChargeRepository(ScreenState.Content(emptyList())))
        vm.charges.test {
            var state = awaitItem()
            if (state is ScreenState.Loading) state = awaitItem()
            assertEquals(ScreenState.Empty, state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun editIsOfferedForLoansAndSavingsButNotClients() {
        assertTrue(viewModel(resource = ChargeResource.LOAN).canEdit)
        assertTrue(viewModel(resource = ChargeResource.SAVINGS).canEdit)
        assertFalse(viewModel(resource = ChargeResource.CLIENT).canEdit)
    }

    @Test
    fun aClientEditActionNavigatesNowhere() = runTest(dispatcher) {
        val vm = viewModel(resource = ChargeResource.CLIENT)
        vm.eventFlow.test {
            vm.trySendAction(ChargeListAction.OnEditCharge(5))
            vm.trySendAction(ChargeListAction.NavigateBack)
            testScheduler.advanceUntilIdle()
            assertEquals(ChargeListEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun rowActionsCarryTheOwnerAndTheCharge() = runTest(dispatcher) {
        val vm = viewModel(resource = ChargeResource.SAVINGS, id = 4)
        vm.eventFlow.test {
            vm.trySendAction(ChargeListAction.OnAddCharge)
            vm.trySendAction(ChargeListAction.OnViewCharge(9))
            vm.trySendAction(ChargeListAction.OnEditCharge(9))
            testScheduler.advanceUntilIdle()
            assertEquals(ChargeListEvent.AddCharge("savingsaccounts", 4), awaitItem())
            assertEquals(ChargeListEvent.OpenCharge("savingsaccounts", 4, 9), awaitItem())
            assertEquals(ChargeListEvent.EditCharge("savingsaccounts", 4, 9), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteIsConfirmedFirstThenSentForThatOwner() = runTest(dispatcher) {
        val commands = FakeChargeCommandRepository()
        val vm = viewModel(commands = commands, resource = ChargeResource.CLIENT, id = 1)

        vm.trySendAction(ChargeListAction.OnDeleteCharge(5))
        testScheduler.advanceUntilIdle()
        assertEquals(5, vm.stateFlow.value.pendingDeleteId)
        assertTrue(commands.deleted.isEmpty())

        vm.trySendAction(ChargeListAction.ConfirmDelete)
        testScheduler.advanceUntilIdle()

        assertEquals(FakeChargeCommandRepository.Write(ChargeResource.CLIENT, 1, 5), commands.deleted.single())
        assertNull(vm.stateFlow.value.pendingDeleteId)
        assertNull(vm.stateFlow.value.writeError)
    }

    @Test
    fun dismissingTheConfirmationDeletesNothing() = runTest(dispatcher) {
        val commands = FakeChargeCommandRepository()
        val vm = viewModel(commands = commands)

        vm.trySendAction(ChargeListAction.OnDeleteCharge(5))
        vm.trySendAction(ChargeListAction.DismissDialog)
        vm.trySendAction(ChargeListAction.ConfirmDelete)
        testScheduler.advanceUntilIdle()

        assertTrue(commands.deleted.isEmpty())
    }

    @Test
    fun anOfflineDeleteIsToldApartFromAFailure() = runTest(dispatcher) {
        val offline = viewModel(commands = FakeChargeCommandRepository { MutationResult.Blocked(BlockReason.OFFLINE) })
        offline.trySendAction(ChargeListAction.OnDeleteCharge(5))
        offline.trySendAction(ChargeListAction.ConfirmDelete)
        testScheduler.advanceUntilIdle()
        assertEquals(ChargeWriteError.Offline, offline.stateFlow.value.writeError)

        val failed = viewModel(
            commands = FakeChargeCommandRepository {
                MutationResult.Failed(IllegalStateException("x"), rolledBack = false)
            },
        )
        failed.trySendAction(ChargeListAction.OnDeleteCharge(5))
        failed.trySendAction(ChargeListAction.ConfirmDelete)
        testScheduler.advanceUntilIdle()
        assertEquals(ChargeWriteError.Failed(), failed.stateFlow.value.writeError)
        assertIs<ChargeWriteError>(failed.stateFlow.value.writeError)
    }

    @Test
    fun aRefusedDeleteCarriesFineractsReason() = runTest(dispatcher) {
        // Device 2026-10-23: deleting a charge on an ACTIVE loan is refused by a domain rule.
        val body = "Client request(DELETE x) invalid: 403. Text: \"{\"errors\":[{\"defaultUserMessage\":" +
            "\"This charge cannot be deleted as the loan is not pending approval\"}]}\""
        val vm = viewModel(
            commands = FakeChargeCommandRepository {
                MutationResult.Failed(IllegalStateException(body), rolledBack = false)
            },
        )
        vm.trySendAction(ChargeListAction.OnDeleteCharge(5))
        vm.trySendAction(ChargeListAction.ConfirmDelete)
        testScheduler.advanceUntilIdle()
        assertEquals(
            ChargeWriteError.Failed("This charge cannot be deleted as the loan is not pending approval"),
            vm.stateFlow.value.writeError,
        )
        assertFalse(vm.stateFlow.value.isDeleting)
    }

    @Test
    fun theDeleteIsInFlightUntilTheServerAnswers() = runTest(dispatcher) {
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val vm = viewModel(commands = FakeChargeCommandRepository(gate = gate))
        vm.trySendAction(ChargeListAction.OnDeleteCharge(5))
        vm.trySendAction(ChargeListAction.ConfirmDelete)
        testScheduler.advanceUntilIdle()
        // The overlay shows while waiting, and a second confirm cannot fire a second delete.
        assertTrue(vm.stateFlow.value.isDeleting)

        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertFalse(vm.stateFlow.value.isDeleting)
    }

    @Test
    fun theRowToggleOpensOneRowAtATime() = runTest(dispatcher) {
        val vm = viewModel()
        vm.trySendAction(ChargeListAction.ToggleExpanded(1))
        vm.trySendAction(ChargeListAction.ToggleExpanded(2))
        testScheduler.advanceUntilIdle()
        assertEquals(2, vm.stateFlow.value.expandedId)

        vm.trySendAction(ChargeListAction.ToggleExpanded(2))
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.expandedId)
    }
}
