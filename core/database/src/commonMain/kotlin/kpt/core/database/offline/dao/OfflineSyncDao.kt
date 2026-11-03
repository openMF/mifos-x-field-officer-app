/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.offline.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.offline.entity.OfflineRecordEntity
import kpt.core.database.offline.entity.SyncJobEntity

/** Room queries for offline sync rows. */
@DbDao
@Dao
interface OfflineSyncDao {

    // ── inventory ──

    /** Observes the `offline_records` rows whose `userId` is `userId`, ordered by `name COLLATE NOCASE`. */
    @Query("SELECT * FROM offline_records WHERE userId = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeRecords(userId: Long): Flow<List<OfflineRecordEntity>>

    /** Reads the `offline_records` rows whose `userId` is `userId`. */
    @Query("SELECT * FROM offline_records WHERE userId = :userId")
    suspend fun records(userId: Long): List<OfflineRecordEntity>

    /** Reads the `offline_records` row whose `userId` is `userId` and `kind` is `kind` and `id` is `id`. */
    @Query("SELECT * FROM offline_records WHERE userId = :userId AND kind = :kind AND id = :id LIMIT 1")
    suspend fun record(userId: Long, kind: String, id: Int): OfflineRecordEntity?

    /** Inserts `record`, or replaces the row with the same key. */
    @Upsert
    suspend fun upsertRecord(record: OfflineRecordEntity)

    /** Sets the sync status of one record. */
    @Query("UPDATE offline_records SET status = :status WHERE userId = :userId AND kind = :kind AND id = :id")
    suspend fun setStatus(userId: Long, kind: String, id: Int, status: String)

    /** Moves every record of the user in status `from` to status `to`. */
    @Query("UPDATE offline_records SET status = :to WHERE userId = :userId AND status = :from")
    suspend fun moveStatus(userId: Long, from: String, to: String)

    /** Deletes the `offline_records` rows whose `userId` is `userId` and `kind` is `kind` and `id` is `id`. */
    @Query("DELETE FROM offline_records WHERE userId = :userId AND kind = :kind AND id = :id")
    suspend fun deleteRecord(userId: Long, kind: String, id: Int)

    /** Deletes the children of a parent record that the user did not choose. */
    @Query(
        "DELETE FROM offline_records WHERE userId = :userId AND parentKind = :parentKind " +
            "AND parentId = :parentId AND chosen = 0",
    )
    suspend fun deleteChildren(userId: Long, parentKind: String, parentId: Int)

    /**
     * Reads the `offline_records` rows whose `userId` is `userId` and `parentKind` is `parentKind` and `parentId`
     * is `parentId`.
     */
    @Query("SELECT * FROM offline_records WHERE userId = :userId AND parentKind = :parentKind AND parentId = :parentId")
    suspend fun children(userId: Long, parentKind: String, parentId: Int): List<OfflineRecordEntity>

    // ── jobs ──

    /** Observes the `sync_jobs` row whose `userId` is `userId`, ordered by `id`. */
    @Query("SELECT * FROM sync_jobs WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    fun observeLatestJob(userId: Long): Flow<SyncJobEntity?>

    /** Reads the user's newest running sync job, if any. */
    @Query("SELECT * FROM sync_jobs WHERE userId = :userId AND status = 'RUNNING' ORDER BY id DESC LIMIT 1")
    suspend fun runningJob(userId: Long): SyncJobEntity?

    /** Saves `job`, replacing any row with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: SyncJobEntity): Long

    /** Inserts `job`, or replaces the row with the same key. */
    @Upsert
    suspend fun updateJob(job: SyncJobEntity)

    /** Keep the newest few jobs per user — the screen shows the running or the last one. */
    @Query(
        "DELETE FROM sync_jobs WHERE userId = :userId AND id NOT IN " +
            "(SELECT id FROM sync_jobs WHERE userId = :userId ORDER BY id DESC LIMIT :keep)",
    )
    suspend fun trimJobs(userId: Long, keep: Int)
}
