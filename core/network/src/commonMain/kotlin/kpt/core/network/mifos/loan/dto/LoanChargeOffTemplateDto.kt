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
data class LoanChargeOffTemplateDto(
    val chargeOffReasonOptions: List<ChargeOffReasonOptionDto> = emptyList(),
)

@Serializable
data class ChargeOffReasonOptionDto(
    val id: Int,
    val name: String,
    val position: Int,
    val description: String,
    val active: Boolean,
    val mandatory: Boolean,
)
