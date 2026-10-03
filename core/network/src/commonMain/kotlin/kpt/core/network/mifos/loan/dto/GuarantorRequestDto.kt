/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable

@Serializable
data class GuarantorRequestDto(
    val clientRelationshipTypeId: Long,
    val dateFormat: String,
    val entityId: Int?,
    val guarantorTypeId: Long,
    val locale: String,

    val firstname: String?,
    val lastname: String?,
    val dob: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val zip: String?,
    val mobileNumber: String?,
    val housePhoneNumber: String?,
)
