/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.collectionsheet.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.collectionsheet.entity.CenterDetailEntity

@DbDao
@Dao
interface CollectionSheetDao {

    @Query("SELECT * FROM collection_sheet_center_details ORDER BY staffName ASC")
    fun observeAll(): Flow<List<CenterDetailEntity>>

    /** The roster is read per staff member — that is the only access path the UI has. */
    @Query("SELECT * FROM collection_sheet_center_details WHERE staffId = :staffId LIMIT 1")
    fun observeForStaff(staffId: Int): Flow<CenterDetailEntity?>

    @Query("SELECT * FROM collection_sheet_center_details WHERE staffId = :staffId LIMIT 1")
    suspend fun getForStaff(staffId: Int): CenterDetailEntity?

    @Upsert
    suspend fun upsertAll(details: List<CenterDetailEntity>)

    @Query("DELETE FROM collection_sheet_center_details")
    suspend fun deleteAll()
}
