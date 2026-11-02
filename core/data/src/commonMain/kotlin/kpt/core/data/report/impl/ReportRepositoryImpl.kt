/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.report.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.report.ReportRepository
import kpt.core.data.report.toReportOptions
import kpt.core.data.util.freshOrCached
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.objects.runreport.client.ClientReportTypeItem
import kpt.core.model.report.ReportOption
import kpt.core.model.report.ReportParameter
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import kpt.core.store.report.GetRunReportWithQueryKey
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

@RepositoryBinding(binds = ReportRepository::class)
internal class ReportRepositoryImpl(
    @FromStore(AppStoreIds.ReportTypes) private val store: Store<Unit, List<ClientReportTypeItem>>,
    @FromStore(AppStoreIds.ReportParameters) private val parameterStore: Store<String, List<ReportParameter>>,
    @FromStore(AppStoreIds.ReportGetRunReportWithQuery)
    private val runStore: Store<GetRunReportWithQueryKey, FullParameterListResponse>,
) : ReportRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
    // until something HAS been fetched, so on a clean install nothing ever loads. Room is still
    // served first, so the offline-first behaviour is unchanged. The ttl stays the declared
    // freshness bound — it drives the "updated N ago" indicator, never whether cache is served.
    override fun reportTypesStream(scope: CoroutineScope): ScreenDataStream<List<ClientReportTypeItem>> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.ReportTypes.LIST,
            scope = scope,
            // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.REPORT_TYPES,
        )

    override fun reportParametersStream(
        reportName: String,
        scope: CoroutineScope,
    ): ScreenDataStream<List<ReportParameter>> {
        return parameterStore.asScreenStream(
            key = reportName,
            cacheKey = AppCacheKeys.ReportParameters.forReport(reportName),
            scope = scope,
            // No `isEmpty`: a report with no parameters is real content (run it as it is).
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.REPORT_PARAMETERS,
        )
    }

    // Options are the parameter's OWN report, run with `parameterType=true` (+ the parent's value),
    // through the same store as a report run — one read path. Cached: an office list is reference data.
    override suspend fun parameterOptions(
        parameterName: String,
        parentValues: Map<String, String>,
    ): List<ReportOption>? =
        runStore.freshOrCached(GetRunReportWithQueryKey(parameterName, parentValues + (PARAMETER_TYPE to "true")))
            ?.toReportOptions()

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun runReport(reportName: String, values: Map<String, String>): Result<FullParameterListResponse> =
        try {
            Result.success(runStore.fresh(GetRunReportWithQueryKey(reportName, values)))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }

    private companion object {
        const val PARAMETER_TYPE = "parameterType"
    }
}
