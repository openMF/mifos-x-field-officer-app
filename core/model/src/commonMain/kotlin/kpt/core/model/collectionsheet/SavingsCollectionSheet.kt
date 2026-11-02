/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.collectionsheet

import kotlinx.serialization.Serializable
import kpt.core.model.savings.SavingAccountCurrency

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsCollectionSheet`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class SavingsCollectionSheet(
    val accountId: String? = null,
    val accountStatusId: Int = 0,
    val currency: SavingAccountCurrency? = null,
    val depositAccountType: String? = null,
    val dueAmount: Int = 0,
    val productId: Int = 0,
    val productName: String? = null,
    val savingsId: Int = 0,
)
