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

package kpt.feature.loan.rescheduleform

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.testing.FakeLoanCommandRepository
import kpt.core.data.testing.FakeLoanRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleTemplate
import kpt.core.model.objects.account.loan.reschedules.RescheduleReasonOption
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The five optional changes are this form's logic: each one contributes to the payload only when
 * checked, and the original contributed an EMPTY STRING when unchecked.
 */
class LoanRescheduleFormViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val reasons = listOf(
        RescheduleReasonOption(id = 9, name = "Zulu reason"),
        RescheduleReasonOption(id = 3, name = "Crop failure"),
    )

    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        rescheduleReasons: () -> LoanRescheduleTemplate? = {
            LoanRescheduleTemplate(rescheduleReasons = reasons)
        },
        loanId: Int = 7,
    ) = LoanRescheduleFormViewModel(
        FakeLoanRepository(rescheduleReasons = rescheduleReasons),
        commands,
        SavedStateHandle(mapOf("loanId" to loanId)),
    )

    private val cropFailure = RescheduleReasonOption(id = 3, name = "Crop failure")

    private fun LoanRescheduleFormViewModel.fillRequired() {
        actionChannel.trySend(LoanRescheduleFormAction.OnReasonSelected(cropFailure))
        actionChannel.trySend(
            LoanRescheduleFormAction.OnDateChanged(RescheduleDate.FROM, FROM_MILLIS),
        )
        actionChannel.trySend(
            LoanRescheduleFormAction.OnDateChanged(RescheduleDate.SUBMITTED_ON, FROM_MILLIS),
        )
    }

    // ---- the reason catalog ----

    @Test
    fun theReasonsAreSortedByName() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Crop failure", "Zulu reason"), vm.stateFlow.value.reasons.map { it.name })
    }

    @Test
    fun aFailedReasonReadIsReportedAndRetryable() = runTest(dispatcher) {
        // Without the reasons there is no proposal to make — the reason is required.
        val vm = viewModel(rescheduleReasons = { null })
        testScheduler.advanceUntilIdle()

        assertTrue(vm.stateFlow.value.reasonsError)
        assertTrue(!vm.stateFlow.value.isLoadingReasons)
    }

    @Test
    fun theCatalogIsAskedForOnceAndAgainOnRetry() = runTest(dispatcher) {
        val repository = FakeLoanRepository(
            rescheduleReasons = { LoanRescheduleTemplate(rescheduleReasons = reasons) },
        )
        val vm = LoanRescheduleFormViewModel(
            repository,
            FakeLoanCommandRepository(),
            SavedStateHandle(mapOf("loanId" to 7)),
        )
        testScheduler.advanceUntilIdle()
        assertEquals(1, repository.rescheduleReasonQueries)

        vm.actionChannel.trySend(LoanRescheduleFormAction.OnRetryFetching)
        testScheduler.advanceUntilIdle()
        assertEquals(2, repository.rescheduleReasonQueries)
    }

    // ---- the payload's REQUIRED half ----

    @Test
    fun theRequestCarriesTheLoanTheReasonAndBothDates() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val request = commands.proposedReschedules.single()
        assertEquals(7, request.loanId)
        assertEquals(3, request.rescheduleReasonId)
        assertEquals("14 March 2026", request.rescheduleFromDate)
        assertEquals("14 March 2026", request.submittedOnDate)
        assertEquals(ApiDateFormatter.DATE_FORMAT, request.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, request.locale)
    }

    @Test
    fun aBlankCommentIsOmitted() = runTest(dispatcher) {
        // The original sent `rescheduleReasonComment = state.comments` — the empty string.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertNull(commands.proposedReschedules.single().rescheduleReasonComment)
    }

    @Test
    fun aCommentIsTrimmedAndSent() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnCommentsChanged("  drought  "))
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals("drought", commands.proposedReschedules.single().rescheduleReasonComment)
    }

    // ---- THE defect: unchecked options sent as empty strings ----

    @Test
    fun anUNCHECKEDOptionIsOMITTEDNotSentAsAnEmptyString() = runTest(dispatcher) {
        // THE REGRESSION. The original sent `""` for every one of the five when unchecked:
        //   adjustedDueDate = if (changeRepaymentDateSelected) adjustedDueDate else "",
        // `null` means "not part of this proposal"; `""` means "here is a date, and it is the
        // empty string" — which Fineract then tries to parse against the declared dateFormat.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val request = commands.proposedReschedules.single()
        assertNull(request.adjustedDueDate)
        assertNull(request.graceOnPrincipal)
        assertNull(request.graceOnInterest)
        assertNull(request.extraTerms)
        assertNull(request.newInterestRate)
        // `true` or omitted, never `false` — Fineract reads the field's presence as the
        // instruction. The original's rule, kept.
        assertNull(request.waivePenalties)
    }

    @Test
    fun aCHECKEDOptionContributesItsValue() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()

        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.CHANGE_REPAYMENT_DATE, true),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnDateChanged(RescheduleDate.ADJUSTED_DUE, FROM_MILLIS),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.INTRODUCE_GRACE_PERIODS, true),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnNumberChanged(RescheduleField.GRACE_ON_PRINCIPAL, "2"),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.EXTEND_REPAYMENT_PERIOD, true),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnNumberChanged(RescheduleField.EXTRA_TERMS, "3"),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.ADJUST_INTEREST_RATE, true),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnNumberChanged(RescheduleField.NEW_INTEREST_RATE, "11.5"),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.WAIVE_PENALTIES, true),
        )
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val request = commands.proposedReschedules.single()
        assertEquals("14 March 2026", request.adjustedDueDate)
        assertEquals("2", request.graceOnPrincipal)
        // Only ONE of the two grace fields was filled, which the form allows.
        assertNull(request.graceOnInterest)
        assertEquals("3", request.extraTerms)
        assertEquals("11.5", request.newInterestRate)
        assertEquals(true, request.waivePenalties)
    }

    @Test
    fun uncheckingAnOptionCLEARSItsFields() = runTest(dispatcher) {
        // A value left behind by an unchecked option is one the officer cannot see and did not
        // mean. The original's behaviour, kept.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()

        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.EXTEND_REPAYMENT_PERIOD, true),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnNumberChanged(RescheduleField.EXTRA_TERMS, "3"),
        )
        testScheduler.advanceUntilIdle()
        assertEquals("3", vm.stateFlow.value.number(RescheduleField.EXTRA_TERMS))

        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.EXTEND_REPAYMENT_PERIOD, false),
        )
        testScheduler.advanceUntilIdle()
        assertEquals("", vm.stateFlow.value.number(RescheduleField.EXTRA_TERMS))

        // And re-checking does not resurrect it.
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.EXTEND_REPAYMENT_PERIOD, true),
        )
        testScheduler.advanceUntilIdle()
        assertEquals("", vm.stateFlow.value.number(RescheduleField.EXTRA_TERMS))
    }

    // ---- validation ----

    @Test
    fun theReasonIsRequired() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(RescheduleField.REASON in vm.stateFlow.value.errors)
        assertTrue(commands.proposedReschedules.isEmpty())
    }

    @Test
    fun aCheckedDateOptionWithNoDatePickedIsRefused() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.CHANGE_REPAYMENT_DATE, true),
        )
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(RescheduleField.ADJUSTED_DUE_DATE in vm.stateFlow.value.errors)
        assertTrue(commands.proposedReschedules.isEmpty())
    }

    @Test
    fun aNonNumericValueInANumberFieldIsRefused() = runTest(dispatcher) {
        // THE OTHER regression: the original only checked these four for non-BLANKNESS, so "two"
        // was sent to the server as-is.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.EXTEND_REPAYMENT_PERIOD, true),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnNumberChanged(RescheduleField.EXTRA_TERMS, "two"),
        )
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(RescheduleField.EXTRA_TERMS in vm.stateFlow.value.errors)
        assertTrue(commands.proposedReschedules.isEmpty())
    }

    @Test
    fun aNegativeNumberIsRefused() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.ADJUST_INTEREST_RATE, true),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnNumberChanged(RescheduleField.NEW_INTEREST_RATE, "-3"),
        )
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(RescheduleField.NEW_INTEREST_RATE in vm.stateFlow.value.errors)
    }

    @Test
    fun gracePeriodsNeedAtLeastONEOfTheTwoFields() = runTest(dispatcher) {
        // The original's rule, kept: a grace period on principal alone is a legitimate proposal.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.INTRODUCE_GRACE_PERIODS, true),
        )
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertTrue(RescheduleField.GRACE_ON_PRINCIPAL in vm.stateFlow.value.errors)

        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnNumberChanged(RescheduleField.GRACE_ON_INTEREST, "1"),
        )
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.stateFlow.value.errors.isEmpty())
        assertEquals("1", commands.proposedReschedules.single().graceOnInterest)
    }

    @Test
    fun everyErrorIsReportedAtOnce() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.CHANGE_REPAYMENT_DATE, true),
        )
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.EXTEND_REPAYMENT_PERIOD, true),
        )
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        // The original disabled the submit BUTTON whenever anything was missing, so an officer saw
        // a dead control and no indication of which of six fields it was waiting for.
        assertEquals(
            setOf(
                RescheduleField.REASON,
                RescheduleField.ADJUSTED_DUE_DATE,
                RescheduleField.EXTRA_TERMS,
            ),
            vm.stateFlow.value.errors,
        )
    }

    @Test
    fun editingAFieldClearsOnlyItsOwnError() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnToggle(RescheduleOption.EXTEND_REPAYMENT_PERIOD, true),
        )
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertTrue(RescheduleField.REASON in vm.stateFlow.value.errors)
        assertTrue(RescheduleField.EXTRA_TERMS in vm.stateFlow.value.errors)

        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OnNumberChanged(RescheduleField.EXTRA_TERMS, "3"),
        )
        testScheduler.advanceUntilIdle()

        assertTrue(RescheduleField.EXTRA_TERMS !in vm.stateFlow.value.errors)
        assertTrue(RescheduleField.REASON in vm.stateFlow.value.errors)
    }

    @Test
    fun aSecondSubmitWhileOneIsInFlightIsIgnored() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeLoanCommandRepository(gate = gate)
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.proposedReschedules.size)
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }

    // ---- the outcome ----

    @Test
    fun retryReSendsTheProvenDraft() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            rescheduleResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanRescheduleFormAction.OnRetrySubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(2, commands.proposedReschedules.size)
        assertEquals(
            commands.proposedReschedules[0].rescheduleFromDate,
            commands.proposedReschedules[1].rescheduleFromDate,
        )
    }

    @Test
    fun retryBeforeAnySubmitDoesNothing() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanRescheduleFormAction.OnRetrySubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(commands.proposedReschedules.isEmpty())
    }

    @Test
    fun successShowsTheDialog() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertIs<LoanRescheduleFormState.DialogState.Submitted>(vm.stateFlow.value.dialogState)
    }

    @Test
    fun aRejectionSurfacesFineractsReasonAndKeepsTheForm() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            rescheduleResult = {
                MutationResult.Failed(
                    fineractRejection("reschedule date should be after the last repayment"),
                    rolledBack = false,
                )
            },
        )
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnCommentsChanged("drought"))
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanRescheduleFormState.DialogState.Failed>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals("reschedule date should be after the last repayment", dialog.msg)
        assertEquals("drought", vm.stateFlow.value.comments)
        assertEquals(3, vm.stateFlow.value.reason?.id)
    }

    @Test
    fun offlineSaysTheProposalWasNotSent() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            rescheduleResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillRequired()
        vm.actionChannel.trySend(LoanRescheduleFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanRescheduleFormState.DialogState.Failed>(
            vm.stateFlow.value.dialogState,
        )
        assertTrue(dialog.msg.contains("No connection"), "message was ${dialog.msg}")
    }

    @Test
    fun theDatePickerTracksWhichFieldOpenedIt() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(
            LoanRescheduleFormAction.OpenDatePicker(RescheduleDate.SUBMITTED_ON),
        )
        testScheduler.advanceUntilIdle()
        assertEquals(RescheduleDate.SUBMITTED_ON, vm.stateFlow.value.activeDatePicker)

        vm.actionChannel.trySend(LoanRescheduleFormAction.CloseDatePicker)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.activeDatePicker)
    }

    @Test
    fun backNavigationIsAnEvent() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(LoanRescheduleFormAction.NavigateBack)
            assertEquals(LoanRescheduleFormEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        /** 2026-03-14 12:00 UTC — noon, so the device zone cannot shift the DATE. */
        const val FROM_MILLIS = 1_773_489_600_000L
    }
}
