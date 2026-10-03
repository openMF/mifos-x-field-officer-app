/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.datatable.impl

import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.datatable.DataTableCommandRepository
import kpt.core.model.objects.users.UserLocation
import kpt.core.network.mifos.datatable.api.DataTableApi

@RepositoryBinding(binds = DataTableCommandRepository::class)
internal class DataTableCommandRepositoryImpl(
    private val dataTableApi: DataTableApi,
    private val gateway: MutationGateway,
) : DataTableCommandRepository {

    override suspend fun deleteDatatableEntry(datatable: String, apptableId: Long, datatableId: Long) =
        online { dataTableApi.deleteDatatableEntry(datatable = datatable, apptableId = apptableId, datatableId = datatableId) }

    override suspend fun createEntryInDataTable(dataTableName: String, entityId: Int, requestPayload: Map<String, String>) =
        online { dataTableApi.createEntryInDataTable(dataTableName = dataTableName, entityId = entityId, requestPayload = requestPayload) }

    override suspend fun deleteEntryOfDataTableManyToMany(dataTableName: String, entityId: Int, dataTableRowId: Int) =
        online { dataTableApi.deleteEntryOfDataTableManyToMany(dataTableName = dataTableName, entityId = entityId, dataTableRowId = dataTableRowId) }

    override suspend fun addUserPathTracking(userId: Int, userLocation: UserLocation?) =
        online { dataTableApi.addUserPathTracking(userId = userId, userLocation = userLocation) }

    /**
     * Commands run [MutationPolicy.OnlineRequired]: they await the server and write nothing
     * offline, yielding `Blocked(OFFLINE)` rather than a local success the officer would act on.
     *
     * The call is closed over rather than threaded through [CommandSpec.payload]: the payload hook
     * exists for `localApply` / `rollback` / `conflictOf`, none of which apply to a write that never
     * lands locally. It also keeps the 28 endpoints whose body is nullable usable, which
     * `CommandSpec<P : Any>` would otherwise reject.
     */
    private suspend fun <R : Any> online(endpoint: suspend () -> R): MutationResult<R> =
        gateway.command(
            CommandSpec(payload = Unit, endpoint = { endpoint() }),
            policy = MutationPolicy.OnlineRequired,
        )
}
