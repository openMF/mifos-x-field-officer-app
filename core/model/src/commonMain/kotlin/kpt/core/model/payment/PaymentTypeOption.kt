/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.payment

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `PaymentTypeOptionEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class PaymentTypeOption(
    /** The server id of this record. */
    val id: Int = 0,
    /** The display name. */
    val name: String = "",
    /** A free-text description. */
    val description: String? = null,
    /** True when the payment type is cash. */
    val isCashPayment: Boolean? = null,
    /** The sort position of this option. */
    val position: Int? = null,
)
