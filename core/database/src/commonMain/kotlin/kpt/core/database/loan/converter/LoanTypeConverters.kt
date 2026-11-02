/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.loan.entity.LoanAccountSummaryEntity
import kpt.core.database.loan.entity.LoanStatusEntity
import kpt.core.database.loan.entity.LoanTypeEntity
import kpt.core.database.savings.entity.SavingAccountCurrencyEntity
import kpt.core.model.objects.account.loan.InterestRateFrequencyType
import kpt.core.model.objects.account.loan.RepaymentFrequencyType
import kpt.core.model.objects.account.loan.TermPeriodFrequencyType

/**
 * Created by Pronay Sarker on 24/01/2025 (3:07 PM)
 */
// TODO remove unused converters
@DbConverters
class LoanTypeConverters {
    @ColumnTypeConverter
    fun fromStatus(status: LoanStatusEntity?): String? {
        return status?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toStatus(json: String?): LoanStatusEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromLoanType(type: LoanTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toLoanType(json: String?): LoanTypeEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromCurrency(currency: SavingAccountCurrencyEntity?): String? {
        return currency?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toCurrency(json: String?): SavingAccountCurrencyEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromTermPeriodFrequencyType(type: TermPeriodFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toTermPeriodFrequencyType(json: String?): TermPeriodFrequencyType? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromRepaymentFrequencyType(type: RepaymentFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toRepaymentFrequencyType(json: String?): RepaymentFrequencyType? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromInterestRateFrequencyType(type: InterestRateFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toInterestRateFrequencyType(json: String?): InterestRateFrequencyType? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromSummary(summary: LoanAccountSummaryEntity?): String? {
        return summary?.let { Json.encodeToString(it) }
    }
}
