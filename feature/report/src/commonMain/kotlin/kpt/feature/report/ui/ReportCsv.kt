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

import kpt.core.model.objects.runreport.FullParameterListResponse

/**
 * The report as CSV (RFC 4180). The original joined values with "," as they were — a comma or quote in
 * a value (a client named "Das, A.") broke the columns, and a missing value was written as `null`.
 * Each field is quoted when it needs to be, quotes are doubled, and a missing value is empty.
 */
internal fun FullParameterListResponse.toCsv(): String = buildString {
    appendLine(columnHeaders.joinToString(",") { csvField(it.columnName) })
    for (dataRow in data) {
        // Pad short rows: every line has one field per header, so columns cannot shift.
        val cells = columnHeaders.indices.map { dataRow.row.getOrNull(it) }
        appendLine(cells.joinToString(",") { csvField(it) })
    }
}

private fun csvField(value: String?): String {
    if (value == null) return ""
    val needsQuotes = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' } || value != value.trim()
    return if (needsQuotes) "\"" + value.replace("\"", "\"\"") + "\"" else value
}
