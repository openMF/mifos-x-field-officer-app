/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.collectionsheet.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.database.center.entity.CenterWithAssociations
import kpt.core.database.collectionsheet.entity.CenterDetail
import kpt.core.database.collectionsheet.entity.CollectionSheetResponse
import kpt.core.database.collectionsheet.entity.IndividualCollectionSheet
import kpt.core.model.collectionsheet.CollectionSheetPayload
import kpt.core.model.collectionsheet.ProductiveCollectionSheetPayload
import kpt.core.model.objects.collectionsheets.CollectionSheetRequestPayload
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.collectionsheet.dto.IndividualCollectionSheetPayload
import kpt.core.network.mifos.collectionsheet.dto.RequestCollectionSheetPayload

/**
 * Created by Tarun on 06-07-2017.
 */
@ApiBinding("mifos")
interface CollectionSheetApi {
    @POST("collectionsheet?command=generateCollectionSheet")
    suspend fun getIndividualCollectionSheet(
        @Body payload: RequestCollectionSheetPayload?,
    ): IndividualCollectionSheet

    @POST("collectionsheet?command=saveCollectionSheet")
    suspend fun saveIndividualCollectionSheet(
        @Body payload: IndividualCollectionSheetPayload?,
    ): GenericResponse
    // Productive CollectionSheet Endpoints

    /**
     * Endpoint to fetch Productive CollectionSheet
     * @param format "dd MMMM yyyy"
     * @param locale "en"
     * @param meetingDate Meeting Date for the center
     * @param officeId Office Id
     * @param staffId Staff Id
     * @return List of CenterDetail
     */
    @GET("centers")
    suspend fun fetchCenterDetails(
        @Query("dateFormat") format: String?,
        @Query("locale") locale: String?,
        @Query("meetingDate") meetingDate: String?,
        @Query("officeId") officeId: Int,
        @Query("staffId") staffId: Int,
    ): List<CenterDetail>

    /**
     * Request Endpoint to fetch Productive CollectionSheet
     * @param centerId Center Id
     * @param payload CollectionSheetRequestPayload
     * @return CollectionSheetResponse
     */
    @POST("centers/{centerId}?command=generateCollectionSheet")
    suspend fun fetchProductiveSheet(
        @Path("centerId") centerId: Int,
        @Body payload: CollectionSheetRequestPayload?,
    ): CollectionSheetResponse

    @POST("centers/{centerId}?command=saveCollectionSheet")
    suspend fun submitProductiveSheet(
        @Path("centerId") centerId: Int,
        @Body payload: ProductiveCollectionSheetPayload?,
    ): GenericResponse

    @POST("groups/{groupId}?command=generateCollectionSheet")
    suspend fun fetchCollectionSheet(
        @Path("groupId") groupId: Int,
        @Body payload: CollectionSheetRequestPayload?,
    ): CollectionSheetResponse

    @POST("groups/{groupId}?command=saveCollectionSheet")
    suspend fun submitCollectionSheet(
        @Path("groupId") groupId: Int,
        @Body payload: CollectionSheetPayload?,
    ): GenericResponse

    @GET("centers/{centerId}?associations=groupMembers,collectionMeetingCalendar")
    suspend fun fetchGroupsAssociatedWithCenter(
        @Path("centerId") centerId: Int,
    ): CenterWithAssociations
}
