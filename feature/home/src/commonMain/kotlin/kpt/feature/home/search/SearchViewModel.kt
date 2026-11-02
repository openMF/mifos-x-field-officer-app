/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.search

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.search.SearchRepository
import kpt.core.model.objects.SearchedEntity
import kpt.core.model.search.SearchResourcesKey

/**
 * Fineract resource search — the Home tab's content.
 *
 * Ported from `6b66e8a43:feature/search/.../SearchViewModel.kt`. The original app had no Home tab and
 * used Search as its de-facto dashboard; adopting the template backbone (spec §6) makes Search the
 * Home body, supplied through `BackboneRegistry.homeBody`.
 *
 * The original's `DataState` fold — `onStart { Loading }` / `catch { Error }` / `onEach { … }` across
 * three branches — collapses into [ScreenState], which the substrate already produces with
 * Empty, NoNetwork and stale-while-revalidate the original could not express.
 */
class SearchViewModel(
    private val repository: SearchRepository,
) : BaseViewModel<SearchState, SearchEvent, SearchAction>(SearchState()) {

    /**
     * The query actually SUBMITTED, as opposed to the one being typed.
     *
     * Search is explicit here (a Search button, as in the original) rather than search-as-you-type,
     * so typing must not fire requests. `null` means nothing has been submitted yet.
     */
    private val submitted = MutableStateFlow<SearchResourcesKey?>(null)

    /**
     * Results for the submitted query.
     *
     * `flatMapLatest` + `stateIn(… WhileSubscribed …)` replaces a hand-managed `searchJob` that was
     * cancelled and relaunched on every submit, with each launch hand-lifting into
     * `mutableStateFlow`. `flatMapLatest` IS that cancellation, declared rather than performed —
     * a new key automatically abandons the previous stream, so an in-flight search can no longer
     * deliver results for a query the user has already replaced.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<ScreenState<List<SearchedEntity>>> = submitted
        .flatMapLatest { key ->
            if (key == null) {
                flowOf(ScreenState.Empty)
            } else {
                // No hits is Empty — the stream reports it as an empty Content.
                repository.searchStream(key, viewModelScope).state.emptyIfContent { it.isEmpty() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Empty)

    override fun handleAction(action: SearchAction) {
        when (action) {
            is SearchAction.QueryChanged ->
                mutableStateFlow.value = state.copy(query = action.value)

            is SearchAction.FilterChanged -> {
                mutableStateFlow.value = state.copy(filter = action.value)
                search()
            }

            SearchAction.ExactMatchToggled -> {
                mutableStateFlow.value = state.copy(exactMatch = !state.exactMatch)
                search()
            }

            SearchAction.Clear -> {
                // Clearing the submitted key is what cancels the in-flight search — `flatMapLatest`
                // abandons the previous stream when the key changes.
                submitted.value = null
                mutableStateFlow.value = SearchState()
            }

            SearchAction.Submit -> search()
        }
    }

    /**
     * Runs the search, or reports a blank query without touching the network.
     *
     * The Fineract `search` endpoint treats an empty `query` as unfiltered and would return the whole
     * directory; the original guarded this with a `showEmptyError` flag, and the guard is preserved.
     */
    private fun search() {
        val query = state.query.trim()
        if (query.isEmpty()) {
            submitted.value = null
            mutableStateFlow.value = state.copy(blankQuery = true)
            return
        }
        mutableStateFlow.value = state.copy(blankQuery = false)

        submitted.value = SearchResourcesKey(
            query = query,
            resource = state.filter?.apiValue,
            exactMatch = state.exactMatch,
        )
    }

    private companion object {
        /** Template default — long enough to survive a rotation or a brief tab switch. */
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * A search filter.
 *
 * [apiValue] is a plain constant, NOT a string resource. The original declared it as
 * `valueRes: StringResource` alongside the label and sent `getString(valueRes)` to the server — so
 * translating `feature_search_filter_options_clients_value` into any other language would have
 * silently broken the query. Only [labelRes] is translatable; the wire value is not text the user
 * reads.
 */
enum class SearchFilter(val apiValue: String) {
    Clients("clients"),
    Groups("groups"),
    LoanAccounts("loans"),
    SavingsAccounts("savings"),
}

data class SearchState(
    val query: String = "",
    val filter: SearchFilter? = null,
    val exactMatch: Boolean = false,
    /** True when Submit was pressed with nothing typed — the original's `showEmptyError`. */
    val blankQuery: Boolean = false,
)

sealed interface SearchAction {
    data class QueryChanged(val value: String) : SearchAction
    data class FilterChanged(val value: SearchFilter?) : SearchAction
    data object ExactMatchToggled : SearchAction
    data object Clear : SearchAction
    data object Submit : SearchAction
}

sealed interface SearchEvent
