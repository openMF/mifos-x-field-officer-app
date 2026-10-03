/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.search

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.objects.SearchedEntity
import kpt.core.network.mifos.search.api.SearchApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for search with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `searchResources` read. */
data class SearchResourcesKey(
    val query: String,
    val resource: String?,
    val exactMatch: Boolean?,
)

@StoreProvider(id = "searchSearchResources", ttl = "12h")
@CacheKey(fn = "forKey", key = "searchSearchResources:{key}", params = ["key:String"])
fun provideSearchResourcesStore(
    searchApi: SearchApi,
    cache: ApiResponseCacheDao,
): Store<SearchResourcesKey, List<SearchedEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.SearchSearchResources.forKey("${key.query}:${key.resource}:${key.exactMatch}") },
    fetch = { key -> searchApi.searchResources(query = key.query, resource = key.resource, exactMatch = key.exactMatch) },
)
