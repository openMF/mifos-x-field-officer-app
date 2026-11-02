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
    val userId: Long,
    /** `RecordKind` name — CENTRE, GROUP or CLIENT. */
    val kind: String,
    val id: Int,
    val name: String,
    val parentKind: String?,
    val parentId: Int?,
    val parentName: String?,
    val chosen: Boolean,
    val groups: Int,
    val clients: Int,
    val loans: Int,
    val savings: Int,
    val documents: Int,
    /** `RecordSyncStatus` name. */
    val status: String,
    val refreshedAtMs: Long?,
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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    /** `SyncJobStatus` name. */
    val status: String,
    val targets: String,
    val done: Int,
    val total: Int,
    val reads: Int,
    val currentLabel: String?,
    val failures: String,
    val startedAtMs: Long,
    val finishedAtMs: Long?,
)
