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

package kpt.feature.loan.repayment

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
import kpt.core.common.utils.DateHelper
import kpt.core.data.testing.FakeLoanCommandRepository
import kpt.core.data.testing.FakeLoanRepository
import kpt.core.data.testing.FakeOfflineQueueRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanAccountSummary
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.payment.PaymentTypeOption
import kpt.core.model.savings.SavingAccountCurrency
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The repayment form is the highest-stakes screen in this feature — it records collected cash, and
 * the two ways to get that wrong are recording it twice and losing it entirely. Both have tests.
 */
class LoanRepaymentViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val loanState = MutableStateFlow<ScreenState<LoanWithAssociations>>(
        ScreenState.Content(
            LoanWithAssociations(
                id = 7,
                accountNo = "000000007",
                clientName = "Asha",
                loanProductName = "Group Loan",
                summary = LoanAccountSummary(totalOverdue = 250.0),
            ),
        ),
    )

    private val templateState = MutableStateFlow<ScreenState<LoanRepaymentTemplate>>(
        ScreenState.Content(
            LoanRepaymentTemplate(
                loanId = 7,
                amount = 1_000.0,
                currency = SavingAccountCurrency(code = "INR", decimalPlaces = 2),
                paymentTypeOptions = listOf(
                    PaymentTypeOption(id = 1, name = "Cash"),
                    PaymentTypeOption(id = 4, name = "Mobile Money"),
                ),
            ),
        ),
    )

    private val pending =
        MutableStateFlow<ScreenState<List<LoanRepaymentRequest>>>(ScreenState.Content(emptyList()))

    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        queue: FakeOfflineQueueRepository = FakeOfflineQueueRepository(pendingLoanRepayments = pending),
        repository: FakeLoanRepository = FakeLoanRepository(
            loan = loanState,
            repaymentTemplate = templateState,
        ),
        loanId: Int = 7,
    ) = LoanRepaymentViewModel(
        repository,
        commands,
        queue,
        SavedStateHandle(mapOf("loanId" to loanId)),
    )

    /** Fills the form with a valid, submittable payment. */
    private fun LoanRepaymentViewModel.fillValid(amount: String = "1000") {
        actionChannel.trySend(LoanRepaymentAction.SelectPaymentType(LoanPaymentType(1, "Cash")))
        actionChannel.trySend(LoanRepaymentAction.UpdateAmount(amount))
    }

    // ---- the total ----

    @Test
    fun theTotalSumsAllThreeFieldsAndRoundsToTwoDecimals() {
        assertEquals(1_150.0, totalOf(fees = "50", amount = "1000", additionalPayment = "100"))
        // Floating-point sums are rounded, as the original rounded them — 0.1 + 0.2 is otherwise
        // 0.30000000000000004 and that string is what would be POSTED as the transaction amount.
        assertEquals(0.3, totalOf(fees = "0.1", amount = "0.2", additionalPayment = ""))
    }

    @Test
    fun anUnparseableOrBlankFieldReadsAsZeroInTheTotal() {
        // The original's `calculateTotal` caught NumberFormatException and used 0.0. Kept, because
        // a half-typed field must not blank the running total while the officer is typing.
        assertEquals(1_000.0, totalOf(fees = "", amount = "1000", additionalPayment = "abc"))
    }

    // ---- validation ----

    @Test
    fun aPaymentTypeIsRequiredAndItIsCheckedByIdNotByName() {
        // The original checked `paymentType.isNotBlank()` on the DISPLAY STRING, so a type whose
        // name came back blank read as unselected and a selection carrying id 0 read as selected.
        val noType = LoanRepaymentState(loanId = 7, amount = "100")
        assertTrue(!noType.isSubmittable())
        assertTrue(noType.copy(paymentTypeId = 1).isSubmittable())
    }

    @Test
    fun aZeroTotalIsNotSubmittable() {
        // THE FIX. All three fields blank passed the original's `isAllFieldsValid` — each read as
        // 0.0 and each `>= 0` — so an officer could post a zero-amount repayment, which Fineract
        // accepts as a real transaction on the account.
        val blank = LoanRepaymentState(loanId = 7, paymentTypeId = 1)
        assertTrue(!blank.isSubmittable())
        assertTrue(!blank.copy(amount = "0").isSubmittable())
        assertTrue(blank.copy(amount = "0.01").isSubmittable())
    }

    @Test
    fun aNonNumericAmountIsNotSubmittable() {
        val state = LoanRepaymentState(loanId = 7, paymentTypeId = 1, amount = "ten")
        assertTrue(!state.isSubmittable())
    }

    @Test
    fun aNegativeFieldIsNotSubmittable() {
        // A negative fee would reduce the posted total below what was collected.
        val state = LoanRepaymentState(loanId = 7, paymentTypeId = 1, amount = "100", fees = "-20")
        assertTrue(!state.isSubmittable())
    }

    // ---- the form ----

    @Test
    fun theHeaderAndTheOptionsComeFromBothReadsAtOnce() = runTest(dispatcher) {
        val vm = viewModel()

        vm.form.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val form = assertIs<ScreenState.Content<LoanRepaymentForm>>(awaitItem()).data
            // From the LOAN read.
            assertEquals("Asha", form.clientName)
            assertEquals("000000007", form.loanAccountNumber)
            assertEquals(250.0, form.amountInArrears)
            // From the TEMPLATE read. Combined, so the screen never draws one loan's arrears
            // beside another request's payment types.
            assertEquals(1_000.0, form.amountDue)
            assertEquals(listOf(1, 4), form.paymentTypes.map { it.id })
            assertEquals("INR", form.currencyCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theFormWaitsForBOTHReads() = runTest(dispatcher) {
        // The original showed whichever had landed: the header rendered from `getLoanById` while
        // the form was still fetching its template, so the screen appeared ready and was not.
        templateState.value = ScreenState.Loading
        val vm = viewModel()

        vm.form.test {
            assertIs<ScreenState.Loading>(awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun retryReachesBothReads() = runTest(dispatcher) {
        // The original re-ran whichever load a flag said had failed, so a failure in the other
        // was unreachable from the retry button.
        val repository = FakeLoanRepository(loan = loanState, repaymentTemplate = templateState)
        val vm = viewModel(repository = repository)
        testScheduler.advanceUntilIdle()

        repository.refreshesFor("loan").test {
            repository.refreshesFor("repaymentTemplate").test {
                vm.actionChannel.trySend(LoanRepaymentAction.OnRetry)
                testScheduler.advanceUntilIdle()
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- the payload ----

    @Test
    fun theRequestPairsItsDateWithTheFormatItDeclares() = runTest(dispatcher) {
        // This is the ONE date payload in feature/loan the original got right: the helper formats
        // with `DateHelper.SHORT_MONTH` ("dd-MM-yyyy") and the request declares that same
        // constant. Carried over, and pinned — because the identical-looking line in
        // LoanApprovalViewModel declared a DIFFERENT format and could not be parsed.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        vm.fillValid()
        vm.actionChannel.trySend(LoanRepaymentAction.UpdateDate(APPROVED_MILLIS))
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val request = commands.payments.single().second
        assertEquals(DateHelper.SHORT_MONTH, request.dateFormat)
        assertEquals(DateHelper.getDateAsStringFromLong(APPROVED_MILLIS), request.transactionDate)
        assertEquals("14-03-2026", request.transactionDate)
    }

    @Test
    fun theRequestSendsTheSUMAsOneTransactionAmount() = runTest(dispatcher) {
        // Fineract's repayment command takes a single amount, so the three fields are summed. The
        // officer sees the breakdown; the server sees the total.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        vm.fillValid(amount = "1000")
        vm.actionChannel.trySend(LoanRepaymentAction.UpdateFees("50"))
        vm.actionChannel.trySend(LoanRepaymentAction.UpdateAdditionalPayment("25.5"))
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals("1075.5", commands.payments.single().second.transactionAmount)
    }

    @Test
    fun theRequestCarriesThePaymentTypeIdAndTheAccountNumberFromTheLoan() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        vm.actionChannel.trySend(
            LoanRepaymentAction.SelectPaymentType(LoanPaymentType(4, "Mobile Money")),
        )
        vm.actionChannel.trySend(LoanRepaymentAction.UpdateAmount("500"))
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val request = commands.payments.single().second
        assertEquals("4", request.paymentTypeId)
        // Read from the form rather than held as a second copy in the editable state.
        assertEquals("000000007", request.accountNumber)
        assertEquals(7, request.loanId)
    }

    // ---- recording it twice ----

    @Test
    fun aRepaymentAlreadyInTheQueueIsSurfacedSoTheFormIsNotDrawn() = runTest(dispatcher) {
        // Two repayments for one visit is money counted twice, and the queued one has not reached
        // Fineract so the server cannot dedupe it. The original refused too; this keeps the
        // refusal and reads the SAME queue the sync screen drains.
        pending.value = ScreenState.Content(listOf(LoanRepaymentRequest(loanId = 7)))
        val vm = viewModel()

        vm.alreadyQueued.test {
            assertEquals(false, awaitItem())
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onceTheQueuedRepaymentSyncsTheFormIsNoLongerBlocked() = runTest(dispatcher) {
        // The queue stream reports a drained queue as an empty Content (no `isEmpty` predicate).
        // With the predicate it held the LAST content — the synced repayment — and kept refusing.
        pending.value = ScreenState.Content(listOf(LoanRepaymentRequest(loanId = 7)))
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        assertEquals(true, vm.alreadyQueued.value)

        pending.value = ScreenState.Content(emptyList())
        testScheduler.advanceUntilIdle()
        assertEquals(false, vm.alreadyQueued.value)
    }

    @Test
    fun aRepaymentQueuedForADIFFERENTLoanDoesNotBlockThisOne() = runTest(dispatcher) {
        pending.value = ScreenState.Content(listOf(LoanRepaymentRequest(loanId = 99)))
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        assertEquals(false, vm.alreadyQueued.value)
    }

    @Test
    fun submitRefusesWhenOneWasQueuedWhileTheFormSatOpen() = runTest(dispatcher) {
        // The render-time flag is a read of a flow that can lag, and the form can sit open for
        // minutes. The submit path re-checks rather than trusting it.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        vm.fillValid()
        testScheduler.advanceUntilIdle()

        pending.value = ScreenState.Content(listOf(LoanRepaymentRequest(loanId = 7)))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(commands.payments.isEmpty(), "nothing is sent while one is queued")
        assertIs<LoanRepaymentState.DialogState.AlreadyQueued>(vm.stateFlow.value.dialogState)
    }

    @Test
    fun aSecondSubmitWhileOneIsInFlightIsIgnored() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeLoanCommandRepository(gate = gate)
        val vm = viewModel(commands)
        vm.fillValid()
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.payments.size, "a double tap must not pay twice")
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }

    // ---- losing it entirely ----

    @Test
    fun offlineQUEUESTheRepaymentRatherThanReportingFailure() = runTest(dispatcher) {
        // The repayment is the one write in this feature with a designed offline path: the officer
        // has already taken the cash, so refusing to record it loses the money. The original threw
        // the offline case into its generic `payment_failed` message and queued nothing.
        val queue = FakeOfflineQueueRepository(pendingLoanRepayments = pending)
        val commands = FakeLoanCommandRepository(
            result = { MutationResult.Blocked(BlockReason.OFFLINE) },
            paymentResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands, queue)
        vm.fillValid(amount = "750")
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, queue.enqueued.size)
        assertEquals("750.0", queue.enqueued.single().second.transactionAmount)
        // Queued is NOT success — the dialog says the server has not seen it.
        assertIs<LoanRepaymentState.DialogState.Queued>(vm.stateFlow.value.dialogState)
    }

    @Test
    fun aQueueWriteThatFAILSIsReportedAsFailureNotAsQueued() = runTest(dispatcher) {
        // The worst case: offline, so nothing reached Fineract, AND the local save failed, so
        // nothing holds the payment either. Reporting "saved to sync" here would lose the cash
        // silently.
        val queue = FakeOfflineQueueRepository(
            pendingLoanRepayments = pending,
            enqueueResult = { MutationResult.Failed(IllegalStateException("disk full"), rolledBack = true) },
        )
        val commands = FakeLoanCommandRepository(
            paymentResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands, queue)
        vm.fillValid()
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanRepaymentState.DialogState.Failed>(vm.stateFlow.value.dialogState)
        assertTrue(dialog.msg.contains("nothing has been recorded"), "message was ${dialog.msg}")
    }

    @Test
    fun aNonOfflineBlockIsAFailureAndIsNotQueued() = runTest(dispatcher) {
        // Only OFFLINE has a designed offline path. Any other block reason means the gateway
        // refused for a reason queueing would not fix.
        val queue = FakeOfflineQueueRepository(pendingLoanRepayments = pending)
        val commands = FakeLoanCommandRepository(
            paymentResult = { MutationResult.Blocked(BlockReason.UNAUTHENTICATED) },
        )
        val vm = viewModel(commands, queue)
        vm.fillValid()
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(queue.enqueued.isEmpty())
        assertIs<LoanRepaymentState.DialogState.Failed>(vm.stateFlow.value.dialogState)
    }

    // ---- the outcome ----

    @Test
    fun successCarriesFineractsTransactionIdAndDoesNotReReadTheLoan() = runTest(dispatcher) {
        val repository = FakeLoanRepository(loan = loanState, repaymentTemplate = templateState)
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands, repository = repository)
        vm.fillValid()

        repository.refreshesFor("loan").test {
            vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
            testScheduler.advanceUntilIdle()
            // The command ingests the repaid loan through the `loans` store; a refresh here
            // would be the post-write re-read the write door replaced.
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        val dialog = assertIs<LoanRepaymentState.DialogState.Submitted>(vm.stateFlow.value.dialogState)
        assertEquals(9_001, dialog.transactionId)
    }

    @Test
    fun aRejectionSurfacesFineractsOwnReasonAndKeepsTheFormFilled() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            paymentResult = {
                MutationResult.Failed(
                    fineractRejection("transaction date is before the last transaction"),
                    rolledBack = false,
                )
            },
        )
        val vm = viewModel(commands)
        vm.fillValid(amount = "600")
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanRepaymentState.DialogState.Failed>(vm.stateFlow.value.dialogState)
        assertEquals("transaction date is before the last transaction", dialog.msg)
        // The amounts are STILL TYPED IN — the officer corrects the date and retries rather than
        // re-entering everything. The original navigated away on every outcome.
        assertEquals("600", vm.stateFlow.value.amount)

        vm.actionChannel.trySend(LoanRepaymentAction.CloseDialog)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun theReviewSheetIsOpenedAndDismissedThroughState() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(LoanRepaymentAction.OnReview)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.showReview)

        vm.actionChannel.trySend(LoanRepaymentAction.OnDismissReview)
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.showReview)
    }

    @Test
    fun submittingClosesTheReviewSheet() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fillValid()
        vm.actionChannel.trySend(LoanRepaymentAction.OnReview)
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(!vm.stateFlow.value.showReview, "the sheet must not sit over the outcome dialog")
    }

    @Test
    fun theRouteArgumentReachesBothReadsAndTheWrite() = runTest(dispatcher) {
        val repository = FakeLoanRepository(loan = loanState, repaymentTemplate = templateState)
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands, repository = repository, loanId = 42)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf<Any>(42), repository.requestedKeys["loan"]?.toList())
        assertEquals(listOf<Any>(42), repository.requestedKeys["repaymentTemplate"]?.toList())

        vm.fillValid()
        vm.actionChannel.trySend(LoanRepaymentAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertEquals(42, commands.payments.single().first)
    }

    private companion object {
        /** 2026-03-14 12:00 UTC — noon, so the device zone cannot shift the DATE. */
        const val APPROVED_MILLIS = 1_773_489_600_000L
    }
}
