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
import kpt.core.database.loan.entity.ActualDisbursementDateEntity
import kpt.core.database.payment.entity.PaymentTypeOptionEntity
import kpt.core.model.objects.Changes
import kpt.core.model.objects.account.loan.Period
import kpt.core.model.objects.account.loan.Type

/** Room column converters for loan schedule fields. */
@DbConverters
class LoanScheduleTypeConverters {
    /** Stores a `Type?` in a JSON column. */
    @ColumnTypeConverter
    fun fromType(type: Type?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `Type?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toType(json: String?): Type? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<Int?>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromListInt(date: List<Int?>?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    /** Stores a `ActualDisbursementDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromActualDisbursementDate(date: ActualDisbursementDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    /** Reads a `ActualDisbursementDateEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toActualDisbursementDate(json: String?): ActualDisbursementDateEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `MutableList<Int>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromMutableListInt(date: MutableList<Int>?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    /** Reads a `MutableList<Int>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toMutableListInt(json: String?): MutableList<Int>? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `MutableList<PaymentTypeOptionEntity>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromMutableListPaymentTypeOptions(type: MutableList<PaymentTypeOptionEntity>?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `MutableList<PaymentTypeOptionEntity>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toMutableListPaymentTypeOptions(json: String?): MutableList<PaymentTypeOptionEntity>? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Reads a `List<Period>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun fromPeriodList(json: String?): List<Period>? {
        return json?.let { Json.decodeFromString<List<Period>>(it) }
    }

    /** Stores a `List<Period>?` in a JSON column. */
    @ColumnTypeConverter
    fun toPeriodList(periodList: List<Period>?): String? {
        return periodList?.let { Json.encodeToString(it) }
    }

    /** Stores a `Changes?` in a JSON column. */
    @ColumnTypeConverter
    fun toChanges(changes: Changes?): String {
        return Json.encodeToString(changes)
    }

    /** Reads a `Changes?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun fromChanges(changes: String?): Changes? {
        return changes?.let { Json.decodeFromString(changes) }
    }
}
