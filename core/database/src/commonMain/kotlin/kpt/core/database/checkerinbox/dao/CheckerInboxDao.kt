/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.checkerinbox.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.checkerinbox.entity.CheckerTaskEntity

/** Room queries for checker inbox rows. */
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

    /** Inserts `tasks`, or replaces rows with the same key. */
    @Upsert
    suspend fun upsertAll(tasks: List<CheckerTaskEntity>)

    /** A decided task leaves the queue — the server is the authority on what remains. */
    @Query("DELETE FROM checker_tasks WHERE id = :id")
    suspend fun deleteById(id: Int)

    /** Deletes every `checker_tasks` row. */
    @Query("DELETE FROM checker_tasks")
    suspend fun deleteAll()

    /**
     * Make the table EXACTLY the server's pending queue. The store's writer used [upsertAll] alone,
     * so a task decided elsewhere (the web app, another checker) stayed pending here for good.
     */
    @Transaction
    suspend fun replaceAll(tasks: List<CheckerTaskEntity>) {
        deleteAll()
        upsertAll(tasks)
    }
}
