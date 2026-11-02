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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kpt.core.data.offline.OfflineSyncRepository
import kpt.core.data.offline.OfflineSyncScheduler
import kpt.core.database.offline.dao.OfflineSyncDao
import kpt.core.database.offline.entity.OfflineRecordEntity
import kpt.core.database.offline.entity.SyncJobEntity
import kpt.core.datastore.offline.OfflineSyncSettingsStore
import kpt.core.datastore.prefs.ProjectPreferencesRepository
import kpt.core.model.offline.DownloadFailure
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.RefreshSettings
import kpt.core.model.offline.SyncJob
import kpt.core.model.offline.SyncJobStatus
import kpt.core.model.offline.SyncTarget

/**
 * Room-backed and keyed by the signed-in user. Wired in `ProjectRepositoryModule` because the engine's
 * stores are grouped into holders there (a constructor of 28 stores is not a readable contract).
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class OfflineSyncRepositoryImpl(
    private val dao: OfflineSyncDao,
    private val engine: OfflineSyncEngine,
    private val scheduler: OfflineSyncScheduler,
    private val settingsStore: OfflineSyncSettingsStore,
    private val preferences: ProjectPreferencesRepository,
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
            val existing = dao.record(user, target.kind.name, target.id)
            dao.upsertRecord(
                (existing ?: OfflineRecord(target.kind, target.id, target.name).toEntity(user))
                    .copy(chosen = true, status = RecordSyncStatus.WAITING.name),
            )
        }
        scheduler.runNow()
    }

    override suspend fun syncNow() {
        val user = userId
        dao.records(user).filter { it.chosen }.forEach {
            dao.setStatus(
                user,
                it.kind,
                it.id,
                RecordSyncStatus.WAITING.name,
            )
        }
        scheduler.runNow()
    }

    override suspend fun refreshRecord(target: SyncTarget) {
        dao.setStatus(userId, target.kind.name, target.id, RecordSyncStatus.WAITING.name)
        scheduler.runNow()
    }

    override suspend fun retryFailed() {
        val user = userId
        dao.records(user).filter { it.chosen && it.status == RecordSyncStatus.FAILED.name }
            .forEach { dao.setStatus(user, it.kind, it.id, RecordSyncStatus.WAITING.name) }
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
            if (!child.chosen) dao.deleteChildren(user, child.kind, child.id)
        }
        dao.deleteChildren(user, target.kind.name, target.id)
        dao.deleteRecord(user, target.kind.name, target.id)
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
            dao.records(user).filter { it.chosen }.forEach {
                dao.setStatus(
                    user,
                    it.kind,
                    it.id,
                    RecordSyncStatus.WAITING.name,
                )
            }
        }
        while (true) {
            val pending = dao.records(user).filter { it.chosen && it.status == RecordSyncStatus.WAITING.name }
                .mapNotNull { it.toModel()?.target }
            if (pending.isEmpty()) break
            runJob(user, pending)
        }
    }

    private suspend fun runJob(user: Long, targets: List<SyncTarget>) {
        val started = nowMs()
        var job = SyncJobEntity(
            userId = user, status = SyncJobStatus.RUNNING.name, targets = targets.encodeTargets(),
            done = 0, total = targets.size, reads = 0, currentLabel = null, failures = "",
            startedAtMs = started, finishedAtMs = null,
        )
        job = job.copy(id = dao.insertJob(job))
        targets.forEach { dao.setStatus(user, it.kind.name, it.id, RecordSyncStatus.SYNCING.name) }
        val progress = Mutex()
        val failures = mutableListOf<DownloadFailure>()
        val sink = object : SyncSink {
            override suspend fun read(label: String) = progress.withLock {
                job = job.copy(reads = job.reads + 1, currentLabel = label)
                dao.updateJob(job)
            }

            override suspend fun failed(failure: DownloadFailure) = progress.withLock {
                failures += failure
                job = job.copy(failures = failures.encodeFailures())
                dao.updateJob(job)
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
                    job = job.copy(done = job.done + 1)
                    dao.updateJob(job)
                }
            }
            progress.withLock {
                job = job.copy(status = SyncJobStatus.FINISHED.name, currentLabel = null, finishedAtMs = nowMs())
                dao.updateJob(job)
            }
            dao.trimJobs(user, KEEP_JOBS)
        } catch (cancellation: CancellationException) {
            withContext(NonCancellable) { stopRunningJob(user) }
            throw cancellation
        }
    }

    /** A record reached two ways (chosen AND under a centre) stays chosen — and keeps its own parent. */
    private suspend fun upsertKeepingChoice(user: Long, record: OfflineRecord) {
        val existing = dao.record(user, record.kind.name, record.id)
        val row = record.toEntity(user)
        dao.upsertRecord(
            if (existing?.chosen == true) {
                row.copy(
                    chosen = true,
                    parentKind = existing.parentKind,
                    parentId = existing.parentId,
                    parentName = existing.parentName,
                )
            } else {
                row
            },
        )
    }

    private suspend fun stopRunningJob(user: Long) {
        dao.runningJob(user)?.let {
            dao.updateJob(
                it.copy(
                    status = SyncJobStatus.CANCELLED.name,
                    currentLabel = null,
                    finishedAtMs = nowMs(),
                ),
            )
        }
        dao.moveStatus(user, RecordSyncStatus.SYNCING.name, RecordSyncStatus.WAITING.name)
    }

    private companion object {
        const val KEEP_JOBS = 5
    }
}

