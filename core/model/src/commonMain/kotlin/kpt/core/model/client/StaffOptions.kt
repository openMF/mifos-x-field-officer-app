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
    val id: Int = 0,
    val firstname: String = "",
    val lastname: String = "",
    val displayName: String = "",
    val officeId: Int = 0,
    val officeName: String = "",
    val isLoanOfficer: Boolean = false,
    val isActive: Boolean = false,
)
