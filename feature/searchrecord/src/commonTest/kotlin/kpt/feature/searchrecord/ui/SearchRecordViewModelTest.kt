/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.searchrecord.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeSearchRecordRepository
import kpt.core.model.objects.searchrecord.GenericSearchRecord
import kpt.core.model.objects.searchrecord.RecordType
import kpt.core.model.objects.searchrecord.SearchRecordKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalDataTestingApi::class)
class SearchRecordViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val home = GenericSearchRecord(
        id = 5,
        name = "Home",
        description = "12 Lake Road, Dhaka",
        type = RecordType.ADDRESS.name,
    )
    private val office = GenericSearchRecord(
        id = 6,
        name = "Office",
        description = "4 Mill Lane, Sylhet",
        type = RecordType.ADDRESS.name,
    )
    private val passport = GenericSearchRecord(
        id = 9,
        name = "Passport",
        description = "scan",
        type = RecordType.IDENTIFIER.name,
    )

    private val repository = FakeSearchRecordRepository(listOf(home, office, passport))

    private fun viewModel(type: RecordType = RecordType.ADDRESS) =
        SearchRecordViewModel(SavedStateHandle(mapOf("type" to type.name)), repository)

    /**
     * A started VM: [SearchRecordViewModel.screen] is `WhileSubscribed`, so — as on device — it reads only while
     * collected.
     */
    private fun TestScope.started(type: RecordType = RecordType.ADDRESS) = viewModel(type).also { vm ->
        backgroundScope.launch { vm.screen.collect {} }
        runCurrent()
    }

    private val SearchRecordViewModel.shown get() = screen.value

    private fun SearchRecordViewModel.type(query: String) = trySendAction(SearchRecordAction.SearchQueryChanged(query))

    @Test
    fun theRouteDecidesWhichRecordsAreSearched() = runTest(dispatcher) {
        val vm = started(RecordType.IDENTIFIER)
        vm.type("pass")
        advanceTimeBy(400)
        runCurrent()

        assertEquals(listOf(9), vm.shown.searchRecords.map { it.id })
        assertEquals(listOf(SearchRecordKey(RecordType.IDENTIFIER, "pass")), repository.requested)
    }

    @Test
    fun typingSearchesOnlyAfterTheDebounceAndOnlyForTheLastQuery() = runTest(dispatcher) {
        val vm = started()
        vm.type("d")
        runCurrent()
        vm.type("dh")
        runCurrent()
        vm.type("dha")
        runCurrent()
        advanceTimeBy(200)
        runCurrent()
        assertTrue(repository.requested.isEmpty(), "nothing is searched inside the 300 ms window")

        advanceTimeBy(200)
        runCurrent()
        assertEquals(listOf("dha"), repository.requested.map { it.query })
        assertEquals(listOf(5), vm.shown.searchRecords.map { it.id })
    }

    @Test
    fun aQueryThatMatchesNothingIsNoResultsNotTheEmptyPrompt() = runTest(dispatcher) {
        val vm = started()
        vm.type("nowhere")
        advanceTimeBy(400)
        runCurrent()

        assertTrue(vm.shown.isNoResultsFound)
        assertTrue(vm.shown.searchRecords.isEmpty())
    }

    @Test
    fun clearingTheBoxDropsTheResultsAtOnce() = runTest(dispatcher) {
        val vm = started()
        vm.type("dhaka")
        advanceTimeBy(400)
        runCurrent()
        assertEquals(1, vm.shown.searchRecords.size)

        vm.type("")
        runCurrent()
        // Immediately — not 300 ms later, and not with "dhaka"'s result still listed.
        assertTrue(vm.shown.searchRecords.isEmpty())
        assertFalse(vm.shown.isNoResultsFound)

        vm.type("sylhet")
        advanceTimeBy(400)
        runCurrent()
        vm.trySendAction(SearchRecordAction.ClearSearch)
        runCurrent()
        assertEquals("", vm.shown.searchQuery)
        assertTrue(vm.shown.searchRecords.isEmpty())
    }

    @Test
    fun aCachedRecordArrivingLaterShowsUpInTheOpenSearch() = runTest(dispatcher) {
        val vm = started()
        vm.type("chittagong")
        advanceTimeBy(400)
        runCurrent()
        assertTrue(vm.shown.isNoResultsFound)

        // Another screen caches a client's addresses while this search is open.
        repository.records.value += GenericSearchRecord(
            id = 7,
            name = "Shop",
            description = "Chittagong",
            type = RecordType.ADDRESS.name,
        )
        runCurrent()
        assertEquals(listOf(7), vm.shown.searchRecords.map { it.id })
    }

    @Test
    fun theScreenFollowsTheBoxBeforeAnyQuerySettles() = runTest(dispatcher) {
        // The fake, like the real stream, emits NOTHING until its first key.
        val silent = FakeSearchRecordRepository()
        val vm = SearchRecordViewModel(SavedStateHandle(mapOf("type" to RecordType.ADDRESS.name)), silent)
        backgroundScope.launch { vm.screen.collect {} }
        runCurrent()

        vm.type("d")
        runCurrent()
        // Device 2026-10-24: `combine` waited on the silent stream, so this stayed "".
        assertEquals("d", vm.shown.searchQuery)
    }

    @Test
    fun nothingIsReadUntilTheScreenCollects() = runTest(dispatcher) {
        val vm = viewModel()
        vm.type("dhaka")
        advanceTimeBy(400)
        runCurrent()
        // No init{} collector: without a subscriber the store is never asked.
        assertTrue(repository.requested.isEmpty())
    }

    @Test
    fun selectingARecordAsksToOpenIt() = runTest(dispatcher) {
        val vm = started()
        vm.eventFlow.test {
            vm.trySendAction(SearchRecordAction.SelectRecord(home))
            runCurrent()
            assertEquals(SearchRecordEvent.NavigateToRecord(home), awaitItem())
            vm.trySendAction(SearchRecordAction.NavigateBack)
            runCurrent()
            assertEquals(SearchRecordEvent.NavigateBack, awaitItem())
        }
    }
}
