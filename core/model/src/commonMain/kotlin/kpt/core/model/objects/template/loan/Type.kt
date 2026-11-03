/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/** A Fineract code value: an id, a code and its display text. */
@Serializable
data class Type(
    /** The server id of this record. */
    var id: Int? = null,

    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    var code: String? = null,

    /** The human-readable label Fineract sends for this value. */
    var value: String? = null,

    /** True for a disbursement transaction. */
    var disbursement: Boolean? = null,

    /** True for a repayment taken at disbursement. */
    var repaymentAtDisbursement: Boolean? = null,

    /** True for a repayment. */
    var repayment: Boolean? = null,

    /** True for a contra transaction. */
    var contra: Boolean? = null,

    /** True for an interest waiver. */
    var waiveInterest: Boolean? = null,

    /** True for a charge waiver. */
    var waiveCharges: Boolean? = null,

    /** True for an accrual transaction. */
    var accrual: Boolean? = null,

    /** True for a write-off. */
    var writeOff: Boolean? = null,

    /** True for a recovery repayment. */
    var recoveryRepayment: Boolean? = null,

    /** True for an initiate-transfer transaction. */
    var initiateTransfer: Boolean? = null,

    /** True for an approve-transfer transaction. */
    var approveTransfer: Boolean? = null,

    /** True for a withdraw-transfer transaction. */
    var withdrawTransfer: Boolean? = null,

    /** True for a reject-transfer transaction. */
    var rejectTransfer: Boolean? = null,

    /** True for a charge-payment transaction. */
    var chargePayment: Boolean? = null,

    /** True for a refund. */
    var refund: Boolean? = null,
)
