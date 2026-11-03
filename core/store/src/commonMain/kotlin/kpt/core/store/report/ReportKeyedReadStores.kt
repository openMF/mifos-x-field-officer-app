/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.report

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.objects.groups.CenterInfo
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.report.ReportParameter
import kpt.core.network.mifos.report.api.RunReportsApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for report with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `getReportFullParameterList` read. */
data class GetReportFullParameterListKey(
    /** The report's name. */
    val reportName: String,
    /** Fineract's `parameterType` flag: true runs the report as a parameter query. */
    val parameterType: Boolean,
)

/** Addresses one `getReportParameterDetails` read. */
data class GetReportParameterDetailsKey(
    /** The report parameter's name. */
    val parameterName: String,
    /** Fineract's `parameterType` flag: true runs the report as a parameter query. */
    val parameterType: Boolean,
)

/** Addresses one `getReportOffice` read. */
data class GetReportOfficeKey(
    /** The report parameter's name. */
    val parameterName: String,
    /** The office's server id. */
    val office: Int,
    /** Fineract's `parameterType` flag: true runs the report as a parameter query. */
    val parameterType: Boolean,
)

/** Addresses one `getReportProduct` read. */
data class GetReportProductKey(
    /** The report parameter's name. */
    val parameterName: String,
    /** The currency code. */
    val currency: String,
    /** Fineract's `parameterType` flag: true runs the report as a parameter query. */
    val parameterType: Boolean,
)

/** Addresses one `getRunReportWithQuery` read. */
data class GetRunReportWithQueryKey(
    /** The report's name. */
    val reportName: String,
    /** The report's parameter values. */
    val options: Map<String, String>,
)

/** Addresses one `getCenterSummaryInfo` read. */
data class GetCenterSummaryInfoKey(
    /** The centre's server id. */
    val centerId: Int,
    /** True to return the generic result-set shape. */
    val genericResultSet: Boolean,
)

/** Store reading the report full parameter list. */
@StoreProvider(id = "reportGetReportFullParameterList", ttl = "12h")
@CacheKey(fn = "forKey", key = "reportGetReportFullParameterList:{key}", params = ["key:String"])
fun provideGetReportFullParameterListStore(
    runReportsApi: RunReportsApi,
    cache: ApiResponseCacheDao,
): Store<GetReportFullParameterListKey, FullParameterListResponse> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ReportGetReportFullParameterList.forKey("${key.reportName}:${key.parameterType}") },
    fetch = { key ->
        runReportsApi.getReportFullParameterList(
            reportName = key.reportName,
            parameterType = key.parameterType,
        )
    },
)

/** Store reading the report parameter details. */
@StoreProvider(id = "reportGetReportParameterDetails", ttl = "12h")
@CacheKey(fn = "forKey", key = "reportGetReportParameterDetails:{key}", params = ["key:String"])
fun provideGetReportParameterDetailsStore(
    runReportsApi: RunReportsApi,
    cache: ApiResponseCacheDao,
): Store<GetReportParameterDetailsKey, FullParameterListResponse> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ReportGetReportParameterDetails.forKey("${key.parameterName}:${key.parameterType}") },
    fetch = { key ->
        runReportsApi.getReportParameterDetails(
            parameterName = key.parameterName,
            parameterType = key.parameterType,
        )
    },
)

/** Store reading the report office. */
@StoreProvider(id = "reportGetReportOffice", ttl = "12h")
@CacheKey(fn = "forKey", key = "reportGetReportOffice:{key}", params = ["key:String"])
fun provideGetReportOfficeStore(
    runReportsApi: RunReportsApi,
    cache: ApiResponseCacheDao,
): Store<GetReportOfficeKey, FullParameterListResponse> = cachedRead(
    dao = cache,
    keyOf = { key ->
        AppCacheKeys.ReportGetReportOffice.forKey("${key.parameterName}:${key.office}:${key.parameterType}")
    },
    fetch = { key ->
        runReportsApi.getReportOffice(
            parameterName = key.parameterName,
            office = key.office,
            parameterType = key.parameterType,
        )
    },
)

/** Store reading the report product. */
@StoreProvider(id = "reportGetReportProduct", ttl = "12h")
@CacheKey(fn = "forKey", key = "reportGetReportProduct:{key}", params = ["key:String"])
fun provideGetReportProductStore(
    runReportsApi: RunReportsApi,
    cache: ApiResponseCacheDao,
): Store<GetReportProductKey, FullParameterListResponse> = cachedRead(
    dao = cache,
    keyOf = { key ->
        AppCacheKeys.ReportGetReportProduct.forKey("${key.parameterName}:${key.currency}:${key.parameterType}")
    },
    fetch = { key ->
        runReportsApi.getReportProduct(
            parameterName = key.parameterName,
            currency = key.currency,
            parameterType = key.parameterType,
        )
    },
)

/** Store reading the run report with query. */
@StoreProvider(id = "reportGetRunReportWithQuery", ttl = "12h")
@CacheKey(fn = "forKey", key = "reportGetRunReportWithQuery:{key}", params = ["key:String"])
fun provideGetRunReportWithQueryStore(
    runReportsApi: RunReportsApi,
    cache: ApiResponseCacheDao,
): Store<GetRunReportWithQueryKey, FullParameterListResponse> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ReportGetRunReportWithQuery.forKey("${key.reportName}:${key.options}") },
    fetch = { key -> runReportsApi.getRunReportWithQuery(reportName = key.reportName, options = key.options) },
)

/** Store reading the center summary info. */
@StoreProvider(id = "reportGetCenterSummaryInfo", ttl = "12h")
@CacheKey(fn = "forKey", key = "reportGetCenterSummaryInfo:{key}", params = ["key:String"])
fun provideGetCenterSummaryInfoStore(
    runReportsApi: RunReportsApi,
    cache: ApiResponseCacheDao,
): Store<GetCenterSummaryInfoKey, List<CenterInfo>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ReportGetCenterSummaryInfo.forKey("${key.centerId}:${key.genericResultSet}") },
    fetch = { key ->
        runReportsApi.getCenterSummaryInfo(
            centerId = key.centerId,
            genericResultSet = key.genericResultSet,
        )
    },
)

/**
 * A report's parameters as data — `FullParameterList` mapped by [toReportParameters]. Keyed by the
 * report's plain name; the SQL-literal quoting `R_reportListing` needs is applied here, once.
 */
@StoreProvider(id = "reportParameters", ttl = "12h")
@CacheKey(fn = "forReport", key = "reportParameters:{report}", params = ["report:String"])
fun provideReportParametersStore(
    runReportsApi: RunReportsApi,
    cache: ApiResponseCacheDao,
): Store<String, List<ReportParameter>> = cachedRead(
    dao = cache,
    keyOf = { report -> AppCacheKeys.ReportParameters.forReport(report) },
    fetch = { report -> runReportsApi.getReportFullParameterList(reportName = "'$report'", parameterType = true) },
    map = { it.toReportParameters() },
)
