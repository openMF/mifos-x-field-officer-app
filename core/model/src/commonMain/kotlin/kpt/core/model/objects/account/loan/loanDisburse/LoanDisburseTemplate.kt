/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.loanDisburse

import kpt.core.model.objects.account.loan.Currency
import kpt.core.model.objects.account.loan.PaymentType

/** Server template for a loan disburse: the options its form offers. */
data class LoanDisburseTemplate(
    /** The amount disbursed after the charges deducted at disbursement. */
    val netDisbursalAmount: Double,
    /** The date. */
    val date: List<Int>,
    /** The currency the amounts are in. */
    val currency: Currency,
    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentType>,
)
