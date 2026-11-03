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
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientStatusEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class ClientStatus(
    /** The server id of this record. */
    val id: Int = 0,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,
    /** The human-readable label Fineract sends for this value. */
    val value: String? = null,
) {

    /** Constants and factories of `ClientStatus`. */
    companion object {
        /** Fineract's code for an active client. */
        const val STATUS_ACTIVE = "Active"

        /** True when `value` is the active status. */
        fun isActive(value: String): Boolean {
            return value == STATUS_ACTIVE
        }
    }
}
