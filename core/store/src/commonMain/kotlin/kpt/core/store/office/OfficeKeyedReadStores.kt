/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.office

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.network.mifos.office.api.OfficeApi
import kpt.core.network.mifos.office.dto.GetOfficesResponse
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for office with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `retrieveOffices` read. */
data class RetrieveOfficesKey(
    val includeAllOffices: Boolean? = false,
    val orderBy: String? = null,
    val sortOrder: String? = null,
)

@StoreProvider(id = "officeRetrieveOffices", ttl = "12h")
@CacheKey(fn = "forKey", key = "officeRetrieveOffices:{key}", params = ["key:String"])
fun provideRetrieveOfficesStore(
    officeApi: OfficeApi,
    cache: ApiResponseCacheDao,
): Store<RetrieveOfficesKey, List<GetOfficesResponse>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.OfficeRetrieveOffices.forKey("${key.includeAllOffices}:${key.orderBy}:${key.sortOrder}") },
    fetch = { key -> officeApi.retrieveOffices(includeAllOffices = key.includeAllOffices, orderBy = key.orderBy, sortOrder = key.sortOrder) },
)
