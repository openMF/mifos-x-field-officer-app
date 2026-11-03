/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.loan

import kotlinx.serialization.Serializable

/** Kind of a loan. */
@Serializable
data class LoanType(
    /** The server id of this record. */
    val id: Int? = null,

    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,

    /** The human-readable label Fineract sends for this value. */
    val value: String? = null,

    /** True for a disbursement transaction. */
    val disbursement: Boolean? = null,

    /** True for a repayment taken at disbursement. */
    val repaymentAtDisbursement: Boolean? = null,

    /** True for a repayment. */
    val repayment: Boolean? = null,

    /** True for a contra transaction. */
    val contra: Boolean? = null,

    /** True for an interest waiver. */
    val waiveInterest: Boolean? = null,

    /** True for a charge waiver. */
    val waiveCharges: Boolean? = null,

    /** True for an accrual transaction. */
    val accrual: Boolean? = null,

    /** True for a write-off. */
    val writeOff: Boolean? = null,

    /** True for a recovery repayment. */
    val recoveryRepayment: Boolean? = null,

    /** True for an initiate-transfer transaction. */
    val initiateTransfer: Boolean? = null,

    /** True for an approve-transfer transaction. */
    val approveTransfer: Boolean? = null,

    /** True for a withdraw-transfer transaction. */
    val withdrawTransfer: Boolean? = null,

    /** True for a reject-transfer transaction. */
    val rejectTransfer: Boolean? = null,

    /** True for a charge-payment transaction. */
    val chargePayment: Boolean? = null,

    /** True for a refund. */
    val refund: Boolean? = null,
)
