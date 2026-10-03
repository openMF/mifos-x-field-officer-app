/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.entity

import kotlinx.serialization.Serializable
import kpt.core.database.payment.entity.PaymentTypeOptionEntity
import kpt.core.model.objects.template.loan.Type

/**
 * Created by Rajan Maurya on 14/02/17.
 */
@Serializable
data class LoanTransactionTemplate(
    val type: Type? = null,

    val date: List<Int> = emptyList(),

    val amount: Double? = null,

    val manuallyReversed: Boolean? = null,

    val possibleNextRepaymentDate: List<Int> = emptyList(),

    val paymentTypeOptions: List<PaymentTypeOptionEntity> = emptyList(),
)
