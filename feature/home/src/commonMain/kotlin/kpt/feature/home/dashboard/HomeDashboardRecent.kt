/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.recent.RecentKind
import kpt.core.model.recent.RecentRecord
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_cached
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_kind_center
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_kind_client
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_kind_group
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_kind_loan
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_title
import kpt.feature.home.generated.resources.feature_home_dashboard_time_days_ago
import kpt.feature.home.generated.resources.feature_home_dashboard_time_hours_ago
import kpt.feature.home.generated.resources.feature_home_dashboard_time_just_now
import kpt.feature.home.generated.resources.feature_home_dashboard_time_minutes_ago
import kpt.feature.home.generated.resources.feature_home_dashboard_time_yesterday
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

internal val RecentKind.label: StringResource
    get() = when (this) {
        RecentKind.CLIENT -> Res.string.feature_home_dashboard_recent_kind_client
        RecentKind.GROUP -> Res.string.feature_home_dashboard_recent_kind_group
        RecentKind.CENTER -> Res.string.feature_home_dashboard_recent_kind_center
        RecentKind.LOAN -> Res.string.feature_home_dashboard_recent_kind_loan
    }

internal val RecentKind.icon: ImageVector
    get() = when (this) {
        RecentKind.CLIENT -> AppIcons.Person
        RecentKind.GROUP -> AppIcons.Groups
        RecentKind.CENTER -> Icons.Outlined.Domain
        RecentKind.LOAN -> Icons.Outlined.RequestQuote
    }

/** 02: client indigo · group emerald · loan blue · centre amber. */
@Composable
internal fun RecentKind.tint(): Pair<Color, Color> {
    val c = KptTheme.colorScheme
    return when (this) {
        RecentKind.CLIENT -> c.primaryContainer to c.primary
        RecentKind.GROUP -> c.secondaryContainer to c.secondary
        RecentKind.LOAN -> c.primaryContainer to c.onPrimaryContainer
        RecentKind.CENTER -> c.tertiaryContainer to c.tertiary
    }
}

/** "12m ago" / "2h ago" / "Yesterday" — how long since the officer last opened the record. */
@Composable
internal fun ago(openedAtMs: Long): String {
    val minutes = ((Clock.System.now().toEpochMilliseconds() - openedAtMs) / MS_PER_MINUTE).coerceAtLeast(0)
    return when {
        minutes < 1 -> stringResource(Res.string.feature_home_dashboard_time_just_now)
        minutes < MINUTES_PER_HOUR -> stringResource(
            Res.string.feature_home_dashboard_time_minutes_ago,
            minutes.toInt(),
        )
        minutes < MINUTES_PER_DAY -> stringResource(
            Res.string.feature_home_dashboard_time_hours_ago,
            (minutes / MINUTES_PER_HOUR).toInt(),
        )
        minutes < 2 * MINUTES_PER_DAY -> stringResource(Res.string.feature_home_dashboard_time_yesterday)
        else -> stringResource(Res.string.feature_home_dashboard_time_days_ago, (minutes / MINUTES_PER_DAY).toInt())
    }
}

internal fun String.initials(): String =
    split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) }.uppercase()

@Composable
internal fun RecentRecord.subtitle(): String =
    listOfNotNull(detail ?: stringResource(kind.label), accountNo?.let { "#$it" }).joinToString(META_SEPARATOR)

internal enum class RecentStyle { Content, Error, Offline }

/** Hidden until something has been opened. */
@Composable
internal fun RecentCard(recent: List<RecentRecord>, onNavigate: (HomeNavigation) -> Unit, style: RecentStyle) {
    if (recent.isEmpty()) return
    val c = KptTheme.colorScheme
    if (style == RecentStyle.Content) {
        SectionCard(true, Modifier.fillMaxWidth().testTag(TestTags.Home.RECENT)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.feature_home_dashboard_recent_title),
                    Modifier.weight(1f),
                    style = KptTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.onSurface,
                )
                Chip(
                    stringResource(Res.string.feature_home_dashboard_recent_cached, recent.size),
                    c.surfaceContainer,
                    c.onSurfaceVariant,
                    KptTheme.typography.labelSmall,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.tw)) {
                recent.forEach { r -> RecentRowContent(r) { onNavigate(HomeNavigation.Recent(r.kind, r.id)) } }
            }
        }
        return
    }
    Column(
        Modifier.testTag(TestTags.Home.RECENT).padding(top = if (style == RecentStyle.Offline) 2.tw else 0.tw),
        verticalArrangement = Arrangement.spacedBy(if (style == RecentStyle.Error) 2.5f.tw else 3.tw),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val title = stringResource(Res.string.feature_home_dashboard_recent_title)
            when (style) {
                RecentStyle.Content -> {
                    Text(
                        title,
                        Modifier.weight(1f),
                        style = KptTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.onSurface,
                    )
                    Chip(
                        stringResource(Res.string.feature_home_dashboard_recent_cached, recent.size),
                        c.surfaceContainer,
                        c.onSurfaceVariant,
                        KptTheme.typography.labelSmall,
                    )
                }
                else -> {
                    Text(
                        if (style == RecentStyle.Offline) title.uppercase() else title,
                        Modifier.weight(1f),
                        style = KptTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (style == RecentStyle.Offline) WIDER else TIGHT,
                        color = c.onSurface,
                    )
                    Text(
                        stringResource(Res.string.feature_home_dashboard_recent_cached, recent.size),
                        style = KptTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = c.onSurfaceVariant,
                    )
                }
            }
        }
        // 02 rounded-2xl shadow-card · 04 rounded-xl bordered · 05 rounded-2xl bordered — rows split by dividers
        Surface(
            Modifier.fillMaxWidth(),
            shape = if (style == RecentStyle.Error) KptTheme.shapes.medium else KptTheme.shapes.large,
            bordered = style != RecentStyle.Content,
        ) {
            Column {
                recent.forEachIndexed { i, record ->
                    if (i > 0) HorizontalDivider(thickness = HAIRLINE, color = c.surfaceContainerHigh)
                    val open = { onNavigate(HomeNavigation.Recent(record.kind, record.id)) }
                    if (style == RecentStyle.Error) RecentRowError(record, open) else RecentRowOffline(record, open)
                }
            }
        }
    }
}

