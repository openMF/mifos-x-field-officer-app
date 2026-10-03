/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

@DbEntity
@Entity(
    tableName = "ClientIdentifier",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
)
data class ClientIdentifierEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val id: Int? = null,
    val clientId: Int? = null,
    val documentKey: String? = null,
    val documentTypeName: String? = null,
    val documentTypeId: Int? = null,
    val description: String? = null,
    val status: String? = null,
)
