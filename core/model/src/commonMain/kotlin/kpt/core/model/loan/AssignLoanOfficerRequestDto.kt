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

/** Request body for an assign loan officer. */
@Serializable
data class AssignLoanOfficerRequestDto(
    /** The date the loan officer is assigned from. */
    val assignmentDate: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The server id of the current loan officer. */
    val fromLoanOfficerId: String?,
    /** The server id of the new loan officer. */
    val toLoanOfficerId: Int,
)
