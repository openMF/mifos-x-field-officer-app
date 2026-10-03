/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable
import kpt.core.model.shared.CurrencyDto
import kpt.core.model.shared.PaymentTypeOptionDto

@Serializable
data class LoanDisburseTemplateDto(
    val netDisbursalAmount: Double,
    val date: List<Int>,
    val currency: CurrencyDto? = null,
    val paymentTypeOptions: List<PaymentTypeOptionDto> = emptyList(),
)
