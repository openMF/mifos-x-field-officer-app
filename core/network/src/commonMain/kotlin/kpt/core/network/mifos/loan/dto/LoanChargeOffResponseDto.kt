/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoanChargeOffResponseDto(
    val officeId: Int,
    val clientId: Int,
    val loanId: Int,
    val resourceId: Int,
    val changes: LoanChargeOffChanges,
)

@Serializable
data class LoanChargeOffChanges(
    val transactionDate: String,
    val locale: String,
    val dateFormat: String,
    val chargeOffReasonId: Int,
)
