/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring

import kotlinx.serialization.Serializable
/** One field officer choice offered by a template. */
@Serializable
data class FieldOfficerOption(
    /** The display name. */
    val displayName: String? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** The person's first name. */
    val firstname: String? = null,
    /** The server id of this record. */
    val id: Int? = null,
    /** True when the record is active. */
    val isActive: Boolean? = null,
    /** True when the staff member is a loan officer. */
    val isLoanOfficer: Boolean? = null,
    /** The date the staff member joined. */
    val joiningDate: List<Int>? = null,
    /** The person's last name. */
    val lastname: String? = null,
    /** The mobile phone number. */
    val mobileNo: String? = null,
    /** The server id of the office. */
    val officeId: Int? = null,
    /** The name of the office. */
    val officeName: String? = null,
)
