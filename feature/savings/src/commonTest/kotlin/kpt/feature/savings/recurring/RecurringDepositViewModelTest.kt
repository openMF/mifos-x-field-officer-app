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

package kpt.feature.savings.recurring

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
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
import kpt.core.data.testing.FakeRecurringDepositCommandRepository
import kpt.core.data.testing.FakeRecurringDepositRepository
import kpt.core.model.objects.template.recurring.FieldOfficerOption
import kpt.core.model.objects.template.recurring.charge.ChargeOption
import kpt.core.model.objects.template.recurring.interest.InterestCalculationDaysInYearTypeOption
import kpt.core.model.objects.template.recurring.interest.InterestCalculationTypeOption
import kpt.core.model.objects.template.recurring.interest.InterestCompoundingPeriodTypeOption
import kpt.core.model.objects.template.recurring.interest.InterestPostingPeriodTypeOption
import kpt.core.model.objects.template.recurring.interest.PreClosurePenalInterestOnTypeOption
import kpt.core.model.objects.template.recurring.period.LockinPeriodFrequencyTypeOption
import kpt.core.model.objects.template.recurring.period.PeriodFrequencyTypeOption
import kpt.core.model.objects.template.recurring.period.ProductOption
import kpt.core.model.recurringdeposit.RecurringDepositAccountTemplate
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The payload is this wizard's risk surface: the original read the product through an unguarded
 * `get(-1)`, and sent pre-closure penalty fields even with the checkbox clear.
 */
class RecurringDepositViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun template() = RecurringDepositAccountTemplate(
        productOptions = listOf(
            ProductOption(id = 4, name = "Monthly RD"),
            // Unsendable: no id. Must not be offered.
            ProductOption(id = null, name = "Draft RD"),
        ),
        fieldOfficerOptions = listOf(FieldOfficerOption(id = 9, displayName = "Officer A")),
        periodFrequencyTypeOptions = listOf(
            PeriodFrequencyTypeOption(id = 2, value = "Months"),
            PeriodFrequencyTypeOption(id = 1, value = "Weeks"),
        ),
        lockinPeriodFrequencyTypeOptions = listOf(LockinPeriodFrequencyTypeOption(id = 2, value = "Months")),
        interestCompoundingPeriodTypeOptions = listOf(InterestCompoundingPeriodTypeOption(id = 1, value = "Daily")),
        interestPostingPeriodTypeOptions = listOf(InterestPostingPeriodTypeOption(id = 4, value = "Monthly")),
        interestCalculationTypeOptions = listOf(InterestCalculationTypeOption(id = 1, value = "Daily Balance")),
        interestCalculationDaysInYearTypeOptions = listOf(
            InterestCalculationDaysInYearTypeOption(id = 365, value = "365 Days"),
        ),
        preClosurePenalInterestOnTypeOptions = listOf(
            PreClosurePenalInterestOnTypeOption(id = 1, value = "Whole term"),
        ),
        chargeOptions = listOf(ChargeOption(id = 11, name = "Account maintenance")),
    )

    private fun viewModel(
        commands: FakeRecurringDepositCommandRepository = FakeRecurringDepositCommandRepository(),
        repo: FakeRecurringDepositRepository = FakeRecurringDepositRepository(
            template = MutableStateFlow(ScreenState.Content(template())),
        ),
        clientId: Int = 55,
        drafts: kpt.core.data.testing.FakeFormDraftRepository = kpt.core.data.testing.FakeFormDraftRepository(),
    ) = RecurringDepositViewModel(
        repository = repo,
        commands = commands,
        savedStateHandle = SavedStateHandle(mapOf("clientId" to clientId)),
        drafts = drafts,
    )

    /** Fill in everything the five steps require. */
    private fun TestScope.fillRequired(vm: RecurringDepositViewModel) {
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(RecurringField.PRODUCT, RecurringOption(4, "Monthly RD")),
        )
        vm.trySendAction(RecurringDepositAction.OnNumberChanged(RecurringField.DEPOSIT_AMOUNT, "500"))
        vm.trySendAction(RecurringDepositAction.OnNumberChanged(RecurringField.RECURRING_FREQUENCY, "1"))
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(
                RecurringField.RECURRING_FREQUENCY_TYPE,
                RecurringOption(2, "Months"),
            ),
        )
        vm.trySendAction(RecurringDepositAction.OnNumberChanged(RecurringField.DEPOSIT_PERIOD, "12"))
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(RecurringField.DEPOSIT_PERIOD_TYPE, RecurringOption(1, "Weeks")),
        )
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(RecurringField.COMPOUNDING_PERIOD, RecurringOption(1, "Daily")),
        )
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(RecurringField.POSTING_PERIOD, RecurringOption(4, "Monthly")),
        )
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(
                RecurringField.CALCULATION_TYPE,
                RecurringOption(1, "Daily Balance"),
            ),
        )
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(RecurringField.DAYS_IN_YEAR, RecurringOption(365, "365 Days")),
        )
        advanceUntilIdle()
    }

    private fun TestScope.advanceToLast(vm: RecurringDepositViewModel) {
        repeat(RecurringStep.entries.size - 1) {
            vm.trySendAction(RecurringDepositAction.OnNext)
            advanceUntilIdle()
        }
    }

    // ---- the product ------------------------------------------------------------------------

    @Test
    fun `submitting with no product chosen reports it instead of crashing`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()

        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        // The original read `productOptions?.get(loanProductSelected)` with a `-1` default, so
        // `get(-1)` threw here.
        assertTrue(RecurringField.PRODUCT in vm.stateFlow.value.errors)
        assertEquals(RecurringStep.DETAILS, vm.stateFlow.value.step)
        assertTrue(commands.applications.isEmpty())
    }

    @Test
    fun `a product with no id is not offered`() = runTest(dispatcher) {
        viewModel().template.test {
            assertEquals(ScreenState.Loading, awaitItem())
            val form = assertIs<ScreenState.Content<RecurringDepositForm>>(awaitItem()).data
            assertEquals(listOf(4), form.products.map { it.id })
        }
    }

    @Test
    fun `the template is asked for THIS client`() = runTest(dispatcher) {
        val repo = FakeRecurringDepositRepository(
            template = MutableStateFlow(ScreenState.Content(template())),
        )
        viewModel(repo = repo, clientId = 77)
        advanceUntilIdle()
        assertEquals(listOf(77), repo.requestedClients)
    }

    // ---- the payload -----------------------------------------------------------------------

    @Test
    fun `the payload carries the product as a value and a paired date`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)

        vm.trySendAction(RecurringDepositAction.OnDateChanged(1_761_000_000_000))
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.applications.single()
        assertEquals(55, sent.clientId)
        assertEquals(4, sent.productId)
        assertEquals(ApiDateFormatter.formatForApi(1_761_000_000_000), sent.submittedOnDate)
        assertEquals(ApiDateFormatter.DATE_FORMAT, sent.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, sent.locale)
    }

    @Test
    fun `the deposit cadence and the term carry their own separate units`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.applications.single()
        // Months for the cadence, Weeks for the term — two DIFFERENT ids from one option list. The
        // original held them in two separate nested state objects, so one could carry an index
        // belonging to the other's selection.
        assertEquals(2, sent.recurringFrequencyType)
        assertEquals(1, sent.depositPeriodFrequencyId)
        assertEquals(1, sent.recurringFrequency)
        assertEquals(12, sent.depositPeriod)
        assertEquals(500.0, sent.mandatoryRecommendedDepositAmount)
    }

    @Test
    fun `penalty fields are NOT sent when the penalty does not apply`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)

        // Typed, then the checkbox left CLEAR. The original sent whatever had been typed.
        vm.trySendAction(RecurringDepositAction.OnNumberChanged(RecurringField.PENALTY_INTEREST, "2.5"))
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(
                RecurringField.PENALTY_INTEREST_ON,
                RecurringOption(1, "Whole term"),
            ),
        )
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.applications.single()
        assertTrue(!sent.preClosurePenalApplicable)
        assertNull(sent.preClosurePenalInterest)
        assertNull(sent.preClosurePenalInterestOnTypeId)
    }

    @Test
    fun `penalty fields ARE sent when the penalty applies`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)

        vm.trySendAction(RecurringDepositAction.OnToggled(RecurringToggle.PRE_CLOSURE_PENALTY, true))
        vm.trySendAction(RecurringDepositAction.OnNumberChanged(RecurringField.PENALTY_INTEREST, "2.5"))
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(
                RecurringField.PENALTY_INTEREST_ON,
                RecurringOption(1, "Whole term"),
            ),
        )
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.applications.single()
        assertTrue(sent.preClosurePenalApplicable)
        assertEquals(2.5, sent.preClosurePenalInterest)
        assertEquals(1, sent.preClosurePenalInterestOnTypeId)
    }

    @Test
    fun `an applicable penalty requires its own fields`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        fillRequired(vm)
        vm.trySendAction(RecurringDepositAction.OnToggled(RecurringToggle.PRE_CLOSURE_PENALTY, true))
        advanceUntilIdle()

        vm.trySendAction(RecurringDepositAction.OnNext)
        advanceUntilIdle()
        vm.trySendAction(RecurringDepositAction.OnNext)
        advanceUntilIdle()
        vm.trySendAction(RecurringDepositAction.OnNext)
        advanceUntilIdle()

        assertEquals(RecurringStep.INTEREST, vm.stateFlow.value.step)
        assertEquals(
            setOf(RecurringField.PENALTY_INTEREST, RecurringField.PENALTY_INTEREST_ON),
            vm.stateFlow.value.errors,
        )
    }

    @Test
    fun `a lockin unit is sent only alongside a lockin number`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)
        vm.trySendAction(
            RecurringDepositAction.OnOptionSelected(RecurringField.LOCKIN_PERIOD_TYPE, RecurringOption(2, "Months")),
        )
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        assertNull(commands.applications.single().lockinPeriodFrequencyType)
    }

    @Test
    fun `unchosen optionals are omitted`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.applications.single()
        assertNull(sent.fieldOfficerId)
        assertNull(sent.externalId)
        assertNull(sent.lockinPeriodFrequency)
    }

    // ---- validation + charges ---------------------------------------------------------------

    @Test
    fun `a zero or negative deposit amount is refused`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        fillRequired(vm)
        vm.trySendAction(RecurringDepositAction.OnNumberChanged(RecurringField.DEPOSIT_AMOUNT, "0"))
        vm.trySendAction(RecurringDepositAction.OnNext)
        advanceUntilIdle()
        vm.trySendAction(RecurringDepositAction.OnNext)
        advanceUntilIdle()

        assertEquals(RecurringStep.TERMS, vm.stateFlow.value.step)
        assertEquals(setOf(RecurringField.DEPOSIT_AMOUNT), vm.stateFlow.value.errors)
    }

    @Test
    fun `charges reach the payload`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)
        vm.trySendAction(
            RecurringDepositAction.OnChargeAdded(RecurringChargeRow(11, "Maintenance", "50")),
        )
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        val charge = commands.applications.single().charges.single()
        assertEquals(11, charge.chargeId)
        assertEquals("50", charge.amount)
    }

    @Test
    fun `a removed charge is not sent`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)
        vm.trySendAction(RecurringDepositAction.OnChargeAdded(RecurringChargeRow(11, "A", "50")))
        vm.trySendAction(RecurringDepositAction.OnChargeAdded(RecurringChargeRow(12, "B", "60")))
        vm.trySendAction(RecurringDepositAction.OnChargeRemoved(0))
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        assertEquals(listOf(12), commands.applications.single().charges.map { it.chargeId })
    }

    // ---- steps + outcomes -------------------------------------------------------------------

    @Test
    fun `submit re-validates every step and lands on the earliest failure`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository()
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)
        advanceToLast(vm)
        assertEquals(RecurringStep.PREVIEW, vm.stateFlow.value.step)

        // Walk back and empty a required field.
        repeat(3) { vm.trySendAction(RecurringDepositAction.OnBack) }
        vm.trySendAction(RecurringDepositAction.OnNumberChanged(RecurringField.DEPOSIT_AMOUNT, ""))
        advanceUntilIdle()
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        assertTrue(commands.applications.isEmpty())
        assertEquals(RecurringStep.TERMS, vm.stateFlow.value.step)
    }

    @Test
    fun `back stops at the first step`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        fillRequired(vm)
        vm.trySendAction(RecurringDepositAction.OnNext)
        advanceUntilIdle()
        assertEquals(RecurringStep.TERMS, vm.stateFlow.value.step)

        vm.trySendAction(RecurringDepositAction.OnBack)
        advanceUntilIdle()
        vm.trySendAction(RecurringDepositAction.OnBack)
        advanceUntilIdle()
        assertEquals(RecurringStep.DETAILS, vm.stateFlow.value.step)
    }

    @Test
    fun `a double submit sends one application`() = runTest(dispatcher) {
        val commands = FakeRecurringDepositCommandRepository(gate = CompletableDeferred())
        val vm = viewModel(commands)
        advanceUntilIdle()
        fillRequired(vm)
        advanceToLast(vm)

        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        assertEquals(1, commands.applications.size)
    }

    @Test
    fun `offline is reported as offline`() = runTest(dispatcher) {
        val vm = viewModel(
            FakeRecurringDepositCommandRepository(
                result = { MutationResult.Blocked(BlockReason.OFFLINE) },
            ),
        )
        advanceUntilIdle()
        fillRequired(vm)
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()

        assertTrue(assertIs<RecurringDialog.Failed>(vm.stateFlow.value.dialogState).offline)
    }

    @Test
    fun `dismissing a failure keeps five pages of values`() = runTest(dispatcher) {
        val vm = viewModel(
            FakeRecurringDepositCommandRepository(
                result = { MutationResult.Failed(IllegalStateException("rejected"), rolledBack = true) },
            ),
        )
        advanceUntilIdle()
        fillRequired(vm)
        vm.trySendAction(RecurringDepositAction.OnExternalIdChanged("EXT-7"))
        vm.trySendAction(RecurringDepositAction.OnChargeAdded(RecurringChargeRow(11, "A", "50")))
        advanceToLast(vm)
        vm.trySendAction(RecurringDepositAction.OnSubmit)
        advanceUntilIdle()
        vm.trySendAction(RecurringDepositAction.CloseDialog)
        advanceUntilIdle()

        assertNull(vm.stateFlow.value.dialogState)
        assertEquals("EXT-7", vm.stateFlow.value.externalId)
        assertEquals("500", vm.stateFlow.value.number(RecurringField.DEPOSIT_AMOUNT))
        assertEquals(1, vm.stateFlow.value.charges.size)
        assertEquals(RecurringStep.PREVIEW, vm.stateFlow.value.step)
    }
}
