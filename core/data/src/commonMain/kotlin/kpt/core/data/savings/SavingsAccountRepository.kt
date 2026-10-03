/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.savings

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.database.savings.entity.SavingsAccountWithAssociationsEntity

interface SavingsAccountRepository {
    /**
     * One savings account with its transactions.
     *
     * [savingsAccountType] is a Fineract path segment ("savingsaccounts",
     * "recurringdepositaccounts", …), so the same numeric id means different accounts under
     * different types — both are required to address one.
     */
    fun savingsAccountStream(
        savingsAccountType: String,
        savingsAccountId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<SavingsAccountWithAssociationsEntity>
}
