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

@DbDao
@Dao
interface ReportDao {

    @Query("SELECT * FROM report_types ORDER BY reportName ASC")
    fun observeAll(): Flow<List<ReportTypeEntity>>

    /** The catalogue is browsed by category, which is why that column is indexed. */
    @Query("SELECT * FROM report_types WHERE reportCategory = :category ORDER BY reportName ASC")
    fun observeByCategory(category: String): Flow<List<ReportTypeEntity>>

    @Query("SELECT * FROM report_types WHERE reportId = :reportId")
    fun observeParametersFor(reportId: Int): Flow<List<ReportTypeEntity>>

    @Query("SELECT * FROM report_types WHERE reportId = :reportId LIMIT 1")
    suspend fun getById(reportId: Int): ReportTypeEntity?

    @Upsert
    suspend fun upsertAll(reports: List<ReportTypeEntity>)

    @Query("DELETE FROM report_types")
    suspend fun deleteAll()
}
