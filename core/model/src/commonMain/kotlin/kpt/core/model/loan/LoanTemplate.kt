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
import kpt.core.model.charge.Charges
import kpt.core.model.datatable.DataTable
import kpt.core.model.objects.account.loan.AccountLinkingOptions
import kpt.core.model.objects.template.loan.AmortizationType
import kpt.core.model.objects.template.loan.AmortizationTypeOptions
import kpt.core.model.objects.template.loan.ChargeOptions
import kpt.core.model.objects.template.loan.Currency
import kpt.core.model.objects.template.loan.DaysInMonthType
import kpt.core.model.objects.template.loan.DaysInYearType
import kpt.core.model.objects.template.loan.FundOptions
import kpt.core.model.objects.template.loan.InterestCalculationPeriodType
import kpt.core.model.objects.template.loan.InterestRateFrequencyType
import kpt.core.model.objects.template.loan.InterestRateFrequencyTypeOptions
import kpt.core.model.objects.template.loan.InterestType
import kpt.core.model.objects.template.loan.InterestTypeOptions
import kpt.core.model.objects.template.loan.LoanCollateralOptions
import kpt.core.model.objects.template.loan.LoanOfficerOption
import kpt.core.model.objects.template.loan.LoanPurposeOptions
import kpt.core.model.objects.template.loan.Product
import kpt.core.model.objects.template.loan.ProductOptions
import kpt.core.model.objects.template.loan.RepaymentFrequencyDaysOfWeekTypeOptions
import kpt.core.model.objects.template.loan.RepaymentFrequencyNthDayTypeOptions
import kpt.core.model.objects.template.loan.RepaymentFrequencyType
import kpt.core.model.objects.template.loan.RepaymentFrequencyTypeOptions
import kpt.core.model.objects.template.loan.TermFrequencyTypeOptions
import kpt.core.model.objects.template.loan.TermPeriodFrequencyType
import kpt.core.model.objects.template.loan.Timeline
import kpt.core.model.objects.template.loan.TransactionProcessingStrategyOptions

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanTemplate`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class LoanTemplate(
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The client's account number. */
    val clientAccountNo: String? = null,
    /** The client's display name. */
    val clientName: String? = null,
    /** The server id of the client's office. */
    val clientOfficeId: Int? = null,
    /** The server id of the loan product. */
    val loanProductId: Int? = null,
    /** The name of the loan product. */
    val loanProductName: String? = null,
    /** True when the product is linked to a floating rate. */
    val isLoanProductLinkedToFloatingRate: Boolean? = null,
    /** The server id of the fund the loan is financed from. */
    val fundId: Int? = null,
    /** The name of the fund the loan is financed from. */
    val fundName: String? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** The loan principal. */
    val principal: Double? = null,
    /** The principal that was approved. */
    val approvedPrincipal: Double? = null,
    /** The principal applied for. */
    val proposedPrincipal: Double? = null,
    /** The loan term, in [termPeriodFrequencyType] units. */
    val termFrequency: Int? = null,
    /** The unit of the loan term. */
    val termPeriodFrequencyType: TermPeriodFrequencyType? = null,
    /** The number of installments. */
    val numberOfRepayments: Int? = null,
    /** How many repayment-frequency units fall between two installments. */
    val repaymentEvery: Int? = null,
    /** The unit of the repayment frequency (days, weeks, months). */
    val repaymentFrequencyType: RepaymentFrequencyType? = null,
    /** The interest rate per interest period, in percent. */
    val interestRatePerPeriod: Double? = null,
    /** The unit [interestRatePerPeriod] is per (month or year). */
    val interestRateFrequencyType: InterestRateFrequencyType? = null,
    /** The nominal annual interest rate, in percent. */
    val annualInterestRate: Double? = null,
    /** True when the interest rate floats. */
    val isFloatingInterestRate: Boolean? = null,
    /** How the principal is amortised (equal installments or equal principal). */
    val amortizationType: AmortizationType? = null,
    /** The interest method (declining balance or flat). */
    val interestType: InterestType? = null,
    /** How interest is calculated over a period (daily or same as the repayment period). */
    val interestCalculationPeriodType: InterestCalculationPeriodType? = null,
    /** True when interest is calculated for a partial first period. */
    val allowPartialPeriodInterestCalculation: Boolean? = null,
    /** The server id of the repayment strategy (the order principal, interest, fees and penalties are paid in). */
    val transactionProcessingStrategyId: Int? = null,
    /** The number of days an overdue installment is not yet counted as in arrears. */
    val graceOnArrearsAgeing: Int? = null,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: Timeline? = null,
    /** The products that may be chosen. */
    val productOptions: List<ProductOptions> = emptyList(),
    /** The data tables registered for this entity. */
    val dataTables: ArrayList<DataTable> = ArrayList(),
    /** The loan officers that may be assigned. */
    val loanOfficerOptions: List<LoanOfficerOption> = emptyList(),
    /** The loan purposes that may be chosen. */
    val loanPurposeOptions: List<LoanPurposeOptions> = emptyList(),
    /** The funds that may be chosen. */
    val fundOptions: List<FundOptions> = emptyList(),
    /** The term unit choices. */
    val termFrequencyTypeOptions: List<TermFrequencyTypeOptions> = emptyList(),
    /** The repayment frequency choices. */
    val repaymentFrequencyTypeOptions: List<RepaymentFrequencyTypeOptions> = emptyList(),
    /** The nth-day choices for the repayment day. */
    val repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions> = emptyList(),
    /** The weekday choices for the repayment day. */
    val repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions> = emptyList(),
    /** The interest rate frequency choices. */
    val interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions> = emptyList(),
    /** The amortisation choices. */
    val amortizationTypeOptions: List<AmortizationTypeOptions> = emptyList(),
    /** The interest method choices. */
    val interestTypeOptions: List<InterestTypeOptions> = emptyList(),
    /** The interest calculation period choices. */
    val interestCalculationPeriodTypeOptions: List<InterestCalculationPeriodType> = emptyList(),
    /** The repayment strategies that may be chosen. */
    val transactionProcessingStrategyOptions: List<TransactionProcessingStrategyOptions> = emptyList(),
    /** The charges that may be added. */
    val chargeOptions: List<ChargeOptions> = emptyList(),
    /** The collateral types that may be pledged. */
    val loanCollateralOptions: List<LoanCollateralOptions> = emptyList(),
    /** True when the loan can be disbursed in several tranches. */
    val multiDisburseLoan: Boolean? = null,
    /** The code of the repayment strategy. */
    val transactionProcessingStrategyCode: String? = null,
    /** True when the installment amount can be set on the account. */
    val canDefineInstallmentAmount: Boolean? = null,
    /** True when the loan can be disbursed now. */
    val canDisburse: Boolean? = null,
    /** The product. */
    val product: Product? = null,
    /** How many days a month has for interest calculation (actual or 30). */
    val daysInMonthType: DaysInMonthType? = null,
    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    val daysInYearType: DaysInYearType? = null,
    /** True when interest is recalculated as repayments arrive. */
    val isInterestRecalculationEnabled: Boolean? = null,
    /** True when installment amounts may vary. */
    val isvaliableInstallmentsAllowed: Boolean? = null,
    /** The smallest number of days allowed between two disbursements. */
    val minimumGap: Int? = null,
    /** The largest number of days allowed between two disbursements. */
    val maximumGap: Int? = null,
    /** The savings accounts that may be linked for repayments. */
    val accountLinkingOptions: List<AccountLinkingOptions> = emptyList(),
    /** The loan's schedule type. */
    val loanScheduleType: TermFrequencyTypeOptions? = null,
    /** The charges applied when an installment is overdue. */
    val overdueCharges: List<Charges> = emptyList(),
)
