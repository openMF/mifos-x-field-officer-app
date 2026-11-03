/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.center.entity.CenterEntity
import kpt.core.database.payment.entity.PaymentTypeOptionEntity
import kpt.core.database.savings.entity.Charge
import kpt.core.database.savings.entity.SavingsAccountTransactionEntity
import kpt.core.model.loan.LoanType
import kpt.core.model.objects.clients.Address

/** Room column converters for list and map fields. */
@DbConverters
class ListTypeConverters {

    /** Reads a `ArrayList<Int?>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun fromIntList(value: String): ArrayList<Int?> {
        return Json.decodeFromString(value)
    }

    /** Stores a `ArrayList<Int?>` in a JSON column. */
    @ColumnTypeConverter
    fun toIntList(list: ArrayList<Int?>): String {
        return Json.encodeToString(list)
    }

    /** Stores a `List<CenterEntity?>` in a JSON column. */
    @ColumnTypeConverter
    fun fromCenterList(centers: List<CenterEntity?>): String {
        return centers.let { Json.encodeToString(it) }
    }

    /** Reads a `List<CenterEntity?>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toCenterList(json: String): List<CenterEntity?> {
        return json.let { Json.decodeFromString(it) }
    }

    /** Reads a `List<Int?>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toListOfInts(json: String?): List<Int?>? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<SavingsAccountTransactionEntity>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromListOfTransactions(list: List<SavingsAccountTransactionEntity>?): String? {
        return list?.let { Json.encodeToString(it) }
    }

    /** Reads a `List<SavingsAccountTransactionEntity>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toListOfTransactions(json: String?): List<SavingsAccountTransactionEntity>? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<Charge?>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromListOfCharges(list: List<Charge?>?): String? {
        return list?.let { Json.encodeToString(it) }
    }

    /** Reads a `List<Charge?>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toListOfCharges(json: String?): List<Charge?>? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<Int>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromGroupActivationDateListInt(date: List<Int>?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    /** Reads a `List<Int>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toGroupActivationDateListInt(json: String?): List<Int>? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `Map<String, Any>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromMap(map: Map<String, Any>?): String? {
        return map?.let { Json.encodeToString(it) }
    }

    /** Reads a `Map<String, Any>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toMap(json: String?): Map<String, Any>? {
        return json?.let {
            Json.decodeFromString<JsonObject>(it)
                .mapValues { entry -> entry.value }
        }
    }

    /** Stores a `LoanType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromType(type: LoanType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `LoanType?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toType(json: String?): LoanType? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<PaymentTypeOptionEntity>` in a JSON column. */
    @ColumnTypeConverter
    fun fromListPaymentTypeOptions(type: List<PaymentTypeOptionEntity>): String {
        return type.let { Json.encodeToString(it) }
    }

    /** Reads a `List<PaymentTypeOptionEntity>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toListPaymentTypeOptions(json: String): List<PaymentTypeOptionEntity> {
        return json.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<Address>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromAddressList(addressList: List<Address>?): String? =
        addressList?.let { Json.encodeToString(it) }

    /** Reads a `List<Address>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toAddressList(json: String?): List<Address>? =
        json?.let { Json.decodeFromString(it) }
}
