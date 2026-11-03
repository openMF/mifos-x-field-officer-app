/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.staff

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `StaffEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class Staff(
    /** The server id of this record. */
    val id: Int? = null,
    /** The person's first name. */
    val firstname: String? = null,
    /** The person's last name. */
    val lastname: String? = null,
    /** The mobile phone number. */
    val mobileNo: String? = null,
    /** The display name. */
    val displayName: String? = null,
    /** The server id of the office. */
    val officeId: Int? = null,
    /** The name of the office. */
    val officeName: String? = null,
    /** True when the staff member is a loan officer. */
    val isLoanOfficer: Boolean? = null,
    /** True when the record is active. */
    val isActive: Boolean? = null,
)
