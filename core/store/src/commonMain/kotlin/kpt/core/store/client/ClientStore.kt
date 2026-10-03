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
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.client.dao.ClientDao
import kpt.core.database.client.entity.ClientEntity
import kpt.core.network.mifos.client.api.ClientApi
import org.mobilenativefoundation.store.store5.Fetcher
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
): Store<Unit, List<ClientEntity>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getAllClients(b = true, offset = 0, limit = CLIENT_PAGE_LIMIT).pageItems
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllClients() },
        writer = { _: Unit, clients: List<ClientEntity> -> clients.forEach { dao.insertClient(it) } },
    ),
)

private const val CLIENT_PAGE_LIMIT = 100
