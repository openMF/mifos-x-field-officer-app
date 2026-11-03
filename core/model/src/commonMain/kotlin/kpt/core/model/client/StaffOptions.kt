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

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `StaffOptionsEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class StaffOptions(
    /** The server id of this record. */
    val id: Int = 0,
    /** The person's first name. */
    val firstname: String = "",
    /** The person's last name. */
    val lastname: String = "",
    /** The display name. */
    val displayName: String = "",
    /** The server id of the office. */
    val officeId: Int = 0,
    /** The name of the office. */
    val officeName: String = "",
    /** True when the staff member is a loan officer. */
    val isLoanOfficer: Boolean = false,
    /** True when the record is active. */
    val isActive: Boolean = false,
)
