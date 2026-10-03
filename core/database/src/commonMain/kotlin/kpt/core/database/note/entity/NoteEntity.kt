/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.note.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "Note",
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    val clientId: Long? = null,

    val noteContent: String? = null,

    val createdById: Long? = null,

    val createdByUsername: String? = null,

    val createdOn: String? = null,

    val updatedById: Long? = null,

    val updatedByUsername: String? = null,

    val updatedOn: String? = null,
)
