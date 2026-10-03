/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.client.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.client.ClientRepository
import kpt.core.database.client.entity.ClientAccounts
import kpt.core.database.client.entity.ClientEntity
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = ClientRepository::class)
internal class ClientRepositoryImpl(
    @FromStore(AppStoreIds.Clients) private val store: Store<Unit, List<ClientEntity>>,
    @FromStore(AppStoreIds.ClientGetClient) private val clientStore: Store<Int, ClientEntity>,
    @FromStore(AppStoreIds.ClientGetClientAccounts) private val accountsStore: Store<Int, ClientAccounts>,
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
    override fun clientsStream(scope: CoroutineScope): ScreenDataStream<List<ClientEntity>> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.Clients.LIST,
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CLIENTS,
        )

    override fun clientStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<ClientEntity> =
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
}
