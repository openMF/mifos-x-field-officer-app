/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.center

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.center.dao.CenterDao
import kpt.core.database.center.entity.CenterEntity
import kpt.core.network.mifos.center.api.CenterApi
import org.mobilenativefoundation.store.store5.Fetcher
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
): Store<Unit, List<CenterEntity>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getCenters(b = true, offset = 0, limit = CENTER_PAGE_LIMIT).pageItems
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.readAllCenters() },
        writer = { _: Unit, centers: List<CenterEntity> -> centers.forEach { dao.saveCenter(it) } },
    ),
)

private const val CENTER_PAGE_LIMIT = 100
