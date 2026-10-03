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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.database.client.entity.ClientEntity
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.icon.MifosIcons
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
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ClientListEvent.OpenClient -> onClientClick?.invoke(event.clientId)
                ClientListEvent.CreateClient -> onCreateClient?.invoke()
            }
        }
    }

    ClientListContent(
        state = state,
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
    state: ClientListState,
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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feature_client_title)) },
                actions = {
                    IconButton(
                        onClick = { onAction(ClientListAction.FilterVisibilityToggled) },
                        modifier = Modifier.testTag(ClientListTestTags.FILTER_TOGGLE),
                    ) {
                        Icon(MifosIcons.FilterList, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(
                    onClick = { onAction(ClientListAction.CreateClientClicked) },
                    modifier = Modifier.testTag(ClientListTestTags.CREATE),
                ) {
                    Icon(MifosIcons.Add, contentDescription = stringResource(Res.string.feature_client_create))
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            MifosOutlinedTextField(
                value = state.query,
                onValueChange = { onAction(ClientListAction.QueryChanged(it)) },
                label = stringResource(Res.string.feature_client_search_hint),
                leadingIcon = MifosIcons.Search,
                showClearIcon = state.query.isNotEmpty(),
                onClickClearIcon = { onAction(ClientListAction.QueryChanged("")) },
                modifier = Modifier.fillMaxWidth().testTag(ClientListTestTags.SEARCH),
            )

            if (state.filtersVisible) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp).testTag(ClientListTestTags.FILTERS),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClientSort.entries.forEach { sort ->
                            FilterChip(
                                selected = state.sort == sort,
                                // Tapping the active sort clears it — the original's sort was
                                // nullable with no way back to unsorted.
                                onClick = {
                                    onAction(ClientListAction.SortChanged(if (state.sort == sort) null else sort))
                                },
                                label = { Text(stringResource(sort.labelRes())) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.officeNames.forEach { office ->
                            FilterChip(
                                selected = office in state.selectedOffices,
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

            ScreenContent(
                state = state.listState,
                onRetry = { onAction(ClientListAction.FiltersCleared) },
                modifier = Modifier.fillMaxSize(),
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
private fun ClientRow(client: ClientEntity, onClick: (() -> Unit)?) {
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

private fun ClientSort.labelRes() = when (this) {
    ClientSort.Name -> Res.string.feature_client_sort_name
    ClientSort.AccountNumber -> Res.string.feature_client_sort_account
    ClientSort.ExternalId -> Res.string.feature_client_sort_external_id
}
