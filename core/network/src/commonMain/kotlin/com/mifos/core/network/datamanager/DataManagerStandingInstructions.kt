/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.network.datamanager

import com.mifos.core.model.objects.clients.Page
import com.mifos.core.network.BaseApiManager
import com.mifos.core.network.dto.standingInstruction.StandingInstructionDto
import com.mifos.core.network.dto.standingInstruction.StandingInstructionUpdateResponseDto
import com.mifos.core.network.dto.standingInstruction.UpdateStandingInstructionDto
import kotlinx.coroutines.flow.Flow

class DataManagerStandingInstructions(
    private val mBaseApiManager: BaseApiManager,
) {
    fun retrieveListStandingInstructions(
        clientId: Long,
        clientName: String,
        fromAccountType: Int,
        fromAccountId: Long,
        limit: Int,
        offset: Int,
        locale: String = "en",
        dateFormat: String = "dd MMMM yyyy",
    ): Flow<Page<StandingInstructionDto>> {
        return mBaseApiManager.standingInstructionService.getStandingInstructions(
            clientId = clientId,
            clientName = clientName,
            fromAccountType = fromAccountType,
            fromAccountId = fromAccountId,
            locale = locale,
            dateFormat = dateFormat,
            limit = limit,
            offset = offset,
        )
    }

    suspend fun updateStandingInstruction(
        standingInstructionId: Long,
        updateStandingInstructionDto: UpdateStandingInstructionDto,
    ): StandingInstructionUpdateResponseDto {
        return mBaseApiManager.standingInstructionService.updateStandingInstruction(
            standingInstructionId = standingInstructionId,
            updateStandingInstructionDto = updateStandingInstructionDto,
        )
    }

    suspend fun deleteStandingInstruction(
        standingInstructionId: Long,
    ): StandingInstructionUpdateResponseDto {
        return mBaseApiManager.standingInstructionService.deleteStandingInstruction(
            standingInstructionId = standingInstructionId,
        )
    }
}
