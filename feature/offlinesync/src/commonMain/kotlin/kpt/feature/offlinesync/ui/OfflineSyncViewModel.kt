/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.offlinesync.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.center.CenterRepository
import kpt.core.data.client.ClientRepository
import kpt.core.data.group.GroupRepository
import kpt.core.data.offline.OfflineSyncRepository
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.RefreshMode
import kpt.core.model.offline.RefreshSettings
import kpt.core.model.offline.SyncJob
import kpt.core.model.offline.SyncJobStatus
import kpt.core.model.offline.SyncTarget
import kotlin.time.Clock

/**
 * Settings → Offline sync: a live dashboard of everything on the device at three levels, what still
 * needs syncing, the running job, and the Manual / Automatic choice. Download-only — work done
 * offline is a draft, uploaded by Sync & Drafts.
 *
 * Every figure here is LOCAL and observed (records, the latest job, preferences, connectivity), so the
 * worker's progress re-renders the screen as it happens. Only the record picker reads the server.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class OfflineSyncViewModel(
    private val repository: OfflineSyncRepository,
    private val centers: CenterRepository,
    private val groups: GroupRepository,
    private val clients: ClientRepository,
    networkMonitor: NetworkMonitor,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : BaseViewModel<OfflineSyncState, OfflineSyncEvent, OfflineSyncAction>(OfflineSyncState()) {

    init {
        combine(
            repository.records,
            repository.latestJob,
            repository.refresh,
            networkMonitor.isOnline,
        ) { records, job, refresh, online ->
            Snapshot(records, job, refresh, online)
        }.onEach { snapshot ->
            updateState {
                copy(
                    records = snapshot.records,
                    job = snapshot.job,
                    refresh = snapshot.refresh,
                    online = snapshot.online,
                    nowMs = this@OfflineSyncViewModel.nowMs(),
                )
            }
        }.launchIn(viewModelScope)
    }

    // The picker's lists are the officer's own centre / group / client lists (cache-first), opened
    // only once the picker is — the dashboard itself never waits on the network.
    private val centreList by lazy { centers.centersStream(viewModelScope) }
    private val groupList by lazy { groups.groupsStream(viewModelScope) }
    private val clientList by lazy { clients.clientsStream(viewModelScope) }

    /** The picker's active tab as rows; Loading until the picker is open. */
    val pickerRows: StateFlow<ScreenState<List<PickerRow>>> = stateFlow
        .map { it.picker?.tab }
        .distinctUntilChanged()
        .flatMapLatest { tab ->
            when (tab) {
                null -> flowOf(ScreenState.Loading)
                RecordKind.CENTRE -> centreList.state.map { s ->
                    s.mapRows { c ->
                        c.id?.let { PickerRow(SyncTarget(RecordKind.CENTRE, it, c.name ?: "#$it"), c.accountNo) }
                    }
                }
                RecordKind.GROUP -> groupList.state.map { s ->
                    s.mapRows { g ->
                        g.id?.let { PickerRow(SyncTarget(RecordKind.GROUP, it, g.name ?: "#$it"), g.accountNo) }
                    }
                }
                RecordKind.CLIENT -> clientList.state.map { s ->
                    s.mapRows { c ->
                        c.id?.let {
                            PickerRow(SyncTarget(RecordKind.CLIENT, it, c.displayName ?: "#$it"), c.accountNo)
                        }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: OfflineSyncAction) {
        handleSyncAction(action)
        handleListAction(action)
        handlePickerAction(action)
    }

    private fun handleSyncAction(action: OfflineSyncAction) {
        when (action) {
            OfflineSyncAction.NavigateBack -> sendEvent(OfflineSyncEvent.NavigateBack)
            OfflineSyncAction.SyncNow -> if (state.canSync) launch { repository.syncNow() }
            OfflineSyncAction.CancelJob -> launch { repository.cancel() }
            OfflineSyncAction.RetryFailed -> if (state.online) launch { repository.retryFailed() }
            is OfflineSyncAction.SetMode -> setRefresh(state.refresh.copy(mode = action.mode))
            is OfflineSyncAction.SetInterval -> setRefresh(state.refresh.copy(intervalHours = action.hours))
            is OfflineSyncAction.SetWifiOnly -> setRefresh(state.refresh.copy(wifiOnly = action.on))
            is OfflineSyncAction.SetChargingOnly -> setRefresh(state.refresh.copy(chargingOnly = action.on))
            OfflineSyncAction.OpenDrafts -> sendEvent(OfflineSyncEvent.OpenDrafts)
            else -> Unit
        }
    }

    private fun handleListAction(action: OfflineSyncAction) {
        when (action) {
            is OfflineSyncAction.Level -> updateState { copy(level = action.level, search = "") }
            is OfflineSyncAction.ToggleExpand -> updateState {
                val key = action.record.kind to action.record.id
                copy(expanded = if (key in expanded) expanded - key else expanded + key)
            }
            is OfflineSyncAction.Search -> updateState { copy(search = action.query) }
            is OfflineSyncAction.OpenRecord ->
                sendEvent(OfflineSyncEvent.OpenRecord(action.record.kind, action.record.id))
            is OfflineSyncAction.RefreshRecord -> launch { repository.refreshRecord(action.record.target) }
            is OfflineSyncAction.AskRemove -> updateState { copy(confirmRemove = action.record) }
            OfflineSyncAction.DismissRemove -> updateState { copy(confirmRemove = null) }
            OfflineSyncAction.ConfirmRemove -> state.confirmRemove?.let { record ->
                updateState { copy(confirmRemove = null) }
                launch { repository.remove(record.target) }
            }
            else -> Unit
        }
    }

    private fun handlePickerAction(action: OfflineSyncAction) {
        when (action) {
            OfflineSyncAction.AddRecords -> updateState { copy(picker = PickerState()) }
            OfflineSyncAction.ClosePicker -> updateState { copy(picker = null) }
            is OfflineSyncAction.PickerTab -> updatePicker { copy(tab = action.tab, query = "") }
            is OfflineSyncAction.PickerSearch -> updatePicker { copy(query = action.query) }
            is OfflineSyncAction.PickerSelect -> updatePicker {
                copy(selected = if (action.target in selected) selected - action.target else selected + action.target)
            }
            OfflineSyncAction.PickerDownload -> download()
            OfflineSyncAction.PickerRetry -> when (state.picker?.tab) {
                RecordKind.CENTRE -> centreList.retry()
                RecordKind.GROUP -> groupList.retry()
                RecordKind.CLIENT -> clientList.retry()
                null -> Unit
            }
            else -> Unit
        }
    }

    private fun download() {
        val picker = state.picker ?: return
        if (picker.selected.isEmpty() || !state.online) return
        updateState { copy(picker = null) }
        launch { repository.download(picker.selected.toList()) }
    }

    private fun setRefresh(settings: RefreshSettings) {
        launch { repository.setRefresh(settings) }
    }

    private fun updatePicker(change: PickerState.() -> PickerState) = updateState { copy(picker = picker?.change()) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private data class Snapshot(
        val records: List<OfflineRecord>,
        val job: SyncJob?,
        val refresh: RefreshSettings,
        val online: Boolean,
    )

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

private fun <T> ScreenState<List<T>>.mapRows(row: (T) -> PickerRow?): ScreenState<List<PickerRow>> = when (this) {
    is ScreenState.Content -> ScreenState.Content(data.mapNotNull(row), fetchedAt, freshnessSignal)
    is ScreenState.Error -> this
    is ScreenState.NoNetwork -> this
    ScreenState.Loading -> ScreenState.Loading
    ScreenState.Empty -> ScreenState.Empty
    ScreenState.Unauthenticated -> ScreenState.Unauthenticated
}

internal data class OfflineSyncState(
    /** Null until the local tables first emit — the loading state. */
    val records: List<OfflineRecord>? = null,
    val job: SyncJob? = null,
    val refresh: RefreshSettings = RefreshSettings(),
    val online: Boolean = true,
    val level: RecordKind = RecordKind.CENTRE,
    val expanded: Set<Pair<RecordKind, Int>> = emptySet(),
    val search: String = "",
    val confirmRemove: OfflineRecord? = null,
    val picker: PickerState? = null,
    val nowMs: Long = 0,
) {
    val screen: OfflineScreen
        get() = when {
            records == null -> OfflineScreen.LOADING
            job?.status == SyncJobStatus.RUNNING -> OfflineScreen.SYNCING
            records.isEmpty() -> OfflineScreen.EMPTY
            job?.status == SyncJobStatus.FINISHED && job.failures.isNotEmpty() -> OfflineScreen.PARTIAL_FAILURE
            else -> OfflineScreen.CONTENT
        }

    val syncing: Boolean get() = job?.status == SyncJobStatus.RUNNING

    val canSync: Boolean get() = online && !syncing && records.orEmpty().any { it.chosen }

    /** Chosen records still waiting, failed, or older than the refresh setting allows. */
    val toSync: Int get() = records.orEmpty().count { it.chosen && it.needsSync(nowMs, refresh.maxAgeMs) }

    fun count(kind: RecordKind): Int = records.orEmpty().count { it.kind == kind }

    fun hasFailures(kind: RecordKind): Boolean =
        records.orEmpty().any { it.kind == kind && it.status == RecordSyncStatus.FAILED }

    /** The selected level's rows: needs-sync first, then by name; the Clients level filters by [search]. */
    val levelRows: List<OfflineRecord>
        get() = records.orEmpty()
            .filter { it.kind == level }
            .filter { search.isBlank() || it.name.contains(search.trim(), ignoreCase = true) }
            .sortedWith(
                compareBy<OfflineRecord> { if (it.needsSync(nowMs, refresh.maxAgeMs)) 0 else 1 }
                    .thenBy { it.name.lowercase() },
            )

    fun childrenOf(record: OfflineRecord): List<OfflineRecord> =
        records.orEmpty()
            .filter { it.parentKind == record.kind && it.parentId == record.id }
            .sortedBy { it.name.lowercase() }

    val lastSyncedAtMs: Long? get() = records.orEmpty().filter { it.chosen }.mapNotNull { it.refreshedAtMs }.maxOrNull()

    val nextSyncAtMs: Long?
        get() = lastSyncedAtMs
            ?.takeIf { refresh.mode == RefreshMode.AUTOMATIC }
            ?.let { it + refresh.intervalHours * RefreshSettings.HOUR_MS }

    /** Records on the device, so the picker can mark them. */
    fun isOnDevice(target: SyncTarget): Boolean = records.orEmpty().any { it.kind == target.kind && it.id == target.id }
}

internal enum class OfflineScreen { LOADING, CONTENT, SYNCING, PARTIAL_FAILURE, EMPTY }

internal data class PickerState(
    val tab: RecordKind = RecordKind.CENTRE,
    val query: String = "",
    val selected: Set<SyncTarget> = emptySet(),
)

/** One row of the picker: the record and its account number (what the search also matches). */
internal data class PickerRow(val target: SyncTarget, val accountNo: String?)

internal sealed interface OfflineSyncAction {
    data object NavigateBack : OfflineSyncAction
    data object SyncNow : OfflineSyncAction
    data object CancelJob : OfflineSyncAction
    data object RetryFailed : OfflineSyncAction
    data class SetMode(val mode: RefreshMode) : OfflineSyncAction
    data class SetInterval(val hours: Int) : OfflineSyncAction
    data class SetWifiOnly(val on: Boolean) : OfflineSyncAction
    data class SetChargingOnly(val on: Boolean) : OfflineSyncAction
    data class Level(val level: RecordKind) : OfflineSyncAction
    data class ToggleExpand(val record: OfflineRecord) : OfflineSyncAction
    data class Search(val query: String) : OfflineSyncAction
    data class OpenRecord(val record: OfflineRecord) : OfflineSyncAction
    data object OpenDrafts : OfflineSyncAction
    data class RefreshRecord(val record: OfflineRecord) : OfflineSyncAction
    data class AskRemove(val record: OfflineRecord) : OfflineSyncAction
    data object ConfirmRemove : OfflineSyncAction
    data object DismissRemove : OfflineSyncAction
    data object AddRecords : OfflineSyncAction
    data object ClosePicker : OfflineSyncAction
    data class PickerTab(val tab: RecordKind) : OfflineSyncAction
    data class PickerSearch(val query: String) : OfflineSyncAction
    data class PickerSelect(val target: SyncTarget) : OfflineSyncAction
    data object PickerDownload : OfflineSyncAction
    data object PickerRetry : OfflineSyncAction
}

internal sealed interface OfflineSyncEvent {
    data object NavigateBack : OfflineSyncEvent
    data object OpenDrafts : OfflineSyncEvent
    data class OpenRecord(val kind: RecordKind, val id: Int) : OfflineSyncEvent
}
