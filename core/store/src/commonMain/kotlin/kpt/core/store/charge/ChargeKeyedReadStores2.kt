/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.charge

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.charge.Charges
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
    /** The resource kind, as Fineract names it in the path (`loans`, `clients`, `savings`). */
    val resourceType: String,
    /** The id of the resource the records belong to. */
    val resourceId: Int,
    /** Index of the first record of the page. */
    val offset: Int,
    /** Most records the page holds. */
    val limit: Int,
)

/** Addresses one `getListOfOtherAccountCharge` read. */
data class GetListOfOtherAccountChargeKey(
    /** The resource kind, as Fineract names it in the path (`loans`, `clients`, `savings`). */
    val resourceType: String,
    /** The id of the resource the records belong to. */
    val resourceId: Int,
)

/** Addresses one `getCharge` read. */
data class GetChargeKey(
    /** The resource kind, as Fineract names it in the path (`loans`, `clients`, `savings`). */
    val resourceType: String,
    /** The id of the resource the records belong to. */
    val resourceId: Int,
    /** The charge's server id. */
    val chargeId: Int,
)

/** Store reading the list of paging charges. */
@StoreProvider(id = "chargeGetListOfPagingCharges", ttl = "12h")
@CacheKey(fn = "forKey", key = "chargeGetListOfPagingCharges:{key}", params = ["key:String"])
fun provideGetListOfPagingChargesStore(
    chargeApi: ChargeApi,
    cache: ApiResponseCacheDao,
): Store<GetListOfPagingChargesKey, Page<Charges>> = cachedRead(
    dao = cache,
    keyOf = { key ->
        AppCacheKeys.ChargeGetListOfPagingCharges.forKey(
            "${key.resourceType}:${key.resourceId}:${key.offset}:${key.limit}",
        )
    },
    fetch = { key ->
        chargeApi.getListOfPagingCharges(
            resourceType = key.resourceType,
            resourceId = key.resourceId,
            offset = key.offset,
            limit = key.limit,
        )
    },
)

/** Store reading the list of other account charge. */
@StoreProvider(id = "chargeGetListOfOtherAccountCharge", ttl = "12h")
@CacheKey(fn = "forKey", key = "chargeGetListOfOtherAccountCharge:{key}", params = ["key:String"])
fun provideGetListOfOtherAccountChargeStore(
    chargeApi: ChargeApi,
    cache: ApiResponseCacheDao,
): Store<GetListOfOtherAccountChargeKey, List<Charges>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ChargeGetListOfOtherAccountCharge.forKey("${key.resourceType}:${key.resourceId}") },
    fetch = { key ->
        chargeApi.getListOfOtherAccountCharge(
            resourceType = key.resourceType,
            resourceId = key.resourceId,
        )
    },
)

/** Store reading the charge. */
@StoreProvider(id = "chargeGetCharge", ttl = "12h")
@CacheKey(fn = "forKey", key = "chargeGetCharge:{key}", params = ["key:String"])
fun provideGetChargeStore(
    chargeApi: ChargeApi,
    cache: ApiResponseCacheDao,
): Store<GetChargeKey, Charges> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ChargeGetCharge.forKey("${key.resourceType}:${key.resourceId}:${key.chargeId}") },
    fetch = { key ->
        chargeApi.getCharge(
            resourceType = key.resourceType,
            resourceId = key.resourceId,
            chargeId = key.chargeId,
        )
    },
)
