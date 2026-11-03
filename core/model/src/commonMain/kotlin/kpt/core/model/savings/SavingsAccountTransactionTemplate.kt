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
    /** The server id of the account. */
    val accountId: Int? = null,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The date. */
    val date: List<Int> = emptyList(),
    /** True when the transaction was reversed. */
    val reversed: Boolean? = null,
    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentTypeOption> = emptyList(),
) {

    /** True when the transaction was reversed; null when the server did not say. */
    fun isReversed(): Boolean? {
        return reversed
    }
}
