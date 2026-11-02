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

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.objects.runreport.client.ClientReportTypeItem
import kpt.core.model.report.ReportOption
import kpt.core.model.report.ReportParameter

interface ReportRepository {
    /** The runnable report catalogue. */
    fun reportTypesStream(scope: CoroutineScope): ScreenDataStream<List<ClientReportTypeItem>>

    /** What [reportName] asks for — its parameters, in the server's order. */
    fun reportParametersStream(reportName: String, scope: CoroutineScope): ScreenDataStream<List<ReportParameter>>

    /**
     * A SELECT parameter's options. [parentValues] narrow them (`R_officeId` for a loan-officer list);
     * null when they could not be read.
     */
    suspend fun parameterOptions(parameterName: String, parentValues: Map<String, String>): List<ReportOption>?

    /** Runs [reportName] with [values] (`R_*` keys). Always from the server — a report is not cached data. */
    suspend fun runReport(reportName: String, values: Map<String, String>): Result<FullParameterListResponse>
}
