/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kotlinx.serialization.Serializable
import kpt.core.model.group.Group
import kpt.core.model.shared.Timeline

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class Client(
    /** The server id of this record. */
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
    val status: ClientStatus? = null,
    /** True when this record is queued to be synced to the server. */
    val sync: Boolean = false,
    /** True when the record is active. */
    val active: Boolean = false,
    /** The client's dates. */
    val clientDate: ClientDate? = null,
    /** The date the record was activated. */
    val activationDate: List<Int?> = emptyList(),
    /** The date of birth. */
    val dateOfBirth: List<Int?> = emptyList(),
    /** The groups. */
    val groups: List<Group>? = emptyList(),
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
    val legalForm: ClientStatus? = null,
    /** The client's gender. */
    val gender: ClientGender? = null,
    /** The client's type. */
    val clientType: ClientType? = null,
    /** The client's classification. */
    val clientClassification: ClientClassification? = null,
)
