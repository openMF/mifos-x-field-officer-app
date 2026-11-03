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

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.dataOrNull
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeReportRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.objects.runreport.ColumnHeader
import kpt.core.model.objects.runreport.DataRow
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.objects.runreport.client.ClientReportTypeItem
import kpt.core.model.report.ReportOption
import kpt.core.model.report.ReportParameter
import kpt.core.model.report.ReportParameterType
import kpt.feature.report.ui.list.ReportCategory
import kpt.feature.report.ui.list.ReportListAction
import kpt.feature.report.ui.list.ReportListEvent
import kpt.feature.report.ui.list.ReportListViewModel
import kpt.feature.report.ui.run.ParameterOptions
import kpt.feature.report.ui.run.ReportRunAction
import kpt.feature.report.ui.run.ReportRunEvent
import kpt.feature.report.ui.run.ReportRunViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalDataTestingApi::class)
class ReportViewModelsTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    // ── catalogue ─────────────────────────────────────────────────────────────────────────────────

    // Fineract's catalogue: one row PER PARAMETER — "Active Loans" has two, so it arrives twice.
    private val catalogue = listOf(
        ClientReportTypeItem(
            reportId = 1,
            reportName = "Active Loans",
            reportCategory = "Loan",
            parameterName = "OfficeIdSelectOne",
        ),
        ClientReportTypeItem(
            reportId = 1,
            reportName = "Active Loans",
            reportCategory = "Loan",
            parameterName = "loanOfficerIdSelectAll",
        ),
        ClientReportTypeItem(reportId = 2, reportName = "Client Listing", reportCategory = "Client"),
        ClientReportTypeItem(reportId = 3, reportName = "Savings Summary", reportCategory = "Savings"),
    )

    private fun TestScope.list() = ReportListViewModel(FakeReportRepository(reports = catalogue)).also { vm ->
        backgroundScope.launch { vm.reports.collect {} }
        runCurrent()
    }

    @Test
    fun theCatalogueIsFilteredByCategoryAndListsEachReportOnce() = runTest(dispatcher) {
        val vm = list()
        assertEquals(listOf("Client Listing"), vm.reports.value.dataOrNull!!.map { it.reportName })
        vm.trySendAction(ReportListAction.CategorySelected(ReportCategory.LOAN))
        runCurrent()
        assertEquals(listOf("Active Loans"), vm.reports.value.dataOrNull!!.map { it.reportName })
        vm.trySendAction(ReportListAction.CategorySelected(ReportCategory.ALL))
        runCurrent()
        assertEquals(3, vm.reports.value.dataOrNull!!.size)
    }

    @Test
    fun tappingAReportOpensItByName() = runTest(dispatcher) {
        val vm = list()
        vm.eventFlow.test {
            vm.trySendAction(ReportListAction.ReportClicked(catalogue[2]))
            runCurrent()
            assertEquals(ReportListEvent.OpenReport("Client Listing"), awaitItem())
        }
    }

    // ── run ───────────────────────────────────────────────────────────────────────────────────────

    private val office = ReportParameter("OfficeIdSelectOne", "officeId", "Office", ReportParameterType.SELECT)
    private val officer = ReportParameter(
        "loanOfficerIdSelectAll",
        "loanOfficerId",
        "Loan Officer",
        ReportParameterType.SELECT,
        allowAll = true,
        parentName = "OfficeIdSelectOne",
    )
    private val start = ReportParameter("startDateSelect", "startDate", "Start", ReportParameterType.DATE)
    private val hidden = ReportParameter(
        "obligDateTypeSelect",
        "obligDateType",
        "x",
        ReportParameterType.HIDDEN,
        defaultValue = "1",
    )

    private val offices = listOf(ReportOption("1", "Head Office"), ReportOption("2", "Branch"))
    private val officers = listOf(ReportOption("7", "Asha"))

    private fun repository(
        result: () -> Result<FullParameterListResponse> = { Result.success(FullParameterListResponse()) },
    ) =
        FakeReportRepository(
            parameters = listOf(office, officer, start, hidden),
            options = { name, _ -> if (name == office.name) offices else officers },
            result = result,
        )

    private fun TestScope.run(repository: FakeReportRepository) =
        ReportRunViewModel(SavedStateHandle(mapOf("reportName" to "Active Loans")), repository).also { vm ->
            backgroundScope.launch { vm.parameters.collect {} }
            backgroundScope.launch { vm.options.collect {} }
            runCurrent()
        }

    @Test
    fun aChildsOptionsWaitForItsParentAndAreNarrowedByIt() = runTest(dispatcher) {
        val repo = repository()
        val vm = run(repo)
        assertEquals(ParameterOptions.Loaded(offices), vm.options.value[office.name])
        assertEquals(ParameterOptions.WaitingForParent, vm.options.value[officer.name])

        vm.trySendAction(ReportRunAction.ValueChanged(office.name, "2"))
        runCurrent()
        assertEquals(ParameterOptions.Loaded(officers), vm.options.value[officer.name])
        assertEquals(officer.name to mapOf("R_officeId" to "2"), repo.optionRequests.last())
    }

    @Test
    fun changingTheParentClearsTheChildsValue() = runTest(dispatcher) {
        val vm = run(repository())
        vm.trySendAction(ReportRunAction.ValueChanged(office.name, "1"))
        runCurrent()
        vm.trySendAction(ReportRunAction.ValueChanged(officer.name, "7"))
        runCurrent()
        vm.trySendAction(ReportRunAction.ValueChanged(office.name, "2"))
        runCurrent()
        assertNull(vm.stateFlow.value.values[officer.name])
    }

    @Test
    fun runRefusesAMissingRequiredSelectAndSendsNothing() = runTest(dispatcher) {
        val repo = repository()
        val vm = run(repo)
        vm.trySendAction(ReportRunAction.Run)
        runCurrent()
        // The office has no "All" — it must be chosen. The officer defaults to All; the date to today.
        assertEquals(setOf(office.name), vm.stateFlow.value.missing)
        assertTrue(repo.runs.isEmpty())
    }

    @Test
    fun runSendsEveryParameterUnderItsVariableWithDerivedDefaults() = runTest(dispatcher) {
        val repo = repository()
        val vm = run(repo)
        vm.trySendAction(ReportRunAction.ValueChanged(office.name, "1"))
        vm.trySendAction(ReportRunAction.ValueChanged(start.name, "2026-10-01"))
        vm.trySendAction(ReportRunAction.Run)
        runCurrent()

        val (name, query) = repo.runs.single()
        assertEquals("Active Loans", name)
        assertEquals(
            mapOf(
                "R_officeId" to "1",
                "R_loanOfficerId" to "-1",
                "R_startDate" to "2026-10-01",
                "R_obligDateType" to "1",
            ),
            query,
        )
        assertNotNull(vm.stateFlow.value.result)
    }

    @Test
    fun aRejectedRunShowsTheServersReasonAndKeepsTheForm() = runTest(dispatcher) {
        val vm = run(repository { Result.failure(fineractRejection("Start date is after end date")) })
        vm.trySendAction(ReportRunAction.ValueChanged(office.name, "1"))
        vm.trySendAction(ReportRunAction.Run)
        runCurrent()
        assertEquals("Start date is after end date", vm.stateFlow.value.failure?.serverMessage)
        assertNull(vm.stateFlow.value.result)
        assertEquals("1", vm.stateFlow.value.values[office.name])
    }

    @Test
    fun exportHandsTheScreenTheResultAsCsvAndBackReturnsToTheForm() = runTest(dispatcher) {
        val result = FullParameterListResponse(
            columnHeaders = listOf(ColumnHeader(columnName = "Client"), ColumnHeader(columnName = "Due")),
            data = listOf(DataRow(listOf("Das, A.", "10"))),
        )
        val vm = run(repository { Result.success(result) })
        vm.trySendAction(ReportRunAction.ValueChanged(office.name, "1"))
        vm.trySendAction(ReportRunAction.Run)
        runCurrent()
        vm.eventFlow.test {
            vm.trySendAction(ReportRunAction.Export)
            runCurrent()
            val export = assertIs<ReportRunEvent.ExportCsv>(awaitItem())
            assertEquals("active-loans", export.fileName)
            assertEquals("Client,Due\n\"Das, A.\",10\n", export.csv)
        }
        vm.trySendAction(ReportRunAction.NavigateBack)
        runCurrent()
        assertNull(vm.stateFlow.value.result)
    }
}
