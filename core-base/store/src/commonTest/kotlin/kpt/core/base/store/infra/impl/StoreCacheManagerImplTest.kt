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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kpt.core.base.database.infra.dao.BookkeeperDao
import kpt.core.base.database.infra.entity.BookkeeperEntity
import kpt.core.base.database.infra.entity.DraftEntity
import kpt.core.base.store.infra.StoreCacheManager
import kpt.core.base.store.infra.StoreFactory
import org.mobilenativefoundation.store.store5.SourceOfTruth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class ClearCountingBookkeeperDao : BookkeeperDao {
    val rows = mutableMapOf<String, Long>()
    var deleteAllCount = 0
    override suspend fun getLastFailedSync(key: String): Long? = rows[key]
    override suspend fun pendingKeys(): List<String> =
        rows.entries.sortedBy { it.value }.map { it.key }
    override suspend fun upsert(entity: BookkeeperEntity) {
        rows[entity.key] = entity.lastFailedSync
    }
    override suspend fun delete(key: String) {
        rows.remove(key)
    }
    override suspend fun deleteAll() {
        deleteAllCount++
        rows.clear()
    }
}

/**
 * Locks [StoreCacheManagerImpl] — the logout purge and the draft-TTL prune.
 *
 * This is a **privacy boundary**: `clearAll()` is what stops user A's cached rows and unsent drafts
 * from surfacing in user B's session on a shared device. It had no tests at all, so a store silently
 * dropping out of the registered set, or a DAO not being cleared, would have gone unnoticed.
 */
class StoreCacheManagerImplTest {

    private fun draft(status: String, updatedAtMs: Long) = DraftEntity(
        formKey = "f",
        payloadJson = "{}",
        status = status,
        createdAtMs = 0L,
        updatedAtMs = updatedAtMs,
    )

    /** A local-only store whose SoT we can inspect — `clear()` must reach it. */
    private fun localStore(backing: MutableStateFlow<String?>) =
        StoreFactory.createOfflineStore<String, String>(
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: String -> backing },
                writer = { _: String, v: String -> backing.value = v },
                delete = { _: String -> backing.value = null },
                deleteAll = { backing.value = null },
            ),
        )

    @Test
    fun clearAllPurgesBookkeeperAndDrafts() = runTest {
        val bk = ClearCountingBookkeeperDao()
        val drafts = FakeDraftDao()
        bk.upsert(BookkeeperEntity(key = "todo:1", lastFailedSync = 5L))
        drafts.insert(draft("PENDING", 1L))

        StoreCacheManagerImpl(bk, drafts).clearAll()

        assertNull(bk.getLastFailedSync("todo:1"), "sync tombstones must not survive logout")
        assertTrue(drafts.rows.value.isEmpty(), "user A's drafts must not surface in user B's session")
        assertEquals(1, bk.deleteAllCount)
    }

    @Test
    fun clearAllClearsEveryRegisteredStore() = runTest {
        val a = MutableStateFlow<String?>("a-cached")
        val b = MutableStateFlow<String?>("b-cached")
        val mgr = StoreCacheManagerImpl(ClearCountingBookkeeperDao(), FakeDraftDao())
        mgr.register(localStore(a))
        mgr.register(localStore(b))

        mgr.clearAll()

        assertNull(a.value, "every registered store must be cleared — a missed one leaks across users")
        assertNull(b.value)
    }

    @Test
    fun clearAllIsSafeWithNoRegisteredStores() = runTest {
        // Logout must not explode on a fork that has registered nothing yet.
        StoreCacheManagerImpl(ClearCountingBookkeeperDao(), FakeDraftDao()).clearAll()
    }

    @Test
    fun pruneExpiredDraftsKeepsPendingAndRecentTerminalRows() = runTest {
        val drafts = FakeDraftDao()
        val now = 1_000_000_000_000L
        val ttl = StoreCacheManager.DEFAULT_DRAFT_TTL_MS
        // Deliberately older than any plausible cutoff, so the assertion does not depend on the
        // wall-clock the impl reads.
        drafts.insert(draft("SUBMITTED", updatedAtMs = 0L))
        drafts.insert(draft("FAILED", updatedAtMs = 0L))
        drafts.insert(draft("PENDING", updatedAtMs = 0L))
        drafts.insert(draft("SUBMITTED", updatedAtMs = now))

        StoreCacheManagerImpl(ClearCountingBookkeeperDao(), drafts).pruneExpiredDrafts(ttl)

        val kept = drafts.rows.value
        assertTrue(
            kept.any { it.status == "PENDING" },
            "PENDING is unsent user work and is never pruned, however old",
        )
        assertTrue(
            kept.none { it.status != "PENDING" && it.updatedAtMs == 0L },
            "ancient terminal rows should be pruned",
        )
    }
}
