/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.collectionsheet.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.collectionsheet.CollectionSheetRepository
import kpt.core.data.util.freshOrCached
import kpt.core.model.collectionsheet.CenterDetail
import kpt.core.model.collectionsheet.CollectionSheetResponse
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.objects.collectionsheets.CollectionSheetRequestPayload
import kpt.core.store.collectionsheet.CollectionSheetKey
import kpt.core.store.collectionsheet.GeneratedSheetKey
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = CollectionSheetRepository::class)
internal class CollectionSheetRepositoryImpl(
    @FromStore(AppStoreIds.CollectionSheetCenters)
    private val store: Store<CollectionSheetKey, List<CenterDetail>>,
    @FromStore(AppStoreIds.CollectionsheetIndividual)
    private val individualStore: Store<RequestCollectionSheetPayload, IndividualCollectionSheet>,
    @FromStore(AppStoreIds.CollectionsheetProductive)
    private val productiveStore: Store<GeneratedSheetKey, CollectionSheetResponse>,
    @FromStore(AppStoreIds.CollectionsheetGroup)
    private val groupStore: Store<GeneratedSheetKey, CollectionSheetResponse>,
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
            // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.COLLECTION_SHEET_CENTERS,
        )

    override fun individualCollectionSheetStream(
        request: RequestCollectionSheetPayload,
        scope: CoroutineScope,
    ): ScreenDataStream<IndividualCollectionSheet> {
        // A COPY: the request type is mutable, and a store key must not change after it is used.
        val key = request.copy()
        return individualStore.asScreenStream(
            key = key,
            cacheKey = AppCacheKeys.CollectionsheetIndividual.forKey(key.toString()),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.COLLECTIONSHEET_INDIVIDUAL,
            // No `isEmpty`: it means "nothing has arrived from the store yet", not "empty content" —
            // passing a no-loans predicate here kept an ARRIVED sheet on Loading forever (device,
            // 2026-10-24). "Nothing due" is the consumer's `emptyIfContent`.
        )
    }

    // Through the sheet stores, never `CollectionSheetApi` directly: `fresh` writes the generated
    // sheet through the read cache, and offline `get` answers from it. Each key is a COPY — the
    // request types are mutable, and a store key must not change after it is used.

    override suspend fun individualCollectionSheet(request: RequestCollectionSheetPayload) =
        individualStore.freshOrCached(request.copy())

    override suspend fun productiveSheet(centerId: Int, request: CollectionSheetRequestPayload) =
        productiveStore.freshOrCached(GeneratedSheetKey(centerId, request.copy()))

    override suspend fun groupCollectionSheet(groupId: Int, request: CollectionSheetRequestPayload) =
        groupStore.freshOrCached(GeneratedSheetKey(groupId, request.copy()))
}
