/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.identity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import kpt.core.base.designsystem.component.KptShimmerLoadingBox
import kpt.core.base.designsystem.theme.KptTheme

// Shared list pieces (Stitch: white 16dp-radius bordered cards, 56dp rows, quiet headings).

@Composable
internal fun Section(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm)) {
        Text(
            title,
            style = KptTheme.typography.titleSmall,
            color = KptTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = KptTheme.spacing.xs),
        )
        Group(content = content)
    }
}

@Composable
internal fun Group(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val c = KptTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .background(c.surface, KptTheme.shapes.large)
            .border(KptTheme.spacing.xs / 4, c.outlineVariant, KptTheme.shapes.large),
    ) { content() }
}

@Composable
internal fun Divider() = HorizontalDivider(color = KptTheme.colorScheme.surfaceVariant)

@Composable
internal fun Item(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    loading: Boolean = false,
) {
    val c = KptTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN)
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Icon(icon, contentDescription = null, tint = c.onSurfaceVariant)
        Column(Modifier.weight(1f)) {
            Text(label, style = KptTheme.typography.bodySmall, color = c.onSurfaceVariant)
            if (loading) {
                KptShimmerLoadingBox(
                    Modifier.fillMaxWidth(0.5f).heightIn(min = KptTheme.spacing.md + KptTheme.spacing.xs),
                )
            } else {
                Text(value, style = KptTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                supporting?.let {
                    Text(
                        it,
                        style = KptTheme.typography.bodySmall,
                        color = c.onSurfaceVariant,
                        modifier = Modifier.padding(top = KptTheme.spacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
internal fun ActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    supporting: String? = null,
    enabled: Boolean = true,
    chevron: Boolean = true,
) {
    val c = KptTheme.colorScheme
    val tint = when {
        !enabled -> c.outline
        color != Color.Unspecified -> color
        else -> c.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .heightIn(min = ROW_MIN)
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = KptTheme.typography.bodyLarge,
                fontWeight = if (color != Color.Unspecified) FontWeight.SemiBold else FontWeight.Medium,
                color = if (enabled) color.takeOrElse(c.onSurface) else c.outline,
            )
            supporting?.let { Text(it, style = KptTheme.typography.bodySmall, color = c.onSurfaceVariant) }
        }
        if (chevron && enabled) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = c.onSurfaceVariant)
        }
    }
}

internal fun Color.takeOrElse(other: Color): Color = if (this == Color.Unspecified) other else this

/** 56dp list rows (Stitch). */
internal val ROW_MIN: Dp
    @Composable get() = KptTheme.spacing.xxl - KptTheme.spacing.sm
