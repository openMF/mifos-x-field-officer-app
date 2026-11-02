/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.dao

import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.database.datatable.entity.ColumnHeader
import kpt.core.database.datatable.entity.ColumnValue
import kpt.core.database.datatable.entity.DataTableEntity

/** Datatable header/value rows that ride along a client template. */
interface ClientDataTableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ColumnHeader::class)
    suspend fun insertColumnHeader(columnHeader: ColumnHeader)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ColumnValue::class)
    suspend fun insertColumnValue(columnValue: ColumnValue)

    @Query("DELETE FROM DataTable")
    suspend fun deleteDataTables()

    @Query("DELETE FROM ColumnHeader")
    suspend fun deleteColumnHeaders()

    @Query("DELETE FROM ColumnValue")
    suspend fun deleteColumnValues()

    @Query("SELECT * FROM DataTable where applicationTableName = :tableName")
    fun getDatatableByTableName(tableName: String): Flow<List<DataTableEntity>>

    @Query("SELECT * FROM ColumnHeader WHERE registeredTableName = :tableName")
    fun getColumnHeadersByTableName(tableName: String): Flow<List<ColumnHeader>>

    @Query("SELECT * FROM ColumnValue WHERE registeredTableName = :tableName")
    fun getColumnValuesByTableName(tableName: String): Flow<List<ColumnValue>>

    @Insert(entity = DataTableEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataTable(dataTable: DataTableEntity)

    /** Replace every datatable (and its headers/values) — cleared ONCE, then rewritten. */
    suspend fun replaceDataTables(dataTables: List<DataTableEntity>) {
        deleteDataTables()
        deleteColumnHeaders()
        deleteColumnValues()
        dataTables.forEach { dataTable ->
            insertDataTable(dataTable)
            dataTable.columnHeaderData.forEach { header ->
                insertColumnHeader(header.copy(registeredTableName = dataTable.applicationTableName))
                header.columnValues.forEach { value ->
                    insertColumnValue(value.copy(registeredTableName = dataTable.registeredTableName))
                }
            }
        }
    }
}
