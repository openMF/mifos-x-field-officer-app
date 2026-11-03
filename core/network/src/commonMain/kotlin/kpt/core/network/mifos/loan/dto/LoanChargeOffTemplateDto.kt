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

/** Server template for a loan charge off: the options its form offers. */
@Serializable
data class LoanChargeOffTemplateDto(
    /** The charge-off reasons that may be chosen. */
    val chargeOffReasonOptions: List<ChargeOffReasonOptionDto> = emptyList(),
)

/** One charge off reason choice offered by a template. */
@Serializable
data class ChargeOffReasonOptionDto(
    /** The server id of this record. */
    val id: Int,
    /** The display name. */
    val name: String,
    /** The sort position of this option. */
    val position: Int,
    /** A free-text description. */
    val description: String,
    /** True when the record is active. */
    val active: Boolean,
    /** True when this option must be filled in. */
    val mandatory: Boolean,
)
