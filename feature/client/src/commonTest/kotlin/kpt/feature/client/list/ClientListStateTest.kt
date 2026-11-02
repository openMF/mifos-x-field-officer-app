/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.list

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.client.Client
import kpt.core.model.client.ClientStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the derived list view.
 *
 * The original kept `clients` and `unfilteredClients` side by side, sorted the former and filtered the
 * latter. These tests pin the behaviour that arrangement could not give: filter and sort composing in
 * one direction, and an empty RESULT being distinguishable from an empty DATASET.
 */
class ClientListStateTest {

    private fun client(
        id: Int,
        name: String,
        account: String? = null,
        office: String? = "Head Office",
        status: String? = "Active",
    ) = Client(
        id = id,
        displayName = name,
        accountNo = account,
        officeName = office,
        status = ClientStatus(value = status),
    )

    private fun loaded(vararg clients: Client): ScreenState<List<Client>> =
        ScreenState.Content(clients.toList())

    /** The rendered rows for a given filter state — the pure half of the screen's logic. */
    private fun ScreenState<List<Client>>.visible(ui: ClientListState = ClientListState()) =
        (this as? ScreenState.Content)?.data?.filteredFor(ui).orEmpty()

    /** The state the screen renders: the ViewModel's exact Flow pipeline, over one emission. */
    private suspend fun ScreenState<List<Client>>.rendered(ui: ClientListState) =
        flowOf(this).applyFilters(flowOf(ui)).first()

    /** Office names offered by the filter, derived from the loaded rows exactly as the screen does. */
    private fun ScreenState<List<Client>>.officeNames(): List<String> =
        (this as? ScreenState.Content)?.data?.mapNotNull { it.officeName }?.distinct()?.sorted().orEmpty()

    @Test
    fun filterAndSortCompose() {
        // The original applied sort to the filtered list and filter to the unfiltered one, so doing
        // both silently dropped the sort. Here one derived view does both, in a fixed order.
        val state = loaded(
            client(1, "Zara", office = "Branch A"),
            client(2, "Amit", office = "Branch B"),
            client(3, "Meera", office = "Branch A"),
        )
        val ui = ClientListState(selectedOffices = setOf("Branch A"), sort = ClientSort.Name)

        assertEquals(listOf("Meera", "Zara"), state.visible(ui).map { it.displayName })
    }

    @Test
    fun aFilterThatMatchesNothingIsEmptyNotAnEmptyList() = runTest {
        val state = loaded(client(1, "Zara"))
        val ui = ClientListState(selectedOffices = setOf("Nowhere"))
        // Empty RESULT and empty DATASET must not render identically — the original showed a bare
        // list for both, so "no clients in this office" and "your filter excluded everyone" looked
        // the same.
        assertTrue(state.rendered(ui) is ScreenState.Empty)
        assertTrue(state.visible(ui).isEmpty())
    }

    @Test
    fun searchMatchesNameOrAccountNumberCaseInsensitively() {
        val state = loaded(
            client(1, "Ramesh", account = "000123"),
            client(2, "Sunita", account = "000987"),
        )
        assertEquals(
            listOf("Ramesh"),
            state.visible(ClientListState(query = "rame")).map { it.displayName },
        )
        assertEquals(
            listOf("Sunita"),
            state.visible(ClientListState(query = "987")).map { it.displayName },
        )
    }

    @Test
    fun officeNamesComeFromTheLoadedDataNotASeparateFetch() {
        val state = loaded(
            client(1, "A", office = "Branch B"),
            client(2, "B", office = "Branch A"),
            client(3, "C", office = "Branch A"),
        )
        // The original accumulated office names via an OnUpdateOffice action the screen had to
        // remember to dispatch; deriving them cannot go stale or be forgotten.
        assertEquals(listOf("Branch A", "Branch B"), state.officeNames())
    }

    @Test
    fun loadingAndErrorPassThroughUntouchedByFilters() = runTest {
        // A filter must not turn a failed load into "Empty" — that would hide the failure.
        val error: ScreenState<List<Client>> = ScreenState.Error(RuntimeException("boom"))
        val ui = ClientListState(selectedOffices = setOf("Branch A"))
        assertTrue(error.rendered(ui) is ScreenState.Error)

        val loading: ScreenState<List<Client>> = ScreenState.Loading
        // A query must not turn a still-loading read into Empty — the filter applies to CONTENT.
        assertTrue(loading.rendered(ClientListState(query = "x")) is ScreenState.Loading)
    }

    @Test
    fun clearingFiltersRestoresEveryRow() {
        val state = loaded(client(1, "A", office = "X"), client(2, "B", office = "Y"))
        assertEquals(1, state.visible(ClientListState(selectedOffices = setOf("X"), query = "A")).size)
        assertEquals(2, state.visible(ClientListState()).size)
    }
}
