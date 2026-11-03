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
import kpt.core.model.client.Client
import kpt.core.model.client.ClientTemplateOptions
import kpt.core.model.client.SavingAccountOption
import kpt.core.model.objects.clients.ClientAddressEntity
import kpt.core.model.objects.clients.ClientAddressResponse
import kpt.core.model.objects.noncoreobjects.Identifier
import kpt.core.model.objects.noncoreobjects.IdentifierTemplate
import kpt.core.model.shared.CollateralItemResult
import kpt.core.network.mifos.client.api.ClientAccountsApi
import kpt.core.network.mifos.client.api.ClientApi
import kpt.core.network.mifos.client.api.ClientIdentifierApi
import kpt.core.network.mifos.client.dto.GetClientsClientIdAccountsResponse
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store
import kpt.core.model.client.ClientAccounts as ClientAccountsModel

/**
 * Keyed reads for client with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

@StoreProvider(id = "clientRetrieveAssociatedAccounts", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientRetrieveAssociatedAccounts:{key}", params = ["key:String"])
fun provideRetrieveAssociatedAccountsStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Long, GetClientsClientIdAccountsResponse> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientRetrieveAssociatedAccounts.forKey(key.toString()) },
    fetch = { key -> clientApi.retrieveAssociatedAccounts(clientId = key) },
)

/** Store reading the client. */
@StoreProvider(id = "clientGetClient", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetClient:{key}", params = ["key:String"])
fun provideGetClientStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Int, Client> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClient.forKey(key.toString()) },
    fetch = { key -> clientApi.getClient(clientId = key) },
)

/** Store reading the client pinpoint locations. */
@StoreProvider(id = "clientGetClientPinpointLocations", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetClientPinpointLocations:{key}", params = ["key:String"])
fun provideGetClientPinpointLocationsStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Int, List<ClientAddressResponse>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClientPinpointLocations.forKey(key.toString()) },
    fetch = { key -> clientApi.getClientPinpointLocations(clientId = key) },
)

/** Store reading the client template. */
@StoreProvider(id = "clientGetClientTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetClientTemplate:{key}", params = ["key:String"])
fun provideGetClientTemplateStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Int, ClientTemplateOptions> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClientTemplate.forKey(key.toString()) },
    // Projected to the DOMAIN type here, not handed on as the wire envelope.
    // `ScreenDataStream` has an internal constructor, so a feature cannot narrow the type itself —
    // the projection has to happen at or below the store, and this is where the fetch already is.
    // Keeping the envelope would force every screen with a staff dropdown to depend on
    // `core:network`. See `ClientTemplateOptions` for the full reasoning.
    fetch = { key ->
        clientApi.getClientTemplate(clientId = key).let { response ->
            ClientTemplateOptions(
                staffOptions = response.staffOptions,
                savingAccountOptions = response.savingAccountOptions.map {
                    SavingAccountOption(id = it.id, accountNo = it.accountNo)
                },
                shareAccounts = response.shareAccounts,
            )
        }
    },
)

/** Store reading the client collateral items. */
@StoreProvider(id = "clientGetClientCollateralItems", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetClientCollateralItems:{key}", params = ["key:String"])
fun provideGetClientCollateralItemsStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Int, List<CollateralItemResult>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClientCollateralItems.forKey(key.toString()) },
    fetch = { key -> clientApi.getClientCollateralItems(clientId = key) },
)

/** Store reading the client list identifiers. */
@StoreProvider(id = "clientGetClientListIdentifiers", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetClientListIdentifiers:{key}", params = ["key:String"])
fun provideGetClientListIdentifiersStore(
    clientIdentifierApi: ClientIdentifierApi,
    cache: ApiResponseCacheDao,
): Store<Long, List<Identifier>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClientListIdentifiers.forKey(key.toString()) },
    fetch = { key -> clientIdentifierApi.getClientListIdentifiers(clientId = key) },
)

/** Store reading the client identifiers. */
@StoreProvider(id = "clientGetClientIdentifiers", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetClientIdentifiers:{key}", params = ["key:String"])
fun provideGetClientIdentifiersStore(
    clientIdentifierApi: ClientIdentifierApi,
    cache: ApiResponseCacheDao,
): Store<GetClientIdentifiersKey, Identifier> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClientIdentifiers.forKey("${key.clientId}:${key.identifierId}") },
    fetch = { key ->
        clientIdentifierApi.getClientIdentifiers(
            clientId = key.clientId,
            identifierId = key.identifierId,
        )
    },
)

/** Store reading the client identifier template. */
@StoreProvider(id = "clientGetClientIdentifierTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetClientIdentifierTemplate:{key}", params = ["key:String"])
fun provideGetClientIdentifierTemplateStore(
    clientIdentifierApi: ClientIdentifierApi,
    cache: ApiResponseCacheDao,
): Store<Long, IdentifierTemplate> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClientIdentifierTemplate.forKey(key.toString()) },
    fetch = { key -> clientIdentifierApi.getClientIdentifierTemplate(clientId = key) },
)

/** Store reading all accounts of client. */
@StoreProvider(id = "clientGetAllAccountsOfClient", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetAllAccountsOfClient:{key}", params = ["key:String"])
fun provideGetAllAccountsOfClientStore(
    clientAccountsApi: ClientAccountsApi,
    cache: ApiResponseCacheDao,
): Store<Int, ClientAccountsModel> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetAllAccountsOfClient.forKey(key.toString()) },
    fetch = { key -> clientAccountsApi.getAllAccountsOfClient(clientId = key) },
)

/** Store reading the client accounts. */
@StoreProvider(id = "clientGetClientAccounts", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetClientAccounts:{key}", params = ["key:String"])
fun provideGetClientAccountsStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Int, ClientAccountsModel> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClientAccounts.forKey(key.toString()) },
    fetch = { key -> clientApi.getClientAccounts(clientId = key) },
)

/** Store reading the client addresses. */
@StoreProvider(id = "clientGetClientAddresses", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientGetClientAddresses:{key}", params = ["key:String"])
fun provideGetClientAddressesStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Int, List<ClientAddressEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClientAddresses.forKey(key.toString()) },
    fetch = { key -> clientApi.getClientAddresses(clientId = key) },
)
