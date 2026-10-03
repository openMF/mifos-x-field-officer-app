/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
        reader = { _: Unit -> dao.observeAll().map { rows -> rows.map { it.toDomain() } } },
        writer = { _: Unit, tasks: List<CheckerTask> -> dao.upsertAll(tasks.map { it.toEntity() }) },
    ),
)
