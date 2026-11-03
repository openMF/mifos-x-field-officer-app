/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingProductOptionsEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class SavingProductOptions(
    /** The server id of this record. */
    var id: Int = 0,
    /** The display name. */
    val name: String = "",
    /** True when the withdrawal fee also applies to transfers. */
    val withdrawalFeeForTransfers: Boolean = false,
    /** True when the account may be overdrawn. */
    val allowOverdraft: Boolean = false,
)
