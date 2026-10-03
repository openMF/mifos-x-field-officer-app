/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.group.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "GroupPayload",
)
@Serializable
data class GroupPayloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val errorMessage: String? = null,

    val officeId: Int = 0,

    val active: Boolean = false,

    val activationDate: String? = null,

    val submittedOnDate: String? = null,

    val externalId: String? = null,

    val name: String? = null,

    val locale: String? = null,

    val dateFormat: String? = null,
)
