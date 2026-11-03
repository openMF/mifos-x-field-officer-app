/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.data.offline.OfflineSyncRepository
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.RefreshSettings
import kpt.core.model.offline.SyncJob
import kpt.core.model.offline.SyncTarget

/**
 * In-memory Offline sync. Every call is recorded in [calls] (`"download:CENTRE:10"`, `"syncNow"`, …)
 * so a test asserts what the screen asked for; [records] / [latestJob] are set directly to drive the
 * screen through its states.
 */
@ExperimentalDataTestingApi
class FakeOfflineSyncRepository(
    records: List<OfflineRecord> = emptyList(),
    job: SyncJob? = null,
    refresh: RefreshSettings = RefreshSettings(),
) : OfflineSyncRepository {
    override val records = MutableStateFlow(records)
    override val latestJob = MutableStateFlow(job)
    override val refresh = MutableStateFlow(refresh)

    /** The calls made, in order (`syncNow`, `download:<kind>:<id>`, …). */
    val calls = mutableListOf<String>()

    override suspend fun download(targets: List<SyncTarget>) {
        targets.forEach { calls += "download:${it.kind}:${it.id}" }
        val existing = this.records.value.associateBy { it.kind to it.id }
        this.records.value = this.records.value + targets.filter { (it.kind to it.id) !in existing }
            .map { OfflineRecord(it.kind, it.id, it.name, chosen = true, status = RecordSyncStatus.WAITING) }
    }

    override suspend fun syncNow() {
        calls += "syncNow"
    }

    override suspend fun refreshRecord(target: SyncTarget) {
        calls += "refresh:${target.kind}:${target.id}"
    }

    override suspend fun retryFailed() {
        calls += "retryFailed"
    }

    override suspend fun cancel() {
        calls += "cancel"
    }

    override suspend fun remove(target: SyncTarget) {
        calls += "remove:${target.kind}:${target.id}"
        records.value = records.value.filterNot {
            (it.kind == target.kind && it.id == target.id) ||
                (it.parentKind == target.kind && it.parentId == target.id)
        }
    }

    override suspend fun setRefresh(settings: RefreshSettings) {
        calls += "setRefresh:${settings.mode}:${settings.intervalHours}:${settings.wifiOnly}:${settings.chargingOnly}"
        refresh.value = settings
    }

    override suspend fun runPending(refreshAll: Boolean) {
        calls += "runPending:$refreshAll"
    }
}
