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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.FloatingActionButtonContent
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AccentColors
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RefreshMode
import kpt.core.model.offline.RefreshSettings
import kpt.core.model.offline.SyncJob
import kpt.feature.offlinesync.generated.resources.Res
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_add_records
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_cancel
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_drafts_link
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_drafts_note
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_empty_body
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_empty_title
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_header_subtitle
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_job_downloading
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_job_reads
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_job_records
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_job_result
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_job_result_body
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_last_synced
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_level_label_centres
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_level_label_clients
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_level_label_groups
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_next_in_hours
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_not_synced
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_offline
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_offline_banner
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_online
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_refresh_automatic
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_refresh_manual
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_retry_failed
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_stat_failed
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_stat_to_sync
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_sync_now
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_sync_when_online
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_syncing_button
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_time_days
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_time_hours
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_time_just_now
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_time_minutes
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun OfflineSyncScreen(
    onNavigateBack: () -> Unit,
    onOpenRecord: (RecordKind, Int) -> Unit,
    onOpenDrafts: () -> Unit,
    viewModel: OfflineSyncViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val pickerRows by viewModel.pickerRows.collectAsStateWithLifecycle()
    EventsEffect(eventFlow = viewModel.eventFlow) { event ->
        when (event) {
            OfflineSyncEvent.NavigateBack -> onNavigateBack()
            OfflineSyncEvent.OpenDrafts -> onOpenDrafts()
            is OfflineSyncEvent.OpenRecord -> onOpenRecord(event.kind, event.id)
        }
    }
    OfflineSyncScreenContent(state = state, pickerRows = pickerRows, onAction = viewModel::trySendAction)
}

