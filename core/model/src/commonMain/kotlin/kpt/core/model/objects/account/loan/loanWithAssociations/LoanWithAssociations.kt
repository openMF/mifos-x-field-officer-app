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

import kpt.core.model.objects.account.loan.AmortizationType
import kpt.core.model.objects.account.loan.InterestCalculationPeriodType
import kpt.core.model.objects.account.loan.InterestRateFrequencyType
import kpt.core.model.objects.account.loan.InterestType
import kpt.core.model.objects.account.loan.RepaymentFrequencyType
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.TermPeriodFrequencyType
import kpt.core.model.objects.account.loan.Transaction

data class LoanWithAssociations(
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
    val loanPurposeId: Int? = null,
    val loanPurposeName: String? = null,
    val loanOfficerId: Int? = null,
    val loanOfficerName: String? = null,
    val loanType: LoanType? = null,
    val currency: SavingAccountCurrency? = null,
    val principal: Double? = null,
    val approvedPrincipal: Double? = null,
    val proposedPrincipal: Double? = null,
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
    val repaymentSchedule: RepaymentSchedule? = null,
    val transactions: List<Transaction>? = null,
    val feeChargesAtDisbursementCharged: Double? = null,
    val totalOverpaid: Double? = null,
    val loanCounter: Int? = null,
    val loanProductCounter: Int? = null,
    val multiDisburseLoan: Boolean? = null,
    val canDisburse: Boolean? = null,
    val inArrears: Boolean? = null,
    val isNPA: Boolean? = null,
    val overpaidOnDate: List<Int>? = null,
    val isEqualAmortization: Boolean? = null,
    val allowPartialPeriodInterestCalculation: Boolean? = null,
    val interestRecognitionOnDisbursementDate: Boolean? = null,
    val enableDownPayment: Boolean? = null,
    val enableIncomeCapitalization: Boolean? = null,
    val enableBuyDownFee: Boolean? = null,
    val enableInstallmentLevelDelinquency: Boolean? = null,
    val isInterestRecalculationEnabled: Boolean? = null,
    val chargedOff: Boolean? = null,
)
