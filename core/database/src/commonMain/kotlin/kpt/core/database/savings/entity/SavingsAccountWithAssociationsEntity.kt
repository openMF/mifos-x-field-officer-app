/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    val accountNo: String? = null,

    val clientId: Int? = null,

    val clientName: String? = null,

    val savingsProductId: Int? = null,

    val savingsProductName: String? = null,

    val fieldOfficerId: Int? = null,

    @ColumnInfo(index = true, name = ColumnInfo.INHERIT_FIELD_NAME, typeAffinity = ColumnInfo.UNDEFINED, collate = ColumnInfo.UNSPECIFIED, defaultValue = ColumnInfo.VALUE_UNSPECIFIED)
    val status: SavingsAccountStatusEntity? = null,

    val timeline: LoanTimelineEntity? = null,

    val currency: SavingAccountCurrencyEntity? = null,

    val nominalAnnualInterestRate: Double? = null,

    val interestCompoundingPeriodType: InterestCompoundingPeriodType? = null,

    val interestPostingPeriodType: InterestPostingPeriodType? = null,

    val interestCalculationType: InterestCalculationType? = null,

    val interestCalculationDaysInYearType: InterestCalculationDaysInYearType? = null,

    val minRequiredOpeningBalance: Double? = null,

    val lockinPeriodFrequency: Int? = null,

    val lockinPeriodFrequencyType: LockinPeriodFrequencyType? = null,

    val withdrawalFeeForTransfers: Boolean? = null,

    val allowOverdraft: Boolean? = null,

    val enforceMinRequiredBalance: Boolean? = null,

    val withHoldTax: Boolean? = null,

    val lastActiveTransactionDate: List<Int?> = emptyList(),

    val dormancyTrackingActive: Boolean? = null,

    val overdraftLimit: Int? = null,

    @ColumnInfo(index = true, name = ColumnInfo.INHERIT_FIELD_NAME, typeAffinity = ColumnInfo.UNDEFINED, collate = ColumnInfo.UNSPECIFIED, defaultValue = ColumnInfo.VALUE_UNSPECIFIED)
    val summary: SavingsAccountSummaryEntity? = null,

    val transactions: List<SavingsAccountTransactionEntity> = emptyList(),

    val charges: List<Charge?> = emptyList(),
)
