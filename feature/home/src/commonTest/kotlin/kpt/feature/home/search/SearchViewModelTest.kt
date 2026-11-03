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

package kpt.feature.home.search

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeSearchRepository
import kpt.core.model.objects.SearchedEntity
import kpt.core.model.search.SearchResourcesKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Search is the only ViewModel in this port with a DYNAMIC read key, so it is the only one whose
 * correctness depends on what happens when the key changes mid-flight.
 *
 * The original hand-managed a `searchJob`, cancelling and relaunching it per submit and lifting
 * each launch into `mutableStateFlow`. That is now `flatMapLatest` over a submitted-key flow —
 * the cancellation is declared rather than performed. These tests pin the behaviour that change
 * is responsible for.
 */
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val repository = FakeSearchRepository()

    private fun hit(id: Int, name: String) = SearchedEntity(entityId = id, entityName = name)

    private fun key(query: String, resource: String? = null, exact: Boolean = false) =
        SearchResourcesKey(query = query, resource = resource, exactMatch = exact)

    @Test
    fun typingAloneSendsNothing() = runTest(dispatcher) {
        // Search is EXPLICIT in this app — a Search button, as in the original. If typing fired
        // requests, every keystroke on a three-letter name would hit Fineract.
        //
        // Asserted INSIDE a collector on purpose. `results` is `WhileSubscribed`, so with nothing
        // collecting no stream is ever built and `requested` stays empty no matter what the
        // ViewModel does — this test passed for exactly that wrong reason before the collector
        // was added.
        val vm = SearchViewModel(repository)
        vm.results.test {
            assertIs<ScreenState.Empty>(awaitItem())
            vm.trySendAction(SearchAction.QueryChanged("ash"))
            testScheduler.advanceUntilIdle()

            assertEquals(emptyList(), repository.requested)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun submitSearchesTheTrimmedQuery() = runTest(dispatcher) {
        val vm = SearchViewModel(repository)
        vm.results.test {
            assertIs<ScreenState.Empty>(awaitItem())

            vm.trySendAction(SearchAction.QueryChanged("  asha  "))
            vm.trySendAction(SearchAction.Submit)
            testScheduler.advanceUntilIdle()

            assertEquals(listOf(key("asha")), repository.requested)
            repository.resolve(key("asha"), hit(1, "Asha"))
            testScheduler.advanceUntilIdle()
            val content = assertIs<ScreenState.Content<List<SearchedEntity>>>(expectMostRecentItem())
            assertEquals("Asha", content.data.single().entityName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aSearchWithNoHitsIsEmpty() = runTest(dispatcher) {
        // The stream reports no hits as an empty Content; `emptyIfContent` makes it Empty.
        val vm = SearchViewModel(repository)
        vm.results.test {
            assertIs<ScreenState.Empty>(awaitItem())
            vm.trySendAction(SearchAction.QueryChanged("zzz"))
            vm.trySendAction(SearchAction.Submit)
            testScheduler.advanceUntilIdle()
            repository.resolve(key("zzz"))
            testScheduler.advanceUntilIdle()
            assertEquals(ScreenState.Empty, expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aBlankQueryNeverReachesTheServer() = runTest(dispatcher) {
        // The Fineract search endpoint treats an empty `query` as UNFILTERED and would return the
        // whole directory. The original guarded this with a showEmptyError flag; the guard is kept.
        val vm = SearchViewModel(repository)
        vm.results.test {
            assertIs<ScreenState.Empty>(awaitItem())
            vm.trySendAction(SearchAction.QueryChanged("   "))
            vm.trySendAction(SearchAction.Submit)
            testScheduler.advanceUntilIdle()

            assertEquals(emptyList(), repository.requested)
            assertTrue(vm.stateFlow.value.blankQuery)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aSupersededSearchCannotDeliverItsResults() = runTest(dispatcher) {
        // THE test this refactor exists for. A slow first search must not paint results over a
        // second, later query — `flatMapLatest` abandons the previous stream when the key changes.
        // The hand-rolled version relied on remembering to cancel `searchJob`.
        val vm = SearchViewModel(repository)
        vm.results.test {
            assertIs<ScreenState.Empty>(awaitItem())

            vm.trySendAction(SearchAction.QueryChanged("asha"))
            vm.trySendAction(SearchAction.Submit)
            testScheduler.advanceUntilIdle()

            vm.trySendAction(SearchAction.QueryChanged("bimal"))
            vm.trySendAction(SearchAction.Submit)
            testScheduler.advanceUntilIdle()

            // The FIRST search now answers, late. `flatMapLatest` has already abandoned that
            // stream, so nothing reaches the screen — the results stay Loading for "bimal".
            repository.resolve(key("asha"), hit(1, "Asha"))
            testScheduler.advanceUntilIdle()
            assertIs<ScreenState.Loading>(vm.results.value)

            repository.resolve(key("bimal"), hit(2, "Bimal"))
            testScheduler.advanceUntilIdle()
            val content = assertIs<ScreenState.Content<List<SearchedEntity>>>(vm.results.value)
            assertEquals(
                "Bimal",
                content.data.single().entityName,
                "a superseded search painted its results over the current query",
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun changingTheFilterResearchesImmediately() = runTest(dispatcher) {
        // Filter and exact-match re-run the search without a second Submit — the original did this
        // too, and it is why they are separate actions rather than plain state edits.
        val vm = SearchViewModel(repository)
        vm.results.test {
            assertIs<ScreenState.Empty>(awaitItem())
            vm.trySendAction(SearchAction.QueryChanged("asha"))
            vm.trySendAction(SearchAction.Submit)
            testScheduler.advanceUntilIdle()

            vm.trySendAction(SearchAction.FilterChanged(SearchFilter.Clients))
            testScheduler.advanceUntilIdle()

            assertEquals(
                listOf(key("asha"), key("asha", resource = SearchFilter.Clients.apiValue)),
                repository.requested,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun exactMatchIsPartOfTheReadKey() = runTest(dispatcher) {
        val vm = SearchViewModel(repository)
        vm.results.test {
            assertIs<ScreenState.Empty>(awaitItem())
            vm.trySendAction(SearchAction.QueryChanged("asha"))
            vm.trySendAction(SearchAction.Submit)
            testScheduler.advanceUntilIdle()

            vm.trySendAction(SearchAction.ExactMatchToggled)
            testScheduler.advanceUntilIdle()

            // A different key, so the Store caches the two answers separately rather than serving
            // the loose match for an exact query.
            assertEquals(listOf(key("asha"), key("asha", exact = true)), repository.requested)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearAbandonsTheSearchAndResetsTheForm() = runTest(dispatcher) {
        val vm = SearchViewModel(repository)
        vm.results.test {
            assertIs<ScreenState.Empty>(awaitItem())
            vm.trySendAction(SearchAction.QueryChanged("asha"))
            vm.trySendAction(SearchAction.Submit)
            testScheduler.advanceUntilIdle()

            vm.trySendAction(SearchAction.Clear)
            testScheduler.advanceUntilIdle()

            // Results drop back to Empty, and a late answer for the cleared query is ignored.
            repository.resolve(key("asha"), hit(1, "Asha"))
            testScheduler.advanceUntilIdle()
            assertIs<ScreenState.Empty>(vm.results.value)
            assertEquals("", vm.stateFlow.value.query)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
