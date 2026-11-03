/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.list

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.screen.ScreenState
import kpt.feature.groups.testing.group
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The derived view is the whole of this screen's logic, so it is tested directly rather than
 * through the ViewModel — there is no stream behaviour here to exercise.
 */
class GroupsListStateTest {

    private val rows: ScreenState<List<kpt.core.model.group.Group>> =
        ScreenState.Content(
            listOf(
                group(1, name = "Zeta", accountNo = "C9", officeName = "Branch A"),
                group(2, name = "alpha", accountNo = "C1", officeName = "Branch B"),
                group(3, name = "Mid", accountNo = "C5", officeName = "Branch A"),
            ),
        )

    /** The filter state is now UI-only; the rows arrive from the stream. */
    private fun visible(ui: GroupsListState) =
        (rows as? ScreenState.Content)?.data?.filteredFor(ui).orEmpty()

    /** The state the screen renders: the ViewModel's exact Flow pipeline, over one emission. */
    private suspend fun ScreenState<List<kpt.core.model.group.Group>>.rendered(ui: GroupsListState) =
        flowOf(this).applyFilters(flowOf(ui)).first()

    @Test
    fun sortByNameIsCaseInsensitive() {
        // "alpha" lowercase must sort BEFORE "Mid" and "Zeta". A naive sortedBy on the raw string
        // puts every capitalised name first, which silently buries lowercase-entered centers at the
        // bottom of a list a field officer scans by eye.
        val names = visible(GroupsListState(sort = GroupsSort.Name)).map { it.name }
        assertEquals(listOf("alpha", "Mid", "Zeta"), names)
    }

    @Test
    fun searchMatchesNameOrAccountNumber() {
        assertEquals(listOf("Zeta"), visible(GroupsListState(query = "zet")).map { it.name })
        assertEquals(listOf("alpha"), visible(GroupsListState(query = "C1")).map { it.name })
    }

    @Test
    fun officeFilterAndSearchCompose() {
        val ui = GroupsListState(selectedOffices = setOf("Branch A"), query = "mid")
        assertEquals(listOf("Mid"), visible(ui).map { it.name })
    }

    @Test
    fun aFilterMatchingNothingIsEmptyNotAnEmptyContent() = runTest {
        // THE distinction this state class exists for. Content(emptyList()) renders as a blank list
        // indistinguishable from "this officer services no groups"; Empty renders the empty state,
        // whose retry clears the filters. Getting this wrong is invisible until a user is stuck.
        assertIs<ScreenState.Empty>(rows.rendered(GroupsListState(query = "no-such-group")))
    }

    @Test
    fun loadingAndErrorPassThroughUntouched() = runTest {
        val ui = GroupsListState()
        val loading = ScreenState.Loading as ScreenState<List<kpt.core.model.group.Group>>
        assertIs<ScreenState.Loading>(loading.rendered(ui))
        val boom: ScreenState<List<kpt.core.model.group.Group>> = ScreenState.Error(RuntimeException("boom"))
        // Identity, not equality: a non-Content state must pass through UNTOUCHED, keeping its
        // cause and its freshness metadata.
        assertTrue(boom.rendered(ui) === boom)
    }
}
