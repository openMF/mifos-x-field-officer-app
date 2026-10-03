/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.shared

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * GetStaffResponse
 *
 * @param displayName
 * @param externalId
 * @param firstname
 * @param id
 * @param isActive
 * @param isLoanOfficer
 * @param joiningDate
 * @param lastname
 * @param officeId
 * @param officeName
 */

@Serializable
data class RetrieveOneResponse(

    val displayName: String? = null,

    val externalId: String? = null,

    val firstname: String? = null,

    val id: Long? = null,

    val isActive: Boolean? = null,

    val isLoanOfficer: Boolean? = null,

    @Contextual
    val joiningDate: LocalDate? = null,

    val lastname: String? = null,

    val officeId: Long? = null,

    val officeName: String? = null,

)
