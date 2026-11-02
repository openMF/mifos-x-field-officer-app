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

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s
 * `SavingsAccountTransactionRequestEntity`.
 * Same field names as the row; the `core/store/mapping` mappers convert between them.
 */
@Serializable
data class SavingsAccountTransactionRequest(
    val id: Int = 0,
    val savingAccountId: Int? = null,
    val savingsAccountType: String? = null,
    val transactionType: String? = null,
    val dateFormat: String? = null,
    val locale: String? = null,
    val transactionDate: String? = null,
    val transactionAmount: String? = null,
    val paymentTypeId: String? = null,
    val note: String? = null,
    val accountNumber: String? = null,
    val checkNumber: String? = null,
    val routingCode: String? = null,
    val receiptNumber: String? = null,
    val bankNumber: String? = null,
    val errorMessage: String? = null,
)
