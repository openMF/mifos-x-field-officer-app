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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import kpt.core.base.designsystem.component.KptShimmerLoadingBox
import kpt.core.base.designsystem.theme.KptTheme
import kpt.feature.home.ui.TestTags

@Composable
internal fun DashboardSkeleton() {
    val c = KptTheme.colorScheme
    Column(Modifier.testTag(TestTags.Home.SKELETON), verticalArrangement = Arrangement.spacedBy(4.tw)) {
        // rounded-2xl bg-primary p-4 — shimmer-indigo bars, progress track, two sub-metrics over a divider
        SkeletonHero()
        // 2×2 bordered h-24 tiles
        Grid(columns = 2, gap = 3.tw) {
            listOf(10 to (16 to 20), 8 to (14 to 24), 12 to (20 to 16), 11 to (16 to 20)).forEach { (tag, lines) ->
                add {
                    Outlined(modifier = Modifier.fillMaxWidth().height(24.tw), padding = 3.5f.tw) {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(Modifier.fillMaxWidth()) {
                                Bar(7.tw, 7.tw, KptTheme.shapes.small)
                                Spacer(Modifier.weight(1f))
                                Bar(tag.tw, 3.5f.tw, KptTheme.shapes.extraSmall)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.tw)) {
                                Bar(lines.first.tw, 5.tw, KptTheme.shapes.extraSmall)
                                Bar(lines.second.tw, 2.5f.tw, KptTheme.shapes.extraSmall)
                            }
                        }
                    }
                }
            }
        }
        // work queue: header bars, then a 3-column grid of six round task cards
        Column(verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.tw)) {
                Bar(28.tw, 4.tw, KptTheme.shapes.extraSmall)
                Bar(7.tw, 4.tw)
                Spacer(Modifier.weight(1f))
                Bar(12.tw, 3.tw, KptTheme.shapes.extraSmall)
            }
            Grid(columns = 3, gap = 2.tw) {
                listOf(12, 14, 10, 12, 14, 11).forEach { w ->
                    add {
                        Outlined(modifier = Modifier.fillMaxWidth(), padding = 2.5f.tw) {
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.tw),
                            ) {
                                Bar(8.tw, 8.tw)
                                Bar(6.tw, 4.tw, KptTheme.shapes.extraSmall)
                                Bar(w.tw, 2.5f.tw, KptTheme.shapes.extraSmall)
                            }
                        }
                    }
                }
            }
        }
        // meetings: header bars, three bordered rows with a status pill and a dot
        Column(verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Bar(32.tw, 4.tw, KptTheme.shapes.extraSmall)
                Spacer(Modifier.weight(1f))
                Bar(16.tw, 3.tw, KptTheme.shapes.extraSmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.tw)) {
                listOf(32 to 20, 28 to 24, 36 to 16).forEach { (title, sub) ->
                    Outlined(modifier = Modifier.fillMaxWidth(), padding = 3.tw) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Bar(10.tw, 10.tw, KptTheme.shapes.small)
                            Column(
                                Modifier.weight(1f).padding(start = 3.tw),
                                verticalArrangement = Arrangement.spacedBy(1.5f.tw),
                            ) {
                                Bar(title.tw, 3.5f.tw, KptTheme.shapes.extraSmall)
                                Bar(sub.tw, 2.5f.tw, KptTheme.shapes.extraSmall)
                            }
                            Bar(12.tw, 5.tw)
                            Spacer(Modifier.width(2.tw))
                            Bar(4.tw, 4.tw)
                        }
                    }
                }
            }
        }
        // portfolio: one bordered card, header bars over a 3-column figure row
        Outlined(modifier = Modifier.fillMaxWidth(), padding = 3.5f.tw) {
            Column(verticalArrangement = Arrangement.spacedBy(3.tw)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Bar(36.tw, 4.tw, KptTheme.shapes.extraSmall)
                    Spacer(Modifier.weight(1f))
                    Bar(10.tw, 3.tw, KptTheme.shapes.extraSmall)
                }
                Row(Modifier.padding(top = 1.tw), horizontalArrangement = Arrangement.spacedBy(3.tw)) {
                    listOf(14 to 16, 16 to 20, 12 to 14).forEach { (label, value) ->
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.5f.tw)) {
                            Bar(label.tw, 2.5f.tw, KptTheme.shapes.extraSmall)
                            Bar(value.tw, 4.tw, KptTheme.shapes.extraSmall)
                        }
                    }
                }
            }
        }
        // quick actions: a title bar over four h-9 chips
        Column(verticalArrangement = Arrangement.spacedBy(2.tw)) {
            Bar(24.tw, 3.5f.tw, KptTheme.shapes.extraSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(2.tw)) {
                repeat(4) { KptShimmerLoadingBox(Modifier.weight(1f).height(9.tw), shape = KptTheme.shapes.small) }
            }
        }
        // recently opened: header bars, two bordered rows with a round avatar
        Column(Modifier.padding(bottom = 2.tw), verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Bar(32.tw, 4.tw, KptTheme.shapes.extraSmall)
                Spacer(Modifier.weight(1f))
                Bar(14.tw, 3.tw, KptTheme.shapes.extraSmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.tw)) {
                listOf(28 to 16, 24 to 20).forEach { (title, sub) ->
                    Outlined(modifier = Modifier.fillMaxWidth(), padding = 2.5f.tw) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Bar(8.tw, 8.tw)
                            Column(
                                Modifier.weight(1f).padding(start = 2.5f.tw),
                                verticalArrangement = Arrangement.spacedBy(1.tw),
                            ) {
                                Bar(title.tw, 3.tw, KptTheme.shapes.extraSmall)
                                Bar(sub.tw, 2.tw, KptTheme.shapes.extraSmall)
                            }
                            Bar(10.tw, 3.tw, KptTheme.shapes.extraSmall)
                        }
                    }
                }
            }
        }
    }
}

