/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.client.mapper

import kpt.core.model.client.Client
import kpt.core.model.client.ClientClassification
import kpt.core.model.client.ClientGender
import kpt.core.model.client.ClientStatus
import kpt.core.model.client.ClientType
import kpt.core.network.mifos.client.dto.GetClientClassificationOptions
import kpt.core.network.mifos.client.dto.GetClientStatus
import kpt.core.network.mifos.client.dto.GetClientTypeOptions
import kpt.core.network.mifos.client.dto.GetClientsPageItemsResponse
import kpt.core.network.mifos.client.dto.GetGenderOptions
import kpt.core.network.mifos.mapper.AbstractMapper

/** Maps client between its network, stored and domain shapes. */
object ClientMapper : AbstractMapper<GetClientsPageItemsResponse, Client>() {

    override fun mapFromEntity(entity: GetClientsPageItemsResponse): Client {
        return Client(
            id = entity.id!!.toInt(),
            accountNo = entity.accountNo,
            fullname = entity.fullName,
            firstname = entity.displayName!!.split(" ")[0],
            lastname =
            if (entity.displayName.split(" ").size >= 2) entity.displayName.split(" ")[1] else "",
            displayName = entity.displayName,
            officeId = entity.officeId!!.toInt(),
            officeName = entity.officeName,
            groupId = entity.groupId,
            groupName = entity.groupName,
            active = entity.active!!,
            status = ClientStatus(
                id = entity.status?.id!!.toInt(),
                code = entity.status.code,
                value = entity.status.value,
            ),
            externalId = entity.externalId,
            emailAddress = entity.emailAddress,
            legalForm = ClientStatus(
                id = entity.legalForm?.id?.toInt() ?: -1,
                code = entity.legalForm?.code,
                value = entity.legalForm?.value,
            ),
            dateOfBirth = entity.dateOfBirth ?: emptyList(),
            gender = entity.gender?.let {
                ClientGender(
                    id = it.id?.toInt() ?: -1,
                    name = it.name,
                )
            },
            clientType = entity.clientType?.let {
                ClientType(
                    id = it.id?.toInt() ?: -1,
                    name = it.name,
                )
            },
            clientClassification = entity.clientClassification?.let {
                ClientClassification(
                    id = it.id?.toInt() ?: -1,
                    name = it.name,
                )
            },
        )
    }

    override fun mapToEntity(domainModel: Client): GetClientsPageItemsResponse {
        return GetClientsPageItemsResponse(
            id = domainModel.id.toLong(),
            accountNo = domainModel.accountNo,
            fullName = domainModel.fullname,
            displayName = domainModel.displayName,
            officeId = domainModel.officeId.toLong(),
            officeName = domainModel.officeName,
            groupId = domainModel.groupId,
            groupName = domainModel.groupName,
            active = domainModel.active,
            status = GetClientStatus(
                id = domainModel.status?.id?.toLong(),
                code = domainModel.status?.code,
                value = domainModel.status?.value,
            ),
            externalId = domainModel.externalId,
            emailAddress = domainModel.emailAddress,
            legalForm = GetClientStatus(
                id = domainModel.legalForm?.id?.toLong(),
                code = domainModel.legalForm?.code,
                value = domainModel.legalForm?.value,
            ),
            dateOfBirth = domainModel.dateOfBirth,
            gender = domainModel.gender?.let {
                GetGenderOptions(
                    id = it.id?.toLong(),
                    name = it.name,
                )
            },
            clientType = domainModel.clientType?.let {
                GetClientTypeOptions(
                    id = it.id?.toLong(),
                    name = it.name,
                )
            },
            clientClassification = domainModel.clientClassification?.let {
                GetClientClassificationOptions(
                    id = it.id?.toLong(),
                    name = it.name,
                )
            },
        )
    }
}
