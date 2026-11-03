/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.center.mapper

import kpt.core.model.center.Center
import kpt.core.model.objects.clients.Page
import kpt.core.network.mifos.center.dto.GetCentersResponse
import kpt.core.network.mifos.mapper.AbstractMapper

/** Maps get centers response between its network, stored and domain shapes. */
object GetCentersResponseMapper : AbstractMapper<GetCentersResponse, Page<Center>>() {

    override fun mapFromEntity(entity: GetCentersResponse): Page<Center> {
        return Page<Center>().apply {
            totalFilteredRecords = entity.totalFilteredRecords!!
            pageItems = CenterMapper.mapFromEntityList(entity.pageItems!!.toList())
        }
    }

    override fun mapToEntity(domainModel: Page<Center>): GetCentersResponse {
        return GetCentersResponse(
            totalFilteredRecords = domainModel.totalFilteredRecords,
            pageItems = CenterMapper.mapToEntityList(domainModel.pageItems).toSet(),
        )
    }
}
