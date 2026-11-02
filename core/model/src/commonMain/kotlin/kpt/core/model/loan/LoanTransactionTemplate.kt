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
import kpt.core.model.objects.template.loan.Type
import kpt.core.model.payment.PaymentTypeOption

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanTransactionTemplate`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class LoanTransactionTemplate(
    val type: Type? = null,
    val date: List<Int> = emptyList(),
    val amount: Double? = null,
    val manuallyReversed: Boolean? = null,
    val possibleNextRepaymentDate: List<Int> = emptyList(),
    val paymentTypeOptions: List<PaymentTypeOption> = emptyList(),
)
