/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.report.ui

import kpt.core.model.objects.runreport.ColumnHeader
import kpt.core.model.objects.runreport.DataRow
import kpt.core.model.objects.runreport.FullParameterListResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportCsvTest {

    @Test
    fun fieldsAreQuotedOnlyWhenTheyMustBeAndRowsKeepTheirColumns() {
        val report = FullParameterListResponse(
            columnHeaders = listOf("Name", "Note", "Amount").map { ColumnHeader(columnName = it) },
            data = listOf(
                DataRow(listOf("Asha", "said \"hi\"", "10")),
                // A null and a SHORT row: both must stay three fields, never "null", never shifted.
                DataRow(listOf("Bimal", null, "5")),
                DataRow(listOf("Chitra")),
                DataRow(listOf(" padded ", "a\nb", "1")),
            ),
        )
        assertEquals(
            "Name,Note,Amount\n" +
                "Asha,\"said \"\"hi\"\"\",10\n" +
                "Bimal,,5\n" +
                "Chitra,,\n" +
                "\" padded \",\"a\nb\",1\n",
            report.toCsv(),
        )
    }
}
