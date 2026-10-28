/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.data.repository

import com.mifos.core.common.utils.DataState
import com.mifos.core.common.utils.Page
import com.mifos.core.model.objects.standingInstructions.StandingInstruction
import com.mifos.core.model.objects.standingInstructions.StandingInstructionUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Created by Shlok Sharma(techsavvy185) on 19/06/2026.
 */
interface StandingInstructionsRepository {
    fun getStandingInstructionList(
        clientId: Long,
        clientName: String,
        fromAccountType: Int,
        fromAccountId: Long,
        limit: Int,
        offset: Int,
    ): Flow<DataState<Page<StandingInstruction>>>

    suspend fun updateStandingInstruction(
        standingInstructionId: Long,
        update: StandingInstructionUpdate,
    ): DataState<Unit>

    suspend fun deleteStandingInstruction(
        standingInstructionId: Long,
    ): DataState<Unit>
}
