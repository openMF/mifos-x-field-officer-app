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

import kpt.core.model.utils.DateConstants

data class LoanDisburseInput(
    val actualDisbursementDate: String,
    val transactionAmount: Double,
    val paymentTypeId: Int? = null,
    val note: String? = null,
    val externalId: Int? = null,
    val accountNumber: String? = null,
    val checkNumber: String? = null,
    val routingCode: String? = null,
    val receiptNumber: String? = null,
    val bankNumber: String? = null,
    val locale: String = DateConstants.LOCALE,
    val dateFormat: String = "dd-MM-yyyy",
)
