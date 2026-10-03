/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.list

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientRepository
import kpt.core.database.client.entity.ClientEntity

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
    private val repository: ClientRepository,
) : BaseViewModel<ClientListState, ClientListEvent, ClientListAction>(ClientListState()) {

    init {
        repository.clientsStream(viewModelScope).state
            .onEach { screenState -> mutableStateFlow.value = state.withClients(screenState) }
            // A read that fails in the stream itself (not inside the Store) would otherwise cancel
            // the ViewModel's scope silently and leave the screen on its last state forever.
            .catch { Logger.e(tag = "ClientList", throwable = it) { "clientsStream failed" } }
            .launchIn(viewModelScope)
    }

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
        }
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value
}

/**
 * Screen state.
 *
 * Search, sort and filtering are DERIVED from [all] rather than stored as a second list. The original
 * kept `clients` and `unfilteredClients` side by side, sorted `clients` and filtered
 * `unfilteredClients` — so sorting then filtering silently discarded the sort, and the two could
 * disagree about what the user was looking at. One source plus a derived view cannot drift.
 */
internal data class ClientListState(
    val all: ScreenState<List<ClientEntity>> = ScreenState.Loading,
    val query: String = "",
    val sort: ClientSort? = null,
    val selectedStatuses: Set<String> = emptySet(),
    val selectedOffices: Set<String> = emptySet(),
    val filtersVisible: Boolean = false,
) {
    /** Office names present in the loaded data — the filter's options, never a separate fetch. */
    val officeNames: List<String>
        get() = contentOrEmpty().mapNotNull { it.officeName }.distinct().sorted()

    /** The rows to render: filtered, searched, then sorted, in that order and only here. */
    val visible: List<ClientEntity>
        get() = contentOrEmpty()
            .filter { client ->
                val statusOk = selectedStatuses.isEmpty() || client.status?.value in selectedStatuses
                val officeOk = selectedOffices.isEmpty() || client.officeName in selectedOffices
                val queryOk = query.isBlank() ||
                    client.displayName?.contains(query, ignoreCase = true) == true ||
                    client.accountNo?.contains(query, ignoreCase = true) == true
                statusOk && officeOk && queryOk
            }
            .let { rows ->
                when (sort) {
                    ClientSort.Name -> rows.sortedBy { it.displayName?.lowercase() }
                    ClientSort.AccountNumber -> rows.sortedBy { it.accountNo }
                    ClientSort.ExternalId -> rows.sortedBy { it.externalId }
                    null -> rows
                }
            }

    /**
     * What the list area should render.
     *
     * A filter that matches nothing is [ScreenState.Empty] even when [all] carries content — the
     * original rendered an empty list, indistinguishable from "this office has no clients".
     */
    val listState: ScreenState<List<ClientEntity>>
        get() = when {
            all is ScreenState.Content && visible.isEmpty() -> ScreenState.Empty
            all is ScreenState.Content -> ScreenState.Content(visible)
            else -> all
        }

    private fun contentOrEmpty(): List<ClientEntity> =
        (all as? ScreenState.Content)?.data.orEmpty()

    fun withClients(next: ScreenState<List<ClientEntity>>) = copy(all = next)
}

internal enum class ClientSort { Name, AccountNumber, ExternalId }

internal sealed interface ClientListAction {
    data class QueryChanged(val value: String) : ClientListAction
    data class SortChanged(val value: ClientSort?) : ClientListAction
    data class StatusFilterToggled(val value: String) : ClientListAction
    data class OfficeFilterToggled(val value: String) : ClientListAction
    data object FiltersCleared : ClientListAction
    data object FilterVisibilityToggled : ClientListAction
    data class ClientClicked(val clientId: Int) : ClientListAction
    data object CreateClientClicked : ClientListAction
}

internal sealed interface ClientListEvent {
    data class OpenClient(val clientId: Int) : ClientListEvent
    data object CreateClient : ClientListEvent
}
