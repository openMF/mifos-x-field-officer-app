/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.savings.entity.SavingsTransactionDateEntity
import kpt.core.database.savings.entity.SavingsTransactionTypeEntity
import kpt.core.model.objects.account.saving.InterestCalculationDaysInYearType
import kpt.core.model.objects.account.saving.InterestCalculationType
import kpt.core.model.objects.account.saving.InterestCompoundingPeriodType

// todo add missing converters
@DbConverters
class SavingsTypeConverters {
    @ColumnTypeConverter
    fun fromTransactionType(type: SavingsTransactionTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toTransactionType(json: String?): SavingsTransactionTypeEntity? {
        return json?.let { Json.decodeFromString<SavingsTransactionTypeEntity>(it) }
    }

    @ColumnTypeConverter
    fun fromSavingsTransactionDate(date: SavingsTransactionDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toSavingsTransactionDate(json: String?): SavingsTransactionDateEntity? {
        return json?.let { Json.decodeFromString<SavingsTransactionDateEntity>(it) }
    }

    @ColumnTypeConverter
    fun fromInterestCalculationDaysInYearType(
        type: InterestCalculationDaysInYearType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toInterestCalculationDaysInYearType(json: String?): InterestCalculationDaysInYearType? {
        return json?.let { Json.decodeFromString<InterestCalculationDaysInYearType>(it) }
    }

    @ColumnTypeConverter
    fun fromInterestCalculationType(type: InterestCalculationType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toInterestCalculationType(json: String?): InterestCalculationType? {
        return json?.let { Json.decodeFromString<InterestCalculationType>(it) }
    }

    @ColumnTypeConverter
    fun fromInterestCompoundingPeriodType(
        type: InterestCompoundingPeriodType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toInterestCompoundingPeriodType(
        json: String?,
    ): InterestCompoundingPeriodType? {
        return json?.let { Json.decodeFromString<InterestCompoundingPeriodType>(it) }
    }
}
