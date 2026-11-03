/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable
import kpt.core.model.shared.CurrencyDto
import kpt.core.model.shared.PaymentTypeOptionDto

/** Server template for a loan disburse: the options its form offers. */
@Serializable
data class LoanDisburseTemplateDto(
    /** The amount disbursed after the charges deducted at disbursement. */
    val netDisbursalAmount: Double,
    /** The date. */
    val date: List<Int>,
    /** The currency the amounts are in. */
    val currency: CurrencyDto? = null,
    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentTypeOptionDto> = emptyList(),
)
