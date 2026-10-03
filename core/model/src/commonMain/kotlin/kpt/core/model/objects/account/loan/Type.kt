/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

import kotlinx.serialization.Serializable
@Serializable
data class Type(
    var id: Int? = null,

    var code: String? = null,

    var value: String? = null,

    var disbursement: Boolean? = null,

    var repaymentAtDisbursement: Boolean? = null,

    var repayment: Boolean? = null,

    var contra: Boolean? = null,

    var waiveInterest: Boolean? = null,

    var waiveCharges: Boolean? = null,

    var accrual: Boolean? = null,

    var writeOff: Boolean? = null,

    var recoveryRepayment: Boolean? = null,

    var initiateTransfer: Boolean? = null,

    var approveTransfer: Boolean? = null,

    var withdrawTransfer: Boolean? = null,

    var rejectTransfer: Boolean? = null,

    var chargePayment: Boolean? = null,

    var refund: Boolean? = null,

    var reAge: Boolean? = null,
    var reAmortize: Boolean? = null,
    var merchantIssuedRefund: Boolean? = null,
    var payoutRefund: Boolean? = null,
    var goodwillCredit: Boolean? = null,
    var interestPaymentWaiver: Boolean? = null,
    var creditBalanceRefund: Boolean? = null,
    var contractTermination: Boolean? = null,
    var chargeoff: Boolean? = null,
    var buyDownFee: Boolean? = null,
    var capitalizedIncome: Boolean? = null,
)
