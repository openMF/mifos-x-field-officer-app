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

/** Maps the stored row to its `ColumnHeader` model. */
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

/** Maps the model to its stored `ColumnHeaderRow`. */
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

/** Maps the stored row to its `ColumnValue` model. */
fun ColumnValueRow.toModel(): ColumnValue = ColumnValue(
    id = id,
    value = value,
    score = score,
    registeredTableName = registeredTableName,
)

/** Maps the model to its stored `ColumnValueRow`. */
fun ColumnValue.toEntity(): ColumnValueRow = ColumnValueRow(
    id = id,
    value = value,
    score = score,
    registeredTableName = registeredTableName,
)

/** Maps the stored row to its `DataTable` model. */
fun DataTableRow.toModel(): DataTable = DataTable(
    id = id,
    applicationTableName = applicationTableName,
    columnHeaderData = columnHeaderData.map { it.toModel() },
    registeredTableName = registeredTableName,
)

/** Maps the model to its stored `DataTableRow`. */
fun DataTable.toEntity(): DataTableRow = DataTableRow(
    id = id,
    applicationTableName = applicationTableName,
    columnHeaderData = columnHeaderData.map { it.toEntity() },
    registeredTableName = registeredTableName,
)

/** Maps the stored row to its `DataTablePayload` model. */
fun DataTablePayloadRow.toModel(): DataTablePayload = DataTablePayload(
    id = id,
    clientCreationTime = clientCreationTime,
    dataTableString = dataTableString,
    registeredTableName = registeredTableName,
    data = data,
)

/** Maps the model to its stored `DataTablePayloadRow`. */
fun DataTablePayload.toEntity(): DataTablePayloadRow = DataTablePayloadRow(
    id = id,
    clientCreationTime = clientCreationTime,
    dataTableString = dataTableString,
    registeredTableName = registeredTableName,
    data = data,
)
