/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.office.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.database.office.entity.OfficeEntity
import kpt.core.network.mifos.office.dto.GetOfficesResponse

@ApiBinding("mifos")
interface OfficeApi {

    /**
     * List Offices
     * Example Requests:  offices   offices?fields&#x3D;id,name,openingDate
     * Responses:
     *  - 200: OK
     *
     * @param includeAllOffices includeAllOffices (optional, default to false)
     * @param orderBy orderBy (optional)
     * @param sortOrder sortOrder (optional)
     * @return [kotlin.collections.List<GetOfficesResponse]
     */
    @GET("offices")
    suspend fun retrieveOffices(
        @Query("includeAllOffices") includeAllOffices: Boolean? = false,
        @Query("orderBy") orderBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
    ): List<GetOfficesResponse>

    /**
     * Fetches List of All the Offices
     *
     * @param listOfOfficesCallback
     */
    @GET("offices")
    suspend fun allOffices(): List<OfficeEntity>
}
