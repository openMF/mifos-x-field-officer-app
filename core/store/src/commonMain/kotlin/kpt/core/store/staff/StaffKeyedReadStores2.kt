/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.staff

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.staff.entity.StaffEntity
import kpt.core.network.mifos.staff.api.StaffApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for staff with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

@StoreProvider(id = "staffAllStaff", ttl = "12h")
@CacheKey(fn = "forKey", key = "staffAllStaff:{key}", params = ["key:String"])
fun provideAllStaffStore(
    staffApi: StaffApi,
    cache: ApiResponseCacheDao,
): Store<Unit, List<StaffEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.StaffAllStaff.forKey("all") },
    fetch = { key -> staffApi.allStaff() },
)

@StoreProvider(id = "staffFieldStaffForOffice", ttl = "12h")
@CacheKey(fn = "forKey", key = "staffFieldStaffForOffice:{key}", params = ["key:String"])
fun provideFieldStaffForOfficeStore(
    staffApi: StaffApi,
    cache: ApiResponseCacheDao,
): Store<Unit, List<StaffEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.StaffFieldStaffForOffice.forKey("all") },
    fetch = { key -> staffApi.fieldStaffForOffice() },
)
