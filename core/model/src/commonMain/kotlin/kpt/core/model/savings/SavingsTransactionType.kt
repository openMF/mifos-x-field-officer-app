/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savings

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsTransactionTypeEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class SavingsTransactionType(
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
