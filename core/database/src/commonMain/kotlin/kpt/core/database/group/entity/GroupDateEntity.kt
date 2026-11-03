/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.group.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
/** Room row for a group date. */
@Serializable
@DbEntity
@Entity(
    tableName = "GroupDate",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
)
data class GroupDateEntity(
    /** The server id of the group. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val groupId: Long = 0,

    /** The server id of the charge. */
    val chargeId: Long = 0,

    /** The day of the month. */
    val day: Int = 0,

    /** The month (1–12). */
    val month: Int = 0,

    /** The year. */
    val year: Int = 0,
)
