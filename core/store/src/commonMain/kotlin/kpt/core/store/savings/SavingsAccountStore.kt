/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.savings

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.savings.dao.SavingsDao
import kpt.core.database.savings.entity.SavingsAccountWithAssociationsEntity
import kpt.core.network.mifos.savings.api.SavingsAccountApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * `savingsAccountType` is a path segment on Fineract ("savingsaccounts" / "recurringdepositaccounts"
 * / ...), so the same numeric id means different accounts under different types — it belongs in the
 * key, not as a caller-side constant.
 */
data class SavingsAccountKey(
    val savingsAccountType: String,
    val savingsAccountId: Int,
    val associations: String? = "transactions",
)

@StoreProvider(id = "savingsAccounts", ttl = "15m")
@CacheKey(
    fn = "forAccount",
    key = "savingsAccounts:{savingsAccountType}:{savingsAccountId}",
    params = ["savingsAccountType:String", "savingsAccountId:Int"],
)
fun provideSavingsAccountStore(
    service: SavingsAccountApi,
    dao: SavingsDao,
): Store<SavingsAccountKey, SavingsAccountWithAssociationsEntity> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: SavingsAccountKey ->
        service.getSavingsAccountWithAssociations(
            savingsAccountType = key.savingsAccountType,
            savingsAccountId = key.savingsAccountId,
            association = key.associations,
        )
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: SavingsAccountKey -> dao.getSavingsAccountWithAssociations(key.savingsAccountId) },
        writer = { _: SavingsAccountKey, account: SavingsAccountWithAssociationsEntity ->
            dao.insertSavingsAccountWithAssociations(account)
        },
    ),
)
