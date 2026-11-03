/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.document

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Why every list SourceOfTruth reader in core/store reports an empty table as an EMPTY LIST, never
 * as null — the case of deleting an entity's LAST row (its last document, its last client).
 *
 * Measured 2026-10-23: with a "no rows = null" reader the stream answers that delete with
 * `Loading` and a refetch, never with an empty list, so the screen cannot show "empty" and the
 * deleted row lingers until the network answers. That reader was briefly used to separate
 * "never fetched" from "really empty"; that question is answered in the repository's offline
 * lookup instead (`DocumentRepositoryImpl.fetchDocuments`).
 */
class NullReaderStreamTest {

    private suspend fun kotlinx.coroutines.test.TestScope.emissionsAfterDeletingTheLastRow(
        reader: (MutableStateFlow<List<String>>) -> Flow<List<String>?>,
    ): List<StoreReadResponse<List<String>>> {
        val table = MutableStateFlow(listOf("doc-1"))
        val store = StoreBuilder.from(
            fetcher = Fetcher.of { _: Int -> table.value },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: Int -> reader(table) },
                writer = { _: Int, rows: List<String> -> table.value = rows },
            ),
        ).build()
        val seen = mutableListOf<StoreReadResponse<List<String>>>()
        val job = launch { store.stream(StoreReadRequest.cached(1, refresh = false)).toList(seen) }
        table.value = emptyList()
        job.cancel()
        return seen
    }

    @Test
    fun anEmptyListReaderShowsTheDeletion() = runTest(UnconfinedTestDispatcher()) {
        val seen = emissionsAfterDeletingTheLastRow { table -> table }
        assertEquals(emptyList(), (seen.last() as StoreReadResponse.Data).value)
    }

    @Test
    fun aNullForEmptyReaderNeverShowsIt() = runTest(UnconfinedTestDispatcher()) {
        val seen = emissionsAfterDeletingTheLastRow { table -> table.map { rows -> rows.takeIf { it.isNotEmpty() } } }
        // The deletion surfaces as Loading (and a refetch), never as an empty list.
        // `value` is typed non-null but the reader emits null: on the CI Android host Store passed a
        // `Data(null)` through before the Loading (2026-10-27, PR #2705 Kover) — so read it nullable.
        assertTrue(seen.none { it is StoreReadResponse.Data && (it.value as List<String>?)?.isEmpty() == true })
        assertTrue(seen.last() is StoreReadResponse.Loading)
    }
}
