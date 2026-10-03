/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.loanWithAssociations

data class LoanAccountSummary(
    val loanId: Int? = null,
    val currency: SavingAccountCurrency? = null,
    val principalDisbursed: Double? = null,
    val principalPaid: Double? = null,
    val principalWaived: Double? = null,
    val principalWrittenOff: Double? = null,
    val principalOutstanding: Double? = null,
    val principalOverdue: Double? = null,
    val interestCharged: Double? = null,
    val interestPaid: Double? = null,
    val interestWaived: Double? = null,
    val interestWrittenOff: Double? = null,
    val interestOutstanding: Double? = null,
    val interestOverdue: Double? = null,
    val feeChargesCharged: Double? = null,
    val feeChargesDueAtDisbursementCharged: Double? = null,
    val feeChargesPaid: Double? = null,
    val feeChargesWaived: Double? = null,
    val feeChargesWrittenOff: Double? = null,
    val feeChargesOutstanding: Double? = null,
    val feeChargesOverdue: Double? = null,
    val penaltyChargesCharged: Double? = null,
    val penaltyChargesPaid: Double? = null,
    val penaltyChargesWaived: Double? = null,
    val penaltyChargesWrittenOff: Double? = null,
    val penaltyChargesOutstanding: Double? = null,
    val penaltyChargesOverdue: Double? = null,
    val totalExpectedRepayment: Double? = null,
    val totalRepayment: Double? = null,
    val totalExpectedCostOfLoan: Double? = null,
    val totalCostOfLoan: Double? = null,
    val totalWaived: Double? = null,
    val totalWrittenOff: Double? = null,
    val totalOutstanding: Double? = null,
    val totalOverdue: Double? = null,
    val overdueSinceDate: List<Int>? = null,
)
