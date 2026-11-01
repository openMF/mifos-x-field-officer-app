/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.report.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.objects.runreport.client.ClientReportTypeItem
import kpt.core.ui.components.AppEmptyCard
import kpt.feature.report.generated.resources.Res
import kpt.feature.report.generated.resources.feature_report_accounting
import kpt.feature.report.generated.resources.feature_report_all
import kpt.feature.report.generated.resources.feature_report_client
import kpt.feature.report.generated.resources.feature_report_fund
import kpt.feature.report.generated.resources.feature_report_loan
import kpt.feature.report.generated.resources.feature_report_no_reports_found
import kpt.feature.report.generated.resources.feature_report_run_reports
import kpt.feature.report.generated.resources.feature_report_savings
import kpt.feature.report.generated.resources.feature_report_xbrl
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Ported from `6b66e8a43:feature/report/runReport/RunReportScreen.kt` — see [ReportListViewModel]. */
@Composable
internal fun ReportListScreen(
    onNavigateBack: () -> Unit,
    onOpenReport: (reportName: String) -> Unit,
    viewModel: ReportListViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ReportListEvent.NavigateBack -> onNavigateBack()
            is ReportListEvent.OpenReport -> onOpenReport(event.reportName)
        }
    }

    ReportListContent(
        state = state,
        reports = reports,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        onRetry = viewModel::onRetry,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ReportListContent(
    state: ReportListState,
    reports: ScreenState<List<ClientReportTypeItem>>,
    onAction: (ReportListAction) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    AppScaffold(
        title = stringResource(Res.string.feature_report_run_reports),
        onBackPressed = { onAction(ReportListAction.NavigateBack) },
        modifier = modifier,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).testTag(ReportListTestTags.SCREEN)) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = KptTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                ReportCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onAction(ReportListAction.CategorySelected(category)) },
                        label = { Text(stringResource(category.label())) },
                        modifier = Modifier.testTag(ReportListTestTags.category(category)),
                    )
                }
            }
            ScreenContent(
                state = reports,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            ) { list, _ ->
                if (list.isEmpty()) {
                    AppEmptyCard(
                        msg = stringResource(Res.string.feature_report_no_reports_found),
                        modifier = Modifier.padding(KptTheme.spacing.md),
                    )
                    return@ScreenContent
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag(ReportListTestTags.LIST),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    items(list, key = { it.reportId ?: it.reportName.hashCode() }) { report ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = KptTheme.spacing.md)
                                .clickable { onAction(ReportListAction.ReportClicked(report)) }
                                .testTag(ReportListTestTags.REPORT),
                        ) {
                            ListItem(
                                headlineContent = { Text(report.reportName.orEmpty()) },
                                supportingContent = {
                                    Text(listOfNotNull(report.reportType, report.reportCategory).joinToString(" · "))
                                },
                                trailingContent = { Icon(AppIcons.ChevronRight, contentDescription = null) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun ReportCategory.label() = when (this) {
    ReportCategory.CLIENT -> Res.string.feature_report_client
    ReportCategory.LOAN -> Res.string.feature_report_loan
    ReportCategory.SAVINGS -> Res.string.feature_report_savings
    ReportCategory.FUND -> Res.string.feature_report_fund
    ReportCategory.ACCOUNTING -> Res.string.feature_report_accounting
    ReportCategory.XBRL -> Res.string.feature_report_xbrl
    ReportCategory.ALL -> Res.string.feature_report_all
}

internal object ReportListTestTags {
    const val SCREEN = "reportList:screen"
    const val LIST = "reportList:list"
    const val REPORT = "reportList:report"

    fun category(category: ReportCategory) = "reportList:category:${category.name.lowercase()}"
}
