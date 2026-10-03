/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.list

import kpt.core.base.store.screen.ScreenState
import kpt.core.database.client.entity.ClientEntity
import kpt.core.database.client.entity.ClientStatusEntity
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
    ) = ClientEntity(
        id = id,
        displayName = name,
        accountNo = account,
        officeName = office,
        status = ClientStatusEntity(value = status),
    )

    private fun loaded(vararg clients: ClientEntity) =
        ClientListState(all = ScreenState.Content(clients.toList()))

    @Test
    fun filterAndSortCompose() {
        // The original applied sort to the filtered list and filter to the unfiltered one, so doing
        // both silently dropped the sort. Here one derived view does both, in a fixed order.
        val state = loaded(
            client(1, "Zara", office = "Branch A"),
            client(2, "Amit", office = "Branch B"),
            client(3, "Meera", office = "Branch A"),
        ).copy(selectedOffices = setOf("Branch A"), sort = ClientSort.Name)

        assertEquals(listOf("Meera", "Zara"), state.visible.map { it.displayName })
    }

    @Test
    fun aFilterThatMatchesNothingIsEmptyNotAnEmptyList() {
        val state = loaded(client(1, "Zara")).copy(selectedOffices = setOf("Nowhere"))
        // Empty RESULT and empty DATASET must not render identically — the original showed a bare
        // list for both, so "no clients in this office" and "your filter excluded everyone" looked
        // the same.
        assertTrue(state.listState is ScreenState.Empty)
        assertTrue(state.visible.isEmpty())
    }

    @Test
    fun searchMatchesNameOrAccountNumberCaseInsensitively() {
        val state = loaded(
            client(1, "Ramesh", account = "000123"),
            client(2, "Sunita", account = "000987"),
        )
        assertEquals(listOf("Ramesh"), state.copy(query = "rame").visible.map { it.displayName })
        assertEquals(listOf("Sunita"), state.copy(query = "987").visible.map { it.displayName })
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
        assertEquals(listOf("Branch A", "Branch B"), state.officeNames)
    }

    @Test
    fun loadingAndErrorPassThroughUntouchedByFilters() {
        // A filter must not turn a failed load into "Empty" — that would hide the failure.
        val error = ClientListState(all = ScreenState.Error(RuntimeException("boom")))
            .copy(selectedOffices = setOf("Branch A"))
        assertTrue(error.listState is ScreenState.Error)

        val loading = ClientListState(all = ScreenState.Loading).copy(query = "x")
        assertTrue(loading.listState is ScreenState.Loading)
    }

    @Test
    fun clearingFiltersRestoresEveryRow() {
        val state = loaded(client(1, "A", office = "X"), client(2, "B", office = "Y"))
            .copy(selectedOffices = setOf("X"), query = "A")
        assertEquals(1, state.visible.size)
        assertEquals(2, state.copy(selectedOffices = emptySet(), query = "").visible.size)
    }
}
