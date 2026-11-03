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
import kpt.core.base.database.infra.dao.FetchedAtDao
import kpt.core.base.database.infra.entity.FetchedAtEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/** In-memory [FetchedAtDao] — exercises [RoomFetchedAtRepository] without a real Room database. */
private class FakeFetchedAtDao : FetchedAtDao {
    val rows = mutableMapOf<String, Long>()
    override suspend fun read(storeKey: String): Long? = rows[storeKey]
    override suspend fun upsert(entity: FetchedAtEntity) {
        rows[entity.storeKey] = entity.lastFetchedMillis
    }
}

/**
 * Locks [RoomFetchedAtRepository] — the per-cacheKey freshness ledger behind the staleness banner and
 * the `CACHE_FIRST_SWR` band gate. If a written timestamp does not round-trip, `decideFreshness`
 * computes the wrong band: either a permanently-stale banner, or an SWR revalidation that never fires.
 */
class RoomFetchedAtRepositoryTest {

    private fun repo(dao: FakeFetchedAtDao = FakeFetchedAtDao()) = RoomFetchedAtRepository(dao)

    @Test
    fun unknownKeyReadsNull() = runTest {
        assertNull(repo().read("never:written"))
    }

    @Test
    fun writeThenReadRoundTripsTheInstant() = runTest {
        val r = repo()
        val instant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
        r.write("crypto:coinMarkets", instant)
        assertEquals(instant, r.read("crypto:coinMarkets"))
    }

    @Test
    fun keysAreIndependent() = runTest {
        val r = repo()
        r.write("a", Instant.fromEpochMilliseconds(1_000L))
        r.write("b", Instant.fromEpochMilliseconds(2_000L))
        assertEquals(Instant.fromEpochMilliseconds(1_000L), r.read("a"))
        assertEquals(Instant.fromEpochMilliseconds(2_000L), r.read("b"))
    }

    @Test
    fun writeOverwritesPreviousTimestamp() = runTest {
        // A refresh must move freshness forward — if the write did not overwrite, the band would stay
        // pinned at the first fetch and the screen would look permanently stale.
        val r = repo()
        r.write("k", Instant.fromEpochMilliseconds(1_000L))
        r.write("k", Instant.fromEpochMilliseconds(9_000L))
        assertEquals(Instant.fromEpochMilliseconds(9_000L), r.read("k"))
    }

    @Test
    fun epochZeroIsPreservedNotTreatedAsAbsent() = runTest {
        // Guards the null-vs-zero seam: epoch 0 is a real timestamp, and must not read back as "never
        // fetched", which would make the band Initial instead of VeryStale.
        val r = repo()
        r.write("k", Instant.fromEpochMilliseconds(0L))
        assertEquals(Instant.fromEpochMilliseconds(0L), r.read("k"))
    }
}
