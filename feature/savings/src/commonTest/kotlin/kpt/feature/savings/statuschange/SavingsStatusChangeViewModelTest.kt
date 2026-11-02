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

package kpt.feature.savings.statuschange

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.testing.FakeSavingsCommandRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The date is this screen's entire payload, and the original sent **epoch milliseconds** for it on
 * both the approval and the activation — while declaring `dateFormat = "dd MMMM yyyy"`.
 */
class SavingsStatusChangeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        commands: FakeSavingsCommandRepository = FakeSavingsCommandRepository(),
        change: SavingsStatusChange = SavingsStatusChange.APPROVE,
    ) = SavingsStatusChangeViewModel(
        commands = commands,
        savedStateHandle = SavedStateHandle(
            mapOf("accountId" to 55, "change" to change.name),
        ),
    )

    @Test
    fun `the approval date is FORMATTED, not epoch millis`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)

        vm.trySendAction(SavingsStatusChangeAction.OnDateChanged(1_761_000_000_000))
        vm.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()

        val (accountId, input) = commands.approvals.single()
        assertEquals(55, accountId)
        // The original sent `approvalDate.toString()` — the literal string "1761000000000".
        assertEquals(ApiDateFormatter.formatForApi(1_761_000_000_000), input.approvedOnDate)
        assertTrue(
            input.approvedOnDate.none { it.isDigit() && input.approvedOnDate.length > 20 },
            "the date must not be a millis string",
        )
        // And paired with the format that parses it.
        assertEquals(ApiDateFormatter.DATE_FORMAT, input.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, input.locale)
    }

    @Test
    fun `the activation date is FORMATTED, not epoch millis`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands, change = SavingsStatusChange.ACTIVATE)

        vm.trySendAction(SavingsStatusChangeAction.OnDateChanged(1_761_000_000_000))
        vm.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()

        val (accountId, input) = commands.activations.single()
        assertEquals(55, accountId)
        // The original built `hashMap["activatedOnDate"] = approvalDate.toString()`.
        assertEquals(ApiDateFormatter.formatForApi(1_761_000_000_000), input.activatedOnDate)
        assertEquals(ApiDateFormatter.DATE_FORMAT, input.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, input.locale)
        // And nothing was sent to the approval command.
        assertTrue(commands.approvals.isEmpty())
    }

    @Test
    fun `approval carries the note and activation has none to carry`() = runTest(dispatcher) {
        val approveCommands = FakeSavingsCommandRepository()
        val approve = viewModel(approveCommands)
        approve.trySendAction(SavingsStatusChangeAction.OnNoteChanged("Verified passbook"))
        approve.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()
        assertEquals("Verified passbook", approveCommands.approvals.single().second.note)

        // `SavingsActivationInput` has no note member at all — Fineract's activate command takes
        // none, and the original's activate screen correctly had no field.
        assertTrue(SavingsStatusChange.ACTIVATE.hasNote.not())
    }

    @Test
    fun `a blank note is omitted rather than sent as an empty string`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)

        vm.trySendAction(SavingsStatusChangeAction.OnNoteChanged("   "))
        vm.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()

        assertNull(commands.approvals.single().second.note)
    }

    @Test
    fun `a double submit sends one request`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository(gate = CompletableDeferred())
        val vm = viewModel(commands)

        vm.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()
        vm.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()

        assertEquals(1, commands.approvals.size)
    }

    @Test
    fun `offline is reported as offline, not as a server refusal`() = runTest(dispatcher) {
        val vm = viewModel(
            FakeSavingsCommandRepository(
                approvalResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
            ),
        )
        vm.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()

        val dialog = assertIs<DialogState.Failed>(vm.stateFlow.value.dialogState)
        assertTrue(dialog.offline)
        assertNull(dialog.msg)
    }

    @Test
    fun `a server refusal carries its own message`() = runTest(dispatcher) {
        val vm = viewModel(
            FakeSavingsCommandRepository(
                approvalResult = {
                    MutationResult.Failed(
                        fineractRejection("approval date cannot be before submission"),
                        rolledBack = false,
                    )
                },
            ),
        )
        vm.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()

        val dialog = assertIs<DialogState.Failed>(vm.stateFlow.value.dialogState)
        assertTrue(!dialog.offline)
        assertEquals("approval date cannot be before submission", dialog.msg)
    }

    @Test
    fun `dismissing a failure keeps the date the officer picked`() = runTest(dispatcher) {
        val vm = viewModel(
            FakeSavingsCommandRepository(
                approvalResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
            ),
        )
        vm.trySendAction(SavingsStatusChangeAction.OnDateChanged(1_761_000_000_000))
        vm.trySendAction(SavingsStatusChangeAction.OnNoteChanged("Verified"))
        vm.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()
        vm.trySendAction(SavingsStatusChangeAction.CloseDialog)
        advanceUntilIdle()

        // The original replaced the form with an error page.
        assertNull(vm.stateFlow.value.dialogState)
        assertEquals(1_761_000_000_000, vm.stateFlow.value.date)
        assertEquals("Verified", vm.stateFlow.value.note)
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }

    @Test
    fun `success does not leave by itself`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.trySendAction(SavingsStatusChangeAction.OnSubmit)
        advanceUntilIdle()

        // The original called `onBackPressed()` from a `LaunchedEffect` in the composition body,
        // so the screen vanished and the officer might not catch the message. The outcome is a
        // dialog; leaving is the officer's tap.
        assertEquals(DialogState.Succeeded, vm.stateFlow.value.dialogState)
    }

    @Test
    fun `an unknown change argument falls back to approve rather than crashing`() = runTest(dispatcher) {
        val vm = SavingsStatusChangeViewModel(
            commands = FakeSavingsCommandRepository(),
            savedStateHandle = SavedStateHandle(mapOf("accountId" to 55, "change" to "REJECT")),
        )
        // The original decoded a JSON nav-arg blob with an uncaught `Json.decodeFromString` in the
        // ViewModel's constructor, so a bad argument crashed before the screen existed.
        assertEquals(SavingsStatusChange.APPROVE, vm.stateFlow.value.change)
    }
}
