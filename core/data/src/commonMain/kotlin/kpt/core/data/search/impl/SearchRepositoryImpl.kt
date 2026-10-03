/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.search.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.search.SearchRepository
import kpt.core.model.objects.SearchedEntity
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import kpt.core.store.search.SearchResourcesKey
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = SearchRepository::class)
internal class SearchRepositoryImpl(
    @FromStore(AppStoreIds.SearchSearchResources)
    private val store: Store<SearchResourcesKey, List<SearchedEntity>>,
) : SearchRepository {

    override fun searchStream(
        key: SearchResourcesKey,
        scope: CoroutineScope,
    ): ScreenDataStream<List<SearchedEntity>> = store.asScreenStream(
        key = key,
        cacheKey = AppCacheKeys.SearchSearchResources.forKey(
            "${key.query}:${key.resource}:${key.exactMatch}",
        ),
        scope = scope,
        isEmpty = { it.isEmpty() },
        fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        ttl = AppStoreRegistry.Ttl.SEARCH_SEARCH_RESOURCES,
    )
}
