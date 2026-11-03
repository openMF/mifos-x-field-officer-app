/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.collectionsheet

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.objects.collectionsheets.CollectionSheetRequestPayload
import kpt.core.network.mifos.collectionsheet.api.CollectionSheetApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store
import kpt.core.model.center.CenterWithAssociations as CenterWithAssociationsModel
import kpt.core.model.collectionsheet.CollectionSheetResponse as CollectionSheetResponseModel
import kpt.core.model.collectionsheet.IndividualCollectionSheet as IndividualCollectionSheetModel

/**
 * Keyed reads for collectionsheet with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

@StoreProvider(id = "collectionsheetFetchGroupsAssociatedWithCenter", ttl = "12h")
@CacheKey(fn = "forKey", key = "collectionsheetFetchGroupsAssociatedWithCenter:{key}", params = ["key:String"])
fun provideFetchGroupsAssociatedWithCenterStore(
    collectionSheetApi: CollectionSheetApi,
    cache: ApiResponseCacheDao,
): Store<Int, CenterWithAssociationsModel> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CollectionsheetFetchGroupsAssociatedWithCenter.forKey(key.toString()) },
    fetch = { key -> collectionSheetApi.fetchGroupsAssociatedWithCenter(centerId = key) },
)

/*
 * Generated sheets. Fineract's `?command=generateCollectionSheet` endpoints are POST-shaped but are
 * READS — the body only selects which sheet — so each is a store keyed by that body. The sheet is
 * computed against live balances, so a save changes it: `CollectionSheetCommandRepository` re-reads
 * the saved sheet through these same stores, and the cached row is the post-save one.
 */

/**
 * One generated sheet: the owning center/group plus the request that selects the meeting.
 *
 * @property ownerId the centre or group the sheet is for
 * @property request the meeting the sheet is generated for
 */
data class GeneratedSheetKey(val ownerId: Int, val request: CollectionSheetRequestPayload)

/** Store reading the individual collection sheet. */
@StoreProvider(id = "collectionsheetIndividual", ttl = "12h")
@CacheKey(fn = "forKey", key = "collectionsheetIndividual:{key}", params = ["key:String"])
fun provideIndividualCollectionSheetStore(
    collectionSheetApi: CollectionSheetApi,
    cache: ApiResponseCacheDao,
): Store<RequestCollectionSheetPayload, IndividualCollectionSheetModel> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CollectionsheetIndividual.forKey(key.toString()) },
    fetch = { key -> collectionSheetApi.getIndividualCollectionSheet(payload = key) },
)

/** Store reading the productive collection sheet. */
@StoreProvider(id = "collectionsheetProductive", ttl = "12h")
@CacheKey(fn = "forKey", key = "collectionsheetProductive:{key}", params = ["key:String"])
fun provideProductiveCollectionSheetStore(
    collectionSheetApi: CollectionSheetApi,
    cache: ApiResponseCacheDao,
): Store<GeneratedSheetKey, CollectionSheetResponseModel> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CollectionsheetProductive.forKey(key.toString()) },
    fetch = { key -> collectionSheetApi.fetchProductiveSheet(centerId = key.ownerId, payload = key.request) },
)

/** Store reading the group collection sheet. */
@StoreProvider(id = "collectionsheetGroup", ttl = "12h")
@CacheKey(fn = "forKey", key = "collectionsheetGroup:{key}", params = ["key:String"])
fun provideGroupCollectionSheetStore(
    collectionSheetApi: CollectionSheetApi,
    cache: ApiResponseCacheDao,
): Store<GeneratedSheetKey, CollectionSheetResponseModel> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CollectionsheetGroup.forKey(key.toString()) },
    fetch = { key -> collectionSheetApi.fetchCollectionSheet(groupId = key.ownerId, payload = key.request) },
)
