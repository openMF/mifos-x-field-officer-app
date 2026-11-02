/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.searchrecord.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.searchrecord.SearchRecordRepository
import kpt.core.model.objects.searchrecord.GenericSearchRecord
import kpt.core.model.objects.searchrecord.SearchRecordKey
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = SearchRecordRepository::class)
internal class SearchRecordRepositoryImpl(
    @FromStore(AppStoreIds.SearchRecords) private val store: Store<SearchRecordKey, List<GenericSearchRecord>>,
) : SearchRecordRepository {

    override fun searchStream(
        keys: Flow<SearchRecordKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<List<GenericSearchRecord>> =
        store.asScreenStream(
            keyFlow = keys,
            cacheKeyFor = { AppCacheKeys.SearchRecords.forQuery(it.type.name, it.query) },
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )
}
