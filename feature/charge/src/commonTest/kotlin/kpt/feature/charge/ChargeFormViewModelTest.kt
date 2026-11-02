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

package kpt.feature.charge

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeChargeCommandRepository
import kpt.core.data.testing.FakeChargeRepository
import kpt.core.model.charge.Charges
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.error.ServerErrorParser
import kpt.core.model.objects.error.userMessage
import kpt.core.model.objects.template.client.ChargeCalculationType
import kpt.core.model.objects.template.client.ChargeOptions
import kpt.core.model.objects.template.client.ChargeTemplate
import kpt.core.model.objects.template.client.ChargeTimeType
import kpt.feature.charge.form.ChargeFormAction
import kpt.feature.charge.form.ChargeFormState
import kpt.feature.charge.form.ChargeFormViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kpt.core.model.charge.ChargeTimeType as ChargesTimeType

class ChargeFormViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    // Fineract enum ids: time 1 disbursement, 2 specified due date, 6 annual fee, 8 installment,
    // 9 overdue installment; calculation 1 flat, 2 % amount.
    private fun option(id: Int?, time: Int, calculation: Int = 1, amount: Double? = 150.0, name: String? = "Fee $id") =
        ChargeOptions(
            id = id,
            name = name,
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
        penalty = false,
        chargeOptions = listOf(
            option(1, time = 2),
            option(2, time = 1),
            option(3, time = 8),
            option(4, time = 2, calculation = 2),
            option(5, time = 6),
            option(6, time = 9),
            option(null, time = 2),
            option(8, time = 2, amount = null),
        ),
    )

    private fun viewModel(
        resource: ChargeResource,
        repository: FakeChargeRepository = FakeChargeRepository(template = { catalogue }),
        commands: FakeChargeCommandRepository = FakeChargeCommandRepository(),
        chargeId: Int = -1,
    ) = ChargeFormViewModel(
        repository,
        commands,
        SavedStateHandle(mapOf("resourceType" to resource.path, "resourceId" to 7, "chargeId" to chargeId)),
    )

    @Test
    fun theTemplateIsAskedForTheRoutedOwner() = runTest(dispatcher) {
        val repository = FakeChargeRepository(template = { catalogue })
        viewModel(ChargeResource.SAVINGS, repository)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(ChargeResource.SAVINGS to 7), repository.templateRequests.toList())
    }

    @Test
    fun aLoanOffersOnlyTheOriginalsThreeFlatKinds() = runTest(dispatcher) {
        val vm = viewModel(ChargeResource.LOAN)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(1, 2, 3), vm.stateFlow.value.chargeTypes.map { it.id })
    }

    @Test
    fun clientAndSavingsDropOnlyWhatTheFormCannotCollect() = runTest(dispatcher) {
        for (resource in listOf(ChargeResource.CLIENT, ChargeResource.SAVINGS)) {
            val vm = viewModel(resource)
            testScheduler.advanceUntilIdle()
            // Annual fee (5) and overdue (6) need inputs the form lacks; 7/8 cannot be sent at all.
            assertEquals(listOf(1, 2, 3, 4), vm.stateFlow.value.chargeTypes.map { it.id })
        }
    }

    @Test
    fun aFailedTemplateIsAnErrorNotAnEmptyForm() = runTest(dispatcher) {
        val vm = viewModel(ChargeResource.LOAN, FakeChargeRepository(template = { null }))
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.typesError)
    }

    @Test
    fun aCreateSendsTheChargeAndADueDateOnlyWhenItHasOne() = runTest(dispatcher) {
        val commands = FakeChargeCommandRepository()
        val vm = viewModel(ChargeResource.CLIENT, commands = commands)
        testScheduler.advanceUntilIdle()
        val types = vm.stateFlow.value.chargeTypes

        vm.trySendAction(ChargeFormAction.OnChargeSelected(types.first { it.id == 1 }))
        vm.trySendAction(ChargeFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        vm.trySendAction(ChargeFormAction.OnChargeSelected(types.first { it.id == 2 }))
        vm.trySendAction(ChargeFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val (dueDated, disbursement) = commands.created
        assertEquals(ChargeResource.CLIENT, dueDated.resource)
        assertEquals(7, dueDated.resourceId)
        assertEquals(1, dueDated.payload?.chargeId)
        assertEquals("150", dueDated.payload?.amount)
        assertNotNull(dueDated.payload?.dueDate)
        assertNull(disbursement.payload?.dueDate)
        assertEquals(ChargeFormState.DialogState.Submitted, vm.stateFlow.value.dialogState)
    }

    @Test
    fun nothingIsSentWithoutAChargeOrAPositiveAmount() = runTest(dispatcher) {
        val commands = FakeChargeCommandRepository()
        val vm = viewModel(ChargeResource.LOAN, commands = commands)
        testScheduler.advanceUntilIdle()

        vm.trySendAction(ChargeFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.chargeError)

        vm.trySendAction(ChargeFormAction.OnChargeSelected(vm.stateFlow.value.chargeTypes.first()))
        vm.trySendAction(ChargeFormAction.OnAmountChanged("0"))
        vm.trySendAction(ChargeFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.stateFlow.value.amountError)
        assertTrue(commands.created.isEmpty())
    }

    @Test
    fun anEditPrefillsFromTheChargeAndUpdatesWithoutAChargeId() = runTest(dispatcher) {
        val repository = FakeChargeRepository(
            ScreenState.Content(
                listOf(
                    Charges(
                        id = 5,
                        chargeId = 1,
                        name = "Fee 1",
                        amount = 200.0,
                        dueDate = listOf(2026, 3, 14),
                        chargeTimeType = ChargesTimeType(id = 2, value = "time 2"),
                    ),
                ),
            ),
            template = { catalogue },
        )
        val commands = FakeChargeCommandRepository()
        val vm = viewModel(ChargeResource.SAVINGS, repository, commands, chargeId = 5)
        testScheduler.advanceUntilIdle()

        val state = vm.stateFlow.value
        assertTrue(state.isEdit)
        // Matched by DEFINITION id, not by name.
        assertEquals(1, state.selectedCharge?.id)
        assertEquals("200", state.amount)

        // The definition cannot change on an update.
        vm.trySendAction(ChargeFormAction.OnChargeSelected(state.chargeTypes.first { it.id == 2 }))
        vm.trySendAction(ChargeFormAction.OnAmountChanged("250"))
        vm.trySendAction(ChargeFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        val update = commands.updated.single()
        assertEquals(5, update.chargeId)
        assertEquals("250", update.payload?.amount)
        assertNull(update.payload?.chargeId)
        assertNotNull(update.payload?.dueDate)
        assertTrue(commands.created.isEmpty())
    }

    @Test
    fun editingAChargeThisDeviceNeverLoadedIsAnError() = runTest(dispatcher) {
        val vm = viewModel(
            ChargeResource.LOAN,
            FakeChargeRepository(ScreenState.Content(emptyList()), template = { catalogue }),
            chargeId = 5,
        )
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.typesError)
    }

    @Test
    fun offlineAndServerFailuresAreToldApartAndKeepTheForm() = runTest(dispatcher) {
        val offline = viewModel(
            ChargeResource.LOAN,
            commands = FakeChargeCommandRepository { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        testScheduler.advanceUntilIdle()
        offline.trySendAction(ChargeFormAction.OnChargeSelected(offline.stateFlow.value.chargeTypes.first()))
        offline.trySendAction(ChargeFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertEquals(ChargeFormState.DialogState.Failed(offline = true), offline.stateFlow.value.dialogState)

        val refused = viewModel(
            ChargeResource.LOAN,
            commands = FakeChargeCommandRepository {
                MutationResult.Failed(IllegalStateException(FINERACT_403), rolledBack = false)
            },
        )
        testScheduler.advanceUntilIdle()
        refused.trySendAction(ChargeFormAction.OnChargeSelected(refused.stateFlow.value.chargeTypes.first()))
        refused.trySendAction(ChargeFormAction.OnSubmit)
        testScheduler.advanceUntilIdle()
        assertEquals(
            ChargeFormState.DialogState.Failed(
                serverMessage = "This charge cannot be updated as the loan is not pending approval",
            ),
            refused.stateFlow.value.dialogState,
        )

        refused.trySendAction(ChargeFormAction.CloseDialog)
        testScheduler.advanceUntilIdle()
        assertNull(refused.stateFlow.value.dialogState)
        assertEquals(1, refused.stateFlow.value.selectedCharge?.id)
    }

    @Test
    fun aFailureShowsFineractsSentenceNotTheRawResponse() {
        // Device finding 2026-10-23: the dialog printed the whole request line + JSON body.
        // The shared core/model parser (it replaced this module's own copy) — pinned here on the
        // body that found the bug.
        assertEquals(
            "This charge cannot be updated as the loan is not pending approval",
            ServerErrorParser.parse(FINERACT_403)?.userMessage,
        )
        // Not a Fineract body (a dropped connection) -> no message; the screen's generic one shows.
        assertNull(ServerErrorParser.parse("Unable to resolve host")?.userMessage)
    }

    private companion object {
        /** The shape the Ktor client error carries, trimmed from the one captured on device. */
        const val FINERACT_403 = "Client request(PUT https://x/loans/1/charges/1) invalid: 403. Text: " +
            "\"{\"defaultUserMessage\":\"Errors contain reason for domain rule violation.\",\"errors\":" +
            "[{\"defaultUserMessage\":\"This charge cannot be updated as the loan is not pending approval\"}]}\""
    }
}
