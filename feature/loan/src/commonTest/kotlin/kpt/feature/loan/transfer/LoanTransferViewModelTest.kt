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

package kpt.feature.loan.transfer

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
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.loanWithAssociations.SavingAccountCurrency
import kpt.core.model.objects.account.loan.transfer.AccountOption
import kpt.core.model.objects.account.loan.transfer.AccountTransferTemplate
import kpt.core.model.objects.account.loan.transfer.AccountTypeOption
import kpt.core.model.objects.account.loan.transfer.ClientOption
import kpt.core.model.objects.account.loan.transfer.OfficeOption
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * This screen's logic is the CASCADE and the submit guard. The original crashed on retry and
 * resolved two of its four selections by list index, so both get tests.
 */
class LoanTransferViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val loanState = MutableStateFlow<ScreenState<LoanWithAssociations>>(
        ScreenState.Content(
            LoanWithAssociations(
                id = 7,
                accountNo = "000000007",
                clientId = 53,
                clientOfficeId = 2,
                currency = SavingAccountCurrency(code = "INR", decimalPlaces = 2),
            ),
        ),
    )

    private val offices = listOf(
        OfficeOption(id = 9, name = "Zulu Branch"),
        OfficeOption(id = 3, name = "Alpha Branch"),
        // Unsendable: no id.
        OfficeOption(id = null, name = "Broken"),
    )
    private val clients = listOf(
        ClientOption(id = 71, displayName = "Zara"),
        ClientOption(id = 12, displayName = "Amit"),
    )
    private val accountTypes = listOf(
        AccountTypeOption(id = 2, value = "Savings"),
        AccountTypeOption(id = 1, value = "Loan"),
    )
    private val accounts = listOf(
        AccountOption(id = 500, accountNo = "SA0002"),
        AccountOption(id = 400, accountNo = "SA0001"),
    )

    /** The cascade: each level appears only once the level above it has been chosen. */
    private fun cascade(
        toOfficeId: Int?,
        toClientId: Int?,
        toAccountType: Int?,
    ) = AccountTransferTemplate(
        toOfficeOptions = offices,
        toClientOptions = if (toOfficeId != null) clients else emptyList(),
        toAccountTypeOptions = if (toClientId != null) accountTypes else emptyList(),
        toAccountOptions = if (toAccountType != null) accounts else emptyList(),
    )

    private fun repository() = FakeLoanRepository(
        loan = loanState,
        transferTemplate = { office, client, type -> cascade(office, client, type) },
    )

    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        repository: FakeLoanRepository = repository(),
        loanId: Int = 7,
    ) = LoanTransferViewModel(
        repository,
        commands,
        SavedStateHandle(mapOf("loanId" to loanId)),
    )

    /** Walks the whole cascade to a submittable form. */
    private fun LoanTransferViewModel.fillValid() {
        actionChannel.trySend(LoanTransferAction.OnOfficeSelected(OfficeOption(id = 3, name = "Alpha Branch")))
        actionChannel.trySend(LoanTransferAction.OnClientSelected(ClientOption(id = 12, displayName = "Amit")))
        actionChannel.trySend(LoanTransferAction.OnAccountTypeSelected(AccountTypeOption(id = 2, value = "Savings")))
        actionChannel.trySend(LoanTransferAction.OnAccountSelected(AccountOption(id = 400, accountNo = "SA0001")))
        actionChannel.trySend(LoanTransferAction.OnAmountChange("250"))
        actionChannel.trySend(LoanTransferAction.OnDescriptionChange("Overpayment refund"))
    }

    // ---- the FROM side is read, not passed in ----

    @Test
    fun theFromSideComesFromTheLOANNotFromTheRoute() = runTest(dispatcher) {
        // The original's route carried five snapshotted fields. One id, and the loan — already
        // cached by whichever detail screen sent the officer here — supplies the rest.
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        val state = vm.stateFlow.value
        assertEquals(53, state.fromClientId)
        assertEquals(2, state.fromOfficeId)
        assertEquals("000000007", state.fromAccountNumber)
        assertEquals("INR", state.currency)
    }

    @Test
    fun theTemplateIsNotQueriedUntilTheLoanHasLanded() = runTest(dispatcher) {
        // The template read needs the from-side, so asking before the loan arrives would send
        // `fromClientId = -1`.
        loanState.value = ScreenState.Loading
        val repository = repository()
        viewModel(repository = repository)
        testScheduler.advanceUntilIdle()

        assertTrue(repository.transferTemplateQueries.isEmpty())

        loanState.value = ScreenState.Content(
            LoanWithAssociations(id = 7, clientId = 53, clientOfficeId = 2),
        )
        testScheduler.advanceUntilIdle()

        assertEquals(1, repository.transferTemplateQueries.size)
        assertEquals(53, repository.transferTemplateQueries.single().fromClientId)
    }

    // ---- the cascade ----

    @Test
    fun theFirstQueryAsksWithNoToSideChosen() = runTest(dispatcher) {
        val repository = repository()
        viewModel(repository = repository)
        testScheduler.advanceUntilIdle()

        val query = repository.transferTemplateQueries.single()
        assertNull(query.toOfficeId)
        assertNull(query.toClientId)
        assertNull(query.toAccountType)
        // A loan is portfolio account type 1 — the original's route defaulted it to that.
        assertEquals(1, query.fromAccountType)
        assertEquals(7, query.fromAccountId)
    }

    @Test
    fun eachSelectionNarrowsTheNextQuery() = runTest(dispatcher) {
        val repository = repository()
        val vm = viewModel(repository = repository)
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(
            LoanTransferAction.OnOfficeSelected(OfficeOption(id = 3, name = "Alpha Branch")),
        )
        testScheduler.advanceUntilIdle()
        assertEquals(3, repository.transferTemplateQueries.last().toOfficeId)

        vm.actionChannel.trySend(
            LoanTransferAction.OnClientSelected(ClientOption(id = 12, displayName = "Amit")),
        )
        testScheduler.advanceUntilIdle()
        assertEquals(12, repository.transferTemplateQueries.last().toClientId)

        vm.actionChannel.trySend(
            LoanTransferAction.OnAccountTypeSelected(AccountTypeOption(id = 2, value = "Savings")),
        )
        testScheduler.advanceUntilIdle()
        assertEquals(2, repository.transferTemplateQueries.last().toAccountType)

        // Choosing the ACCOUNT is the last level, so it asks nothing further.
        val before = repository.transferTemplateQueries.size
        vm.actionChannel.trySend(
            LoanTransferAction.OnAccountSelected(AccountOption(id = 400, accountNo = "SA0001")),
        )
        testScheduler.advanceUntilIdle()
        assertEquals(before, repository.transferTemplateQueries.size)
    }

    @Test
    fun changingALevelCLEARSEveryLevelBelowIt() = runTest(dispatcher) {
        // The original cleared the levels below office and below client, but NOT below ACCOUNT
        // TYPE — so changing the account type left the previously-chosen ACCOUNT selected, and
        // that account may not exist under the new type.
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        testScheduler.advanceUntilIdle()
        assertEquals(400, vm.stateFlow.value.account?.id)

        vm.actionChannel.trySend(
            LoanTransferAction.OnAccountTypeSelected(AccountTypeOption(id = 1, value = "Loan")),
        )
        testScheduler.advanceUntilIdle()

        assertNull(vm.stateFlow.value.account, "the account below the changed type is cleared")
        assertEquals(1, vm.stateFlow.value.accountType?.id)
        // The levels ABOVE are untouched.
        assertEquals(3, vm.stateFlow.value.office?.id)
        assertEquals(12, vm.stateFlow.value.client?.id)
    }

    @Test
    fun changingTheOfficeClearsAllThreeLevelsBelow() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(
            LoanTransferAction.OnOfficeSelected(OfficeOption(id = 9, name = "Zulu Branch")),
        )
        testScheduler.advanceUntilIdle()

        val state = vm.stateFlow.value
        assertNull(state.client)
        assertNull(state.accountType)
        assertNull(state.account)
        assertTrue(state.clients.isEmpty() || state.client == null)
    }

    @Test
    fun theOptionsAreSortedAndUnsendableOnesAreDropped() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        // Sorted by name, as the original sorted them — and the id-less "Broken" office is gone,
        // because it cannot be sent so it must not be selectable.
        assertEquals(listOf("Alpha Branch", "Zulu Branch"), vm.stateFlow.value.offices.map { it.name })
    }

    @Test
    fun aFailedFirstTemplateReadIsReportedAndRetryable() = runTest(dispatcher) {
        val repository = FakeLoanRepository(
            loan = loanState,
            transferTemplate = { _, _, _ -> null },
        )
        val vm = viewModel(repository = repository)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.stateFlow.value.templateError)
        assertTrue(vm.stateFlow.value.offices.isEmpty())
        assertTrue(!vm.stateFlow.value.isLoadingTemplate)
    }

    // ---- validation ----

    @Test
    fun everyMissingFieldIsReportedAtONCE() = runTest(dispatcher) {
        // The original collected every error too — kept, because a form with six required fields
        // that reports one error per tap is six taps to discover what it wants.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val state = vm.stateFlow.value
        assertEquals(TransferFieldError.AMOUNT_INVALID, state.amountError)
        assertEquals(TransferFieldError.DESCRIPTION_REQUIRED, state.descriptionError)
        assertEquals(TransferFieldError.OFFICE_REQUIRED, state.officeError)
        assertEquals(TransferFieldError.CLIENT_REQUIRED, state.clientError)
        assertEquals(TransferFieldError.ACCOUNT_TYPE_REQUIRED, state.accountTypeError)
        assertEquals(TransferFieldError.ACCOUNT_REQUIRED, state.accountError)
        assertTrue(commands.transfers.isEmpty())
    }

    @Test
    fun aZeroAmountIsRefusedWithItsOwnMessage() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        vm.actionChannel.trySend(LoanTransferAction.OnAmountChange("0"))
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        // A distinct error from "not a number", as the original distinguished them.
        assertEquals(TransferFieldError.AMOUNT_NOT_POSITIVE, vm.stateFlow.value.amountError)
        assertTrue(commands.transfers.isEmpty())
    }

    @Test
    fun aBlankDescriptionIsRefused() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        vm.actionChannel.trySend(LoanTransferAction.OnDescriptionChange("   "))
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(TransferFieldError.DESCRIPTION_REQUIRED, vm.stateFlow.value.descriptionError)
        assertTrue(commands.transfers.isEmpty())
    }

    @Test
    fun typingClearsThatFieldsError() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.amountError != null)

        vm.actionChannel.trySend(LoanTransferAction.OnAmountChange("5"))
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.amountError)
        // The OTHER errors are still showing — only the field that changed is cleared.
        assertTrue(vm.stateFlow.value.officeError != null)
    }

    // ---- the payload ----

    @Test
    fun theRequestCarriesBothSidesAndAPairedDate() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val request = commands.transfers.single()
        // FROM, read off the loan.
        assertEquals(2, request.fromOfficeId)
        assertEquals(53, request.fromClientId)
        assertEquals(1, request.fromAccountType)
        assertEquals(7, request.fromAccountId)
        // TO, chosen by the officer.
        assertEquals(3, request.toOfficeId)
        assertEquals(12, request.toClientId)
        assertEquals(2, request.toAccountType)
        assertEquals(400, request.toAccountId)
        assertEquals(250.0, request.transferAmount)
        assertEquals("Overpayment refund", request.transferDescription)
        // The original paired `formattedFullDate` with `DATE_FORMAT_LONG` — both "dd MMMM yyyy",
        // so this screen's date was one of only two in the feature that worked. Held in one place
        // now rather than in two constants that happened to agree.
        assertEquals(ApiDateFormatter.DATE_FORMAT, request.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, request.locale)
    }

    @Test
    fun theDescriptionIsTrimmed() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        vm.actionChannel.trySend(LoanTransferAction.OnDescriptionChange("  refund  "))
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals("refund", commands.transfers.single().transferDescription)
    }

    // ---- THE crash ----

    @Test
    fun retryReSendsTheProvenDraftRatherThanReReadingNullableState() = runTest(dispatcher) {
        // THE REGRESSION. `submitTransfer` resolved four nullable ids with `!!`. Safe from the
        // happy path, because `validateFields()` ran first — but `OnRetryClick` called
        // `submitTransfer()` DIRECTLY, skipping validation, so retrying after a failure threw.
        val commands = FakeLoanCommandRepository(
            transferResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertEquals(1, commands.transfers.size)

        vm.actionChannel.trySend(LoanTransferAction.OnRetrySubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(2, commands.transfers.size, "retry re-sends")
        // Byte-for-byte the same TO side, because it is the same draft.
        assertEquals(commands.transfers[0].toAccountId, commands.transfers[1].toAccountId)
        assertEquals(commands.transfers[0].transferAmount, commands.transfers[1].transferAmount)
    }

    @Test
    fun retryBeforeAnySubmitDoesNothing() = runTest(dispatcher) {
        // No draft exists yet, so there is nothing to re-send — and nothing to dereference. The
        // original would have thrown on the four `!!`s.
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(LoanTransferAction.OnRetrySubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(commands.transfers.isEmpty())
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun aSecondSubmitWhileOneIsInFlightIsIgnored() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeLoanCommandRepository(gate = gate)
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.transfers.size, "a double tap must not transfer twice")
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }

    // ---- the outcome ----

    @Test
    fun successShowsTheDialogWithNoRetryOffered() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanTransferState.DialogState.Result>(vm.stateFlow.value.dialogState)
        assertEquals(ResultStatus.SUCCESS, dialog.status)
        assertTrue(!dialog.retryable)
    }

    @Test
    fun aRejectionSurfacesFineractsReasonAndOffersRetry() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            transferResult = {
                MutationResult.Failed(
                    fineractRejection("insufficient balance on the source account"),
                    rolledBack = false,
                )
            },
        )
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanTransferState.DialogState.Result>(vm.stateFlow.value.dialogState)
        assertEquals("insufficient balance on the source account", dialog.msg)
        assertTrue(dialog.retryable)
        // Everything the officer chose is still chosen.
        assertEquals(400, vm.stateFlow.value.account?.id)
        assertEquals("250", vm.stateFlow.value.amount)
    }

    @Test
    fun offlineSaysTheTransferWasNotSent() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            transferResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()
        vm.fillValid()
        vm.actionChannel.trySend(LoanTransferAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<LoanTransferState.DialogState.Result>(vm.stateFlow.value.dialogState)
        // OnlineRequired with no offline path: a transfer moves money between two accounts and
        // nothing has been captured that refusing would lose.
        assertTrue(dialog.msg.contains("No connection"), "message was ${dialog.msg}")
    }

    @Test
    fun retryFetchingReachesTheLoanReadToo() = runTest(dispatcher) {
        val repository = repository()
        val vm = viewModel(repository = repository)
        testScheduler.advanceUntilIdle()

        repository.refreshesFor("loan").test {
            vm.actionChannel.trySend(LoanTransferAction.OnRetryFetching)
            testScheduler.advanceUntilIdle()
            // Both reads: the loan may be what failed, and without it the template read has no
            // from-side to ask about.
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun backNavigationIsAnEvent() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(LoanTransferAction.NavigateBack)
            assertEquals(LoanTransferEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
