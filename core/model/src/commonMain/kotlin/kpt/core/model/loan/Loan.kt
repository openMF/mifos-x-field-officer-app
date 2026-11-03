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
    /** The server id of this record. */
    val id: Int? = null,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The record's Fineract status. */
    val status: LoanStatus? = null,
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The client's display name. */
    val clientName: String? = null,
    /** The server id of the client's office. */
    val clientOfficeId: Int? = null,
    /** The server id of the loan product. */
    val loanProductId: Int? = null,
    /** The name of the loan product. */
    val loanProductName: String? = null,
    /** The description of the loan product. */
    val loanProductDescription: String? = null,
    /** The server id of the fund the loan is financed from. */
    val fundId: Int? = null,
    /** The name of the fund the loan is financed from. */
    val fundName: String? = null,
    /** The server id of the loan officer. */
    val loanOfficerId: Int? = null,
    /** The loan officer's display name. */
    val loanOfficerName: String? = null,
    /** The loan type (individual, group or JLG). */
    val loanType: LoanAccountType? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** The loan principal. */
    val principal: Double? = null,
    /** The principal that was approved. */
    val approvedPrincipal: Double? = null,
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
    /** How the principal is amortised (equal installments or equal principal). */
    val amortizationType: AmortizationType? = null,
    /** The interest method (declining balance or flat). */
    val interestType: InterestType? = null,
    /** How interest is calculated over a period (daily or same as the repayment period). */
    val interestCalculationPeriodType: InterestCalculationPeriodType? = null,
    /** The server id of the repayment strategy (the order principal, interest, fees and penalties are paid in). */
    val transactionProcessingStrategyId: Int? = null,
    /** The name of the repayment strategy. */
    val transactionProcessingStrategyName: String? = null,
    /** True when the disbursement date follows the meeting calendar. */
    val syncDisbursementWithMeeting: Boolean? = null,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: LoanTimeline? = null,
    /** The account's running totals. */
    val summary: LoanAccountSummary? = null,
    /** The fees charged at disbursement. */
    val feeChargesAtDisbursementCharged: Double? = null,
    /** The client's loan cycle: how many loans they have had. */
    val loanCounter: Int? = null,
    /** How many loans of this product the client has had. */
    val loanProductCounter: Int? = null,
    /** True when the loan can be disbursed in several tranches. */
    val multiDisburseLoan: Boolean? = null,
    /** True when the loan can be disbursed now. */
    val canDisburse: Boolean? = null,
    /** True when an installment is overdue. */
    val inArrears: Boolean? = null,
    /** True when the loan is a non-performing asset. */
    val isNPA: Boolean? = null,
)
