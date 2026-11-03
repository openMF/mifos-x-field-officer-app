/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.loan.entity.LoanTimelineEntity
import kpt.core.model.objects.account.saving.InterestCalculationDaysInYearType
import kpt.core.model.objects.account.saving.InterestCalculationType
import kpt.core.model.objects.account.saving.InterestCompoundingPeriodType
import kpt.core.model.objects.account.saving.InterestPostingPeriodType
import kpt.core.model.objects.account.saving.LockinPeriodFrequencyType

/** Room row for a savings account with associations. */
@DbEntity
@Entity(
    tableName = "SavingsAccountWithAssociations",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
    foreignKeys = [],
)
@Serializable
data class SavingsAccountWithAssociationsEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    /** The account number Fineract assigned. */
    val accountNo: String? = null,

    /** The server id of the client. */
    val clientId: Int? = null,

    /** The client's display name. */
    val clientName: String? = null,

    /** The server id of the savings product. */
    val savingsProductId: Int? = null,

    /** The name of the savings product. */
    val savingsProductName: String? = null,

    /** The server id of the field officer. */
    val fieldOfficerId: Int? = null,

    /** The record's Fineract status. */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val status: SavingsAccountStatusEntity? = null,

    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: LoanTimelineEntity? = null,

    /** The currency the amounts are in. */
    val currency: SavingAccountCurrencyEntity? = null,

    /** The nominal annual interest rate, in percent. */
    val nominalAnnualInterestRate: Double? = null,

    /** How often interest is compounded. */
    val interestCompoundingPeriodType: InterestCompoundingPeriodType? = null,

    /** How often interest is posted to the account. */
    val interestPostingPeriodType: InterestPostingPeriodType? = null,

    /** How interest is calculated on the balance (daily or average daily balance). */
    val interestCalculationType: InterestCalculationType? = null,

    /** How many days a year has for interest calculation (360, 365, actual). */
    val interestCalculationDaysInYearType: InterestCalculationDaysInYearType? = null,

    /** The minimum balance required to open the account. */
    val minRequiredOpeningBalance: Double? = null,

    /** The length of the lock-in period, in [lockinPeriodFrequencyType] units. */
    val lockinPeriodFrequency: Int? = null,

    /** The unit of the lock-in period. */
    val lockinPeriodFrequencyType: LockinPeriodFrequencyType? = null,

    /** True when the withdrawal fee also applies to transfers. */
    val withdrawalFeeForTransfers: Boolean? = null,

    /** True when the account may be overdrawn. */
    val allowOverdraft: Boolean? = null,

    /** True when the minimum balance is enforced. */
    val enforceMinRequiredBalance: Boolean? = null,

    /** True when tax is withheld on interest. */
    val withHoldTax: Boolean? = null,

    /** The date of the last active transaction. */
    val lastActiveTransactionDate: List<Int?> = emptyList(),

    /** True when the account is tracked for dormancy. */
    val dormancyTrackingActive: Boolean? = null,

    /** The overdraft limit. */
    val overdraftLimit: Int? = null,

    /** The account's running totals. */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val summary: SavingsAccountSummaryEntity? = null,

    /** The transactions. */
    val transactions: List<SavingsAccountTransactionEntity> = emptyList(),

    /** The charges. */
    val charges: List<Charge?> = emptyList(),
)
