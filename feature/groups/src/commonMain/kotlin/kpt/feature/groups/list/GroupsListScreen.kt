/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.group.Group
import kpt.core.ui.components.OfflineDownloadDialog
import kpt.core.ui.components.OfflineSelectionBar
import kpt.feature.groups.generated.resources.Res
import kpt.feature.groups.generated.resources.feature_groups_create_title
import kpt.feature.groups.generated.resources.feature_groups_filter
import kpt.feature.groups.generated.resources.feature_groups_filter_clear
import kpt.feature.groups.generated.resources.feature_groups_search_hint
import kpt.feature.groups.generated.resources.feature_groups_sort_account
import kpt.feature.groups.generated.resources.feature_groups_sort_name
import kpt.feature.groups.generated.resources.feature_groups_synced
import kpt.feature.groups.generated.resources.feature_groups_unnamed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun GroupsListScreen(
    onGroupClick: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    /**
     * Open the create-group form.
     *
     * The original draws `MifosFAB(icon = MifosIcons.Add, onClick = onAddGroupClick)` here; this
     * port had no create callback at all, so the affordance was MISSING rather than null-gated.
     * `null` keeps it absent for any caller that has no destination.
     */
    onCreateGroup: (() -> Unit)? = null,
    viewModel: GroupsListViewModel = koinViewModel(),
) {
    // TWO flows: `screenState` is the stream (collected WhileSubscribed, so it stops off-screen);
    // `stateFlow` is the screen's own filter state, which the chips read.
    val rows by viewModel.screenState.collectAsStateWithLifecycle()
    val ui by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is GroupsListEvent.OpenGroup -> onGroupClick?.invoke(event.groupId)
            }
        }
    }

    GroupsListContent(
        ui = ui,
        rows = rows,
        onAction = { viewModel.actionChannel.trySend(it) },
        rowsClickable = onGroupClick != null,
        onCreateGroup = onCreateGroup,
        modifier = modifier,
    )
}

/** Stateless half, so the render is testable without Koin or a signed-in session. */
@Composable
internal fun GroupsListContent(
    ui: GroupsListState,
    rows: ScreenState<List<Group>>,
    onAction: (GroupsListAction) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Whether a row has somewhere to go. Rows render WITHOUT a click rather than with one that does
     * nothing when there is no destination — a tap that silently does nothing is worse than no
     * affordance, because the user cannot tell it is unfinished (RULE-IMPL-DEAD-CLICKABLE-001).
     */
    rowsClickable: Boolean = true,
    /**
     * Open the create-group form, or `null` when no destination exists.
     *
     * Same gate as [rowsClickable]: the FAB is ABSENT rather than drawn inert.
     */
    onCreateGroup: (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(GroupsListTestTags.SCREEN),
        floatingActionButton = {
            // Drawn only when a destination exists — the same gate the client list uses.
            onCreateGroup?.let { create ->
                FloatingActionButton(
                    onClick = create,
                    modifier = Modifier.testTag(GroupsListTestTags.CREATE),
                ) {
                    Icon(
                        AppIcons.Add,
                        contentDescription = stringResource(Res.string.feature_groups_create_title),
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (ui.selected.isNotEmpty()) {
                OfflineSelectionBar(
                    count = ui.selected.size,
                    onDownload = { onAction(GroupsListAction.DownloadSelected) },
                    onClear = { onAction(GroupsListAction.SelectionCleared) },
                    modifier = Modifier.padding(vertical = KptTheme.spacing.sm),
                )
            }
            // No TopAppBar: Groups is a TAB ROOT and the dashboard shell draws one bar above every
            // tab root. The filter toggle sits beside the search field, as it does on Clients.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppOutlinedTextField(
                    value = ui.query,
                    onValueChange = { onAction(GroupsListAction.QueryChanged(it)) },
                    label = stringResource(Res.string.feature_groups_search_hint),
                    leadingIcon = AppIcons.Search,
                    showClearIcon = ui.query.isNotEmpty(),
                    onClickClearIcon = { onAction(GroupsListAction.QueryChanged("")) },
                    modifier = Modifier.weight(1f).testTag(GroupsListTestTags.SEARCH),
                )
                IconButton(
                    onClick = { onAction(GroupsListAction.FilterVisibilityToggled) },
                    modifier = Modifier.testTag(GroupsListTestTags.FILTER_TOGGLE),
                ) {
                    Icon(
                        AppIcons.FilterList,
                        contentDescription = stringResource(Res.string.feature_groups_filter),
                    )
                }
            }

            if (ui.filtersVisible) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp).testTag(GroupsListTestTags.FILTERS),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GroupsSort.entries.forEach { sort ->
                            FilterChip(
                                selected = ui.sort == sort,
                                // Tapping the active sort clears it, so a user can get back to the
                                // server's order without a separate "none" chip.
                                onClick = {
                                    onAction(
                                        GroupsListAction.SortChanged(sort.takeIf { ui.sort != sort }),
                                    )
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
                                onClick = { onAction(GroupsListAction.OfficeFilterToggled(office)) },
                                label = { Text(office) },
                            )
                        }
                    }
                    TextButton(onClick = { onAction(GroupsListAction.FiltersCleared) }) {
                        Text(stringResource(Res.string.feature_groups_filter_clear))
                    }
                }
            }

            ScreenContent(
                state = rows,
                // Retry clears the filters rather than refetching: the Empty this renders is most
                // often a filter matching nothing, and refetching the same data would not change it.
                onRetry = {
                    if (ui.query.isNotBlank() || ui.selectedOffices.isNotEmpty()) {
                        onAction(GroupsListAction.FiltersCleared)
                    } else {
                        onAction(GroupsListAction.Retry)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) { groups, _ ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs)) {
                    items(groups) { group ->
                        // Bound to a local: `id` is a nullable property of a class in another
                        // module, so the compiler cannot smart-cast it inside the lambda.
                        val groupId = group.id
                        GroupsCard(
                            group = group,
                            onClick = if (rowsClickable && groupId != null) {
                                { onAction(GroupsListAction.GroupClicked(groupId)) }
                            } else {
                                null
                            },
                            // Long-press selects for download — the original's selection mode.
                            onLongClick = groupId?.let { { onAction(GroupsListAction.SelectionToggled(it)) } },
                            selected = groupId != null && groupId in ui.selected,
                        )
                    }
                }
            }
        }
    }

    ui.download?.let { progress ->
        OfflineDownloadDialog(
            progress = progress,
            onCancel = { onAction(GroupsListAction.DownloadCancelled) },
            onDone = { onAction(GroupsListAction.DownloadDismissed) },
        )
    }
}

