/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.client

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.client.entity.AddressConfiguration
import kpt.core.database.client.entity.AddressTemplate
import kpt.core.model.shared.CollateralItem
import kpt.core.network.mifos.client.api.ClientApi
import kpt.core.network.mifos.client.dto.ClientCloseTemplateResponse
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Catalogue and template reads for client — option lists and form templates with no table of their
 * own, cached through the shared read cache so they still render with no signal.
 */

@StoreProvider(id = "addressConfiguration", ttl = "12h")
@CacheKey(name = "LIST", key = "addressConfiguration")
fun provideAddressConfigurationStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Unit, AddressConfiguration> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.AddressConfiguration.LIST },
    fetch = { clientApi.getAddressConfiguration() },
)

@StoreProvider(id = "addressTemplate", ttl = "12h")
@CacheKey(name = "LIST", key = "addressTemplate")
fun provideAddressTemplateStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Unit, AddressTemplate> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.AddressTemplate.LIST },
    fetch = { clientApi.getAddressTemplate() },
)

@StoreProvider(id = "clientCloseTemplate", ttl = "12h")
@CacheKey(name = "LIST", key = "clientCloseTemplate")
fun provideClientCloseTemplateStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Unit, ClientCloseTemplateResponse> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.ClientCloseTemplate.LIST },
    fetch = { clientApi.getClientCloseTemplate() },
)

@StoreProvider(id = "collateralItems", ttl = "12h")
@CacheKey(name = "LIST", key = "collateralItems")
fun provideCollateralItemsStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Unit, List<CollateralItem>> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.CollateralItems.LIST },
    fetch = { clientApi.getCollateralItems() },
)
