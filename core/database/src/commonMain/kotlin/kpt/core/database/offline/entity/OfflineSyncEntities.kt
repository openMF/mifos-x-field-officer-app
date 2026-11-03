/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.offline.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * A centre, group or client held on the device for Offline sync — chosen by the officer or brought
 * in through a parent. Keyed by user as well as record, so on a shared field device one officer's
 * inventory is never shown to the next.
 */
@DbEntity
@Entity(tableName = "offline_records", primaryKeys = ["userId", "kind", "id"])
data class OfflineRecordEntity(
    /** The server id of the user. */
    val userId: Long,
    /** `RecordKind` name — CENTRE, GROUP or CLIENT. */
    val kind: String,
    /** The server id of this record. */
    val id: Int,
    /** The display name. */
    val name: String,
    /** The kind of the parent record. */
    val parentKind: String?,
    /** The server id of the parent record. */
    val parentId: Int?,
    /** The name of the parent record. */
    val parentName: String?,
    /** True when the user picked this record for offline use. */
    val chosen: Boolean,
    /** The number of groups downloaded with the record. */
    val groups: Int,
    /** The number of clients downloaded with the record. */
    val clients: Int,
    /** The number of loans downloaded with the record. */
    val loans: Int,
    /** The number of savings accounts downloaded with the record. */
    val savings: Int,
    /** The number of documents downloaded with the record. */
    val documents: Int,
    /** `RecordSyncStatus` name. */
    val status: String,
    /** When the record was last refreshed from the server, in epoch milliseconds. */
    val refreshedAtMs: Long?,
    /** The number of items that failed to download. */
    val failedCount: Int,
)

/**
 * One background download — durable so its progress survives the screen and the process.
 * [targets] and [failures] are line-encoded (`KIND\tid\tname`, `item\treason`): they are only ever
 * read back whole, so a child table would be ceremony.
 */
@DbEntity
@Entity(tableName = "sync_jobs")
data class SyncJobEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** The server id of the user. */
    val userId: Long,
    /** `SyncJobStatus` name. */
    val status: String,
    /** The job's targets, one `KIND\tid\tname` line each. */
    val targets: String,
    /** The number of items processed so far. */
    val done: Int,
    /** The number of items the job will process. */
    val total: Int,
    /** The number of server reads the job made. */
    val reads: Int,
    /** What the sync job is working on now, for the progress line. */
    val currentLabel: String?,
    /** The failed items, one `item\treason` line each. */
    val failures: String,
    /** When the job started, in epoch milliseconds. */
    val startedAtMs: Long,
    /** When the job finished, in epoch milliseconds; null while running. */
    val finishedAtMs: Long?,
)
