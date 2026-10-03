/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.checkerinbox.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.checkerinbox.entity.CheckerTaskEntity

@DbDao
@Dao
interface CheckerInboxDao {

    /** Observe the pending queue, newest first. Emits on every change. */
    @Query("SELECT * FROM checker_tasks ORDER BY madeOnDate DESC")
    fun observeAll(): Flow<List<CheckerTaskEntity>>

    /** Observe one task. Emits `null` once it is approved/rejected and purged. */
    @Query("SELECT * FROM checker_tasks WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<CheckerTaskEntity?>

    /** One-shot read for non-reactive callers. */
    @Query("SELECT * FROM checker_tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): CheckerTaskEntity?

    @Upsert
    suspend fun upsertAll(tasks: List<CheckerTaskEntity>)

    /** A decided task leaves the queue — the server is the authority on what remains. */
    @Query("DELETE FROM checker_tasks WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM checker_tasks")
    suspend fun deleteAll()
}
