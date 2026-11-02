/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.client

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.cache.entity.ApiResponseCacheEntity
import kpt.core.database.utils.getCurrentTimeInMillis
import kpt.core.model.client.Client
import kpt.core.model.objects.clients.ClientAddressResponse
import kpt.core.model.objects.noncoreobjects.Identifier
import kpt.core.network.mifos.client.api.ClientApi
import kpt.core.store.cache.cacheJson
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * The client profile photo: `GET clients/{id}/images` → the shared read cache → this store →
 * `ClientRepository.clientImage`. It used to be fetched straight from `ClientApi` in `core/data` and
 * cached nowhere, so a photo seen once was gone the moment the signal was.
 *
 * The value is the `data:image/...;base64,...` STRING Fineract returns, so it needs no table of its
 * own — `api_response_cache` holds text. An EMPTY string is the server's "this client has no photo"
 * (a 404, the common case): cached as an answer, so a photo deleted elsewhere stops showing instead
 * of the last copy being served forever as the offline fallback.
 */
@StoreProvider(id = "clientImage", ttl = "12h")
@CacheKey(fn = "forKey", key = "clientImage:{key}", params = ["key:String"])
fun provideClientImageStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<Int, String> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientImage.forKey(key.toString()) },
    fetch = { key ->
        try {
            clientApi.getClientImage(key).bodyAsText()
        } catch (notFound: ResponseException) {
            if (notFound.response.status != HttpStatusCode.NotFound) throw notFound
            ""
        }
    },
)

/*
 * The WRITE half for the client reads that live in the shared read cache. A client write is an
 * `OnlineRequired` command, and once the server accepts it `ClientCommandRepository` records the
 * outcome by writing THROUGH these stores — never a DAO call in `core/data` (S5-1). Each addresses
 * the SAME `api_response_cache` row as its read store in `ClientKeyedReadStores.kt`, so the read
 * streams re-emit with no refresh.
 *
 * Local-only (`createOfflineMutableStore`): the network leg already ran. `logout = false`: the rows
 * are purged by the paired read store's `deleteAll` (same pairing as `queue/OfflineQueueStores.kt`).
 */

/** One pinpoint row — the client plus the datatable row's id. */
data class PinpointItemKey(val clientId: Int, val datatableId: Int)

/** One identifier row — the client plus the identifier's id. */
data class IdentifierItemKey(val clientId: Long, val identifierId: Long)

// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "clientLocal", logout = false)
@CacheKey(fn = "byId", key = "clientLocal:{id}", params = ["id:Int"])
fun provideClientLocalStore(
    cache: ApiResponseCacheDao,
): MutableStore<Int, Client> = cachedLocal(cache) { id -> AppCacheKeys.ClientGetClient.forKey(id.toString()) }

// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "clientPinpointItem", logout = false)
@CacheKey(
    fn = "item",
    key = "clientPinpointItem:{clientId}:{datatableId}",
    params = ["clientId:Int", "datatableId:Int"],
)
fun provideClientPinpointItemStore(
    cache: ApiResponseCacheDao,
): MutableStore<PinpointItemKey, ClientAddressResponse> = cachedListItem(
    dao = cache,
    rowKeyOf = { key -> AppCacheKeys.ClientGetClientPinpointLocations.forKey(key.clientId.toString()) },
    matches = { key, row -> row.id == key.datatableId },
)

// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "clientIdentifierItem", logout = false)
@CacheKey(
    fn = "item",
    key = "clientIdentifierItem:{clientId}:{identifierId}",
    params = ["clientId:Long", "identifierId:Long"],
)
fun provideClientIdentifierItemStore(
    cache: ApiResponseCacheDao,
): MutableStore<IdentifierItemKey, Identifier> = cachedListItem(
    dao = cache,
    rowKeyOf = { key -> AppCacheKeys.ClientGetClientListIdentifiers.forKey(key.clientId.toString()) },
    matches = { key, row -> row.id?.toLong() == key.identifierId },
)

/** A local-only store over the cache row [keyOf] names — the same row a `cachedRead` store reads. */
internal inline fun <Key : Any, reified T : Any> cachedLocal(
    dao: ApiResponseCacheDao,
    crossinline keyOf: (Key) -> String,
): MutableStore<Key, T> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: Key ->
            dao.observe(keyOf(key)).map { row -> row?.let { cacheJson.decodeFromString(serializer<T>(), it.payload) } }
        },
        writer = { key: Key, value: T ->
            dao.upsert(
                ApiResponseCacheEntity(
                    cacheKey = keyOf(key),
                    payload = cacheJson.encodeToString(serializer<T>(), value),
                    // A local edit is not a fetch: keep the row's own fetch time when there is one.
                    fetchedAtMs = dao.observe(keyOf(key)).first()?.fetchedAtMs ?: getCurrentTimeInMillis(),
                ),
            )
        },
        delete = { key: Key -> dao.delete(keyOf(key)) },
    ),
)

/**
 * A local-only store over ONE ITEM of a cached list row — the per-item write half of a `cachedRead`
 * list, the way `documentItem` is for `documents`. Writes replace the matching item (or append it);
 * clears remove it.
 *
 * A list that was never fetched is left absent rather than seeded with the one item: a one-element
 * row would then read as the client's WHOLE list. The row keeps its `fetchedAtMs`, since a local
 * edit is not a fetch and must not make the "updated N ago" indicator claim one.
 */
internal inline fun <Key : Any, reified Item : Any> cachedListItem(
    dao: ApiResponseCacheDao,
    crossinline rowKeyOf: (Key) -> String,
    crossinline matches: (Key, Item) -> Boolean,
): MutableStore<Key, Item> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: Key ->
            dao.observe(rowKeyOf(key)).map { row ->
                row?.let { cacheJson.decodeFromString(serializer<List<Item>>(), it.payload) }
                    ?.firstOrNull { matches(key, it) }
            }
        },
        writer = { key: Key, item: Item ->
            val row = dao.observe(rowKeyOf(key)).first()
            if (row != null) {
                val items = cacheJson.decodeFromString(serializer<List<Item>>(), row.payload)
                val updated = if (items.any { matches(key, it) }) {
                    items.map { if (matches(key, it)) item else it }
                } else {
                    items + item
                }
                dao.upsert(row.copy(payload = cacheJson.encodeToString(serializer<List<Item>>(), updated)))
            }
        },
        delete = { key: Key ->
            val row = dao.observe(rowKeyOf(key)).first()
            if (row != null) {
                val items = cacheJson.decodeFromString(serializer<List<Item>>(), row.payload)
                val kept = items.filterNot { matches(key, it) }
                dao.upsert(
                    row.copy(payload = cacheJson.encodeToString(serializer<List<Item>>(), kept)),
                )
            }
        },
    ),
)
