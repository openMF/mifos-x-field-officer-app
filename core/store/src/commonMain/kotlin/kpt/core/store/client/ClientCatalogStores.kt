/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.client

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.client.entity.AddressConfiguration
import kpt.core.database.client.entity.AddressTemplate
import kpt.core.model.client.ClientCloseTemplateResponse
import kpt.core.model.client.ClientsTemplate
import kpt.core.model.shared.CollateralItem
import kpt.core.network.mifos.client.api.ClientApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store
import kpt.core.model.client.AddressConfiguration as AddressConfigurationModel
import kpt.core.model.client.AddressTemplate as AddressTemplateModel

/**
 * Catalogue and template reads for client — option lists and form templates with no table of their
 * own, cached through the shared read cache so they still render with no signal.
 */

@StoreProvider(id = "addressConfiguration", ttl = "12h")
@CacheKey(name = "LIST", key = "addressConfiguration")
fun provideAddressConfigurationStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Unit, AddressConfigurationModel> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.AddressConfiguration.LIST },
    fetch = { clientApi.getAddressConfiguration() },
)

/**
 * The TENANT's client template — the gender / client-type / classification / legal-form code
 * values a client form offers, plus its configured data tables.
 *
 * Distinct from `clientTemplateStream(clientId)`, which is the PER-CLIENT read
 * (`clients/{id}?template=true`) carrying staff and savings options for one client. This one is
 * `clients/template` and is the same for every client in the tenant, so it is keyed `Unit` and
 * cached for 12h like its sibling catalogues.
 *
 * `ClientApi.getClientTemplate()` and `ClientDao.getClientsTemplate()` both already existed and
 * were referenced by NOTHING — the create-client and edit-details forms are the first readers, and
 * without this provider their four code-value dropdowns had no source at all.
 */
@StoreProvider(id = "clientsTemplate", ttl = "12h")
@CacheKey(name = "LIST", key = "clientsTemplate")
fun provideClientsTemplateStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Unit, ClientsTemplate> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.ClientsTemplate.LIST },
    fetch = { clientApi.getClientTemplate() },
)

@StoreProvider(id = "addressTemplate", ttl = "12h")
@CacheKey(name = "LIST", key = "addressTemplate")
fun provideAddressTemplateStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Unit, AddressTemplateModel> = cachedRead(
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
