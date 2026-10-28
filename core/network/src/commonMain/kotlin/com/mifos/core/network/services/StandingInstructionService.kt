/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.network.services

import com.mifos.core.model.objects.clients.Page
import com.mifos.core.network.dto.standingInstruction.StandingInstructionDto
import com.mifos.core.network.dto.standingInstruction.StandingInstructionUpdateResponseDto
import com.mifos.core.network.dto.standingInstruction.UpdateStandingInstructionDto
import com.mifos.room.basemodel.APIEndPoint
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow

interface StandingInstructionService {
    /**
     * Retrieve a list of standing instructions based on client and account details.
     *
     * @param clientId The ID of the client.
     * @param clientName The name of the client.
     * @param fromAccountId The ID of the source account.
     * @param fromAccountType The type of the source account (e.g., 1 for Savings, 2 for Loans).
     * @param locale The locale for formatting (default: "en").
     * @param dateFormat The date format for parsing dates (default: "dd MMMM yyyy").
     * @param limit The maximum number of results to return.
     * @param offset The number of results to skip.
     * @return A [Flow] of [Page] containing [StandingInstructionDto].
     */
    @GET(APIEndPoint.STANDING_INSTRUCTIONS)
    fun getStandingInstructions(
        @Query("clientId") clientId: Long,
        @Query("clientName") clientName: String,
        @Query("fromAccountId") fromAccountId: Long,
        @Query("fromAccountType") fromAccountType: Int,
        @Query("locale") locale: String = "en",
        @Query("dateFormat") dateFormat: String = "dd MMMM yyyy",
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Flow<Page<StandingInstructionDto>>

    /**
     * Update an existing standing instruction.
     *
     * @param standingInstructionId The unique ID of the standing instruction to update.
     * @param command The command to execute (default: "update").
     * @param updateStandingInstructionDto The data to be updated.
     * @return A [StandingInstructionUpdateResponseDto] containing the result of the update.
     */
    @PUT("${APIEndPoint.STANDING_INSTRUCTIONS}/{standingInstructionId}")
    suspend fun updateStandingInstruction(
        @Path("standingInstructionId") standingInstructionId: Long,
        @Query("command") command: String = "update",
        @Body updateStandingInstructionDto: UpdateStandingInstructionDto,
    ): StandingInstructionUpdateResponseDto

    /**
     * Delete (disable) an existing standing instruction.
     *
     * @param standingInstructionId The unique ID of the standing instruction to delete.
     * @param command The command to execute (default: "delete").
     * @return A [StandingInstructionUpdateResponseDto] containing the result of the deletion.
     */
    @PUT("${APIEndPoint.STANDING_INSTRUCTIONS}/{standingInstructionId}")
    suspend fun deleteStandingInstruction(
        @Path("standingInstructionId") standingInstructionId: Long,
        @Query("command") command: String = "delete",
    ): StandingInstructionUpdateResponseDto
}
