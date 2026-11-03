/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.checkerinbox

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.checkerinbox.dao.CheckerInboxDao
import kpt.core.database.checkerinbox.mapper.toDomain
import kpt.core.database.checkerinbox.mapper.toEntity
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.network.mifos.checkerinbox.api.CheckerInboxApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Maker-checker tasks awaiting approval. Unfiltered (all three query params null) because the
 * screen filters client-side over the cached set — narrowing server-side would make the cache a
 * partial view keyed by nothing.
 */
@StoreProvider(id = "checkerTasks", ttl = "5m")
@CacheKey(name = "LIST", key = "checkerTasks")
fun provideCheckerInboxStore(
    service: CheckerInboxApi,
    dao: CheckerInboxDao,
): Store<Unit, List<CheckerTask>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit -> service.getCheckerList() },
    sourceOfTruth = SourceOfTruth.of(
        // `[]` for no rows, not null-for-empty: Store5 emits NOTHING when a reader goes from rows
        // to null (measured 2026-10-23), so deciding the last task would leave it on screen.
        reader = { _: Unit -> dao.observeAll().map { rows -> rows.map { it.toDomain() } } },
        // REPLACE, not upsert: the fetch is the server's whole pending queue, so a task it no longer
        // contains has been decided and must leave the table.
        writer = { _: Unit, tasks: List<CheckerTask> -> dao.replaceAll(tasks.map { it.toEntity() }) },
        delete = { _: Unit -> dao.deleteAll() },
        // What the logout purge (`Store.clear()`) calls. Without it the purge cleared the in-memory
        // cache and left the queue — another user's approvals — in the database.
        deleteAll = { dao.deleteAll() },
    ),
)

/**
 * The WRITE half. Approve, reject and delete all take a task OUT of the pending queue; once the
 * server accepts one, `CheckerInboxCommandRepository` clears the row THROUGH this store — never a
 * DAO call in `core/data` (S5-1). It shares `checker_tasks` with [provideCheckerInboxStore], so the
 * inbox re-emits with no refresh.
 *
 * Local-only (`createOfflineMutableStore`): the network leg already ran in the command.
 * `logout = false`: the rows are purged by the paired read store's `deleteAll`.
 */
// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "checkerTask", logout = false)
@CacheKey(fn = "item", key = "checkerTask:{auditId}", params = ["auditId:Int"])
fun provideCheckerTaskStore(
    dao: CheckerInboxDao,
): MutableStore<Int, CheckerTask> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { auditId: Int -> dao.observeById(auditId).map { it?.toDomain() } },
        writer = { _: Int, task: CheckerTask -> dao.upsertAll(listOf(task.toEntity())) },
        delete = { auditId: Int -> dao.deleteById(auditId) },
    ),
)
