/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.group.mapper

import kpt.core.model.objects.clients.Page
import kpt.core.database.group.entity.GroupEntity
import kpt.core.network.data.AbstractMapper
import kpt.core.network.mifos.group.dto.GetGroupsResponse

object GetGroupsResponseMapper : AbstractMapper<GetGroupsResponse, Page<GroupEntity>>() {

    override fun mapFromEntity(entity: GetGroupsResponse): Page<GroupEntity> {
        return Page<GroupEntity>().apply {
            totalFilteredRecords = entity.totalFilteredRecords!!
            pageItems = GroupMapper.mapFromEntityList(entity.pageItems!!.toList())
        }
    }

    override fun mapToEntity(domainModel: Page<GroupEntity>): GetGroupsResponse {
        return GetGroupsResponse(
            totalFilteredRecords = domainModel.totalFilteredRecords,
            pageItems = GroupMapper.mapToEntityList(domainModel.pageItems).toSet(),
        )
    }
}
