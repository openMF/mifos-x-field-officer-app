/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.searchrecord.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.searchrecord.SearchRecordRepository
import kpt.core.model.objects.searchrecord.GenericSearchRecord
import kpt.core.model.objects.searchrecord.RecordType
import kpt.core.model.objects.searchrecord.SearchRecordKey
import kpt.feature.searchrecord.generated.resources.Res
import kpt.feature.searchrecord.generated.resources.error_searching_records
import org.jetbrains.compose.resources.StringResource

/**
 * Ported from `6b66e8a43:feature/search-record/.../SearchRecordViewModel.kt`. Same state, actions,
 * events and 300 ms debounce; two changes:
 *
 *  - The data path. The original collected `repository.searchRecords(type, query)` — a fresh
 *    `Flow<Result<…>>` per query, from a datasource that read Room directly. Here ONE store-backed
 *    stream is fed the debounced query as a KEY, and [screen] is DERIVED from it with
 *    `stateIn(WhileSubscribed)` — no `init {}` collector, so nothing runs while the screen is away.
 *  - The title. The original set `displayTitle = "Address"` / `"Identifiers"` — hardcoded English
 *    beside its own unused `search_record_address` / `search_record_identifiers` resources. The
 *    state now carries the [RecordType] and the screen resolves the title from those resources.
 */
@OptIn(FlowPreview::class)
internal class SearchRecordViewModel(
    savedStateHandle: SavedStateHandle,
    repository: SearchRecordRepository,
) : BaseViewModel<SearchRecordInput, SearchRecordEvent, SearchRecordAction>(
    initialState = SearchRecordInput(
        recordType = RecordType.valueOf(requireNotNull(savedStateHandle.get<String>("type"))),
    ),
) {

    /**
     * The query once typing has paused for [SEARCH_DEBOUNCE_DELAY_MS] — the KEY the stream reads.
     * Shared (one debounce, read by both the stream and [screen]) and `WhileSubscribed`, like
     * every stream in this app: nothing runs while the screen is not collected.
     */
    private val settledQuery: StateFlow<String> = stateFlow
        .map { it.searchQuery.trim() }
        .distinctUntilChanged()
        .debounce(SEARCH_DEBOUNCE_DELAY_MS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), "")

    /** A blank box searches nothing: the store is asked only for a real query. */
    private val searchKeys = settledQuery.filter { it.isNotBlank() }.map { SearchRecordKey(state.recordType, it) }

    private val stream = repository.searchStream(searchKeys, viewModelScope)

    /** What the screen draws — derived from the input, the settled query and the stream; never assigned. */
    val screen: StateFlow<SearchRecordState> =
        // `onStart(Loading)`: the stream emits nothing until the first non-blank KEY, and `combine`
        // waits for every source — without it the screen froze at its initial value and the
        // search box could not show the first character typed (device, 2026-10-24).
        combine(stateFlow, settledQuery, stream.state.onStart { emit(ScreenState.Loading) }, ::toScreen)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
                SearchRecordState(recordType = state.recordType),
            )

    override fun handleAction(action: SearchRecordAction) {
        when (action) {
            // A new query re-arms the error dialog the officer dismissed for the previous one.
            is SearchRecordAction.SearchQueryChanged ->
                mutableStateFlow.value = state.copy(searchQuery = action.query, errorDismissed = false)
            SearchRecordAction.ClearSearch ->
                mutableStateFlow.value = state.copy(searchQuery = "", errorDismissed = false)
            SearchRecordAction.NavigateBack -> sendEvent(SearchRecordEvent.NavigateBack)
            is SearchRecordAction.SelectRecord -> sendEvent(SearchRecordEvent.NavigateToRecord(action.record))
            SearchRecordAction.CloseDialog -> mutableStateFlow.value = state.copy(errorDismissed = true)
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY_MS = 300L
        private const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * The original screen's model from the three inputs. A blank box is the empty prompt AT ONCE — not
 * after the debounce, and not with the previous query's results still listed. While typing, the
 * last settled query's results stay up until the next settle, as in the original.
 */
private fun toScreen(
    input: SearchRecordInput,
    settledQuery: String,
    results: ScreenState<List<GenericSearchRecord>>,
): SearchRecordState {
    val base = SearchRecordState(recordType = input.recordType, searchQuery = input.searchQuery)
    if (input.searchQuery.isBlank() || settledQuery.isBlank()) return base
    return when (results) {
        is ScreenState.Content -> base.copy(searchRecords = results.data, isNoResultsFound = results.data.isEmpty())
        ScreenState.Empty -> base.copy(isNoResultsFound = true)
        ScreenState.Loading -> base.copy(dialogState = SearchRecordState.DialogState.Loading)
        // A local-only read has no network to lose; any failure is reported as the error it is.
        else -> if (input.errorDismissed) {
            base
        } else {
            base.copy(
                dialogState = SearchRecordState.DialogState.Error(
                    message = (results as? ScreenState.Error)?.error?.message.orEmpty(),
                    messageRes = Res.string.error_searching_records,
                ),
            )
        }
    }
}

/** What the officer has entered — the VM's own state. Everything drawn is derived from it. */
internal data class SearchRecordInput(
    val recordType: RecordType,
    val searchQuery: String = "",
    val errorDismissed: Boolean = false,
)

internal data class SearchRecordState(
    val recordType: RecordType,
    val searchQuery: String = "",
    val searchRecords: List<GenericSearchRecord> = emptyList(),
    val dialogState: DialogState? = null,
    val isNoResultsFound: Boolean = false,
) {
    sealed interface DialogState {
        data object Loading : DialogState
        data class Error(val message: String, val messageRes: StringResource? = null) : DialogState
    }
}

internal sealed interface SearchRecordEvent {
    data object NavigateBack : SearchRecordEvent
    data class NavigateToRecord(val record: GenericSearchRecord) : SearchRecordEvent
}

internal sealed interface SearchRecordAction {
    data class SearchQueryChanged(val query: String) : SearchRecordAction
    data object ClearSearch : SearchRecordAction
    data object NavigateBack : SearchRecordAction
    data class SelectRecord(val record: GenericSearchRecord) : SearchRecordAction
    data object CloseDialog : SearchRecordAction
}
