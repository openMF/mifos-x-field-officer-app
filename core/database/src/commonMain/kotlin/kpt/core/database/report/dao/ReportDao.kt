/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.report.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.report.entity.ReportTypeEntity

/** Room queries for report rows. */
@DbDao
@Dao
interface ReportDao {

    /** Observes the `report_types` rows, ordered by `reportName`. */
    @Query("SELECT * FROM report_types ORDER BY reportName ASC")
    fun observeAll(): Flow<List<ReportTypeEntity>>

    /** The catalogue is browsed by category, which is why that column is indexed. */
    @Query("SELECT * FROM report_types WHERE reportCategory = :category ORDER BY reportName ASC")
    fun observeByCategory(category: String): Flow<List<ReportTypeEntity>>

    /** Observes the `report_types` rows whose `reportId` is `reportId`. */
    @Query("SELECT * FROM report_types WHERE reportId = :reportId")
    fun observeParametersFor(reportId: Int): Flow<List<ReportTypeEntity>>

    /** Reads the `report_types` row whose `reportId` is `reportId`. */
    @Query("SELECT * FROM report_types WHERE reportId = :reportId LIMIT 1")
    suspend fun getById(reportId: Int): ReportTypeEntity?

    /** Inserts `reports`, or replaces rows with the same key. */
    @Upsert
    suspend fun upsertAll(reports: List<ReportTypeEntity>)

    /** Deletes every `report_types` row. */
    @Query("DELETE FROM report_types")
    suspend fun deleteAll()
}
