/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val accountNo: String = "",

    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val status: LoanStatusEntity = LoanStatusEntity(),

    val clientId: Int = 0,

    val clientName: String = "",

    val clientOfficeId: Int = 0,

    val loanProductId: Int = 0,

    val loanProductName: String = "",

    val loanProductDescription: String = "",

    val fundId: Int = 0,

    val fundName: String = "",

    val loanPurposeId: Int = 0,

    val loanPurposeName: String = "",

    val loanOfficerId: Int = 0,

    val loanOfficerName: String = "",

    val loanType: LoanTypeEntity = LoanTypeEntity(),
    val currency: SavingAccountCurrencyEntity = SavingAccountCurrencyEntity(),

    val principal: Double = 0.0,

    val approvedPrincipal: Double = 0.0,

    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0.0",
    )
    val proposedPrincipal: Double = 0.0,

    val termFrequency: Int = 0,
    val termPeriodFrequencyType: TermPeriodFrequencyType = TermPeriodFrequencyType(),

    val numberOfRepayments: Int = 0,

    val repaymentEvery: Int = 0,
    val repaymentFrequencyType: RepaymentFrequencyType = RepaymentFrequencyType(),

    val interestRatePerPeriod: Double = 0.0,
    val interestRateFrequencyType: InterestRateFrequencyType = InterestRateFrequencyType(),

    val annualInterestRate: Double = 0.0,
    val amortizationType: AmortizationType = AmortizationType(),
    val interestType: InterestType = InterestType(),
    val interestCalculationPeriodType: InterestCalculationPeriodType = InterestCalculationPeriodType(),

    val transactionProcessingStrategyId: Int = 0,

    val transactionProcessingStrategyName: String = "",

    val syncDisbursementWithMeeting: Boolean = false,

    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val timeline: LoanTimelineEntity = LoanTimelineEntity(),

    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val summary: LoanAccountSummaryEntity = LoanAccountSummaryEntity(),
    val repaymentSchedule: RepaymentSchedule = RepaymentSchedule(),
    val transactions: List<Transaction> = emptyList(),

    val feeChargesAtDisbursementCharged: Double = 0.0,

    val totalOverpaid: Double = 0.0,

    val loanCounter: Int = 0,

    val loanProductCounter: Int = 0,

    val multiDisburseLoan: Boolean = false,

    val canDisburse: Boolean = false,

    val inArrears: Boolean = false,

    val isNPA: Boolean = false,

    val overpaidOnDate: List<Int>? = null,
    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val isEqualAmortization: Boolean = false,

    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val allowPartialPeriodInterestCalculation: Boolean = false,

    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val interestRecognitionOnDisbursementDate: Boolean = false,

    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val enableDownPayment: Boolean = false,

    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val enableIncomeCapitalization: Boolean = false,

    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val enableBuyDownFee: Boolean = false,

    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val enableInstallmentLevelDelinquency: Boolean = false,

    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val isInterestRecalculationEnabled: Boolean = false,

    @ColumnInfo(
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        index = false,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = "0",
    )
    val chargedOff: Boolean = false,
)
