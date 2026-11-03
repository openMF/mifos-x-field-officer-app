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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.ui.components.AppEmptyUi
import kpt.feature.datatable.generated.resources.Res
import kpt.feature.datatable.generated.resources.feature_data_table_delete_data_table
import kpt.feature.datatable.generated.resources.feature_data_table_dismiss
import kpt.feature.datatable.generated.resources.feature_data_table_failed_to_delete_data_table
import kpt.feature.datatable.generated.resources.feature_data_table_no_data_table_details_to_show
import kpt.feature.datatable.generated.resources.feature_data_table_offline
import kpt.feature.datatable.generated.resources.feature_data_table_row_deleted
import kpt.feature.datatable.generated.resources.feature_data_table_select_options
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * One datatable's rows — carried over from
 * `6b66e8a43:feature/data-table/dataTableData/DataTableDataScreen.kt`.
 *
 * ## What is DIFFERENT, and why it is not a redesign
 *
 * The original card showed exactly two labelled values — "Client Id" and "Data Id" — because the
 * ViewModel's mapping kept only `client_id` and `id` from each JSON object and discarded every
 * other column. For a datatable, whose whole purpose is the tenant's own columns, that meant the
 * screen never showed the data. Each card now lists every column the tenant configured, using the
 * original's card, row and spacing treatment.
 *
 * This is the one place in the client slices where the rendered CONTENT changes rather than just
 * its state plumbing, and it is recorded in CAPABILITY_GAPS as the reason.
 *
 * The Add FAB is absent: it opened `DataTableRowDialogScreen`, which belongs to the create-client
 * form flow that is not ported (see this module's MIGRATION.md).
 */
@Composable
internal fun DataTableRowsScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataTableRowsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val rows by viewModel.rows.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            DataTableRowsEvent.NavigateBack -> onBackPressed()
        }
    }

    DataTableRowsContent(
        state = state,
        rows = rows,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        onBackPressed = onBackPressed,
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun DataTableRowsContent(
    state: DataTableRowsState,
    rows: ScreenState<List<DataTableRow>>,
    onAction: (DataTableRowsAction) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val notice = state.notice
    val noticeText = notice?.let { stringResource(it.messageRes()) }

    LaunchedEffect(notice) {
        if (noticeText != null) {
            snackbarHostState.showSnackbar(noticeText)
            onAction(DataTableRowsAction.DismissNotice)
        }
    }

    var pendingDelete by remember { mutableStateOf<Int?>(null) }

    AppScaffold(
        // The datatable's own name — the original titled this screen with it too.
        title = state.tableName,
        onBackPressed = onBackPressed,
        snackbarHostState = snackbarHostState,
        modifier = modifier.testTag(DataTableRowsTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = rows,
            onRetry = { onAction(DataTableRowsAction.Refresh) },
            empty = {
                AppEmptyUi(
                    text = stringResource(Res.string.feature_data_table_no_data_table_details_to_show),
                )
            },
            modifier = Modifier.fillMaxSize().padding(padding).testTag(DataTableRowsTestTags.LIST),
        ) { list, _ ->
            LazyColumn {
                // Indexed, because a tenant datatable row need not have an `id` — a many-to-one
                // table has no row key of its own. The original keyed nothing and re-appended the
                // whole list on every recomposition.
                itemsIndexed(list) { index, row ->
                    RowCard(
                        row = row,
                        enabled = !state.isDeleting,
                        onLongPress = { row.id?.let { pendingDelete = it } },
                        modifier = Modifier.testTag(DataTableRowsTestTags.row(index)),
                    )
                }
            }
        }
    }

    pendingDelete?.let { rowId ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(Res.string.feature_data_table_select_options)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        onAction(DataTableRowsAction.Delete(rowId))
                    },
                    modifier = Modifier.testTag(DataTableRowsTestTags.CONFIRM_DELETE),
                ) { Text(stringResource(Res.string.feature_data_table_delete_data_table)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(Res.string.feature_data_table_dismiss))
                }
            },
            modifier = Modifier.testTag(DataTableRowsTestTags.DELETE_DIALOG),
        )
    }
}

@Composable
private fun RowCard(
    row: DataTableRow,
    enabled: Boolean,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.padding(KptTheme.spacing.sm),
        colors = CardDefaults.outlinedCardColors(),
        onClick = { if (enabled) onLongPress() },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            // Every column the tenant configured — see this file's header for why the original
            // showed only two.
            row.cells.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = label,
                        style = KptTheme.typography.bodyMedium,
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        // "null" is what a JSON null reads as; shown as a dash, like the original
                        // did for its two missing values.
                        text = value.takeIf { it.isNotBlank() && it != "null" } ?: "-",
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

private fun RowNotice.messageRes() = when (this) {
    RowNotice.Deleted -> Res.string.feature_data_table_row_deleted
    RowNotice.DeleteFailed -> Res.string.feature_data_table_failed_to_delete_data_table
    RowNotice.Offline -> Res.string.feature_data_table_offline
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object DataTableRowsTestTags {
    const val SCREEN = "dataTableRows:screen"
    const val LIST = "dataTableRows:list"
    const val DELETE_DIALOG = "dataTableRows:deleteDialog"
    const val CONFIRM_DELETE = "dataTableRows:confirmDelete"

    fun row(index: Int): String = "dataTableRows:row:$index"
}
