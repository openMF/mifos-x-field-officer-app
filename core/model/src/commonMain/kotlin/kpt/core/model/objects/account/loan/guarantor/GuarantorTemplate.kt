/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.guarantor

data class GuarantorTemplate(
    val allowedClientRelationshipTypes: List<GuarantorRelationshipOption>,
    val guarantorTypeOptions: List<GuarantorType>,
)

data class GuarantorRelationshipOption(
    val id: Long,
    val name: String,
)

data class GuarantorAccountTemplate(
    val guarantorType: GuarantorType,
    val status: Boolean,
    val externalGuarantor: Boolean,
    val existingGroup: Boolean,
    val existingClient: Boolean,
    val staffMember: Boolean,
)

data class GuarantorType(
    val id: Long,
    val code: String,
    val value: String,
)
