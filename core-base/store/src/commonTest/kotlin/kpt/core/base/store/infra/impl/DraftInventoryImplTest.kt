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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kpt.core.base.database.infra.entity.DraftEntity
import kpt.core.base.store.submit.SubmitOutboxStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks [DraftInventoryImpl] — the feed behind the **Sync & Drafts** screen, where a user sees and
 * acts on their unsent work. A silent bug here means a draft the user believes is queued is invisible
 * (or a discarded one reappears), so the entity→record mapping and the three actions are pinned.
 */
class DraftInventoryImplTest {

    private fun entity(
        formKey: String = "bill/edit",
        status: String = "PENDING",
        updatedAtMs: Long = 100L,
        uniqueKey: String? = null,
        error: String? = null,
    ) = DraftEntity(
        formKey = formKey,
        uniqueKey = uniqueKey,
        payloadJson = """{"x":1}""",
        status = status,
        createdAtMs = 1L,
        updatedAtMs = updatedAtMs,
        errorMessage = error,
    )

    @Test
    fun observeAllMapsEntityToRecord() = runTest {
        val dao = FakeDraftDao()
        dao.insert(entity(status = "FAILED", error = "boom"))
        val records = DraftInventoryImpl(dao).observeAll().first()

        assertEquals(1, records.size)
        val r = records.single()
        assertEquals("bill/edit", r.formKey)
        assertEquals(SubmitOutboxStatus.FAILED, r.status)
        assertEquals("boom", r.errorMessage, "the failure reason drives the resume UI's copy")
    }

    @Test
    fun observeAllExcludesTerminalSubmittedRows() = runTest {
        // SUBMITTED work is done and not actionable — surfacing it would make the screen look like
        // the user has pending work when they don't.
        val dao = FakeDraftDao()
        dao.insert(entity(formKey = "a", status = "PENDING"))
        dao.insert(entity(formKey = "b", status = "SUBMITTED"))
        dao.insert(entity(formKey = "c", status = "FAILED"))

        val keys = DraftInventoryImpl(dao).observeAll().first().map { it.formKey }.toSet()
        assertEquals(setOf("a", "c"), keys)
    }

    @Test
    fun observeAllIsNewestFirst() = runTest {
        val dao = FakeDraftDao()
        dao.insert(entity(formKey = "old", updatedAtMs = 10L))
        dao.insert(entity(formKey = "new", updatedAtMs = 900L))
        assertEquals(
            listOf("new", "old"),
            DraftInventoryImpl(dao).observeAll().first().map { it.formKey },
        )
    }

    @Test
    fun unknownStatusFallsBackToPendingRatherThanCrashing() = runTest {
        // A row written by a newer build (or a hand-edited DB) must not take down the whole screen —
        // the impl uses runCatching + PENDING, and a draft shown as pending is recoverable, whereas a
        // crash loses every other draft on the list too.
        val dao = FakeDraftDao()
        dao.insert(entity(status = "SOME_FUTURE_STATUS"))
        assertEquals(
            SubmitOutboxStatus.PENDING,
            DraftInventoryImpl(dao).observeAll().first().single().status,
        )
    }

    @Test
    fun discardRemovesOnlyThatRow() = runTest {
        val dao = FakeDraftDao()
        val keep = dao.insert(entity(formKey = "keep"))
        val drop = dao.insert(entity(formKey = "drop"))
        val inv = DraftInventoryImpl(dao)

        inv.discard(drop)

        val ids = inv.observeAll().first().map { it.id }
        assertEquals(listOf(keep), ids)
    }

    @Test
    fun retryRequeuesFailedRowAndClearsItsError() = runTest {
        // The whole point of Retry: a FAILED draft returns to PENDING so the syncer picks it up, and
        // the stale error message must not linger on a row that is about to be attempted again.
        val dao = FakeDraftDao()
        val id = dao.insert(entity(status = "FAILED", error = "network down"))
        val inv = DraftInventoryImpl(dao)

        inv.retry(id)

        val r = inv.observeAll().first().single()
        assertEquals(SubmitOutboxStatus.PENDING, r.status)
        assertEquals(null, r.errorMessage)
        assertTrue(dao.getAllPending().any { it.id == id }, "a retried draft must be visible to the syncer")
    }

    @Test
    fun pruneExpiredKeepsPendingDraftsForever() = runTest {
        // PENDING is unsent user work — it is never pruned, no matter how old. Only terminal
        // SUBMITTED/FAILED rows age out.
        val dao = FakeDraftDao()
        dao.insert(entity(formKey = "ancient-pending", status = "PENDING", updatedAtMs = 0L))
        dao.insert(entity(formKey = "ancient-failed", status = "FAILED", updatedAtMs = 0L))

        DraftInventoryImpl(dao).pruneExpired()

        assertEquals(
            listOf("ancient-pending"),
            dao.rows.value.map { it.formKey },
            "pruning must never delete unsent (PENDING) work",
        )
    }
}
