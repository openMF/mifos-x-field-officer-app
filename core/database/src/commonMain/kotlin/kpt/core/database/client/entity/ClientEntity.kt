/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.group.entity.GroupEntity
import kpt.core.model.shared.Timeline
/** Room row for a client. */
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
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val id: Int = 0,

    /** The server id of the group. */
    val groupId: Int? = 0,

    /** The name of the group. */
    val groupName: String? = null,

    /** The account number Fineract assigned. */
    val accountNo: String? = null,

    /** The server id of the client. */
    val clientId: Int? = null,

    /** The record's Fineract status. */
    val status: ClientStatusEntity? = null,

    /** True when this record is queued to be synced to the server. */
    val sync: Boolean = false,

    /** True when the record is active. */
    val active: Boolean = false,

    /** The client's dates. */
    val clientDate: ClientDateEntity? = null,

    /** The date the record was activated. */
    val activationDate: List<Int?> = emptyList(),

    /** The date of birth. */
    val dateOfBirth: List<Int?> = emptyList(),

    /** The groups. */
    val groups: List<GroupEntity>? = emptyList(),

    /** The mobile phone number. */
    val mobileNo: String? = null,

    /** The person's first name. */
    val firstname: String? = null,

    /** The person's middle name. */
    val middlename: String? = null,

    /** The person's last name. */
    val lastname: String? = null,

    /** The display name. */
    val displayName: String? = null,

    /** The server id of the office. */
    val officeId: Int = 0,

    /** The name of the office. */
    val officeName: String? = null,

    /** The server id of the staff member (loan officer). */
    val staffId: Int = 0,

    /** The staff member's display name. */
    val staffName: String? = null,

    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: Timeline? = null,

    /** The full name. */
    val fullname: String? = null,

    /** The server id of the savings account. */
    val savingsAccountId: Long? = null,

    /** The server id of the client's photo. */
    val imageId: Int = 0,

    /** True when the client has a photo. */
    val imagePresent: Boolean = false,

    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,

    /** The email address. */
    val emailAddress: String? = null,

    /** The client's legal form (person or entity). */
    val legalForm: ClientStatusEntity? = null,

    /** The client's gender. */
    val gender: ClientGenderEntity? = null,

    /** The client's type. */
    val clientType: ClientTypeEntity? = null,

    /** The client's classification. */
    val clientClassification: ClientClassificationEntity? = null,
)
