/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.datatable

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.objects.users.UserLocation
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.datatable.dto.DeleteDataTablesDatatableAppTableIdDatatableIdResponse

/**
 * Every datatable write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * These are commands, so they carry no Store and no cache. Offline CAPTURE is a different
 * operation with its own explicit queue ([kpt.core.data.sync.OfflineQueueRepository]); it is never
 * a silent fallback from here.
 */
interface DataTableCommandRepository {

    suspend fun deleteDatatableEntry(datatable: String, apptableId: Long, datatableId: Long): MutationResult<DeleteDataTablesDatatableAppTableIdDatatableIdResponse>

    suspend fun createEntryInDataTable(dataTableName: String, entityId: Int, requestPayload: Map<String, String>): MutationResult<GenericResponse>

    suspend fun deleteEntryOfDataTableManyToMany(dataTableName: String, entityId: Int, dataTableRowId: Int): MutationResult<GenericResponse>

    suspend fun addUserPathTracking(userId: Int, userLocation: UserLocation?): MutationResult<GenericResponse>
}
