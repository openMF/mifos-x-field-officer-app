/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.collectionsheet.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.collectionsheet.CollectionSheetRepository
import kpt.core.database.collectionsheet.entity.CenterDetail
import kpt.core.store.collectionsheet.CollectionSheetKey
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = CollectionSheetRepository::class)
internal class CollectionSheetRepositoryImpl(
    @FromStore(AppStoreIds.CollectionSheetCenters)
    private val store: Store<CollectionSheetKey, List<CenterDetail>>,
) : CollectionSheetRepository {

    override fun centerDetailsStream(
        officeId: Int,
        staffId: Int,
        meetingDate: String,
        scope: CoroutineScope,
    ): ScreenDataStream<List<CenterDetail>> =
        store.asScreenStream(
            key = CollectionSheetKey(officeId = officeId, staffId = staffId, meetingDate = meetingDate),
            cacheKey = AppCacheKeys.CollectionSheetCenters.forQuery(officeId, staffId, meetingDate),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.COLLECTION_SHEET_CENTERS,
        )
}
