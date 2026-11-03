/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.collectionsheets

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.loan.Currency
/** A loan collection sheet. */
@Serializable
data class LoanCollectionSheet(
    /** The server id of the account. */
    val accountId: String? = null,
    /** The account's Fineract status id. */
    val accountStatusId: Int = 0,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** The interest due. */
    val interestDue: Double? = null,
    /** The interest already paid. */
    val interestPaid: Double? = null,
    /** Principal already collected against today's installment (`principal_completed_derived`). */
    val principalPaid: Double? = null,
    /** The fees paid. */
    val feePaid: Double? = null,
    /** The server id of the loan. */
    val loanId: Int = 0,
    /** The principal due. */
    val principalDue: Double? = null,
    /** The server id of the product. */
    val productId: Double? = null,
    /** The total due. */
    val totalDue: Double = 0.0,
    /** The charges due. */
    val chargesDue: Double = 0.0,
    /** The product's short name. */
    val productShortName: String? = null,
)
