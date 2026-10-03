/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.entity

import kotlinx.serialization.Serializable
import kpt.core.model.loan.AmortizationType
import kpt.core.model.objects.account.loan.Currency
import kpt.core.model.objects.account.loan.InterestCalculationPeriodType
import kpt.core.model.objects.account.loan.InterestRateFrequencyType
import kpt.core.model.objects.account.loan.InterestType
import kpt.core.model.objects.account.loan.RepaymentFrequencyType
import kpt.core.model.objects.account.loan.TermPeriodFrequencyType

@Serializable
data class Loan(
    val id: Int? = null,

    val accountNo: String? = null,

    val status: LoanStatusEntity? = null,

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

    val loanType: LoanTypeEntity? = null,

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

    val timeline: LoanTimelineEntity? = null,

    val summary: LoanAccountSummaryEntity? = null,

    val feeChargesAtDisbursementCharged: Double? = null,

    val loanCounter: Int? = null,

    val loanProductCounter: Int? = null,

    val multiDisburseLoan: Boolean? = null,

    val canDisburse: Boolean? = null,

    val inArrears: Boolean? = null,

    val isNPA: Boolean? = null,

//    val overdueCharges: List<Any> = emptyList(),
//
//    val additionalProperties: MutableMap<String, Any> = mutableMapOf(),
)
