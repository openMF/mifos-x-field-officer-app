/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.collectionsheet

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.base.store.infra.StoreFactory
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder

/*
 * REAL Store5 stores over in-memory "tables", shaped the way `core/store` shapes them, for the
 * center / group / collection-sheet repository tests. A table is a `MutableStateFlow` map, so every
 * store sharing one sees each other's writes exactly as Room-backed stores sharing a table do.
 */

/** A `cachedRead`-shaped keyed read: the fetch, then one row per key in [rows]. */
internal fun <K : Any, V : Any> keyedReadStore(
    rows: MutableStateFlow<Map<K, V>>,
    fetch: suspend (K) -> V,
): Store<K, V> = StoreBuilder.from(
    fetcher = Fetcher.of { key: K -> fetch(key) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: K -> rows.map { it[key] } },
        writer = { key: K, value: V -> rows.value += key to value },
        delete = { key: K -> rows.value -= key },
        deleteAll = { rows.value = emptyMap() },
    ),
).build()

/** A list read over the whole [rows] table — the `centers` / `groups` shape (upsert, never replace). */
internal fun <V : Any> listReadStore(
    rows: MutableStateFlow<Map<Int, V>>,
    idOf: (V) -> Int,
    fetch: suspend () -> List<V>,
): Store<Unit, List<V>> = StoreBuilder.from(
    fetcher = Fetcher.of { _: Unit -> fetch() },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> rows.map { it.values.toList() } },
        writer = { _: Unit, list: List<V> -> rows.value += list.associateBy(idOf) },
        delete = { _: Unit -> rows.value = emptyMap() },
        deleteAll = { rows.value = emptyMap() },
    ),
).build()

/** The local write half over the same [rows] table — `createOfflineMutableStore`, as in production. */
internal fun <V : Any> localRowStore(rows: MutableStateFlow<Map<Int, V>>): MutableStore<Int, V> =
    StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: Int -> rows.map { it[id] } },
            writer = { id: Int, value: V -> rows.value += id to value },
            delete = { id: Int -> rows.value -= id },
        ),
    )
