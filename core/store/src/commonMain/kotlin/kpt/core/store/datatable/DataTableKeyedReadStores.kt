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
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.objects.users.UserLocation
import kpt.core.network.mifos.datatable.api.DataTableApi
import kpt.core.network.mifos.datatable.dto.GetDataTablesResponse
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for datatable with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `getDatatables` read. */
data class GetDatatablesKey(
    val apptable: String? = null,
)

@StoreProvider(id = "datatableGetDatatables", ttl = "12h")
@CacheKey(fn = "forKey", key = "datatableGetDatatables:{key}", params = ["key:String"])
fun provideGetDatatablesStore(
    dataTableApi: DataTableApi,
    cache: ApiResponseCacheDao,
): Store<GetDatatablesKey, List<GetDataTablesResponse>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.DatatableGetDatatables.forKey("${key.apptable}") },
    fetch = { key -> dataTableApi.getDatatables(apptable = key.apptable) },
)

@StoreProvider(id = "datatableGetUserPathTracking", ttl = "12h")
@CacheKey(fn = "forKey", key = "datatableGetUserPathTracking:{key}", params = ["key:String"])
fun provideGetUserPathTrackingStore(
    dataTableApi: DataTableApi,
    cache: ApiResponseCacheDao,
): Store<Int, List<UserLocation>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.DatatableGetUserPathTracking.forKey(key.toString()) },
    fetch = { key -> dataTableApi.getUserPathTracking(userId = key) },
)
