/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.report.ui.list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.report.ReportRepository
import kpt.core.model.objects.runreport.client.ClientReportTypeItem

/**
 * Ported from `6b66e8a43:feature/report/runReport/RunReportViewModel.kt`. The original fetched one
 * category at a time (a menu in the top bar); here the catalogue is read once and [ReportCategory] is a
 * filter on it — switching category is instant and works offline.
 *
 * Fineract's catalogue returns one row PER REPORT PARAMETER, so the original listed a report with four
 * parameters four times. Rows are distinct by report id.
 */
internal class ReportListViewModel(
    repository: ReportRepository,
) : BaseViewModel<ReportListState, ReportListEvent, ReportListAction>(ReportListState()) {

    private val stream = repository.reportTypesStream(viewModelScope)

    val reports: StateFlow<ScreenState<List<ClientReportTypeItem>>> = stream.state
        .combineContent(stateFlow) { all, ui, _ -> all.distinctReports().filter { ui.category.matches(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    fun onRetry() = stream.retry()

    override fun handleAction(action: ReportListAction) {
        when (action) {
            is ReportListAction.CategorySelected -> mutableStateFlow.value = state.copy(category = action.category)
            is ReportListAction.ReportClicked ->
                action.report.reportName?.let { sendEvent(ReportListEvent.OpenReport(it)) }
            ReportListAction.NavigateBack -> sendEvent(ReportListEvent.NavigateBack)
        }
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

internal fun List<ClientReportTypeItem>.distinctReports(): List<ClientReportTypeItem> =
    filter { !it.reportName.isNullOrBlank() }.distinctBy { it.reportId ?: it.reportName }

/** The original's menu, in its order. [server] is Fineract's `report_category`; [ALL] filters nothing. */
internal enum class ReportCategory(val server: String?) {
    CLIENT("Client"),
    LOAN("Loan"),
    SAVINGS("Savings"),
    FUND("Fund"),
    ACCOUNTING("Accounting"),
    XBRL("XBRL"),
    ALL(null),
    ;

    fun matches(report: ClientReportTypeItem) =
        server == null || report.reportCategory.equals(server, ignoreCase = true)
}

internal data class ReportListState(val category: ReportCategory = ReportCategory.CLIENT)

internal sealed interface ReportListEvent {
    data object NavigateBack : ReportListEvent
    data class OpenReport(val reportName: String) : ReportListEvent
}

internal sealed interface ReportListAction {
    data class CategorySelected(val category: ReportCategory) : ReportListAction
    data class ReportClicked(val report: ClientReportTypeItem) : ReportListAction
    data object NavigateBack : ReportListAction
}
