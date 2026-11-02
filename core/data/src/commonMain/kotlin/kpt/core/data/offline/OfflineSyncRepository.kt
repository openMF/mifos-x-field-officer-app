/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.offline

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RefreshSettings
import kpt.core.model.offline.SyncJob
import kpt.core.model.offline.SyncTarget

/**
 * Settings → Offline sync: the officer chooses centres, groups or clients and a durable background
 * job downloads each one's whole tree, so every screen of those records opens with no network.
 * DOWNLOAD-ONLY — work done offline is saved as drafts and uploaded by Sync & Drafts.
 *
 * Everything here is local and observed: the worker writes job progress and per-record status to the
 * database, and the dashboard re-renders from it live — after leaving the screen or a restart too.
 */
interface OfflineSyncRepository {
    /** Everything held on the device, at all three levels, for the signed-in officer. */
    val records: Flow<List<OfflineRecord>>

    /** The running job, else the last one (its result card), else null. */
    val latestJob: Flow<SyncJob?>

    val refresh: StateFlow<RefreshSettings>

    /** Keep [targets] on the device and download them now (picker, list long-press). */
    suspend fun download(targets: List<SyncTarget>)

    /** Re-download every chosen record. */
    suspend fun syncNow()

    /** Re-download one chosen record. */
    suspend fun refreshRecord(target: SyncTarget)

    /** Re-download the records whose last download had refused reads. */
    suspend fun retryFailed()

    /** Stop the running job; what it already finished stays on the device. */
    suspend fun cancel()

    /** Stop keeping [target] (and what came in only through it) on the device. */
    suspend fun remove(target: SyncTarget)

    /** Save Manual / Automatic and (re)schedule — or cancel — the one periodic job. */
    suspend fun setRefresh(settings: RefreshSettings)

    /**
     * The background job's body: download every chosen record waiting for it, until none is left.
     * [refreshAll] (the periodic run) first marks every chosen record for re-download.
     */
    suspend fun runPending(refreshAll: Boolean)
}

/** Where the work runs — the background scheduler, so a download survives the screen and the process. */
interface OfflineSyncScheduler {
    suspend fun runNow()
    suspend fun cancelRun()
    suspend fun schedule(settings: RefreshSettings)
}
