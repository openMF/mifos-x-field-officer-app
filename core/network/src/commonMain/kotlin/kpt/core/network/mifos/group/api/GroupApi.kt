/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.group.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.QueryMap
import io.ktor.client.statement.HttpResponse
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.model.objects.clients.Page
import kpt.core.database.group.entity.GroupAccounts
import kpt.core.database.group.entity.GroupEntity
import kpt.core.database.group.entity.GroupPayloadEntity
import kpt.core.database.group.entity.GroupWithAssociations
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.network.mifos.group.dto.GetGroupsResponse

@ApiBinding("mifos")
interface GroupApi {

    /**
     * List Groups
     * The default implementation of listing Groups returns 200 entries with support for pagination and sorting. Using the parameter limit with description -1 returns all entries.  Example Requests:    groups    groups?fields&#x3D;name,officeName,joinedDate    groups?offset&#x3D;10&amp;limit&#x3D;50    groups?orderBy&#x3D;name&amp;sortOrder&#x3D;DESC
     * Responses:
     *  - 200: OK
     *
     * @param officeId officeId (optional)
     * @param staffId staffId (optional)
     * @param externalId externalId (optional)
     * @param name name (optional)
     * @param underHierarchy underHierarchy (optional)
     * @param paged paged (optional)
     * @param offset offset (optional)
     * @param limit limit (optional)
     * @param orderBy orderBy (optional)
     * @param sortOrder sortOrder (optional)
     * @param orphansOnly orphansOnly (optional)
     * @return [GetGroupsResponse]
     */
    @GET("groups")
    suspend fun retrieveAll24(
        @Query("officeId") officeId: Long? = null,
        @Query("staffId") staffId: Long? = null,
        @Query("externalId") externalId: String? = null,
        @Query("name") name: String? = null,
        @Query("underHierarchy") underHierarchy: String? = null,
        @Query("paged") paged: Boolean? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("orderBy") orderBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
        @Query("orphansOnly") orphansOnly: Boolean? = null,
    ): GetGroupsResponse

    @GET("groups")
    suspend fun getGroups(
        @Query("paged") b: Boolean,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): Page<GroupEntity>

    @GET("groups/{groupId}?associations=all")
    suspend fun getGroupWithAssociations(@Path("groupId") groupId: Int): GroupWithAssociations

    @GET("groups")
    suspend fun getAllGroupsInOffice(
        @Query("officeId") officeId: Int,
        @QueryMap params: Map<String, String>,
    ): List<GroupEntity>

    @POST("groups")
    suspend fun createGroup(@Body groupPayload: GroupPayloadEntity?): SaveResponse

    @GET("groups/{groupId}")
    suspend fun getGroup(@Path("groupId") groupId: Int): GroupEntity

    @GET("groups/{groupId}/accounts")
    suspend fun getGroupAccounts(@Path("groupId") groupId: Int): GroupAccounts

    /**
     * This is the service to activate the Group
     * REST ENT POINT
     * https://demo.openmf.org/fineract-provider/api/v1/groups/{groupId}?command=activate
     *
     * @param groupId
     * @return GenericResponse
     */
    @POST("groups/{groupId}?command=activate")
    suspend fun activateGroup(
        @Path("groupId") groupId: Int,
        @Body activatePayload: ActivatePayload,
    ): HttpResponse
}