/**
 * One group row — ported from `6b66e8a43:…/centerList/ui/CenterListScreen.kt#GroupsCard`.
 *
 * Kept as the original drew it: an `OutlinedCard` wrapping a `ListItem` whose slots carry the
 * office (overline), name (headline) and account number (supporting), with a status dot leading and
 * a sync marker plus chevron trailing. An earlier pass replaced all of this with a plain `Column`
 * and a divider — a different screen wearing the same data.
 *
 * Long-press enters the original's SELECTION MODE for the bulk offline download
 * (`OfflineDownloadRepository`); a selected card takes the secondary container colour.
 */
@Composable
internal fun GroupsCard(
    group: Group,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardDefaults.outlinedShape)
            .then(
                if (onClick != null || onLongClick != null) {
                    Modifier.combinedClickable(onClick = onClick ?: {}, onLongClick = onLongClick)
                } else {
                    Modifier
                },
            )
            .testTag(GroupsListTestTags.ROW),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) KptTheme.colorScheme.secondaryContainer else KptTheme.colorScheme.surface,
        ),
    ) {
        ListItem(
            leadingContent = {
                // Active/inactive at a glance, drawn rather than themed: the original uses literal
                // green/red here because the signal is a universal status convention, not a brand
                // colour, and it has to read the same in every fork's palette.
                Canvas(
                    modifier = Modifier.size(DesignToken.sizes.iconSmall),
                    onDraw = {
                        drawCircle(color = if (group.active == true) Color.Green else Color.Red)
                    },
                )
            },
            overlineContent = group.officeName?.let { { Text(text = it) } },
            headlineContent = {
                Text(text = group.name ?: stringResource(Res.string.feature_groups_unnamed))
            },
            supportingContent = group.accountNo?.let { { Text(text = it) } },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    if (group.sync) {
                        Icon(
                            modifier = Modifier.size(DesignToken.sizes.iconSmall),
                            imageVector = AppIcons.Check,
                            contentDescription = stringResource(Res.string.feature_groups_synced),
                        )
                    }
                    Icon(
                        imageVector = AppIcons.ArrowForward,
                        contentDescription = null,
                    )
                }
            },
            // Transparent, not Unspecified: Unspecified fell back to an opaque surface over the card, hiding
            // the selected tint (invisible before, as both were the surface colour).
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

/** Offices present in the loaded rows — the filter's options. Empty until content arrives. */
private fun officeNames(rows: ScreenState<List<Group>>): List<String> =
    (rows as? ScreenState.Content)?.data?.mapNotNull { it.officeName }?.distinct()?.sorted().orEmpty()

private fun GroupsSort.labelRes() = when (this) {
    GroupsSort.Name -> Res.string.feature_groups_sort_name
    GroupsSort.AccountNumber -> Res.string.feature_groups_sort_account
}
