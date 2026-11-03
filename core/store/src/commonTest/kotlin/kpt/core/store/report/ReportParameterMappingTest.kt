/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.report

import kpt.core.model.objects.runreport.ColumnHeader
import kpt.core.model.objects.runreport.DataRow
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.report.ReportParameterType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReportParameterMappingTest {

    private val headers = listOf(
        "parameter_name", "parameter_variable", "parameter_label", "parameter_displayType", "parameter_FormatType",
        "parameter_default", "selectOne", "selectAll", "parentParameterName",
    ).map { ColumnHeader(columnName = it) }

    // Fineract's `FullParameterList` for a disbursal-style report: office, officer (depends on office), dates.
    private val response = FullParameterListResponse(
        columnHeaders = headers,
        data = listOf(
            DataRow(listOf("OfficeIdSelectOne", "officeId", "Office", "select", "number", "0", "Y", null, null)),
            DataRow(
                listOf(
                    "loanOfficerIdSelectAll",
                    "loanOfficerId",
                    "Loan Officer",
                    "select",
                    "number",
                    "0",
                    null,
                    "Y",
                    "OfficeIdSelectOne",
                ),
            ),
            DataRow(listOf("startDateSelect", "startDate", "startDate", "date", "date", "today", null, null, null)),
            DataRow(listOf("endDateSelect", "endDate", "endDate", "date", "date", "today", null, null, null)),
            DataRow(
                listOf(
                    "obligDateTypeSelect",
                    "obligDateType",
                    "obligDateType",
                    "none",
                    "number",
                    "1",
                    null,
                    null,
                    null,
                ),
            ),
        ),
    )

    @Test
    fun everyParameterIsReadIncludingTheDatesTheOriginalDropped() {
        val parameters = response.toReportParameters()
        assertEquals(
            listOf(
                ReportParameterType.SELECT,
                ReportParameterType.SELECT,
                ReportParameterType.DATE,
                ReportParameterType.DATE,
                ReportParameterType.HIDDEN,
            ),
            parameters.map { it.displayType },
        )
        assertEquals("R_startDate", parameters[2].queryKey)
        val officer = parameters[1]
        assertTrue(officer.allowAll)
        assertEquals("OfficeIdSelectOne", officer.parentName)
        assertEquals("Loan Officer", officer.label)
        assertEquals("1", parameters[4].defaultValue)
    }

    @Test
    fun columnsAreFoundByNameNotPosition() {
        // The same data with the columns REVERSED — a positional reader would swap name and parent.
        val reversed = FullParameterListResponse(
            columnHeaders = headers.reversed(),
            data = response.data.map { DataRow(it.row.reversed()) },
        )
        assertEquals(response.toReportParameters(), reversed.toReportParameters())
    }

    @Test
    fun aRowWithNoNameIsSkippedAndMissingHeadersFallBackToFineractsOrder() {
        val headerless = FullParameterListResponse(data = response.data + DataRow(listOf(null, "x")))
        val parameters = headerless.toReportParameters()
        assertEquals(5, parameters.size)
        assertEquals("officeId", parameters.first().variable)
        assertNull(parameters.first().parentName)
    }
}
