/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.group.mapper

import kpt.core.model.group.Group
import kpt.core.model.objects.clients.Page
import kpt.core.network.mifos.group.dto.GetGroupsResponse
import kpt.core.network.mifos.mapper.AbstractMapper

object GetGroupsResponseMapper : AbstractMapper<GetGroupsResponse, Page<Group>>() {

    override fun mapFromEntity(entity: GetGroupsResponse): Page<Group> {
        return Page<Group>().apply {
            totalFilteredRecords = entity.totalFilteredRecords!!
            pageItems = GroupMapper.mapFromEntityList(entity.pageItems!!.toList())
        }
    }

    override fun mapToEntity(domainModel: Page<Group>): GetGroupsResponse {
        return GetGroupsResponse(
            totalFilteredRecords = domainModel.totalFilteredRecords,
            pageItems = GroupMapper.mapToEntityList(domainModel.pageItems).toSet(),
        )
    }
}
