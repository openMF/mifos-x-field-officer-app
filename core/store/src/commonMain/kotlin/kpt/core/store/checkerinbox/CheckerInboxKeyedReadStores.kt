/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.checkerinbox

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.network.mifos.checkerinbox.api.CheckerInboxApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for checkerinbox with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `getCheckerTasksFromResourceId` read. */
data class GetCheckerTasksFromResourceIdKey(
    val actionName: String? = null,
    val entityName: String? = null,
    val resourceId: Int? = null,
)

@StoreProvider(id = "checkerinboxGetCheckerTasksFromResourceId", ttl = "12h")
@CacheKey(fn = "forKey", key = "checkerinboxGetCheckerTasksFromResourceId:{key}", params = ["key:String"])
fun provideGetCheckerTasksFromResourceIdStore(
    checkerInboxApi: CheckerInboxApi,
    cache: ApiResponseCacheDao,
): Store<GetCheckerTasksFromResourceIdKey, List<CheckerTask>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.CheckerinboxGetCheckerTasksFromResourceId.forKey("${key.actionName}:${key.entityName}:${key.resourceId}") },
    fetch = { key -> checkerInboxApi.getCheckerTasksFromResourceId(actionName = key.actionName, entityName = key.entityName, resourceId = key.resourceId) },
)
