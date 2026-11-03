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

/** Server response to a loan reschedule request. */
@Serializable
data class LoanRescheduleResponse(
    /** The server id of this record. */
    val id: Int,
    /** The server id of the loan. */
    val loanId: Int,
    /** The installment date the reschedule starts from. */
    val rescheduleFromDate: List<Int>? = null,
    /** The reason given for the reschedule. */
    val rescheduleReasonCodeValue: RescheduleReasonOption? = null,
    /** The reschedule request's status. */
    val statusEnum: RescheduleStatus? = null,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: RescheduleTimeline? = null,
)
