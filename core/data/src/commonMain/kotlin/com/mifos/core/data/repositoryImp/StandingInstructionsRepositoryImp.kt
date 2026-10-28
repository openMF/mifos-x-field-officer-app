/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.data.repositoryImp

import com.mifos.core.common.utils.DataState
import com.mifos.core.common.utils.Page
import com.mifos.core.common.utils.asDataStateFlow
import com.mifos.core.data.mappers.standingInstructions.toDomain
import com.mifos.core.data.mappers.standingInstructions.toDto
import com.mifos.core.data.repository.StandingInstructionsRepository
import com.mifos.core.data.util.NetworkMonitor
import com.mifos.core.data.util.runAsDataState
import com.mifos.core.data.util.withNetworkCheck
import com.mifos.core.model.objects.standingInstructions.StandingInstruction
import com.mifos.core.model.objects.standingInstructions.StandingInstructionUpdate
import com.mifos.core.network.datamanager.DataManagerStandingInstructions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import template.core.base.common.manager.DispatcherManager

class StandingInstructionsRepositoryImp(
    private val dataManagerStandingInstructions: DataManagerStandingInstructions,
    private val networkMonitor: NetworkMonitor,
    private val dispatcher: DispatcherManager,
) : StandingInstructionsRepository {

    override fun getStandingInstructionList(
        clientId: Long,
        clientName: String,
        fromAccountType: Int,
        fromAccountId: Long,
        limit: Int,
        offset: Int,
    ): Flow<DataState<Page<StandingInstruction>>> {
        return networkMonitor.withNetworkCheck(
            dataManagerStandingInstructions
                .retrieveListStandingInstructions(
                    clientId = clientId,
                    clientName = clientName,
                    fromAccountType = fromAccountType,
                    fromAccountId = fromAccountId,
                    limit = limit,
                    offset = offset,
                )
                .map { pageDto ->
                    Page(
                        totalFilteredRecords = pageDto.totalFilteredRecords,
                        pageItems = pageDto.pageItems.map { it.toDomain() },
                    )
                }
                .asDataStateFlow(),
        ).flowOn(dispatcher.io)
    }

    override suspend fun updateStandingInstruction(
        standingInstructionId: Long,
        update: StandingInstructionUpdate,
    ): DataState<Unit> {
        return runAsDataState(
            networkMonitor,
            dispatcher.io,
        ) {
            dataManagerStandingInstructions.updateStandingInstruction(
                standingInstructionId,
                update.toDto(),
            )
        }
    }

    override suspend fun deleteStandingInstruction(
        standingInstructionId: Long,
    ): DataState<Unit> {
        return runAsDataState(
            networkMonitor,
            dispatcher.io,
        ) {
            dataManagerStandingInstructions.deleteStandingInstruction(
                standingInstructionId,
            )
        }
    }
}
