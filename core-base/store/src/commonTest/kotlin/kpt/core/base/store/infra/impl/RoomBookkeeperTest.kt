/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.infra.impl

import kotlinx.coroutines.test.runTest
import kpt.core.base.database.infra.dao.BookkeeperDao
import kpt.core.base.database.infra.entity.BookkeeperEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** In-memory [BookkeeperDao] — exercises [RoomBookkeeper] without a real Room database. */
private class FakeBookkeeperDao : BookkeeperDao {
    val rows = mutableMapOf<String, Long>()
    override suspend fun getLastFailedSync(key: String): Long? = rows[key]
    override suspend fun pendingKeys(): List<String> =
        rows.entries.sortedBy { it.value }.map { it.key }
    override suspend fun upsert(entity: BookkeeperEntity) {
        rows[entity.key] = entity.lastFailedSync
    }
    override suspend fun delete(key: String) {
        rows.remove(key)
    }
    override suspend fun deleteAll() = rows.clear()
}

/**
 * Locks [RoomBookkeeper] — the Store5 [org.mobilenativefoundation.store.store5.Bookkeeper] that makes
 * offline writes retryable. If this silently no-ops, a failed write is never retried on reconnect and
 * the user's mutation is lost, so the key-serialization + round-trip contract is worth pinning.
 */
class RoomBookkeeperTest {

    private data class Key(val id: Int)

    private fun bookkeeper(dao: FakeBookkeeperDao = FakeBookkeeperDao()) =
        RoomBookkeeper<Key>(dao) { "todo:${it.id}" }

    @Test
    fun unknownKeyHasNoFailedSync() = runTest {
        assertNull(bookkeeper().getLastFailedSync(Key(1)))
    }

    @Test
    fun setThenGetRoundTrips() = runTest {
        val bk = bookkeeper()
        assertTrue(bk.setLastFailedSync(Key(7), timestamp = 1_234L))
        assertEquals(1_234L, bk.getLastFailedSync(Key(7)))
    }

    @Test
    fun keysAreSerializedIndependently() = runTest {
        val bk = bookkeeper()
        bk.setLastFailedSync(Key(1), 100L)
        bk.setLastFailedSync(Key(2), 200L)
        assertEquals(100L, bk.getLastFailedSync(Key(1)))
        assertEquals(200L, bk.getLastFailedSync(Key(2)))
    }

    @Test
    fun setOverwritesPreviousTimestampForSameKey() = runTest {
        val bk = bookkeeper()
        bk.setLastFailedSync(Key(1), 100L)
        bk.setLastFailedSync(Key(1), 999L)
        assertEquals(999L, bk.getLastFailedSync(Key(1)))
    }

    @Test
    fun clearRemovesOnlyThatKey() = runTest {
        val bk = bookkeeper()
        bk.setLastFailedSync(Key(1), 100L)
        bk.setLastFailedSync(Key(2), 200L)
        bk.clear(Key(1))
        assertNull(bk.getLastFailedSync(Key(1)))
        assertEquals(200L, bk.getLastFailedSync(Key(2)), "clear(key) must not touch sibling keys")
    }

    @Test
    fun clearAllRemovesEverything() = runTest {
        val bk = bookkeeper()
        bk.setLastFailedSync(Key(1), 100L)
        bk.setLastFailedSync(Key(2), 200L)
        bk.clearAll()
        assertNull(bk.getLastFailedSync(Key(1)))
        assertNull(bk.getLastFailedSync(Key(2)))
    }

    @Test
    fun serializerCollisionsShareARow() = runTest {
        // Documents the contract: the bookkeeper is only as unique as its keySerializer. A serializer
        // that collapses distinct keys makes them share one tombstone — a real source of "the retry
        // fired for the wrong entity" bugs, so it is pinned rather than left implicit.
        val dao = FakeBookkeeperDao()
        val collapsing = RoomBookkeeper<Key>(dao) { "constant" }
        collapsing.setLastFailedSync(Key(1), 100L)
        collapsing.setLastFailedSync(Key(2), 200L)
        assertEquals(1, dao.rows.size)
        assertEquals(200L, collapsing.getLastFailedSync(Key(1)))
    }
}
