/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.feature.client.list

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeClientRepository
import kpt.core.model.client.Client
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * `ClientListStateTest` covers the derived view (search / filter / sort) as a pure function. This
 * covers the half it cannot reach: the stream subscription and the action dispatch.
 *
 * Both were the audit's finding — a state class can be perfect while the ViewModel never
 * subscribes, and every assertion in the other file still passes.
 */
class ClientListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val clients = MutableStateFlow<ScreenState<List<Client>>>(ScreenState.Loading)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    /** The shared fake from `core/data`, supplied only the roster stream. */
    private val repository = FakeClientRepository(clients = clients)

    private fun client(id: Int, name: String, office: String? = "Branch A") =
        Client(id = id, displayName = name, officeName = office, accountNo = "C$id")

    @Test
    fun pullingToRefreshAsksTheStreamToRefetch() = runTest(dispatcher) {
        val vm = ClientListViewModel(repository)
        vm.screenState.test {
            clients.value = ScreenState.Content(listOf(client(1, "Asha")))
            repository.refreshes.test {
                vm.trySendAction(ClientListAction.Refresh)
                // The original's pull (`refreshClientList`) — without it a client deleted on the
                // server stayed listed until the cache TTL ran out.
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theStreamIsSubscribedOnlyWhileSomethingCollects() = runTest(dispatcher) {
        val vm = ClientListViewModel(repository)
        clients.value = ScreenState.Content(listOf(client(1, "Asha"), client(2, "Bimal")))
        testScheduler.advanceUntilIdle()

        // `WhileSubscribed` means nothing flows until a collector attaches — the screen is the
        // collector. That is what stops the Clients tab refreshing while the user is elsewhere.
        assertIs<ScreenState.Loading>(vm.screenState.value)

        vm.screenState.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<List<Client>>>(awaitItem())
            assertEquals(2, content.data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theFilterStateIsCombinedIntoTheRenderedRows() = runTest(dispatcher) {
        val vm = ClientListViewModel(repository)
        clients.value = ScreenState.Content(listOf(client(1, "Asha"), client(2, "Bimal")))

        vm.screenState.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Content<List<Client>>>(awaitItem())

            vm.trySendAction(ClientListAction.QueryChanged("bim"))
            // The combine re-emits on a filter change WITHOUT re-reading the stream.
            val filtered = assertIs<ScreenState.Content<List<Client>>>(awaitItem())
            assertEquals("Bimal", filtered.data.single().displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aTappedClientRaisesOpenClientWithItsId() = runTest(dispatcher) {
        val vm = ClientListViewModel(repository)
        vm.eventFlow.test {
            vm.trySendAction(ClientListAction.ClientClicked(42))
            assertEquals(ClientListEvent.OpenClient(42), awaitItem())
        }
    }

    @Test
    fun clearingFiltersResetsTheSearchButKeepsTheSort() = runTest(dispatcher) {
        val vm = ClientListViewModel(repository)
        vm.trySendAction(ClientListAction.QueryChanged("abc"))
        vm.trySendAction(ClientListAction.OfficeFilterToggled("Branch A"))
        vm.trySendAction(ClientListAction.StatusFilterToggled("Active"))
        vm.trySendAction(ClientListAction.SortChanged(ClientSort.Name))
        testScheduler.advanceUntilIdle()

        vm.trySendAction(ClientListAction.FiltersCleared)
        testScheduler.advanceUntilIdle()

        assertEquals("", vm.stateFlow.value.query)
        assertEquals(emptySet(), vm.stateFlow.value.selectedOffices)
        assertEquals(emptySet(), vm.stateFlow.value.selectedStatuses)
        // The sort SURVIVES: it is an ordering preference, not a filter. Wiping it would reorder
        // the list under a user who only wanted to widen the search.
        assertEquals(ClientSort.Name, vm.stateFlow.value.sort)
    }

    @Test
    fun filtersToggleOffWhenTappedTwice() = runTest(dispatcher) {
        val vm = ClientListViewModel(repository)
        vm.trySendAction(ClientListAction.OfficeFilterToggled("A"))
        testScheduler.advanceUntilIdle()
        assertEquals(setOf("A"), vm.stateFlow.value.selectedOffices)

        vm.trySendAction(ClientListAction.OfficeFilterToggled("A"))
        testScheduler.advanceUntilIdle()
        assertEquals(emptySet(), vm.stateFlow.value.selectedOffices)
    }
}
