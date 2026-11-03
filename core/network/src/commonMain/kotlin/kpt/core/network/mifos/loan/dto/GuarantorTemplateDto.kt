/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto
import kotlinx.serialization.Serializable

/** Server template for a guarantor: the options its form offers. */
@Serializable
data class GuarantorTemplateDto(
    /** The relationships a guarantor may have to the client. */
    val allowedClientRelationshipTypes: List<GuarantorRelationshipOptionDto> = emptyList(),
    /** The guarantor types that may be chosen. */
    val guarantorTypeOptions: List<GuarantorTypeDto>,

)

/** One guarantor relationship choice offered by a template. */
@Serializable
data class GuarantorRelationshipOptionDto(
    /** The server id of this record. */
    val id: Long,
    /** The display name. */
    val name: String,
)

/** Server template for a guarantor account: the options its form offers. */
@Serializable
data class GuarantorAccountTemplateDto(
    /** The kind of guarantor. */
    val guarantorType: GuarantorTypeDto,
    /** The record's Fineract status. */
    val status: Boolean,
    /** True when the guarantor is not a client. */
    val externalGuarantor: Boolean,
    /** True when the guarantor is an existing group. */
    val existingGroup: Boolean,
    /** True when the guarantor is an existing client. */
    val existingClient: Boolean,
    /** True when the guarantor is a staff member. */
    val staffMember: Boolean,
)

/** Wire shape of a guarantor type. */
@Serializable
data class GuarantorTypeDto(
    /** The server id of this record. */
    val id: Long,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String,
    /** The human-readable label Fineract sends for this value. */
    val value: String,
)
