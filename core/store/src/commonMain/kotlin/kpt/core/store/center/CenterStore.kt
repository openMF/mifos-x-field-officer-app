/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.center

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.center.dao.CenterDao
import kpt.core.model.center.Center
import kpt.core.network.mifos.center.api.CenterApi
import kpt.core.store.mapping.toEntity
import kpt.core.store.mapping.toModel
import kpt.core.store.offline.keepingDownloadedFlag
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Centers — READ-CACHE (`createStore`).
 *
 * A field officer opens the center list in the field, so this is the archetype that matters most
 * for them: Room answers immediately from cache with no connectivity, and a reconnect refreshes.
 * The wire call is paged (`Page<CenterEntity>`) while Room holds the accumulated rows, so the
 * fetcher unwraps `pageItems` and the SoT writes them through.
 */
@StoreProvider(id = "centers", ttl = "1h")
@CacheKey(name = "LIST", key = "centers")
fun provideCenterStore(
    service: CenterApi,
    dao: CenterDao,
): Store<Unit, List<Center>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getCenters(b = true, offset = 0, limit = CENTER_PAGE_LIMIT).pageItems
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.readAllCenters().map { rows -> rows.map { it.toModel() } } },
        writer = { _: Unit, centers: List<Center> ->
            // Keep the local "downloaded" flag — the server's rows do not carry it (KeepDownloadedFlag.kt).
            val existing = dao.readAllCenters().first()
            centers.map { it.toEntity() }.keepingDownloadedFlag(existing).forEach { dao.saveCenter(it) }
        },
        // The list IS the table, so clearing its one key and the logout purge (`Store.clear()`) both
        // empty it. Without these the purge cleared memory and left every center for the next officer.
        delete = { _: Unit -> dao.deleteAllCenters() },
        deleteAll = { dao.deleteAllCenters() },
    ),
)

/**
 * The WRITE half for centers: one row of the `Center` table, keyed by center id.
 *
 * `CenterCommandRepository` records an accepted create / activate / close by writing THROUGH this
 * store — never a DAO call in `core/data` (S5-1). It shares the table with [provideCenterStore], so
 * the center list and detail re-emit with no refresh.
 *
 * Local-only (`createOfflineMutableStore`): the command already ran the network leg. `logout = false`
 * because Store5 keeps `MutableStore` outside the purged `Store` hierarchy; the rows are still purged
 * by the paired `centers` store's `deleteAll` (same pairing as `document/DocumentStore.kt`).
 */
// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "centerItem", logout = false)
@CacheKey(fn = "item", key = "centerItem:{id}", params = ["id:Int"])
fun provideCenterItemStore(
    dao: CenterDao,
): MutableStore<Int, Center> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: Int -> dao.observeCenter(id).map { it?.toModel() } },
        writer = { _: Int, center: Center -> dao.saveCenter(center.toEntity()) },
        delete = { id: Int -> dao.deleteCenterById(id) },
    ),
)

private const val CENTER_PAGE_LIMIT = 100
