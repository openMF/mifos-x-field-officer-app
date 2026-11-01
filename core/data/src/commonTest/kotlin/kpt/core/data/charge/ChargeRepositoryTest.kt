/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.charge

import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.infra.StoreFactory
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.charge.impl.ChargeCommandRepositoryImpl
import kpt.core.data.charge.impl.ChargeRepositoryImpl
import kpt.core.data.fieldofficer.infra.OkHttpResponse
import kpt.core.data.infra.testMutationGateway
import kpt.core.model.charge.Charges
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.clients.Page
import kpt.core.model.objects.payloads.ChargesPayload
import kpt.core.model.objects.template.client.ChargeTemplate
import kpt.core.network.mifos.charge.api.ChargeApi
import kpt.core.store.charge.ChargeItemKey
import kpt.core.store.charge.ChargeKey
import kpt.core.store.charge.GetChargeTemplateKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * The unified charge repositories over REAL Store5 stores wired as `core/store/charge/ChargeStore.kt`
 * wires them: the read store and the local item store both over ONE in-memory `Charges` table that
 * keys on `(resourceType, resourceId, id)` like the Room entity — so a write through either is
 * visible to the reads with nothing refreshed in between.
 */
class ChargeRepositoryTest {

    private var online = true

    /**
     * The server, per owner. All three hold a charge with id 5 ON PURPOSE: on the old `id`-only key
     * whichever owner was fetched last replaced the others' row.
     */
    private val server = mutableMapOf(
        ChargeKey("clients", 9) to listOf(Charges(id = 5, clientId = 9, name = "card fee")),
        ChargeKey("loans", 1) to listOf(Charges(id = 5, name = "processing fee")),
        ChargeKey("savingsaccounts", 3) to listOf(Charges(id = 5, name = "withdrawal fee")),
    )
    private var nextId = 6

    /** The `Charges` table. Its identity is the composite primary key. */
    private val table = MutableStateFlow<List<Charges>>(emptyList())

    private fun Charges.sameRow(type: String, owner: Int, id: Int) =
        resourceType == type && resourceId == owner && this.id == id

    private fun rows(resource: ChargeResource, owner: Int) =
        table.value.filter { it.resourceType == resource.path && it.resourceId == owner }

    // ---- core/store, as provided in ChargeStore.kt ----

    private val chargeStore = StoreBuilder.from(
        fetcher = Fetcher.of { key: ChargeKey ->
            check(online) { "offline" }
            server[key].orEmpty()
        },
        sourceOfTruth = SourceOfTruth.of(
            // An empty table is an empty list, never null (NullReaderStreamTest).
            reader = { key: ChargeKey ->
                table.map { all ->
                    all.filter { it.resourceType == key.resourceType && it.resourceId == key.resourceId }
                }
            },
            // ChargeDao.replaceCharges: the owner's rows become the fetch, stamped with the owner.
            writer = { key: ChargeKey, fetched: List<Charges> ->
                table.value = table.value
                    .filterNot { it.resourceType == key.resourceType && it.resourceId == key.resourceId } +
                    fetched.map { it.copy(resourceType = key.resourceType, resourceId = key.resourceId) }
            },
        ),
    ).build()

