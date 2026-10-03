/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.center.mapper

import kpt.core.model.objects.clients.Page
import kpt.core.database.center.entity.CenterEntity
import kpt.core.network.data.AbstractMapper
import kpt.core.network.mifos.center.dto.GetCentersResponse

object GetCentersResponseMapper : AbstractMapper<GetCentersResponse, Page<CenterEntity>>() {

    override fun mapFromEntity(entity: GetCentersResponse): Page<CenterEntity> {
        return Page<CenterEntity>().apply {
            totalFilteredRecords = entity.totalFilteredRecords!!
            pageItems = CenterMapper.mapFromEntityList(entity.pageItems!!.toList())
        }
    }

    override fun mapToEntity(domainModel: Page<CenterEntity>): GetCentersResponse {
        return GetCentersResponse(
            totalFilteredRecords = domainModel.totalFilteredRecords,
            pageItems = CenterMapper.mapToEntityList(domainModel.pageItems).toSet(),
        )
    }
}
