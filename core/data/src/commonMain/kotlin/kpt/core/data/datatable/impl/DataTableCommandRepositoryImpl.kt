/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.datatable.impl

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonArray
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.datatable.DataTableCommandRepository
import kpt.core.model.objects.users.UserLocation
import kpt.core.network.mifos.datatable.api.DataTableApi
import kpt.core.store.config.AppStoreIds
import kpt.core.store.datatable.DataTableDataKey
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/**
 * Datatable writes, through the single write door ([MutationGateway.command]).
 *
 * Network → store → database: [CommandSpec.endpoint] is the Fineract call and
 * [CommandSpec.localApply] ingests the server's rows for the written entity through the read store
 * the screen observes (`fresh` — network → SoT writer → `api_response_cache`), so the rows stream
 * re-emits by itself. It used to end at the endpoint with `payload = Unit`, and the rows screen had
 * to force a re-read after every delete to stop the row lingering.
 *
 * Why ingestion and not a locally composed change: a datatable's rows are a TENANT-shaped
 * `JsonArray` — the server assigns the row id, audit columns and code-value labels — and the cache
 * holds the whole array as one opaque row, so there is no row-level local write to make, even for a
 * delete. Re-reading the array is the only change that is exactly what the server now holds.
 *
 * `OnlineRequired`, as before, so `localApply` runs only with the server's result.
 */
@RepositoryBinding(binds = DataTableCommandRepository::class)
internal class DataTableCommandRepositoryImpl(
    private val dataTableApi: DataTableApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.DatatableGetDataOfDataTable)
    private val rowsStore: Store<DataTableDataKey, JsonArray>,
    @FromStore(AppStoreIds.DatatableGetUserPathTracking)
    private val pathStore: Store<Int, List<UserLocation>>,
) : DataTableCommandRepository {

    override suspend fun deleteDatatableEntry(datatable: String, apptableId: Long, datatableId: Long) =
        command(payload = datatableId, ingest = { rowsStore.fresh(DataTableDataKey(datatable, apptableId.toInt())) }) {
            dataTableApi.deleteDatatableEntry(datatable = datatable, apptableId = apptableId, datatableId = it)
        }

    override suspend fun createEntryInDataTable(
        dataTableName: String,
        entityId: Int,
        requestPayload: Map<String, String>,
    ) =
        command(payload = requestPayload, ingest = { rowsStore.fresh(DataTableDataKey(dataTableName, entityId)) }) {
            dataTableApi.createEntryInDataTable(dataTableName = dataTableName, entityId = entityId, requestPayload = it)
        }

    override suspend fun deleteEntryOfDataTableManyToMany(dataTableName: String, entityId: Int, dataTableRowId: Int) =
        command(payload = dataTableRowId, ingest = { rowsStore.fresh(DataTableDataKey(dataTableName, entityId)) }) {
            dataTableApi.deleteEntryOfDataTableManyToMany(
                dataTableName = dataTableName,
                entityId = entityId,
                dataTableRowId = it,
            )
        }

    // `m_staff_path_tracking` is a datatable too: its rows are read back through `pathStore`.
    override suspend fun addUserPathTracking(userId: Int, userLocation: UserLocation?) =
        command(payload = userId, ingest = { pathStore.fresh(userId) }) {
            dataTableApi.addUserPathTracking(userId = it, userLocation = userLocation)
        }

    /**
     * One datatable command. An ingest failure is swallowed, not thrown: the server HAS the write,
     * and a throw would report it `Failed` — inviting the officer to send it again (a second row,
     * or a delete answered 404). The rows arrive with the screen's next read instead.
     */
    private suspend fun <P : Any, R : Any> command(
        payload: P,
        ingest: suspend () -> Any,
        endpoint: suspend (P) -> R,
    ): MutationResult<R> = gateway.command(
        CommandSpec(
            payload = payload,
            endpoint = endpoint,
            localApply = { response ->
                if (response != null) {
                    try {
                        ingest()
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        // see KDoc
                    }
                }
            },
        ),
        policy = MutationPolicy.OnlineRequired,
    )
}