private fun OfflineRecord.toEntity(userId: Long) = OfflineRecordEntity(
    userId = userId, kind = kind.name, id = id, name = name,
    parentKind = parentKind?.name, parentId = parentId, parentName = parentName, chosen = chosen,
    groups = groups, clients = clients, loans = loans, savings = savings, documents = documents,
    status = status.name, refreshedAtMs = refreshedAtMs, failedCount = failedCount,
)

private fun OfflineRecordEntity.toModel(): OfflineRecord? {
    val kind = RecordKind.entries.firstOrNull { it.name == kind } ?: return null
    return OfflineRecord(
        kind = kind, id = id, name = name,
        parentKind = RecordKind.entries.firstOrNull { it.name == parentKind },
        parentId = parentId,
        parentName = parentName,
        chosen = chosen, groups = groups, clients = clients, loans = loans, savings = savings, documents = documents,
        status = RecordSyncStatus.entries.firstOrNull { it.name == status } ?: RecordSyncStatus.WAITING,
        refreshedAtMs = refreshedAtMs, failedCount = failedCount,
    )
}

private fun SyncJobEntity.toModel(): SyncJob? {
    val status = SyncJobStatus.entries.firstOrNull { it.name == status } ?: return null
    return SyncJob(
        id = id, status = status, targets = targets.decodeTargets(), done = done, total = total, reads = reads,
        currentLabel = currentLabel,
        failures = failures.decodeFailures(),
        startedAtMs = startedAtMs,
        finishedAtMs = finishedAtMs,
    )
}

// Line-encoded (see SyncJobEntity): tabs and newlines inside a name or a server message are flattened
// to spaces first, so a field can never split a row.
private fun String.flat() = replace('\t', ' ').replace('\n', ' ')

internal fun List<SyncTarget>.encodeTargets(): String =
    joinToString("\n") { "${it.kind.name}\t${it.id}\t${it.name.flat()}" }

internal fun String.decodeTargets(): List<SyncTarget> = lineSequence().mapNotNull { line ->
    val parts = line.split('\t', limit = 3)
    val kind = RecordKind.entries.firstOrNull { it.name == parts.getOrNull(0) } ?: return@mapNotNull null
    val id = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
    SyncTarget(kind, id, parts.getOrNull(2).orEmpty())
}.toList()

internal fun List<DownloadFailure>.encodeFailures(): String =
    joinToString("\n") { "${it.item.flat()}\t${it.reason?.flat().orEmpty()}" }

internal fun String.decodeFailures(): List<DownloadFailure> = lineSequence().filter { it.isNotBlank() }.map { line ->
    val parts = line.split('\t', limit = 2)
    DownloadFailure(parts[0], parts.getOrNull(1)?.takeIf { it.isNotEmpty() })
}.toList()
