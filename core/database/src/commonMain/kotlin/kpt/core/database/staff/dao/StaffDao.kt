/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.staff.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.staff.entity.StaffEntity

/**
 * Created by Pronay Sarker on 14/02/2025 (1:54 AM)
 */
@DbDao
@Dao
interface StaffDao {

    /** Saves `staffs`, replacing rows with the same key. */
    @Insert(entity = StaffEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffs(staffs: List<StaffEntity>)

    /** Saves `staff`, replacing any row with the same key. */
    @Insert(entity = StaffEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity)

    /** Observes the `Staff` rows whose `officeId` is `officeId`. */
    @Query("SELECT * FROM Staff WHERE officeId = :officeId")
    fun getAllStaff(officeId: Int): Flow<List<StaffEntity>>

    /** Deletes the `Staff` rows whose `officeId` is `officeId`. */
    @Query("DELETE FROM Staff WHERE officeId = :officeId")
    suspend fun deleteForOffice(officeId: Int)

    /** Deletes every `Staff` row. */
    @Query("DELETE FROM Staff")
    suspend fun deleteAll()
}
