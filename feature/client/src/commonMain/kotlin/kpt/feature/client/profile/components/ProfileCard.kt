/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fieldofficer.component.AppCard
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppUserImage
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_group_label
import kpt.feature.client.generated.resources.feature_client_group_na
import org.jetbrains.compose.resources.stringResource

/**
 * Ported verbatim from `6b66e8a43:feature/client/clientProfile/components/ProfileCard.kt`.
 *
 * The primary-coloured card at the top of the client profile: avatar, name, "Acc. No. X", office,
 * a group chip, and a trailing chevron — the whole card tappable through to client details. Same
 * colours, same spacing tokens, same typography, same alpha values.
 */
@Composable
internal fun ProfileCard(
    image: ByteArray?,
    name: String,
    accountNo: String,
    office: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    groupName: String? = null,
    onGroupClick: () -> Unit = {},
) {
    AppCard(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.primary,
            contentColor = KptTheme.colorScheme.onPrimary,
            disabledContainerColor = KptTheme.colorScheme.primary,
            disabledContentColor = KptTheme.colorScheme.onPrimary,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppUserImage(
                bitmap = image,
                modifier = Modifier.size(DesignToken.sizes.avatarLarge),
            )
            Spacer(Modifier.width(DesignToken.padding.medium))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
            ) {
                Text(
                    text = name,
                    style = AppTypography.titleMediumEmphasized,
                )
                // Literal prefix, as in the original — not a format string.
                Text(
                    text = "Acc. No. $accountNo",
                    style = AppTypography.bodySmall,
                    color = KptTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
                Text(
                    text = office,
                    style = AppTypography.bodySmall,
                    color = KptTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )

                Spacer(Modifier.height(KptTheme.spacing.sm))

                val groupNa = stringResource(Res.string.feature_client_group_na)
                val displayGroupName = groupName ?: groupNa
                // The chip is tappable only when there is a real group to open — a client with no
                // group gets the chip, not a dead control.
                val isClickable = !groupName.isNullOrBlank() && groupName != groupNa
                val groupLabel = stringResource(Res.string.feature_client_group_label)

                GroupChip(
                    text = "$groupLabel $displayGroupName",
                    isClickable = isClickable,
                    onClick = onGroupClick,
                )
            }
            Spacer(Modifier.width(DesignToken.padding.medium))
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(DesignToken.sizes.iconSmall),
                tint = KptTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun GroupChip(
    text: String,
    isClickable: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(KptTheme.shapes.small)
            .background(color = KptTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
            .clickable(enabled = isClickable, onClick = onClick)
            .padding(horizontal = KptTheme.spacing.sm, vertical = KptTheme.spacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = AppIcons.Group,
                contentDescription = null,
                modifier = Modifier.size(DesignToken.sizes.iconSmall),
                tint = KptTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.width(KptTheme.spacing.xs))
            Text(
                text = text,
                style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = KptTheme.colorScheme.onPrimary,
            )
        }
    }
}
