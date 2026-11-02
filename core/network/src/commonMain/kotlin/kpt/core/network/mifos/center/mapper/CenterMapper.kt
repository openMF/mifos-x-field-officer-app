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
import kpt.core.model.client.ClientStatus
import kpt.core.network.mifos.center.dto.GetCentersPageItems
import kpt.core.network.mifos.center.dto.GetCentersStatus
import kpt.core.network.mifos.mapper.AbstractMapper

object CenterMapper : AbstractMapper<GetCentersPageItems, Center>() {

    override fun mapFromEntity(entity: GetCentersPageItems): Center {
        return Center(
            id = entity.id?.toInt(),
            active = entity.active,
            name = entity.name,
            officeName = entity.officeName,
            officeId = entity.officeId?.toInt(),
            hierarchy = entity.hierarchy,
            status = ClientStatus(
                id = entity.status?.id!!.toInt(),
                code = entity.status?.code,
                value = entity.status?.description,
            ),
        )
    }

    override fun mapToEntity(domainModel: Center): GetCentersPageItems {
        return GetCentersPageItems(
            id = domainModel.id?.toLong(),
            active = domainModel.active,
            name = domainModel.name,
            officeName = domainModel.officeName,
            officeId = domainModel.officeId?.toLong(),
            hierarchy = domainModel.hierarchy,
            status = GetCentersStatus(
                id = domainModel.status?.id?.toLong(),
                code = domainModel.status?.code,
                description = domainModel.status?.value,
            ),
        )
    }
}
