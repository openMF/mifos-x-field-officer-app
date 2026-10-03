/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.datatable.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.serialization.json.JsonArray
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.database.datatable.entity.DataTableEntity
import kpt.core.model.objects.users.UserLocation
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.datatable.dto.DeleteDataTablesDatatableAppTableIdDatatableIdResponse
import kpt.core.network.mifos.datatable.dto.GetDataTablesResponse

@ApiBinding("mifos")
interface DataTableApi {

    /**
     * List Data Tables
     * Lists registered data tables and the Apache Fineract Core application table they are registered to.  ARGUMENTS  apptable  - optional The Apache Fineract core application table.  Example Requests:  datatables?apptable&#x3D;m_client   datatables
     * Responses:
     *  - 200: OK
     *
     * @param apptable apptable (optional)
     * @return [kotlin.collections.List<GetDataTablesResponse>]
     */
    @GET("datatables")
    suspend fun getDatatables(@Query("apptable") apptable: String? = null): List<GetDataTablesResponse>

    /**
     * Delete Entry in Datatable (One to Many)
     * Deletes the entry (if it exists) for data tables that are one to many with the application table.
     * Responses:
     *  - 200: OK
     *
     * @param datatable datatable
     * @param apptableId apptableId
     * @param datatableId datatableId
     * @return [DeleteDataTablesDatatableAppTableIdDatatableIdResponse]
     */
    @DELETE("datatables/{datatable}/{apptableId}/{datatableId}")
    suspend fun deleteDatatableEntry(
        @Path("datatable") datatable: String,
        @Path("apptableId") apptableId: Long,
        @Path("datatableId") datatableId: Long,
    ): DeleteDataTablesDatatableAppTableIdDatatableIdResponse

    @GET("datatables")
    suspend fun getTableOf(@Query("apptable") table: String?): List<DataTableEntity>

    @GET("datatables/{dataTableName}/{entityId}/")
    suspend fun getDataOfDataTable(
        @Path("dataTableName") dataTableName: String,
        @Path("entityId") entityId: Int,
    ): JsonArray

    // TODO Improve Body Implementation with Payload
    @POST("datatables/{dataTableName}/{entityId}/")
    suspend fun createEntryInDataTable(
        @Path("dataTableName") dataTableName: String,
        @Path("entityId") entityId: Int,
        @Body requestPayload: Map<String, String>,
    ): GenericResponse

    @DELETE("datatables/{dataTableName}/{entityId}/{dataTableRowId}")
    suspend fun deleteEntryOfDataTableManyToMany(
        @Path("dataTableName") dataTableName: String,
        @Path("entityId") entityId: Int,
        @Path("dataTableRowId") dataTableRowId: Int,
    ): GenericResponse

    @POST("datatables/m_staff_path_tracking/{userId}")
    suspend fun addUserPathTracking(
        @Path("userId") userId: Int,
        @Body userLocation: UserLocation?,
    ): GenericResponse

    @GET("datatables/m_staff_path_tracking/{userId}")
    suspend fun getUserPathTracking(@Path("userId") userId: Int): List<UserLocation>
}
