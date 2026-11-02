/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kpt.core.data.collectionsheet.keyedReadStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StoreOneShotReadTest {

    private var server = "v1"
    private var online = true
    private val rows = MutableStateFlow<Map<Int, String>>(emptyMap())
    private val store = keyedReadStore(rows) { _: Int ->
        check(online) { "offline" }
        server
    }

    // The S5-5 defect: `cached(key, refresh = false)` would answer "v1" here forever, because the
    // value is already in the (persistent) source of truth.
    @Test
    fun aCachedValueIsReplacedByWhatTheServerSaysNow() = runTest {
        assertEquals("v1", store.freshOrCached(1))
        server = "v2"

        assertEquals("v2", store.freshOrCached(1))
        assertEquals("v2", rows.value[1])
    }

    @Test
    fun offlineAnswersWithTheLastFetchedValue() = runTest {
        store.freshOrCached(1)
        online = false

        assertEquals("v1", store.freshOrCached(1))
    }

    @Test
    fun offlineWithNothingCachedIsNull() = runTest {
        online = false

        assertNull(store.freshOrCached(1))
    }
}
