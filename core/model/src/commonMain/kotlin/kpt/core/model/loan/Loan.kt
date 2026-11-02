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
import kpt.core.model.objects.account.loan.InterestCalculationPeriodType
import kpt.core.model.objects.account.loan.InterestRateFrequencyType
import kpt.core.model.objects.account.loan.InterestType
import kpt.core.model.objects.account.loan.RepaymentFrequencyType
import kpt.core.model.objects.account.loan.TermPeriodFrequencyType

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `Loan`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class Loan(
    val id: Int? = null,
    val accountNo: String? = null,
    val status: LoanStatus? = null,
    val clientId: Int? = null,
    val clientName: String? = null,
    val clientOfficeId: Int? = null,
    val loanProductId: Int? = null,
    val loanProductName: String? = null,
    val loanProductDescription: String? = null,
    val fundId: Int? = null,
    val fundName: String? = null,
    val loanOfficerId: Int? = null,
    val loanOfficerName: String? = null,
    val loanType: LoanAccountType? = null,
    val currency: Currency? = null,
    val principal: Double? = null,
    val approvedPrincipal: Double? = null,
    val termFrequency: Int? = null,
    val termPeriodFrequencyType: TermPeriodFrequencyType? = null,
    val numberOfRepayments: Int? = null,
    val repaymentEvery: Int? = null,
    val repaymentFrequencyType: RepaymentFrequencyType? = null,
    val interestRatePerPeriod: Double? = null,
    val interestRateFrequencyType: InterestRateFrequencyType? = null,
    val annualInterestRate: Double? = null,
    val amortizationType: AmortizationType? = null,
    val interestType: InterestType? = null,
    val interestCalculationPeriodType: InterestCalculationPeriodType? = null,
    val transactionProcessingStrategyId: Int? = null,
    val transactionProcessingStrategyName: String? = null,
    val syncDisbursementWithMeeting: Boolean? = null,
    val timeline: LoanTimeline? = null,
    val summary: LoanAccountSummary? = null,
    val feeChargesAtDisbursementCharged: Double? = null,
    val loanCounter: Int? = null,
    val loanProductCounter: Int? = null,
    val multiDisburseLoan: Boolean? = null,
    val canDisburse: Boolean? = null,
    val inArrears: Boolean? = null,
    val isNPA: Boolean? = null,
)
