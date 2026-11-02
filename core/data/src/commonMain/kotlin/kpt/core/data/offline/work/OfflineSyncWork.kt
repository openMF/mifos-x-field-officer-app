/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(io.github.mobilebytelabs.worker.ExperimentalWorkerApi::class)

package kpt.core.data.offline.work

import io.github.mobilebytelabs.worker.Constraints
import io.github.mobilebytelabs.worker.CoroutineWorker
import io.github.mobilebytelabs.worker.ExistingPeriodicWorkPolicy
import io.github.mobilebytelabs.worker.ExistingWorkPolicy
import io.github.mobilebytelabs.worker.NetworkType
import io.github.mobilebytelabs.worker.WorkManager
import io.github.mobilebytelabs.worker.WorkResult
import io.github.mobilebytelabs.worker.WorkerContext
import io.github.mobilebytelabs.worker.oneTimeWorkRequest
import io.github.mobilebytelabs.worker.periodicWorkRequest
import io.github.mobilebytelabs.worker.workDataOf
import kpt.core.data.offline.OfflineSyncRepository
import kpt.core.data.offline.OfflineSyncScheduler
import kpt.core.model.offline.RefreshMode
import kpt.core.model.offline.RefreshSettings
import kotlin.time.Duration.Companion.hours

/**
 * The background body of Settings → Offline sync. It survives leaving the screen and process death
 * (the platform re-runs it), and it drains every chosen record waiting for a download — so a record
 * chosen WHILE a job runs is picked up by the same run rather than lost to the KEEP policy.
 *
 * Registered in `cmp-shared/ForkWorkerDeclarations.kt`; its constructor params are Koin-autowired by
 * the worker-kmp codegen, so they stay public types.
 */
public class OfflineSyncWorker(
    context: WorkerContext,
    private val repository: OfflineSyncRepository,
) : CoroutineWorker(context) {

    override suspend fun doWork(): WorkResult {
        repository.runPending(refreshAll = inputData.getBoolean(KEY_REFRESH_ALL, false))
        return WorkResult.success()
    }

    public companion object {
        internal const val KEY_REFRESH_ALL: String = "offline_sync.refresh_all"
    }
}

/**
 * [OfflineSyncScheduler] over worker-kmp: ONE unique run at a time (KEEP — the running job drains
 * new work itself), and ONE unique periodic job for Automatic, updated in place when the interval or
 * constraints change, cancelled on Manual.
 */
internal class WorkManagerOfflineSyncScheduler(private val workManager: WorkManager) : OfflineSyncScheduler {

    override suspend fun runNow() {
        val request = oneTimeWorkRequest<OfflineSyncWorker> {
            setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            addTag(RUN_NAME)
        }
        workManager.enqueueUniqueWork(RUN_NAME, ExistingWorkPolicy.KEEP, request)
    }

    override suspend fun cancelRun() {
        workManager.cancelAllWorkByTag(RUN_NAME)
    }

    override suspend fun schedule(settings: RefreshSettings) {
        if (settings.mode == RefreshMode.MANUAL) {
            workManager.cancelAllWorkByTag(PERIODIC_NAME)
            return
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (settings.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresCharging(settings.chargingOnly)
            .build()
        val request = periodicWorkRequest<OfflineSyncWorker>(settings.intervalHours.hours) {
            setConstraints(constraints)
            setInputData(workDataOf(OfflineSyncWorker.KEY_REFRESH_ALL to true))
            addTag(PERIODIC_NAME)
        }
        workManager.enqueueUniquePeriodicWork(PERIODIC_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private companion object {
        const val RUN_NAME = "kpt.offline-sync"
        const val PERIODIC_NAME = "kpt.offline-refresh"
    }
}
