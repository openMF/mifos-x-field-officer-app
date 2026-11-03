/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
    /** The record's type. */
    val type: Type? = null,

    /** The date. */
    val date: List<Int> = emptyList(),

    /** The amount, in the record's currency. */
    val amount: Double? = null,

    /** True when the transaction was reversed by hand. */
    val manuallyReversed: Boolean? = null,

    /** The earliest date the next repayment can be made. */
    val possibleNextRepaymentDate: List<Int> = emptyList(),

    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentTypeOptionEntity> = emptyList(),
)
