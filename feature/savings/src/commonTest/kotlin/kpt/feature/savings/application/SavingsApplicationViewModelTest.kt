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

package kpt.feature.savings.application

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
import kpt.core.data.testing.FakeSavingsAccountRepository
import kpt.core.data.testing.FakeSavingsCommandRepository
import kpt.core.model.objects.commonfiles.InterestType
import kpt.core.model.objects.organisations.ProductSavings
import kpt.core.model.objects.template.client.ChargeOptions
import kpt.core.model.savings.SavingProductsTemplate
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The two defects this port fixes are both about what the SERVER receives: the group path was
 * dropped between the route and the payload, and the submitted date was formatted one way while
 * declaring another.
 */
class SavingsApplicationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val products = listOf(
        ProductSavings(id = 7, name = "Passbook Savings"),
        ProductSavings(id = 8, name = "Youth Savings"),
        // Unsendable: no id. Must not be offered.
        ProductSavings(id = null, name = "Draft product"),
    )

    private fun template() = SavingProductsTemplate(
        interestCompoundingPeriodType = InterestType(id = 1, value = "Daily"),
        interestPostingPeriodType = InterestType(id = 4, value = "Monthly"),
        interestCalculationType = InterestType(id = 1, value = "Daily Balance"),
        interestCalculationDaysInYearType = InterestType(id = 365, value = "365 Days"),
        interestCompoundingPeriodTypeOptions = listOf(InterestType(id = 1, value = "Daily")),
        interestPostingPeriodTypeOptions = listOf(InterestType(id = 4, value = "Monthly")),
        interestCalculationTypeOptions = listOf(InterestType(id = 1, value = "Daily Balance")),
        interestCalculationDaysInYearTypeOptions = listOf(InterestType(id = 365, value = "365 Days")),
        lockinPeriodFrequencyTypeOptions = listOf(InterestType(id = 2, value = "Months")),
        // `template.client.ChargeOptions`, NOT `template.loan` — two different types with the
        // same simple name, and the savings template uses the client one. Same trap as
        // `DateHelper.SHORT_MONTH` vs `DateFormatPattern.SHORT_MONTH`.
        chargeOptions = listOf(ChargeOptions(id = 11, name = "Account maintenance")),
    )

    private fun viewModel(
        commands: FakeSavingsCommandRepository = FakeSavingsCommandRepository(),
        repo: FakeSavingsAccountRepository = FakeSavingsAccountRepository(
            savingsProducts = MutableStateFlow(ScreenState.Content(products)),
            applicationTemplate = { template() },
        ),
        ownerId: Int = 31,
        isGroup: Boolean = false,
        drafts: kpt.core.data.testing.FakeFormDraftRepository = kpt.core.data.testing.FakeFormDraftRepository(),
    ) = SavingsApplicationViewModel(
        repository = repo,
        commands = commands,
        savedStateHandle = SavedStateHandle(mapOf("ownerId" to ownerId, "isGroup" to isGroup)),
        drafts = drafts,
    )

    private fun TestScope.chooseProduct(vm: SavingsApplicationViewModel, id: Int = 7) {
        vm.trySendAction(
            SavingsApplicationAction.OnProductSelected(SavingsProductOption(id, "Passbook Savings")),
        )
        advanceUntilIdle()
    }

    private fun TestScope.advanceSteps(vm: SavingsApplicationViewModel, times: Int) {
        repeat(times) {
            vm.trySendAction(SavingsApplicationAction.OnNext)
            advanceUntilIdle()
        }
    }

    // ---- the group path ---------------------------------------------------------------------

    @Test
    fun `a GROUP application carries the group as its owner`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands, ownerId = 31, isGroup = true)
        chooseProduct(vm)
        advanceSteps(vm, 3)
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.applications.single()
        // The original read only `clientId` from a route carrying `groupId` + `isGroupAccount`, so
        // this path posted a client id and no group at all.
        assertEquals(31, sent.ownerId)
        assertTrue(sent.isGroup)
    }

    @Test
    fun `a CLIENT application carries the client as its owner`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands, ownerId = 55, isGroup = false)
        chooseProduct(vm)
        advanceSteps(vm, 3)
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.applications.single()
        assertEquals(55, sent.ownerId)
        assertTrue(!sent.isGroup)
    }

    @Test
    fun `the group flag selects the group TEMPLATE endpoint`() = runTest(dispatcher) {
        val repo = FakeSavingsAccountRepository(
            savingsProducts = MutableStateFlow(ScreenState.Content(products)),
            applicationTemplate = { template() },
        )
        val vm = viewModel(repo = repo, ownerId = 31, isGroup = true)
        chooseProduct(vm)

        // Fineract has a separate group template endpoint; the original read NEITHER per-owner
        // template, so the form's option lists came from the generic one.
        assertEquals(listOf(Triple(31, 7, true)), repo.applicationTemplateQueries)
    }

    @Test
    fun `a client application reads the client template endpoint`() = runTest(dispatcher) {
        val repo = FakeSavingsAccountRepository(
            savingsProducts = MutableStateFlow(ScreenState.Content(products)),
            applicationTemplate = { template() },
        )
        val vm = viewModel(repo = repo, ownerId = 55, isGroup = false)
        chooseProduct(vm)

        assertEquals(listOf(Triple(55, 7, false)), repo.applicationTemplateQueries)
    }

    // ---- the date -------------------------------------------------------------------------

    @Test
    fun `the submitted date is paired with the format that parses it`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        chooseProduct(vm)

        vm.trySendAction(SavingsApplicationAction.OnDateChanged(1_761_000_000_000))
        advanceSteps(vm, 3)
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.applications.single()
        // The original declared `ApiDateFormatter.DATE_FORMAT` ("dd MMMM yyyy") while sending a
        // date built by `DateHelper.getDateAsStringFromLong` ("dd-MM-yyyy").
        assertEquals(ApiDateFormatter.formatForApi(1_761_000_000_000), sent.submittedOnDate)
        assertEquals(ApiDateFormatter.DATE_FORMAT, sent.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, sent.locale)
    }

    // ---- the form -------------------------------------------------------------------------

    @Test
    fun `a product with no id is not offered`() = runTest(dispatcher) {
        // COLLECTED, not `.value`: `products` is `stateIn(WhileSubscribed)`, so with nothing
        // collecting it `.value` returns the SEED (Loading) rather than the mapped list. Bitten by
        // this four times across the migration — see `while-subscribed-read-off-the-composition-path`.
        viewModel().products.test {
            assertEquals(ScreenState.Loading, awaitItem())
            val offered = assertIs<ScreenState.Content<List<SavingsProductOption>>>(awaitItem()).data
            assertEquals(listOf(7, 8), offered.map { it.id })
        }
    }

    @Test
    fun `next does nothing until a product is chosen`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.trySendAction(SavingsApplicationAction.OnNext)
        advanceUntilIdle()

        assertEquals(SavingsStep.DETAILS, vm.stateFlow.value.step)
        assertEquals(setOf(SavingsField.PRODUCT), vm.stateFlow.value.errors)
    }

    @Test
    fun `the product's own interest options are prefilled`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)

        val s = vm.stateFlow.value
        assertEquals(1, s.selectionId(SavingsField.COMPOUNDING_PERIOD))
        assertEquals(4, s.selectionId(SavingsField.POSTING_PERIOD))
        assertEquals(1, s.selectionId(SavingsField.CALCULATION_TYPE))
        assertEquals(365, s.selectionId(SavingsField.DAYS_IN_YEAR))
    }

    @Test
    fun `changing the product clears the selections and the charges`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm, id = 7)
        vm.trySendAction(
            SavingsApplicationAction.OnChargeAdded(SavingsChargeRow(11, "Maintenance", "50")),
        )
        vm.trySendAction(
            SavingsApplicationAction.OnOptionSelected(
                SavingsField.FIELD_OFFICER,
                SavingsOption(99, "Officer A"),
            ),
        )
        advanceUntilIdle()
        assertEquals(1, vm.stateFlow.value.charges.size)

        chooseProduct(vm, id = 8)

        // Both came from the previous product's template.
        assertEquals(emptyList(), vm.stateFlow.value.charges)
        assertNull(vm.stateFlow.value.selectionId(SavingsField.FIELD_OFFICER))
    }

    @Test
    fun `a blank optional number is accepted and omitted`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        chooseProduct(vm)
        advanceSteps(vm, 3)
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        val sent = commands.applications.single()
        assertNull(sent.minRequiredOpeningBalance)
        assertNull(sent.minRequiredBalance)
        assertNull(sent.lockinPeriodFrequency)
        assertNull(sent.externalId)
    }

    @Test
    fun `a negative optional number is refused on its own field`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)
        advanceSteps(vm, 1)

        vm.trySendAction(
            SavingsApplicationAction.OnNumberChanged(SavingsField.OPENING_BALANCE, "-5"),
        )
        vm.trySendAction(SavingsApplicationAction.OnNext)
        advanceUntilIdle()

        assertEquals(setOf(SavingsField.OPENING_BALANCE), vm.stateFlow.value.errors)
        assertEquals(SavingsStep.TERMS, vm.stateFlow.value.step)
    }

    @Test
    fun `a lockin unit is sent only alongside a lockin number`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        chooseProduct(vm)
        advanceSteps(vm, 1)

        // A unit with no number names nothing. The original's own `takeIf`, kept.
        vm.trySendAction(
            SavingsApplicationAction.OnOptionSelected(
                SavingsField.LOCKIN_PERIOD_TYPE,
                SavingsOption(2, "Months"),
            ),
        )
        advanceSteps(vm, 2)
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertNull(commands.applications.single().lockinPeriodFrequencyType)
    }

    // ---- charges, which DO reach the payload -----------------------------------------------

    @Test
    fun `charges reach the payload`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        chooseProduct(vm)
        vm.trySendAction(
            SavingsApplicationAction.OnChargeAdded(SavingsChargeRow(11, "Maintenance", "50")),
        )
        advanceSteps(vm, 3)
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        // Unlike the LOAN wizard, whose whole charges page was discarded because `LoansPayload`
        // has no charges member, `SavingsPayload` carries them.
        val charge = commands.applications.single().charges.single()
        assertEquals(11, charge.chargeId)
        assertEquals("50", charge.amount)
    }

    @Test
    fun `a removed charge is not sent`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        chooseProduct(vm)
        vm.trySendAction(SavingsApplicationAction.OnChargeAdded(SavingsChargeRow(11, "A", "50")))
        vm.trySendAction(SavingsApplicationAction.OnChargeAdded(SavingsChargeRow(12, "B", "60")))
        vm.trySendAction(SavingsApplicationAction.OnChargeRemoved(0))
        advanceSteps(vm, 3)
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertEquals(listOf(12), commands.applications.single().charges.map { it.chargeId })
    }

    // ---- steps + outcomes -------------------------------------------------------------------

    @Test
    fun `submit re-validates every step and lands on the earliest failure`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository()
        val vm = viewModel(commands)
        chooseProduct(vm)
        advanceSteps(vm, 3)
        assertEquals(SavingsStep.PREVIEW, vm.stateFlow.value.step)

        // Reachable: walk back and put a bad value in.
        vm.trySendAction(SavingsApplicationAction.OnBack)
        vm.trySendAction(SavingsApplicationAction.OnBack)
        vm.trySendAction(
            SavingsApplicationAction.OnNumberChanged(SavingsField.MINIMUM_BALANCE, "abc"),
        )
        advanceUntilIdle()
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertTrue(commands.applications.isEmpty())
        assertEquals(SavingsStep.TERMS, vm.stateFlow.value.step)
    }

    @Test
    fun `back stops at the first step and then leaves`() = runTest(dispatcher) {
        val vm = viewModel()
        chooseProduct(vm)
        advanceSteps(vm, 1)

        vm.trySendAction(SavingsApplicationAction.OnBack)
        advanceUntilIdle()
        assertEquals(SavingsStep.DETAILS, vm.stateFlow.value.step)

        // The original computed `currentStep - 1` with no floor.
        vm.trySendAction(SavingsApplicationAction.OnBack)
        advanceUntilIdle()
        assertEquals(SavingsStep.DETAILS, vm.stateFlow.value.step)
    }

    @Test
    fun `a double submit sends one application`() = runTest(dispatcher) {
        val commands = FakeSavingsCommandRepository(gate = CompletableDeferred())
        val vm = viewModel(commands)
        chooseProduct(vm)
        advanceSteps(vm, 3)

        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertEquals(1, commands.applications.size)
    }

    @Test
    fun `offline is reported as offline`() = runTest(dispatcher) {
        val vm = viewModel(
            FakeSavingsCommandRepository(
                createResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
            ),
        )
        chooseProduct(vm)
        advanceSteps(vm, 3)
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()

        assertTrue(assertIs<ApplicationDialog.Failed>(vm.stateFlow.value.dialogState).offline)
    }

    @Test
    fun `dismissing a failure keeps four pages of values`() = runTest(dispatcher) {
        val vm = viewModel(
            FakeSavingsCommandRepository(
                createResult = {
                    MutationResult.Failed(IllegalStateException("rejected"), rolledBack = true)
                },
            ),
        )
        chooseProduct(vm)
        vm.trySendAction(SavingsApplicationAction.OnExternalIdChanged("EXT-9"))
        vm.trySendAction(SavingsApplicationAction.OnChargeAdded(SavingsChargeRow(11, "A", "50")))
        advanceSteps(vm, 3)
        vm.trySendAction(SavingsApplicationAction.OnSubmit)
        advanceUntilIdle()
        vm.trySendAction(SavingsApplicationAction.CloseDialog)
        advanceUntilIdle()

        assertNull(vm.stateFlow.value.dialogState)
        assertEquals("EXT-9", vm.stateFlow.value.externalId)
        assertEquals(1, vm.stateFlow.value.charges.size)
        assertEquals(SavingsStep.PREVIEW, vm.stateFlow.value.step)
    }

    @Test
    fun `a failed template read is recoverable`() = runTest(dispatcher) {
        var fail = true
        val vm = viewModel(
            repo = FakeSavingsAccountRepository(
                savingsProducts = MutableStateFlow(ScreenState.Content(products)),
                applicationTemplate = { if (fail) null else template() },
            ),
        )
        chooseProduct(vm)
        assertTrue(vm.stateFlow.value.templateError)

        fail = false
        vm.trySendAction(SavingsApplicationAction.OnRetryTemplate)
        advanceUntilIdle()

        assertTrue(!vm.stateFlow.value.templateError)
        assertEquals(1, vm.stateFlow.value.selectionId(SavingsField.COMPOUNDING_PERIOD))
    }

    @Test
    fun `a group's and a client's drafts with the same id do not share a slot`() = runTest(dispatcher) {
        // ownerKind is part of the slot: group 31's application is not client 31's.
        val drafts = kpt.core.data.testing.FakeFormDraftRepository()
        val offline = FakeSavingsCommandRepository(
            createResult = { MutationResult.Blocked(kpt.core.base.store.mutation.BlockReason.OFFLINE) },
        )
        listOf(false, true).forEach { group ->
            val vm = viewModel(commands = offline, isGroup = group, drafts = drafts)
            chooseProduct(vm)
            advanceSteps(vm, 3)
            vm.trySendAction(SavingsApplicationAction.OnSubmit)
            advanceUntilIdle()
        }
        assertEquals(listOf("client:31", "group:31"), drafts.saved.value.map { it.slot }.sorted())
    }
}
