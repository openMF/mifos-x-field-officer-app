/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savings

import kotlinx.serialization.Serializable
import kpt.core.model.payment.PaymentTypeOption

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s
 * `SavingsAccountTransactionTemplateEntity`.
 * Same field names as the row; the `core/store/mapping` mappers convert between them.
 */
@Serializable
data class SavingsAccountTransactionTemplate(
    val accountId: Int? = null,
    val accountNo: String? = null,
    val date: List<Int> = emptyList(),
    val reversed: Boolean? = null,
    val paymentTypeOptions: List<PaymentTypeOption> = emptyList(),
) {

    fun isReversed(): Boolean? {
        return reversed
    }
}
