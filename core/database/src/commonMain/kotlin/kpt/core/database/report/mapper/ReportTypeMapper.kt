/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.report.mapper

import kpt.core.database.report.entity.ReportTypeEntity
import kpt.core.model.objects.runreport.client.ClientReportTypeItem

fun ReportTypeEntity.toDomain(): ClientReportTypeItem = ClientReportTypeItem(
    reportId = reportId,
    parameterId = parameterId,
    reportName = reportName,
    reportCategory = reportCategory,
    parameterName = parameterName,
    reportParameterName = reportParameterName,
    reportSubtype = reportSubtype,
    reportType = reportType,
)

/**
 * `(reportId, parameterId)` is the composite primary key, so a row missing either cannot be
 * cached; the caller drops those rather than inventing a 0 that would collide.
 */
fun ClientReportTypeItem.toEntityOrNull(): ReportTypeEntity? {
    val report = reportId ?: return null
    val parameter = parameterId ?: return null
    return ReportTypeEntity(
        reportId = report,
        parameterId = parameter,
        reportName = reportName,
        reportCategory = reportCategory,
        parameterName = parameterName,
        reportParameterName = reportParameterName,
        reportSubtype = reportSubtype,
        reportType = reportType,
    )
}
