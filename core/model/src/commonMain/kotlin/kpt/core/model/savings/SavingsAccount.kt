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
    val id: Int? = null,
    val clientId: Long = 0,
    val groupId: Long = 0,
    val centerId: Long = 0,
    val accountNo: String? = null,
    val productId: Int? = null,
    val shortProductName: String? = null,
    val productName: String? = null,
    val status: SavingsAccountStatus? = null,
    val currency: SavingAccountCurrency? = null,
    val accountBalance: Double? = null,
    val depositType: SavingAccountDepositType? = null,
    val lastActiveTransactionDate: List<Int>? = null,
)
