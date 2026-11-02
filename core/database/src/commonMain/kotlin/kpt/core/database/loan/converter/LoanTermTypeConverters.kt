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
import kpt.core.database.loan.entity.LoanTimelineEntity
import kpt.core.model.objects.account.loan.AmortizationType
import kpt.core.model.objects.account.loan.InterestCalculationPeriodType
import kpt.core.model.objects.account.loan.InterestType
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.Transaction

@DbConverters
class LoanTermTypeConverters {
    @ColumnTypeConverter
    fun toSummary(json: String?): LoanAccountSummaryEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromAmortizationType(type: AmortizationType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toAmortizationType(json: String?): AmortizationType? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromInterestType(type: InterestType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toInterestType(json: String?): InterestType? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromInterestCalculationPeriodType(type: InterestCalculationPeriodType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toInterestCalculationPeriodType(json: String?): InterestCalculationPeriodType? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromTimeline(timeline: LoanTimelineEntity?): String? {
        return timeline?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toTimeline(json: String?): LoanTimelineEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromRepaymentSchedule(schedule: RepaymentSchedule?): String? {
        return schedule?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toRepaymentSchedule(json: String?): RepaymentSchedule? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromTransactionList(transactions: List<Transaction>?): String? {
        return transactions?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toTransactionList(json: String?): List<Transaction>? {
        return json?.let { Json.decodeFromString(it) }
    }
}
