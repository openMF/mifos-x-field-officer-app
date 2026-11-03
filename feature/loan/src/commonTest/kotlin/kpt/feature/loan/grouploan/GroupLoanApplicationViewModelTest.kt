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

package kpt.feature.loan.grouploan

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeLoanCommandRepository
import kpt.core.data.testing.FakeLoanRepository
import kpt.core.model.objects.organisations.LoanProducts
import kpt.core.model.objects.template.loan.AmortizationTypeOptions
import kpt.core.model.objects.template.loan.GroupLoanTemplate
import kpt.core.model.objects.template.loan.InterestTypeOptions
import kpt.core.model.objects.template.loan.RepaymentFrequencyTypeOptions
import kpt.core.model.objects.template.loan.TermFrequencyTypeOptions
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The payload is this screen's logic, and the original's got six things wrong — the loan type, two
 * dates, five defaults, the repayment frequency, two unfillable fields and an unchosen `0`. Each
 * one is a test here, because every one of them is a value the server accepts without complaint.
 */
class GroupLoanApplicationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val products = listOf(
        LoanProducts(id = 11, name = "Group Grameen"),
        LoanProducts(id = 22, name = "Group Emergency"),
        // Unsendable: no id. Must not be offered.
        LoanProducts(id = null, name = "Draft product"),
    )

    /** The product's OWN terms — the five numbers the original hardcoded instead. */
    private fun template(principal: Double = 48_000.0) = GroupLoanTemplate(
        principal = principal,
        numberOfRepayments = 24,
        interestRatePerPeriod = 1.75,
        repaymentEvery = 1,
        termFrequency = 24,
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
    )

    private fun viewModel(
        repo: FakeLoanRepository = FakeLoanRepository(
            loanProducts = MutableStateFlow(ScreenState.Content(products)),
            groupLoanTemplate = { template() },
        ),
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
    ) = GroupLoanApplicationViewModel(
        repository = repo,
        commands = commands,
        savedStateHandle = SavedStateHandle(mapOf("groupId" to 31)),
        drafts = kpt.core.data.testing.FakeFormDraftRepository(),
    )

    /**
     * Fill in everything the form requires, so each test can vary one thing.
     *
     * The `advanceUntilIdle()` after the product is NOT incidental: the template load clears every
     * selection (the options came from the previous product), so choosing before it lands would
     * have them wiped. On screen the order is forced — the dropdowns are not drawn until the
     * template is there — and this helper has to reproduce that order rather than assume it.
     */
    private fun TestScope.chooseProductAndRequiredOptions(vm: GroupLoanApplicationViewModel) = with(vm) {
        trySendAction(GroupLoanApplicationAction.OnProductSelected(LoanProductOption(11, "Group Grameen")))
        advanceUntilIdle()
        trySendAction(
            GroupLoanApplicationAction.OnOptionSelected(
                GroupLoanField.LOAN_TERM_TYPE,
                GroupLoanOption(id = 2, label = "Months"),
            ),
        )
        trySendAction(
            GroupLoanApplicationAction.OnOptionSelected(
                GroupLoanField.REPAYMENT_FREQUENCY_TYPE,
                GroupLoanOption(id = 1, label = "Weeks"),
            ),
        )
        trySendAction(
            GroupLoanApplicationAction.OnOptionSelected(
                GroupLoanField.AMORTIZATION,
                GroupLoanOption(id = 1, label = "Equal instalments"),
            ),
        )
        trySendAction(
            GroupLoanApplicationAction.OnOptionSelected(
                GroupLoanField.INTEREST_TYPE,
                GroupLoanOption(id = 0, label = "Declining balance"),
            ),
        )
    }

    @Test
    fun `a product with no id is not offered`() = runTest(dispatcher) {
        viewModel().products.test {
            // WhileSubscribed, so the first emission is the seed; await the real one.
            assertEquals(ScreenState.Loading, awaitItem())
            val offered = assertIs<ScreenState.Content<List<LoanProductOption>>>(awaitItem()).data
            assertEquals(listOf(11, 22), offered.map { it.id })
        }
    }

    @Test
    fun `choosing a product prefills the five numbers from ITS template`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.trySendAction(GroupLoanApplicationAction.OnProductSelected(LoanProductOption(11, "Group Grameen")))
        advanceUntilIdle()

        // Not "10000.0" / "10" / "5.0" / "2" / "20" — the original's five literals, which belonged
        // to no product and which the officer had to notice were wrong.
        assertEquals("48000.0", vm.stateFlow.value.number(GroupLoanField.PRINCIPAL))
        assertEquals("24", vm.stateFlow.value.number(GroupLoanField.NUMBER_OF_REPAYMENTS))
        assertEquals("1.75", vm.stateFlow.value.number(GroupLoanField.NOMINAL_INTEREST))
        assertEquals("1", vm.stateFlow.value.number(GroupLoanField.REPAID_EVERY))
        assertEquals("24", vm.stateFlow.value.number(GroupLoanField.LOAN_TERM))
    }

    @Test
    fun `changing the product re-asks the template and re-prefills`() = runTest(dispatcher) {
        val repo = FakeLoanRepository(
            loanProducts = MutableStateFlow(ScreenState.Content(products)),
            groupLoanTemplate = { productId -> template(principal = if (productId == 11) 48_000.0 else 5_000.0) },
        )
        val vm = viewModel(repo = repo)

        vm.trySendAction(GroupLoanApplicationAction.OnProductSelected(LoanProductOption(11, "Group Grameen")))
        advanceUntilIdle()
        vm.trySendAction(GroupLoanApplicationAction.OnProductSelected(LoanProductOption(22, "Group Emergency")))
        advanceUntilIdle()

        assertEquals("5000.0", vm.stateFlow.value.number(GroupLoanField.PRINCIPAL))
        // Keyed on the GROUP too — the template is per (group, product), and the original passed a
        // groupId it had defaulted to 0.
        assertEquals(listOf(31 to 11, 31 to 22), repo.groupLoanTemplateQueries)
    }

    @Test
    fun `selections are dropped when the product changes`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()
        assertEquals(4, vm.stateFlow.value.selections.size)

        vm.trySendAction(GroupLoanApplicationAction.OnProductSelected(LoanProductOption(22, "Group Emergency")))
        advanceUntilIdle()

        // The options themselves came from the PREVIOUS product's template, so keeping the choices
        // would submit ids the new product may not offer.
        assertEquals(emptyMap(), vm.stateFlow.value.selections)
    }

    @Test
    fun `a failed template read is recoverable`() = runTest(dispatcher) {
        var fail = true
        val vm = viewModel(
            repo = FakeLoanRepository(
                loanProducts = MutableStateFlow(ScreenState.Content(products)),
                groupLoanTemplate = { if (fail) null else template() },
            ),
        )

        vm.trySendAction(GroupLoanApplicationAction.OnProductSelected(LoanProductOption(11, "Group Grameen")))
        advanceUntilIdle()
        assertTrue(vm.stateFlow.value.templateError)
        assertNull(vm.stateFlow.value.template)

        fail = false
        vm.trySendAction(GroupLoanApplicationAction.OnRetryTemplate)
        advanceUntilIdle()

        assertTrue(!vm.stateFlow.value.templateError)
        assertEquals("48000.0", vm.stateFlow.value.number(GroupLoanField.PRINCIPAL))
    }

    @Test
    fun `submit names every missing field instead of crashing`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)

        // Nothing chosen: no product, so no template, so no prefilled numbers either.
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertEquals(
            // The original called `.toInt()` on the empty strings and crashed on the first one.
            setOf(GroupLoanField.PRODUCT) +
                GroupLoanField.numeric +
                GroupLoanField.requiredSelections,
            vm.stateFlow.value.errors,
        )
        assertTrue(commands.groupLoanApplications.isEmpty())
    }

    @Test
    fun `a non-numeric principal is an error on that field only`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()

        vm.trySendAction(GroupLoanApplicationAction.OnNumberChanged(GroupLoanField.PRINCIPAL, "forty"))
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertEquals(setOf(GroupLoanField.PRINCIPAL), vm.stateFlow.value.errors)
        assertTrue(commands.groupLoanApplications.isEmpty())
    }

    @Test
    fun `zero is not an application`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()

        vm.trySendAction(GroupLoanApplicationAction.OnNumberChanged(GroupLoanField.PRINCIPAL, "0"))
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertEquals(setOf(GroupLoanField.PRINCIPAL), vm.stateFlow.value.errors)
    }

    @Test
    fun `the payload is a GROUP loan with paired date and format`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()

        vm.trySendAction(GroupLoanApplicationAction.OnDateChanged(GroupLoanDate.SUBMITTED_ON, 1_761_000_000_000))
        vm.trySendAction(
            GroupLoanApplicationAction.OnDateChanged(GroupLoanDate.EXPECTED_DISBURSEMENT, 1_763_000_000_000),
        )
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val payload = commands.groupLoanApplications.single()

        // `"group"`. The original hardcoded `"individual"` on a group loan screen.
        assertEquals("group", payload.loanType)
        assertEquals(31, payload.groupId)
        assertEquals(11, payload.productId)

        // Both dates formatted by the helper that OWNS the two format fields. The original used
        // `DateHelper.getDateAsStringFromLong` (dd-MM-yyyy) while declaring
        // `ApiDateFormatter.DATE_FORMAT` — the 5th occurrence of that pairing defect.
        assertEquals(ApiDateFormatter.formatForApi(1_761_000_000_000), payload.submittedOnDate)
        assertEquals(ApiDateFormatter.formatForApi(1_763_000_000_000), payload.expectedDisbursementDate)
        assertEquals(ApiDateFormatter.DATE_FORMAT, payload.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, payload.locale)
    }

    @Test
    fun `repayment frequency comes from its own dropdown, not the loan term's`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val payload = commands.groupLoanApplications.single()
        // Months for the term, Weeks for the repayment — two DIFFERENT ids. The original fed both
        // payload fields from the loan term's dropdown, so these could never disagree and a weekly
        // repayment on a monthly term was unexpressible.
        assertEquals(2, payload.loanTermFrequencyType)
        assertEquals(1, payload.repaymentFrequencyType)
    }

    @Test
    fun `the two fields with no control are omitted, not filled from a neighbour`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val payload = commands.groupLoanApplications.single()
        // The original filled BOTH from the day-of-week dropdown, so the nth-day field received a
        // day-of-week value. This form has no control for either, so neither is sent.
        assertNull(payload.repaymentFrequencyDayOfWeekType)
        assertNull(payload.repaymentFrequencyNthDayType)
    }

    @Test
    fun `an unchosen optional dropdown is omitted rather than sent as zero`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val payload = commands.groupLoanApplications.single()
        // `0` is an id that belongs to no row. The original sent it for each unchosen optional.
        assertNull(payload.loanPurposeId)
        assertNull(payload.transactionProcessingStrategyId)
        assertNull(payload.interestCalculationPeriodType)
    }

    @Test
    fun `success surfaces the new loan id`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val dialog = assertIs<GroupLoanApplicationState.DialogState.Created>(vm.stateFlow.value.dialogState)
        assertEquals(7_700, dialog.loanId)
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }

    @Test
    fun `offline is reported as offline, not as a failure`() = runTest(dispatcher) {
        val vm = viewModel(
            commands = FakeLoanCommandRepository(
                groupLoanResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
            ),
        )
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val dialog = assertIs<GroupLoanApplicationState.DialogState.Failed>(vm.stateFlow.value.dialogState)
        assertTrue(dialog.msg.contains("connection"), dialog.msg)
    }

    @Test
    fun `dismissing a failure keeps every value`() = runTest(dispatcher) {
        val vm = viewModel(
            commands = FakeLoanCommandRepository(
                groupLoanResult = { MutationResult.Failed(IllegalStateException("rejected"), rolledBack = true) },
            ),
        )
        chooseProductAndRequiredOptions(vm)
        advanceUntilIdle()
        vm.trySendAction(GroupLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertIs<GroupLoanApplicationState.DialogState.Failed>(vm.stateFlow.value.dialogState)

        vm.trySendAction(GroupLoanApplicationAction.CloseDialog)
        advanceUntilIdle()

        // A rejected application is a long form to retype.
        assertNull(vm.stateFlow.value.dialogState)
        assertEquals("48000.0", vm.stateFlow.value.number(GroupLoanField.PRINCIPAL))
    }
}
