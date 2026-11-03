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
/** One column of a data table. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "ColumnHeader",
)
data class ColumnHeader(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    /** The code-value list the column takes values from. */
    val columnCode: String? = null,

    /** How the column is displayed (text, date, code lookup, …). */
    val columnDisplayType: String? = null,

    /** The column's maximum length. */
    val columnLength: Int? = null,

    /** The column's name inside the data table. */
    val dataTableColumnName: String? = null,

    /** The column's database type. */
    val columnType: String? = null,

    /** True when the column may be empty. */
    val columnNullable: Boolean? = null,

    /** True when the column is the primary key. */
    val columnPrimaryKey: Boolean? = null,

    /** The registered name of the data table. */
    val registeredTableName: String? = null,

    /** The values the column may take. */
    val columnValues: List<ColumnValue> = emptyList(),
)
