/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.report

import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.report.ReportOption

/** A parameter's own report lists its options as rows of `[id, label]`. */
internal fun FullParameterListResponse.toReportOptions(): List<ReportOption> =
    data.mapNotNull { dataRow ->
        val id = dataRow.row.getOrNull(0)?.trim()?.ifEmpty { null } ?: return@mapNotNull null
        ReportOption(id = id, label = dataRow.row.getOrNull(1)?.trim()?.ifEmpty { null } ?: id)
    }