/**
 * 03: `p-3 rounded-xl bg-surface-container border` · w-8 initials circle (kind/10) · text-sm bold ·
 * time over a chevron.
 */
@Composable
internal fun RecentRowContent(record: RecentRecord, onClick: () -> Unit) {
    val c = KptTheme.colorScheme
    val (fill, ink) = when (record.kind) {
        RecentKind.CLIENT -> c.primary.copy(alpha = INITIALS_FILL) to c.primary
        RecentKind.GROUP -> c.secondary.copy(alpha = INITIALS_FILL) to c.secondary
        RecentKind.LOAN -> c.tertiary.copy(alpha = INITIALS_FILL) to c.tertiary
        RecentKind.CENTER -> c.surfaceContainerHigh to c.onSurfaceVariant
    }
    Row(
        Modifier.fillMaxWidth().clip(KptTheme.shapes.medium).background(c.surfaceContainerLow)
            .border(HAIRLINE, c.surfaceContainerHigh, KptTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick).padding(3.tw).testTag(TestTags.Home.RECENT_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.tw),
    ) {
        Box(Modifier.size(8.tw).background(fill, CircleShape), contentAlignment = Alignment.Center) {
            Text(
                record.title.initials(),
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ink,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                record.title,
                style = KptTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = c.onSurface,
            )
            Text(
                record.subtitle(),
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
                color = c.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                ago(record.openedAtMs),
                style = KptTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                color = c.onSurfaceVariant,
            )
            Icon(
                AppIcons.ChevronRight,
                contentDescription = null,
                tint = c.onSurfaceVariant,
                modifier = Modifier.size(4.5f.tw),
            )
        }
    }
}

/** 04: px-3.5 py-3 min-h-56 · w-8 slate circle · text-xs semibold name + kind chip · text-[11px] meta + chevron. */
@Composable
internal fun RecentRowError(record: RecentRecord, onClick: () -> Unit) {
    val c = KptTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(
            min = 14.tw,
        ).clickable(
            role = Role.Button,
            onClick = onClick,
        ).padding(horizontal = 3.5f.tw, vertical = 3.tw).testTag(TestTags.Home.RECENT_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.tw),
    ) {
        IconBox(
            record.kind.icon,
            c.surfaceContainer,
            c.onSurfaceVariant,
            box = 8.tw,
            glyphSize = 4.5f.tw,
            shape = CircleShape,
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.tw)) {
                Text(
                    record.title,
                    Modifier.weight(1f, fill = false),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = c.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Chip(
                    stringResource(record.kind.label),
                    c.surfaceContainer,
                    c.onSurfaceVariant,
                    KptTheme.typography.labelSmall,
                    weight = FontWeight.Medium,
                    shape = KptTheme.shapes.extraSmall,
                    horizontal = 1.5f.tw,
                    vertical = 0.tw,
                )
            }
            Text(
                record.subtitle(),
                Modifier.padding(top = 0.5f.tw),
                style = KptTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                color = c.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.5f.tw)) {
            Text(
                ago(record.openedAtMs),
                style = KptTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                color = c.onSurfaceVariant,
            )
            Icon(
                AppIcons.ChevronRight,
                contentDescription = null,
                tint = c.onSurfaceVariant,
                modifier = Modifier.size(4.5f.tw),
            )
        }
    }
}

/** 05: p-3.5 · w-10 tinted circle with initials · text-sm bold name + kind chip · text-xs meta + chevron. */
@Composable
internal fun RecentRowOffline(record: RecentRecord, onClick: () -> Unit) {
    val c = KptTheme.colorScheme
    val (tint, glyph) = record.kind.tint()
    Row(
        Modifier.fillMaxWidth().clickable(
            role = Role.Button,
            onClick = onClick,
        ).padding(3.5f.tw).testTag(TestTags.Home.RECENT_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.tw),
    ) {
        Box(Modifier.size(10.tw).background(tint, CircleShape), contentAlignment = Alignment.Center) {
            if (record.kind == RecentKind.LOAN) {
                Icon(AppIcons.Payments, contentDescription = null, tint = glyph, modifier = Modifier.size(4.5f.tw))
            } else {
                Text(
                    record.title.initials(),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = glyph,
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.tw)) {
                Text(
                    record.title,
                    Modifier.weight(1f, fill = false),
                    style = KptTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Chip(
                    stringResource(record.kind.label),
                    c.surfaceContainer,
                    c.onSurfaceVariant,
                    KptTheme.typography.labelSmall,
                    shape = KptTheme.shapes.extraSmall,
                    horizontal = 1.5f.tw,
                    vertical = 0.tw,
                )
            }
            Text(
                record.subtitle(),
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
                color = c.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.5f.tw)) {
            Text(
                ago(record.openedAtMs),
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = c.outline,
            )
            Icon(AppIcons.ChevronRight, contentDescription = null, tint = c.outline, modifier = Modifier.size(4.tw))
        }
    }
}

// ── 03 card error · 04 screen error · 05 offline error + banner ───────────────────────────────
