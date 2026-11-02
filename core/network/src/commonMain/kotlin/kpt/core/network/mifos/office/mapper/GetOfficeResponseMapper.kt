/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.office.mapper

import kpt.core.model.office.Office
import kpt.core.network.mifos.mapper.AbstractMapper
import kpt.core.network.mifos.office.dto.GetOfficesResponse

object GetOfficeResponseMapper : AbstractMapper<GetOfficesResponse, Office>() {

    override fun mapFromEntity(entity: GetOfficesResponse): Office {
        return Office(
            id = entity.id?.toInt()!!,
            externalId = entity.externalId,
            name = entity.name,
            nameDecorated = entity.nameDecorated,
            officeOpeningDate = null,
            openingDate = emptyList(),
        )
    }

    override fun mapToEntity(domainModel: Office): GetOfficesResponse {
        return GetOfficesResponse(
            id = domainModel.id.toLong(),
            name = domainModel.name,
            nameDecorated = domainModel.nameDecorated,
            externalId = domainModel.externalId,
        )
    }
}
