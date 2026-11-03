/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.organisations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by Rajan Maurya on 12/15/2015.
 */
@Serializable
data class LoanProducts(
    /** The server id of this record. */
    @SerialName("id")
    var id: Int? = null,

    /** The display name. */
    @SerialName("name")
    var name: String? = null,

    /** The product's short name. */
    @SerialName("shortName")
    var shortName: String? = null,

    /** A free-text description. */
    @SerialName("description")
    var description: String? = null,

    /** The server id of the fund the loan is financed from. */
    @SerialName("fundId")
    var fundId: Int? = null,

    /** The name of the fund the loan is financed from. */
    @SerialName("fundName")
    var fundName: String? = null,

    /** True when the loan counts towards the borrower cycle. */
    @SerialName("includeInBorrowerCycle")
    var includeInBorrowerCycle: Boolean? = null,

    /** True when terms depend on the borrower cycle. */
    @SerialName("useBorrowerCycle")
    var useBorrowerCycle: Boolean? = null,

    /** The date this starts. */
    @SerialName("startDate")
    var startDate: List<Int?>? = null,

    /** The date the product closes. */
    @SerialName("closeDate")
    var closeDate: List<Int?>? = null,

    /** The record's Fineract status. */
    @SerialName("status")
    var status: String? = null,

    /** The currency the amounts are in. */
    @SerialName("currency")
    var currency: Currency? = null,

    /** The loan principal. */
    @SerialName("principal")
    var principal: Double? = null,

    /** The smallest principal the product allows. */
    @SerialName("minPrincipal")
    var minPrincipal: Double? = null,

    /** The largest principal the product allows. */
    @SerialName("maxPrincipal")
    var maxPrincipal: Double? = null,

    /** The number of installments. */
    @SerialName("numberOfRepayments")
    var numberOfRepayments: Int? = null,

    /** The fewest installments the product allows. */
    @SerialName("minNumberOfRepayments")
    var minNumberOfRepayments: Int? = null,

    /** The most installments the product allows. */
    @SerialName("maxNumberOfRepayments")
    var maxNumberOfRepayments: Int? = null,

    /** How many repayment-frequency units fall between two installments. */
    @SerialName("repaymentEvery")
    var repaymentEvery: Int? = null,

    /** The unit of the repayment frequency (days, weeks, months). */
    @SerialName("repaymentFrequencyType")
    var repaymentFrequencyType: RepaymentFrequencyType? = null,

    /** The interest rate per interest period, in percent. */
    @SerialName("interestRatePerPeriod")
    var interestRatePerPeriod: Double? = null,

    /** The lowest interest rate per period the product allows. */
    @SerialName("minInterestRatePerPeriod")
    var minInterestRatePerPeriod: Double? = null,

    /** The highest interest rate per period the product allows. */
    @SerialName("maxInterestRatePerPeriod")
    var maxInterestRatePerPeriod: Double? = null,

    /** The unit [interestRatePerPeriod] is per (month or year). */
    @SerialName("interestRateFrequencyType")
    var interestRateFrequencyType: InterestRateFrequencyType? = null,

    /** The nominal annual interest rate, in percent. */
    @SerialName("annualInterestRate")
    var annualInterestRate: Double? = null,

    /** True when the product is linked to floating interest rates. */
    @SerialName("isLinkedToFloatingInterestRates")
    var linkedToFloatingInterestRates: Boolean? = null,

    /** True when the floating-rate calculation may be used. */
    @SerialName("isFloatingInterestRateCalculationAllowed")
    var floatingInterestRateCalculationAllowed: Boolean? = null,

    /** True when installment amounts may vary. */
    @SerialName("allowVariableInstallments")
    var allowVariableInstallments: Boolean? = null,

    /** The smallest number of days allowed between two disbursements. */
    @SerialName("minimumGap")
    var minimumGap: Double? = null,

    /** The largest number of days allowed between two disbursements. */
    @SerialName("maximumGap")
    var maximumGap: Double? = null,

    /** How the principal is amortised (equal installments or equal principal). */
    @SerialName("amortizationType")
    var amortizationType: AmortizationType? = null,

    /** The interest method (declining balance or flat). */
    @SerialName("interestType")
    var interestType: InterestType? = null,

    /** How interest is calculated over a period (daily or same as the repayment period). */
    @SerialName("interestCalculationPeriodType")
    var interestCalculationPeriodType: InterestCalculationPeriodType? = null,

    /** True when interest is calculated for a partial first period. */
    @SerialName("allowPartialPeriodInterestCalculation")
    var allowPartialPeriodInterestCalculation: Boolean? = null,

    /** The server id of the repayment strategy (the order principal, interest, fees and penalties are paid in). */
    @SerialName("transactionProcessingStrategyId")
    var transactionProcessingStrategyId: Int? = null,

    /** The name of the repayment strategy. */
    @SerialName("transactionProcessingStrategyName")
    var transactionProcessingStrategyName: String? = null,

    /** The number of installments with no principal due. */
    @SerialName("graceOnPrincipalPayment")
    var graceOnPrincipalPayment: Int? = null,

    /** The number of installments with no interest due. */
    @SerialName("graceOnInterestPayment")
    var graceOnInterestPayment: Int? = null,

    /** How many days a month has for interest calculation (actual or 30). */
    @SerialName("daysInMonthType")
    var daysInMonthType: DaysInMonthType? = null,

    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    @SerialName("daysInYearType")
    var daysInYearType: DaysInYearType? = null,

    /** True when interest is recalculated as repayments arrive. */
    @SerialName("isInterestRecalculationEnabled")
    var interestRecalculationEnabled: Boolean? = null,

    /** True when the installment amount can be set on the account. */
    @SerialName("canDefineInstallmentAmount")
    var canDefineInstallmentAmount: Boolean? = null,

    /** The unit installment amounts are rounded to. */
    @SerialName("installmentAmountInMultiplesOf")
    var installmentAmountInMultiplesOf: Int? = null,

    /** The accounting rule (none, cash or accrual). */
    @SerialName("accountingRule")
    var accountingRule: AccountingRule? = null,

    /** True when the loan can be disbursed in several tranches. */
    @SerialName("multiDisburseLoan")
    var multiDisburseLoan: Boolean? = null,

    /** The most tranches the product allows. */
    @SerialName("maxTrancheCount")
    var maxTrancheCount: Int? = null,

    /** The share of an installment below which the last installment is merged into the previous one. */
    @SerialName("principalThresholdForLastInstallment")
    var principalThresholdForLastInstallment: Double? = null,

    /** True when guarantee funds are held on the guarantor's account. */
    @SerialName("holdGuaranteeFunds")
    var holdGuaranteeFunds: Boolean? = null,

    /** True when the loan leaves non-performing status only once its arrears are cleared. */
    @SerialName("accountMovesOutOfNPAOnlyOnArrearsCompletion")
    var accountMovesOutOfNPAOnlyOnArrearsCompletion: Boolean? = null,

    /** Which product attributes an account may override. */
    @SerialName("allowAttributeOverrides")
    var allowAttributeOverrides: AllowAttributeOverrides? = null,
)
