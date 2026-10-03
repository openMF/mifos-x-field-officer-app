/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.client.mapper

import kpt.core.model.objects.clients.Page
import kpt.core.database.client.entity.ClientEntity
import kpt.core.network.data.AbstractMapper
import kpt.core.network.mifos.client.dto.GetClientsResponse

object GetClientResponseMapper : AbstractMapper<GetClientsResponse, Page<ClientEntity>>() {

    override fun mapFromEntity(entity: GetClientsResponse): Page<ClientEntity> {
        return Page<ClientEntity>().apply {
            totalFilteredRecords = entity.totalFilteredRecords!!
            pageItems = ClientMapper.mapFromEntityList(entity.pageItems!!.toList())
        }
    }

    override fun mapToEntity(domainModel: Page<ClientEntity>): GetClientsResponse {
        return GetClientsResponse(
            totalFilteredRecords = domainModel.totalFilteredRecords,
            pageItems = ClientMapper.mapToEntityList(domainModel.pageItems).toSet(),
        )
    }
}