/** rounded-2xl bg-primary p-4 — the collections hero while loading: shimmer bars, progress track, two sub-metrics. */
@Composable
private fun SkeletonHero() {
    val c = KptTheme.colorScheme
    Column(Modifier.fillMaxWidth().clip(KptTheme.shapes.large).background(c.primary).padding(4.tw)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 3.tw), verticalAlignment = Alignment.CenterVertically) {
            OnPrimaryBar(28.tw, 4.tw)
            Spacer(Modifier.weight(1f))
            OnPrimaryBar(20.tw, 5.tw)
        }
        OnPrimaryBar(48.tw, 9.tw, KptTheme.shapes.small)
        Spacer(Modifier.height(2.tw))
        OnPrimaryBar(36.tw, 3.tw, KptTheme.shapes.extraSmall)
        Spacer(Modifier.height(4.tw))
        Box(
            Modifier.fillMaxWidth().height(
                2.tw,
            ).clip(CircleShape).background(Color.Black.copy(alpha = SKELETON_TRACK)),
        ) {
            OnPrimaryBar(null, 2.tw, fraction = 2f / 3f)
        }
        Spacer(Modifier.height(4.tw))
        HorizontalDivider(thickness = HAIRLINE, color = c.onPrimary.copy(alpha = SKELETON_RULE))
        Row(Modifier.padding(top = 3.tw), horizontalArrangement = Arrangement.spacedBy(3.tw)) {
            listOf(16 to 24, 20 to 24).forEach { (label, value) ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.5f.tw)) {
                    OnPrimaryBar(label.tw, 3.tw, KptTheme.shapes.extraSmall)
                    OnPrimaryBar(value.tw, 5.tw, KptTheme.shapes.extraSmall)
                }
            }
        }
    }
}

/** One card refreshing on its own — its tiles shimmer under the real title. */
@Composable
internal fun CardSkeleton(id: HomeCardId) {
    Column(Modifier.testTag(TestTags.Home.SKELETON), verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
        val rows = when (id) {
            HomeCardId.MEETINGS -> 2
            HomeCardId.WORK_QUEUE -> 3
            else -> 2
        }
        if (id == HomeCardId.MEETINGS) {
            repeat(rows) {
                Outlined(
                    modifier = Modifier.fillMaxWidth().height(16.tw),
                    padding = 3.tw,
                ) { Bar(32.tw, 3.5f.tw, KptTheme.shapes.extraSmall) }
            }
        } else {
            Grid(columns = 2, gap = 2.5f.tw) {
                repeat(rows * 2) {
                    add {
                        Outlined(modifier = Modifier.fillMaxWidth().height(24.tw), padding = 3.5f.tw) {
                            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Bar(7.tw, 7.tw, KptTheme.shapes.small)
                                Bar(16.tw, 5.tw, KptTheme.shapes.extraSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** `shimmer-light` bar; a null [width] fills the row. */
@Composable
internal fun Bar(width: Dp?, height: Dp, shape: Shape = CircleShape) {
    KptShimmerLoadingBox(
        (if (width == null) Modifier.fillMaxWidth() else Modifier.width(width)).height(height),
        shape = shape,
    )
}

/** `shimmer-indigo` — the bars painted on the brand-coloured hero. */
@Composable
internal fun OnPrimaryBar(width: Dp?, height: Dp, shape: Shape = CircleShape, fraction: Float = 1f) {
    val on = KptTheme.colorScheme.onPrimary
    KptShimmerLoadingBox(
        (if (width == null) Modifier.fillMaxWidth(fraction) else Modifier.width(width)).height(height),
        shape = shape,
        shimmerColor = on.copy(alpha = HERO_SHIMMER),
        highlightColor = on.copy(alpha = HERO_SHIMMER * 2),
    )
}

/** `bg-surface-white rounded-xl border border-border-subtle` — the skeleton's flat tile. */
@Composable
internal fun Outlined(padding: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(KptTheme.shapes.medium)
            .background(KptTheme.colorScheme.surfaceContainerLowest)
            .border(HAIRLINE, KptTheme.colorScheme.surfaceContainerHigh, KptTheme.shapes.medium)
            .padding(padding),
    ) { content() }
}

// ── Primitives ─────────────────────────────────────────────────────────────────────────────────
