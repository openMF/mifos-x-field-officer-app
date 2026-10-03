/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.report

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.report.dao.ReportDao
import kpt.core.database.report.mapper.toDomain
import kpt.core.database.report.mapper.toEntityOrNull
import kpt.core.model.objects.runreport.client.ClientReportTypeItem
import kpt.core.network.mifos.report.api.RunReportsApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * The full report catalogue. `genericResultSet = true` is what makes Fineract return the rows as
 * data rather than a rendered report, and `parameterType = false` keeps the response to report
 * definitions instead of their parameter metadata.
 */
@StoreProvider(id = "reportTypes", ttl = "24h")
@CacheKey(name = "LIST", key = "reportTypes")
fun provideReportStore(
    service: RunReportsApi,
    dao: ReportDao,
): Store<Unit, List<ClientReportTypeItem>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getReportCategories(category = null, genericResultSet = true, parameterType = false)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.observeAll().map { rows -> rows.map { it.toDomain() } } },
        // Rows without the composite key cannot be stored; dropping them keeps the cache honest
        // rather than collapsing every keyless row onto (0, 0).
        writer = { _: Unit, reports: List<ClientReportTypeItem> ->
            dao.upsertAll(reports.mapNotNull { it.toEntityOrNull() })
        },
    ),
)
