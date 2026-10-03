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
data class CollateralItem(
    val quality: String,
    val basePrice: Double,
    val unitType: String,
    val pctToBase: Double,
    val currency: String,
    val name: String,
    val id: Int,
)

@Serializable
data class CollateralItemResult(
    val name: String,
    val collateralId: Int,
    val basePrice: Double,
    val pctToBase: Double,
    val quantity: Double,
)
