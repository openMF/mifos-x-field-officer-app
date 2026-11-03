/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

import kotlinx.serialization.Serializable
/** A transaction. */
@Serializable
data class Transaction(
    /** The server id of this record. */
    var id: Int? = null,

    /** The server id of the office. */
    var officeId: Int? = null,
    /** The name of the office. */
    var officeName: String? = null,
    /** The record's type. */
    var type: Type? = null,
    /** The date. */
    var date: List<Int> = ArrayList(),
    /** The currency the amounts are in. */
    var currency: Currency? = null,
    /** The payment details of the transaction. */
    var paymentDetailData: PaymentDetailData? = null,
    /** The amount, in the record's currency. */
    var amount: Double? = null,
    /** The amount disbursed after the charges deducted at disbursement. */
    var netDisbursalAmount: Double? = null,
    /** The part of the transaction that went to principal. */
    var principalPortion: Double? = null,
    /** The part of the transaction that went to interest. */
    var interestPortion: Double? = null,
    /** The part of the transaction that went to fees. */
    var feeChargesPortion: Double? = null,
    /** The part of the transaction that went to penalties. */
    var penaltyChargesPortion: Double? = null,
    /** The part of the transaction that overpaid the loan. */
    var overpaymentPortion: Double? = null,
    /** The part of the transaction not yet recognised as income. */
    var unrecognizedIncomePortion: Double? = null,
    /** The loan balance outstanding after the transaction. */
    var outstandingLoanBalance: Double? = null,
    /** The date the record was submitted. */
    var submittedOnDate: List<Int>? = null,
    /** True when the transaction was reversed by hand. */
    var manuallyReversed: Boolean? = null,
)
