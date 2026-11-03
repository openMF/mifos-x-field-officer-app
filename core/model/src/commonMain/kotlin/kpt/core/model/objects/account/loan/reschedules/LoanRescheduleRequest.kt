/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.reschedules

import kotlinx.serialization.Serializable

/** Request body for a loan reschedule. */
@Serializable
data class LoanRescheduleRequest(
    /** The server id of the loan. */
    val loanId: Int,
    /** The installment date the reschedule starts from. */
    val rescheduleFromDate: String,
    /** The server id of the reschedule reason. */
    val rescheduleReasonId: Int,
    /** The date the record was submitted. */
    val submittedOnDate: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The number of installments with no principal due, after the reschedule. */
    val graceOnPrincipal: String? = null,
    /** The number of installments with no interest due, after the reschedule. */
    val graceOnInterest: String? = null,
    /** The number of installments added by the reschedule. */
    val extraTerms: String? = null,
    /** The new due date for the installment. */
    val adjustedDueDate: String? = null,
    /** The interest rate after the reschedule. */
    val newInterestRate: String? = null,
    /** The comment on the reschedule. */
    val rescheduleReasonComment: String? = null,
    /** True to waive penalties on the rescheduled installments. */
    val waivePenalties: Boolean? = null,
)
