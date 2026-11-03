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
    /** The server id of this record. */
    val id: Int = 0,
    /** The server id of the savings account. */
    val savingsAccountId: Int? = null,
    /** The transaction type. */
    val transactionType: SavingsTransactionType? = null,
    /** The server id of the account. */
    val accountId: Int? = null,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The date of the savings transaction. */
    val savingsTransactionDate: SavingsTransactionDate? = null,
    /** The date. */
    val date: List<Int?> = emptyList(),
    /** The currency the amounts are in. */
    val currency: SavingAccountCurrency? = null,
    /** The amount, in the record's currency. */
    val amount: Double? = null,
    /** The balance after the transaction. */
    val runningBalance: Double? = null,
    /** True when the transaction was reversed. */
    val reversed: Boolean? = null,
)
