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

package kpt.feature.client.transfer

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.office.OfficeRepository
import kpt.core.data.testing.FakeClientCommandRepository
import kpt.core.model.office.Office
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ClientTransferViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val officesState = MutableStateFlow<ScreenState<List<Office>>>(ScreenState.Loading)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    /**
     * `OfficeRepository` has ONE method, so it is faked inline rather than given a shared fake in
     * `core/data` — there is nothing for the interface to grow into that would break this.
     */
    private val offices = object : OfficeRepository {
        override fun officesStream(scope: CoroutineScope): ScreenDataStream<List<Office>> =
            screenDataStreamForTesting(officesState, refreshTrigger = refreshes)
    }

    private fun viewModel(
        commands: FakeClientCommandRepository = FakeClientCommandRepository(),
        clientId: Int = 42,
    ) = ClientTransferViewModel(offices, commands, SavedStateHandle(mapOf("clientId" to clientId)))

    private fun office(id: Int, name: String) = Office(id = id, name = name)

    @Test
    fun theOfficeListComesFromTheOfficeRepositoryNotTheGroupFeatures() = runTest(dispatcher) {
        val vm = viewModel()
        officesState.value = ScreenState.Content(listOf(office(1, "Head Office"), office(2, "Branch A")))

        vm.offices.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<List<Office>>>(awaitItem())
            // The original borrowed `CreateNewGroupRepository.offices()` for this — the GROUP
            // feature's repository, for office data.
            assertEquals(listOf("Head Office", "Branch A"), content.data.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theTransferDateIsSentInTheFormatTheRequestDeclares() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository()
        val vm = viewModel(commands)
        officesState.value = ScreenState.Content(listOf(office(1, "Head Office")))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientTransferAction.NoteChanged("Relocating"))
        vm.actionChannel.trySend(ClientTransferAction.UpdateDate(1_760_000_000_000L))
        vm.actionChannel.trySend(ClientTransferAction.OnSubmit(destinationOfficeId = 1))
        testScheduler.advanceUntilIdle()

        val (clientId, request) = commands.proposedTransfers.single()
        assertEquals(42, clientId)
        assertEquals(1, request.destinationOfficeId)
        assertEquals("Relocating", request.note)
        // Same regression as clientClosure: the original sent DateHelper's `dd-MM-yyyy` while
        // declaring `dd MMMM yyyy`.
        assertEquals(ApiDateFormatter.DATE_FORMAT, request.dateFormat)
        assertEquals(ApiDateFormatter.formatForApi(1_760_000_000_000L), request.transferDate)
        assertFalse(
            request.transferDate.contains('-'),
            "transferDate must not be the dd-MM-yyyy display format: ${request.transferDate}",
        )
    }

    @Test
    fun submitIsDisabledUntilANoteIsEntered() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        // The original's gate, kept — Fineract accepts an empty note but the original required one.
        assertFalse(vm.stateFlow.value.isEnabled)

        vm.actionChannel.trySend(ClientTransferAction.NoteChanged("Relocating"))
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.isEnabled)
    }

    @Test
    fun submitIsDisabledWhileOneIsInFlight() = runTest(dispatcher) {
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val commands = FakeClientCommandRepository(gate = gate)
        val vm = viewModel(commands)
        officesState.value = ScreenState.Content(listOf(office(1, "Head Office")))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientTransferAction.NoteChanged("Relocating"))
        vm.actionChannel.trySend(ClientTransferAction.OnSubmit(1))
        testScheduler.advanceUntilIdle()

        // `isEnabled` folds in `isSubmitting`, so the BUTTON is also disabled — belt and braces
        // with the ViewModel's own guard.
        assertFalse(vm.stateFlow.value.isEnabled)

        vm.actionChannel.trySend(ClientTransferAction.OnSubmit(1))
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.proposedTransfers.size)
    }

    @Test
    fun anOfflineTransferSaysSo() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository(
            unitResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        officesState.value = ScreenState.Content(listOf(office(1, "Head Office")))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientTransferAction.NoteChanged("x"))
        vm.actionChannel.trySend(ClientTransferAction.OnSubmit(1))
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<ClientTransferState.DialogState.ShowStatusDialog>(vm.stateFlow.value.dialogState)
        assertEquals(ResultStatus.FAILURE, dialog.status)
        assertTrue(dialog.msg.contains("No connection"), dialog.msg)
    }

    @Test
    fun retryReachesTheOfficeStream() = runTest(dispatcher) {
        val vm = viewModel()
        refreshes.test {
            vm.actionChannel.trySend(ClientTransferAction.OnRetry)
            testScheduler.advanceUntilIdle()
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
