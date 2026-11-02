/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.datatable.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.datatable.DataTableRepository
import kpt.core.model.datatable.DataTable

/**
 * The datatables registered against one application table — ported from
 * `6b66e8a43:feature/data-table/dataTable/DataTableViewModel.kt`.
 *
 * Reached from group detail (`groupNavGraph(loadGroupDataTables = …)`). Client reaches it too in
 * the original, but only via the dead `clientDetailRoute`; it will reach it properly when
 * create-client lands.
 *
 * ## `isRefreshing` was never observably true — the same bug as `feature/document`
 *
 * `refresh()` emitted `true`, called a SUSPENDING load, then set `false`. Because the load
 * suspended inside the same coroutine the flag was flipped back only after it finished — so the
 * spinner did show here, unlike the document list's synchronous version. The stream drives it now
 * either way.
 *
 * ## `ShowEmptyDataTables` is the stream's Empty
 *
 * The original branched on `result.isEmpty()` to pick between two UI states. `isEmpty` is declared
 * on the stream, so `ScreenState.Empty` arrives for free and the branch goes.
 */
internal class DataTableListViewModel(
    repository: DataTableRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, DataTableListEvent, DataTableListAction>(Unit) {

    /**
     * The application table — `m_client`, `m_group`, `m_loan`. The ROUTE calls it `tableName`
     * because that is what the original's route called it, but the API query parameter is
     * `apptable`: it selects which ENTITY KIND's datatables to list, not a datatable by name.
     */
    private val appTable: String = savedStateHandle.get<String>(TABLE_NAME_KEY).orEmpty()

    private val entityId: Int = savedStateHandle.get<Int>(ENTITY_ID_KEY) ?: -1

    private val stream = repository.dataTablesStream(appTable, viewModelScope)

    val dataTables: StateFlow<ScreenState<List<DataTable>>> = stream.state
        // An empty list must be marked Empty, or the `empty = { ... }` slot the screen supplies
        // is unreachable and the officer gets a BLANK body under a "Refreshing…" banner. Found on
        // device 2026-10-23: "More Group Info" fetched `datatables?apptable=m_group` → 200 with no
        // rows and rendered nothing at all, while the screen was carrying the original's own
        // "no datatables" copy the whole time.
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: DataTableListAction) {
        when (action) {
            DataTableListAction.NavigateBack -> sendEvent(DataTableListEvent.NavigateBack)
            DataTableListAction.Refresh -> stream.refresh()

            is DataTableListAction.TableClicked -> sendEvent(
                // The ROW's registered name is the datatable to open; the entity id comes from
                // this screen's own route. The original passed both through a JSON-encoded nav
                // argument — see `DataTableRowsViewModel`.
                DataTableListEvent.OpenTable(
                    tableName = action.registeredTableName,
                    entityId = entityId,
                ),
            )
        }
    }

    private companion object {
        const val TABLE_NAME_KEY = "tableName"
        const val ENTITY_ID_KEY = "entityId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

internal sealed interface DataTableListEvent {
    data object NavigateBack : DataTableListEvent
    data class OpenTable(val tableName: String, val entityId: Int) : DataTableListEvent
}

internal sealed interface DataTableListAction {
    data object NavigateBack : DataTableListAction
    data object Refresh : DataTableListAction
    data class TableClicked(val registeredTableName: String) : DataTableListAction
}
