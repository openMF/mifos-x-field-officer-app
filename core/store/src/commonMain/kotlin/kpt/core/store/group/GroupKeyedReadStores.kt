/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.group

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.group.entity.GroupAccounts
import kpt.core.database.group.entity.GroupEntity
import kpt.core.database.group.entity.GroupWithAssociations
import kpt.core.network.mifos.group.api.GroupApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for group with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `getAllGroupsInOffice` read. */
data class GetAllGroupsInOfficeKey(
    val officeId: Int,
    val params: Map<String, String>,
)

@StoreProvider(id = "groupGetGroupWithAssociations", ttl = "12h")
@CacheKey(fn = "forKey", key = "groupGetGroupWithAssociations:{key}", params = ["key:String"])
fun provideGetGroupWithAssociationsStore(
    groupApi: GroupApi,
    cache: ApiResponseCacheDao,
): Store<Int, GroupWithAssociations> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.GroupGetGroupWithAssociations.forKey(key.toString()) },
    fetch = { key -> groupApi.getGroupWithAssociations(groupId = key) },
)

@StoreProvider(id = "groupGetAllGroupsInOffice", ttl = "12h")
@CacheKey(fn = "forKey", key = "groupGetAllGroupsInOffice:{key}", params = ["key:String"])
fun provideGetAllGroupsInOfficeStore(
    groupApi: GroupApi,
    cache: ApiResponseCacheDao,
): Store<GetAllGroupsInOfficeKey, List<GroupEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.GroupGetAllGroupsInOffice.forKey("${key.officeId}:${key.params}") },
    fetch = { key -> groupApi.getAllGroupsInOffice(officeId = key.officeId, params = key.params) },
)

@StoreProvider(id = "groupGetGroup", ttl = "12h")
@CacheKey(fn = "forKey", key = "groupGetGroup:{key}", params = ["key:String"])
fun provideGetGroupStore(
    groupApi: GroupApi,
    cache: ApiResponseCacheDao,
): Store<Int, GroupEntity> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.GroupGetGroup.forKey(key.toString()) },
    fetch = { key -> groupApi.getGroup(groupId = key) },
)

@StoreProvider(id = "groupGetGroupAccounts", ttl = "12h")
@CacheKey(fn = "forKey", key = "groupGetGroupAccounts:{key}", params = ["key:String"])
fun provideGetGroupAccountsStore(
    groupApi: GroupApi,
    cache: ApiResponseCacheDao,
): Store<Int, GroupAccounts> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.GroupGetGroupAccounts.forKey(key.toString()) },
    fetch = { key -> groupApi.getGroupAccounts(groupId = key) },
)
