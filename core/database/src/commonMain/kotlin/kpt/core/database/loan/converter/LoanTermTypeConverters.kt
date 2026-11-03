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

/** Room column converters for loan term fields. */
@DbConverters
class LoanTermTypeConverters {
    /** Reads a `LoanAccountSummaryEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toSummary(json: String?): LoanAccountSummaryEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `AmortizationType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromAmortizationType(type: AmortizationType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `AmortizationType?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toAmortizationType(json: String?): AmortizationType? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `InterestType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromInterestType(type: InterestType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `InterestType?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toInterestType(json: String?): InterestType? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `InterestCalculationPeriodType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromInterestCalculationPeriodType(type: InterestCalculationPeriodType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `InterestCalculationPeriodType?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toInterestCalculationPeriodType(json: String?): InterestCalculationPeriodType? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `LoanTimelineEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromTimeline(timeline: LoanTimelineEntity?): String? {
        return timeline?.let { Json.encodeToString(it) }
    }

    /** Reads a `LoanTimelineEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toTimeline(json: String?): LoanTimelineEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `RepaymentSchedule?` in a JSON column. */
    @ColumnTypeConverter
    fun fromRepaymentSchedule(schedule: RepaymentSchedule?): String? {
        return schedule?.let { Json.encodeToString(it) }
    }

    /** Reads a `RepaymentSchedule?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toRepaymentSchedule(json: String?): RepaymentSchedule? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<Transaction>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromTransactionList(transactions: List<Transaction>?): String? {
        return transactions?.let { Json.encodeToString(it) }
    }

    /** Reads a `List<Transaction>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toTransactionList(json: String?): List<Transaction>? {
        return json?.let { Json.decodeFromString(it) }
    }
}
