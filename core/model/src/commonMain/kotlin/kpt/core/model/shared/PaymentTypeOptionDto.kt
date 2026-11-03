/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.shared

import kotlinx.serialization.Serializable

/** One payment type choice offered by a template. */
@Serializable
data class PaymentTypeOptionDto(
    /** The server id of this record. */
    val id: Int,
    /** The display name. */
    val name: String,
    /** A free-text description. */
    val description: String? = null,
    /** True when the payment type is cash. */
    val isCashPayment: Boolean = false,
    /** The sort position of this option. */
    val position: Int = 0,
    /** The name of the code the payment type belongs to. */
    val codeName: String? = null,
    /** True when the value is built into Fineract. */
    val isSystemDefined: Boolean = false,
)
