/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.offline

/** The three levels an officer can keep on the device (Settings → Offline sync). */
enum class RecordKind { CENTRE, GROUP, CLIENT }

/** One record the officer chose to keep on the device. */
data class SyncTarget(val kind: RecordKind, val id: Int, val name: String)

enum class RecordSyncStatus { WAITING, SYNCING, SYNCED, FAILED }

/**
 * A record held on the device — chosen directly ([chosen]) or brought in through a parent
 * ([parentKind]/[parentId], "via Centro Arin"). Counts describe what came down under it.
 */
data class OfflineRecord(
    val kind: RecordKind,
    val id: Int,
    val name: String,
    val parentKind: RecordKind? = null,
    val parentId: Int? = null,
    val parentName: String? = null,
    val chosen: Boolean = false,
    val groups: Int = 0,
    val clients: Int = 0,
    val loans: Int = 0,
    val savings: Int = 0,
    val documents: Int = 0,
    val status: RecordSyncStatus = RecordSyncStatus.WAITING,
    val refreshedAtMs: Long? = null,
    val failedCount: Int = 0,
) {
    val target: SyncTarget get() = SyncTarget(kind, id, name)

    /** Synced, but longer ago than [maxAgeMs] — the refresh interval, or a day on manual. */
    fun isOutOfDate(nowMs: Long, maxAgeMs: Long): Boolean =
        status == RecordSyncStatus.SYNCED && refreshedAtMs != null && nowMs - refreshedAtMs > maxAgeMs

    fun needsSync(nowMs: Long, maxAgeMs: Long): Boolean =
        status == RecordSyncStatus.WAITING || status == RecordSyncStatus.FAILED || isOutOfDate(nowMs, maxAgeMs)
}

enum class SyncJobStatus { RUNNING, FINISHED, CANCELLED }

/**
 * One background download. [done]/[total] count the chosen records; [reads] every server read so far
 * (the job discovers a record's tree as it walks it, so a read total is not known up front).
 */
data class SyncJob(
    val id: Long,
    val status: SyncJobStatus,
    val targets: List<SyncTarget>,
    val done: Int = 0,
    val total: Int = targets.size,
    val reads: Int = 0,
    val currentLabel: String? = null,
    val failures: List<DownloadFailure> = emptyList(),
    val startedAtMs: Long,
    val finishedAtMs: Long? = null,
)

/** The job as the centre / group lists' progress dialog draws it (`core/ui` OfflineDownload). */
fun SyncJob.toDownloadProgress(): DownloadProgress = DownloadProgress(
    completed = done,
    total = total,
    current = currentLabel,
    records = reads,
    failures = failures,
    finished = status != SyncJobStatus.RUNNING,
)

enum class RefreshMode { MANUAL, AUTOMATIC }

/** How the chosen records are kept current. Only the CHOSEN records ever sync, in either mode. */
data class RefreshSettings(
    val mode: RefreshMode = RefreshMode.MANUAL,
    val intervalHours: Int = 6,
    val wifiOnly: Boolean = true,
    val chargingOnly: Boolean = false,
) {
    /** Age past which a synced record shows "Out of date". */
    val maxAgeMs: Long
        get() = (if (mode == RefreshMode.AUTOMATIC) intervalHours else MANUAL_STALE_HOURS) * HOUR_MS

    companion object {
        val INTERVALS: List<Int> = listOf(1, 6, 12, 24)
        const val MANUAL_STALE_HOURS: Int = 24
        const val HOUR_MS: Long = 3_600_000L
    }
}
