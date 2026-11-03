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
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsAccountEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class SavingsAccount(
    /** The server id of this record. */
    val id: Int? = null,
    /** The server id of the client. */
    val clientId: Long = 0,
    /** The server id of the group. */
    val groupId: Long = 0,
    /** The server id of the centre. */
    val centerId: Long = 0,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The server id of the product. */
    val productId: Int? = null,
    /** The product's short name. */
    val shortProductName: String? = null,
    /** The name of the product. */
    val productName: String? = null,
    /** The record's Fineract status. */
    val status: SavingsAccountStatus? = null,
    /** The currency the amounts are in. */
    val currency: SavingAccountCurrency? = null,
    /** The account balance. */
    val accountBalance: Double? = null,
    /** The deposit product type (savings, fixed deposit or recurring deposit). */
    val depositType: SavingAccountDepositType? = null,
    /** The date of the last active transaction. */
    val lastActiveTransactionDate: List<Int>? = null,
)
