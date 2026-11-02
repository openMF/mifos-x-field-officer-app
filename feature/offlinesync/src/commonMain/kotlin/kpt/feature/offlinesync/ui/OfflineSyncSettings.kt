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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.offline.RefreshMode
import kpt.core.model.offline.RefreshSettings
import kpt.feature.offlinesync.generated.resources.Res
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_charging_only
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_every_hours
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_refresh_automatic
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_refresh_automatic_note
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_refresh_data_note
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_refresh_manual
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_refresh_manual_note
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_refresh_title
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_resync_label
import kpt.feature.offlinesync.generated.resources.feature_offlinesync_wifi_only
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RefreshCard(settings: RefreshSettings, onAction: (OfflineSyncAction) -> Unit) {
    OutlinedCard(Modifier.testTag(OfflineSyncTestTags.REFRESH_MODE)) {
        Column(
            Modifier.padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
        ) {
            Text(
                stringResource(Res.string.feature_offlinesync_refresh_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag(OfflineSyncTestTags.REFRESH_MODE_TOGGLE)) {
                RefreshMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = settings.mode == mode,
                        onClick = { onAction(OfflineSyncAction.SetMode(mode)) },
                        shape = SegmentedButtonDefaults.itemShape(index, RefreshMode.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Text(
                            stringResource(
                                if (mode == RefreshMode.MANUAL) {
                                    Res.string.feature_offlinesync_refresh_manual
                                } else {
                                    Res.string.feature_offlinesync_refresh_automatic
                                },
                            ),
                        )
                    }
                }
            }
            if (settings.mode == RefreshMode.AUTOMATIC) {
                Text(
                    stringResource(Res.string.feature_offlinesync_resync_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    Modifier.testTag(OfflineSyncTestTags.REFRESH_INTERVAL),
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    RefreshSettings.INTERVALS.forEach { hours ->
                        FilterChip(
                            selected = settings.intervalHours == hours,
                            colors = selectedChipColors(),
                            onClick = { onAction(OfflineSyncAction.SetInterval(hours)) },
                            label = { Text(stringResource(Res.string.feature_offlinesync_every_hours, hours)) },
                        )
                    }
                }
                SwitchRow(
                    Res.string.feature_offlinesync_wifi_only,
                    settings.wifiOnly,
                    OfflineSyncTestTags.WIFI_ONLY,
                ) { onAction(OfflineSyncAction.SetWifiOnly(it)) }
                SwitchRow(
                    Res.string.feature_offlinesync_charging_only,
                    settings.chargingOnly,
                    OfflineSyncTestTags.CHARGING_ONLY,
                ) {
                    onAction(OfflineSyncAction.SetChargingOnly(it))
                }
            }
            Note(
                AppIcons.Info,
                if (settings.mode == RefreshMode.MANUAL) {
                    Res.string.feature_offlinesync_refresh_manual_note
                } else {
                    Res.string.feature_offlinesync_refresh_automatic_note
                },
            )
            Note(AppIcons.CloudDownload, Res.string.feature_offlinesync_refresh_data_note)
        }
    }
}

@Composable
private fun SwitchRow(label: StringResource, checked: Boolean, tag: String, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = KptTheme.spacing.xl + KptTheme.spacing.md).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun Note(icon: ImageVector, text: StringResource) {
    Row(horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm)) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(KptTheme.spacing.md + KptTheme.spacing.xs / 2),
        )
        Text(
            stringResource(text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
