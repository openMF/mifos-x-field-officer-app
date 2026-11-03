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
 * Created by Rajan Maurya on 16/07/16.
 */
@Serializable
data class Product(
    /** The server id of this record. */
    var id: Int? = null,

    /** The display name. */
    var name: String? = null,

    /** The product's short name. */
    var shortName: String? = null,

    /** The server id of the fund the loan is financed from. */
    var fundId: Int? = null,

    /** The name of the fund the loan is financed from. */
    var fundName: String? = null,

    /** True when the loan counts towards the borrower cycle. */
    var includeInBorrowerCycle: Boolean? = null,

    /** True when terms depend on the borrower cycle. */
    var useBorrowerCycle: Boolean? = null,

    /** The date this starts. */
    var startDate: List<Int>? = null,

    /** The record's Fineract status. */
    var status: String? = null,

    /** The currency the amounts are in. */
    var currency: Currency? = null,

    /** The loan principal. */
    var principal: Double? = null,

    /** The smallest principal the product allows. */
    var minPrincipal: Double? = null,

    /** The largest principal the product allows. */
    var maxPrincipal: Double? = null,

    /** The number of installments. */
    var numberOfRepayments: Int? = null,

    /** The fewest installments the product allows. */
    var minNumberOfRepayments: Int? = null,

    /** The most installments the product allows. */
    var maxNumberOfRepayments: Int? = null,

    /** How many repayment-frequency units fall between two installments. */
    var repaymentEvery: Int? = null,

    /** The unit of the repayment frequency (days, weeks, months). */
    var repaymentFrequencyType: RepaymentFrequencyType? = null,

    /** The interest rate per interest period, in percent. */
    var interestRatePerPeriod: Double? = null,

    /** The lowest interest rate per period the product allows. */
    var minInterestRatePerPeriod: Double? = null,

    /** The highest interest rate per period the product allows. */
    var maxInterestRatePerPeriod: Double? = null,

    /** The unit [interestRatePerPeriod] is per (month or year). */
    var interestRateFrequencyType: InterestRateFrequencyType? = null,

    /** The nominal annual interest rate, in percent. */
    var annualInterestRate: Double? = null,

    /** True when the product is linked to floating interest rates. */
    var isLinkedToFloatingInterestRates: Boolean? = null,

    /** True when the floating-rate calculation may be used. */
    var isFloatingInterestRateCalculationAllowed: Boolean? = null,

    /** True when installment amounts may vary. */
    var allowVariableInstallments: Boolean? = null,

    /** The smallest number of days allowed between two disbursements. */
    var minimumGap: Double? = null,

    /** The largest number of days allowed between two disbursements. */
    var maximumGap: Double? = null,

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

    /** The name of the repayment strategy. */
    var transactionProcessingStrategyName: String? = null,

    /** The number of days an overdue installment is not yet counted as in arrears. */
    var graceOnArrearsAgeing: Int? = null,

    /** The number of days overdue after which the loan becomes non-performing. */
    var overdueDaysForNPA: Int? = null,

    /** How many days a month has for interest calculation (actual or 30). */
    var daysInMonthType: DaysInMonthType? = null,

    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    var daysInYearType: DaysInYearType? = null,

    /** True when interest is recalculated as repayments arrive. */
    var isInterestRecalculationEnabled: Boolean? = null,

    /** The product's interest-recalculation settings. */
    var interestRecalculationData: InterestRecalculationData? = null,

    /** True when the installment amount can be set on the account. */
    var canDefineInstallmentAmount: Boolean? = null,

    /** The accounting rule (none, cash or accrual). */
    var accountingRule: AccountingRule? = null,

    /** True when the loan can be disbursed in several tranches. */
    var multiDisburseLoan: Boolean? = null,

    /** The most tranches the product allows. */
    var maxTrancheCount: Int? = null,

    /** The share of an installment below which the last installment is merged into the previous one. */
    var principalThresholdForLastInstallment: Double? = null,

    /** True when guarantee funds are held on the guarantor's account. */
    var holdGuaranteeFunds: Boolean? = null,

    /** True when the loan leaves non-performing status only once its arrears are cleared. */
    var accountMovesOutOfNPAOnlyOnArrearsCompletion: Boolean? = null,

    /** Which product attributes an account may override. */
    var allowAttributeOverrides: AllowAttributeOverrides? = null,
)
