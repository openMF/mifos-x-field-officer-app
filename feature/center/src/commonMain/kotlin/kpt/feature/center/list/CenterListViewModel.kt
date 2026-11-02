/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.center.list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.center.CenterRepository
import kpt.core.data.offline.OfflineSyncRepository
import kpt.core.model.center.Center
import kpt.core.model.offline.DownloadProgress
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.SyncTarget
import kpt.core.model.offline.toDownloadProgress
import kotlin.time.Clock

/**
 * The center list — ported from `6b66e8a43:feature/center/centerList/ui/CenterListViewModel.kt`.
 *
 * ## What the port drops
 *
 * The original paged manually: `loadCentersPaged(limit, offset)` into a mutable list, a
 * `isLastPage` flag, and a `loadMore()` the screen called from a scroll listener. It also carried
 * the same `userStatus`-flag read-path branch the client list had — `if (userStatus)
 * loadCentersFromDb() else loadCentersFromApi()`.
 *
 * `centersStream()` serves cache-first with a background refresh, so both the branch and the
 * hand-rolled paging state leave this screen. If server-side paging is wanted back, it belongs in
 * the Store as a `PagingScreenStream`, not as list bookkeeping in a ViewModel — which is exactly
 * the mistake that made the original's `isLastPage` disagree with its list after a refresh.
 *
 * Mirrors [kpt.feature.client.list.ClientListViewModel] deliberately: same stream subscription,
 * same derived-view state, same `.catch` guard. Two list screens over the same substrate that
 * behave differently are two screens to debug.
 */
