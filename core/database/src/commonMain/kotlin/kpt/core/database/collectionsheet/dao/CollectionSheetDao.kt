/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.collectionsheet.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.collectionsheet.entity.CenterDetailEntity

/** Room queries for collection sheet rows. */
@DbDao
@Dao
interface CollectionSheetDao {

    /** Observes the `collection_sheet_center_details` rows, ordered by `staffName`. */
    @Query("SELECT * FROM collection_sheet_center_details ORDER BY staffName ASC")
    fun observeAll(): Flow<List<CenterDetailEntity>>

    /** The roster is read per staff member — that is the only access path the UI has. */
    @Query("SELECT * FROM collection_sheet_center_details WHERE staffId = :staffId LIMIT 1")
    fun observeForStaff(staffId: Int): Flow<CenterDetailEntity?>

    /** Reads the `collection_sheet_center_details` row whose `staffId` is `staffId`. */
    @Query("SELECT * FROM collection_sheet_center_details WHERE staffId = :staffId LIMIT 1")
    suspend fun getForStaff(staffId: Int): CenterDetailEntity?

    /** Inserts `details`, or replaces rows with the same key. */
    @Upsert
    suspend fun upsertAll(details: List<CenterDetailEntity>)

    /** Deletes the `collection_sheet_center_details` rows whose `staffId` is `staffId`. */
    @Query("DELETE FROM collection_sheet_center_details WHERE staffId = :staffId")
    suspend fun deleteForStaff(staffId: Int)

    /** Deletes every `collection_sheet_center_details` row. */
    @Query("DELETE FROM collection_sheet_center_details")
    suspend fun deleteAll()
}
