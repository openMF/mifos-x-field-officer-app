/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.clients

import kotlinx.serialization.Serializable

/** Request body for a collateral. */
@Serializable
data class CollateralPayload(
    /** The server id of the collateral. */
    val collateralId: Int,
    /** The quantity of collateral. */
    val quantity: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
)
