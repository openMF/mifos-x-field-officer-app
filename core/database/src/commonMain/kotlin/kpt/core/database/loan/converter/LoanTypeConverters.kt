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
    /** Stores a `LoanStatusEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromStatus(status: LoanStatusEntity?): String? {
        return status?.let { Json.encodeToString(it) }
    }

    /** Reads a `LoanStatusEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toStatus(json: String?): LoanStatusEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `LoanTypeEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromLoanType(type: LoanTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `LoanTypeEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toLoanType(json: String?): LoanTypeEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `SavingAccountCurrencyEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromCurrency(currency: SavingAccountCurrencyEntity?): String? {
        return currency?.let { Json.encodeToString(it) }
    }

    /** Reads a `SavingAccountCurrencyEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toCurrency(json: String?): SavingAccountCurrencyEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `TermPeriodFrequencyType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromTermPeriodFrequencyType(type: TermPeriodFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `TermPeriodFrequencyType?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toTermPeriodFrequencyType(json: String?): TermPeriodFrequencyType? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `RepaymentFrequencyType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromRepaymentFrequencyType(type: RepaymentFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `RepaymentFrequencyType?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toRepaymentFrequencyType(json: String?): RepaymentFrequencyType? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `InterestRateFrequencyType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromInterestRateFrequencyType(type: InterestRateFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `InterestRateFrequencyType?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toInterestRateFrequencyType(json: String?): InterestRateFrequencyType? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `LoanAccountSummaryEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromSummary(summary: LoanAccountSummaryEntity?): String? {
        return summary?.let { Json.encodeToString(it) }
    }
}
