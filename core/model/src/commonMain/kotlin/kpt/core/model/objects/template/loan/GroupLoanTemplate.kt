/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/**
 * Created by mayankjindal on 02/10/16.
 */
@Serializable
data class GroupLoanTemplate(
    var group: Group? = null,

    var loanProductId: Int? = null,

    var loanProductName: String? = null,

    var isLoanProductLinkedToFloatingRate: Boolean? = null,

    var fundId: Int? = null,

    var fundName: String? = null,

    var currency: Currency? = null,

    var principal: Double? = null,

    var approvedPrincipal: Double? = null,

    var proposedPrincipal: Double? = null,

    var termFrequency: Int? = null,

    var termPeriodFrequencyType: TermPeriodFrequencyType? = null,

    var numberOfRepayments: Int? = null,

    var repaymentEvery: Int? = null,

    var repaymentFrequencyType: RepaymentFrequencyType? = null,

    var interestRatePerPeriod: Double? = null,

    var interestRateFrequencyType: InterestRateFrequencyType? = null,

    var annualInterestRate: Double? = null,

    var isFloatingInterestRate: Boolean? = null,

    var amortizationType: AmortizationType? = null,

    var interestType: InterestType? = null,

    var interestCalculationPeriodType: InterestCalculationPeriodType? = null,

    var allowPartialPeriodInterestCalculation: Boolean? = null,

    var transactionProcessingStrategyId: Int? = null,

    var timeline: Timeline? = null,

    var charges: List<Charges>? = null,

    var productOptions: List<ProductOptions>? = null,

    var loanOfficerOptions: List<LoanOfficerOption>? = null,

    var loanPurposeOptions: List<LoanPurposeOptions>? = null,

    var fundOptions: List<FundOptions>? = null,

    var termFrequencyTypeOptions: List<TermFrequencyTypeOptions>? = null,

    var repaymentFrequencyTypeOptions: List<RepaymentFrequencyTypeOptions>? = null,

    var repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions>? = null,

    var repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions>? = null,

    var interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions>? = null,

    var amortizationTypeOptions: List<AmortizationTypeOptions>? = null,

    var interestTypeOptions: List<InterestTypeOptions>? = null,

    var interestCalculationPeriodTypeOptions: List<InterestCalculationPeriodType>? = null,

    var transactionProcessingStrategyOptions: List<TransactionProcessingStrategyOptions>? = null,

    var chargeOptions: List<ChargeOptions>? = null,

    var calendarOptions: List<CalendarOptions>? = null,

    var multiDisburseLoan: Boolean? = null,

    var canDefineInstallmentAmount: Boolean? = null,

    var canDisburse: Boolean? = null,

    var canUseForTopup: Boolean? = null,

    var isTopup: Boolean? = null,

    var product: Product? = null,

    var overdueCharges: List<OverdueCharges>? = null,

    var daysInMonthType: DaysInMonthType? = null,

    var daysInYearType: DaysInYearType? = null,

    var isInterestRecalculationEnabled: Boolean? = null,

    var interestRecalculationData: InterestRecalculationData? = null,

    var isVariableInstallmentsAllowed: Boolean? = null,

    var minimumGap: Int? = null,

    var maximumGap: Int? = null,
)
