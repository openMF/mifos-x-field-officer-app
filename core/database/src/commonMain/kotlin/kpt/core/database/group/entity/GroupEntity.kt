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

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.client.entity.ClientStatusEntity
import kpt.core.model.shared.Timeline
@DbEntity
@Entity(
    tableName = "GroupTable",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
    // NO foreign keys.
    //
    // This entity used to declare one naming GroupDateEntity as the PARENT of its own `id`, which inverts the
    // relationship: it made every insert conditional on a group's own date block ALREADY existing as a row. Since that
    // detail is an embedded FIELD on this class, no such row is ever written first, so SQLite refused
    // every insert with `787 FOREIGN KEY constraint failed` — and Store5 swallowed the failure, so the
    // screen rendered an empty list rather than an error. Measured on device 2026-10-02: the clients
    // fetch returned 100 rows, the write failed, and the list showed "Nothing here yet".
)
@Serializable
data class GroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    val accountNo: String? = null,

    val sync: Boolean = false,

    val name: String? = null,

    val status: ClientStatusEntity? = null,

    val active: Boolean? = null,

    @ColumnInfo(index = true, name = ColumnInfo.INHERIT_FIELD_NAME, typeAffinity = ColumnInfo.UNDEFINED, collate = ColumnInfo.UNSPECIFIED, defaultValue = ColumnInfo.VALUE_UNSPECIFIED)
    val groupDate: GroupDateEntity? = null,

    val activationDate: List<Int> = emptyList(),

    val officeId: Int? = null,

    val officeName: String? = null,

    val centerId: Int? = 0,

    val centerName: String? = null,

    val staffId: Int? = null,

    val staffName: String? = null,

    val hierarchy: String? = null,

    val groupLevel: Int = 0,

    val timeline: Timeline? = null,

    val externalId: String? = null,
)
