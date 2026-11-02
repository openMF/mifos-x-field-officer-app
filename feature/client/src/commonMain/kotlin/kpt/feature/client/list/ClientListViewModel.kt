/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientRepository
import kpt.core.model.client.Client

/**
 * The client list — ported from `6b66e8a43:feature/client/clientsList/ClientListViewModel.kt`.
 *
 * NOTE the plural: the original also shipped a `clientList` (singular) package whose 76-line
 * ViewModel had **zero references** anywhere. It is deliberately not ported (`MIGRATION.md` §1);
 * porting it would have manufactured a `G-IMPL-DUP` violation by copying a defect faithfully.
 *
 * **The manual read-path branch is gone.** The original opened with:
 *
 * ```kotlin
 * val userStatus = userPreferencesRepository.userInfo.first().userStatus
 * if (userStatus) processClientsFromDb() else processClientsFromApi()
 * ```
 *
 * — two code paths for one read, chosen by a persisted flag, each with its own `DataState` fold and
 * its own error handling. `clientsStream()` serves cache-first with a background refresh, so both
 * collapse into one subscription and the `userStatus` read leaves this screen entirely. That is the
 * `DataState` → `ScreenState` change deleting a code path, not renaming a type.
 */
internal class ClientListViewModel(
    repository: ClientRepository,
) : BaseViewModel<ClientListState, ClientListEvent, ClientListAction>(ClientListState()) {

    private val stream = repository.clientsStream(viewModelScope)

    /**
     * The rows to render — the stream combined with this screen's filter state.
     *
     * `stateIn(… WhileSubscribed …)`, not an `init{}` collector. The hand-lift this replaced
     * collected for the ViewModel's whole lifetime, so the Clients tab kept its Store subscription
     * and background refreshes alive while the user was on another tab.
     */
    val screenState: StateFlow<ScreenState<List<Client>>> =
        stream.state.applyFilters(stateFlow)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** Re-run the read. Wired to `ScreenContent`'s retry. */
    fun onRetry() = stream.retry()

    override fun handleAction(action: ClientListAction) {
        when (action) {
            is ClientListAction.QueryChanged ->
                mutableStateFlow.value = state.copy(query = action.value)

            is ClientListAction.SortChanged ->
                mutableStateFlow.value = state.copy(sort = action.value)

            is ClientListAction.StatusFilterToggled ->
                mutableStateFlow.value = state.copy(
                    selectedStatuses = state.selectedStatuses.toggle(action.value),
                )

            is ClientListAction.OfficeFilterToggled ->
                mutableStateFlow.value = state.copy(
                    selectedOffices = state.selectedOffices.toggle(action.value),
                )

            ClientListAction.FiltersCleared ->
                mutableStateFlow.value = state.copy(
                    selectedStatuses = emptySet(),
                    selectedOffices = emptySet(),
                    query = "",
                )

            ClientListAction.FilterVisibilityToggled ->
                mutableStateFlow.value = state.copy(filtersVisible = !state.filtersVisible)

            is ClientListAction.ClientClicked ->
                sendEvent(ClientListEvent.OpenClient(action.clientId))

            ClientListAction.CreateClientClicked ->
                sendEvent(ClientListEvent.CreateClient)

            ClientListAction.Retry -> onRetry()
            // Pull-to-refresh: a FORCED fetch. Cache-first alone would re-serve the list until its TTL
            // ran out, so a client deleted on the server stayed listed (and opened, and 404'd on
            // every write) with no way for the officer to see the server's truth.
            ClientListAction.Refresh -> stream.refresh(forceFresh = true)
        }
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

    private companion object {
        /** Template default — long enough to survive a rotation or a brief tab switch. */
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * The screen's own UI state — what the search field and filter chips show. The loaded rows are NOT
 * here; they live on [ClientListViewModel.screenState], which is the stream.
 */
internal data class ClientListState(
    val query: String = "",
    val sort: ClientSort? = null,
    val selectedStatuses: Set<String> = emptySet(),
    val selectedOffices: Set<String> = emptySet(),
    val filtersVisible: Boolean = false,
)

/**
 * Filter, search and sort, as a PURE function of the loaded rows and the filter state.
 *
 * The original kept `clients` and `unfilteredClients` side by side, sorted one and filtered the
 * other — so sorting then filtering silently discarded the sort. One source and a derived view
 * cannot drift. A filter matching nothing is [ScreenState.Empty], not a Content of zero rows:
 * otherwise "no match" renders identically to "this officer has no clients".
 */
internal fun Flow<ScreenState<List<Client>>>.applyFilters(
    ui: Flow<ClientListState>,
): Flow<ScreenState<List<Client>>> =
    combineContent(ui) { rows, filters, _ -> rows.filteredFor(filters) }
        .emptyIfContent { it.isEmpty() }

/** The rows [ui] keeps, in [ui]'s order. Loading/Error never reach here — [combineContent] passes them through. */
internal fun List<Client>.filteredFor(ui: ClientListState): List<Client> =
    filter { client ->
        val statusOk = ui.selectedStatuses.isEmpty() || client.status?.value in ui.selectedStatuses
        val officeOk = ui.selectedOffices.isEmpty() || client.officeName in ui.selectedOffices
        val queryOk = ui.query.isBlank() ||
            client.displayName?.contains(ui.query, ignoreCase = true) == true ||
            client.accountNo?.contains(ui.query, ignoreCase = true) == true
        statusOk && officeOk && queryOk
    }
        .let { filtered ->
            when (ui.sort) {
                ClientSort.Name -> filtered.sortedBy { it.displayName?.lowercase() }
                ClientSort.AccountNumber -> filtered.sortedBy { it.accountNo }
                ClientSort.ExternalId -> filtered.sortedBy { it.externalId }
                null -> filtered
            }
        }

internal enum class ClientSort { Name, AccountNumber, ExternalId }

internal sealed interface ClientListAction {
    /** The pull gesture — the original's `refreshClientList`. */
    data object Refresh : ClientListAction
    data class QueryChanged(val value: String) : ClientListAction
    data class SortChanged(val value: ClientSort?) : ClientListAction
    data class StatusFilterToggled(val value: String) : ClientListAction
    data class OfficeFilterToggled(val value: String) : ClientListAction
    data object FiltersCleared : ClientListAction
    data object FilterVisibilityToggled : ClientListAction
    data class ClientClicked(val clientId: Int) : ClientListAction
    data object CreateClientClicked : ClientListAction
    data object Retry : ClientListAction
}

internal sealed interface ClientListEvent {
    data class OpenClient(val clientId: Int) : ClientListEvent
    data object CreateClient : ClientListEvent
}
