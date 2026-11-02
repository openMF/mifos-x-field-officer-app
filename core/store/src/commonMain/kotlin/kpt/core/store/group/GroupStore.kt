/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.group

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.group.dao.GroupsDao
import kpt.core.model.group.Group
import kpt.core.network.mifos.group.api.GroupApi
import kpt.core.store.mapping.toEntity
import kpt.core.store.mapping.toModel
import kpt.core.store.offline.keepingDownloadedFlag
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Groups — READ-CACHE (`createStore`). Same shape as centers; groups sit under a center in the
 * Fineract hierarchy and are read on the same offline paths.
 */
@StoreProvider(id = "groups", ttl = "1h")
@CacheKey(name = "LIST", key = "groups")
fun provideGroupStore(
    service: GroupApi,
    dao: GroupsDao,
): Store<Unit, List<Group>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getGroups(b = true, offset = 0, limit = GROUP_PAGE_LIMIT).pageItems
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllGroups().map { rows -> rows.map { it.toModel() } } },
        writer = { _: Unit, groups: List<Group> ->
            // Keep the local "downloaded" flag — the server's rows do not carry it (KeepDownloadedFlag.kt).
            val existing = dao.getAllGroups().first()
            groups.map { it.toEntity() }.keepingDownloadedFlag(existing).forEach { dao.insertGroup(it) }
        },
        // The list IS the table, so clearing its one key and the logout purge (`Store.clear()`) both
        // empty it. Without these the purge cleared memory and left every group for the next officer.
        delete = { _: Unit -> dao.deleteAllGroups() },
        deleteAll = { dao.deleteAllGroups() },
    ),
)

/**
 * The WRITE half for groups: one row of `GroupTable`, keyed by group id.
 *
 * `GroupCommandRepository` records an accepted create / activate by writing THROUGH this store —
 * never a DAO call in `core/data` (S5-1). It shares the table with [provideGroupStore], so the group
 * list and detail re-emit with no refresh.
 *
 * Local-only (`createOfflineMutableStore`): the command already ran the network leg. `logout = false`
 * because Store5 keeps `MutableStore` outside the purged `Store` hierarchy; the rows are still purged
 * by the paired `groups` store's `deleteAll` (same pairing as `document/DocumentStore.kt`).
 */
// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "groupItem", logout = false)
@CacheKey(fn = "item", key = "groupItem:{id}", params = ["id:Int"])
fun provideGroupItemStore(
    dao: GroupsDao,
): MutableStore<Int, Group> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: Int -> dao.observeGroup(id).map { it?.toModel() } },
        writer = { _: Int, group: Group -> dao.insertGroup(group.toEntity()) },
        delete = { id: Int -> dao.deleteGroupById(id) },
    ),
)

private const val GROUP_PAGE_LIMIT = 100
