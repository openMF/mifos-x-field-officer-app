/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/**
 * Created by mayankjindal on 02/10/16.
 */
@Serializable
data class GroupLoanTemplate(
    /** The group. */
    var group: Group? = null,

    /** The server id of the loan product. */
    var loanProductId: Int? = null,

    /** The name of the loan product. */
    var loanProductName: String? = null,

    /** True when the product is linked to a floating rate. */
    var isLoanProductLinkedToFloatingRate: Boolean? = null,

    /** The server id of the fund the loan is financed from. */
    var fundId: Int? = null,

    /** The name of the fund the loan is financed from. */
    var fundName: String? = null,

    /** The currency the amounts are in. */
    var currency: Currency? = null,

    /** The loan principal. */
    var principal: Double? = null,

    /** The principal that was approved. */
    var approvedPrincipal: Double? = null,

    /** The principal applied for. */
    var proposedPrincipal: Double? = null,

    /** The loan term, in [termPeriodFrequencyType] units. */
    var termFrequency: Int? = null,

    /** The unit of the loan term. */
    var termPeriodFrequencyType: TermPeriodFrequencyType? = null,

    /** The number of installments. */
    var numberOfRepayments: Int? = null,

    /** How many repayment-frequency units fall between two installments. */
    var repaymentEvery: Int? = null,

    /** The unit of the repayment frequency (days, weeks, months). */
    var repaymentFrequencyType: RepaymentFrequencyType? = null,

    /** The interest rate per interest period, in percent. */
    var interestRatePerPeriod: Double? = null,

    /** The unit [interestRatePerPeriod] is per (month or year). */
    var interestRateFrequencyType: InterestRateFrequencyType? = null,

    /** The nominal annual interest rate, in percent. */
    var annualInterestRate: Double? = null,

    /** True when the interest rate floats. */
    var isFloatingInterestRate: Boolean? = null,

    /** How the principal is amortised (equal installments or equal principal). */
    var amortizationType: AmortizationType? = null,

    /** The interest method (declining balance or flat). */
    var interestType: InterestType? = null,

    /** How interest is calculated over a period (daily or same as the repayment period). */
    var interestCalculationPeriodType: InterestCalculationPeriodType? = null,

    /** True when interest is calculated for a partial first period. */
    var allowPartialPeriodInterestCalculation: Boolean? = null,

    /** The server id of the repayment strategy (the order principal, interest, fees and penalties are paid in). */
    var transactionProcessingStrategyId: Int? = null,

    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    var timeline: Timeline? = null,

    /** The charges. */
    var charges: List<Charges>? = null,

    /** The products that may be chosen. */
    var productOptions: List<ProductOptions>? = null,

    /** The loan officers that may be assigned. */
    var loanOfficerOptions: List<LoanOfficerOption>? = null,

    /** The loan purposes that may be chosen. */
    var loanPurposeOptions: List<LoanPurposeOptions>? = null,

    /** The funds that may be chosen. */
    var fundOptions: List<FundOptions>? = null,

    /** The term unit choices. */
    var termFrequencyTypeOptions: List<TermFrequencyTypeOptions>? = null,

    /** The repayment frequency choices. */
    var repaymentFrequencyTypeOptions: List<RepaymentFrequencyTypeOptions>? = null,

    /** The nth-day choices for the repayment day. */
    var repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions>? = null,

    /** The weekday choices for the repayment day. */
    var repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions>? = null,

    /** The interest rate frequency choices. */
    var interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions>? = null,

    /** The amortisation choices. */
    var amortizationTypeOptions: List<AmortizationTypeOptions>? = null,

    /** The interest method choices. */
    var interestTypeOptions: List<InterestTypeOptions>? = null,

    /** The interest calculation period choices. */
    var interestCalculationPeriodTypeOptions: List<InterestCalculationPeriodType>? = null,

    /** The repayment strategies that may be chosen. */
    var transactionProcessingStrategyOptions: List<TransactionProcessingStrategyOptions>? = null,

    /** The charges that may be added. */
    var chargeOptions: List<ChargeOptions>? = null,

    /** The meeting calendars the loan may follow. */
    var calendarOptions: List<CalendarOptions>? = null,

    /** True when the loan can be disbursed in several tranches. */
    var multiDisburseLoan: Boolean? = null,

    /** True when the installment amount can be set on the account. */
    var canDefineInstallmentAmount: Boolean? = null,

    /** True when the loan can be disbursed now. */
    var canDisburse: Boolean? = null,

    /** True when the product can be used for a top-up loan. */
    var canUseForTopup: Boolean? = null,

    /** True for a top-up loan. */
    var isTopup: Boolean? = null,

    /** The product. */
    var product: Product? = null,

    /** The charges applied when an installment is overdue. */
    var overdueCharges: List<OverdueCharges>? = null,

    /** How many days a month has for interest calculation (actual or 30). */
    var daysInMonthType: DaysInMonthType? = null,

    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    var daysInYearType: DaysInYearType? = null,

    /** True when interest is recalculated as repayments arrive. */
    var isInterestRecalculationEnabled: Boolean? = null,

    /** The product's interest-recalculation settings. */
    var interestRecalculationData: InterestRecalculationData? = null,

    /** True when installment amounts may vary. */
    var isVariableInstallmentsAllowed: Boolean? = null,

    /** The smallest number of days allowed between two disbursements. */
    var minimumGap: Int? = null,

    /** The largest number of days allowed between two disbursements. */
    var maximumGap: Int? = null,
)
