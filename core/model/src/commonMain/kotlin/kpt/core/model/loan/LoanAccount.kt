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
import kpt.core.model.objects.account.loan.Currency

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanAccountEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class LoanAccount(
    /** The server id of this record. */
    val id: Int? = null,
    /** The server id of the client. */
    val clientId: Long = 0,
    /** The server id of the group. */
    val groupId: Long = 0,
    /** The server id of the centre. */
    val centerId: Long = 0,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** The server id of the product. */
    val productId: Int? = null,
    /** The name of the product. */
    val productName: String? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** The record's Fineract status. */
    val status: LoanStatus? = null,
    /** The loan type (individual, group or JLG). */
    val loanType: LoanAccountType? = null,
    /** The loan's cycle number for the client. */
    val loanCycle: Int? = null,
    /** True when an installment is overdue. */
    val inArrears: Boolean? = null,
    /** The original loan amount. */
    val originalLoan: Double? = null,
    /** The loan balance. */
    val loanBalance: Double? = null,
    /** The amount already paid. */
    val amountPaid: Double? = null,
)
