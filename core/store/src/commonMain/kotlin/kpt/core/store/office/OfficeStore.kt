/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.office

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.office.dao.OfficeDao
import kpt.core.model.office.Office
import kpt.core.network.mifos.office.api.OfficeApi
import kpt.core.store.mapping.toEntity
import kpt.core.store.mapping.toModel
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Offices — READ-CACHE archetype (`createStore`).
 *
 * The first vertical of the DataManager→Store5 collapse. It replaces the
 * `Repository → DataManagerOffice → { OfficeApi , OfficeDaoHelper }` chain, in which the
 * Room branch was commented out and every read went to the network — so this store is not a
 * re-wrapping of the old path, it is the offline-first read that path intended.
 *
 * `fetcher` is the Ktorfit service; `sourceOfTruth` is the Room DAO, so a field officer with no
 * connectivity still renders the office list from cache and a reconnect refreshes it. Offices are
 * reference data — server-authoritative, low-churn, read-only on the device — which is exactly
 * the read-cache shape rather than a ledger (no delta/paging) or a mutable store (no local writes).
 */
@StoreProvider(id = "offices", ttl = "24h")
@CacheKey(name = "LIST", key = "offices")
fun provideOfficeStore(
    service: OfficeApi,
    dao: OfficeDao,
): Store<Unit, List<Office>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit -> service.allOffices() },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllOffices().map { rows -> rows.map { it.toModel() } } },
        writer = { _: Unit, offices: List<Office> -> dao.insertOffices(offices.map { it.toEntity() }) },
        delete = { _: Unit -> dao.deleteAll() },
        // What the logout purge (`Store.clear()`) calls; without it the rows outlived the session.
        deleteAll = { dao.deleteAll() },
    ),
)
