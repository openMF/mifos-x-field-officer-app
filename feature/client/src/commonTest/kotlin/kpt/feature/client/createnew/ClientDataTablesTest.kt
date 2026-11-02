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
import kpt.core.model.datatable.ColumnValue
import kpt.core.model.datatable.DataTable
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClientDataTablesTest {

    // A tenant's client datatable, shaped like Fineract's template: key + FK first, then data columns.
    private val table = DataTable(
        registeredTableName = "client_extra",
        columnHeaderData = listOf(
            ColumnHeader(
                dataTableColumnName = "client_id",
                columnDisplayType = "INTEGER",
                columnPrimaryKey = true,
                columnNullable = false,
            ),
            ColumnHeader(
                dataTableColumnName = "household_size",
                columnDisplayType = ColumnType.INTEGER,
                columnNullable = false,
            ),
            ColumnHeader(
                dataTableColumnName = "monthly_income",
                columnDisplayType = ColumnType.DECIMAL,
                columnNullable = true,
            ),
            ColumnHeader(dataTableColumnName = "joined_on", columnDisplayType = ColumnType.DATE, columnNullable = true),
            ColumnHeader(
                dataTableColumnName = "owns_home",
                columnDisplayType = ColumnType.BOOLEAN,
                columnNullable = false,
            ),
            ColumnHeader(
                dataTableColumnName = "occupation",
                columnDisplayType = ColumnType.CODELOOKUP,
                columnNullable = true,
                columnValues = listOf(ColumnValue(id = 41, value = "Farmer"), ColumnValue(id = 42, value = "Trader")),
            ),
            ColumnHeader(dataTableColumnName = "notes", columnDisplayType = ColumnType.TEXT, columnNullable = true),
        ),
    )

    private fun f(column: String) = DataTableField("client_extra", column)

    @Test
    fun theKeyAndClientColumnsAreNotAskedFor() {
        assertEquals(
            listOf("household_size", "monthly_income", "joined_on", "owns_home", "occupation", "notes"),
            table.entryColumns().map { it.dataTableColumnName },
        )
        // A table with nothing to fill in is not shown at all.
        val keysOnly = DataTable(
            registeredTableName = "keys_only",
            columnHeaderData = listOf(table.columnHeaderData.first()),
        )
        assertTrue(listOf(keysOnly).withEntryColumns().isEmpty())
    }

    @Test
    fun requiredAndNumericFieldsAreValidated() {
        val errors = validateDataTables(listOf(table), mapOf(f("monthly_income") to "12k"))
        assertEquals(DataTableFieldError.REQUIRED, errors[f("household_size")])
        assertEquals(DataTableFieldError.NOT_A_NUMBER, errors[f("monthly_income")])
        // A required BOOLEAN is never "missing": unchecked is false.
        assertFalse(f("owns_home") in errors)
        assertTrue(validateDataTables(listOf(table), mapOf(f("household_size") to "4")).isEmpty())
    }

    @Test
    fun eachValueIsSentInItsColumnsType() {
        val payload = dataTablePayloads(
            listOf(table),
            mapOf(
                f("household_size") to "4",
                f("monthly_income") to "1250.5",
                f("joined_on") to DAY.toString(),
                f("owns_home") to "true",
                f("occupation") to "42",
                f("notes") to "  ",
            ),
        ).single()

        assertEquals("client_extra", payload.registeredTableName)
        assertEquals(4L, payload.data["household_size"])
        assertEquals(1250.5, payload.data["monthly_income"])
        assertEquals(ApiDateFormatter.formatForApi(DAY), payload.data["joined_on"])
        assertEquals(ApiDateFormatter.DATE_FORMAT, payload.data["dateFormat"])
        assertEquals(true, payload.data["owns_home"])
        assertEquals(42, payload.data["occupation"]) // the code value's ID, not its label
        // An empty optional value is ABSENT — "" is not a valid INTEGER/DATE.
        assertFalse("notes" in payload.data)
        assertFalse("client_id" in payload.data)
    }

    private companion object {
        /** 2026-10-23T12:00Z. */
        const val DAY = 1_792_756_800_000L
    }
}
