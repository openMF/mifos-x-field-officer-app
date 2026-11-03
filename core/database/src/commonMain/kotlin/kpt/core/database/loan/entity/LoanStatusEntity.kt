/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.entity

import kotlinx.serialization.Serializable

/**
 * A COLUMN, not a table.
 *
 * This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON
 * string. It carried `@DbEntity` as well, so Room also generated a standalone table that no
 * DAO ever read or wrote — dead schema that counted against the generated
 * `onValidateSchema`, which at 85 tables exceeded the JVM 64KB method limit and broke every
 * JVM target.
 */
@Serializable
data class LoanStatusEntity(
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
