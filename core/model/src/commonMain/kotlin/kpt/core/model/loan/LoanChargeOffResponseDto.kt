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

/** Server response to a loan charge off request. */
@Serializable
data class LoanChargeOffResponseDto(
    /** The server id of the office. */
    val officeId: Int,
    /** The server id of the client. */
    val clientId: Int,
    /** The server id of the loan. */
    val loanId: Int,
    /** The server id of the resource the request created or changed. */
    val resourceId: Int,
    /** The fields the request changed, as Fineract reports them. */
    val changes: LoanChargeOffChanges,
)

/** A loan charge off changes. */
@Serializable
data class LoanChargeOffChanges(
    /** The date of the transaction. */
    val transactionDate: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
    /** The server id of the charge-off reason. */
    val chargeOffReasonId: Int,
)
