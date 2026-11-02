/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.pathtracking

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.objects.users.UserLocation
import kpt.core.network.mifos.datatable.api.DataTableApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * The paths recorded for one user — the `m_staff_path_tracking` datatable the original's
 * `PathTrackingRepositoryImp` read (`6b66e8a43`). Offline-first through the shared read cache.
 */
@StoreProvider(id = "userPathTracking", ttl = "15m")
@CacheKey(fn = "forKey", key = "userPathTracking:{key}", params = ["key:String"])
fun provideUserPathTrackingStore(
    dataTableApi: DataTableApi,
    cache: ApiResponseCacheDao,
): Store<Int, List<UserLocation>> = cachedRead(
    dao = cache,
    keyOf = { userId -> AppCacheKeys.UserPathTracking.forKey("$userId") },
    fetch = { userId -> dataTableApi.getUserPathTracking(userId) },
)
