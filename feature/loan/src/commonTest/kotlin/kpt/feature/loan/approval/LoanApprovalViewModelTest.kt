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

package kpt.feature.loan.approval

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
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanTimeline
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoanApprovalViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val loanState = MutableStateFlow<ScreenState<LoanWithAssociations>>(ScreenState.Loading)

    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        repository: FakeLoanRepository = FakeLoanRepository(loan = loanState),
        loanId: Int = 7,
    ) = LoanApprovalViewModel(repository, commands, SavedStateHandle(mapOf("loanId" to loanId)))

    @Test
    fun theRouteArgumentReachesBothTheReadAndTheWrite() = runTest(dispatcher) {
        val repository = FakeLoanRepository(loan = loanState)
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands, repository, loanId = 42)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf<Any>(42), repository.requestedKeys["loan"]?.toList())

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("5000"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(42, commands.approvals.single().first)
    }

    // ---- the payload ----

    @Test
    fun theApprovedDateIsFormattedToMatchItsOwnDeclaredDateFormat() = runTest(dispatcher) {
        // THE REGRESSION, and the third time this exact bug has appeared in this port (after
        // clientClosure and clientTransfer). The original sent
        // `DateHelper.getDateAsStringFromLong(...)`, which emits "14-03-2026", while
        // `LoanApproval.dateFormat` declares "dd MMMM yyyy" — so Fineract was handed a string it
        // was told to parse one way and could not.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("5000"))
        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedOn(APPROVED_MILLIS))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val approval = commands.approvals.single().second
        assertEquals(ApiDateFormatter.DATE_FORMAT, approval.dateFormat)
        assertEquals(ApiDateFormatter.formatForApi(APPROVED_MILLIS), approval.approvedOnDate)
        // Spelled out, so a future "simplification" back to the display helper fails here.
        assertEquals("14 March 2026", approval.approvedOnDate)
        assertEquals(ApiDateFormatter.LOCALE, approval.locale)
    }

    @Test
    fun theDisbursementDateIsFormattedForTheApiNotForDisplay() = runTest(dispatcher) {
        // The original sent the DISPLAY string it had rendered in the text field — a different
        // format again, derived from the loan's `[y, m, d]` triple.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("5000"))
        vm.actionChannel.trySend(LoanApprovalAction.UpdateDisbursementOn(APPROVED_MILLIS))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals("14 March 2026", commands.approvals.single().second.expectedDisbursementDate)
    }

    @Test
    fun noDisbursementDateSendsNoFieldRatherThanTheWordNull() = runTest(dispatcher) {
        // The original's field rendered `disbursementDate ?: "null"` and sent whatever it held.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("5000"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertNull(commands.approvals.single().second.expectedDisbursementDate)
    }

    @Test
    fun aBlankNoteIsOmittedRatherThanSentAsAnEmptyString() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("5000"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertNull(commands.approvals.single().second.note)
    }

    @Test
    fun theNoteAndAmountTheOfficerTypedAreWhatIsSent() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("7500.50"))
        vm.actionChannel.trySend(LoanApprovalAction.UpdateNote("Approved after site visit"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val approval = commands.approvals.single().second
        assertEquals("7500.50", approval.approvedLoanAmount)
        assertEquals("Approved after site visit", approval.note)
    }

    // ---- validation ----

    @Test
    fun anInvalidAmountSurfacesAnErrorInsteadOfDoingNothing() = runTest(dispatcher) {
        // The original's submit branch was `if (isFieldValid(...)) { … }` with the else commented
        // out, so a blank or non-numeric amount made the button look broken.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("not a number"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.stateFlow.value.amountError)
        assertTrue(commands.approvals.isEmpty(), "nothing is sent for an unparseable amount")
    }

    @Test
    fun theErrorClearsOnceTheAmountIsValid() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.amountError)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("100"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(!vm.stateFlow.value.amountError)
        assertEquals(1, commands.approvals.size)
    }

    @Test
    fun aSecondSubmitWhileOneIsInFlightIsIgnored() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeLoanCommandRepository(gate = gate)
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("5000"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.approvals.size, "a double tap must not approve twice")

        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }

    // ---- the prefill ----

    @Test
    fun thePrefillCarriesTheLoansOwnFiguresAndSurvivesAMissingTimeline() = runTest(dispatcher) {
        // `loanWithAssociations?.timeline!!` in the original — a `?.` followed by a `!!` on the
        // same chain, which crashed the screen on a loan whose timeline Fineract omitted.
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            LoanWithAssociations(id = 7, approvedPrincipal = 5_000.0, timeline = null),
        )

        vm.prefill.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val prefill = assertIs<ScreenState.Content<LoanApprovalPrefill>>(awaitItem()).data
            assertEquals(5_000.0, prefill.approvedAmount)
            assertNull(prefill.expectedDisbursementOn)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aTruncatedDisbursementDateIsDroppedRatherThanIndexedInto() = runTest(dispatcher) {
        val vm = viewModel()
        loanState.value = ScreenState.Content(
            LoanWithAssociations(
                id = 7,
                timeline = LoanTimeline(expectedDisbursementDate = listOf(2026)),
            ),
        )

        vm.prefill.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertNull(
                assertIs<ScreenState.Content<LoanApprovalPrefill>>(awaitItem()).data.expectedDisbursementOn,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFineractDateTripleConvertsToTheStartOfThatDay() {
        val millis = assertNotNull(listOf(2026, 3, 14).toStartOfDayMillis())
        // Round-trips through the API formatter, which is what the submit path will do with it.
        assertEquals("14 March 2026", ApiDateFormatter.formatForApi(millis))
        // A month Fineract could never send — the conversion returns null rather than throwing.
        assertNull(listOf(2026, 13, 1).toStartOfDayMillis())
    }

    @Test
    fun theFormIsSeededOnceAndASecondPrefillCannotOverwriteTyping() = runTest(dispatcher) {
        // The screen fires `Prefilled` only while `prefilled` is false; this pins the state side
        // of that, so a refresh landing mid-edit cannot replace what the officer typed.
        val vm = viewModel()

        vm.actionChannel.trySend(LoanApprovalAction.Prefilled("5000", disbursementOn = null))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("6000"))
        testScheduler.advanceUntilIdle()

        assertTrue(vm.stateFlow.value.prefilled)
        assertEquals("6000", vm.stateFlow.value.approvedAmount)
    }

    // ---- the outcome ----

    @Test
    fun successShowsTheDialogWithoutReReadingTheLoan() = runTest(dispatcher) {
        // The command ingests the approved loan through the `loans` store, so the profile behind
        // this screen re-emits on its own — the ViewModel must not re-read after the write.
        val repository = FakeLoanRepository(loan = loanState)
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands, repository)

        repository.refreshes.test {
            vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("5000"))
            vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
            testScheduler.advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        val dialog = assertIs<LoanApprovalState.DialogState.ShowStatusDialog>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals(ResultStatus.SUCCESS, dialog.status)
    }

    @Test
    fun offlineSaysTheApprovalWasNotSent() = runTest(dispatcher) {
        // An approval is OnlineRequired — nothing is written optimistically, because telling an
        // officer a loan is approved when the server never saw it is worse than telling them to
        // find signal.
        val commands = FakeLoanCommandRepository(
            result = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("5000"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanApprovalState.DialogState.ShowStatusDialog>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals(ResultStatus.FAILURE, dialog.status)
        assertTrue(dialog.msg.contains("No connection"), "message was ${dialog.msg}")
    }

    @Test
    fun aRejectionSurfacesFineractsOwnReason() = runTest(dispatcher) {
        // "approved amount exceeds proposed principal" is the common one and tells the officer
        // exactly what to change — a generic failure message would not.
        val commands = FakeLoanCommandRepository(
            result = {
                MutationResult.Failed(
                    cause = fineractRejection("approved amount exceeds the proposed principal"),
                    // An OnlineRequired write never applied anything locally, so there is nothing
                    // to roll back — the gateway reports the same.
                    rolledBack = false,
                )
            },
        )
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("999999"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanApprovalState.DialogState.ShowStatusDialog>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals("approved amount exceeds the proposed principal", dialog.msg)
        // Submitting is finished, so the officer can correct the amount and try again.
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }

    @Test
    fun theDialogCanBeDismissedWithoutLeaving() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            result = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanApprovalAction.UpdateApprovedAmount("5000"))
        vm.actionChannel.trySend(LoanApprovalAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanApprovalAction.CloseDialog)
        testScheduler.advanceUntilIdle()

        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun theDatePickerTracksWhichFieldOpenedIt() = runTest(dispatcher) {
        // The original kept two booleans and two picker states, and chose between them with
        // `if (pickApproveDate)` — so both true at once was representable and meant the first.
        val vm = viewModel()

        vm.actionChannel.trySend(
            LoanApprovalAction.OpenDatePicker(LoanApprovalState.DateField.DISBURSEMENT_ON),
        )
        testScheduler.advanceUntilIdle()
        assertEquals(LoanApprovalState.DateField.DISBURSEMENT_ON, vm.stateFlow.value.datePicker)

        vm.actionChannel.trySend(LoanApprovalAction.CloseDatePicker)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.datePicker)
    }

    private companion object {
        /**
         * 2026-03-14 **12:00 UTC**, not midnight.
         *
         * The formatter converts to the DEVICE's zone, so a midnight-UTC instant reads as the 13th
         * anywhere west of Greenwich and as the 14th elsewhere — a test that passes or fails by
         * where it runs. Noon leaves the date the same across every real offset (UTC-12..UTC+14).
         */
        const val APPROVED_MILLIS = 1_773_489_600_000L
    }
}
