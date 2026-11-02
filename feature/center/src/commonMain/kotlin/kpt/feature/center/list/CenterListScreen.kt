/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.center.list

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
import kpt.core.model.center.Center
import kpt.core.ui.components.OfflineDownloadDialog
import kpt.core.ui.components.OfflineSelectionBar
import kpt.feature.center.generated.resources.Res
import kpt.feature.center.generated.resources.feature_center_create_new_center
import kpt.feature.center.generated.resources.feature_center_filter
import kpt.feature.center.generated.resources.feature_center_filter_clear
import kpt.feature.center.generated.resources.feature_center_search_hint
import kpt.feature.center.generated.resources.feature_center_sort_account
import kpt.feature.center.generated.resources.feature_center_sort_name
import kpt.feature.center.generated.resources.feature_center_synced
import kpt.feature.center.generated.resources.feature_center_unnamed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CenterListScreen(
    onCenterClick: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    /** The original's create-center FAB. Null draws no FAB — never one that goes nowhere. */
    onCreateCenter: (() -> Unit)? = null,
    viewModel: CenterListViewModel = koinViewModel(),
) {
    // TWO flows, deliberately. `screenState` is the stream (Loading / Content / Empty / NoNetwork),
    // collected `WhileSubscribed` so it stops when this screen is not on display. `stateFlow` is
    // the screen's own filter state, which the chips read and which never pauses.
    val rows by viewModel.screenState.collectAsStateWithLifecycle()
    val ui by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CenterListEvent.OpenCenter -> onCenterClick?.invoke(event.centerId)
                CenterListEvent.CreateCenter -> onCreateCenter?.invoke()
            }
        }
    }

    CenterListContent(
        ui = ui,
        rows = rows,
        onAction = { viewModel.actionChannel.trySend(it) },
        rowsClickable = onCenterClick != null,
        canCreate = onCreateCenter != null,
        modifier = modifier,
    )
}

