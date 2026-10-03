/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.reschedules

import kotlinx.serialization.Serializable

@Serializable
data class LoanRescheduleRequest(
    val loanId: Int,
    val rescheduleFromDate: String,
    val rescheduleReasonId: Int,
    val submittedOnDate: String,
    val dateFormat: String,
    val locale: String,
    val graceOnPrincipal: String? = null,
    val graceOnInterest: String? = null,
    val extraTerms: String? = null,
    val adjustedDueDate: String? = null,
    val newInterestRate: String? = null,
    val rescheduleReasonComment: String? = null,
    val waivePenalties: Boolean? = null,
)
