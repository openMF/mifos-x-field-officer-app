/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.datatable.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.datatable.entity.ColumnHeader
import kpt.core.database.datatable.entity.ColumnValue
import kpt.core.database.datatable.entity.DataTableEntity
import kpt.core.database.datatable.entity.DataTablePayload

@DbConverters
object DataTableTypeConverters {

    @ColumnTypeConverter
    fun fromDataTable(date: DataTableEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toDataTable(json: String?): DataTableEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromListDataTable(date: List<DataTableEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListDataTable(json: String): List<DataTableEntity> {
        return json.let { Json.decodeFromString(it) } ?: emptyList()
    }

    @ColumnTypeConverter
    fun fromListColumnValue(date: List<ColumnValue>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListColumnValue(json: String?): List<ColumnValue> {
        return json?.let { Json.decodeFromString(it) } ?: emptyList()
    }

    @ColumnTypeConverter
    fun fromListColumnHeader(date: List<ColumnHeader>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListColumnHeader(json: String?): List<ColumnHeader> {
        return json?.let { Json.decodeFromString(it) } ?: emptyList()
    }

    @ColumnTypeConverter
    fun fromListDataTablePayload(date: List<DataTablePayload>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListDataTablePayload(json: String?): List<DataTablePayload> {
        return json?.let { Json.decodeFromString(it) } ?: emptyList()
    }
}
