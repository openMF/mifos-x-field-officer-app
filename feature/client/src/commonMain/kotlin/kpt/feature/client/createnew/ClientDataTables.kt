/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.createnew

import kpt.core.model.datatable.ColumnHeader
import kpt.core.model.datatable.DataTable
import kpt.core.model.datatable.DataTablePayload
import kpt.core.model.utils.ApiDateFormatter

/*
 * The tenant's client DATATABLES, filled in as part of create-client.
 *
 * Ported from `6b66e8a43:feature/data-table/dataTableList` (`DataTableListScreen` + `…ViewModel`), which
 * create-client opened when `clientTemplate.dataTables` was non-empty. That form never worked:
 *  - every field was hardwired to `value = ""` with `onValueChange = {}` — nothing could be typed;
 *  - the payload list started as `null`, so each `dataTablePayloadElements?.add(...)` was a no-op and
 *    the client was created with `datatables = null` whatever the form showed;
 *  - an unparseable number was sent as 0, and the date picker allowed only the future.
 * A tenant whose datatable has a NOT NULL column could therefore never create a client from the app.
 *
 * Here the fields are inline sections of the create-client form, their values live in the form's
 * state, and [dataTablePayloads] is what `ClientPayload.datatables` carries.
 */

/** Fineract's `columnDisplayType` values (the original's `BaseFormWidget.SCHEMA_KEY_*`). */
internal object ColumnType {
    const val STRING = "STRING"
    const val TEXT = "TEXT"
    const val INTEGER = "INTEGER"
    const val DECIMAL = "DECIMAL"
    const val DATE = "DATE"
    const val BOOLEAN = "BOOLEAN"
    const val CODELOOKUP = "CODELOOKUP"
    const val CODEVALUE = "CODEVALUE"
}

/** One field's address: which table, which column. */
internal data class DataTableField(val table: String, val column: String)

/** Why a datatable field was refused on submit. */
internal enum class DataTableFieldError { REQUIRED, NOT_A_NUMBER }

/** The FK column a client datatable links through — Fineract fills it from the new client itself. */
private const val CLIENT_FK_COLUMN = "client_id"

/**
 * The columns an officer fills in. Fineract fills the row's own primary key and the client foreign
 * key, so offering them would ask for a value the server overwrites (or rejects).
 */
internal fun DataTable.entryColumns(): List<ColumnHeader> = columnHeaderData.filter {
    it.columnPrimaryKey != true && !it.dataTableColumnName.equals(CLIENT_FK_COLUMN, ignoreCase = true)
}

internal val ColumnHeader.isRequired: Boolean get() = columnNullable == false

/** Only tables with something to fill in are shown — an all-key table would be an empty heading. */
internal fun List<DataTable>.withEntryColumns(): List<DataTable> =
    filter { !it.registeredTableName.isNullOrBlank() && it.entryColumns().isNotEmpty() }

/**
 * The fields that block submit. Values are the raw strings the form holds: text as typed, a date as
 * epoch millis, a boolean as `"true"`, a code lookup as the chosen code value's id.
 */
internal fun validateDataTables(
    tables: List<DataTable>,
    values: Map<DataTableField, String>,
): Map<DataTableField, DataTableFieldError> = buildMap {
    for (table in tables) {
        val name = table.registeredTableName ?: continue
        for (column in table.entryColumns()) {
            val key = DataTableField(name, column.dataTableColumnName ?: continue)
            val raw = values[key]?.trim().orEmpty()
            fieldError(column, raw)?.let { put(key, it) }
        }
    }
}

private fun fieldError(column: ColumnHeader, raw: String): DataTableFieldError? = when {
    // A BOOLEAN is never "missing" — unchecked is a value (false).
    raw.isEmpty() -> DataTableFieldError.REQUIRED.takeIf {
        column.isRequired && column.columnDisplayType != ColumnType.BOOLEAN
    }
    column.columnDisplayType == ColumnType.INTEGER && raw.toLongOrNull() == null ->
        DataTableFieldError.NOT_A_NUMBER
    column.columnDisplayType == ColumnType.DECIMAL && raw.toDoubleOrNull() == null ->
        DataTableFieldError.NOT_A_NUMBER
    else -> null
}

/**
 * One payload per table, each value in the type Fineract expects for its column. Empty optional values
 * are left out (absent, not `""` — an empty string is not a valid INTEGER or DATE). A table every one of
 * whose values is empty still sends a row: an empty optional row is a legal entry, and a table with a
 * NOT NULL column cannot get here empty (validation refuses it).
 */
internal fun dataTablePayloads(
    tables: List<DataTable>,
    values: Map<DataTableField, String>,
): List<DataTablePayload> = tables.mapNotNull { table ->
    val name = table.registeredTableName ?: return@mapNotNull null
    val data = buildMap<String, Any> {
        // A DATE column is read with the payload's own format, so it is declared with it.
        put("dateFormat", ApiDateFormatter.DATE_FORMAT)
        put("locale", ApiDateFormatter.LOCALE)
        for (column in table.entryColumns()) {
            val columnName = column.dataTableColumnName ?: continue
            val raw = values[DataTableField(name, columnName)]?.trim().orEmpty()
            val typed: Any? = when (column.columnDisplayType) {
                ColumnType.BOOLEAN -> raw == "true"
                ColumnType.INTEGER -> raw.toLongOrNull()
                ColumnType.DECIMAL -> raw.toDoubleOrNull()
                ColumnType.DATE -> raw.toLongOrNull()?.let(ApiDateFormatter::formatForApi)
                ColumnType.CODELOOKUP, ColumnType.CODEVALUE -> raw.toIntOrNull()
                else -> raw.ifEmpty { null }
            }
            if (typed != null) put(columnName, typed)
        }
    }
    DataTablePayload(registeredTableName = name, data = data)
}
