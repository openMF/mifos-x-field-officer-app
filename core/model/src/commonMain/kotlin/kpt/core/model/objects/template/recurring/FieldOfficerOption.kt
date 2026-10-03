/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring

import kotlinx.serialization.Serializable
@Serializable
data class FieldOfficerOption(
    val displayName: String? = null,
    val externalId: String? = null,
    val firstname: String? = null,
    val id: Int? = null,
    val isActive: Boolean? = null,
    val isLoanOfficer: Boolean? = null,
    val joiningDate: List<Int>? = null,
    val lastname: String? = null,
    val mobileNo: String? = null,
    val officeId: Int? = null,
    val officeName: String? = null,
)
