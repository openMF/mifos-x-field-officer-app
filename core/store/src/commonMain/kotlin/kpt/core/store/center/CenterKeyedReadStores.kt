/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.center

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.center.entity.CenterAccounts
import kpt.core.database.center.entity.CenterEntity
import kpt.core.database.center.entity.CenterWithAssociations
import kpt.core.network.mifos.center.api.CenterApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for center with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

@StoreProvider(id = "centerGetCenterAccounts", ttl = "12h")
@CacheKey(fn = "forKey", key = "centerGetCenterAccounts:{key}", params = ["key:String"])
fun provideGetCenterAccountsStore(
    centerApi: CenterApi,
    cache: ApiResponseCacheDao,
): Store<Int, CenterAccounts> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CenterGetCenterAccounts.forKey(key.toString()) },
    fetch = { key -> centerApi.getCenterAccounts(centerId = key) },
)

@StoreProvider(id = "centerGetCenterWithGroupMembersAndCollectionMeetingCalendar", ttl = "12h")
@CacheKey(fn = "forKey", key = "centerGetCenterWithGroupMembersAndCollectionMeetingCalendar:{key}", params = ["key:String"])
fun provideGetCenterWithGroupMembersAndCollectionMeetingCalendarStore(
    centerApi: CenterApi,
    cache: ApiResponseCacheDao,
): Store<Int, CenterWithAssociations> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CenterGetCenterWithGroupMembersAndCollectionMeetingCalendar.forKey(key.toString()) },
    fetch = { key -> centerApi.getCenterWithGroupMembersAndCollectionMeetingCalendar(centerId = key) },
)

@StoreProvider(id = "centerGetAllGroupsForCenter", ttl = "12h")
@CacheKey(fn = "forKey", key = "centerGetAllGroupsForCenter:{key}", params = ["key:String"])
fun provideGetAllGroupsForCenterStore(
    centerApi: CenterApi,
    cache: ApiResponseCacheDao,
): Store<Int, CenterWithAssociations> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CenterGetAllGroupsForCenter.forKey(key.toString()) },
    fetch = { key -> centerApi.getAllGroupsForCenter(centerId = key) },
)

/** Addresses one `getAllCentersInOffice` read. */
data class GetAllCentersInOfficeKey(
    val officeId: Int,
    val additionalParams: Map<String, String>,
)

@StoreProvider(id = "centerGetAllCentersInOffice", ttl = "12h")
@CacheKey(fn = "forKey", key = "centerGetAllCentersInOffice:{key}", params = ["key:String"])
fun provideGetAllCentersInOfficeStore(
    centerApi: CenterApi,
    cache: ApiResponseCacheDao,
): Store<GetAllCentersInOfficeKey, List<CenterEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CenterGetAllCentersInOffice.forKey("${key.officeId}:${key.additionalParams}") },
    fetch = { key -> centerApi.getAllCentersInOffice(officeId = key.officeId, additionalParams = key.additionalParams) },
)
