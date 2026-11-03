/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.databaseobjects

import kotlinx.serialization.Serializable
/** A loan. */
@Serializable
data class Loan(
    /** The amount to disburse. */
    var disbursementAmount: Double = 0.0,

    /** The interest due. */
    var interestDue: Double = 0.0,

    /** The interest already paid. */
    var interestPaid: Double = 0.0,

    /** The server id of the loan. */
    var loanId: Int = 0,

    /** The charges due. */
    var chargesDue: Double = 0.0,

    /** The total due. */
    var totalDue: Double = 0.0,

    /** The principal due. */
    var principalDue: Double = 0.0,

    /** The principal already paid. */
    var principalPaid: Double = 0.0,

    /** The server id of the account. */
    var accountId: String? = null,

    /** The account's Fineract status id. */
    var accountStatusId: Int = 0,

    /** The product's short name. */
    var productShortName: String? = null,

    /** The server id of the product. */
    var productId: Int = 0,

    /** The currency the amounts are in. */
    var currency: Currency? = null,

    /** The client. */
    var client: Client? = null,

    /** Whether the payment amount was changed from the due amount. */
    var isPaymentChanged: String? = null,
)
