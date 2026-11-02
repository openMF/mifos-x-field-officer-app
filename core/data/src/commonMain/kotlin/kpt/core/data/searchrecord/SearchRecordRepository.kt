/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.searchrecord

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.objects.searchrecord.GenericSearchRecord
import kpt.core.model.objects.searchrecord.SearchRecordKey

/**
 * Search over the client addresses / identifiers already cached on this device — no network.
 *
 * ONE stream for the screen's whole life, driven by [keys]: each new query is a new key, so typing
 * re-reads the store instead of building a stream per keystroke. An empty list is a real result
 * (nothing matched), not "no data yet".
 */
interface SearchRecordRepository {
    fun searchStream(keys: Flow<SearchRecordKey>, scope: CoroutineScope): ScreenDataStream<List<GenericSearchRecord>>
}
