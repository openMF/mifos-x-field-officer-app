/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.shared

import kotlinx.serialization.Serializable

@Serializable
data class PaymentTypeOptionDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val isCashPayment: Boolean = false,
    val position: Int = 0,
    val codeName: String? = null,
    val isSystemDefined: Boolean = false,
)
