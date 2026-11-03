/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.client.entity.ClientDateEntity
import kpt.core.database.client.entity.ClientGenderEntity
import kpt.core.database.client.entity.ClientStatusEntity
import kpt.core.database.client.entity.ClientTypeEntity
import kpt.core.database.loan.entity.LoanAccountEntity
import kpt.core.model.objects.account.loan.Currency

/** Room column converters for client fields. */
@DbConverters
class ClientTypeConverters {
    /** Stores a `Currency?` in a JSON column. */
    @ColumnTypeConverter
    fun fromCurrency(type: Currency?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `Currency?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toCurrency(json: String?): Currency? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `LoanAccountEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromLoanAccountEntity(type: LoanAccountEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `LoanAccountEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toLoanAccountEntity(json: String?): LoanAccountEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `ClientDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromClientDate(date: ClientDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    /** Reads a `ClientDateEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toClientDate(json: String?): ClientDateEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `ClientStatusEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromClientStatus(status: ClientStatusEntity?): String? {
        return status?.let { Json.encodeToString(it) }
    }

    /** Reads a `ClientStatusEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toClientStatus(json: String?): ClientStatusEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `ClientGenderEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromClientGenderEntity(gender: ClientGenderEntity?): String? {
        return gender?.let { Json.encodeToString(it) }
    }

    /** Reads a `ClientGenderEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toClientGenderEntity(json: String?): ClientGenderEntity? {
        return json?.let { Json.decodeFromString<ClientGenderEntity>(it) }
    }

    /** Stores a `ClientTypeEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromClientTypeEntity(clientType: ClientTypeEntity?): String? {
        return clientType?.let { Json.encodeToString(it) }
    }

    /** Reads a `ClientTypeEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toClientTypeEntity(json: String?): ClientTypeEntity? {
        return json?.let { Json.decodeFromString<ClientTypeEntity>(it) }
    }
}
