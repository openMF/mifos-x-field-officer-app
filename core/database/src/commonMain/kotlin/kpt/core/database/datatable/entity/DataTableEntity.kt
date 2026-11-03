/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.datatable.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
/** Room row for a data table. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "DataTable",
)
data class DataTableEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** The application table the data table extends (`m_client`, `m_loan`, …). */
    val applicationTableName: String? = null,
    /** The table's column headers. */
    val columnHeaderData: List<ColumnHeader> = emptyList(),
    /** The registered name of the data table. */
    val registeredTableName: String? = null,
)
