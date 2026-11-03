/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.entity

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
data class SavingsTransactionTypeEntity(
    /** The server id of this record. */
    val id: Int? = null,

    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,

    /** The human-readable label Fineract sends for this value. */
    val value: String? = null,

    /** True for a deposit. */
    val deposit: Boolean? = null,

    /** True for a withdrawal. */
    val withdrawal: Boolean? = null,

    /** True for an interest posting. */
    val interestPosting: Boolean? = null,

    /** True for a fee deduction. */
    val feeDeduction: Boolean? = null,

    /** True for an initiate-transfer transaction. */
    val initiateTransfer: Boolean? = null,

    /** True for an approve-transfer transaction. */
    val approveTransfer: Boolean? = null,

    /** True for a withdraw-transfer transaction. */
    val withdrawTransfer: Boolean? = null,

    /** True for a reject-transfer transaction. */
    val rejectTransfer: Boolean? = null,

    /** True for overdraft interest. */
    val overdraftInterest: Boolean? = null,

    /** True for a write-off. */
    val writtenoff: Boolean? = null,

    /** True for an overdraft fee. */
    val overdraftFee: Boolean? = null,
)
