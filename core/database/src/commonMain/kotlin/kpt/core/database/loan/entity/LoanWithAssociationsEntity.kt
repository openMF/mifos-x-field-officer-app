/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.savings.entity.SavingAccountCurrencyEntity
import kpt.core.model.objects.account.loan.AmortizationType
import kpt.core.model.objects.account.loan.InterestCalculationPeriodType
import kpt.core.model.objects.account.loan.InterestRateFrequencyType
import kpt.core.model.objects.account.loan.InterestType
import kpt.core.model.objects.account.loan.RepaymentFrequencyType
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.TermPeriodFrequencyType
import kpt.core.model.objects.account.loan.Transaction

// @ColumnTypeConverters(
//    AmortizationTypeConverter::class,
//    CurrencyTypeConverter::class,
//    InterestCalculationPeriodTypeConverter::class,
//    InterestRateFrequencyTypeConverter::class,
//    InterestTypeConverter::class,
//    LoanTypeConverter::class,
//    RepaymentFrequencyTypeConverter::class,
//    RepaymentScheduleTypeConverter::class,
//    TermPeriodFrequencyTypeConverter::class,
//    TimelineTypeConverter::class,
//    TransactionListConverter::class,
// )
/** Room row for a loan with associations. */
@DbEntity
@Entity(
    tableName = "LoanWithAssociations",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
    // NO foreign keys.
    //
    // This entity used to declare one naming LoanTimelineEntity as the PARENT of its own `id`, which inverts the
    // relationship: it made every insert conditional on a loan's own timeline ALREADY existing as a row. Since that
    // detail is an embedded FIELD on this class, no such row is ever written first, so SQLite refused
    // every insert with `787 FOREIGN KEY constraint failed` — and Store5 swallowed the failure, so the
    // screen rendered an empty list rather than an error. Measured on device 2026-10-02: the clients
    // fetch returned 100 rows, the write failed, and the list showed "Nothing here yet".
)
@Serializable
data class LoanWithAssociationsEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** The account number Fineract assigned. */
    val accountNo: String = "",

    /** The record's Fineract status. */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val status: LoanStatusEntity = LoanStatusEntity(),

    /** The server id of the client. */
    val clientId: Int = 0,

    /** The client's display name. */
    val clientName: String = "",

    /** The server id of the client's office. */
    val clientOfficeId: Int = 0,

    /** The server id of the loan product. */
    val loanProductId: Int = 0,

    /** The name of the loan product. */
    val loanProductName: String = "",

    /** The description of the loan product. */
    val loanProductDescription: String = "",

    /** The server id of the fund the loan is financed from. */
    val fundId: Int = 0,

    /** The name of the fund the loan is financed from. */
    val fundName: String = "",

    /** The server id of the loan purpose. */
    val loanPurposeId: Int = 0,

    /** The name of the loan purpose. */
    val loanPurposeName: String = "",

    /** The server id of the loan officer. */
    val loanOfficerId: Int = 0,

    /** The loan officer's display name. */
    val loanOfficerName: String = "",

    /** The loan type (individual, group or JLG). */
    val loanType: LoanTypeEntity = LoanTypeEntity(),
    /** The currency the amounts are in. */
    val currency: SavingAccountCurrencyEntity = SavingAccountCurrencyEntity(),

    /** The loan principal. */
    val principal: Double = 0.0,

    /** The principal that was approved. */
    val approvedPrincipal: Double = 0.0,

    /** The principal applied for. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0.0",
    )
    val proposedPrincipal: Double = 0.0,

    /** The loan term, in [termPeriodFrequencyType] units. */
    val termFrequency: Int = 0,
    /** The unit of the loan term. */
    val termPeriodFrequencyType: TermPeriodFrequencyType = TermPeriodFrequencyType(),

    /** The number of installments. */
    val numberOfRepayments: Int = 0,

    /** How many repayment-frequency units fall between two installments. */
    val repaymentEvery: Int = 0,
    /** The unit of the repayment frequency (days, weeks, months). */
    val repaymentFrequencyType: RepaymentFrequencyType = RepaymentFrequencyType(),

    /** The interest rate per interest period, in percent. */
    val interestRatePerPeriod: Double = 0.0,
    /** The unit [interestRatePerPeriod] is per (month or year). */
    val interestRateFrequencyType: InterestRateFrequencyType = InterestRateFrequencyType(),

    /** The nominal annual interest rate, in percent. */
    val annualInterestRate: Double = 0.0,
    /** How the principal is amortised (equal installments or equal principal). */
    val amortizationType: AmortizationType = AmortizationType(),
    /** The interest method (declining balance or flat). */
    val interestType: InterestType = InterestType(),
    /** How interest is calculated over a period (daily or same as the repayment period). */
    val interestCalculationPeriodType: InterestCalculationPeriodType = InterestCalculationPeriodType(),

    /** The server id of the repayment strategy (the order principal, interest, fees and penalties are paid in). */
    val transactionProcessingStrategyId: Int = 0,

    /** The name of the repayment strategy. */
    val transactionProcessingStrategyName: String = "",

    /** True when the disbursement date follows the meeting calendar. */
    val syncDisbursementWithMeeting: Boolean = false,

    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val timeline: LoanTimelineEntity = LoanTimelineEntity(),

    /** The account's running totals. */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val summary: LoanAccountSummaryEntity = LoanAccountSummaryEntity(),
    /** The repayment schedule. */
    val repaymentSchedule: RepaymentSchedule = RepaymentSchedule(),
    /** The transactions. */
    val transactions: List<Transaction> = emptyList(),

    /** The fees charged at disbursement. */
    val feeChargesAtDisbursementCharged: Double = 0.0,

    /** The total overpaid. */
    val totalOverpaid: Double = 0.0,

    /** The client's loan cycle: how many loans they have had. */
    val loanCounter: Int = 0,

    /** How many loans of this product the client has had. */
    val loanProductCounter: Int = 0,

    /** True when the loan can be disbursed in several tranches. */
    val multiDisburseLoan: Boolean = false,

    /** True when the loan can be disbursed now. */
    val canDisburse: Boolean = false,

    /** True when an installment is overdue. */
    val inArrears: Boolean = false,

    /** True when the loan is a non-performing asset. */
    val isNPA: Boolean = false,

    /** The date the loan was overpaid. */
    val overpaidOnDate: List<Int>? = null,
    /** True when the loan amortises in equal installments. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val isEqualAmortization: Boolean = false,

    /** True when interest is calculated for a partial first period. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val allowPartialPeriodInterestCalculation: Boolean = false,

    /** True when interest is recognised from the disbursement date. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val interestRecognitionOnDisbursementDate: Boolean = false,

    /** True when the loan takes a down payment. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val enableDownPayment: Boolean = false,

    /** True when income can be capitalised into the principal. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val enableIncomeCapitalization: Boolean = false,

    /** True when the loan supports a buy-down fee. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val enableBuyDownFee: Boolean = false,

    /** True when delinquency is tracked per installment. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val enableInstallmentLevelDelinquency: Boolean = false,

    /** True when interest is recalculated as repayments arrive. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val isInterestRecalculationEnabled: Boolean = false,

    /** True when the loan has been charged off. */
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val chargedOff: Boolean = false,
)
