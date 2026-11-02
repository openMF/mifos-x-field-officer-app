/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.recent.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.recent.entity.RecentRecordEntity

@DbDao
@Dao
interface RecentRecordDao {

    @Query("SELECT * FROM recent_records WHERE userId = :userId ORDER BY openedAtMs DESC LIMIT :limit")
    fun observe(userId: Long, limit: Int): Flow<List<RecentRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: RecentRecordEntity)

    /** Keep the newest [keep] per user — the card shows a handful, the table never grows. */
    @Query(
        "DELETE FROM recent_records WHERE userId = :userId AND openedAtMs NOT IN " +
            "(SELECT openedAtMs FROM recent_records WHERE userId = :userId ORDER BY openedAtMs DESC LIMIT :keep)",
    )
    suspend fun trim(userId: Long, keep: Int)

    /** Reopening a record moves it to the top (same key, newer time) rather than duplicating it. */
    @Transaction
    suspend fun record(record: RecentRecordEntity, keep: Int) {
        upsert(record)
        trim(record.userId, keep)
    }
}
