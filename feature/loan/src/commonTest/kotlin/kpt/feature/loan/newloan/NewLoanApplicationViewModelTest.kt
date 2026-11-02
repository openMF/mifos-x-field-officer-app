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

package kpt.feature.loan.newloan

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
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.data.testing.FakeFormDraftRepository
import kpt.core.data.testing.FakeLoanCommandRepository
import kpt.core.data.testing.FakeLoanRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.loan.LoanTemplate
import kpt.core.model.objects.account.loan.Currency
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.organisations.LoanProducts
import kpt.core.model.objects.template.loan.AmortizationType
import kpt.core.model.objects.template.loan.AmortizationTypeOptions
import kpt.core.model.objects.template.loan.InterestCalculationPeriodType
import kpt.core.model.objects.template.loan.InterestType
import kpt.core.model.objects.template.loan.InterestTypeOptions
import kpt.core.model.objects.template.loan.RepaymentFrequencyType
import kpt.core.model.objects.template.loan.RepaymentFrequencyTypeOptions
import kpt.core.model.objects.template.loan.TermFrequencyTypeOptions
import kpt.core.model.objects.template.loan.TermPeriodFrequencyType
import kpt.core.model.objects.template.loan.TransactionProcessingStrategyOptions
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The original walked from an empty form to submit with no validation, then crashed in the payload
 * builder on six unguarded `options[-1]` reads and two unguarded `toDouble()` calls. Every one of
 * those is a test here, because every one was reachable without the officer doing anything unusual.
 */
class NewLoanApplicationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val products = listOf(
        LoanProducts(id = 11, name = "Individual Grameen"),
        LoanProducts(id = 22, name = "Individual Emergency"),
        // Unsendable: no id. Must not be offered.
        LoanProducts(id = null, name = "Draft product"),
    )

    /**
     * A template whose own chosen values ARE in its option lists, so the prefill resolves them.
     *
     * The fixture deliberately gives the loan TERM and the REPAYMENT frequency different ids
     * (2 = Months, 1 = Weeks), which is what lets a test prove they reach different payload fields.
     */
    private fun template(principal: Double = 36_000.0) = LoanTemplate(
        clientId = 77,
        principal = principal,
        numberOfRepayments = 12,
        repaymentEvery = 1,
        interestRatePerPeriod = 2.5,
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        interestCalculationPeriodTypeOptions = listOf(InterestCalculationPeriodType(id = 1, value = "Daily")),
        transactionProcessingStrategyOptions = listOf(
            TransactionProcessingStrategyOptions(
                id = 4,
                code = "mifos-standard-strategy",
                name = "Penalties, Fees, Interest, Principal order",
            ),
        ),
    )

    private fun viewModel(
        repo: FakeLoanRepository = FakeLoanRepository(
            loanProducts = MutableStateFlow(ScreenState.Content(products)),
            individualLoanTemplate = { template() },
        ),
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        drafts: FakeFormDraftRepository = FakeFormDraftRepository(),
    ) = NewLoanApplicationViewModel(
        repository = repo,
        commands = commands,
        savedStateHandle = SavedStateHandle(mapOf("clientId" to 77)),
        drafts = drafts,
    )

    /** Choose a product and let its template land — the only order the screen permits. */
    private fun TestScope.chooseProduct(vm: NewLoanApplicationViewModel, id: Int = 11) {
        vm.trySendAction(
            NewLoanApplicationAction.OnProductSelected(LoanProductOption(id, "Individual Grameen")),
        )
        advanceUntilIdle()
    }

    @Test
    fun `a product with no id is not offered`() = runTest(dispatcher) {
        viewModel().products.test {
            assertEquals(ScreenState.Loading, awaitItem())
            val offered = assertIs<ScreenState.Content<List<LoanProductOption>>>(awaitItem()).data
            assertEquals(listOf(11, 22), offered.map { it.id })
        }
    }

    @Test
    fun `next does nothing until a product is chosen`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()

        // The original's `moveToNextStep()` incremented the step with no checks at all, so the
        // wizard walked to submit on an empty form.
        assertEquals(LoanStep.DETAILS, vm.stateFlow.value.step)
        assertEquals(setOf(LoanField.PRODUCT), vm.stateFlow.value.errors)
    }

    @Test
    fun `choosing a product prefills the terms AND the six type fields from ITS template`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)
        val state = vm.stateFlow.value

        assertEquals("36000.0", state.number(LoanField.PRINCIPAL))
        assertEquals("12", state.number(LoanField.NUMBER_OF_REPAYMENTS))
        assertEquals("1", state.number(LoanField.REPAID_EVERY))
        assertEquals("2.5", state.number(LoanField.NOMINAL_INTEREST))

        // Resolved by VALUE match, so each is a real option id — never `indexOfFirst`'s -1.
        assertEquals(2, state.selectionId(LoanField.TERM_FREQUENCY_TYPE))
        assertEquals(1, state.selectionId(LoanField.REPAYMENT_FREQUENCY_TYPE))
        assertEquals(1, state.selectionId(LoanField.AMORTIZATION))
        assertEquals(0, state.selectionId(LoanField.INTEREST_METHOD))
        assertEquals(1, state.selectionId(LoanField.INTEREST_CALCULATION_PERIOD))
        assertEquals(4, state.selectionId(LoanField.REPAYMENT_STRATEGY))
    }

    @Test
    fun `a template value absent from its own option list leaves the field unchosen`() = runTest(dispatcher) {
        val vm = viewModel(
            repo = FakeLoanRepository(
                loanProducts = MutableStateFlow(ScreenState.Content(products)),
                individualLoanTemplate = {
                    // The chosen amortization is NOT among the options — a real Fineract response
                    // shape, and the case where `indexOfFirst` returned -1 and the original stored
                    // it as a position.
                    template().copy(amortizationTypeOptions = emptyList())
                },
            ),
        )
        chooseProduct(vm)

        assertNull(vm.stateFlow.value.selectionId(LoanField.AMORTIZATION))
        // And it is then reported as missing rather than crashing on submit.
        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()
        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()
        assertTrue(LoanField.AMORTIZATION in vm.stateFlow.value.errors)
    }

    @Test
    fun `changing the product clears the whole selection map, not six of twelve`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)

        // A reference field the template prefill never touches — exactly the kind the original
        // left stale across a product change.
        vm.trySendAction(
            NewLoanApplicationAction.OnOptionSelected(LoanField.FUND, LoanOption(99, "Donor fund")),
        )
        advanceUntilIdle()
        assertEquals(99, vm.stateFlow.value.selectionId(LoanField.FUND))

        chooseProduct(vm, id = 22)

        // Gone. Its option list came from the previous product's template, so position-or-id, the
        // old value is not valid against the new product.
        assertNull(vm.stateFlow.value.selectionId(LoanField.FUND))
    }

    @Test
    fun `the template is asked for per client AND product`() = runTest(dispatcher) {
        val repo = FakeLoanRepository(
            loanProducts = MutableStateFlow(ScreenState.Content(products)),
            individualLoanTemplate = { template() },
        )
        val vm = viewModel(repo = repo)

        chooseProduct(vm, id = 11)
        chooseProduct(vm, id = 22)

        // Keyed on the CLIENT too — a template is only valid for one (client, product) pair.
        assertEquals(listOf(77 to 11, 77 to 22), repo.loanTemplateQueries)
    }

    @Test
    fun `terms names every missing field instead of crashing`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(
            repo = FakeLoanRepository(
                loanProducts = MutableStateFlow(ScreenState.Content(products)),
                // A template with NO prefill and NO options — every type field unchosen, which is
                // the state the original's six unguarded `options[-1]` reads crashed on.
                individualLoanTemplate = { LoanTemplate(clientId = 77) },
            ),
            commands = commands,
        )
        chooseProduct(vm)

        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()
        assertEquals(LoanStep.TERMS, vm.stateFlow.value.step)

        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()

        assertEquals(LoanStep.TERMS, vm.stateFlow.value.step)
        assertEquals(
            (
                LoanField.requiredPositive + LoanField.requiredNonNegative +
                    LoanStep.REQUIRED_TERM_SELECTIONS
                ).toSet(),
            vm.stateFlow.value.errors,
        )
        assertTrue(commands.schedulePreviews.isEmpty())
    }

    @Test
    fun `a non-numeric principal is an error on that field only`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)
        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()

        vm.trySendAction(NewLoanApplicationAction.OnNumberChanged(LoanField.PRINCIPAL, "thirty"))
        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()

        // The original called `.toDouble()` on this string in the submit builder.
        assertEquals(setOf(LoanField.PRINCIPAL), vm.stateFlow.value.errors)
        assertEquals(LoanStep.TERMS, vm.stateFlow.value.step)
    }

    @Test
    fun `a zero interest rate is allowed but a zero principal is not`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)
        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()

        // A 0% product is legitimate; a zero principal is not an application.
        vm.trySendAction(NewLoanApplicationAction.OnNumberChanged(LoanField.NOMINAL_INTEREST, "0"))
        vm.trySendAction(NewLoanApplicationAction.OnNumberChanged(LoanField.PRINCIPAL, "0"))
        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()

        assertEquals(setOf(LoanField.PRINCIPAL), vm.stateFlow.value.errors)
    }

    @Test
    fun `strategies with no id still prefill and let Terms advance`() = runTest(dispatcher) {
        // Current Fineract sends the strategies as {code, name} only (gateway, 2026-10-26). Requiring
        // an id dropped them all, so the required field was never filled and Next did nothing.
        val idLess = template().copy(
            transactionProcessingStrategyOptions = listOf(
                TransactionProcessingStrategyOptions(code = "creocore-strategy", name = "Creocore Unique"),
                TransactionProcessingStrategyOptions(
                    code = "mifos-standard-strategy",
                    name = "Penalties, Fees, Interest, Principal order",
                ),
            ),
        )
        val vm = viewModel(
            repo = FakeLoanRepository(
                loanProducts = MutableStateFlow(ScreenState.Content(products)),
                individualLoanTemplate = { idLess },
            ),
        )
        chooseProduct(vm)

        assertEquals("mifos-standard-strategy", vm.stateFlow.value.selectionCode(LoanField.REPAYMENT_STRATEGY))
        advanceSteps(vm, 2)
        assertEquals(LoanStep.SCHEDULE, vm.stateFlow.value.step)
    }

    @Test
    fun `reaching the schedule step asks the server to calculate it`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(
            scheduleResult = {
                MutationResult.Applied(
                    RepaymentSchedule(
                        currency = Currency(code = "USD", decimalPlaces = 2),
                        totalRepaymentExpected = 40_000.0,
                    ),
                    synced = true,
                )
            },
        )
        val vm = viewModel(commands = commands)
        chooseProduct(vm)
        advanceSteps(vm, 2)

        assertEquals(LoanStep.SCHEDULE, vm.stateFlow.value.step)
        assertEquals(1, commands.schedulePreviews.size)
        assertEquals(40_000.0, vm.stateFlow.value.schedule?.totalRepaymentExpected)
    }

    @Test
    fun `editing a term invalidates a schedule already calculated`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)
        advanceSteps(vm, 2)
        assertTrue(vm.stateFlow.value.schedule != null)

        vm.trySendAction(NewLoanApplicationAction.OnBack)
        vm.trySendAction(NewLoanApplicationAction.OnNumberChanged(LoanField.PRINCIPAL, "99000"))
        advanceUntilIdle()

        // The original kept showing the schedule for the terms you had BEFORE the edit.
        assertNull(vm.stateFlow.value.schedule)
    }

    @Test
    fun `a reference field does NOT invalidate the schedule`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)
        advanceSteps(vm, 2)

        // The fund has no bearing on what the schedule computes, so recalculating would be a
        // wasted round trip on a screen an officer fills in the field.
        vm.trySendAction(
            NewLoanApplicationAction.OnOptionSelected(LoanField.FUND, LoanOption(99, "Donor fund")),
        )
        advanceUntilIdle()

        assertTrue(vm.stateFlow.value.schedule != null)
    }

    @Test
    fun `a server-rejected schedule keeps the form and carries the reason`() = runTest(dispatcher) {
        val vm = viewModel(
            commands = FakeLoanCommandRepository(
                scheduleResult = {
                    MutationResult.Failed(
                        fineractRejection("repayment every must be greater than zero"),
                        rolledBack = false,
                    )
                },
            ),
        )
        chooseProduct(vm)
        advanceSteps(vm, 2)

        val error = assertIs<ScheduleError.Rejected>(vm.stateFlow.value.scheduleError)
        assertEquals("repayment every must be greater than zero", error.msg)
        // The form survives. The original replaced the whole wizard with an error page.
        assertEquals("36000.0", vm.stateFlow.value.number(LoanField.PRINCIPAL))
    }

    @Test
    fun `an offline schedule is reported as offline, not as a rejection`() = runTest(dispatcher) {
        val vm = viewModel(
            commands = FakeLoanCommandRepository(
                scheduleResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
            ),
        )
        chooseProduct(vm)
        advanceSteps(vm, 2)

        assertEquals(ScheduleError.Offline, vm.stateFlow.value.scheduleError)
    }

    @Test
    fun `back walks a step and stops at the first`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)
        advanceSteps(vm, 1)
        assertEquals(LoanStep.TERMS, vm.stateFlow.value.step)

        vm.trySendAction(NewLoanApplicationAction.OnBack)
        advanceUntilIdle()
        assertEquals(LoanStep.DETAILS, vm.stateFlow.value.step)

        // From the first step, Back LEAVES — it does not compute step -1, which is what the
        // original's unguarded `currentStep - 1` did.
        vm.eventFlow.test {
            vm.trySendAction(NewLoanApplicationAction.OnBack)
            advanceUntilIdle()
            assertEquals(NewLoanApplicationEvent.NavigateBack, awaitItem())
        }
        assertEquals(LoanStep.DETAILS, vm.stateFlow.value.step)
    }

    @Test
    fun `the payload is an INDIVIDUAL loan with paired date and format`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProduct(vm)

        vm.trySendAction(
            NewLoanApplicationAction.OnDateChanged(LoanDate.SUBMITTED_ON, 1_761_000_000_000),
        )
        vm.trySendAction(
            NewLoanApplicationAction.OnDateChanged(LoanDate.EXPECTED_DISBURSEMENT, 1_763_000_000_000),
        )
        advanceSteps(vm, 3)
        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.loanApplications.single()
        assertEquals("individual", sent.loanType)
        assertEquals(77, sent.clientId)
        assertEquals(11, sent.productId)
        assertEquals(ApiDateFormatter.formatForApi(1_761_000_000_000), sent.submittedOnDate)
        assertEquals(ApiDateFormatter.formatForApi(1_763_000_000_000), sent.expectedDisbursementDate)
        assertEquals(ApiDateFormatter.DATE_FORMAT, sent.dateFormat)
        // Not the original's hardcoded "en".
        assertEquals(ApiDateFormatter.LOCALE, sent.locale)
    }

    @Test
    fun `repayment frequency comes from its own dropdown, not the loan term's`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProduct(vm)
        advanceSteps(vm, 3)
        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.loanApplications.single()
        // Months for the term, Weeks for the repayment. The original fed BOTH payload fields from
        // the term dropdown, so they could never disagree.
        assertEquals(2, sent.loanTermFrequencyType)
        assertEquals(1, sent.repaymentFrequencyType)
    }

    @Test
    fun `the repayment strategy travels as a CODE, not an id`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProduct(vm)
        advanceSteps(vm, 3)
        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        // The one option Fineract identifies by code on this endpoint — the id (4) would be wrong.
        assertEquals("mifos-standard-strategy", commands.loanApplications.single().transactionProcessingStrategyCode)
    }

    @Test
    fun `unchosen optional fields are omitted rather than sent as zero`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProduct(vm)
        advanceSteps(vm, 3)
        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.loanApplications.single()
        assertNull(sent.loanPurposeId)
        assertNull(sent.fundId)
        assertNull(sent.loanOfficerId)
        assertNull(sent.linkAccountId)
        // The fixture template offers no day-of-week or nth-day options, so neither is sent.
        assertNull(sent.repaymentFrequencyDayOfWeekType)
        assertNull(sent.repaymentFrequencyNthDayType)
        // Blank external id omitted, not sent as "".
        assertNull(sent.externalId)
    }

    @Test
    fun `the term length is the repayments times their interval`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProduct(vm)
        vm.trySendAction(NewLoanApplicationAction.OnNext)
        advanceUntilIdle()
        vm.trySendAction(NewLoanApplicationAction.OnNumberChanged(LoanField.NUMBER_OF_REPAYMENTS, "6"))
        vm.trySendAction(NewLoanApplicationAction.OnNumberChanged(LoanField.REPAID_EVERY, "2"))
        advanceSteps(vm, 2)
        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        // The original's own derivation, kept.
        assertEquals(12, commands.loanApplications.single().loanTermFrequency)
    }

    @Test
    fun `submit re-validates every step, not only the last`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository()
        val vm = viewModel(commands = commands)
        chooseProduct(vm)
        advanceSteps(vm, 3)
        assertEquals(LoanStep.PREVIEW, vm.stateFlow.value.step)

        // Walk back and empty a required field, then forward again — reachable, and the original
        // validated nothing at any point.
        vm.trySendAction(NewLoanApplicationAction.OnBack)
        vm.trySendAction(NewLoanApplicationAction.OnBack)
        vm.trySendAction(NewLoanApplicationAction.OnNumberChanged(LoanField.PRINCIPAL, ""))
        advanceUntilIdle()
        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertTrue(commands.loanApplications.isEmpty())
        assertEquals(setOf(LoanField.PRINCIPAL), vm.stateFlow.value.errors)
        // And it LANDS on the step that owns the field, so the error is visible.
        assertEquals(LoanStep.TERMS, vm.stateFlow.value.step)
    }

    @Test
    fun `a double submit sends one application`() = runTest(dispatcher) {
        val commands = FakeLoanCommandRepository(gate = kotlinx.coroutines.CompletableDeferred())
        val vm = viewModel(commands = commands)
        chooseProduct(vm)
        advanceSteps(vm, 3)

        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()
        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertEquals(1, commands.loanApplications.size)
    }

    @Test
    fun `offline is reported as offline, not as a failure`() = runTest(dispatcher) {
        val vm = viewModel(
            commands = FakeLoanCommandRepository(
                createLoanResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
            ),
        )
        chooseProduct(vm)
        advanceSteps(vm, 3)
        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val dialog = assertIs<NewLoanApplicationState.DialogState.Failed>(vm.stateFlow.value.dialogState)
        assertTrue(dialog.msg.contains("connection"), dialog.msg)
    }

    @Test
    fun `dismissing a failure keeps four pages of values`() = runTest(dispatcher) {
        val vm = viewModel(
            commands = FakeLoanCommandRepository(
                createLoanResult = {
                    MutationResult.Failed(IllegalStateException("rejected"), rolledBack = true)
                },
            ),
        )
        chooseProduct(vm)
        vm.trySendAction(NewLoanApplicationAction.OnExternalIdChanged("EXT-12"))
        advanceSteps(vm, 3)
        vm.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertIs<NewLoanApplicationState.DialogState.Failed>(vm.stateFlow.value.dialogState)
        vm.trySendAction(NewLoanApplicationAction.CloseDialog)
        advanceUntilIdle()

        assertNull(vm.stateFlow.value.dialogState)
        assertEquals("EXT-12", vm.stateFlow.value.externalId)
        assertEquals("36000.0", vm.stateFlow.value.number(LoanField.PRINCIPAL))
        assertEquals(LoanStep.PREVIEW, vm.stateFlow.value.step)
    }

    @Test
    fun `a failed template read is recoverable`() = runTest(dispatcher) {
        var fail = true
        val vm = viewModel(
            repo = FakeLoanRepository(
                loanProducts = MutableStateFlow(ScreenState.Content(products)),
                individualLoanTemplate = { if (fail) null else template() },
            ),
        )
        chooseProduct(vm)
        assertTrue(vm.stateFlow.value.templateError)
        assertNull(vm.stateFlow.value.template)

        fail = false
        vm.trySendAction(NewLoanApplicationAction.OnRetryTemplate)
        advanceUntilIdle()

        assertTrue(!vm.stateFlow.value.templateError)
        assertEquals("36000.0", vm.stateFlow.value.number(LoanField.PRINCIPAL))
    }

    /** Tap Next [times] times, letting each step settle. */
    private fun TestScope.advanceSteps(vm: NewLoanApplicationViewModel, times: Int) {
        repeat(times) {
            vm.trySendAction(NewLoanApplicationAction.OnNext)
            advanceUntilIdle()
        }
    }

    // ---- drafts ----

    @Test
    fun `an offline application is kept and resuming restores input over template defaults`() = runTest(dispatcher) {
        val drafts = FakeFormDraftRepository()
        val offline = viewModel(
            commands = FakeLoanCommandRepository(createLoanResult = { MutationResult.Blocked(BlockReason.OFFLINE) }),
            drafts = drafts,
        )
        chooseProduct(offline)
        offline.trySendAction(NewLoanApplicationAction.OnNumberChanged(LoanField.PRINCIPAL, "12345"))
        advanceSteps(offline, 3)
        offline.trySendAction(NewLoanApplicationAction.OnSubmit)
        advanceUntilIdle()

        val kept = drafts.saved.value.single()
        assertEquals(SubmitOutboxStatus.PENDING, kept.status)
        assertEquals("Individual Grameen", kept.draft.title)

        val reopened = viewModel(drafts = drafts)
        advanceUntilIdle()
        reopened.trySendAction(NewLoanApplicationAction.ResumeDraft)
        advanceUntilIdle()

        // The template's default principal is 36,000; the officer had typed 12,345.
        assertEquals("12345", reopened.stateFlow.value.numbers[LoanField.PRINCIPAL])
        assertEquals(11, reopened.stateFlow.value.selections[LoanField.PRODUCT]?.id)
        assertTrue(reopened.stateFlow.value.template != null, "the template is fetched again on resume")
    }
}
