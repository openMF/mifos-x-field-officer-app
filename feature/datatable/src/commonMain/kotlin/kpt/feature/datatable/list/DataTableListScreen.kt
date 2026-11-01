/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.datatable.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.datatable.DataTable
import kpt.core.ui.components.AppEmptyUi
import kpt.feature.datatable.generated.resources.Res
import kpt.feature.datatable.generated.resources.feature_data_table_empty_data_table
import kpt.feature.datatable.generated.resources.feature_data_table_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/data-table/dataTable/DataTableScreen.kt` — one flat `Card`
 * per registered datatable showing its `registeredTableName`, inside `AppScaffold`.
 *
 * §6a's adaptations: the four-case `DataTableUiState` becomes [ScreenState] through
 * [ScreenContent] (which is what that enum was approximating — see the ViewModel), and the row's
 * tap dispatches an action.
 */
@Composable
internal fun DataTableListScreen(
    onBackPressed: () -> Unit,
    onOpenTable: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataTableListViewModel = koinViewModel(),
) {
    val dataTables by viewModel.dataTables.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            DataTableListEvent.NavigateBack -> onBackPressed()
            is DataTableListEvent.OpenTable -> onOpenTable(event.tableName, event.entityId)
        }
    }

    DataTableListContent(
        dataTables = dataTables,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        onBackPressed = onBackPressed,
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun DataTableListContent(
    dataTables: ScreenState<List<DataTable>>,
    onAction: (DataTableListAction) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_data_table_title),
        onBackPressed = onBackPressed,
        modifier = modifier.testTag(DataTableListTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = dataTables,
            onRetry = { onAction(DataTableListAction.Refresh) },
            // The original's own "no datatables" text, kept — passed into ScreenContent's `empty`
            // slot so the copy is the original's while the STATE is the stream's.
            empty = {
                AppEmptyUi(text = stringResource(Res.string.feature_data_table_empty_data_table))
            },
            modifier = Modifier.fillMaxSize().padding(padding).testTag(DataTableListTestTags.LIST),
        ) { tables, _ ->
            LazyColumn {
                items(tables, key = { it.registeredTableName.orEmpty() }) { table ->
                    DataTableRow(
                        table = table,
                        onClick = {
                            table.registeredTableName?.let {
                                onAction(DataTableListAction.TableClicked(it))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DataTableRow(table: DataTable, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.xs, vertical = KptTheme.spacing.xs),
        shape = DesignToken.shapes.none,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignToken.elevation.dp2),
        colors = CardDefaults.cardColors(containerColor = KptTheme.colorScheme.surface),
        onClick = onClick,
    ) {
        // Only rendered when the name is present, as in the original — a datatable with no
        // registered name has nothing to show and nothing to open.
        table.registeredTableName?.let {
            Text(
                modifier = Modifier.padding(
                    horizontal = KptTheme.spacing.md,
                    vertical = DesignToken.spacing.dp18,
                ),
                style = KptTheme.typography.bodyLarge,
                text = it,
            )
        }
    }
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object DataTableListTestTags {
    const val SCREEN = "dataTableList:screen"
    const val LIST = "dataTableList:list"
}
