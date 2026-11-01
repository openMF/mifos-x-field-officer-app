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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.DefaultErrorContent
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.client.Client
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_create
import kpt.feature.client.generated.resources.feature_client_filter_clear
import kpt.feature.client.generated.resources.feature_client_search_hint
import kpt.feature.client.generated.resources.feature_client_sort_account
import kpt.feature.client.generated.resources.feature_client_sort_external_id
import kpt.feature.client.generated.resources.feature_client_sort_name
import kpt.feature.client.generated.resources.feature_client_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ClientListScreen(
    onClientClick: ((Int) -> Unit)?,
    onCreateClient: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: ClientListViewModel = koinViewModel(),
) {
    // TWO flows: `screenState` is the stream (collected WhileSubscribed, so it stops when the
    // Clients tab is not on display); `stateFlow` is the filter state the chips read.
    val rows by viewModel.screenState.collectAsStateWithLifecycle()
    val ui by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ClientListEvent.OpenClient -> onClientClick?.invoke(event.clientId)
                ClientListEvent.CreateClient -> onCreateClient?.invoke()
            }
        }
    }

    ClientListContent(
        ui = ui,
        rows = rows,
        onAction = { viewModel.actionChannel.trySend(it) },
        rowsClickable = onClientClick != null,
        canCreate = onCreateClient != null,
        modifier = modifier,
    )
}

/** Stateless half, so the render is testable without Koin or a signed-in session. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClientListContent(
    ui: ClientListState,
    rows: ScreenState<List<Client>>,
    onAction: (ClientListAction) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Whether a row has somewhere to go. Client detail arrives in S3b; until then rows render
     * WITHOUT a click rather than with one that does nothing — a tap that silently does nothing is
     * the dead-clickable defect (RULE-IMPL-DEAD-CLICKABLE-001), and it is worse than no affordance
     * because the user cannot tell it is unfinished.
     */
    rowsClickable: Boolean = true,
    /** Whether create-client exists yet. Same reasoning; the FAB arrives with S3c. */
    canCreate: Boolean = true,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(ClientListTestTags.SCREEN),
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(
                    onClick = { onAction(ClientListAction.CreateClientClicked) },
                    modifier = Modifier.testTag(ClientListTestTags.CREATE),
                ) {
                    Icon(AppIcons.Add, contentDescription = stringResource(Res.string.feature_client_create))
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            // The screen's own TopAppBar is gone: Clients is a TAB ROOT, and the dashboard shell
            // draws one bar (hamburger + search + notifications) above every tab root. Two bars
            // stacked is what a port that kept both looks like. Its only action — the filter
            // toggle — moves beside the search field, which is what it reveals anyway; the title
            // was redundant with the tab label and the shell bar.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppOutlinedTextField(
                    value = ui.query,
                    onValueChange = { onAction(ClientListAction.QueryChanged(it)) },
                    label = stringResource(Res.string.feature_client_search_hint),
                    leadingIcon = AppIcons.Search,
                    showClearIcon = ui.query.isNotEmpty(),
                    onClickClearIcon = { onAction(ClientListAction.QueryChanged("")) },
                    modifier = Modifier.weight(1f).testTag(ClientListTestTags.SEARCH),
                )
                IconButton(
                    onClick = { onAction(ClientListAction.FilterVisibilityToggled) },
                    modifier = Modifier.testTag(ClientListTestTags.FILTER_TOGGLE),
                ) {
                    Icon(
                        AppIcons.FilterList,
                        contentDescription = stringResource(Res.string.feature_client_title),
                    )
                }
            }

            if (ui.filtersVisible) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp).testTag(ClientListTestTags.FILTERS),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClientSort.entries.forEach { sort ->
                            FilterChip(
                                selected = ui.sort == sort,
                                // Tapping the active sort clears it — the original's sort was
                                // nullable with no way back to unsorted.
                                onClick = {
                                    onAction(ClientListAction.SortChanged(if (ui.sort == sort) null else sort))
                                },
                                label = { Text(stringResource(sort.labelRes())) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Derived from the rows on screen, never a second fetch.
                        officeNames(rows).forEach { office ->
                            FilterChip(
                                selected = office in ui.selectedOffices,
                                onClick = { onAction(ClientListAction.OfficeFilterToggled(office)) },
                                label = { Text(office) },
                            )
                        }
                    }
                    TextButton(
                        onClick = { onAction(ClientListAction.FiltersCleared) },
                        modifier = Modifier.testTag(ClientListTestTags.CLEAR_FILTERS),
                    ) {
                        Text(stringResource(Res.string.feature_client_filter_clear))
                    }
                }
            }

            val retry = {
                if (ui.query.isNotBlank() || ui.selectedOffices.isNotEmpty() ||
                    ui.selectedStatuses.isNotEmpty()
                ) {
                    onAction(ClientListAction.FiltersCleared)
                } else {
                    onAction(ClientListAction.Retry)
                }
            }
            // Pull-to-refresh, as in the original (`PullToRefreshBox` → `refreshClientList`). The
            // pull always refetches; the error state's button keeps its clear-filters-first retry.
            RefreshableScreenContent(
                state = rows,
                onRefresh = { onAction(ClientListAction.Refresh) },
                error = { DefaultErrorContent(it, retry) },
                modifier = Modifier.fillMaxSize().testTag(ClientListTestTags.REFRESH),
            ) { clients, _ ->
                LazyColumn(modifier = Modifier.testTag(ClientListTestTags.LIST)) {
                    items(clients) { client ->
                        ClientRow(
                            client = client,
                            onClick = if (rowsClickable) {
                                { onAction(ClientListAction.ClientClicked(client.id)) }
                            } else {
                                null
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientRow(client: Client, onClick: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
    ) {
        Text(text = client.displayName.orEmpty(), style = MaterialTheme.typography.bodyLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            client.accountNo?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            client.status?.value?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

/** Offices present in the loaded rows — the filter's options. Empty until content arrives. */
private fun officeNames(rows: ScreenState<List<Client>>): List<String> =
    (rows as? ScreenState.Content)?.data?.mapNotNull { it.officeName }?.distinct()?.sorted().orEmpty()

private fun ClientSort.labelRes() = when (this) {
    ClientSort.Name -> Res.string.feature_client_sort_name
    ClientSort.AccountNumber -> Res.string.feature_client_sort_account
    ClientSort.ExternalId -> Res.string.feature_client_sort_external_id
}
