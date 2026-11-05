/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.offline.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kpt.core.data.offline.OfflineSyncRepository
import kpt.core.data.offline.OfflineSyncScheduler
import kpt.core.database.offline.dao.OfflineSyncDao
import kpt.core.datastore.offline.OfflineSyncSettingsStore
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.offline.DownloadFailure
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.RefreshSettings
import kpt.core.model.offline.SyncJob
import kpt.core.model.offline.SyncJobStatus
import kpt.core.model.offline.SyncTarget
import kpt.core.store.offline.OfflineRecordKey
import kpt.core.store.offline.SyncJobKey
import kpt.core.store.offline.toModel
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse

/**
 * Room-backed and keyed by the signed-in user. Wired in `ForkRepositoryModule` because the engine's
 * stores are grouped into holders there (a constructor of 28 stores is not a readable contract).
 *
 * Reads come straight from [dao]; keyed writes go through the three local stores in
 * `core/store/offline` ([recordStore], [statusStore], [jobStore]). The two set-based statements —
 * every SYNCING record back to WAITING, and trimming old jobs — stay single DAO statements: a keyed
 * store would split each into a read and N writes, and lose the atomic `WHERE`.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStoreApi::class)
internal class OfflineSyncRepositoryImpl(
    private val dao: OfflineSyncDao,
    private val recordStore: MutableStore<OfflineRecordKey, OfflineRecord>,
    private val statusStore: MutableStore<OfflineRecordKey, RecordSyncStatus>,
    private val jobStore: MutableStore<SyncJobKey, SyncJob>,
    private val engine: OfflineSyncEngine,
    private val scheduler: OfflineSyncScheduler,
    private val settingsStore: OfflineSyncSettingsStore,
    private val preferences: ForkPreferencesRepository,
    private val nowMs: () -> Long,
) : OfflineSyncRepository {

    /** One job at a time in this process, whichever trigger started it. */
    private val running = Mutex()

    private val userId: Long get() = preferences.fineractUser.value.userId

    private val userIds = preferences.fineractUser.map { it.userId }.distinctUntilChanged()

    override val records: Flow<List<OfflineRecord>> =
        userIds.flatMapLatest { dao.observeRecords(it) }.map { rows -> rows.mapNotNull { it.toModel() } }

    override val latestJob: Flow<SyncJob?> = userIds.flatMapLatest { dao.observeLatestJob(it) }.map { it?.toModel() }

    override val refresh: StateFlow<RefreshSettings> = settingsStore.settings

    override suspend fun download(targets: List<SyncTarget>) {
        val user = userId
        for (target in targets) {
            val existing = dao.record(user, target.kind.name, target.id)?.toModel()
            recordStore.put(
                OfflineRecordKey(user, target.kind.name, target.id),
                (existing ?: OfflineRecord(target.kind, target.id, target.name))
                    .copy(chosen = true, status = RecordSyncStatus.WAITING),
            )
        }
        scheduler.runNow()
    }

    override suspend fun syncNow() {
        val user = userId
        dao.records(user).filter { it.chosen }.forEach { setStatus(user, it.kind, it.id, RecordSyncStatus.WAITING) }
        scheduler.runNow()
    }

    override suspend fun refreshRecord(target: SyncTarget) {
        setStatus(userId, target.kind.name, target.id, RecordSyncStatus.WAITING)
        scheduler.runNow()
    }

    override suspend fun retryFailed() {
        val user = userId
        dao.records(user).filter { it.chosen && it.status == RecordSyncStatus.FAILED.name }
            .forEach { setStatus(user, it.kind, it.id, RecordSyncStatus.WAITING) }
        scheduler.runNow()
    }

    override suspend fun cancel() {
        scheduler.cancelRun()
        stopRunningJob(userId)
    }

    override suspend fun remove(target: SyncTarget) {
        val user = userId
        // A centre's groups bring their clients; remove those two levels deep, keeping anything chosen.
        dao.children(user, target.kind.name, target.id).forEach { child ->
            if (!child.chosen) removeUnchosenChildren(user, child.kind, child.id)
        }
        removeUnchosenChildren(user, target.kind.name, target.id)
        recordStore.clear(OfflineRecordKey(user, target.kind.name, target.id))
    }

    override suspend fun setRefresh(settings: RefreshSettings) {
        settingsStore.update(settings)
        scheduler.schedule(settings)
    }

    override suspend fun runPending(refreshAll: Boolean) = running.withLock {
        val user = userId
        // We hold the one-run lock, so a RUNNING row here is a run whose process died (the platform
        // re-runs the work after a kill). Close it and put its records back in the queue.
        stopRunningJob(user)
        if (refreshAll) {
            dao.records(user).filter { it.chosen }.forEach { setStatus(user, it.kind, it.id, RecordSyncStatus.WAITING) }
        }
        while (true) {
            val pending = dao.records(user).filter { it.chosen && it.status == RecordSyncStatus.WAITING.name }
                .mapNotNull { it.toModel()?.target }
            if (pending.isEmpty()) break
            runJob(user, pending)
        }
    }

    private suspend fun runJob(user: Long, targets: List<SyncTarget>) {
        // Id 0: the write inserts the row and Room assigns its id. Only runJob inserts jobs, and it runs
        // under [running], so the user's newest job right after that write is this one.
        jobStore.put(
            SyncJobKey(user, 0),
            SyncJob(id = 0, status = SyncJobStatus.RUNNING, targets = targets, startedAtMs = nowMs()),
        )
        var job = checkNotNull(dao.observeLatestJob(user).first()?.toModel()) { "the sync job was not saved" }
        targets.forEach { setStatus(user, it.kind.name, it.id, RecordSyncStatus.SYNCING) }
        val progress = Mutex()
        val failures = mutableListOf<DownloadFailure>()
        suspend fun save(change: SyncJob.() -> SyncJob) {
            job = job.change()
            jobStore.put(SyncJobKey(user, job.id), job)
        }
        val sink = object : SyncSink {
            override suspend fun read(label: String) = progress.withLock {
                save { copy(reads = reads + 1, currentLabel = label) }
            }

            override suspend fun failed(failure: DownloadFailure) = progress.withLock {
                failures += failure
                save { copy(failures = failures.toList()) }
            }

            override suspend fun record(record: OfflineRecord) = progress.withLock { upsertKeepingChoice(user, record) }
        }
        val run = SyncRun()
        try {
            engine.syncGlobals(sink)
            for (target in targets) {
                val record = engine.sync(target, sink, run)
                progress.withLock {
                    upsertKeepingChoice(user, record.copy(chosen = true))
                    save { copy(done = done + 1) }
                }
            }
            progress.withLock {
                save { copy(status = SyncJobStatus.FINISHED, currentLabel = null, finishedAtMs = nowMs()) }
            }
            dao.trimJobs(user, KEEP_JOBS)
        } catch (cancellation: CancellationException) {
            withContext(NonCancellable) { stopRunningJob(user) }
            throw cancellation
        }
    }

    /** A record reached two ways (chosen AND under a centre) stays chosen — and keeps its own parent. */
    private suspend fun upsertKeepingChoice(user: Long, record: OfflineRecord) {
        val chosen = dao.record(user, record.kind.name, record.id)?.toModel()?.takeIf { it.chosen }
        recordStore.put(
            OfflineRecordKey(user, record.kind.name, record.id),
            chosen?.let {
                record.copy(
                    chosen = true,
                    parentKind = it.parentKind,
                    parentId = it.parentId,
                    parentName = it.parentName,
                )
            } ?: record,
        )
    }

    private suspend fun removeUnchosenChildren(user: Long, parentKind: String, parentId: Int) {
        dao.children(user, parentKind, parentId).filterNot { it.chosen }
            .forEach { recordStore.clear(OfflineRecordKey(user, it.kind, it.id)) }
    }

    private suspend fun setStatus(user: Long, kind: String, id: Int, status: RecordSyncStatus) =
        statusStore.put(OfflineRecordKey(user, kind, id), status)

    private suspend fun stopRunningJob(user: Long) {
        dao.runningJob(user)?.toModel()?.let {
            jobStore.put(
                SyncJobKey(user, it.id),
                it.copy(status = SyncJobStatus.CANCELLED, currentLabel = null, finishedAtMs = nowMs()),
            )
        }
        dao.moveStatus(user, RecordSyncStatus.SYNCING.name, RecordSyncStatus.WAITING.name)
    }

    private companion object {
        const val KEEP_JOBS = 5
    }
}

/**
 * Writes through the store and FAILS like the DAO call it replaces. Store5 reports a failed write as a
 * [StoreWriteResponse.Error] instead of throwing, which would make a sync that could not save its
 * progress look like one that did.
 */
private suspend fun <K : Any, V : Any> MutableStore<K, V>.put(key: K, value: V) {
    when (val response = write(StoreWriteRequest.of<K, V, Any>(key = key, value = value))) {
        is StoreWriteResponse.Error.Exception -> throw response.error
        is StoreWriteResponse.Error.Message -> error(response.message)
        is StoreWriteResponse.Success -> Unit
    }
}
