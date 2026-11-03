/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.shared

import kotlinx.serialization.Serializable
/** One repayment row of a collection sheet. */
@Serializable
data class BulkRepaymentTransactions(

    /** The server id of the loan. */
    var loanId: Int = 0,

    /** The transaction amount. */
    var transactionAmount: Double = 0.0,

    // Optional fields
    /** The account number the payment was made from or to. */
    var accountNumber: String? = null,

    /** The bank number of the payment. */
    var bankNumber: String? = null,

    /** The cheque number of the payment. */
    var checkNumber: String? = null,

    /** The server id of the payment type. */
    var paymentTypeId: Int? = null,

    /** The receipt number of the payment. */
    var receiptNumber: String? = null,

    /** The routing code of the payment. */
    var routingCode: String? = null,
)
