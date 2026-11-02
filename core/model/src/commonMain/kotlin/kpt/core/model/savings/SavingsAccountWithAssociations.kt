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
import kpt.core.model.loan.LoanTimeline
import kpt.core.model.objects.account.saving.InterestCalculationDaysInYearType
import kpt.core.model.objects.account.saving.InterestCalculationType
import kpt.core.model.objects.account.saving.InterestCompoundingPeriodType
import kpt.core.model.objects.account.saving.InterestPostingPeriodType
import kpt.core.model.objects.account.saving.LockinPeriodFrequencyType

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s
 * `SavingsAccountWithAssociationsEntity`.
 * Same field names as the row; the `core/store/mapping` mappers convert between them.
 */
@Serializable
data class SavingsAccountWithAssociations(
    val id: Int? = null,
    val accountNo: String? = null,
    val clientId: Int? = null,
    val clientName: String? = null,
    /** A GROUP account's owner (`groupName`) — Fineract leaves `clientName` null on one. Wire-only: not a column. */
    val groupName: String? = null,
    val savingsProductId: Int? = null,
    val savingsProductName: String? = null,
    val fieldOfficerId: Int? = null,
    val status: SavingsAccountStatus? = null,
    val timeline: LoanTimeline? = null,
    val currency: SavingAccountCurrency? = null,
    val nominalAnnualInterestRate: Double? = null,
    val interestCompoundingPeriodType: InterestCompoundingPeriodType? = null,
    val interestPostingPeriodType: InterestPostingPeriodType? = null,
    val interestCalculationType: InterestCalculationType? = null,
    val interestCalculationDaysInYearType: InterestCalculationDaysInYearType? = null,
    val minRequiredOpeningBalance: Double? = null,
    val lockinPeriodFrequency: Int? = null,
    val lockinPeriodFrequencyType: LockinPeriodFrequencyType? = null,
    val withdrawalFeeForTransfers: Boolean? = null,
    val allowOverdraft: Boolean? = null,
    val enforceMinRequiredBalance: Boolean? = null,
    val withHoldTax: Boolean? = null,
    val lastActiveTransactionDate: List<Int?> = emptyList(),
    val dormancyTrackingActive: Boolean? = null,
    val overdraftLimit: Int? = null,
    val summary: SavingsAccountSummary? = null,
    val transactions: List<SavingsAccountTransaction> = emptyList(),
    val charges: List<Charge?> = emptyList(),
)
