/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.datatable

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.datatable.impl.DataTableCommandRepositoryImpl
import kpt.core.data.infra.testMutationGateway
import kpt.core.model.datatable.DataTable
import kpt.core.model.datatable.DeleteDataTablesDatatableAppTableIdDatatableIdResponse
import kpt.core.model.objects.users.UserLocation
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.datatable.api.DataTableApi
import kpt.core.network.mifos.datatable.dto.GetDataTablesResponse
import kpt.core.store.datatable.DataTableDataKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Datatable writes over REAL Store5 stores: each command ingests the server's rows through the read
 * store the rows screen observes (an in-memory stand-in for its `api_response_cache` row), so the
 * screen needs no re-read of its own.
 */
class DataTableRepositoryTest {

    private val key = DataTableDataKey("client_extra", 42)

    /** Fineract's rows for the one entity — the server assigns each row's `id`. */
    private val serverRows = mutableListOf(row(1, "a"), row(2, "b"))
    private var nextRowId = 3
    private val serverPath = mutableListOf<UserLocation>()

    private fun row(
        id: Int,
        value: String,
    ) = JsonObject(mapOf("id" to JsonPrimitive(id), "value" to JsonPrimitive(value)))

    /** The cached rows, keyed as `cachedRead` keys them. */
    private val cache = MutableStateFlow<Map<Any, Any>>(emptyMap())

    @Suppress("UNCHECKED_CAST")
    private fun <K : Any, V : Any> cachedStore(fetch: suspend (K) -> V) = StoreBuilder.from(
        fetcher = Fetcher.of { k: K -> fetch(k) },
        sourceOfTruth = SourceOfTruth.of(
            reader = { k: K -> cache.map { it[k] as V? } },
            writer = { k: K, v: V -> cache.value += k to v },
        ),
    ).build()

    private val rowsStore = cachedStore<DataTableDataKey, JsonArray> { JsonArray(serverRows.toList()) }
    private val pathStore = cachedStore<Int, List<UserLocation>> { serverPath.toList() }

    private val api = object : DataTableApi {
        override suspend fun getDatatables(apptable: String?): List<GetDataTablesResponse> = error("not used")
        override suspend fun getTableOf(table: String?): List<DataTable> = error("not used")
        override suspend fun getDataOfDataTable(dataTableName: String, entityId: Int): JsonArray =
            error("reads go through the store")
        override suspend fun getUserPathTracking(userId: Int): List<UserLocation> = error("reads go through the store")

        override suspend fun deleteDatatableEntry(
            datatable: String,
            apptableId: Long,
            datatableId: Long,
        ): DeleteDataTablesDatatableAppTableIdDatatableIdResponse {
            serverRows.removeAll { it.id() == datatableId.toInt() }
            return DeleteDataTablesDatatableAppTableIdDatatableIdResponse()
        }

        override suspend fun createEntryInDataTable(
            dataTableName: String,
            entityId: Int,
            requestPayload: Map<String, String>,
        ): GenericResponse {
            serverRows += row(nextRowId++, requestPayload.getValue("value"))
            return GenericResponse()
        }

        override suspend fun deleteEntryOfDataTableManyToMany(
            dataTableName: String,
            entityId: Int,
            dataTableRowId: Int,
        ): GenericResponse {
            serverRows.removeAll { it.id() == dataTableRowId }
            return GenericResponse()
        }

        override suspend fun addUserPathTracking(userId: Int, userLocation: UserLocation?): GenericResponse {
            serverPath += userLocation!!
            return GenericResponse()
        }
    }

    private fun JsonObject.id() = getValue("id").jsonPrimitive.int

    private fun cachedIds() = (cache.value[key] as JsonArray?)?.map { it.jsonObject.id() }

    private val commands = DataTableCommandRepositoryImpl(api, testMutationGateway(), rowsStore, pathStore)

    @Test
    fun aCreatedRowLandsInTheRowsTheScreenObserves() = runTest {
        rowsStore.fresh(key)

        assertIs<MutationResult.Applied<GenericResponse>>(
            commands.createEntryInDataTable(
                "client_extra",
                42,
                mapOf("value" to "c"),
            ),
        )

        assertEquals(listOf(1, 2, 3), cachedIds())
    }

    @Test
    fun bothDeleteEndpointsRemoveTheRow() = runTest {
        rowsStore.fresh(key)

        commands.deleteEntryOfDataTableManyToMany("client_extra", 42, 1)
        commands.deleteDatatableEntry("client_extra", 42L, 2L)

        assertEquals(emptyList(), cachedIds())
    }

    @Test
    fun aPathPointLandsInThePathTrackingRead() = runTest {
        commands.addUserPathTracking(5, UserLocation(staffId = 5, latLng = "1,2"))

        assertEquals(listOf("1,2"), (cache.value[5] as List<*>).map { (it as UserLocation).latLng })
    }

    @Test
    fun anOfflineWriteIsBlockedAndWritesNothing() = runTest {
        val offline = DataTableCommandRepositoryImpl(api, testMutationGateway(isOnline = false), rowsStore, pathStore)

        assertIs<MutationResult.Blocked>(offline.createEntryInDataTable("client_extra", 42, mapOf("value" to "c")))
        assertEquals(emptyMap(), cache.value)
        assertEquals(2, serverRows.size)
    }

    @Test
    fun aFailedIngestStillReportsTheAcceptedWrite() = runTest {
        // The server HAS the row; reporting Failed would have the officer send it again.
        val brokenRows = cachedStore<DataTableDataKey, JsonArray> { error("read timed out") }
        val repo = DataTableCommandRepositoryImpl(api, testMutationGateway(), brokenRows, pathStore)

        assertIs<MutationResult.Applied<GenericResponse>>(
            repo.createEntryInDataTable(
                "client_extra",
                42,
                mapOf("value" to "c"),
            ),
        )
        assertEquals(3, serverRows.size)
    }
}
