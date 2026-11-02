/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.mapping

import kpt.core.model.datatable.ColumnHeader
import kpt.core.model.datatable.ColumnValue
import kpt.core.model.datatable.DataTable
import kpt.core.model.datatable.DataTablePayload
import kpt.core.database.datatable.entity.ColumnHeader as ColumnHeaderRow
import kpt.core.database.datatable.entity.ColumnValue as ColumnValueRow
import kpt.core.database.datatable.entity.DataTableEntity as DataTableRow
import kpt.core.database.datatable.entity.DataTablePayload as DataTablePayloadRow

fun ColumnHeaderRow.toModel(): ColumnHeader = ColumnHeader(
    id = id,
    columnCode = columnCode,
    columnDisplayType = columnDisplayType,
    columnLength = columnLength,
    dataTableColumnName = dataTableColumnName,
    columnType = columnType,
    columnNullable = columnNullable,
    columnPrimaryKey = columnPrimaryKey,
    registeredTableName = registeredTableName,
    columnValues = columnValues.map { it.toModel() },
)

fun ColumnHeader.toEntity(): ColumnHeaderRow = ColumnHeaderRow(
    id = id,
    columnCode = columnCode,
    columnDisplayType = columnDisplayType,
    columnLength = columnLength,
    dataTableColumnName = dataTableColumnName,
    columnType = columnType,
    columnNullable = columnNullable,
    columnPrimaryKey = columnPrimaryKey,
    registeredTableName = registeredTableName,
    columnValues = columnValues.map { it.toEntity() },
)

fun ColumnValueRow.toModel(): ColumnValue = ColumnValue(
    id = id,
    value = value,
    score = score,
    registeredTableName = registeredTableName,
)

fun ColumnValue.toEntity(): ColumnValueRow = ColumnValueRow(
    id = id,
    value = value,
    score = score,
    registeredTableName = registeredTableName,
)

fun DataTableRow.toModel(): DataTable = DataTable(
    id = id,
    applicationTableName = applicationTableName,
    columnHeaderData = columnHeaderData.map { it.toModel() },
    registeredTableName = registeredTableName,
)

fun DataTable.toEntity(): DataTableRow = DataTableRow(
    id = id,
    applicationTableName = applicationTableName,
    columnHeaderData = columnHeaderData.map { it.toEntity() },
    registeredTableName = registeredTableName,
)

fun DataTablePayloadRow.toModel(): DataTablePayload = DataTablePayload(
    id = id,
    clientCreationTime = clientCreationTime,
    dataTableString = dataTableString,
    registeredTableName = registeredTableName,
    data = data,
)

fun DataTablePayload.toEntity(): DataTablePayloadRow = DataTablePayloadRow(
    id = id,
    clientCreationTime = clientCreationTime,
    dataTableString = dataTableString,
    registeredTableName = registeredTableName,
    data = data,
)
