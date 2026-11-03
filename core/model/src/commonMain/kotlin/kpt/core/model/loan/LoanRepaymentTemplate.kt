/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.loan

import kotlinx.serialization.Serializable
import kpt.core.model.payment.PaymentTypeOption
import kpt.core.model.savings.SavingAccountCurrency

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanRepaymentTemplateEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class LoanRepaymentTemplate(
    /** The server id of the loan. */
    val loanId: Int = 0,
    /** The record's type. */
    val type: LoanType? = null,
    /** The date. */
    val date: List<Int>? = null,
    /** The currency the amounts are in. */
    val currency: SavingAccountCurrency? = null,
    /** The amount, in the record's currency. */
    val amount: Double? = null,
    /** The part of the transaction that went to principal. */
    val principalPortion: Double? = null,
    /** The part of the transaction that went to interest. */
    val interestPortion: Double? = null,
    /** The part of the transaction that went to fees. */
    val feeChargesPortion: Double? = null,
    /** The part of the transaction that went to penalties. */
    val penaltyChargesPortion: Double? = null,
    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentTypeOption>? = null,
)
