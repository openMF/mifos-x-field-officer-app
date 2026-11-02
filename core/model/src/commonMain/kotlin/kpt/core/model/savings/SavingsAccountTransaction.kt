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
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsAccountTransactionEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class SavingsAccountTransaction(
    val id: Int = 0,
    val savingsAccountId: Int? = null,
    val transactionType: SavingsTransactionType? = null,
    val accountId: Int? = null,
    val accountNo: String? = null,
    val savingsTransactionDate: SavingsTransactionDate? = null,
    val date: List<Int?> = emptyList(),
    val currency: SavingAccountCurrency? = null,
    val amount: Double? = null,
    val runningBalance: Double? = null,
    val reversed: Boolean? = null,
)
