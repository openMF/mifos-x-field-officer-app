/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.datatable.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
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
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    val columnCode: String? = null,

    val columnDisplayType: String? = null,

    val columnLength: Int? = null,

    val dataTableColumnName: String? = null,

    val columnType: String? = null,

    val columnNullable: Boolean? = null,

    val columnPrimaryKey: Boolean? = null,

    val registeredTableName: String? = null,

    val columnValues: List<ColumnValue> = emptyList(),
)
