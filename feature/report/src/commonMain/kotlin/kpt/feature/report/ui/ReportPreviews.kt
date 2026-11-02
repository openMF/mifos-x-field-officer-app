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

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.runreport.ColumnHeader
import kpt.core.model.objects.runreport.DataRow
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.objects.runreport.client.ClientReportTypeItem
import kpt.core.model.report.ReportOption
import kpt.core.model.report.ReportParameter
import kpt.core.model.report.ReportParameterType
import kpt.feature.report.ui.list.ReportCategory
import kpt.feature.report.ui.list.ReportListContent
import kpt.feature.report.ui.list.ReportListState
import kpt.feature.report.ui.run.ParameterOptions
import kpt.feature.report.ui.run.ReportFailure
import kpt.feature.report.ui.run.ReportRunContent
import kpt.feature.report.ui.run.ReportRunState
import org.jetbrains.compose.ui.tooling.preview.Preview

// i18n:skip — preview fixtures
private val previewReports = listOf(
    ClientReportTypeItem(
        reportId = 1,
        reportName = "Active Loans - Details",
        reportType = "Table",
        reportCategory = "Loan",
    ),
    ClientReportTypeItem(
        reportId = 2,
        reportName = "Loans Awaiting Disbursal",
        reportType = "Table",
        reportCategory = "Loan",
    ),
    ClientReportTypeItem(reportId = 3, reportName = "Loan Payments Due", reportType = "Table", reportCategory = "Loan"),
)

// i18n:skip — preview fixtures
private val previewParameters = listOf(
    ReportParameter("OfficeIdSelectOne", "officeId", "Office", ReportParameterType.SELECT),
    ReportParameter(
        "loanOfficerIdSelectAll",
        "loanOfficerId",
        "Loan Officer",
        ReportParameterType.SELECT,
        allowAll = true,
        parentName = "OfficeIdSelectOne",
    ),
    ReportParameter("startDateSelect", "startDate", "Start Date", ReportParameterType.DATE),
    ReportParameter("endDateSelect", "endDate", "End Date", ReportParameterType.DATE),
)

// i18n:skip — preview fixtures
private val previewOptions = mapOf(
    "OfficeIdSelectOne" to ParameterOptions.Loaded(listOf(ReportOption("1", "Head Office"))),
    "loanOfficerIdSelectAll" to ParameterOptions.Loaded(listOf(ReportOption("7", "Asha Rahman"))),
)

// i18n:skip — preview fixtures
private val previewValues = mapOf(
    "OfficeIdSelectOne" to "1",
    "startDateSelect" to "2026-10-01",
    "endDateSelect" to "2026-10-24",
)

// i18n:skip — preview fixtures
private val previewResult = FullParameterListResponse(
    columnHeaders = listOf("Office", "Client", "Loan", "Principal", "Outstanding")
        .map { ColumnHeader(columnName = it) },
    data = listOf(
        DataRow(listOf("Head Office", "Asha Rahman", "000000011", "12000.00", "8400.00")),
        DataRow(listOf("Head Office", "Bimal Das", "000000021", "8000.00", null)),
        DataRow(listOf("Head Office", "Chitra Sen", "000000034", "15000.00", "15000.00")),
    ),
)

@Preview
@Composable
internal fun ReportListPreview() {
    KptTheme {
        ReportListContent(
            state = ReportListState(category = ReportCategory.LOAN),
            reports = ScreenState.Content(previewReports),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun ReportParametersPreview() {
    KptTheme {
        ReportRunContent(
            // i18n:skip — preview fixture
            state = ReportRunState(reportName = "Active Loans - Details", values = previewValues),
            parameters = ScreenState.Content(previewParameters),
            options = previewOptions,
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun ReportParametersMissingPreview() {
    KptTheme {
        ReportRunContent(
            // i18n:skip — preview fixture
            // Dates pinned: an unset date renders as TODAY, which would change this golden every day.
            state = ReportRunState(
                reportName = "Active Loans - Details",
                values = previewValues - "OfficeIdSelectOne",
                missing = setOf("OfficeIdSelectOne"),
            ),
            parameters = ScreenState.Content(previewParameters),
            options = previewOptions,
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun ReportResultPreview() {
    KptTheme {
        ReportRunContent(
            // i18n:skip — preview fixture
            state = ReportRunState(reportName = "Active Loans - Details", result = previewResult),
            parameters = ScreenState.Content(previewParameters),
            options = previewOptions,
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun ReportFailurePreview() {
    KptTheme {
        ReportRunContent(
            // i18n:skip — preview fixture
            state = ReportRunState(
                reportName = "Active Loans - Details",
                values = previewValues,
                failure = ReportFailure("Start date is after end date"),
            ),
            parameters = ScreenState.Content(previewParameters),
            options = previewOptions,
            onAction = {},
        )
    }
}
