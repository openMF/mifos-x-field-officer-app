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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.em
import kpt.core.base.designsystem.theme.KptTheme

/** A Tailwind spacing unit (`p-3`, `w-11`, `gap-2.5` …) — one unit is the theme's `spacing.xs` (4 dp). */
internal val Int.tw: Dp @Composable get() = KptTheme.spacing.xs * this
internal val Float.tw: Dp @Composable get() = KptTheme.spacing.xs * this

/**
 * `bg-surface-card rounded-xl shadow-card` (or, [bordered], `bg-white border border-outline-variant
 * shadow-sm`) with [padding] inside. Clickable when [onClick] is given.
 */
@Composable
internal fun Surface(
    modifier: Modifier = Modifier,
    padding: Dp = 0.tw,
    shape: Shape = KptTheme.shapes.medium,
    color: Color = KptTheme.colorScheme.surfaceContainerLowest,
    border: BorderStroke? = null,
    bordered: Boolean = false,
    elevation: Dp = CARD_SHADOW,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val edge = border ?: if (bordered) BorderStroke(HAIRLINE, KptTheme.colorScheme.surfaceContainerHigh) else null
    val colors = CardDefaults.cardColors(containerColor = color)
    val lift = CardDefaults.cardElevation(defaultElevation = elevation)
    val inner: @Composable () -> Unit = { Box(Modifier.padding(padding)) { content() } }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, elevation = lift, border = edge) {
            inner()
        }
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = lift, border = edge) { inner() }
    }
}

@Composable
internal fun IconBox(
    icon: ImageVector,
    tint: Color,
    glyph: Color,
    box: Dp,
    glyphSize: Dp,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    Box(modifier.size(box).background(tint, shape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = glyph, modifier = Modifier.size(glyphSize))
    }
}

/** `px-2 py-0.5 rounded-full` label chip (optionally bordered / with a leading glyph). */
@Composable
internal fun Chip(
    text: String,
    container: Color,
    content: Color,
    style: androidx.compose.ui.text.TextStyle,
    weight: FontWeight = FontWeight.SemiBold,
    shape: Shape = CircleShape,
    horizontal: Dp = 2.tw,
    vertical: Dp = 0.5f.tw,
    border: Color? = null,
    leading: ImageVector? = null,
) {
    Row(
        Modifier
            .clip(shape)
            .background(container)
            .then(if (border != null) Modifier.border(HAIRLINE, border, shape) else Modifier)
            .padding(horizontal = horizontal, vertical = vertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.tw),
    ) {
        leading?.let { Icon(it, contentDescription = null, tint = content, modifier = Modifier.size(3.25f.tw)) }
        Text(text, style = style, fontWeight = weight, color = content, maxLines = 1)
    }
}

@Composable
internal fun Figure(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = KptTheme.typography.bodyLarge,
    color: Color = KptTheme.colorScheme.onSurface,
    weight: FontWeight = FontWeight.Bold,
) = Text(text, modifier = modifier, style = style, fontWeight = weight, color = color)

/** Equal-width tiles, [columns] per row, [gap] both ways; a short last row keeps the tile width. */
@Composable
internal fun Grid(
    columns: Int,
    gap: Dp,
    modifier: Modifier = Modifier,
    content: MutableList<@Composable () -> Unit>.() -> Unit,
) {
    val tiles = buildList(content)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        tiles.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { tile -> Box(Modifier.weight(1f).fillMaxHeight()) { tile() } }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

internal const val NOON = 12
internal const val HTTP_UNAUTHORIZED = 401
internal const val HTTP_FORBIDDEN = 403
internal const val HTTP_NOT_FOUND = 404
internal const val EVENING = 17

/** The hour every preview greets at ("Good evening"), so a golden is the same whenever it is rendered. */
internal const val PREVIEW_HOUR = 18
internal const val HOURS_HALF_DAY = 12
internal const val META_SEPARATOR = " · "
internal const val META_DOT = "·"
internal const val PERCENT_TENTHS = 1000
internal const val MS_PER_MINUTE = 60_000L
internal const val MINUTES_PER_HOUR = 60L
internal const val MINUTES_PER_DAY = 1_440L

/**
 * Portfolio-at-risk benchmark the PAR tile is read against (Stitch: "tgt <5%"). PAR30 under 5% is the
 * common microfinance health line; one constant so an institution with a different policy changes one place.
 */
internal const val PAR_TARGET = 0.05

// Tailwind opacity steps and tracking the markup uses (`/20`, `/80`, `tracking-wider` …).
internal const val HERO_SUB = 0.9f // text-blue-100/90
internal const val HERO_PILL = 0.2f // bg-white/20
internal const val HERO_DISC = 0.1f // bg-white/10
internal const val HERO_SHIMMER = 0.18f // shimmer-indigo
internal const val TRACK_ALPHA = 0.2f // bg-black/20
internal const val SKELETON_TRACK = 0.15f // bg-black/15
internal const val SKELETON_RULE = 0.25f // border-indigo-400/25
internal const val DRAFTS_FILL = 0.7f // bg-amber-50/70
internal const val DRAFTS_EDGE = 0.3f // border-amber-200/60
internal const val DRAFTS_BOX = 0.35f // bg-amber-200
internal const val ERROR_FILL = 0.8f // bg-red-50/80 · bg-emerald-50/80
internal const val EDGE_ALPHA = 0.25f // border-*-200
internal const val SOFT_EDGE_ALPHA = 0.15f // border-rose-100 · border-red-100
internal const val TILE_FILL = 0.4f // bg-surface-variant/40
internal const val TINT_FILL = 0.5f // bg-red-50/50 · bg-amber-50/50
internal const val OUTSTANDING_FILL = 0.4f // bg-primary-container/40
internal const val INITIALS_FILL = 0.1f // bg-primary/10
internal val TIGHT = (-0.025f).em // tracking-tight
internal val WIDER = 0.05f.em // tracking-wider

internal val CARD_SHADOW: Dp @Composable get() = KptTheme.elevation.level1 // shadow-card / shadow-sm
internal val ELEVATED_SHADOW: Dp @Composable get() = KptTheme.elevation.level3 // shadow-elevated
internal val HAIRLINE: Dp @Composable get() = KptTheme.spacing.xs / 4
