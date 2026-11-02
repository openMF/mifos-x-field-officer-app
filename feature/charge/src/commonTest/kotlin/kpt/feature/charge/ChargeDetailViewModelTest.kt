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
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeChargeRepository
import kpt.core.model.charge.Charges
import kpt.core.model.objects.charge.ChargeResource
import kpt.feature.charge.detail.ChargeDetailAction
import kpt.feature.charge.detail.ChargeDetailEvent
import kpt.feature.charge.detail.ChargeDetailViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChargeDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val rows = listOf(Charges(id = 4, name = "Other"), Charges(id = 5, name = "Fee", penalty = true))

    private fun viewModel(repository: FakeChargeRepository, resource: ChargeResource = ChargeResource.LOAN) =
        ChargeDetailViewModel(
            repository,
            SavedStateHandle(mapOf("resourceType" to resource.path, "resourceId" to 7, "chargeId" to 5)),
        )

    private suspend fun app.cash.turbine.ReceiveTurbine<ScreenState<kpt.feature.charge.ui.ChargeUi>>.settled() =
        awaitItem().let { if (it is ScreenState.Loading) awaitItem() else it }

    @Test
    fun showsTheRoutedChargeOutOfItsOwnersList() = runTest(dispatcher) {
        val repository = FakeChargeRepository(ScreenState.Content(rows))
        val vm = viewModel(repository)

        vm.charge.test {
            val charge = assertIs<ScreenState.Content<kpt.feature.charge.ui.ChargeUi>>(settled()).data
            assertEquals("Fee", charge.name)
            assertEquals(true, charge.penalty)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(ChargeResource.LOAN to 7), repository.streamRequests.toList())
    }

    @Test
    fun aChargeNoLongerOnTheAccountIsEmpty() = runTest(dispatcher) {
        val repository = FakeChargeRepository(ScreenState.Content(rows))
        val vm = viewModel(repository)

        vm.charge.test {
            settled()
            // Deleted from the list screen: the same table, so the detail follows.
            repository.charges.value = ScreenState.Content(rows.filterNot { it.id == 5 })
            assertEquals(ScreenState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun editNavigatesForSavingsButNeverForAClient() = runTest(dispatcher) {
        val savings = viewModel(FakeChargeRepository(ScreenState.Content(rows)), ChargeResource.SAVINGS)
        savings.eventFlow.test {
            savings.trySendAction(ChargeDetailAction.OnEdit)
            testScheduler.advanceUntilIdle()
            assertEquals(ChargeDetailEvent.EditCharge("savingsaccounts", 7, 5), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val client = viewModel(FakeChargeRepository(ScreenState.Content(rows)), ChargeResource.CLIENT)
        client.eventFlow.test {
            client.trySendAction(ChargeDetailAction.OnEdit)
            client.trySendAction(ChargeDetailAction.NavigateBack)
            testScheduler.advanceUntilIdle()
            assertEquals(ChargeDetailEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
