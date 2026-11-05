/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.offline

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.offline.dao.OfflineSyncDao
import kpt.core.database.offline.entity.OfflineRecordEntity
import kpt.core.database.offline.entity.SyncJobEntity
import kpt.core.model.offline.DownloadFailure
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.SyncJob
import kpt.core.model.offline.SyncJobStatus
import kpt.core.model.offline.SyncTarget
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth

/*
 * The WRITE half of Offline sync's own bookkeeping — which records are on the device and the jobs that
 * downloaded them. Both tables are LOCAL state the server knows nothing about, so each store is
 * `createOfflineMutableStore`: the write lands in Room with no network leg. `OfflineSyncRepository`
 * writes through these and never calls a DAO write itself (S5-1); it still READS the tables straight
 * from the DAO, as `WatchlistRepositoryImpl` does in the template.
 *
 * `logout = false` on all three: a `MutableStore` sits outside `StoreCacheManager`'s purge, and these
 * rows are keyed by user, so a shared field device never shows one officer's inventory to the next.
 */

/**
 * One `offline_records` row.
 *
 * @property userId the signed-in user the row belongs to
 * @property kind the `RecordKind` name, as the table stores it
 * @property id the record's server id
 */
data class OfflineRecordKey(val userId: Long, val kind: String, val id: Int)

/**
 * One `sync_jobs` row.
 *
 * @property userId the user who ran the job
 * @property id the job's row id; `0` for a job not written yet — Room assigns the id on that write
 */
data class SyncJobKey(val userId: Long, val id: Long)

/** Store writing one record of the user's offline inventory. */
// logout-exempt: local-only MutableStore keyed by user; outside the StoreCacheManager purge by design
@StoreProvider(id = "offlineRecord", logout = false)
fun provideOfflineRecordStore(
    dao: OfflineSyncDao,
): MutableStore<OfflineRecordKey, OfflineRecord> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: OfflineRecordKey -> dao.observeRecords(key.userId).map { rows -> rows.find(key)?.toModel() } },
        writer = { key: OfflineRecordKey, record: OfflineRecord -> dao.upsertRecord(record.toEntity(key.userId)) },
        delete = { key: OfflineRecordKey -> dao.deleteRecord(key.userId, key.kind, key.id) },
    ),
)

/**
 * Store writing ONE column of a record — its sync status. A separate store, not a read-modify-write of
 * [provideOfflineRecordStore], because the running job rewrites the whole row concurrently: a status
 * change must not put back the counts the job just wrote.
 */
// logout-exempt: local-only MutableStore keyed by user; outside the StoreCacheManager purge by design
@StoreProvider(id = "offlineRecordStatus", logout = false)
fun provideOfflineRecordStatusStore(
    dao: OfflineSyncDao,
): MutableStore<OfflineRecordKey, RecordSyncStatus> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: OfflineRecordKey ->
            dao.observeRecords(key.userId).map { rows ->
                rows.find(key)?.let { row -> RecordSyncStatus.entries.firstOrNull { it.name == row.status } }
            }
        },
        writer = { key: OfflineRecordKey, status: RecordSyncStatus ->
            dao.setStatus(key.userId, key.kind, key.id, status.name)
        },
    ),
)

/** Store writing one background download job. */
// logout-exempt: local-only MutableStore keyed by user; outside the StoreCacheManager purge by design
@StoreProvider(id = "syncJob", logout = false)
fun provideSyncJobStore(
    dao: OfflineSyncDao,
): MutableStore<SyncJobKey, SyncJob> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: SyncJobKey ->
            dao.observeLatestJob(key.userId).map { row -> row?.takeIf { it.id == key.id }?.toModel() }
        },
        // An upsert: id 0 inserts a new row (Room generates the id), any other id replaces that row.
        writer = { key: SyncJobKey, job: SyncJob -> dao.updateJob(job.toEntity(key.userId)) },
    ),
)

private fun List<OfflineRecordEntity>.find(key: OfflineRecordKey) =
    firstOrNull { it.kind == key.kind && it.id == key.id }

/** The row for this record, owned by [userId]. */
fun OfflineRecord.toEntity(userId: Long) = OfflineRecordEntity(
    userId = userId, kind = kind.name, id = id, name = name,
    parentKind = parentKind?.name, parentId = parentId, parentName = parentName, chosen = chosen,
    groups = groups, clients = clients, loans = loans, savings = savings, documents = documents,
    status = status.name, refreshedAtMs = refreshedAtMs, failedCount = failedCount,
)

/** The record this row holds; null for a row whose kind this build does not know. */
fun OfflineRecordEntity.toModel(): OfflineRecord? {
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

/** The row for this job, owned by [userId]. */
fun SyncJob.toEntity(userId: Long) = SyncJobEntity(
    id = id, userId = userId, status = status.name, targets = targets.encodeTargets(),
    done = done, total = total, reads = reads, currentLabel = currentLabel, failures = failures.encodeFailures(),
    startedAtMs = startedAtMs, finishedAtMs = finishedAtMs,
)

/** The job this row holds; null for a status this build does not know. */
fun SyncJobEntity.toModel(): SyncJob? {
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
