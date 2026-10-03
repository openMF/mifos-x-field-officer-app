/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.loanDisburse

import kpt.core.model.objects.account.loan.Currency
import kpt.core.model.objects.account.loan.PaymentType

data class LoanDisburseTemplate(
    val netDisbursalAmount: Double,
    val date: List<Int>,
    val currency: Currency,
    val paymentTypeOptions: List<PaymentType>,
)
