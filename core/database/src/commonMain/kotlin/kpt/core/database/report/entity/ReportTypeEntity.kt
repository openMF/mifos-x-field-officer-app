/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.report.entity

import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * A runnable report definition (the CATALOGUE, not a result set).
 *
 * Definitions are slow-changing server reference data, so caching them lets a field officer browse
 * available reports offline. RUNNING a report stays online — its output is a live query against
 * Fineract, and a stale result is worse than no result.
 *
 * Keyed by `reportId`; `parameterId` is part of the row rather than the key because one report
 * has many parameters, which is also why the category index exists.
 */
@DbEntity
@Entity(
    tableName = "report_types",
    primaryKeys = ["reportId", "parameterId"],
    indices = [Index(value = ["reportCategory"])],
)
data class ReportTypeEntity(
    val reportId: Int,
    val parameterId: Int,
    val reportName: String? = null,
    val reportCategory: String? = null,
    val parameterName: String? = null,
    val reportParameterName: String? = null,
    val reportSubtype: String? = null,
    val reportType: String? = null,
)
