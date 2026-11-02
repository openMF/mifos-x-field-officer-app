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

@DbConverters
class ClientOptionsTypeConverters {
    @ColumnTypeConverter
    fun fromClientClassificationEntity(clientClassification: ClientClassificationEntity?): String? {
        return clientClassification?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toClientClassificationEntity(json: String?): ClientClassificationEntity? {
        return json?.let { Json.decodeFromString<ClientClassificationEntity>(it) }
    }

    @ColumnTypeConverter
    fun fromListOfficeOptions(date: List<OfficeOptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListOfficeOptions(json: String): List<OfficeOptionsEntity> {
        return json.let { Json.decodeFromString(it) } ?: emptyList()
    }

    @ColumnTypeConverter
    fun fromListStaffOptions(date: List<StaffOptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListStaffOptions(json: String): List<StaffOptionsEntity> {
        return json.let { Json.decodeFromString(it) } ?: emptyList()
    }

    @ColumnTypeConverter
    fun fromListOptions(date: List<OptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListOptions(json: String): List<OptionsEntity> {
        return json.let { Json.decodeFromString(it) } ?: emptyList()
    }

    @ColumnTypeConverter
    fun fromListInterestType(date: List<InterestTypeEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListInterestType(json: String): List<InterestTypeEntity> {
        return json.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromListSavingProductOptions(date: List<SavingProductOptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListSavingProductOptions(json: String): List<SavingProductOptionsEntity> {
        return json.let { Json.decodeFromString(it) }
    }
}
