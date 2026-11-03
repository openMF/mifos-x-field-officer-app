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
/** A payment detail data. */
@Serializable
data class PaymentDetailData(
    /** The server id of this record. */
    var id: Int? = null,

    /** The payment type. */
    var paymentType: PaymentType? = null,

    /** The account number the payment was made from or to. */
    var accountNumber: String? = null,

    /** The cheque number of the payment. */
    var checkNumber: String? = null,

    /** The routing code of the payment. */
    var routingCode: String? = null,

    /** The receipt number of the payment. */
    var receiptNumber: String? = null,

    /** The bank number of the payment. */
    var bankNumber: String? = null,
)
