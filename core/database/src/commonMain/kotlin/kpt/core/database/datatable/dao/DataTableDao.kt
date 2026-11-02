/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.datatable.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.datatable.entity.DataTableEntity

/**
 * `DataTable` had an entity and no DAO, so the registered-datatable list had no offline path at
 * all — every screen that needed it went to the network or did without.
 */
@DbDao
@Dao
interface DataTableDao {

    @Query(
        "SELECT * FROM DataTable WHERE applicationTableName = :appTable " +
            "ORDER BY registeredTableName ASC",
    )
    fun observeForAppTable(appTable: String): Flow<List<DataTableEntity>>

    @Query("DELETE FROM DataTable WHERE applicationTableName = :appTable")
    suspend fun deleteForAppTable(appTable: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tables: List<DataTableEntity>)

    /**
     * Replace rather than upsert: the primary key is auto-generated, so re-inserting a fetched
     * list would append a second copy of every row instead of refreshing it.
     */
    @Transaction
    suspend fun replaceForAppTable(appTable: String, tables: List<DataTableEntity>) {
        deleteForAppTable(appTable)
        insertAll(tables)
    }

    @Query("DELETE FROM DataTable")
    suspend fun deleteAll()
}
