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
/** One allowed value of a data-table column. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "ColumnValue",
)
data class ColumnValue(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    /** The value stored in this cell. */
    val value: String? = null,
    /** The score attached to this value. */
    val score: Int? = null,
    /** The registered name of the data table. */
    val registeredTableName: String? = null,
)
