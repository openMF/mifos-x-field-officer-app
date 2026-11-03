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
import kpt.core.model.datatable.DataTablePayload
import kpt.core.model.objects.clients.Address

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientPayloadEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class ClientPayload(
    /** The server id of this record. */
    val id: Int? = null,
    /** When the client was created offline, in epoch milliseconds. */
    val clientCreationTime: Long? = null,
    /** The error from the last failed sync attempt; null when it has not failed. */
    val errorMessage: String? = null,
    /** The person's first name. */
    val firstname: String? = null,
    /** The person's last name. */
    val lastname: String? = null,
    /** The person's middle name. */
    val middlename: String? = null,
    /** The email address. */
    val emailAddress: String? = null,
    /** The server id of the office. */
    val officeId: Int? = null,
    /** The server id of the staff member (loan officer). */
    val staffId: Int? = null,
    /** The server id of the gender. */
    val genderId: Int? = null,
    /** True when the record is active. */
    val active: Boolean? = null,
    /** The date the record was activated. */
    val activationDate: String? = null,
    /** The date the record was submitted. */
    val submittedOnDate: String? = null,
    /** The date of birth. */
    val dateOfBirth: String? = null,
    /** The mobile phone number. */
    val mobileNo: String? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** The server id of the client type. */
    val clientTypeId: Int? = null,
    /** The server id of the client classification. */
    val clientClassificationId: Int? = null,
    /** The addresses. */
    val address: List<Address>? = emptyList(),
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String? = null,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String? = null,
    /** The data-table entries submitted with the client. */
    val datatables: List<DataTablePayload>? = null,
    /** The server id of the legal form. */
    val legalFormId: Int? = null,
)
