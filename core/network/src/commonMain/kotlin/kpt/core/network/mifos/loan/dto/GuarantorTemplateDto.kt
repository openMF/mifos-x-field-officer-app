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
data class GuarantorTemplateDto(
    val allowedClientRelationshipTypes: List<GuarantorRelationshipOptionDto> = emptyList(),
    val guarantorTypeOptions: List<GuarantorTypeDto>,

)

@Serializable
data class GuarantorRelationshipOptionDto(
    val id: Long,
    val name: String,
)

@Serializable
data class GuarantorAccountTemplateDto(
    val guarantorType: GuarantorTypeDto,
    val status: Boolean,
    val externalGuarantor: Boolean,
    val existingGroup: Boolean,
    val existingClient: Boolean,
    val staffMember: Boolean,
)

@Serializable
data class GuarantorTypeDto(
    val id: Long,
    val code: String,
    val value: String,
)
