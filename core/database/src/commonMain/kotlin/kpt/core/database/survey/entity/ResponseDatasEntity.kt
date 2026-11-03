/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.survey.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
/** Room row for a survey answer option. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "ResponseDatas",
)
data class ResponseDatasEntity(
    /** The server id of the response. */
    @PrimaryKey(autoGenerate = true)
    val responseId: Int = 0,

    /** The server id of this record. */
    val id: Int = 0,

    /** The text shown for this item. */
    val text: String? = null,

    /** The order this item is shown in. */
    val sequenceNo: Int = 0,

    /** The score this answer carries. */
    val value: Int = 0,
)
