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

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.group.entity.GroupEntity
import kpt.core.model.shared.Timeline
@DbEntity
@Entity(
    tableName = "Client",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
    // NO foreign keys.
    //
    // This entity used to declare one naming ClientDateEntity as the PARENT of its own `id`, which inverts the
    // relationship: it made every insert conditional on a client's own date block ALREADY existing as a row. Since that
    // detail is an embedded FIELD on this class, no such row is ever written first, so SQLite refused
    // every insert with `787 FOREIGN KEY constraint failed` — and Store5 swallowed the failure, so the
    // screen rendered an empty list rather than an error. Measured on device 2026-10-02: the clients
    // fetch returned 100 rows, the write failed, and the list showed "Nothing here yet".
)
@Serializable
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = ColumnInfo.INHERIT_FIELD_NAME, typeAffinity = ColumnInfo.UNDEFINED, collate = ColumnInfo.UNSPECIFIED, defaultValue = ColumnInfo.VALUE_UNSPECIFIED)
    val id: Int = 0,

    val groupId: Int? = 0,

    val groupName: String? = null,

    val accountNo: String? = null,

    val clientId: Int? = null,

    val status: ClientStatusEntity? = null,

    val sync: Boolean = false,

    val active: Boolean = false,

    val clientDate: ClientDateEntity? = null,

    val activationDate: List<Int?> = emptyList(),

    val dateOfBirth: List<Int?> = emptyList(),

    val groups: List<GroupEntity>? = emptyList(),

    val mobileNo: String? = null,

    val firstname: String? = null,

    val middlename: String? = null,

    val lastname: String? = null,

    val displayName: String? = null,

    val officeId: Int = 0,

    val officeName: String? = null,

    val staffId: Int = 0,

    val staffName: String? = null,

    val timeline: Timeline? = null,

    val fullname: String? = null,

    val savingsAccountId: Long? = null,

    val imageId: Int = 0,

    val imagePresent: Boolean = false,

    val externalId: String? = null,

    val emailAddress: String? = null,

    val legalForm: ClientStatusEntity? = null,

    val gender: ClientGenderEntity? = null,

    val clientType: ClientTypeEntity? = null,

    val clientClassification: ClientClassificationEntity? = null,
)
