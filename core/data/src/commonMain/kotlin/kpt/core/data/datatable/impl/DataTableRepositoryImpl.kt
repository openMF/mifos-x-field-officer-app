/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.datatable.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonArray
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.datatable.DataTableRepository
import kpt.core.model.datatable.DataTable
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import kpt.core.store.datatable.DataTableDataKey
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = DataTableRepository::class)
internal class DataTableRepositoryImpl(
    @FromStore(AppStoreIds.DataTables) private val store: Store<String, List<DataTable>>,
    @FromStore(AppStoreIds.DatatableGetDataOfDataTable)
    private val dataStore: Store<DataTableDataKey, JsonArray>,
) : DataTableRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
    // until something HAS been fetched, so on a clean install nothing ever loads. Room is still
    // served first, so the offline-first behaviour is unchanged. The ttl stays the declared
    // freshness bound — it drives the "updated N ago" indicator, never whether cache is served.
    override fun dataTablesStream(appTable: String, scope: CoroutineScope): ScreenDataStream<List<DataTable>> =
        store.asScreenStream(
            key = appTable,
            cacheKey = AppCacheKeys.DataTables.forAppTable(appTable),
            scope = scope,
            // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.DATA_TABLES,
        )

    override fun dataTableDataStream(
        tableName: String,
        entityId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<JsonArray> = dataStore.asScreenStream(
        key = DataTableDataKey(tableName, entityId),
        cacheKey = AppCacheKeys.DatatableGetDataOfDataTable.forKey("$tableName:$entityId"),
        scope = scope,
        // An entity with no rows in this datatable is the common case — the officer has simply not
        // filled it in — so it is an EMPTY state, not an error.
        // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
        fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        ttl = AppStoreRegistry.Ttl.DATATABLE_GET_DATA_OF_DATA_TABLE,
    )
}
