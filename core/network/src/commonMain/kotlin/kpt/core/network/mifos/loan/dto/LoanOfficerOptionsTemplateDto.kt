/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable

/** Server template for assigning a loan officer: the officers on offer. */
@Serializable
data class LoanOfficerOptionsTemplateDto(
    /** The loan officers that may be assigned. */
    val loanOfficerOptions: List<LoanOfficerOptionDto> = emptyList(),
)

/** One loan officer choice offered by a template. */
@Serializable
data class LoanOfficerOptionDto(
    /** The server id of this record. */
    val id: Int,
    /** The person's first name. */
    val firstname: String? = null,
    /** The person's last name. */
    val lastname: String? = null,
    /** The display name. */
    val displayName: String? = null,
    /** The mobile phone number. */
    val mobileNo: String? = null,
    /** The server id of the office. */
    val officeId: Int? = null,
    /** The name of the office. */
    val officeName: String? = null,
    /** True when the staff member is a loan officer. */
    val isLoanOfficer: Boolean? = null,
    /** True when the record is active. */
    val isActive: Boolean? = null,
)
