/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.office.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity

@DbEntity
@Entity(
    tableName = "Office",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
    foreignKeys = [],
)
@Serializable
data class OfficeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val externalId: String? = null,

    val name: String? = null,

    val nameDecorated: String? = null,

    val officeOpeningDate: OfficeOpeningDateEntity? = null,

    val openingDate: List<Int?> = emptyList(),
)
