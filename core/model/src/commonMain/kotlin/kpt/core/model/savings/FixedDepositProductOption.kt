/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One fixed deposit product choice offered by a template. */
@Serializable
data class FixedDepositProductOption(
    /** The server id of this record. */
    @SerialName("id")
    val id: Int? = null,

    /** The display name. */
    @SerialName("name")
    val name: String? = null,

    /** True when tax is withheld on interest. */
    @SerialName("withHoldTax")
    val withHoldTax: Boolean? = null,
)
