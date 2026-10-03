/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.search

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.objects.SearchedEntity
import kpt.core.store.search.SearchResourcesKey

/**
 * Fineract resource search — the read behind the Home tab.
 *
 * `SearchApi`, `SearchKeyedReadStores` and `SearchedEntity` all existed; only this repository was
 * missing, which is why S2 could not start (`feature/home/MIGRATION.md` §3). The original consumed a
 * `SearchRepository` of the same name from `com.mifos.core.data`.
 */
interface SearchRepository {

    /**
     * Results for [key], served through the store so the last answer still renders with no signal.
     *
     * A BLANK query returns empty without reaching the server — see [SearchRepositoryImpl]. The
     * search endpoint treats an empty `query` as unfiltered and would return the whole directory.
     */
    fun searchStream(key: SearchResourcesKey, scope: CoroutineScope): ScreenDataStream<List<SearchedEntity>>
}
