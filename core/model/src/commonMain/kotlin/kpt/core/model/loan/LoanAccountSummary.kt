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
import kpt.core.model.savings.SavingAccountCurrency

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanAccountSummaryEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class LoanAccountSummary(
    /** The server id of the loan. */
    val loanId: Int? = null,
    /** The currency the amounts are in. */
    val currency: SavingAccountCurrency? = null,
    /** The principal disbursed. */
    val principalDisbursed: Double? = null,
    /** The principal already paid. */
    val principalPaid: Double? = null,
    /** The principal waived. */
    val principalWaived: Double? = null,
    /** The principal written off. */
    val principalWrittenOff: Double? = null,
    /** The principal still outstanding. */
    val principalOutstanding: Double? = null,
    /** The principal overdue. */
    val principalOverdue: Double? = null,
    /** The interest charged. */
    val interestCharged: Double? = null,
    /** The interest already paid. */
    val interestPaid: Double? = null,
    /** The interest waived. */
    val interestWaived: Double? = null,
    /** The interest written off. */
    val interestWrittenOff: Double? = null,
    /** The interest still outstanding. */
    val interestOutstanding: Double? = null,
    /** The interest overdue. */
    val interestOverdue: Double? = null,
    /** The fees charged. */
    val feeChargesCharged: Double? = null,
    /** The fees charged at disbursement. */
    val feeChargesDueAtDisbursementCharged: Double? = null,
    /** The fees already paid. */
    val feeChargesPaid: Double? = null,
    /** The fees waived. */
    val feeChargesWaived: Double? = null,
    /** The fees written off. */
    val feeChargesWrittenOff: Double? = null,
    /** The fees still outstanding. */
    val feeChargesOutstanding: Double? = null,
    /** The fees overdue. */
    val feeChargesOverdue: Double? = null,
    /** The penalties charged. */
    val penaltyChargesCharged: Double? = null,
    /** The penalties already paid. */
    val penaltyChargesPaid: Double? = null,
    /** The penalties waived. */
    val penaltyChargesWaived: Double? = null,
    /** The penalties written off. */
    val penaltyChargesWrittenOff: Double? = null,
    /** The penalties still outstanding. */
    val penaltyChargesOutstanding: Double? = null,
    /** The penalties overdue. */
    val penaltyChargesOverdue: Double? = null,
    /** The total expected to be repaid. */
    val totalExpectedRepayment: Double? = null,
    /** The total repaid. */
    val totalRepayment: Double? = null,
    /** The expected total cost of the loan. */
    val totalExpectedCostOfLoan: Double? = null,
    /** The total cost of the loan: interest, fees and penalties. */
    val totalCostOfLoan: Double? = null,
    /** The total waived. */
    val totalWaived: Double? = null,
    /** The total written off. */
    val totalWrittenOff: Double? = null,
    /** The total amount outstanding. */
    val totalOutstanding: Double? = null,
    /** The total amount overdue. */
    val totalOverdue: Double? = null,
    /** The date the loan has been overdue since. */
    val overdueSinceDate: List<Int>? = null,
)
