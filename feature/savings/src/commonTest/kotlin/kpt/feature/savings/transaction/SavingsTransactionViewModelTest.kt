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

package kpt.feature.savings.transaction

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
import kpt.core.data.testing.FakeSavingsAccountRepository
import kpt.core.data.testing.FakeSavingsCommandRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.payment.PaymentTypeOption
import kpt.core.model.savings.SavingsAccountTransactionTemplate
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The amount is this screen's whole risk surface. The original validated it with
 * `amount.toFloat() == 0f`, which rejects exactly one value — so a NEGATIVE deposit submitted, and
 * a large one lost its cents to Float.
 */
class SavingsTransactionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun template() = SavingsAccountTransactionTemplate(
        paymentTypeOptions = listOf(
            PaymentTypeOption(id = 3, name = "Cash"),
            PaymentTypeOption(id = 4, name = "Mobile Money"),
        ),
    )

    private fun viewModel(
        commands: FakeSavingsCommandRepository = FakeSavingsCommandRepository(),
        repo: FakeSavingsAccountRepository = FakeSavingsAccountRepository(
            transactionTemplate = { template() },
        ),
        kind: TransactionKind = TransactionKind.DEPOSIT,
        accountType: String = "savingsaccounts",
    ) = SavingsTransactionViewModel(
        repository = repo,
        commands = commands,
        savedStateHandle = SavedStateHandle(
            mapOf("accountType" to accountType, "accountId" to 55, "kind" to kind.name),
        ),
    )

    // ---- amount validation ------------------------------------------------------------------

    @Test
    fun `a negative amount is refused`() {
        // The original's `amount.toFloat() == 0f` rejects ONLY exactly zero, so "-500" parsed to
        // -500f, was not 0f, and submitted a negative deposit.
        assertEquals(AmountError.NOT_POSITIVE, "-500".validateAmount())
        assertEquals(AmountError.NOT_POSITIVE, "0".validateAmount())
        assertEquals(AmountError.NOT_POSITIVE, "0.00".validateAmount())
    }

    @Test
    fun `a non-numeric amount is refused and a blank one names itself`() {
        assertEquals(AmountError.NOT_A_NUMBER, "five hundred".validateAmount())
        assertEquals(AmountError.MISSING, "".validateAmount())
        assertEquals(AmountError.MISSING, "   ".validateAmount())
    }

    @Test
    fun `a large amount keeps its cents`() {
        // `toFloat()` carries ~7 significant digits, so this became 1.0E7 and lost the .50.
        assertNull("10000000.50".validateAmount())
    }

    @Test
    fun `review refuses to open on an invalid amount`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()

        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("-5"))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        advanceUntilIdle()

        assertEquals(AmountError.NOT_POSITIVE, vm.stateFlow.value.amountError)
        assertTrue(!vm.stateFlow.value.isReviewing)
        assertTrue(commands.transactions.isEmpty())
    }

    @Test
    fun `editing the amount clears its error`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.trySendAction(SavingsTransactionAction.OnReview)
        advanceUntilIdle()
        assertEquals(AmountError.MISSING, vm.stateFlow.value.amountError)

        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        advanceUntilIdle()
        assertNull(vm.stateFlow.value.amountError)
    }

    // ---- the payload ------------------------------------------------------------------------

    @Test
    fun `the payload carries the officer's own amount string and a paired date`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()

        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("10000000.50"))
        vm.trySendAction(SavingsTransactionAction.OnDateChanged(1_761_000_000_000))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()

        val (accountType, accountId, input) = commands.transactions.single()
        assertEquals("savingsaccounts", accountType)
        assertEquals(55, accountId)
        // Verbatim — no Double round-trip to re-render what the officer typed.
        assertEquals("10000000.50", input.transactionAmount)
        assertEquals(ApiDateFormatter.formatForApi(1_761_000_000_000), input.transactionDate)
        assertEquals(ApiDateFormatter.DATE_FORMAT, input.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, input.locale)
    }

    @Test
    fun `an unchosen payment type is omitted, not sent as zero`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()

        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()

        // The original defaulted `paymentTypeId` to the Int `0` and sent `"0"` — an id that names
        // no row.
        assertNull(commands.transactions.single().third.paymentTypeId)
    }

    @Test
    fun `a chosen payment type travels as its id`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()

        val option = vm.stateFlow.value.paymentTypes.first { it.name == "Mobile Money" }
        vm.trySendAction(SavingsTransactionAction.OnPaymentTypeSelected(option))
        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()

        // The ID, from the same option the dropdown showed. The original had a free TEXT field for
        // the payment-type LABEL while `paymentTypeId` stayed 0 — two unrelated values.
        assertEquals(4, commands.transactions.single().third.paymentTypeId)
    }

    @Test
    fun `deposit and withdrawal send different commands`() = runTest(dispatcher) {
        listOf(TransactionKind.DEPOSIT to "deposit", TransactionKind.WITHDRAWAL to "withdrawal")
            .forEach { (kind, command) ->
                val commands = FakeSavingsCommandRepository()
                val vm = viewModel(commands, kind = kind)
                advanceUntilIdle()
                vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
                vm.trySendAction(SavingsTransactionAction.OnReview)
                vm.trySendAction(SavingsTransactionAction.OnConfirm)
                advanceUntilIdle()

                assertEquals(listOf(command), commands.transactionCommands)
            }
    }

    @Test
    fun `a blank note is omitted`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()

        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        vm.trySendAction(SavingsTransactionAction.OnNoteChanged("  "))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()

        assertNull(commands.transactions.single().third.note)
    }

    // ---- the template -----------------------------------------------------------------------

    @Test
    fun `the template is asked for with the account type AND the command`() = runTest(dispatcher) {
        val repo = FakeSavingsAccountRepository(transactionTemplate = { template() })
        viewModel(repo = repo, kind = TransactionKind.WITHDRAWAL, accountType = "recurringdepositaccounts")
        advanceUntilIdle()

        assertEquals(
            listOf(Triple("recurringdepositaccounts", 55, "withdrawal")),
            repo.templateQueries,
        )
    }

    @Test
    fun `a failed template read does NOT block the write`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(
            commands,
            repo = FakeSavingsAccountRepository(transactionTemplate = { null }),
        )
        advanceUntilIdle()

        assertTrue(vm.stateFlow.value.templateFailed)
        assertEquals(emptyList(), vm.stateFlow.value.paymentTypes)

        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()

        // Refusing to let an officer record cash they have already taken, because a REFERENCE list
        // did not load, would be the worse failure.
        assertEquals(1, commands.transactions.size)
    }

    @Test
    fun `a failed template read is recoverable`() = runTest(dispatcher) {
        var fail = true
        val vm = viewModel(
            repo = FakeSavingsAccountRepository(
                transactionTemplate = { if (fail) null else template() },
            ),
        )
        advanceUntilIdle()
        assertTrue(vm.stateFlow.value.templateFailed)

        fail = false
        vm.trySendAction(SavingsTransactionAction.OnRetryTemplate)
        advanceUntilIdle()

        assertTrue(!vm.stateFlow.value.templateFailed)
        assertEquals(listOf(3, 4), vm.stateFlow.value.paymentTypes.map { it.id })
    }

    // ---- outcomes ---------------------------------------------------------------------------

    @Test
    fun `success surfaces Fineract's transaction id as the officer's reference`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()

        val dialog = assertIs<TransactionDialog.Succeeded>(vm.stateFlow.value.dialogState)
        assertEquals(4_200, dialog.transactionId)
        assertTrue(!vm.stateFlow.value.isReviewing)
    }

    @Test
    fun `offline is reported as offline`() = runTest(dispatcher) {
        val vm = viewModel(
            FakeSavingsCommandRepository(
                transactionResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
            ),
        )
        advanceUntilIdle()
        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()

        assertTrue(assertIs<TransactionDialog.Failed>(vm.stateFlow.value.dialogState).offline)
    }

    @Test
    fun `dismissing a failure keeps the amount`() = runTest(dispatcher) {
        val vm = viewModel(
            FakeSavingsCommandRepository(
                transactionResult = {
                    MutationResult.Failed(fineractRejection("insufficient balance"), rolledBack = false)
                },
            ),
        )
        advanceUntilIdle()
        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()

        assertEquals(
            "insufficient balance",
            assertIs<TransactionDialog.Failed>(vm.stateFlow.value.dialogState).msg,
        )

        vm.trySendAction(SavingsTransactionAction.CloseDialog)
        advanceUntilIdle()

        // The cash is already in the officer's hand; the amount is the one value they cannot
        // re-derive. The original replaced the form with a progress bar then an error.
        assertEquals("500", vm.stateFlow.value.amount)
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun `a double confirm sends one transaction`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository(gate = CompletableDeferred())
        val vm = viewModel(commands)
        advanceUntilIdle()

        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()
        vm.trySendAction(SavingsTransactionAction.OnConfirm)
        advanceUntilIdle()

        // Money moves here, so the guard matters more than anywhere else in the port.
        assertEquals(1, commands.transactions.size)
    }

    @Test
    fun `cancelling the review sends nothing and keeps the form`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()

        vm.trySendAction(SavingsTransactionAction.OnAmountChanged("500"))
        vm.trySendAction(SavingsTransactionAction.OnReview)
        advanceUntilIdle()
        assertTrue(vm.stateFlow.value.isReviewing)

        vm.trySendAction(SavingsTransactionAction.CloseReview)
        advanceUntilIdle()

        assertTrue(!vm.stateFlow.value.isReviewing)
        assertEquals("500", vm.stateFlow.value.amount)
        assertTrue(commands.transactions.isEmpty())
    }
}
