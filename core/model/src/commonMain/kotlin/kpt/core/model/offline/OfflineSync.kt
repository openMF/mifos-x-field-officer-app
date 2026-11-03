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

/**
 * The three levels an officer can keep on the device (Settings → Offline sync).
 *
 * @property CENTRE A centre
 * @property CLIENT A client
 * @property GROUP A group
 * @property CENTRE A centre
 * @property CLIENT A client
 * @property GROUP A group
 */
enum class RecordKind {
    /** A centre. */
    CENTRE,

    /** A group. */
    GROUP,

    /** A client. */
    CLIENT,
}

/**
 * One record the officer chose to keep on the device.
 *
 * @property id the server id of this record
 * @property kind the kind of record
 * @property name the display name
 */
data class SyncTarget(val kind: RecordKind, val id: Int, val name: String)

/**
 * Lifecycle status of a record sync.
 *
 * @property FAILED the last sync attempt failed
 * @property SYNCED synced
 * @property SYNCING syncing now
 * @property WAITING waiting to sync
 */
enum class RecordSyncStatus {
    /** Queued, not yet sent. */
    WAITING,

    /** Being sent now. */
    SYNCING,

    /** Sent and accepted. */
    SYNCED,

    /** Sent and rejected, or the send failed. */
    FAILED,
}

/**
 * A record held on the device — chosen directly ([chosen]) or brought in through a parent
 * ([parentKind]/[parentId], "via Centro Arin"). Counts describe what came down under it.
 */
data class OfflineRecord(
    /** The kind of record. */
    val kind: RecordKind,
    /** The server id of this record. */
    val id: Int,
    /** The display name. */
    val name: String,
    /** The kind of the parent this record came in through, when it was not chosen directly. */
    val parentKind: RecordKind? = null,
    /** The id of that parent. */
    val parentId: Int? = null,
    /** The name of the parent record. */
    val parentName: String? = null,
    /** True when the officer chose this record directly. */
    val chosen: Boolean = false,
    /** The number of groups downloaded with the record. */
    val groups: Int = 0,
    /** The number of clients downloaded with the record. */
    val clients: Int = 0,
    /** The number of loans downloaded with the record. */
    val loans: Int = 0,
    /** The number of savings accounts downloaded with the record. */
    val savings: Int = 0,
    /** The number of documents downloaded with the record. */
    val documents: Int = 0,
    /** The record's Fineract status. */
    val status: RecordSyncStatus = RecordSyncStatus.WAITING,
    /** When the record was last refreshed from the server, in epoch milliseconds. */
    val refreshedAtMs: Long? = null,
    /** The number of items that failed to download. */
    val failedCount: Int = 0,
) {
    /** The sync state of the record. */
    val target: SyncTarget get() = SyncTarget(kind, id, name)

    /** Synced, but longer ago than [maxAgeMs] — the refresh interval, or a day on manual. */
    fun isOutOfDate(nowMs: Long, maxAgeMs: Long): Boolean =
        status == RecordSyncStatus.SYNCED && refreshedAtMs != null && nowMs - refreshedAtMs > maxAgeMs

    /** True when the record is unsent, failed, or older than `maxAgeMs`. */
    fun needsSync(nowMs: Long, maxAgeMs: Long): Boolean =
        status == RecordSyncStatus.WAITING || status == RecordSyncStatus.FAILED || isOutOfDate(nowMs, maxAgeMs)
}

/**
 * Lifecycle status of a sync job.
 *
 * @property CANCELLED cancelled before it finished
 * @property FINISHED finished
 * @property RUNNING running now
 */
enum class SyncJobStatus {
    /** The job is still sending records. */
    RUNNING,

    /** Every record was tried. */
    FINISHED,

    /** The user stopped the job. */
    CANCELLED,
}

/**
 * One background download. [done]/[total] count the chosen records; [reads] every server read so far
 * (the job discovers a record's tree as it walks it, so a read total is not known up front).
 */
data class SyncJob(
    /** The server id of this record. */
    val id: Long,
    /** The record's Fineract status. */
    val status: SyncJobStatus,
    /** The records the job syncs, with their state. */
    val targets: List<SyncTarget>,
    /** The chosen records synced so far. */
    val done: Int = 0,
    /** The number of chosen records. */
    val total: Int = targets.size,
    /** The server reads made so far. */
    val reads: Int = 0,
    /** What the sync job is working on now, for the progress line. */
    val currentLabel: String? = null,
    /** The items that failed to download, with why. */
    val failures: List<DownloadFailure> = emptyList(),
    /** When the job started, in epoch milliseconds. */
    val startedAtMs: Long,
    /** When the job finished, in epoch milliseconds; null while running. */
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

/**
 * Refresh mode.
 *
 * @property AUTOMATIC refresh in the background on a schedule
 * @property MANUAL refresh only when the user asks
 */
enum class RefreshMode {
    /** Refresh only when the user asks. */
    MANUAL,

    /** Refresh on the configured schedule. */
    AUTOMATIC,
}

/** How the chosen records are kept current. Only the CHOSEN records ever sync, in either mode. */
data class RefreshSettings(
    /** How refreshing is triggered. */
    val mode: RefreshMode = RefreshMode.MANUAL,
    /** How often to refresh, in hours. */
    val intervalHours: Int = 6,
    /** True to refresh only on Wi-Fi. */
    val wifiOnly: Boolean = true,
    /** True to refresh only while the device is charging. */
    val chargingOnly: Boolean = false,
) {
    /** Age past which a synced record shows "Out of date". */
    val maxAgeMs: Long
        get() = (if (mode == RefreshMode.AUTOMATIC) intervalHours else MANUAL_STALE_HOURS) * HOUR_MS

    /** Constants and factories of `RefreshSettings`. */
    companion object {
        /** The refresh intervals the user may choose from, in hours. */
        val INTERVALS: List<Int> = listOf(1, 6, 12, 24)

        /** How many hours before manually refreshed data counts as stale. */
        const val MANUAL_STALE_HOURS: Int = 24

        /** One hour, in milliseconds. */
        const val HOUR_MS: Long = 3_600_000L
    }
}