    private val chargeItem = StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: ChargeItemKey ->
                table.map { all -> all.firstOrNull { it.sameRow(key.resourceType, key.resourceId, key.chargeId) } }
            },
            writer = { key: ChargeItemKey, charge: Charges ->
                table.value = table.value.filterNot { it.sameRow(key.resourceType, key.resourceId, key.chargeId) } +
                    charge.copy(resourceType = key.resourceType, resourceId = key.resourceId)
            },
            delete = { key: ChargeItemKey ->
                table.value = table.value.filterNot { it.sameRow(key.resourceType, key.resourceId, key.chargeId) }
            },
        ),
    )

    private val templateStore = StoreBuilder.from(
        fetcher = Fetcher.of { _: GetChargeTemplateKey ->
            check(online) { "offline" }
            ChargeTemplate(false, emptyList())
        },
    ).build()

    // ---- core/network ----

    private var sent = 0

    private val api = object : ChargeApi {
        override suspend fun createCharges(
            resourceType: String,
            resourceId: Int,
            chargesPayload: ChargesPayload,
        ): HttpResponse {
            sent++
            val key = ChargeKey(resourceType, resourceId)
            // Fineract names the row from the charge DEFINITION, not from the request.
            server[key] = server[key].orEmpty() +
                Charges(
                    id = nextId++,
                    chargeId = chargesPayload.chargeId,
                    name = "late fee",
                    amount = chargesPayload.amount?.toDouble(),
                )
            return OkHttpResponse
        }

        override suspend fun deleteCharge(resourceType: String, resourceId: Int, chargeId: Int) {
            sent++
            val key = ChargeKey(resourceType, resourceId)
            server[key] = server[key].orEmpty().filterNot { it.id == chargeId }
        }

        override suspend fun updateCharge(
            resourceType: String,
            resourceId: Int,
            chargeId: Int,
            payload: ChargesPayload,
        ) {
            sent++
            val key = ChargeKey(resourceType, resourceId)
            // Fineract recomputes the row — outstanding follows the new amount.
            server[key] = server.getValue(key).map {
                if (it.id == chargeId) {
                    it.copy(
                        amount = payload.amount?.toDouble(),
                        amountOutstanding = payload.amount?.toDouble(),
                    )
                } else {
                    it
                }
            }
        }

        override suspend fun listAllCharges(): HttpResponse = unused()
        override suspend fun getChargeTemplate(resourceType: String, resourceId: Int): ChargeTemplate = unused()
        override suspend fun getListOfPagingCharges(
            resourceType: String,
            resourceId: Int,
            offset: Int,
            limit: Int,
        ): Page<Charges> = unused()
        override suspend fun getListOfClientCharges(resourceType: String, resourceId: Int): Page<Charges> = unused()
        override suspend fun getListOfOtherAccountCharge(
            resourceType: String,
            resourceId: Int,
        ): List<Charges> = unused()
        override suspend fun getCharge(resourceType: String, resourceId: Int, chargeId: Int): Charges = unused()

        private fun unused(): Nothing = error("not exercised; reads go through the stores")
    }

    private val repository = ChargeRepositoryImpl(chargeStore, templateStore)

    private fun commands(isOnline: Boolean = true) =
        ChargeCommandRepositoryImpl(api, testMutationGateway(isOnline = isOnline), chargeStore, chargeItem)

    private suspend fun fetchAll() {
        chargeStore.fresh(ChargeKey("clients", 9))
        chargeStore.fresh(ChargeKey("loans", 1))
        chargeStore.fresh(ChargeKey("savingsaccounts", 3))
    }

    // ---- reads ----

    @Test
    fun eachResourceReadsItsOwnCharges() = runTest {
        assertEquals("card fee", repository.charge(ChargeResource.CLIENT, 9, 5)?.name)
        assertEquals("processing fee", repository.charge(ChargeResource.LOAN, 1, 5)?.name)
        assertEquals("withdrawal fee", repository.charge(ChargeResource.SAVINGS, 3, 5)?.name)
    }

    @Test
    fun theSameChargeIdUnderClientLoanAndSavingsCoexists() = runTest {
        fetchAll()

        // Three rows, one per owner — on the old key the last fetch left exactly one.
        assertEquals(3, table.value.count { it.id == 5 })
        assertEquals(listOf("card fee"), rows(ChargeResource.CLIENT, 9).map { it.name })
        assertEquals(listOf("processing fee"), rows(ChargeResource.LOAN, 1).map { it.name })
        assertEquals(listOf("withdrawal fee"), rows(ChargeResource.SAVINGS, 3).map { it.name })
    }

    @Test
    fun aChargeIsStampedWithItsOwnerBecauseTheEndpointDoesNotEchoIt() = runTest {
        chargeStore.fresh(ChargeKey("loans", 1))

        val row = rows(ChargeResource.LOAN, 1).single()
        assertEquals("loans", row.resourceType)
        assertEquals(1, row.resourceId)
    }

    @Test
    fun offlineAFetchedChargeIsServedFromTheTable() = runTest {
        chargeStore.fresh(ChargeKey("loans", 1))
        online = false

        assertEquals("processing fee", repository.charge(ChargeResource.LOAN, 1, 5)?.name)
    }

    @Test
    fun anUncachedChargeOfflineIsNull() = runTest {
        online = false
        assertNull(repository.charge(ChargeResource.LOAN, 1, 5))
    }

    // ---- writes land where the reads observe ----

    @Test
    fun aLeviedChargeLandsInThatResourcesListOnly() = runTest {
        fetchAll()

        assertIs<MutationResult.Applied<Unit>>(
            commands().createCharge(ChargeResource.SAVINGS, 3, ChargesPayload(chargeId = 2, amount = "12")),
        )

        // Fineract's row, ingested through the read store — the name came from the server.
        assertEquals(listOf("withdrawal fee", "late fee"), rows(ChargeResource.SAVINGS, 3).map { it.name })
        assertEquals(1, rows(ChargeResource.LOAN, 1).size)
        assertEquals(1, rows(ChargeResource.CLIENT, 9).size)
    }

    @Test
    fun anUpdateLandsTheServersRecomputedRow() = runTest {
        fetchAll()

        commands().updateCharge(ChargeResource.LOAN, 1, 5, ChargesPayload(amount = "40"))

        assertEquals(40.0, rows(ChargeResource.LOAN, 1).single().amountOutstanding)
        // The client's charge with the same id is untouched.
        assertNull(rows(ChargeResource.CLIENT, 9).single().amountOutstanding)
    }

    @Test
    fun aDeleteRemovesOnlyThatOwnersRow() = runTest {
        fetchAll()

        assertIs<MutationResult.Applied<Unit>>(commands().deleteCharge(ChargeResource.CLIENT, 9, 5))

        assertEquals(emptyList(), rows(ChargeResource.CLIENT, 9))
        assertEquals(listOf(5), rows(ChargeResource.LOAN, 1).map { it.id })
        assertEquals(listOf(5), rows(ChargeResource.SAVINGS, 3).map { it.id })
    }

    @Test
    fun aRefetchDropsAChargeTheServerNoLongerHas() = runTest {
        chargeStore.fresh(ChargeKey("loans", 1))
        server[ChargeKey("loans", 1)] = emptyList()

        chargeStore.fresh(ChargeKey("loans", 1))

        // The upsert-only writer left a waived charge on screen forever.
        assertEquals(emptyList(), rows(ChargeResource.LOAN, 1))
    }

    @Test
    fun offlineEveryWriteIsBlockedAndWritesNothing() = runTest {
        online = false
        val offline = commands(isOnline = false)

        assertIs<MutationResult.Blocked>(offline.createCharge(ChargeResource.CLIENT, 9, ChargesPayload(chargeId = 3)))
        assertIs<MutationResult.Blocked>(offline.updateCharge(ChargeResource.LOAN, 1, 5, ChargesPayload(amount = "1")))
        assertIs<MutationResult.Blocked>(offline.deleteCharge(ChargeResource.SAVINGS, 3, 5))
        assertEquals(0, sent)
        assertEquals(emptyList(), table.value)
    }
}
