/*
 * Copyright 2025 Mifos Initiative
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
data class ProductOption(
    val id: Int,

    val name: String,

    val shortName: String,

    val totalShares: Int,

    val currency: ProductCurrency? = null,

    val unitPrice: Double? = null,
)

@Serializable
data class ProductCurrency(
    val code: String,

    val name: String,

    val decimalPlaces: Int? = null,

    val displaySymbol: String? = null,

    val nameCode: String? = null,

    val displayLabel: String? = null,
)
