/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.searchrecord.SearchRecordRepository
import kpt.core.model.objects.searchrecord.GenericSearchRecord
import kpt.core.model.objects.searchrecord.SearchRecordKey

/**
 * Test double for [SearchRecordRepository]: [records] is the cached data, [matches] decides which of
 * it a key finds (default: name or description contains the query). [requested] records every key.
 */
@ExperimentalDataTestingApi
class FakeSearchRecordRepository(
    initial: List<GenericSearchRecord> = emptyList(),
    private val matches: (SearchRecordKey, GenericSearchRecord) -> Boolean = { key, r ->
        r.type == key.type.name &&
            (r.name.contains(key.query, ignoreCase = true) || r.description.contains(key.query, ignoreCase = true))
    },
) : SearchRecordRepository {

    val records = MutableStateFlow(initial)
    val requested = mutableListOf<SearchRecordKey>()

    override fun searchStream(
        keys: Flow<SearchRecordKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<List<GenericSearchRecord>> =
        screenDataStreamForTesting(
            combine(keys, records) { key, all ->
                requested += key
                ScreenState.Content(all.filter { matches(key, it) }) as ScreenState<List<GenericSearchRecord>>
            },
        )
}
