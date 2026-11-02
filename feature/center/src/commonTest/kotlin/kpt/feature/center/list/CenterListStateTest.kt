/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.center.list

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.screen.ScreenState
import kpt.feature.center.testing.center
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The derived view is the whole of this screen's logic, so it is tested directly rather than
 * through the ViewModel — there is no stream behaviour here to exercise.
 */
class CenterListStateTest {

    private val rows: ScreenState<List<kpt.core.model.center.Center>> =
        ScreenState.Content(
            listOf(
                center(1, name = "Zeta", accountNo = "C9", officeName = "Branch A"),
                center(2, name = "alpha", accountNo = "C1", officeName = "Branch B"),
                center(3, name = "Mid", accountNo = "C5", officeName = "Branch A"),
            ),
        )

    /** The filter state is now UI-only; the rows arrive from the stream. */
    private fun visible(ui: CenterListState) =
        (rows as? ScreenState.Content)?.data?.filteredFor(ui).orEmpty()

    /** The state the screen renders: the ViewModel's exact Flow pipeline, over one emission. */
    private suspend fun ScreenState<List<kpt.core.model.center.Center>>.rendered(ui: CenterListState) =
        flowOf(this).applyFilters(flowOf(ui)).first()

    @Test
    fun sortByNameIsCaseInsensitive() {
        // "alpha" lowercase must sort BEFORE "Mid" and "Zeta". A naive sortedBy on the raw string
        // puts every capitalised name first, which silently buries lowercase-entered centers at the
        // bottom of a list a field officer scans by eye.
        val names = visible(CenterListState(sort = CenterSort.Name)).map { it.name }
        assertEquals(listOf("alpha", "Mid", "Zeta"), names)
    }

    @Test
    fun searchMatchesNameOrAccountNumber() {
        assertEquals(listOf("Zeta"), visible(CenterListState(query = "zet")).map { it.name })
        assertEquals(listOf("alpha"), visible(CenterListState(query = "C1")).map { it.name })
    }

    @Test
    fun officeFilterAndSearchCompose() {
        val ui = CenterListState(selectedOffices = setOf("Branch A"), query = "mid")
        assertEquals(listOf("Mid"), visible(ui).map { it.name })
    }

    @Test
    fun aFilterMatchingNothingIsEmptyNotAnEmptyContent() = runTest {
        // THE distinction this state class exists for. Content(emptyList()) renders as a blank list
        // indistinguishable from "this officer services no centers"; Empty renders the empty state,
        // whose retry clears the filters. Getting this wrong is invisible until a user is stuck.
        assertIs<ScreenState.Empty>(rows.rendered(CenterListState(query = "no-such-center")))
    }

    @Test
    fun loadingAndErrorPassThroughUntouched() = runTest {
        val ui = CenterListState()
        val loading = ScreenState.Loading as ScreenState<List<kpt.core.model.center.Center>>
        assertIs<ScreenState.Loading>(loading.rendered(ui))
        val boom: ScreenState<List<kpt.core.model.center.Center>> = ScreenState.Error(RuntimeException("boom"))
        // Identity, not equality: a non-Content state must pass through UNTOUCHED, keeping its
        // cause and its freshness metadata.
        assertTrue(boom.rendered(ui) === boom)
    }
}
