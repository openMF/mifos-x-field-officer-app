/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.report.ReportRepository
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.objects.runreport.client.ClientReportTypeItem
import kpt.core.model.report.ReportOption
import kpt.core.model.report.ReportParameter

/** Scripted reports: [reports] and [parameters] stream; [options] answer by parameter name; [result] answers a run. */
@ExperimentalDataTestingApi
class FakeReportRepository(
    reports: List<ClientReportTypeItem> = emptyList(),
    parameters: List<ReportParameter> = emptyList(),
    private val options: (name: String, parents: Map<String, String>) -> List<ReportOption>? = { _, _ -> emptyList() },
    private val result: () -> Result<FullParameterListResponse> = { Result.success(FullParameterListResponse()) },
) : ReportRepository {

    val reports = MutableStateFlow<ScreenState<List<ClientReportTypeItem>>>(ScreenState.Content(reports))
    val parameters = MutableStateFlow<ScreenState<List<ReportParameter>>>(ScreenState.Content(parameters))

    /** The parameter-option reads, as (parameter, parent values), in order. */
    val optionRequests = mutableListOf<Pair<String, Map<String, String>>>()

    /** The report runs, as (report, parameters), in order. */
    val runs = mutableListOf<Pair<String, Map<String, String>>>()

    override fun reportTypesStream(scope: CoroutineScope) = screenDataStreamForTesting(reports)

    override fun reportParametersStream(
        reportName: String,
        scope: CoroutineScope,
    ) = screenDataStreamForTesting(parameters)

    override suspend fun parameterOptions(
        parameterName: String,
        parentValues: Map<String, String>,
    ): List<ReportOption>? {
        optionRequests += parameterName to parentValues
        return options(parameterName, parentValues)
    }

    override suspend fun runReport(reportName: String, values: Map<String, String>): Result<FullParameterListResponse> {
        runs += reportName to values
        return result()
    }
}
