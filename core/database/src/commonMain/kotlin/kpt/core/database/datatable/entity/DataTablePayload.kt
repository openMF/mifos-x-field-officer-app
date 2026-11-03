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
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.common.utils.MapDeserializer

/** Request body for a data-table row. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "DataTablePayload",
)
data class DataTablePayload(

    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    /** When the client was created offline, in epoch milliseconds. */
    val clientCreationTime: Long? = null,

    /** The data-table values, serialised as JSON. */
    val dataTableString: String? = null,

    /** The registered name of the data table. */
    val registeredTableName: String? = null,

    /** The data. */
    @Serializable(with = MapDeserializer::class)
    @Contextual
    val data: Map<
        String,
        @Contextual
        Any,
        >,
)
