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

import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.report.ReportParameter
import kpt.core.model.report.ReportParameterType

/*
 * Fineract's report metadata, read as data. The original read ONLY column 0 (the parameter name) of
 * `FullParameterList` and recognised ten hardcoded names; every other parameter — including the start /
 * end DATES most transaction reports take — was silently dropped, so those reports could not run. The
 * metadata already says what each parameter is: its variable, label, display type, default, and parent.
 */

/** `FullParameterList`'s columns, by name; the positions are Fineract's query order, used if a name is missing. */
private val COLUMNS = mapOf(
    "parameter_name" to 0,
    "parameter_variable" to 1,
    "parameter_label" to 2,
    "parameter_displayType" to 3,
    "parameter_default" to 5,
    "selectAll" to 7,
    "parentParameterName" to 8,
)

fun FullParameterListResponse.toReportParameters(): List<ReportParameter> {
    val index = COLUMNS.mapValues { (name, position) ->
        columnHeaders.indexOfFirst { it.columnName.equals(name, ignoreCase = true) }.takeIf { it >= 0 } ?: position
    }
    fun List<String?>.at(column: String) = getOrNull(index.getValue(column))?.trim()?.ifEmpty { null }
    return data.mapNotNull { dataRow ->
        val row = dataRow.row
        val name = row.at("parameter_name") ?: return@mapNotNull null
        ReportParameter(
            name = name,
            // A parameter without a variable cannot be sent; its name is the best remaining key.
            variable = row.at("parameter_variable") ?: name,
            label = row.at("parameter_label") ?: name,
            displayType = when (row.at("parameter_displayType")?.lowercase()) {
                "select" -> ReportParameterType.SELECT
                "date" -> ReportParameterType.DATE
                "none" -> ReportParameterType.HIDDEN
                else -> ReportParameterType.TEXT
            },
            defaultValue = row.at("parameter_default"),
            allowAll = row.at("selectAll")?.let { it.equals("Y", true) || it.equals("true", true) } == true,
            parentName = row.at("parentParameterName"),
        )
    }
}
