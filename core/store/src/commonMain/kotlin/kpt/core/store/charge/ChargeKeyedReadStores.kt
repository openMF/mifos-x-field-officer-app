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
import kpt.core.model.objects.template.client.ChargeTemplate
import kpt.core.network.mifos.charge.api.ChargeApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for charge with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `getChargeTemplate` read. */
data class GetChargeTemplateKey(
    val resourceType: String,
    val resourceId: Int,
)

@StoreProvider(id = "chargeGetChargeTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "chargeGetChargeTemplate:{key}", params = ["key:String"])
fun provideGetChargeTemplateStore(
    chargeApi: ChargeApi,
    cache: ApiResponseCacheDao,
): Store<GetChargeTemplateKey, ChargeTemplate> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ChargeGetChargeTemplate.forKey("${key.resourceType}:${key.resourceId}") },
    fetch = { key -> chargeApi.getChargeTemplate(resourceType = key.resourceType, resourceId = key.resourceId) },
)
