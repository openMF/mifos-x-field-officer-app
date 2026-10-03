/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.datatable

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.datatable.dao.DataTableDao
import kpt.core.database.datatable.entity.DataTableEntity
import kpt.core.network.mifos.datatable.api.DataTableApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Registered datatables for one application table ("m_client", "m_loan", ...). The key is stamped
 * onto each row on write: it is the column the cache is read back by, and Fineract does not
 * reliably echo it on every row.
 */
@StoreProvider(id = "dataTables", ttl = "24h")
@CacheKey(fn = "forAppTable", key = "dataTables:{appTable}", params = ["appTable:String"])
fun provideDataTableStore(
    service: DataTableApi,
    dao: DataTableDao,
): Store<String, List<DataTableEntity>> = StoreFactory.createStore(
    fetcher = Fetcher.of { appTable: String -> service.getTableOf(appTable) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { appTable: String -> dao.observeForAppTable(appTable) },
        writer = { appTable: String, tables: List<DataTableEntity> ->
            dao.replaceForAppTable(
                appTable = appTable,
                // id = 0 lets Room assign a fresh rowid; carrying the fetched id would collide
                // with the row it is meant to replace.
                tables = tables.map { it.copy(id = 0, applicationTableName = appTable) },
            )
        },
    ),
)
