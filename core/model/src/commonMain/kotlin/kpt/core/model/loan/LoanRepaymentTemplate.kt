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
    val loanId: Int = 0,
    val type: LoanType? = null,
    val date: List<Int>? = null,
    val currency: SavingAccountCurrency? = null,
    val amount: Double? = null,
    val principalPortion: Double? = null,
    val interestPortion: Double? = null,
    val feeChargesPortion: Double? = null,
    val penaltyChargesPortion: Double? = null,
    val paymentTypeOptions: List<PaymentTypeOption>? = null,
)
