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
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.common.utils.MapDeserializer

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

    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    val clientCreationTime: Long? = null,

    val dataTableString: String? = null,

    val registeredTableName: String? = null,

    @Serializable(with = MapDeserializer::class)
    @Contextual
    val data: Map<
        String,
        @Contextual
        Any,
        >,
)
