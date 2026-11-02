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

package kpt.feature.client.address

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeClientRepository
import kpt.core.model.objects.clients.ClientAddressEntity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ClientAddressViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val addresses = MutableStateFlow<ScreenState<List<ClientAddressEntity>>>(ScreenState.Loading)
    private val repository = FakeClientRepository(addresses = addresses)

    private fun viewModel(clientId: Int = 42) =
        ClientAddressViewModel(repository, SavedStateHandle(mapOf("clientId" to clientId)))

    private fun address(id: Int, type: String) =
        ClientAddressEntity(addressId = id, clientID = 42, addressType = type, city = "Pune")

    @Test
    fun theRouteArgumentReachesTheRead() = runTest(dispatcher) {
        viewModel(clientId = 7)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf<Any>(7), repository.requestedKeys["addresses"]?.toList())
    }

    @Test
    fun theReadIsSubscribedExactlyONCE() = runTest(dispatcher) {
        // The original loaded in `init` AND again from a `LaunchedEffect(Unit)` on the screen, so
        // opening the screen fetched the address list twice.
        val vm = viewModel()
        addresses.value = ScreenState.Content(listOf(address(1, "Home")))

        vm.addresses.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Content<List<ClientAddressEntity>>>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, repository.requestedKeys["addresses"]?.size)
    }

    @Test
    fun retryDoesNotStackCollectorsTheWayTheOriginalDid() = runTest(dispatcher) {
        // THE LEAK. The original's `handleRetry()` called `observeNetwork()` again with no
        // cancellation, so each retry added another collector on `networkMonitor.isOnline` — after
        // three retries one reconnect fired four parallel address loads. There is no such observer
        // here; retry is one call on the stream.
        val vm = viewModel()
        repository.refreshesFor("addresses").test {
            repeat(3) {
                vm.actionChannel.trySend(ClientAddressAction.OnRetry)
                testScheduler.advanceUntilIdle()
            }
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
        // Still one stream, one subscription.
        assertEquals(1, repository.requestedKeys["addresses"]?.size)
    }

    @Test
    fun theAddFormEventCarriesTheClientId() = runTest(dispatcher) {
        val vm = viewModel(clientId = 42)
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientAddressAction.ShowAddressForm)
            // The original read `state.id`, which was only set once the list load succeeded — so
            // tapping "+" before the list arrived opened the form for client -1.
            assertEquals(ClientAddressEvent.ShowAddressForm(42), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removingTheLastAddressLeavesAnEmptyListNotTheRemovedRow() = runTest(dispatcher) {
        // The stream reports "no addresses" as an empty Content (no `isEmpty` predicate) and the
        // screen draws the original's EmptyAddressCard from it. With the predicate the stream held
        // the last address on screen after it was gone.
        val vm = viewModel()
        vm.addresses.test {
            assertIs<ScreenState.Loading>(awaitItem())
            addresses.value = ScreenState.Content(listOf(address(1, "Home")))
            assertEquals(1, assertIs<ScreenState.Content<List<ClientAddressEntity>>>(awaitItem()).data.size)
            addresses.value = ScreenState.Content(emptyList())
            assertEquals(
                emptyList<ClientAddressEntity>(),
                assertIs<ScreenState.Content<List<ClientAddressEntity>>>(awaitItem()).data,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
