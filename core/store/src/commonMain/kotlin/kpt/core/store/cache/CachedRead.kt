/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.cache

import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.cache.entity.ApiResponseCacheEntity
import kpt.core.database.utils.getCurrentTimeInMillis
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * JSON for the shared read cache.
 *
 * `ignoreUnknownKeys` matters here specifically: a cached row outlives the app version that wrote
 * it, so a field that Fineract adds — or one the fork drops — must not make an existing row
 * unreadable. An officer with no signal would otherwise lose the cached answer at exactly the
 * moment they need it.
 */
@PublishedApi
internal val cacheJson: Json = Json { ignoreUnknownKeys = true }

/**
 * An offline-first Store for a read with no table of its own.
 *
 * The fetcher is the API call; the source of truth is one row in [ApiResponseCacheEntity], keyed by
 * the same `@CacheKey` string the stream uses. Behaviour matches a hand-written store: cache is
 * served first, a refresh runs behind it, and with no signal the last-known answer still renders.
 *
 * Deliberately no `validator` — see `scripts/product-health/checks/offline-first-reads.sh`. A TTL
 * here would make an expired row unusable, which offline means unusable full stop.
 *
 * @param keyOf the cache key for a given store key — pass the generated `AppCacheKeys.X.y(...)`.
 */
/**
 * [cachedRead] for an endpoint whose wire type is not what the app should see: [fetch] returns the
 * row type the network layer produces, [map] turns it into the `core/model` type the store caches and
 * emits. Entities stay below `core/store`; everything above it reads [T].
 *
 * The cached JSON is the MAPPED value. The twins keep the rows' field names, so a row cached before
 * this mapping existed still decodes.
 */
inline fun <Key : Any, R : Any, reified T : Any> cachedRead(
    dao: ApiResponseCacheDao,
    crossinline keyOf: (Key) -> String,
    crossinline fetch: suspend (Key) -> R,
    crossinline map: (R) -> T,
): Store<Key, T> = cachedRead(dao = dao, keyOf = keyOf, fetch = { key: Key -> map(fetch(key)) })

/**
 * Store that fetches with `fetch`, caches the value as JSON in the response cache under `keyOf(key)`, and serves
 * the cache offline.
 */
inline fun <Key : Any, reified T : Any> cachedRead(
    dao: ApiResponseCacheDao,
    crossinline keyOf: (Key) -> String,
    crossinline fetch: suspend (Key) -> T,
): Store<Key, T> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: Key -> fetch(key) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: Key ->
            dao.observe(keyOf(key)).map { row ->
                row?.let { cacheJson.decodeFromString(serializer<T>(), it.payload) }
            }
        },
        writer = { key: Key, value: T ->
            dao.upsert(
                ApiResponseCacheEntity(
                    cacheKey = keyOf(key),
                    payload = cacheJson.encodeToString(serializer<T>(), value),
                    fetchedAtMs = getCurrentTimeInMillis(),
                ),
            )
        },
        delete = { key: Key -> dao.delete(keyOf(key)) },
        // `Store.clear()` — what the logout purge runs — reaches the database ONLY through this.
        // Without it every cachedRead store (~70) cleared its memory at sign-out and left its rows
        // for the next officer to read. The table is shared, so this wipes it whole: each of the
        // stores runs it at logout, and they all mean "nothing cached survives the session".
        deleteAll = { dao.deleteAll() },
    ),
)
