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
import kpt.core.database.client.entity.ClientClassificationEntity
import kpt.core.database.client.entity.InterestTypeEntity
import kpt.core.database.client.entity.OfficeOptionsEntity
import kpt.core.database.client.entity.OptionsEntity
import kpt.core.database.client.entity.SavingProductOptionsEntity
import kpt.core.database.client.entity.StaffOptionsEntity

/** Room column converters for client options fields. */
@DbConverters
class ClientOptionsTypeConverters {
    /** Stores a `ClientClassificationEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromClientClassificationEntity(clientClassification: ClientClassificationEntity?): String? {
        return clientClassification?.let { Json.encodeToString(it) }
    }

    /** Reads a `ClientClassificationEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toClientClassificationEntity(json: String?): ClientClassificationEntity? {
        return json?.let { Json.decodeFromString<ClientClassificationEntity>(it) }
    }

    /** Stores a `List<OfficeOptionsEntity>` in a JSON column. */
    @ColumnTypeConverter
    fun fromListOfficeOptions(date: List<OfficeOptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    /** Reads a `List<OfficeOptionsEntity>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toListOfficeOptions(json: String): List<OfficeOptionsEntity> {
        return json.let { Json.decodeFromString(it) } ?: emptyList()
    }

    /** Stores a `List<StaffOptionsEntity>` in a JSON column. */
    @ColumnTypeConverter
    fun fromListStaffOptions(date: List<StaffOptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    /** Reads a `List<StaffOptionsEntity>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toListStaffOptions(json: String): List<StaffOptionsEntity> {
        return json.let { Json.decodeFromString(it) } ?: emptyList()
    }

    /** Stores a `List<OptionsEntity>` in a JSON column. */
    @ColumnTypeConverter
    fun fromListOptions(date: List<OptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    /** Reads a `List<OptionsEntity>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toListOptions(json: String): List<OptionsEntity> {
        return json.let { Json.decodeFromString(it) } ?: emptyList()
    }

    /** Stores a `List<InterestTypeEntity>` in a JSON column. */
    @ColumnTypeConverter
    fun fromListInterestType(date: List<InterestTypeEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    /** Reads a `List<InterestTypeEntity>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toListInterestType(json: String): List<InterestTypeEntity> {
        return json.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<SavingProductOptionsEntity>` in a JSON column. */
    @ColumnTypeConverter
    fun fromListSavingProductOptions(date: List<SavingProductOptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    /** Reads a `List<SavingProductOptionsEntity>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toListSavingProductOptions(json: String): List<SavingProductOptionsEntity> {
        return json.let { Json.decodeFromString(it) }
    }
}
