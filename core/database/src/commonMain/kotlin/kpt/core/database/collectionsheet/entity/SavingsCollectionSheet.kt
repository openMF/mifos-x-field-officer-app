/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.collectionsheet.entity

import kotlinx.serialization.Serializable
import kpt.core.database.savings.entity.SavingAccountCurrencyEntity

/**
 * Created by Tarun on 31-07-17.
 */
@Serializable
data class SavingsCollectionSheet(
    // The accountId is of String type only. It's not a mistake.
    val accountId: String? = null,

    val accountStatusId: Int = 0,

    val currency: SavingAccountCurrencyEntity? = null,

    val depositAccountType: String? = null,

    val dueAmount: Int = 0,

    val productId: Int = 0,

    val productName: String? = null,

    val savingsId: Int = 0,
)
