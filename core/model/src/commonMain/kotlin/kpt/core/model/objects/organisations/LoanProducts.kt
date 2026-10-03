/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
    @SerialName("id")
    var id: Int? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("shortName")
    var shortName: String? = null,

    @SerialName("description")
    var description: String? = null,

    @SerialName("fundId")
    var fundId: Int? = null,

    @SerialName("fundName")
    var fundName: String? = null,

    @SerialName("includeInBorrowerCycle")
    var includeInBorrowerCycle: Boolean? = null,

    @SerialName("useBorrowerCycle")
    var useBorrowerCycle: Boolean? = null,

    @SerialName("startDate")
    var startDate: List<Int?>? = null,

    @SerialName("closeDate")
    var closeDate: List<Int?>? = null,

    @SerialName("status")
    var status: String? = null,

    @SerialName("currency")
    var currency: Currency? = null,

    @SerialName("principal")
    var principal: Double? = null,

    @SerialName("minPrincipal")
    var minPrincipal: Double? = null,

    @SerialName("maxPrincipal")
    var maxPrincipal: Double? = null,

    @SerialName("numberOfRepayments")
    var numberOfRepayments: Int? = null,

    @SerialName("minNumberOfRepayments")
    var minNumberOfRepayments: Int? = null,

    @SerialName("maxNumberOfRepayments")
    var maxNumberOfRepayments: Int? = null,

    @SerialName("repaymentEvery")
    var repaymentEvery: Int? = null,

    @SerialName("repaymentFrequencyType")
    var repaymentFrequencyType: RepaymentFrequencyType? = null,

    @SerialName("interestRatePerPeriod")
    var interestRatePerPeriod: Double? = null,

    @SerialName("minInterestRatePerPeriod")
    var minInterestRatePerPeriod: Double? = null,

    @SerialName("maxInterestRatePerPeriod")
    var maxInterestRatePerPeriod: Double? = null,

    @SerialName("interestRateFrequencyType")
    var interestRateFrequencyType: InterestRateFrequencyType? = null,

    @SerialName("annualInterestRate")
    var annualInterestRate: Double? = null,

    @SerialName("isLinkedToFloatingInterestRates")
    var linkedToFloatingInterestRates: Boolean? = null,

    @SerialName("isFloatingInterestRateCalculationAllowed")
    var floatingInterestRateCalculationAllowed: Boolean? = null,

    @SerialName("allowVariableInstallments")
    var allowVariableInstallments: Boolean? = null,

    @SerialName("minimumGap")
    var minimumGap: Double? = null,

    @SerialName("maximumGap")
    var maximumGap: Double? = null,

    @SerialName("amortizationType")
    var amortizationType: AmortizationType? = null,

    @SerialName("interestType")
    var interestType: InterestType? = null,

    @SerialName("interestCalculationPeriodType")
    var interestCalculationPeriodType: InterestCalculationPeriodType? = null,

    @SerialName("allowPartialPeriodInterestCalculation")
    var allowPartialPeriodInterestCalculation: Boolean? = null,

    @SerialName("transactionProcessingStrategyId")
    var transactionProcessingStrategyId: Int? = null,

    @SerialName("transactionProcessingStrategyName")
    var transactionProcessingStrategyName: String? = null,

    @SerialName("graceOnPrincipalPayment")
    var graceOnPrincipalPayment: Int? = null,

    @SerialName("graceOnInterestPayment")
    var graceOnInterestPayment: Int? = null,

    @SerialName("daysInMonthType")
    var daysInMonthType: DaysInMonthType? = null,

    @SerialName("daysInYearType")
    var daysInYearType: DaysInYearType? = null,

    @SerialName("isInterestRecalculationEnabled")
    var interestRecalculationEnabled: Boolean? = null,

    @SerialName("canDefineInstallmentAmount")
    var canDefineInstallmentAmount: Boolean? = null,

    @SerialName("installmentAmountInMultiplesOf")
    var installmentAmountInMultiplesOf: Int? = null,

    @SerialName("accountingRule")
    var accountingRule: AccountingRule? = null,

    @SerialName("multiDisburseLoan")
    var multiDisburseLoan: Boolean? = null,

    @SerialName("maxTrancheCount")
    var maxTrancheCount: Int? = null,

    @SerialName("principalThresholdForLastInstallment")
    var principalThresholdForLastInstallment: Double? = null,

    @SerialName("holdGuaranteeFunds")
    var holdGuaranteeFunds: Boolean? = null,

    @SerialName("accountMovesOutOfNPAOnlyOnArrearsCompletion")
    var accountMovesOutOfNPAOnlyOnArrearsCompletion: Boolean? = null,

    @SerialName("allowAttributeOverrides")
    var allowAttributeOverrides: AllowAttributeOverrides? = null,
)
