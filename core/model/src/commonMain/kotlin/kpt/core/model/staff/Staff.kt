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
    val id: Int? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val mobileNo: String? = null,
    val displayName: String? = null,
    val officeId: Int? = null,
    val officeName: String? = null,
    val isLoanOfficer: Boolean? = null,
    val isActive: Boolean? = null,
)
