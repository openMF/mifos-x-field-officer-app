/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.staff.mapper

import kpt.core.model.shared.RetrieveOneResponse
import kpt.core.model.staff.Staff
import kpt.core.network.mifos.mapper.AbstractMapper

/** Maps staff between its network, stored and domain shapes. */
object StaffMapper : AbstractMapper<RetrieveOneResponse, Staff>() {
    override fun mapFromEntity(entity: RetrieveOneResponse): Staff {
        return Staff(
            id = entity.id!!.toInt(),
            firstname = entity.firstname,
            lastname = entity.lastname,
            displayName = entity.displayName,
            officeId = entity.officeId!!.toInt(),
            officeName = entity.officeName,
            isLoanOfficer = entity.isLoanOfficer,
            isActive = entity.isActive,
        )
    }

    override fun mapToEntity(domainModel: Staff): RetrieveOneResponse {
        return RetrieveOneResponse(
            id = domainModel.id?.toLong(),
            firstname = domainModel.firstname,
            lastname = domainModel.lastname,
            displayName = domainModel.displayName,
            officeId = domainModel.officeId?.toLong(),
            officeName = domainModel.officeName,
            isLoanOfficer = domainModel.isLoanOfficer,
            isActive = domainModel.isActive,
        )
    }
}
