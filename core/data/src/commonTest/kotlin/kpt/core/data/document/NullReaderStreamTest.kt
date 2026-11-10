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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

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

    /**
     * Streams one store, deletes its last row, and returns everything seen until [reacted] holds.
     *
     * Store5 answers on its own dispatchers, so cancelling the collector straight after the delete
     * races the answer — and a slower CI host lost that race (Kover, 2026-11-10). The wait is in REAL
     * time, off the test scheduler, so virtual time cannot fire the timeout before Store has replied.
     */
    private suspend fun kotlinx.coroutines.test.TestScope.emissionsAfterDeletingTheLastRow(
        reader: (MutableStateFlow<List<String>>) -> Flow<List<String>?>,
        reacted: (List<StoreReadResponse<List<String>>>) -> Boolean,
    ): List<StoreReadResponse<List<String>>> {
        val table = MutableStateFlow(listOf("doc-1"))
        val store = StoreBuilder.from(
            fetcher = Fetcher.of { _: Int -> table.value },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: Int -> reader(table) },
                writer = { _: Int, rows: List<String> -> table.value = rows },
            ),
        ).build()
        val seen = MutableStateFlow(emptyList<StoreReadResponse<List<String>>>())
        val job = launch {
            store.stream(StoreReadRequest.cached(1, refresh = false)).collect { r -> seen.update { it + r } }
        }
        seen.awaitInRealTime { it.any { r -> r is StoreReadResponse.Data } }
        table.value = emptyList()
        seen.awaitInRealTime(reacted)
        job.cancel()
        return seen.value
    }

    private suspend fun <T> MutableStateFlow<T>.awaitInRealTime(predicate: (T) -> Boolean) {
        withContext(Dispatchers.Default) { withTimeout(5.seconds) { first(predicate) } }
    }

    @Test
    fun anEmptyListReaderShowsTheDeletion() = runTest(UnconfinedTestDispatcher()) {
        val seen = emissionsAfterDeletingTheLastRow(reader = { table -> table }) { seen ->
            (seen.last() as? StoreReadResponse.Data)?.value?.isEmpty() == true
        }
        assertEquals(emptyList(), (seen.last() as StoreReadResponse.Data).value)
    }

    @Test
    fun aNullForEmptyReaderNeverShowsIt() = runTest(UnconfinedTestDispatcher()) {
        // The deletion surfaces as Loading (and a refetch), never as an empty list. Waiting for a Loading
        // AFTER the first Data is itself the assertion: if it never comes, the wait times out and fails.
        val seen = emissionsAfterDeletingTheLastRow(
            reader = { table -> table.map { rows -> rows.takeIf { it.isNotEmpty() } } },
        ) { seen ->
            val firstData = seen.indexOfFirst { it is StoreReadResponse.Data }
            seen.drop(firstData + 1).any { it is StoreReadResponse.Loading }
        }
        // `value` is typed non-null but the reader emits null: on the CI Android host Store passed a
        // `Data(null)` through before the Loading (2026-10-27, PR #2705 Kover) — so read it nullable.
        assertTrue(seen.none { it is StoreReadResponse.Data && (it.value as List<String>?)?.isEmpty() == true })
    }
}
