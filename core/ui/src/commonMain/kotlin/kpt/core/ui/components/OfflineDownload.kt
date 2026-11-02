/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.offline.DownloadProgress
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_offline_cancel
import kpt.core.ui.generated.resources.core_ui_offline_clear_selection
import kpt.core.ui.generated.resources.core_ui_offline_current
import kpt.core.ui.generated.resources.core_ui_offline_done
import kpt.core.ui.generated.resources.core_ui_offline_download
import kpt.core.ui.generated.resources.core_ui_offline_downloaded
import kpt.core.ui.generated.resources.core_ui_offline_downloading
import kpt.core.ui.generated.resources.core_ui_offline_failures
import kpt.core.ui.generated.resources.core_ui_offline_progress
import kpt.core.ui.generated.resources.core_ui_offline_selected
import org.jetbrains.compose.resources.stringResource

/*
 * The offline-download affordances the center and group lists share — ported from the original's
 * `SelectionModeTopAppBar` (selection count + Sync) and `Sync{Centers,Groups}Dialog` (progress), which
 * existed as two near-identical copies.
 */

/** Shown while list rows are selected: how many, a download action, and a way out of selection. */
@Composable
fun OfflineSelectionBar(
    count: Int,
    onDownload: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = KptTheme.colorScheme.secondaryContainer,
        shape = DesignToken.shapes.medium,
        modifier = modifier.fillMaxWidth().testTag(OfflineDownloadTestTags.SELECTION_BAR),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClear, modifier = Modifier.testTag(OfflineDownloadTestTags.CLEAR)) {
                Icon(AppIcons.Close, contentDescription = stringResource(Res.string.core_ui_offline_clear_selection))
            }
            Text(
                text = stringResource(Res.string.core_ui_offline_selected, count),
                style = KptTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDownload, modifier = Modifier.testTag(OfflineDownloadTestTags.DOWNLOAD)) {
                Icon(AppIcons.CloudDownload, contentDescription = stringResource(Res.string.core_ui_offline_download))
            }
        }
    }
}

/**
 * Progress of a download. While running it can be cancelled (what is already downloaded stays); once
 * finished it says what could not be downloaded, by name and with the server's reason when it gave one.
 */
@Composable
fun OfflineDownloadDialog(
    progress: DownloadProgress,
    onCancel: () -> Unit,
    onDone: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (progress.finished) onDone() },
        title = {
            Text(
                stringResource(
                    if (progress.finished) {
                        Res.string.core_ui_offline_downloaded
                    } else {
                        Res.string.core_ui_offline_downloading
                    },
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm)) {
                LinearProgressIndicator(
                    progress = { if (progress.total == 0) 0f else progress.completed.toFloat() / progress.total },
                    modifier = Modifier.fillMaxWidth().testTag(OfflineDownloadTestTags.PROGRESS),
                )
                Text(
                    text = stringResource(
                        Res.string.core_ui_offline_progress,
                        progress.completed,
                        progress.total,
                        progress.records,
                    ),
                    style = KptTheme.typography.bodyMedium,
                )
                progress.current?.let {
                    Text(
                        text = stringResource(Res.string.core_ui_offline_current, it),
                        style = KptTheme.typography.bodySmall,
                    )
                }
                if (progress.failures.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.core_ui_offline_failures, progress.failures.size),
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.error,
                    )
                    Column(
                        modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState())
                            .testTag(OfflineDownloadTestTags.FAILURES),
                    ) {
                        progress.failures.forEach { failure ->
                            Text(
                                text = failure.reason?.let { "${failure.item} — $it" } ?: failure.item,
                                style = KptTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (progress.finished) {
                TextButton(onClick = onDone, modifier = Modifier.testTag(OfflineDownloadTestTags.DONE)) {
                    Text(stringResource(Res.string.core_ui_offline_done))
                }
            } else {
                TextButton(onClick = onCancel, modifier = Modifier.testTag(OfflineDownloadTestTags.CANCEL)) {
                    Text(stringResource(Res.string.core_ui_offline_cancel))
                }
            }
        },
        modifier = Modifier.testTag(OfflineDownloadTestTags.DIALOG),
    )
}

object OfflineDownloadTestTags {
    const val SELECTION_BAR = "offline:selectionBar"
    const val CLEAR = "offline:clear"
    const val DOWNLOAD = "offline:download"
    const val DIALOG = "offline:dialog"
    const val PROGRESS = "offline:progress"
    const val FAILURES = "offline:failures"
    const val CANCEL = "offline:cancel"
    const val DONE = "offline:done"
}
