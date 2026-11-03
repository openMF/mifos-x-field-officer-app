/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.searchrecord

import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.common.utils.Constants
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.cache.entity.ApiResponseCacheEntity
import kpt.core.model.objects.clients.ClientAddressEntity
import kpt.core.model.objects.noncoreobjects.Identifier
import kpt.core.model.objects.searchrecord.GenericSearchRecord
import kpt.core.model.objects.searchrecord.RecordType
import kpt.core.model.objects.searchrecord.SearchRecordKey
import kpt.core.store.cache.cacheJson
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Local search over the client records this device has cached — ported from the original
 * `SearchRecordLocalDataSourceImpl` (`6b66e8a43:core/data/.../datasource/`).
 *
 * The original queried two Room tables (`ClientAddress`, `ClientIdentifier`). Here each client's
 * addresses and identifiers are one `api_response_cache` row apiece, written by
 * `clientGetClientAddresses` / `clientGetClientListIdentifiers`, so the search reads every row under
 * those prefixes and matches in memory. The rules are the original SQL's, not new ones:
 *  - addresses: a case-insensitive substring of addressLine1-3, city, state, country or postal code;
 *  - identifiers: of the id, document key, description or document type; ordered by document key,
 *    at most [IDENTIFIER_LIMIT].
 * (SQLite's LIKE is case-insensitive for ASCII; `ignoreCase` matches that, and extends it to
 * non-ASCII text, which LIKE quietly did not.)
 *
 * OFFLINE_LOCAL_ONLY and read-only: there is no network leg — a search finds only what was cached
 * — and the rows belong to the client stores, which purge them at sign-out. `logout = false` here:
 * this store owns no rows.
 */
// logout-exempt: owns no rows; the client stores it reads purge them at sign-out
@StoreProvider(id = "searchRecords", logout = false)
@CacheKey(fn = "forQuery", key = "searchRecords:{type}:{query}", params = ["type:String", "query:String"])
fun provideSearchRecordStore(cache: ApiResponseCacheDao): Store<SearchRecordKey, List<GenericSearchRecord>> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: SearchRecordKey ->
                when (key.type) {
                    RecordType.ADDRESS ->
                        cache.observeByPrefix(AppCacheKeys.ClientGetClientAddresses.forKey(""))
                            .map { rows -> searchAddresses(rows, key.query) }
                    RecordType.IDENTIFIER ->
                        cache.observeByPrefix(AppCacheKeys.ClientGetClientListIdentifiers.forKey(""))
                            .map { rows -> searchIdentifiers(rows, key.query) }
                }
            },
            writer = { _: SearchRecordKey, _: List<GenericSearchRecord> -> Unit },
        ),
    )

private const val IDENTIFIER_LIMIT = 50

private fun String?.hit(query: String) = this?.contains(query, ignoreCase = true) == true

/** A row that no longer decodes (an older payload shape) contributes nothing rather than failing the search. */
private inline fun <T> ApiResponseCacheEntity.decode(block: (String) -> List<T>): List<T> =
    runCatching { block(payload) }.getOrDefault(emptyList())

internal fun searchAddresses(rows: List<ApiResponseCacheEntity>, query: String): List<GenericSearchRecord> =
    rows.flatMap { row ->
        row.decode { cacheJson.decodeFromString(ListSerializer(ClientAddressEntity.serializer()), it) }
    }
        .filter { a ->
            listOf(a.addressLine1, a.addressLine2, a.addressLine3, a.city, a.stateName, a.countryName, a.postalCode)
                .any { it.hit(query) }
        }
        .map { a ->
            GenericSearchRecord(
                id = a.addressId,
                name = a.addressType,
                description = listOf(a.addressLine1, a.city, a.stateName, a.countryName)
                    .filter { it.isNotBlank() }
                    .joinToString(", "),
                type = RecordType.ADDRESS.name,
                metadata = mapOf(
                    Constants.CLIENT_ID to a.clientID.toString(),
                    Constants.CITY to a.city,
                    Constants.STATE to a.stateName,
                    Constants.COUNTRY to a.countryName,
                    Constants.POSTAL_CODE to a.postalCode,
                    Constants.ADDRESS_LINE_1 to a.addressLine1,
                    Constants.ADDRESS_LINE_2 to a.addressLine2,
                    Constants.ADDRESS_LINE_3 to a.addressLine3,
                ),
            )
        }

internal fun searchIdentifiers(rows: List<ApiResponseCacheEntity>, query: String): List<GenericSearchRecord> =
    rows.flatMap { row ->
        // The row is keyed per client, and an identifier's own `clientId` is not always populated by
        // the list endpoint — the key suffix is the authoritative owner.
        val ownerId = row.cacheKey.substringAfterLast(':').toIntOrNull()
        row.decode { cacheJson.decodeFromString(ListSerializer(Identifier.serializer()), it) }
            .map { it to (it.clientId ?: ownerId) }
    }
        .filter { (i, _) ->
            i.id?.toString().hit(query) || i.documentKey.hit(query) || i.description.hit(query) ||
                i.documentType?.name.hit(query)
        }
        .sortedBy { (i, _) -> i.documentKey.orEmpty() }
        .take(IDENTIFIER_LIMIT)
        .map { (i, clientId) ->
            GenericSearchRecord(
                id = i.id ?: 0,
                name = i.documentType?.name.orEmpty(),
                description = i.description.orEmpty(),
                type = RecordType.IDENTIFIER.name,
                metadata = mapOf(
                    Constants.CLIENT_ID to (clientId?.toString() ?: ""),
                    Constants.STATUS to i.status.orEmpty(),
                    Constants.DOCUMENT_KEY to i.documentKey.orEmpty(),
                ),
            )
        }
