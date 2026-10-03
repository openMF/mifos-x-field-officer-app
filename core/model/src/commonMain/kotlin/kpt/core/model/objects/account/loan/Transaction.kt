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
data class Transaction(
    var id: Int? = null,

    var officeId: Int? = null,
    var officeName: String? = null,
    var type: Type? = null,
    var date: List<Int> = ArrayList(),
    var currency: Currency? = null,
    var paymentDetailData: PaymentDetailData? = null,
    var amount: Double? = null,
    var netDisbursalAmount: Double? = null,
    var principalPortion: Double? = null,
    var interestPortion: Double? = null,
    var feeChargesPortion: Double? = null,
    var penaltyChargesPortion: Double? = null,
    var overpaymentPortion: Double? = null,
    var unrecognizedIncomePortion: Double? = null,
    var outstandingLoanBalance: Double? = null,
    var submittedOnDate: List<Int>? = null,
    var manuallyReversed: Boolean? = null,
)
