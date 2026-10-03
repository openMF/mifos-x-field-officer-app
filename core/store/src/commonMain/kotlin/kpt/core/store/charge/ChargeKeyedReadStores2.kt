/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.charge

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.charge.entity.ChargesEntity
import kpt.core.model.objects.clients.Page
import kpt.core.network.mifos.charge.api.ChargeApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for charge with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `getListOfPagingCharges` read. */
data class GetListOfPagingChargesKey(
    val resourceType: String,
    val resourceId: Int,
    val offset: Int,
    val limit: Int,
)

/** Addresses one `getListOfOtherAccountCharge` read. */
data class GetListOfOtherAccountChargeKey(
    val resourceType: String,
    val resourceId: Int,
)

/** Addresses one `getCharge` read. */
data class GetChargeKey(
    val resourceType: String,
    val resourceId: Int,
    val chargeId: Int,
)

@StoreProvider(id = "chargeGetListOfPagingCharges", ttl = "12h")
@CacheKey(fn = "forKey", key = "chargeGetListOfPagingCharges:{key}", params = ["key:String"])
fun provideGetListOfPagingChargesStore(
    chargeApi: ChargeApi,
    cache: ApiResponseCacheDao,
): Store<GetListOfPagingChargesKey, Page<ChargesEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ChargeGetListOfPagingCharges.forKey("${key.resourceType}:${key.resourceId}:${key.offset}:${key.limit}") },
    fetch = { key -> chargeApi.getListOfPagingCharges(resourceType = key.resourceType, resourceId = key.resourceId, offset = key.offset, limit = key.limit) },
)

@StoreProvider(id = "chargeGetListOfOtherAccountCharge", ttl = "12h")
@CacheKey(fn = "forKey", key = "chargeGetListOfOtherAccountCharge:{key}", params = ["key:String"])
fun provideGetListOfOtherAccountChargeStore(
    chargeApi: ChargeApi,
    cache: ApiResponseCacheDao,
): Store<GetListOfOtherAccountChargeKey, List<ChargesEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ChargeGetListOfOtherAccountCharge.forKey("${key.resourceType}:${key.resourceId}") },
    fetch = { key -> chargeApi.getListOfOtherAccountCharge(resourceType = key.resourceType, resourceId = key.resourceId) },
)

@StoreProvider(id = "chargeGetCharge", ttl = "12h")
@CacheKey(fn = "forKey", key = "chargeGetCharge:{key}", params = ["key:String"])
fun provideGetChargeStore(
    chargeApi: ChargeApi,
    cache: ApiResponseCacheDao,
): Store<GetChargeKey, ChargesEntity> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ChargeGetCharge.forKey("${key.resourceType}:${key.resourceId}:${key.chargeId}") },
    fetch = { key -> chargeApi.getCharge(resourceType = key.resourceType, resourceId = key.resourceId, chargeId = key.chargeId) },
)
