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

package kpt.feature.loan.disbursement

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
import kpt.core.model.objects.account.loan.Currency
import kpt.core.model.objects.account.loan.PaymentType
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseTemplate
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoanDisbursementViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val templateState = MutableStateFlow<ScreenState<LoanDisburseTemplate>>(
        ScreenState.Content(
            LoanDisburseTemplate(
                netDisbursalAmount = 10_000.0,
                date = listOf(2026, 3, 14),
                currency = Currency(code = "INR", decimalPlaces = 2),
                paymentTypeOptions = listOf(
                    PaymentType(id = 1, name = "Cash"),
                    PaymentType(id = 4, name = "Mobile Money"),
                ),
            ),
        ),
    )

    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        repository: FakeLoanRepository = FakeLoanRepository(disburseTemplate = templateState),
        loanId: Int = 7,
    ) = LoanDisbursementViewModel(
        repository,
        commands,
        SavedStateHandle(mapOf("loanId" to loanId)),
    )

    private fun LoanDisbursementViewModel.fillValid(amount: String = "10000") {
        actionChannel.trySend(LoanDisbursementAction.UpdateAmount(amount))
        actionChannel.trySend(
            LoanDisbursementAction.SelectPaymentType(DisbursePaymentType(1, "Cash")),
        )
    }

    // ---- the template ----

    @Test
    fun theRouteArgumentReachesBothTheReadAndTheWrite() = runTest(dispatcher) {
        val repository = FakeLoanRepository(disburseTemplate = templateState)
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands, repository, loanId = 42)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf<Any>(42), repository.requestedKeys["disburseTemplate"]?.toList())

        vm.fillValid()
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertEquals(42, commands.disbursements.single().first)
    }

    @Test
    fun theTemplateCarriesFineractsExpectedAmountAndTheTenantsPaymentTypes() = runTest(dispatcher) {
        val vm = viewModel()

        vm.template.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val template = assertIs<ScreenState.Content<LoanDisbursementTemplate>>(awaitItem()).data
            assertEquals(10_000.0, template.netDisbursalAmount)
            assertEquals(listOf(1, 4), template.paymentTypes.map { it.id })
            assertEquals("INR", template.currencyCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aPaymentTypeWithNoIdIsNotOFFERED() = runTest(dispatcher) {
        // It cannot be sent, so it must not be selectable. The original mapped options to NAMES
        // and resolved the id back by INDEX, so an idless entry silently shifted every later
        // selection onto the wrong payment type.
        templateState.value = ScreenState.Content(
            LoanDisburseTemplate(
                netDisbursalAmount = 500.0,
                date = listOf(2026, 3, 14),
                currency = Currency(code = "INR", decimalPlaces = 2),
                paymentTypeOptions = listOf(
                    PaymentType(id = null, name = "Broken"),
                    PaymentType(id = 4, name = "Mobile Money"),
                ),
            ),
        )
        val vm = viewModel()

        vm.template.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val types = assertIs<ScreenState.Content<LoanDisbursementTemplate>>(awaitItem())
                .data.paymentTypes
            assertEquals(listOf(4), types.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- the payload ----

    @Test
    fun theDisbursementDateIsFormattedToMatchItsOwnDeclaredFormat() = runTest(dispatcher) {
        // THE REGRESSION — the FOURTH occurrence of this bug in this port. The original sent
        // `DateHelper.getDateAsStringFromLong(...)` ("14-03-2026") on a payload whose `dateFormat`
        // defaulted to "dd MMMM yyyy".
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        vm.fillValid()
        vm.actionChannel.trySend(LoanDisbursementAction.UpdateDate(DISBURSE_MILLIS))
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val request = commands.disbursements.single().second
        assertEquals(ApiDateFormatter.DATE_FORMAT, request.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, request.locale)
        // Spelled out, so a revert to the display helper fails here.
        assertEquals("14 March 2026", request.actualDisbursementDate)
    }

    @Test
    fun theRequestCarriesTheAmountThePaymentTypeAndTheNote() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        vm.actionChannel.trySend(LoanDisbursementAction.UpdateAmount("9500.50"))
        vm.actionChannel.trySend(
            LoanDisbursementAction.SelectPaymentType(DisbursePaymentType(4, "Mobile Money")),
        )
        vm.actionChannel.trySend(LoanDisbursementAction.UpdateNote("Handed over at the centre"))
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val request = commands.disbursements.single().second
        assertEquals(9_500.50, request.transactionAmount)
        assertEquals(4, request.paymentTypeId)
        assertEquals("Handed over at the centre", request.note)
    }

    @Test
    fun noPaymentTypeChosenSendsNULLNotZero() = runTest(dispatcher) {
        // The original initialised `paymentTypeId` to 0 and sent it. Payment type 0 does not
        // exist — Fineract rejects it, or accepts it against whatever row holds that id.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        vm.actionChannel.trySend(LoanDisbursementAction.UpdateAmount("10000"))
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertNull(commands.disbursements.single().second.paymentTypeId)
    }

    @Test
    fun aBlankNoteIsOmitted() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        vm.fillValid()
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertNull(commands.disbursements.single().second.note)
    }

    // ---- validation ----

    @Test
    fun anInvalidAmountSurfacesAnErrorInsteadOfDoingNothing() = runTest(dispatcher) {
        // The original's `isFieldValid` returned false with BOTH of its Toasts commented out.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        vm.actionChannel.trySend(LoanDisbursementAction.UpdateAmount("not a number"))
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.stateFlow.value.amountError)
        assertTrue(commands.disbursements.isEmpty())
    }

    @Test
    fun aZeroOrNegativeAmountIsRefused() = runTest(dispatcher) {
        // The original checked only that the string PARSED, so "0" and "-500" both passed —
        // a zero disbursement is a no-op transaction on the account, a negative one is nonsense.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(LoanDisbursementAction.UpdateAmount("0"))
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.amountError)

        vm.actionChannel.trySend(LoanDisbursementAction.UpdateAmount("-500"))
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.amountError)

        assertTrue(commands.disbursements.isEmpty())
    }

    @Test
    fun typingClearsTheAmountError() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.amountError)

        vm.actionChannel.trySend(LoanDisbursementAction.UpdateAmount("1"))
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.amountError)
    }

    @Test
    fun aSecondSubmitWhileOneIsInFlightIsIgnored() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeLoanCommandRepository(gate = gate)
        val vm = viewModel(commands)
        vm.fillValid()
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.disbursements.size, "a double tap must not disburse twice")
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }

    // ---- the outcome ----

    @Test
    fun successShowsTheDialog() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fillValid()
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanDisbursementState.DialogState.ShowStatusDialog>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals(ResultStatus.SUCCESS, dialog.status)
    }

    @Test
    fun offlineSaysTheDisbursementWasNotSentAndQueuesNOTHING() = runTest(dispatcher) {
        // Unlike a repayment, a disbursement has NO offline path: the money has not moved yet, so
        // there is nothing captured that refusing would lose. Telling the officer to find signal
        // is the correct outcome, not a queued promise.
        val commands = FakeLoanCommandRepository(
            disburseResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        vm.fillValid()
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanDisbursementState.DialogState.ShowStatusDialog>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals(ResultStatus.FAILURE, dialog.status)
        assertTrue(dialog.msg.contains("No connection"), "message was ${dialog.msg}")
    }

    @Test
    fun aRejectionSurfacesFineractsOwnReasonAndKeepsTheFormFilled() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            disburseResult = {
                MutationResult.Failed(
                    fineractRejection("disbursement date cannot be before the approval date"),
                    rolledBack = false,
                )
            },
        )
        val vm = viewModel(commands)
        vm.fillValid(amount = "8000")
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanDisbursementState.DialogState.ShowStatusDialog>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals("disbursement date cannot be before the approval date", dialog.msg)
        // Still filled in, so the officer corrects the DATE and retries. The original replaced the
        // whole form with a full-screen error.
        assertEquals("8000", vm.stateFlow.value.amount)
    }

    @Test
    fun theDialogCanBeDismissedWithoutLeaving() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            disburseResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        vm.fillValid()
        vm.actionChannel.trySend(LoanDisbursementAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanDisbursementAction.CloseDialog)
        testScheduler.advanceUntilIdle()

        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun theFormIsSeededOnceAndASecondPrefillCannotOverwriteTyping() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(LoanDisbursementAction.Prefilled("10000.0"))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanDisbursementAction.UpdateAmount("9000"))
        testScheduler.advanceUntilIdle()

        assertTrue(vm.stateFlow.value.prefilled)
        assertEquals("9000", vm.stateFlow.value.amount)
    }

    @Test
    fun retryReachesTheRead() = runTest(dispatcher) {
        val repository = FakeLoanRepository(disburseTemplate = templateState)
        val vm = viewModel(repository = repository)
        testScheduler.advanceUntilIdle()

        repository.refreshes.test {
            vm.actionChannel.trySend(LoanDisbursementAction.OnRetry)
            testScheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        /** 2026-03-14 12:00 UTC — noon, so the device zone cannot shift the DATE. */
        const val DISBURSE_MILLIS = 1_773_489_600_000L
    }
}