@Composable
internal fun OfflineSyncScreenContent(
    state: OfflineSyncState,
    pickerRows: ScreenState<List<PickerRow>>,
    onAction: (OfflineSyncAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screen = state.screen
    val showFab = state.online && screen != OfflineScreen.LOADING && screen != OfflineScreen.EMPTY
    AppScaffold(
        title = stringResource(Res.string.feature_offlinesync_title),
        onBackPressed = { onAction(OfflineSyncAction.NavigateBack) },
        modifier = modifier.testTag(OfflineSyncTestTags.SCREEN),
        floatingActionButtonContent = if (showFab) addRecordsFab(onAction) else null,
    ) { padding ->
        if (screen == OfflineScreen.LOADING) {
            LoadingSkeleton(Modifier.padding(padding))
        } else {
            OfflineSyncBody(state, onAction, Modifier.padding(padding))
        }
    }
    state.picker?.let { RecordPickerSheet(it, pickerRows, state, onAction) }
    state.confirmRemove?.let { RemoveDialog(it, onAction) }
}

@Composable
private fun addRecordsFab(onAction: (OfflineSyncAction) -> Unit) = FloatingActionButtonContent(
    onClick = { onAction(OfflineSyncAction.AddRecords) },
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Row(
        Modifier.padding(horizontal = KptTheme.spacing.md).testTag(OfflineSyncTestTags.ADD_RECORDS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        Icon(AppIcons.Add, contentDescription = null)
        Text(stringResource(Res.string.feature_offlinesync_add_records))
    }
}

@Composable
private fun OfflineSyncBody(
    state: OfflineSyncState,
    onAction: (OfflineSyncAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screen = state.screen
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = KptTheme.spacing.md)
            .padding(bottom = KptTheme.spacing.xxl + KptTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        if (!state.online) OfflineBanner()
        SummaryCard(state, onAction)
        when (screen) {
            OfflineScreen.SYNCING -> state.job?.let { ActiveJobCard(it, onAction) }
            OfflineScreen.PARTIAL_FAILURE -> state.job?.let { ResultCard(it, state.online, onAction) }
            else -> Unit
        }
        if (screen == OfflineScreen.EMPTY) {
            EmptyCard(state.online, onAction)
        } else {
            RecordsCard(state, onAction)
        }
        if (state.online) RefreshCard(state.refresh, onAction) else DraftsCard(onAction)
    }
}

// ───────────────────────────── header ─────────────────────────────

@Composable
private fun SummaryCard(state: OfflineSyncState, onAction: (OfflineSyncAction) -> Unit) {
    OutlinedCard(Modifier.testTag(OfflineSyncTestTags.HEADER)) {
        Column(
            Modifier.padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm)) {
                if (state.online) {
                    StatusChip(
                        AppIcons.Sync,
                        stringResource(Res.string.feature_offlinesync_online),
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                } else {
                    StatusChip(
                        AppIcons.WifiOff,
                        stringResource(Res.string.feature_offlinesync_offline),
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                val failures = state.job?.failures?.size ?: 0
                when {
                    state.screen == OfflineScreen.PARTIAL_FAILURE && failures > 0 -> StatusChip(
                        AppIcons.Error,
                        stringResource(Res.string.feature_offlinesync_stat_failed, failures),
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer,
                    )
                    state.toSync > 0 -> StatusChip(
                        AppIcons.Update,
                        stringResource(Res.string.feature_offlinesync_stat_to_sync, state.toSync),
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm)) {
                LevelTile(
                    state.count(RecordKind.CENTRE),
                    Res.string.feature_offlinesync_level_label_centres,
                    AppIcons.Groups,
                    Modifier.weight(1f),
                )
                LevelTile(
                    state.count(RecordKind.GROUP),
                    Res.string.feature_offlinesync_level_label_groups,
                    AppIcons.Group,
                    Modifier.weight(1f),
                )
                LevelTile(
                    state.count(RecordKind.CLIENT),
                    Res.string.feature_offlinesync_level_label_clients,
                    AppIcons.Person,
                    Modifier.weight(1f),
                )
            }
            Text(
                headerSubtitle(state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.records.orEmpty().isNotEmpty()) {
                Button(
                    onClick = { onAction(OfflineSyncAction.SyncNow) },
                    enabled = state.canSync,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = KptTheme.spacing.xl + KptTheme.spacing.md)
                        .testTag(OfflineSyncTestTags.SYNC_NOW),
                ) {
                    Icon(AppIcons.Sync, contentDescription = null)
                    Spacer(Modifier.size(KptTheme.spacing.sm))
                    Text(
                        stringResource(
                            when {
                                state.syncing -> Res.string.feature_offlinesync_syncing_button
                                !state.online -> Res.string.feature_offlinesync_sync_when_online
                                else -> Res.string.feature_offlinesync_sync_now
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun headerSubtitle(state: OfflineSyncState): String {
    val last = state.lastSyncedAtMs
        ?.let { stringResource(Res.string.feature_offlinesync_last_synced, relative(it, state.nowMs)) }
        ?: stringResource(Res.string.feature_offlinesync_not_synced)
    val mode = when (state.refresh.mode) {
        RefreshMode.MANUAL -> stringResource(Res.string.feature_offlinesync_refresh_manual)
        RefreshMode.AUTOMATIC -> state.nextSyncAtMs?.let { next ->
            val hours = ((next - state.nowMs).coerceAtLeast(0) / RefreshSettings.HOUR_MS).toInt().coerceAtLeast(1)
            stringResource(Res.string.feature_offlinesync_next_in_hours, hours)
        } ?: stringResource(Res.string.feature_offlinesync_refresh_automatic)
    }
    return stringResource(Res.string.feature_offlinesync_header_subtitle, last, mode)
}

@Composable
private fun LevelTile(count: Int, label: StringResource, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
            .padding(KptTheme.spacing.sm + KptTheme.spacing.xs),
    ) {
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(KptTheme.spacing.md),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OfflineBanner() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.medium)
            .padding(KptTheme.spacing.sm + KptTheme.spacing.xs)
            .testTag(OfflineSyncTestTags.OFFLINE_BANNER),
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
    ) {
        Icon(AppIcons.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
        Text(
            stringResource(Res.string.feature_offlinesync_offline_banner),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

// ───────────────────────────── job ─────────────────────────────

@Composable
private fun ActiveJobCard(job: SyncJob, onAction: (OfflineSyncAction) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(KptTheme.spacing.xs / 2, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
            .testTag(OfflineSyncTestTags.ACTIVE_JOB),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            Modifier.padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
            ) {
                IconTile(
                    AppIcons.Sync,
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        pluralStringResource(Res.plurals.feature_offlinesync_job_downloading, job.total, job.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        job.targets.joinToString { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OutlinedButton(
                    onClick = { onAction(OfflineSyncAction.CancelJob) },
                    modifier = Modifier.testTag(OfflineSyncTestTags.CANCEL_JOB),
                ) {
                    Text(stringResource(Res.string.feature_offlinesync_cancel))
                }
            }
            val progressLabel = pluralStringResource(
                Res.plurals.feature_offlinesync_job_records,
                job.total,
                job.done,
                job.total,
            )
            // A record's tree is discovered as it is walked, so until the first record finishes there is
            // no honest fraction — moving, not stuck at 0%.
            if (job.done == 0) {
                LinearProgressIndicator(Modifier.fillMaxWidth().semantics { contentDescription = progressLabel })
            } else {
                LinearProgressIndicator(
                    progress = { job.done.toFloat() / job.total },
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = progressLabel },
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(progressLabel, style = MaterialTheme.typography.bodySmall)
                Text(
                    pluralStringResource(Res.plurals.feature_offlinesync_job_reads, job.reads, job.reads),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            job.currentLabel?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ResultCard(job: SyncJob, online: Boolean, onAction: (OfflineSyncAction) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag(OfflineSyncTestTags.JOB_RESULT),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
        ) {
            IconTile(AppIcons.Error, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onErrorContainer)
            Column {
                Text(
                    stringResource(Res.string.feature_offlinesync_job_result, job.reads, job.failures.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    stringResource(Res.string.feature_offlinesync_job_result_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
        job.failures.take(MAX_FAILURE_ROWS).forEach { failure ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm + KptTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
            ) {
                Icon(
                    AppIcons.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(KptTheme.spacing.md + KptTheme.spacing.xs),
                )
                Column {
                    Text(failure.item, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    failure.reason?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(KptTheme.spacing.md), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = { onAction(OfflineSyncAction.RetryFailed) },
                enabled = online,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag(OfflineSyncTestTags.RETRY_FAILED),
            ) {
                Icon(AppIcons.Refresh, contentDescription = null)
                Spacer(Modifier.size(KptTheme.spacing.xs))
                Text(stringResource(Res.string.feature_offlinesync_retry_failed))
            }
        }
    }
}

@Composable
private fun DraftsCard(onAction: (OfflineSyncAction) -> Unit) {
    OutlinedCard(Modifier.testTag(OfflineSyncTestTags.DRAFTS_LINK)) {
        Row(
            Modifier.padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
        ) {
            Icon(AppIcons.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                stringResource(Res.string.feature_offlinesync_drafts_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { onAction(OfflineSyncAction.OpenDrafts) }) {
                Text(stringResource(Res.string.feature_offlinesync_drafts_link))
            }
        }
    }
}

@Composable
private fun EmptyCard(online: Boolean, onAction: (OfflineSyncAction) -> Unit) {
    OutlinedCard(Modifier.testTag(OfflineSyncTestTags.EMPTY)) {
        Column(
            Modifier.fillMaxWidth().padding(KptTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                IconTile(
                    AppIcons.Groups,
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                )
                IconTile(AppIcons.Group, AccentColors.purpleContainer, AccentColors.purple)
                IconTile(
                    AppIcons.Person,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Text(
                stringResource(Res.string.feature_offlinesync_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(Res.string.feature_offlinesync_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { onAction(OfflineSyncAction.AddRecords) },
                enabled = online,
                modifier = Modifier.testTag(OfflineSyncTestTags.ADD_RECORDS),
            ) {
                Icon(AppIcons.Add, contentDescription = null)
                Spacer(Modifier.size(KptTheme.spacing.sm))
                Text(stringResource(Res.string.feature_offlinesync_add_records))
            }
        }
    }
}

// ───────────────────────────── shared pieces ─────────────────────────────

/** Selected = the design's indigo container, not Material's default secondary tint. */
@Composable
internal fun selectedChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

@Composable
internal fun OutlinedCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) { content() }
}

@Composable
internal fun StatusChip(icon: ImageVector, text: String, container: Color, content: Color) {
    Row(
        Modifier.background(container, MaterialTheme.shapes.extraLarge)
            .padding(
                horizontal = KptTheme.spacing.sm + KptTheme.spacing.xs,
                vertical = KptTheme.spacing.xs + KptTheme.spacing.xs / 2,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs + KptTheme.spacing.xs / 2),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(KptTheme.spacing.md))
        Text(text, style = MaterialTheme.typography.labelMedium, color = content)
    }
}

@Composable
internal fun IconTile(icon: ImageVector, container: Color, content: Color) {
    Box(
        Modifier.size(KptTheme.spacing.xl + KptTheme.spacing.sm).background(container, MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = content)
    }
}

@Composable
private fun LoadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(KptTheme.spacing.md).testTag(OfflineSyncTestTags.SKELETON),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        repeat(SKELETON_CARDS) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = KptTheme.spacing.xxl * 2)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large),
            )
        }
    }
}

/** "just now" · "5 min ago" · "2 h ago" · "3 days ago". */
@Composable
internal fun relative(atMs: Long, nowMs: Long): String {
    val minutes = ((nowMs - atMs).coerceAtLeast(0) / MINUTE_MS).toInt()
    return when {
        minutes < 1 -> stringResource(Res.string.feature_offlinesync_time_just_now)
        minutes < MINUTES_PER_HOUR -> stringResource(Res.string.feature_offlinesync_time_minutes, minutes)
        minutes < MINUTES_PER_DAY ->
            stringResource(Res.string.feature_offlinesync_time_hours, minutes / MINUTES_PER_HOUR)
        else -> (minutes / MINUTES_PER_DAY).let {
            pluralStringResource(Res.plurals.feature_offlinesync_time_days, it, it)
        }
    }
}

private const val MAX_FAILURE_ROWS = 5
private const val SKELETON_CARDS = 3
private const val MINUTE_MS = 60_000L
private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 1_440

internal object OfflineSyncTestTags {
    const val SCREEN = "offline_sync_screen"
    const val HEADER = "offline_sync_header"
    const val SYNC_NOW = "offline_sync_sync_now"
    const val OFFLINE_BANNER = "offline_sync_offline_banner"
    const val ACTIVE_JOB = "offline_sync_active_job"
    const val CANCEL_JOB = "offline_sync_cancel_job"
    const val JOB_RESULT = "offline_sync_job_result"
    const val RETRY_FAILED = "offline_sync_retry_failed"
    const val RECORDS = "offline_sync_records"
    const val RECORDS_LEVEL = "offline_sync_records_level"
    const val RECORDS_SEARCH = "offline_sync_records_search"
    const val RECORD_ROW = "offline_sync_record_row"
    const val RECORD_REFRESH = "offline_sync_record_refresh"
    const val RECORD_REMOVE = "offline_sync_record_remove"
    const val REMOVE_CONFIRM = "offline_sync_remove_confirm"
    const val REFRESH_MODE = "offline_sync_refresh_mode"
    const val REFRESH_MODE_TOGGLE = "offline_sync_refresh_mode_toggle"
    const val REFRESH_INTERVAL = "offline_sync_refresh_interval"
    const val WIFI_ONLY = "offline_sync_wifi_only"
    const val CHARGING_ONLY = "offline_sync_charging_only"
    const val DRAFTS_LINK = "offline_sync_drafts_link"
    const val EMPTY = "offline_sync_empty"
    const val ADD_RECORDS = "offline_sync_add_records"
    const val SKELETON = "offline_sync_skeleton"
    const val PICKER = "offline_sync_picker"
    const val PICKER_TABS = "offline_sync_picker_tabs"
    const val PICKER_SEARCH = "offline_sync_picker_search"
    const val PICKER_LIST = "offline_sync_picker_list"
    const val PICKER_ROW = "offline_sync_picker_row"
    const val PICKER_DOWNLOAD = "offline_sync_picker_download"
}
