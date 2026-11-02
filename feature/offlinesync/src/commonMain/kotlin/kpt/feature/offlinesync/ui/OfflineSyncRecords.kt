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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AccentColors
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RecordSyncStatus
import kpt.feature.offlinesync.generated.resources.Res
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_collapse
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_count_accounts
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_count_clients
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_count_documents
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_count_groups
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_count_loans
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_count_savings
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_expand
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_level_centres
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_level_clients
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_level_groups
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_open
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_records_title
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_refresh
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_remove
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_search_local
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_status_failed
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_status_out_of_date
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_status_synced
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_status_syncing
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_status_waiting
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_via_parent
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecordsCard(state: OfflineSyncState, onAction: (OfflineSyncAction) -> Unit) {
    OutlinedCard(Modifier.testTag(OfflineSyncTestTags.RECORDS)) {
        Column(Modifier.padding(top = KptTheme.spacing.md)) {
            Text(
                stringResource(Res.string.feature_offlinesync_records_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
            )
            Row(
                Modifier
                    .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm)
                    .testTag(OfflineSyncTestTags.RECORDS_LEVEL),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                LevelChip(state, RecordKind.CENTRE, Res.string.feature_offlinesync_level_centres, onAction)
                LevelChip(state, RecordKind.GROUP, Res.string.feature_offlinesync_level_groups, onAction)
                LevelChip(state, RecordKind.CLIENT, Res.string.feature_offlinesync_level_clients, onAction)
            }
            if (state.level == RecordKind.CLIENT && state.count(RecordKind.CLIENT) > 0) {
                OutlinedTextField(
                    value = state.search,
                    onValueChange = { onAction(OfflineSyncAction.Search(it)) },
                    placeholder = {
                        val clientCount = state.count(RecordKind.CLIENT)
                        Text(
                            pluralStringResource(
                                Res.plurals.feature_offlinesync_search_local,
                                clientCount,
                                clientCount,
                            ),
                        )
                    },
                    leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = KptTheme.spacing.md)
                        .testTag(OfflineSyncTestTags.RECORDS_SEARCH),
                )
            }
            state.levelRows.forEach { record ->
                RecordRow(record, state, indent = false, onAction)
                if ((record.kind to record.id) in state.expanded) {
                    state.childrenOf(record).forEach { child ->
                        RecordRow(child, state, indent = true, onAction)
                        if ((child.kind to child.id) in state.expanded) {
                            state.childrenOf(child).forEach { grandchild ->
                                RecordRow(grandchild, state, indent = true, onAction)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelChip(
    state: OfflineSyncState,
    kind: RecordKind,
    label: StringResource,
    onAction: (OfflineSyncAction) -> Unit,
) {
    FilterChip(
        selected = state.level == kind,
        colors = selectedChipColors(),
        onClick = { onAction(OfflineSyncAction.Level(kind)) },
        label = { Text(stringResource(label, state.count(kind))) },
        trailingIcon = if (state.hasFailures(kind)) {
            {
                Box(
                    Modifier.size(KptTheme.spacing.sm)
                        .background(MaterialTheme.colorScheme.error, MaterialTheme.shapes.extraSmall),
                )
            }
        } else {
            null
        },
    )
}

@Composable
private fun RecordRow(
    record: OfflineRecord,
    state: OfflineSyncState,
    indent: Boolean,
    onAction: (OfflineSyncAction) -> Unit,
) {
    val expandable = record.kind != RecordKind.CLIENT && state.childrenOf(record).isNotEmpty()
    val expanded = (record.kind to record.id) in state.expanded
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (indent) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent)
            .clickable { onAction(OfflineSyncAction.OpenRecord(record)) }
            .padding(
                start = if (indent) KptTheme.spacing.xl + KptTheme.spacing.lg else KptTheme.spacing.md,
                end = KptTheme.spacing.xs,
            )
            .heightIn(min = KptTheme.spacing.xxl)
            .testTag(OfflineSyncTestTags.RECORD_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
    ) {
        Initials(record, small = indent)
        Column(Modifier.weight(1f).padding(vertical = KptTheme.spacing.sm)) {
            Text(
                record.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            countsLine(record)?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            StatusLine(record, state)
        }
        if (record.chosen && !state.syncing) {
            if (record.status != RecordSyncStatus.WAITING) {
                IconButton(
                    onClick = { onAction(OfflineSyncAction.RefreshRecord(record)) },
                    enabled = state.online,
                    modifier = Modifier.testTag(OfflineSyncTestTags.RECORD_REFRESH),
                ) {
                    Icon(AppIcons.Refresh, contentDescription = stringResource(Res.string.feature_offlinesync_refresh))
                }
            }
            IconButton(
                onClick = { onAction(OfflineSyncAction.AskRemove(record)) },
                modifier = Modifier.testTag(OfflineSyncTestTags.RECORD_REMOVE),
            ) {
                Icon(AppIcons.Delete, contentDescription = stringResource(Res.string.feature_offlinesync_remove))
            }
        }
        if (expandable) {
            IconButton(onClick = { onAction(OfflineSyncAction.ToggleExpand(record)) }) {
                Icon(
                    if (expanded) AppIcons.ChevronUp else AppIcons.ChevronDown,
                    contentDescription = stringResource(
                        if (expanded) {
                            Res.string.feature_offlinesync_collapse
                        } else {
                            Res.string.feature_offlinesync_expand
                        },
                    ),
                )
            }
        } else if (!record.chosen) {
            Icon(
                AppIcons.ChevronRight,
                contentDescription = stringResource(Res.string.feature_offlinesync_open),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(KptTheme.spacing.sm + KptTheme.spacing.xs),
            )
        }
    }
}

@Composable
private fun countsLine(record: OfflineRecord): String? {
    val parts = buildList {
        if (record.kind == RecordKind.CENTRE) add(plural(Res.plurals.feature_offlinesync_count_groups, record.groups))
        if (record.kind != RecordKind.CLIENT) add(plural(Res.plurals.feature_offlinesync_count_clients, record.clients))
        if (record.kind == RecordKind.CLIENT) {
            if (record.loans > 0) add(plural(Res.plurals.feature_offlinesync_count_loans, record.loans))
            if (record.savings > 0) add(plural(Res.plurals.feature_offlinesync_count_savings, record.savings))
            if (record.documents > 0) add(plural(Res.plurals.feature_offlinesync_count_documents, record.documents))
        } else if (record.loans + record.savings > 0) {
            add(plural(Res.plurals.feature_offlinesync_count_accounts, record.loans + record.savings))
        }
        if (!record.chosen) {
            record.parentName?.let { add(stringResource(Res.string.feature_offlinesync_via_parent, it)) }
        }
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = SEPARATOR)
}

@Composable
private fun plural(resource: PluralStringResource, count: Int) = pluralStringResource(resource, count, count)

@Composable
private fun StatusLine(record: OfflineRecord, state: OfflineSyncState) {
    val synced = record.refreshedAtMs?.let { relative(it, state.nowMs) }.orEmpty()
    val (text, color, icon) = when {
        record.status == RecordSyncStatus.SYNCING ->
            Triple(
                stringResource(Res.string.feature_offlinesync_status_syncing),
                MaterialTheme.colorScheme.primary,
                AppIcons.CloudDownload,
            )
        record.status == RecordSyncStatus.WAITING ->
            Triple(
                stringResource(Res.string.feature_offlinesync_status_waiting),
                MaterialTheme.colorScheme.primary,
                AppIcons.Update,
            )
        record.status == RecordSyncStatus.FAILED ->
            Triple(
                stringResource(Res.string.feature_offlinesync_status_failed, record.failedCount, synced),
                MaterialTheme.colorScheme.error,
                AppIcons.Error,
            )
        record.isOutOfDate(state.nowMs, state.refresh.maxAgeMs) ->
            Triple(
                stringResource(Res.string.feature_offlinesync_status_out_of_date, synced),
                MaterialTheme.colorScheme.tertiary,
                AppIcons.Update,
            )
        else ->
            Triple(
                stringResource(Res.string.feature_offlinesync_status_synced, synced),
                MaterialTheme.colorScheme.secondary,
                AppIcons.RightTick,
            )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(KptTheme.spacing.md - KptTheme.spacing.xs / 2),
        )
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Initials(record: OfflineRecord, small: Boolean) {
    val (container, content) = when (record.kind) {
        RecordKind.CENTRE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        RecordKind.GROUP -> AccentColors.purpleContainer to AccentColors.purple
        RecordKind.CLIENT ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    val size: Dp = if (small) KptTheme.spacing.xl else KptTheme.spacing.xl + KptTheme.spacing.sm
    Box(Modifier.size(size).background(container, MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
        Text(
            record.name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() },
            style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
    }
}

/** Joins a row's facts ("3 groups · 31 clients"); the separator is punctuation, not copy. */
private const val SEPARATOR = " · "
