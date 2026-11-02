/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(
    kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class,
    kpt.core.data.testing.ExperimentalDataTestingApi::class,
)

package kpt.feature.client.collateral

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeClientCommandRepository
import kpt.core.data.testing.FakeClientRepository
import kpt.core.model.shared.CollateralItem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClientCollateralViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val catalog = MutableStateFlow<ScreenState<List<CollateralItem>>>(ScreenState.Loading)

    private val repository = FakeClientRepository(collateralCatalog = catalog)

    private fun viewModel(
        commands: FakeClientCommandRepository = FakeClientCommandRepository(),
        clientId: Int = 42,
    ) = ClientCollateralViewModel(
        repository,
        commands,
        SavedStateHandle(mapOf("clientId" to clientId)),
        kpt.core.data.testing.FakeFormDraftRepository(),
    )

    private fun item(id: Int, name: String, basePrice: Double, pctToBase: Double) = CollateralItem(
        quality = "A",
        basePrice = basePrice,
        unitType = "kg",
        pctToBase = pctToBase,
        currency = "INR",
        name = name,
        id = id,
    )

    private val gold = item(1, "Gold", basePrice = 100.0, pctToBase = 80.0)
    private val silver = item(2, "Silver", basePrice = 10.0, pctToBase = 50.0)

    // ---- collateralTotals: the two fixed defects live here ----

    @Test
    fun totalsAreComputedFromThePriceAndTheQuantity() {
        val (total, collateralValue) = collateralTotals(gold, "3")
        assertEquals(300.0, total)
        // 300 * 80 / 100
        assertEquals(240.0, collateralValue)
    }

    @Test
    fun anEmptyOrNonNumericQuantityYieldsZeroInsteadOfCrashing() {
        // THE CRASH. The original did `quantity.toInt()` with no guard, on every keystroke — the
        // first character after clearing the field, or any paste with a non-digit, threw
        // NumberFormatException inside handleAction. The `isEmpty()` checks two lines later look
        // like they cover it and cannot: toInt() has already thrown.
        assertEquals(0.0 to 0.0, collateralTotals(gold, ""))
        assertEquals(0.0 to 0.0, collateralTotals(gold, "abc"))
        assertEquals(0.0 to 0.0, collateralTotals(gold, "1.5"))
        assertEquals(0.0 to 0.0, collateralTotals(gold, "-"))
    }

    @Test
    fun totalsFollowTheSelectedTypeNotJustTheQuantity() {
        // THE SECOND DEFECT. `total`/`totalCollateral` were stored in state and only recomputed on
        // a quantity keystroke, so switching type left the previous type's figures on screen. The
        // original's dropdown re-fired OnQuantityChange to paper over it. Derived now, so the same
        // quantity against a different type simply gives different figures.
        assertEquals(300.0 to 240.0, collateralTotals(gold, "3"))
        assertEquals(30.0 to 15.0, collateralTotals(silver, "3"))
    }

    // ---- the ViewModel ----

    @Test
    fun theCatalogIsRead() = runTest(dispatcher) {
        viewModel()
        testScheduler.advanceUntilIdle()
        // Keyed `Unit` — a tenant catalog, so no clientId is recorded for it.
        assertTrue(repository.requestedKeys["collateralCatalog"].isNullOrEmpty())
    }

    @Test
    fun anInvalidQuantityBlocksTheSaveAndSurfacesTheValidatorsMessage() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository()
        val vm = viewModel(commands)
        catalog.value = ScreenState.Content(listOf(gold))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientCollateralAction.OnQuantityChange("0"))
        vm.actionChannel.trySend(ClientCollateralAction.OnSave(collateralId = 1))
        testScheduler.advanceUntilIdle()

        // The original's validator and timing, kept: checked on SUBMIT, not on edit.
        assertNotNull(vm.stateFlow.value.quantityError)
        assertTrue(commands.createdCollateral.isEmpty())
    }

    @Test
    fun editingTheQuantityClearsTheError() = runTest(dispatcher) {
        val vm = viewModel()
        catalog.value = ScreenState.Content(listOf(gold))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientCollateralAction.OnSave(1))
        testScheduler.advanceUntilIdle()
        assertNotNull(vm.stateFlow.value.quantityError)

        vm.actionChannel.trySend(ClientCollateralAction.OnQuantityChange("5"))
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.quantityError)
    }

    @Test
    fun aValidSaveSendsTheSelectedTypeAndQuantity() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository()
        val vm = viewModel(commands)
        catalog.value = ScreenState.Content(listOf(gold, silver))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientCollateralAction.OnQuantityChange("7"))
        // The id comes from the ACTION — the original re-indexed the list at submit time.
        vm.actionChannel.trySend(ClientCollateralAction.OnSave(collateralId = 2))
        testScheduler.advanceUntilIdle()

        val (clientId, payload) = commands.createdCollateral.single()
        assertEquals(42, clientId)
        assertEquals(2, payload.collateralId)
        assertEquals("7", payload.quantity)
    }

    @Test
    fun aSecondSaveInsideTheInFlightWindowIsDropped() = runTest(dispatcher) {
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val commands = FakeClientCommandRepository(gate = gate)
        val vm = viewModel(commands)
        catalog.value = ScreenState.Content(listOf(gold))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientCollateralAction.OnQuantityChange("7"))
        vm.actionChannel.trySend(ClientCollateralAction.OnSave(1))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(ClientCollateralAction.OnSave(1))
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        // Two POSTs pledge the collateral twice, and nothing on the screen would say so.
        assertEquals(1, commands.createdCollateral.size)
    }
}
