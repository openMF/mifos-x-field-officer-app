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
    kotlin.time.ExperimentalTime::class,
)

package kpt.feature.client.closure

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
import kpt.core.model.client.ClientCloseTemplateResponse
import kpt.core.model.client.Narration
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ClientClosureViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val closeTemplate =
        MutableStateFlow<ScreenState<ClientCloseTemplateResponse>>(ScreenState.Loading)

    private val repository = FakeClientRepository(closeTemplate = closeTemplate)

    private fun viewModel(
        commands: FakeClientCommandRepository = FakeClientCommandRepository(),
        clientId: Int = 42,
    ) = ClientClosureViewModel(repository, commands, SavedStateHandle(mapOf("clientId" to clientId)))

    private fun narration(id: Int, name: String) =
        Narration(id = id, name = name, position = id, description = name, active = true, mandatory = false)

    private fun template(vararg names: String) = ClientCloseTemplateResponse(
        isStaff = false,
        narrations = names.mapIndexed { i, n -> narration(i + 1, n) },
    )

    @Test
    fun theClosureReasonsAreProjectedFromTheTemplateStream() = runTest(dispatcher) {
        val vm = viewModel()
        closeTemplate.value = ScreenState.Content(template("Withdrawn", "Transferred"))

        vm.reasons.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<List<Narration>>>(awaitItem())
            assertEquals(listOf("Withdrawn", "Transferred"), content.data.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailedReadStaysAReadStateRatherThanBecomingADialog() = runTest(dispatcher) {
        val vm = viewModel()
        closeTemplate.value = ScreenState.Error(RuntimeException("boom"))
        testScheduler.advanceUntilIdle()

        vm.reasons.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Error>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(null, vm.stateFlow.value.dialogState)
    }

    @Test
    fun theClosureDateIsSentInTheFormatTheRequestDeclares() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository()
        val vm = viewModel(commands)
        closeTemplate.value = ScreenState.Content(template("Withdrawn"))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientClosureAction.UpdateDate(1_760_000_000_000L))
        vm.actionChannel.trySend(ClientClosureAction.OnSubmit(closureReasonId = 1))
        testScheduler.advanceUntilIdle()

        val (clientId, request) = commands.closedClients.single()
        assertEquals(42, clientId)
        assertEquals(1, request.closureReasonId)
        // THE REGRESSION. The original sent DateHelper's `dd-MM-yyyy` while declaring
        // `dd MMMM yyyy`, so Fineract was asked to parse a string it could not parse. The date and
        // the declared format must come from the same formatter.
        assertEquals(ApiDateFormatter.DATE_FORMAT, request.dateFormat)
        assertEquals(ApiDateFormatter.formatForApi(1_760_000_000_000L), request.closureDate)
        assertTrue(
            request.closureDate.none { it == '-' },
            "closureDate must not be the dd-MM-yyyy display format: ${request.closureDate}",
        )
    }

    @Test
    fun aSecondSubmitInsideTheInFlightWindowIsDropped() = runTest(dispatcher) {
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val commands = FakeClientCommandRepository(gate = gate)
        val vm = viewModel(commands)
        closeTemplate.value = ScreenState.Content(template("Withdrawn"))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientClosureAction.OnSubmit(1))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(ClientClosureAction.OnSubmit(1))
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        // Closing a client twice is not idempotent on Fineract — the second attempt 400s on an
        // already-closed client and the officer sees a failure for a closure that worked.
        assertEquals(1, commands.closedClients.size)
    }

    @Test
    fun anOfflineClosureSaysSo() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository(
            unitResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        closeTemplate.value = ScreenState.Content(template("Withdrawn"))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientClosureAction.OnSubmit(1))
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<ClientClosureState.DialogState.ShowStatusDialog>(vm.stateFlow.value.dialogState)
        assertEquals(ResultStatus.FAILURE, dialog.status)
        assertTrue(dialog.msg.contains("No connection"), dialog.msg)
    }

    @Test
    fun aRejectionKeepsFineractsReason() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository(
            unitResult = {
                MutationResult.Failed(fineractRejection("client has active loans"), rolledBack = false)
            },
        )
        val vm = viewModel(commands)
        closeTemplate.value = ScreenState.Content(template("Withdrawn"))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientClosureAction.OnSubmit(1))
        testScheduler.advanceUntilIdle()

        // "client has active loans" tells the officer exactly what to do next; a generic string
        // does not.
        val dialog = assertIs<ClientClosureState.DialogState.ShowStatusDialog>(vm.stateFlow.value.dialogState)
        assertEquals("client has active loans", dialog.msg)
    }

    @Test
    fun theDatePickerTogglesWithoutTouchingTheRead() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(ClientClosureAction.UpdateDatePicker(true))
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.showDatePicker)

        vm.actionChannel.trySend(ClientClosureAction.UpdateDatePicker(false))
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.showDatePicker)
    }
}
