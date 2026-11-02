/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.offlinesync.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordKind
import kpt.feature.offlinesync.generated.resources.Res
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_cancel
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_on_device
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_picker_download
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_picker_empty
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_picker_error
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_picker_hint
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_picker_search
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_picker_title
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_remove
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_remove_confirm
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_retry
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_tab_centres
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_tab_clients
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_tab_groups
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordPickerSheet(
    picker: PickerState,
    rows: ScreenState<List<PickerRow>>,
    state: OfflineSyncState,
    onAction: (OfflineSyncAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(OfflineSyncAction.ClosePicker) },
        modifier = Modifier.testTag(OfflineSyncTestTags.PICKER),
    ) {
        PickerSheetBody(picker, rows, state, onAction)
    }
}

@Composable
internal fun PickerSheetBody(
    picker: PickerState,
    rows: ScreenState<List<PickerRow>>,
    state: OfflineSyncState,
    onAction: (OfflineSyncAction) -> Unit,
) {
    Column {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            Text(
                stringResource(Res.string.feature_offlinesync_picker_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                Modifier.testTag(OfflineSyncTestTags.PICKER_TABS),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                PickerTabChip(picker, RecordKind.CENTRE, Res.string.feature_offlinesync_tab_centres, onAction)
                PickerTabChip(picker, RecordKind.GROUP, Res.string.feature_offlinesync_tab_groups, onAction)
                PickerTabChip(picker, RecordKind.CLIENT, Res.string.feature_offlinesync_tab_clients, onAction)
            }
            OutlinedTextField(
                value = picker.query,
                onValueChange = { onAction(OfflineSyncAction.PickerSearch(it)) },
                placeholder = { Text(stringResource(Res.string.feature_offlinesync_picker_search)) },
                leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag(OfflineSyncTestTags.PICKER_SEARCH),
            )
            if (picker.tab == RecordKind.CENTRE) {
                Text(
                    stringResource(Res.string.feature_offlinesync_picker_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = KptTheme.spacing.xxl * 2, max = KptTheme.spacing.xxl * PICKER_LIST_HEIGHT_UNITS),
        ) {
            when (rows) {
                is ScreenState.Content -> PickerList(rows.data, picker, state, onAction)
                is ScreenState.Error, is ScreenState.NoNetwork -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PickerMessage(Res.string.feature_offlinesync_picker_error)
                    TextButton(onClick = { onAction(OfflineSyncAction.PickerRetry) }) {
                        Text(stringResource(Res.string.feature_offlinesync_retry))
                    }
                }
                ScreenState.Empty -> PickerMessage(Res.string.feature_offlinesync_picker_empty)
                ScreenState.Loading, ScreenState.Unauthenticated ->
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(KptTheme.spacing.md))
            }
        }
        Button(
            onClick = { onAction(OfflineSyncAction.PickerDownload) },
            enabled = picker.selected.isNotEmpty() && state.online,
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md)
                .heightIn(min = KptTheme.spacing.xl + KptTheme.spacing.md + KptTheme.spacing.xs)
                .testTag(OfflineSyncTestTags.PICKER_DOWNLOAD),
        ) {
            Icon(AppIcons.CloudDownload, contentDescription = null)
            Spacer(Modifier.size(KptTheme.spacing.sm))
            Text(stringResource(Res.string.feature_offlinesync_picker_download, picker.selected.size))
        }
    }
}

@Composable
private fun PickerList(
    all: List<PickerRow>,
    picker: PickerState,
    state: OfflineSyncState,
    onAction: (OfflineSyncAction) -> Unit,
) {
    val query = picker.query.trim()
    val shown = all.filter { row ->
        query.isEmpty() ||
            row.target.name.contains(query, ignoreCase = true) ||
            row.accountNo?.contains(query, ignoreCase = true) == true
    }
    if (shown.isEmpty()) {
        PickerMessage(Res.string.feature_offlinesync_picker_empty)
        return
    }
    LazyColumn(
        Modifier.testTag(OfflineSyncTestTags.PICKER_LIST),
        contentPadding = PaddingValues(vertical = KptTheme.spacing.sm),
    ) {
        items(shown, key = { "${it.target.kind}:${it.target.id}" }) { row ->
            PickerRowItem(
                row,
                selected = row.target in picker.selected,
                onDevice = state.isOnDevice(row.target),
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun PickerTabChip(
    picker: PickerState,
    kind: RecordKind,
    label: StringResource,
    onAction: (OfflineSyncAction) -> Unit,
) {
    FilterChip(
        selected = picker.tab == kind,
        colors = selectedChipColors(),
        onClick = { onAction(OfflineSyncAction.PickerTab(kind)) },
        label = { Text(stringResource(label)) },
    )
}

@Composable
private fun PickerRowItem(row: PickerRow, selected: Boolean, onDevice: Boolean, onAction: (OfflineSyncAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onAction(OfflineSyncAction.PickerSelect(row.target)) }
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.xs)
            .testTag(OfflineSyncTestTags.PICKER_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        Checkbox(checked = selected, onCheckedChange = { onAction(OfflineSyncAction.PickerSelect(row.target)) })
        Column(Modifier.weight(1f)) {
            Text(
                row.target.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            row.accountNo?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (onDevice) {
            StatusChip(
                AppIcons.RightTick,
                stringResource(Res.string.feature_offlinesync_on_device),
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun PickerMessage(text: StringResource) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.lg),
    )
}

@Composable
internal fun RemoveDialog(record: OfflineRecord, onAction: (OfflineSyncAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(OfflineSyncAction.DismissRemove) },
        text = { Text(stringResource(Res.string.feature_offlinesync_remove_confirm, record.name)) },
        confirmButton = {
            TextButton(
                onClick = { onAction(OfflineSyncAction.ConfirmRemove) },
                modifier = Modifier.testTag(OfflineSyncTestTags.REMOVE_CONFIRM),
            ) {
                Text(stringResource(Res.string.feature_offlinesync_remove))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(OfflineSyncAction.DismissRemove) }) {
                Text(stringResource(Res.string.feature_offlinesync_cancel))
            }
        },
    )
}

private const val PICKER_LIST_HEIGHT_UNITS = 7
