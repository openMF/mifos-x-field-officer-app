/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.loan

import kotlinx.serialization.Serializable

/** Request body for a guarantor. */
@Serializable
data class GuarantorRequestDto(
    /** The server id of the guarantor's relationship to the client. */
    val clientRelationshipTypeId: Long,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
    /** The server id of the entity this record belongs to. */
    val entityId: Int?,
    /** The server id of the guarantor type. */
    val guarantorTypeId: Long,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,

    /** The person's first name. */
    val firstname: String?,
    /** The person's last name. */
    val lastname: String?,
    /** The guarantor's date of birth. */
    val dob: String?,
    /** The first address line. */
    val addressLine1: String?,
    /** The second address line. */
    val addressLine2: String?,
    /** The city. */
    val city: String?,
    /** The guarantor's postal code. */
    val zip: String?,
    /** The guarantor's mobile number. */
    val mobileNumber: String?,
    /** The guarantor's home phone number. */
    val housePhoneNumber: String?,
)
