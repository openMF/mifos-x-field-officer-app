/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
    /** The server id of the account. */
    val accountId: String? = null,

    /** The account's Fineract status id. */
    val accountStatusId: Int = 0,

    /** The currency the amounts are in. */
    val currency: SavingAccountCurrencyEntity? = null,

    /** The kind of deposit account. */
    val depositAccountType: String? = null,

    /** The amount due. */
    val dueAmount: Int = 0,

    /** The server id of the product. */
    val productId: Int = 0,

    /** The name of the product. */
    val productName: String? = null,

    /** The server id of the savings account. */
    val savingsId: Int = 0,
)
