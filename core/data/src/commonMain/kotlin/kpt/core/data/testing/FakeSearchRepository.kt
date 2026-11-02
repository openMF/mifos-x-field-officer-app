/*
 * Copyright 2026 Mifos Initiative
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.search.SearchRepository
import kpt.core.model.objects.SearchedEntity
import kpt.core.model.search.SearchResourcesKey

/**
 * Test double for [SearchRepository], owned by the module that owns the CONTRACT.
 *
 * Lives in `core/data` for the same reason [FakeClientCommandRepository] does: `SearchRepository`
 * is a `core/data` interface and its signature names `SearchResourcesKey`, a `core/network` type.
 * Faking it inside `feature/home` would drag `core:network` onto that feature's test classpath,
 * which inverts the layering — a feature talking to network types.
 *
 * ## One state PER KEY
 *
 * Deliberately not a single shared state flow. Search is the only read in this port with a DYNAMIC
 * key, so the behaviour worth testing is what happens when the key CHANGES mid-flight — a slow
 * first query answering after a second has been submitted. A shared state cannot express that; a
 * per-key map can, by resolving the keys out of order on purpose.
 */
@ExperimentalDataTestingApi
class FakeSearchRepository : SearchRepository {

    /** Live state per key, so a test can resolve searches out of order. */
    val states = mutableMapOf<SearchResourcesKey, MutableStateFlow<ScreenState<List<SearchedEntity>>>>()

    /** Every key asked for, in order — proves WHAT was searched, not merely that something was. */
    val requested = mutableListOf<SearchResourcesKey>()

    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    override fun searchStream(
        key: SearchResourcesKey,
        scope: CoroutineScope,
    ): ScreenDataStream<List<SearchedEntity>> {
        requested += key
        return screenDataStreamForTesting(stateFor(key), refreshTrigger = refreshes)
    }

    /** Answer [key] with [found] — an empty Content when nothing is passed, as the real stream does. */
    fun resolve(key: SearchResourcesKey, vararg found: SearchedEntity) {
        stateFor(key).value = ScreenState.Content(found.toList())
    }

    private fun stateFor(key: SearchResourcesKey) =
        states.getOrPut(key) { MutableStateFlow(ScreenState.Loading) }
}
