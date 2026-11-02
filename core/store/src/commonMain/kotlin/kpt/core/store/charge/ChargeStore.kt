/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.charge

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.charge.dao.ChargeDao
import kpt.core.model.charge.Charges
import kpt.core.network.mifos.charge.api.ChargeApi
import kpt.core.store.mapping.toEntity
import kpt.core.store.mapping.toModel
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * The charges levied on one resource. [resourceType] is the Fineract path segment
 * (`clients` / `loans` / `savingsaccounts`) — the same pair the endpoint and the table key on.
 */
data class ChargeKey(val resourceType: String, val resourceId: Int)

/** One charge ROW — the owner plus the charge's id. */
data class ChargeItemKey(val resourceType: String, val resourceId: Int, val chargeId: Int)

/**
 * Charges on ANY resource — READ-CACHE (`createStore`), keyed by the owner.
 *
 * ONE store for clients, loans and savings, where there used to be a client store and a loan store
 * (and nothing for savings). Their split was justified by the two reading different Room columns
 * (`clientId` vs `loanId`); the table now keys on the owner itself, so the only remaining
 * difference is the list ENVELOPE — `clients/{id}/charges` answers a `Page`, the other two a bare
 * array — and that is one branch in the fetcher.
 *
 * The key is the whole owner pair, never a bare id: an `Int` key would serve client 5's charges to
 * loan 5 (the Store5 `inconsistent-read-paths` anti-pattern).
 */
@StoreProvider(id = "charges", ttl = "15m")
@CacheKey(
    fn = "forResource",
    key = "charges:{resourceType}:{resourceId}",
    params = ["resourceType:String", "resourceId:Int"],
)
fun provideChargeStore(
    service: ChargeApi,
    dao: ChargeDao,
): Store<ChargeKey, List<Charges>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: ChargeKey ->
        if (key.resourceType == CLIENTS) {
            service.getListOfClientCharges(resourceType = CLIENTS, resourceId = key.resourceId).pageItems
        } else {
            service.getListOfOtherAccountCharge(resourceType = key.resourceType, resourceId = key.resourceId)
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        // An empty table reads as an empty LIST, never as null — a null reader made Store5 emit
        // Loading and refetch instead of showing "no charges" (see NullReaderStreamTest).
        reader = { key: ChargeKey ->
            dao.observeCharges(key.resourceType, key.resourceId).map { rows -> rows.map { it.toModel() } }
        },
        // REPLACE, not upsert: the fetch is the owner's whole list, so a charge it no longer
        // contains was removed on the server. Stamps the owner, which the endpoints do not echo.
        writer = { key: ChargeKey, charges: List<Charges> ->
            dao.replaceCharges(key.resourceType, key.resourceId, charges.map { it.toEntity() })
        },
        delete = { key: ChargeKey -> dao.deleteCharges(key.resourceType, key.resourceId) },
        // The logout purge (`Store.clear()`) reaches the database only through this.
        deleteAll = { dao.deleteAllCharges() },
    ),
)

/**
 * The WRITE half: local-only (`createOfflineMutableStore`) over the SAME `Charges` rows
 * [provideChargeStore] observes, so `ChargeCommandRepository` records a delete through a store and
 * the list re-emits with no refetch. `logout = false`: the rows are purged by the paired read
 * store's `deleteAll` (same pairing as `document/DocumentStore.kt`).
 */
// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "chargeItem", logout = false)
@CacheKey(
    fn = "item",
    key = "chargeItem:{resourceType}:{resourceId}:{chargeId}",
    params = ["resourceType:String", "resourceId:Int", "chargeId:Int"],
)
fun provideChargeItemStore(
    dao: ChargeDao,
): MutableStore<ChargeItemKey, Charges> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: ChargeItemKey ->
            dao.observeCharge(key.resourceType, key.resourceId, key.chargeId).map { it?.toModel() }
        },
        writer = { key: ChargeItemKey, charge: Charges ->
            dao.upsertCharge(key.resourceType, key.resourceId, charge.toEntity())
        },
        delete = { key: ChargeItemKey -> dao.deleteCharge(key.resourceType, key.resourceId, key.chargeId) },
    ),
)

private const val CLIENTS = "clients"