internal class CenterListViewModel(
    repository: CenterRepository,
    private val downloads: OfflineSyncRepository,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : BaseViewModel<CenterListState, CenterListEvent, CenterListAction>(CenterListState()) {

    private val stream = repository.centersStream(viewModelScope)

    /**
     * The rows to render — the stream combined with the screen's own filter state.
     *
     * `stateIn(..., WhileSubscribed, ...)`, NOT an `init{}` collector. The first cut of this file
     * hand-lifted the stream with `.onEach { mutableStateFlow.value = … }.launchIn(viewModelScope)`,
     * which the template forbids in as many words ("the VM never hand-lifts a Flow into
     * ScreenState") — and the reason is not style. `launchIn(viewModelScope)` collects for the
     * ViewModel's WHOLE LIFETIME, so a tab the user is not looking at keeps its Store subscription
     * and its background refreshes alive. With four tabs that is four live refresh loops where
     * there should be one. `WhileSubscribed` stops the upstream 5s after the last collector
     * detaches, and restarts it on return.
     *
     * The filter state is COMBINED in rather than folded into the data: it is UI state the screen
     * also reads directly (to draw the chips), so it belongs on `stateFlow`, not inside the
     * content.
     */
    val screenState: StateFlow<ScreenState<List<Center>>> =
        stream.state.applyFilters(stateFlow)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** Re-run the read. Wired to `ScreenContent`'s retry. */
    fun onRetry() = stream.retry()

    /** Watching the background job for the progress dialog. Closing the dialog stops watching, never the job. */
    private var watch: Job? = null

    /**
     * The original's long-press SELECTION + Sync: the selected centres go to Settings → Offline sync's
     * background job (their groups, clients and every account come with them), so leaving this list
     * does not stop the download. The dialog follows the job from its durable row. Nothing runs until
     * the officer asks.
     */
    private fun downloadSelected() {
        val rows = (screenState.value as? ScreenState.Content)?.data.orEmpty()
        val targets = rows.mapNotNull { c ->
            c.id?.takeIf { it in state.selected }?.let { SyncTarget(RecordKind.CENTRE, it, c.name ?: "#$it") }
        }
        if (targets.isEmpty() || watch?.isActive == true) return
        mutableStateFlow.value = state.copy(selected = emptySet(), download = DownloadProgress(total = targets.size))
        val requestedAt = nowMs()
        watch = viewModelScope.launch {
            downloads.download(targets)
            downloads.latestJob.filterNotNull().filter { it.startedAtMs >= requestedAt }.collect { job ->
                mutableStateFlow.value = state.copy(download = job.toDownloadProgress())
            }
        }
    }

    override fun handleAction(action: CenterListAction) {
        when (action) {
            is CenterListAction.QueryChanged ->
                mutableStateFlow.value = state.copy(query = action.value)

            is CenterListAction.SortChanged ->
                mutableStateFlow.value = state.copy(sort = action.value)

            is CenterListAction.OfficeFilterToggled ->
                mutableStateFlow.value = state.copy(
                    selectedOffices = state.selectedOffices.toggle(action.value),
                )

            CenterListAction.FiltersCleared ->
                mutableStateFlow.value = state.copy(selectedOffices = emptySet(), query = "")

            CenterListAction.FilterVisibilityToggled ->
                mutableStateFlow.value = state.copy(filtersVisible = !state.filtersVisible)

            // In selection mode a tap toggles the row, as in the original; otherwise it opens it.
            is CenterListAction.CenterClicked -> onCenterClicked(action.centerId)

            CenterListAction.CreateCenterClicked -> sendEvent(CenterListEvent.CreateCenter)

            CenterListAction.Retry -> onRetry()

            is CenterListAction.SelectionToggled ->
                mutableStateFlow.value = state.copy(selected = state.selected.toggle(action.centerId))
            CenterListAction.SelectionCleared -> mutableStateFlow.value = state.copy(selected = emptySet())
            CenterListAction.DownloadSelected -> downloadSelected()
            CenterListAction.DownloadCancelled -> {
                watch?.cancel()
                viewModelScope.launch { downloads.cancel() }
                mutableStateFlow.value = state.copy(download = null)
            }
            CenterListAction.DownloadDismissed -> {
                watch?.cancel()
                mutableStateFlow.value = state.copy(download = null)
            }
        }
    }

    private fun onCenterClicked(centerId: Int) {
        if (state.selected.isNotEmpty()) {
            handleAction(CenterListAction.SelectionToggled(centerId))
        } else {
            sendEvent(CenterListEvent.OpenCenter(centerId))
        }
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

    private companion object {
        /** Template default — long enough to survive a rotation or a brief tab switch. */
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * Filter, search and sort, as a PURE function of the loaded rows and the screen's filter state.
 *
 * Kept out of the ViewModel so it stays unit-testable without a ViewModel or a dispatcher —
 * `CenterListStateTest` exercises it directly. A filter matching nothing becomes [ScreenState.Empty]
 * rather than a Content of zero rows: otherwise "no match for this search" renders identically to
 * "this officer services no centers", and only one of those is worth clearing a filter over.
 */
internal fun Flow<ScreenState<List<Center>>>.applyFilters(
    ui: Flow<CenterListState>,
): Flow<ScreenState<List<Center>>> =
    combineContent(ui) { rows, filters, _ -> rows.filteredFor(filters) }
        .emptyIfContent { it.isEmpty() }

/** The rows [ui] keeps, in [ui]'s order. Loading/Error never reach here — [combineContent] passes them through. */
internal fun List<Center>.filteredFor(ui: CenterListState): List<Center> =
    filter { center ->
        val officeOk = ui.selectedOffices.isEmpty() || center.officeName in ui.selectedOffices
        val queryOk = ui.query.isBlank() ||
            center.name?.contains(ui.query, ignoreCase = true) == true ||
            center.accountNo?.contains(ui.query, ignoreCase = true) == true
        officeOk && queryOk
    }
        .let { filtered ->
            when (ui.sort) {
                CenterSort.Name -> filtered.sortedBy { it.name?.lowercase() }
                CenterSort.AccountNumber -> filtered.sortedBy { it.accountNo }
                null -> filtered
            }
        }

/**
 * The screen's own UI state — what the FILTER CHIPS show. The loaded rows are NOT here; they live
 * on [CenterListViewModel.screenState], which is the stream. Holding both in one object is what
 * made the first cut hand-lift the stream into it on every emission.
 */
internal data class CenterListState(
    val query: String = "",
    val sort: CenterSort? = null,
    val selectedOffices: Set<String> = emptySet(),
    val filtersVisible: Boolean = false,
    /** Centers selected by long-press, for download. Non-empty = selection mode. */
    val selected: Set<Int> = emptySet(),
    /** The download in progress (or just finished); null when there is none to show. */
    val download: DownloadProgress? = null,
)

internal enum class CenterSort { Name, AccountNumber }

internal sealed interface CenterListAction {
    data class QueryChanged(val value: String) : CenterListAction
    data class SortChanged(val value: CenterSort?) : CenterListAction
    data class OfficeFilterToggled(val value: String) : CenterListAction
    data object FiltersCleared : CenterListAction
    data object FilterVisibilityToggled : CenterListAction
    data class CenterClicked(val centerId: Int) : CenterListAction
    data object CreateCenterClicked : CenterListAction
    data class SelectionToggled(val centerId: Int) : CenterListAction
    data object SelectionCleared : CenterListAction
    data object DownloadSelected : CenterListAction
    data object DownloadCancelled : CenterListAction
    data object DownloadDismissed : CenterListAction
    data object Retry : CenterListAction
}

internal sealed interface CenterListEvent {
    data class OpenCenter(val centerId: Int) : CenterListEvent
    data object CreateCenter : CenterListEvent
}
