/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.client.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.client.ClientRepository
import kpt.core.model.client.AddressConfiguration
import kpt.core.model.client.AddressTemplate
import kpt.core.model.client.Client
import kpt.core.model.client.ClientAccounts
import kpt.core.model.client.ClientCloseTemplateResponse
import kpt.core.model.client.ClientTemplateOptions
import kpt.core.model.client.ClientsTemplate
import kpt.core.model.objects.clients.ClientAddressEntity
import kpt.core.model.objects.clients.ClientAddressResponse
import kpt.core.model.objects.noncoreobjects.Identifier
import kpt.core.model.objects.noncoreobjects.IdentifierTemplate
import kpt.core.model.shared.CollateralItem
import kpt.core.model.shared.CollateralItemResult
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

@RepositoryBinding(binds = ClientRepository::class)
internal class ClientRepositoryImpl(
    @FromStore(AppStoreIds.Clients) private val store: Store<Unit, List<Client>>,
    @FromStore(AppStoreIds.ClientGetClient) private val clientStore: Store<Int, Client>,
    @FromStore(AppStoreIds.ClientGetClientAccounts) private val accountsStore: Store<Int, ClientAccounts>,
    @FromStore(AppStoreIds.ClientGetClientIdentifierTemplate)
    private val identifierTemplateStore: Store<Long, IdentifierTemplate>,
    @FromStore(AppStoreIds.ClientGetClientListIdentifiers)
    private val identifiersStore: Store<Long, List<Identifier>>,
    @FromStore(AppStoreIds.ClientGetClientPinpointLocations)
    private val pinpointStore: Store<Int, List<ClientAddressResponse>>,
    @FromStore(AppStoreIds.ClientGetClientCollateralItems)
    private val clientCollateralStore: Store<Int, List<CollateralItemResult>>,
    @FromStore(AppStoreIds.CollateralItems)
    private val collateralCatalogStore: Store<Unit, List<CollateralItem>>,
    @FromStore(AppStoreIds.ClientCloseTemplate)
    private val closeTemplateStore: Store<Unit, ClientCloseTemplateResponse>,
    @FromStore(AppStoreIds.ClientGetClientTemplate)
    private val clientTemplateStore: Store<Int, ClientTemplateOptions>,
    @FromStore(AppStoreIds.ClientGetClientAddresses)
    private val addressesStore: Store<Int, List<ClientAddressEntity>>,
    @FromStore(AppStoreIds.AddressTemplate)
    private val addressTemplateStore: Store<Unit, AddressTemplate>,
    @FromStore(AppStoreIds.ClientsTemplate)
    private val clientsTemplateStore: Store<Unit, ClientsTemplate>,
    @FromStore(AppStoreIds.AddressConfiguration)
    private val addressConfigurationStore: Store<Unit, AddressConfiguration>,
    @FromStore(AppStoreIds.ClientImage)
    private val imageStore: Store<Int, String>,
) : ClientRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default.
    //
    // SWR's read path is `cached(key, refresh = false)` — it makes no request itself and leaves
    // revalidation to a freshness-band gate that only fires once something HAS been fetched. On a
    // cold cache (`fetchedAt == null`, the band is `Initial`) nothing ever fetches: the Clients tab
    // sat on Loading forever with zero network requests on a clean install, verified on device
    // 2026-10-01. NETWORK_WITH_CACHE asks the network on the read path and still serves Room first,
    // so the offline-first behaviour this screen needs is unchanged — a field officer with no signal
    // still gets the cached roster, and the first run actually loads.
    //
    // The ttl stays the declared freshness bound: it drives the "updated N ago" indicator, never
    // whether cache is served.
    override fun clientsStream(scope: CoroutineScope): ScreenDataStream<List<Client>> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.Clients.LIST,
            scope = scope,
            // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CLIENTS,
        )

    override fun clientStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<Client> =
        clientStore.asScreenStream(
            key = clientId,
            cacheKey = AppCacheKeys.ClientGetClient.forKey(clientId.toString()),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CLIENT_GET_CLIENT,
        )

    override fun clientAccountsStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<ClientAccounts> =
        accountsStore.asScreenStream(
            key = clientId,
            cacheKey = AppCacheKeys.ClientGetClientAccounts.forKey(clientId.toString()),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CLIENT_GET_CLIENT_ACCOUNTS,
        )

    override fun clientIdentifiersStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Identifier>> = identifiersStore.asScreenStream(
        key = clientId,
        cacheKey = AppCacheKeys.ClientGetClientListIdentifiers.forKey(clientId.toString()),
        scope = scope,
        fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        ttl = AppStoreRegistry.Ttl.CLIENT_GET_CLIENT_LIST_IDENTIFIERS,
    )

    override fun clientIdentifierTemplateStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<IdentifierTemplate> = identifierTemplateStore.asScreenStream(
        key = clientId,
        cacheKey = AppCacheKeys.ClientGetClientIdentifierTemplate.forKey(clientId.toString()),
        scope = scope,
        fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        ttl = AppStoreRegistry.Ttl.CLIENT_GET_CLIENT_IDENTIFIER_TEMPLATE,
    )

    override fun pinpointLocationsStream(
        clientId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<List<ClientAddressResponse>> = pinpointStore.asScreenStream(
        key = clientId,
        cacheKey = AppCacheKeys.ClientGetClientPinpointLocations.forKey(clientId.toString()),
        scope = scope,
        // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
        fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        ttl = AppStoreRegistry.Ttl.CLIENT_GET_CLIENT_PINPOINT_LOCATIONS,
    )

    override fun clientCollateralStream(
        clientId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<List<CollateralItemResult>> = clientCollateralStore.asScreenStream(
        key = clientId,
        cacheKey = AppCacheKeys.ClientGetClientCollateralItems.forKey(clientId.toString()),
        scope = scope,
        // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
        fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        ttl = AppStoreRegistry.Ttl.CLIENT_GET_CLIENT_COLLATERAL_ITEMS,
    )

    // The two catalogs below are NOT marked `isEmpty`. An empty collateral-type list or an empty
    // closure-reason list is a tenant MISCONFIGURATION, not an empty state the officer can act on:
    // rendering "nothing here" would invite them to keep waiting. Letting it through as Content
    // makes the form render with an empty picker, which is the honest symptom.
    override fun collateralItemsStream(scope: CoroutineScope): ScreenDataStream<List<CollateralItem>> =
        collateralCatalogStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.CollateralItems.LIST,
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.COLLATERAL_ITEMS,
        )

    override fun clientCloseTemplateStream(scope: CoroutineScope): ScreenDataStream<ClientCloseTemplateResponse> =
        closeTemplateStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.ClientCloseTemplate.LIST,
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CLIENT_CLOSE_TEMPLATE,
        )

    override fun clientTemplateStream(
        clientId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<ClientTemplateOptions> = clientTemplateStore.asScreenStream(
        key = clientId,
        cacheKey = AppCacheKeys.ClientGetClientTemplate.forKey(clientId.toString()),
        scope = scope,
        fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        ttl = AppStoreRegistry.Ttl.CLIENT_GET_CLIENT_TEMPLATE,
    )

    /**
     * Fineract answers `GET clients/{id}/images` with a `data:image/png;base64,<payload>` STRING,
     * not binary. The `data:` prefix is left ON: `imageToByteArray` strips it itself, and stripping
     * it twice would cut into the payload of any photo whose base64 happens to contain a comma.
     *
     * Through the `clientImage` store, not `ClientApi` — the original's call went straight to the
     * network from here and cached nothing. `fresh` first, so a photo changed elsewhere shows; offline
     * it falls back to the source of truth, so a photo seen once still renders in the field. The
     * store caches "no photo" as `""` (a 404, the common case), which reads as `null` here, as does
     * any failure: an unreachable photo must not turn the profile into an error screen.
     */
    override suspend fun clientImage(clientId: Int): String? =
        (orNull { imageStore.fresh(clientId) } ?: orNull { imageStore.get(clientId) })?.takeIf { it.isNotEmpty() }

    private inline fun <T> orNull(block: () -> T): T? = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        null
    }

    override fun clientAddressesStream(
        clientId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<List<ClientAddressEntity>> = addressesStore.asScreenStream(
        key = clientId,
        cacheKey = AppCacheKeys.ClientGetClientAddresses.forKey(clientId.toString()),
        scope = scope,
        // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
        fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        ttl = AppStoreRegistry.Ttl.CLIENT_GET_CLIENT_ADDRESSES,
    )

    override fun addressTemplateStream(scope: CoroutineScope): ScreenDataStream<AddressTemplate> =
        addressTemplateStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.AddressTemplate.LIST,
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.ADDRESS_TEMPLATE,
        )

    // Deliberately NOT marked `isEmpty`: an empty option list here is a TENANT MISCONFIGURATION,
    // not an empty state. The same reasoning as `collateralItemsStream` above — a form that shows
    // "nothing to see here" when the tenant forgot to configure genders sends the officer looking
    // for data that was never going to arrive.
    override fun clientsTemplateStream(scope: CoroutineScope): ScreenDataStream<ClientsTemplate> =
        clientsTemplateStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.ClientsTemplate.LIST,
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CLIENTS_TEMPLATE,
        )

    override fun addressConfigurationStream(scope: CoroutineScope): ScreenDataStream<AddressConfiguration> =
        addressConfigurationStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.AddressConfiguration.LIST,
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.ADDRESS_CONFIGURATION,
        )
}
