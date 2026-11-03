/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.mutation.conflict

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kpt.core.base.database.infra.dao.ConflictDao
import kpt.core.base.database.infra.entity.ConflictEntity
import kpt.core.base.store.mutation.conflict.impl.RoomConflictInbox
import kotlin.test.Test
import kotlin.test.assertEquals

/** In-memory [ConflictDao] — exercises [RoomConflictInbox] without a real Room database. */
private class FakeConflictDao : ConflictDao {
    private val rows = MutableStateFlow<List<ConflictEntity>>(emptyList())
    private var nextId = 1L
    override suspend fun insert(entity: ConflictEntity): Long {
        val id = nextId++
        rows.value = rows.value + entity.copy(id = id)
        return id
    }
    override fun observePending(): Flow<List<ConflictEntity>> =
        rows.map { list -> list.filter { it.resolved == 0 }.sortedByDescending { it.recordedAtMs } }
    override suspend fun getById(id: Long): ConflictEntity? = rows.value.firstOrNull { it.id == id }
    override suspend fun markResolved(id: Long) {
        rows.value = rows.value.map { if (it.id == id) it.copy(resolved = 1) else it }
    }
}

/** Tests the Room-backed [RoomConflictInbox] record → observe → resolve contract (SP-1/T4). */
class RoomConflictInboxTest {

    private fun inbox(clock: () -> Long = { 1L }) = RoomConflictInbox(FakeConflictDao(), clock)

    @Test
    fun record_thenObservePending_returnsIt() = runTest {
        val inbox = inbox()
        val id = inbox.record("todo", "5", "\"local\"", "\"server\"", "todo/edit/5")
        val pending = inbox.observePending().first()
        assertEquals(1, pending.size)
        assertEquals(id, pending.single().id)
        assertEquals("todo/edit/5", pending.single().formRoute)
        assertEquals("5", pending.single().key)
    }

    @Test
    fun resolve_clearsFromPending() = runTest {
        val inbox = inbox()
        val a = inbox.record("todo", "1", "\"la\"", "\"sa\"", null)
        inbox.record("todo", "2", "\"lb\"", "\"sb\"", null)
        assertEquals(2, inbox.observePending().first().size)
        inbox.resolve(a, ConflictResolution.ACCEPT_SERVER)
        val pending = inbox.observePending().first()
        assertEquals(1, pending.size)
        assertEquals("2", pending.single().key)
    }

    @Test
    fun observePending_newestFirst() = runTest {
        var t = 100L
        val inbox = inbox(clock = { t })
        inbox.record("todo", "old", "\"\"", "\"\"", null)
        t = 200L
        inbox.record("todo", "new", "\"\"", "\"\"", null)
        val pending = inbox.observePending().first()
        assertEquals("new", pending.first().key, "newest conflict must sort first")
    }
}
