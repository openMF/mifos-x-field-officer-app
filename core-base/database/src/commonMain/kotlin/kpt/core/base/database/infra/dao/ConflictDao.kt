/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database.infra.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.infra.entity.ConflictEntity

/** DAO for the framework write-conflict inbox (`framework_write_conflicts`). */
@Dao
interface ConflictDao {

    @Insert
    suspend fun insert(entity: ConflictEntity): Long

    /** Pending (unresolved) conflicts, newest first — drives the Settings inbox list + badge. */
    @Query("SELECT * FROM framework_write_conflicts WHERE resolved = 0 ORDER BY recordedAtMs DESC")
    fun observePending(): Flow<List<ConflictEntity>>

    @Query("SELECT * FROM framework_write_conflicts WHERE id = :id")
    suspend fun getById(id: Long): ConflictEntity?

    @Query("UPDATE framework_write_conflicts SET resolved = 1 WHERE id = :id")
    suspend fun markResolved(id: Long)
}
