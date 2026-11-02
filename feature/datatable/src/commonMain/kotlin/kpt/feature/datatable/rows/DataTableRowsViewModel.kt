/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.datatable.rows

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.datatable.DataTableCommandRepository
import kpt.core.data.datatable.DataTableRepository

/**
 * One datatable's rows for one entity — ported from
 * `6b66e8a43:feature/data-table/dataTableData/DataTableDataViewModel.kt`.
 *
 * ## The rows are DERIVED from the JSON, not accumulated during composition
 *
 * The original's screen iterated the `JsonArray` inside the composition body and appended each row
 * to a `rememberSaveable` list:
 *
 * ```kotlin
 * while (jsonElementIterator.hasNext()) { … dataList = dataList + dataTableDataItem }
 * ```
 *
 * Writing state from the composition body re-runs on every recomposition, so the list grew by the
 * whole table each time — duplicating every row and re-triggering composition. The rows are mapped
 * from the array here, which renders the same rows without the growth.
 *
 * ## The nav argument was a JSON blob
 *
 * The original passed `DataTableDataNavigationArg` as a JSON string under a `SavedStateHandle` key
 * and `Json.decodeFromString`-ed it in the ViewModel's constructor — so a malformed or absent
 * argument threw during construction, before the screen could render anything. Two type-safe route
 * fields instead.
 */
internal class DataTableRowsViewModel(
    repository: DataTableRepository,
    private val commands: DataTableCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<DataTableRowsState, DataTableRowsEvent, DataTableRowsAction>(
    DataTableRowsState(
        tableName = savedStateHandle.get<String>(TABLE_NAME_KEY).orEmpty(),
        entityId = savedStateHandle.get<Int>(ENTITY_ID_KEY) ?: -1,
    ),
) {

    private val stream = repository.dataTableDataStream(
        tableName = state.tableName,
        entityId = state.entityId,
        scope = viewModelScope,
    )

    val rows: StateFlow<ScreenState<List<DataTableRow>>> = stream.state
        .mapContent { json, _ -> json.toRows() }
        // An empty list must be marked Empty, or the `empty = { ... }` slot the screen supplies
        // is unreachable and the officer gets a BLANK body under a "Refreshing…" banner. Found on
        // device 2026-10-23: "More Group Info" fetched `datatables?apptable=m_group` → 200 with no
        // rows and rendered nothing at all, while the screen was carrying the original's own
        // "no datatables" copy the whole time.
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: DataTableRowsAction) {
        when (action) {
            DataTableRowsAction.NavigateBack -> sendEvent(DataTableRowsEvent.NavigateBack)
            DataTableRowsAction.Refresh -> stream.refresh()
            DataTableRowsAction.DismissNotice -> mutableStateFlow.value = state.copy(notice = null)
            is DataTableRowsAction.Delete -> delete(action.rowId)
        }
    }

    private fun delete(rowId: Int) {
        if (state.isDeleting) return
        mutableStateFlow.value = state.copy(isDeleting = true, notice = null)

        viewModelScope.launch {
            val result = commands.deleteEntryOfDataTableManyToMany(
                dataTableName = state.tableName,
                entityId = state.entityId,
                dataTableRowId = rowId,
            )
            mutableStateFlow.value = when (result) {
                // No re-read: the command records the delete through the rows' own store, so the
                // stream has already dropped the row. (It used to be cache-free and forced one here.)
                is MutationResult.Applied -> state.copy(isDeleting = false, notice = RowNotice.Deleted)

                is MutationResult.Blocked -> state.copy(
                    isDeleting = false,
                    notice = if (result.reason == BlockReason.OFFLINE) {
                        RowNotice.Offline
                    } else {
                        RowNotice.DeleteFailed
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "delete datatable row failed" }
                    state.copy(isDeleting = false, notice = RowNotice.DeleteFailed)
                }

                is MutationResult.Conflicted ->
                    state.copy(isDeleting = false, notice = RowNotice.DeleteFailed)
            }
        }
    }

    private companion object {
        const val TAG = "DataTableRows"
        const val TABLE_NAME_KEY = "tableName"
        const val ENTITY_ID_KEY = "entityId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * One row of a tenant-defined datatable.
 *
 * [cells] is every column the tenant configured, in the order the API returned them — the original
 * read only `client_id` and `id` and discarded the rest, so a datatable's actual CONTENT never
 * reached the screen. [id] is kept separate because it is what a delete addresses.
 */
internal data class DataTableRow(
    val id: Int?,
    val cells: List<Pair<String, String>>,
)

/**
 * Maps the raw array to rows.
 *
 * Non-object entries are skipped rather than crashing: the shape is tenant-defined, so a
 * deployment returning something unexpected must not take the screen down.
 */
internal fun JsonArray.toRows(): List<DataTableRow> = mapNotNull { element ->
    val obj = element as? JsonObject ?: return@mapNotNull null
    DataTableRow(
        // `id` is the datatable row's own key. The original also accepted `client_id` as the
        // delete target, which is the ENTITY's id — deleting by it would address the wrong row.
        id = (obj["id"] as? JsonPrimitive)?.content?.toIntOrNull(),
        cells = obj.entries.map { (key, value) ->
            // `.content` on a primitive, so a string value is not re-quoted. The original used
            // `toString()`, which printed `"42"` with the quotes for every text column.
            key to ((value as? JsonPrimitive)?.content ?: value.toString())
        },
    )
}

/** Convenience for the screen: the entity id a row belongs to, when the tenant exposes it. */
internal fun JsonObject.entityIdOrNull(): Int? =
    (this["client_id"] as? JsonPrimitive)?.content?.toIntOrNull()

internal data class DataTableRowsState(
    val tableName: String = "",
    val entityId: Int = -1,
    val isDeleting: Boolean = false,
    val notice: RowNotice? = null,
)

internal enum class RowNotice { Deleted, DeleteFailed, Offline }

internal sealed interface DataTableRowsEvent {
    data object NavigateBack : DataTableRowsEvent
}

internal sealed interface DataTableRowsAction {
    data object NavigateBack : DataTableRowsAction
    data object Refresh : DataTableRowsAction
    data object DismissNotice : DataTableRowsAction
    data class Delete(val rowId: Int) : DataTableRowsAction
}
