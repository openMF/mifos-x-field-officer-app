/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.recurringdeposit.deposit

import kotlinx.serialization.Serializable

/** A deposit period frequency. */
@Serializable
data class DepositPeriodFrequency(
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String,
    /** The server id of this record. */
    val id: Int,
    /** The human-readable label Fineract sends for this value. */
    val value: String,
)
