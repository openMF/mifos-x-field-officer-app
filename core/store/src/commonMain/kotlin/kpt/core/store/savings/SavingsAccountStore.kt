/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.savings

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.savings.dao.SavingsDao
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.core.network.mifos.savings.api.SavingsAccountApi
import kpt.core.store.mapping.toEntity
import kpt.core.store.mapping.toModel
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

@StoreProvider(id = "savingsAccounts", ttl = "15m")
@CacheKey(
    fn = "forAccount",
    key = "savingsAccounts:{savingsAccountType}:{savingsAccountId}",
    params = ["savingsAccountType:String", "savingsAccountId:Int"],
)
fun provideSavingsAccountStore(
    service: SavingsAccountApi,
    dao: SavingsDao,
): Store<SavingsAccountKey, SavingsAccountWithAssociations> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: SavingsAccountKey ->
        service.getSavingsAccountWithAssociations(
            savingsAccountType = key.savingsAccountType,
            savingsAccountId = key.savingsAccountId,
            association = key.associations,
        ).let { account ->
            // A group account has no client: its owner's name is `groupName`, and the summary's header
            // read "Client name unavailable" (device, 2026-10-25). Folded in here so the cached row keeps it.
            account.copy(clientName = account.clientName ?: account.groupName)
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: SavingsAccountKey ->
            dao.getSavingsAccountWithAssociations(key.savingsAccountId).map { it?.toModel() }
        },
        writer = { _: SavingsAccountKey, account: SavingsAccountWithAssociations ->
            dao.insertSavingsAccountWithAssociations(account.toEntity())
        },
        delete = { key: SavingsAccountKey -> dao.deleteSavingsAccountWithAssociations(key.savingsAccountId) },
        // What the logout purge (`Store.clear()`) reaches the database through; without it an
        // officer's account balances and transactions outlived the session that loaded them.
        deleteAll = { dao.deleteAllSavingsAccountsWithAssociations() },
    ),
)
