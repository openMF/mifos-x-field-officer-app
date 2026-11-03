/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.shared

import kotlinx.serialization.Serializable

/** A collateral item a client may pledge. */
@Serializable
data class CollateralItem(
    /** The collateral's quality. */
    val quality: String,
    /** The collateral's base price per unit. */
    val basePrice: Double,
    /** The unit the collateral is counted in. */
    val unitType: String,
    /** The percentage of the base price the collateral counts for. */
    val pctToBase: Double,
    /** The currency the amounts are in. */
    val currency: String,
    /** The display name. */
    val name: String,
    /** The server id of this record. */
    val id: Int,
)

/** Collateral pledged by a client. */
@Serializable
data class CollateralItemResult(
    /** The display name. */
    val name: String,
    /** The server id of the collateral. */
    val collateralId: Int,
    /** The collateral's base price per unit. */
    val basePrice: Double,
    /** The percentage of the base price the collateral counts for. */
    val pctToBase: Double,
    /** The quantity of collateral. */
    val quantity: Double,
)
