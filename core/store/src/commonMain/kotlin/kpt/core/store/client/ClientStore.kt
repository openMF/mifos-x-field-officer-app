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

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.client.dao.ClientDao
import kpt.core.model.client.Client
import kpt.core.network.mifos.client.api.ClientApi
import kpt.core.store.mapping.toEntity
import kpt.core.store.mapping.toModel
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Clients — READ-CACHE (`createStore`).
 *
 * The app's hub entity: most other features are reached THROUGH a client, so this is the store a
 * field officer depends on most when offline. Room answers from cache immediately and a reconnect
 * refreshes it.
 */
@StoreProvider(id = "clients", ttl = "1h")
@CacheKey(name = "LIST", key = "clients")
fun provideClientStore(
    service: ClientApi,
    dao: ClientDao,
): Store<Unit, List<Client>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getAllClients(b = true, offset = 0, limit = CLIENT_PAGE_LIMIT).pageItems
    },
    sourceOfTruth = SourceOfTruth.of(
        // An empty table is an empty LIST, never null — a null-for-empty reader stops the stream from
        // ever showing "empty" once data existed (measured, `NullReaderStreamTest` in core/data).
        reader = { _: Unit -> dao.getAllClients().map { rows -> rows.map { it.toModel() } } },
        writer = { _: Unit, clients: List<Client> -> clients.map { it.toEntity() }.forEach { dao.insertClient(it) } },
        delete = { _: Unit -> dao.deleteAllClients() },
        // What the logout purge (`Store.clear()`) reaches the `Client` table through. Without it the
        // roster survived sign-out for the next officer to read.
        deleteAll = { dao.deleteAllClients() },
    ),
)

/**
 * The WRITE half for the roster: one `Client` row. Local-only (`createOfflineMutableStore`) — the
 * network leg already ran in the `ClientCommandRepository` command, which records the server's
 * accepted client here so the roster's reader re-emits with no refresh. `logout = false`: purged by
 * [provideClientStore]'s `deleteAll` over the same table (pairing as in `queue/OfflineQueueStores.kt`).
 */
// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "clientRow", logout = false)
@CacheKey(fn = "byId", key = "clientRow:{id}", params = ["id:Int"])
fun provideClientRowStore(
    dao: ClientDao,
): MutableStore<Int, Client> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: Int -> dao.getClientByClientId(id).map { it?.toModel() } },
        writer = { _: Int, client: Client -> dao.insertClient(client.toEntity()) },
        delete = { id: Int -> dao.deleteClientById(id) },
    ),
)

private const val CLIENT_PAGE_LIMIT = 100
