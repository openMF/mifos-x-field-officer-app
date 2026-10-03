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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.base.database.infra.dao.DraftDao
import kpt.core.base.database.infra.entity.DraftEntity

/**
 * In-memory [DraftDao] shared by the `infra/impl` tests ([RoomSubmitOutboxTest],
 * [DraftInventoryImplTest], [StoreCacheManagerImplTest]) — exercises the Room-backed
 * implementations without a real Room database, mirroring the `FakeConflictDao` pattern.
 *
 * Query semantics deliberately mirror the real `@Query` SQL: `getAllPending` filters to PENDING,
 * `observeAll` excludes terminal SUBMITTED rows newest-first, and `markRetrying` increments
 * `attemptCount` exactly as the DAO's `SET attemptCount = attemptCount + 1` does. A fake that
 * diverges here would let a broken implementation pass.
 */
internal class FakeDraftDao : DraftDao {

    val rows = MutableStateFlow<List<DraftEntity>>(emptyList())
    private var nextId = 1L

    private fun mutate(id: Long, block: (DraftEntity) -> DraftEntity) {
        rows.value = rows.value.map { if (it.id == id) block(it) else it }
    }

    override suspend fun insert(entity: DraftEntity): Long {
        val id = nextId++
        rows.value = rows.value + entity.copy(id = id)
        return id
    }

    override suspend fun getById(id: Long): DraftEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun getPendingByFormKey(formKey: String): DraftEntity? =
        rows.value.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == "PENDING"
        }

    override fun observePendingByFormKey(formKey: String): Flow<DraftEntity?> =
        rows.map { list ->
            list.firstOrNull { it.formKey == formKey && it.uniqueKey == null && it.status == "PENDING" }
        }

    override suspend fun getPendingByUniqueKey(formKey: String, uniqueKey: String): DraftEntity? =
        rows.value.firstOrNull {
            it.formKey == formKey && it.uniqueKey == uniqueKey && it.status == "PENDING"
        }

    override fun observePendingByUniqueKey(formKey: String, uniqueKey: String): Flow<DraftEntity?> =
        rows.map { list ->
            list.firstOrNull {
                it.formKey == formKey && it.uniqueKey == uniqueKey && it.status == "PENDING"
            }
        }

    override fun observeAllByFormKey(formKey: String): Flow<List<DraftEntity>> =
        rows.map { list -> list.filter { it.formKey == formKey } }

    override suspend fun getAllPending(): List<DraftEntity> =
        rows.value.filter { it.status == "PENDING" }

    /** Mirrors the real query: non-terminal rows only (SUBMITTED excluded), newest-first. */
    override fun observeAll(): Flow<List<DraftEntity>> =
        rows.map { list -> list.filter { it.status != "SUBMITTED" }.sortedByDescending { it.updatedAtMs } }

    override suspend fun markRetrying(id: Long, nowMs: Long) = mutate(id) {
        it.copy(status = "RETRYING", updatedAtMs = nowMs, attemptCount = it.attemptCount + 1)
    }

    override suspend fun markSubmitted(id: Long, nowMs: Long) = mutate(id) {
        it.copy(status = "SUBMITTED", updatedAtMs = nowMs)
    }

    override suspend fun requeue(id: Long, nowMs: Long) = mutate(id) {
        it.copy(status = "PENDING", updatedAtMs = nowMs, errorMessage = null)
    }

    override suspend fun markFailed(id: Long, nowMs: Long, error: String?) = mutate(id) {
        it.copy(status = "FAILED", updatedAtMs = nowMs, errorMessage = error)
    }

    override suspend fun updatePayload(id: Long, payloadJson: String, nowMs: Long) = mutate(id) {
        it.copy(payloadJson = payloadJson, updatedAtMs = nowMs)
    }

    override suspend fun deleteByFormKey(formKey: String) {
        rows.value = rows.value.filterNot { it.formKey == formKey }
    }

    override suspend fun deleteByUniqueKey(formKey: String, uniqueKey: String) {
        rows.value = rows.value.filterNot { it.formKey == formKey && it.uniqueKey == uniqueKey }
    }

    override suspend fun deleteById(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }

    /** Mirrors the real query: prunes only TERMINAL rows (SUBMITTED / FAILED) older than the cutoff. */
    override suspend fun deleteOlderThan(thresholdMs: Long) {
        rows.value = rows.value.filterNot {
            it.updatedAtMs < thresholdMs && (it.status == "SUBMITTED" || it.status == "FAILED")
        }
    }
}
