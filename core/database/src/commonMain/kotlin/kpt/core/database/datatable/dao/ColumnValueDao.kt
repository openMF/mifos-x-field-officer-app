/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.datatable.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.datatable.entity.ColumnValue

/** Room queries for column value rows. */
@DbDao
@Dao
interface ColumnValueDao {
    /** Saves `columnValue`, replacing any row with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ColumnValue::class)
    suspend fun insert(columnValue: ColumnValue)

    /** Updates the stored row of `columnValue`. */
    @Update(entity = ColumnValue::class, onConflict = OnConflictStrategy.NONE)
    suspend fun update(columnValue: ColumnValue)

    /** Deletes the stored row of `columnValue`. */
    @Delete(entity = ColumnValue::class)
    suspend fun delete(columnValue: ColumnValue)

    /** Reads the `ColumnValue` row whose `id` is `id`. */
    @Query("SELECT * FROM ColumnValue WHERE id = :id")
    suspend fun getColumnValue(id: Int): ColumnValue?
}
