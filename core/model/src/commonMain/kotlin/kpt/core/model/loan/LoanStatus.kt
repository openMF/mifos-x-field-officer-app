/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.loan

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanStatusEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class LoanStatus(
    /** The server id of this record. */
    val id: Int? = null,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,
    /** The human-readable label Fineract sends for this value. */
    val value: String? = null,
    /** True while the loan waits for approval. */
    val pendingApproval: Boolean? = null,
    /** True while the loan waits to be disbursed. */
    val waitingForDisbursal: Boolean? = null,
    /** True when the record is active. */
    val active: Boolean? = null,
    /** True when the loan closed with every obligation met. */
    val closedObligationsMet: Boolean? = null,
    /** True when the loan closed by being written off. */
    val closedWrittenOff: Boolean? = null,
    /** True when the loan closed because it was rescheduled. */
    val closedRescheduled: Boolean? = null,
    /** True when the account is closed. */
    val closed: Boolean? = null,
    /** True when the loan has been overpaid. */
    val overpaid: Boolean? = null,
)