/** Stateless half, so the render is testable without Koin or a signed-in session. */
@Composable
internal fun CenterListContent(
    ui: CenterListState,
    rows: ScreenState<List<Center>>,
    onAction: (CenterListAction) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Whether a row has somewhere to go. Rows render WITHOUT a click rather than with one that does
     * nothing when there is no destination — a tap that silently does nothing is worse than no
     * affordance, because the user cannot tell it is unfinished (RULE-IMPL-DEAD-CLICKABLE-001).
     */
    rowsClickable: Boolean = true,
    /** Whether create-center is wired. Same reasoning as [rowsClickable]. */
    canCreate: Boolean = false,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(CenterListTestTags.SCREEN),
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(
                    onClick = { onAction(CenterListAction.CreateCenterClicked) },
                    modifier = Modifier.testTag(CenterListTestTags.CREATE),
                ) {
                    Icon(AppIcons.Add, contentDescription = stringResource(Res.string.feature_center_create_new_center))
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (ui.selected.isNotEmpty()) {
                OfflineSelectionBar(
                    count = ui.selected.size,
                    onDownload = { onAction(CenterListAction.DownloadSelected) },
                    onClear = { onAction(CenterListAction.SelectionCleared) },
                    modifier = Modifier.padding(vertical = KptTheme.spacing.sm),
                )
            }
            // No TopAppBar: Centers is a TAB ROOT and the dashboard shell draws one bar above every
            // tab root. The filter toggle sits beside the search field, as it does on Clients.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppOutlinedTextField(
                    value = ui.query,
                    onValueChange = { onAction(CenterListAction.QueryChanged(it)) },
                    label = stringResource(Res.string.feature_center_search_hint),
                    leadingIcon = AppIcons.Search,
                    showClearIcon = ui.query.isNotEmpty(),
                    onClickClearIcon = { onAction(CenterListAction.QueryChanged("")) },
                    modifier = Modifier.weight(1f).testTag(CenterListTestTags.SEARCH),
                )
                IconButton(
                    onClick = { onAction(CenterListAction.FilterVisibilityToggled) },
                    modifier = Modifier.testTag(CenterListTestTags.FILTER_TOGGLE),
                ) {
                    Icon(
                        AppIcons.FilterList,
                        contentDescription = stringResource(Res.string.feature_center_filter),
                    )
                }
            }

            if (ui.filtersVisible) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp).testTag(CenterListTestTags.FILTERS),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CenterSort.entries.forEach { sort ->
                            FilterChip(
                                selected = ui.sort == sort,
                                // Tapping the active sort clears it, so a user can get back to the
                                // server's order without a separate "none" chip.
                                onClick = {
                                    onAction(
                                        CenterListAction.SortChanged(sort.takeIf { ui.sort != sort }),
                                    )
                                },
                                label = { Text(stringResource(sort.labelRes())) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // The filter's options are DERIVED from the rows on screen, never a second
                        // fetch — so they cannot offer an office the list does not contain.
                        officeNames(rows).forEach { office ->
                            FilterChip(
                                selected = office in ui.selectedOffices,
                                onClick = { onAction(CenterListAction.OfficeFilterToggled(office)) },
                                label = { Text(office) },
                            )
                        }
                    }
                    TextButton(onClick = { onAction(CenterListAction.FiltersCleared) }) {
                        Text(stringResource(Res.string.feature_center_filter_clear))
                    }
                }
            }

            ScreenContent(
                state = rows,
                // Empty here is usually a filter matching nothing, so clearing the filters is the
                // useful action; a genuine read failure is retried through the action channel.
                onRetry = {
                    if (ui.query.isNotBlank() || ui.selectedOffices.isNotEmpty()) {
                        onAction(CenterListAction.FiltersCleared)
                    } else {
                        onAction(CenterListAction.Retry)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) { centers, _ ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs)) {
                    items(centers) { center ->
                        // Bound to a local: `id` is a nullable property of a class in another
                        // module, so the compiler cannot smart-cast it inside the lambda.
                        val centerId = center.id
                        CenterCard(
                            center = center,
                            onClick = if (rowsClickable && centerId != null) {
                                { onAction(CenterListAction.CenterClicked(centerId)) }
                            } else {
                                null
                            },
                            // Long-press selects for download — the original's selection mode.
                            onLongClick = centerId?.let { { onAction(CenterListAction.SelectionToggled(it)) } },
                            selected = centerId != null && centerId in ui.selected,
                        )
                    }
                }
            }
        }
    }

    ui.download?.let { progress ->
        OfflineDownloadDialog(
            progress = progress,
            onCancel = { onAction(CenterListAction.DownloadCancelled) },
            onDone = { onAction(CenterListAction.DownloadDismissed) },
        )
    }
}

/**
 * One center row — ported from `6b66e8a43:…/centerList/ui/CenterListScreen.kt#CenterCard`.
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
internal fun CenterCard(
    center: Center,
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
            .testTag(CenterListTestTags.ROW),
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
                        drawCircle(color = if (center.active == true) Color.Green else Color.Red)
                    },
                )
            },
            overlineContent = center.officeName?.let { { Text(text = it) } },
            headlineContent = {
                Text(text = center.name ?: stringResource(Res.string.feature_center_unnamed))
            },
            supportingContent = center.accountNo?.let { { Text(text = it) } },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    if (center.sync) {
                        Icon(
                            modifier = Modifier.size(DesignToken.sizes.iconSmall),
                            imageVector = AppIcons.Check,
                            contentDescription = stringResource(Res.string.feature_center_synced),
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
private fun officeNames(rows: ScreenState<List<Center>>): List<String> =
    (rows as? ScreenState.Content)?.data?.mapNotNull { it.officeName }?.distinct()?.sorted().orEmpty()

private fun CenterSort.labelRes() = when (this) {
    CenterSort.Name -> Res.string.feature_center_sort_name
    CenterSort.AccountNumber -> Res.string.feature_center_sort_account
}
