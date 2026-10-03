/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.collectionsheet

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.center.entity.CenterWithAssociations
import kpt.core.network.mifos.collectionsheet.api.CollectionSheetApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for collectionsheet with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

@StoreProvider(id = "collectionsheetFetchGroupsAssociatedWithCenter", ttl = "12h")
@CacheKey(fn = "forKey", key = "collectionsheetFetchGroupsAssociatedWithCenter:{key}", params = ["key:String"])
fun provideFetchGroupsAssociatedWithCenterStore(
    collectionSheetApi: CollectionSheetApi,
    cache: ApiResponseCacheDao,
): Store<Int, CenterWithAssociations> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CollectionsheetFetchGroupsAssociatedWithCenter.forKey(key.toString()) },
    fetch = { key -> collectionSheetApi.fetchGroupsAssociatedWithCenter(centerId = key) },
)
