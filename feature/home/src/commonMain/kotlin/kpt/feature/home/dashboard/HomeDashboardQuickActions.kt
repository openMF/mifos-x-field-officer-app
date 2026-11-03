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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.DomainAdd
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AccentColors
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_enabled
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_collection_sheet
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_new_center
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_new_client
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_new_group
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_path_tracker
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_run_reports
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_search
import kpt.feature.home.generated.resources.feature_home_dashboard_quick_actions_available
import kpt.feature.home.generated.resources.feature_home_dashboard_quick_actions_title
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal val QuickAction.label: StringResource
    get() = when (this) {
        QuickAction.NEW_CLIENT -> Res.string.feature_home_dashboard_qa_new_client
        QuickAction.NEW_GROUP -> Res.string.feature_home_dashboard_qa_new_group
        QuickAction.NEW_CENTER -> Res.string.feature_home_dashboard_qa_new_center
        QuickAction.COLLECTION_SHEET -> Res.string.feature_home_dashboard_qa_collection_sheet
        QuickAction.RUN_REPORTS -> Res.string.feature_home_dashboard_qa_run_reports
        QuickAction.SEARCH -> Res.string.feature_home_dashboard_qa_search
        QuickAction.PATH_TRACKER -> Res.string.feature_home_dashboard_qa_path_tracker
    }

internal val QuickAction.icon: ImageVector
    get() = when (this) {
        QuickAction.NEW_CLIENT -> Icons.Outlined.PersonAdd
        QuickAction.NEW_GROUP -> AppIcons.Groups
        QuickAction.NEW_CENTER -> Icons.Outlined.Domain
        QuickAction.COLLECTION_SHEET -> Icons.AutoMirrored.Outlined.FactCheck
        QuickAction.RUN_REPORTS -> Icons.Outlined.BarChart
        QuickAction.SEARCH -> AppIcons.Search
        QuickAction.PATH_TRACKER -> AppIcons.PersonPinCircle
    }

internal val QuickAction.tag: String
    get() = when (this) {
        QuickAction.NEW_CLIENT -> TestTags.Home.QA_CLIENT
        QuickAction.NEW_GROUP -> TestTags.Home.QA_GROUP
        QuickAction.NEW_CENTER -> TestTags.Home.QA_CENTER
        QuickAction.COLLECTION_SHEET -> TestTags.Home.QA_COLLECTION
        QuickAction.RUN_REPORTS -> TestTags.Home.QA_REPORTS
        QuickAction.SEARCH -> TestTags.Home.QA_SEARCH
        QuickAction.PATH_TRACKER -> TestTags.Home.QA_PATH_TRACKER
    }

/** 05 gives each action its own tint (brand · indigo · blue · amber · purple · slate). */
@Composable
internal fun QuickAction.tint(): Pair<Color, Color> {
    val c = KptTheme.colorScheme
    return when (this) {
        QuickAction.NEW_CLIENT -> c.primaryContainer to c.primary
        QuickAction.NEW_GROUP -> c.primaryContainer to c.primary
        QuickAction.NEW_CENTER -> AccentColors.blueContainer to AccentColors.blue
        QuickAction.COLLECTION_SHEET -> c.tertiaryContainer to c.tertiary
        QuickAction.RUN_REPORTS -> AccentColors.purpleContainer to AccentColors.purple
        QuickAction.SEARCH -> c.surfaceContainer to c.onSurfaceVariant
        QuickAction.PATH_TRACKER -> c.secondaryContainer to c.secondary
    }
}

internal enum class QuickStyle { Content, Error, Offline }

/** 03: client indigo · group emerald · centre amber · sheet indigo-50 · reports + search slate. */
@Composable
internal fun QuickAction.cardTint(): Pair<Color, Color> {
    val c = KptTheme.colorScheme
    return when (this) {
        QuickAction.NEW_CLIENT -> c.primaryContainer to c.primary
        QuickAction.NEW_GROUP -> c.secondaryContainer to c.secondary
        QuickAction.NEW_CENTER -> c.tertiaryContainer to c.tertiary
        QuickAction.COLLECTION_SHEET -> c.primaryContainer to c.primary
        QuickAction.RUN_REPORTS -> c.surfaceContainer to c.onSurfaceVariant
        QuickAction.SEARCH -> c.surfaceContainer to c.onSurfaceVariant
        QuickAction.PATH_TRACKER -> c.secondaryContainer to c.secondary
    }
}

internal val QuickAction.cardIcon: ImageVector
    get() = when (this) {
        QuickAction.NEW_CLIENT -> Icons.Outlined.PersonAdd
        QuickAction.NEW_GROUP -> Icons.Outlined.GroupAdd
        QuickAction.NEW_CENTER -> Icons.Outlined.DomainAdd
        QuickAction.COLLECTION_SHEET -> Icons.AutoMirrored.Outlined.ReceiptLong
        QuickAction.RUN_REPORTS -> Icons.Outlined.Analytics
        QuickAction.SEARCH -> AppIcons.Search
        QuickAction.PATH_TRACKER -> AppIcons.PersonPinCircle
    }

@Composable
internal fun QuickActions(actions: List<QuickAction>, onNavigate: (HomeNavigation) -> Unit, style: QuickStyle) {
    if (actions.isEmpty()) return
    val c = KptTheme.colorScheme
    if (style == QuickStyle.Content) {
        SectionCard(true, Modifier.fillMaxWidth().testTag(TestTags.Home.QUICK_ACTIONS)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.feature_home_dashboard_quick_actions_title),
                    Modifier.weight(1f),
                    style = KptTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.onSurface,
                )
                Chip(
                    stringResource(Res.string.feature_home_dashboard_quick_actions_available, actions.size),
                    c.secondaryContainer,
                    c.secondary,
                    KptTheme.typography.labelSmall,
                    weight = FontWeight.Bold,
                )
            }
            // grid-cols-3 gap-2 · p-3 rounded-xl bg-surface-container border · w-9 rounded-full · text-xs semibold
            Grid(columns = 3, gap = 2.tw) {
                actions.forEach { action ->
                    add {
                        val (tint, glyph) = action.cardTint()
                        Box(
                            Modifier.fillMaxSize().heightIn(
                                min = 12.tw,
                            ).clip(KptTheme.shapes.medium).background(c.surfaceContainerLow)
                                .border(HAIRLINE, c.surfaceContainerHigh, KptTheme.shapes.medium)
                                .clickable(role = Role.Button) { onNavigate(HomeNavigation.Quick(action)) }
                                .padding(3.tw).testTag(action.tag),
                        ) {
                            QuickTileBody(
                                action.cardIcon,
                                stringResource(
                                    action.label,
                                ),
                                tint,
                                glyph,
                                box = 9.tw,
                                glyph = 5.tw,
                                shape = CircleShape,
                                gap = 1.5f.tw,
                                weight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
        return
    }
    Column(
        Modifier.testTag(TestTags.Home.QUICK_ACTIONS).padding(top = if (style == QuickStyle.Offline) 2.tw else 0.tw),
        verticalArrangement = Arrangement.spacedBy(3.tw),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val title = stringResource(Res.string.feature_home_dashboard_quick_actions_title)
            when (style) {
                // 04: text-sm bold · 02/05: text-sm bold uppercase tracking-wider
                QuickStyle.Error -> Text(
                    title,
                    Modifier.weight(1f),
                    style = KptTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = TIGHT,
                    color = c.onSurface,
                )
                else -> Text(
                    title.uppercase(),
                    Modifier.weight(1f),
                    style = KptTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = WIDER,
                    color = c.onSurface,
                )
            }
            // 04 / 05: the actions work without a network — every create keeps a draft and syncs later.
            run {
                Chip(
                    stringResource(Res.string.feature_home_dashboard_offline_enabled),
                    container = c.secondaryContainer,
                    content = c.secondary,
                    style = KptTheme.typography.labelSmall,
                    border = c.secondary.copy(alpha = EDGE_ALPHA),
                    leading = if (style == QuickStyle.Error) AppIcons.CloudOff else null,
                )
            }
        }
        Grid(columns = 3, gap = 2.5f.tw) {
            actions.forEach { action ->
                add {
                    val label = stringResource(action.label)
                    val onClick = { onNavigate(HomeNavigation.Quick(action)) }
                    when (style) {
                        // 04: bordered min-h-[76px] tile · w-9 rounded-lg primary-container · text-xs medium
                        QuickStyle.Error -> Surface(
                            Modifier.fillMaxSize().heightIn(min = 19.tw).testTag(action.tag),
                            padding = 3.tw,
                            bordered = true,
                            onClick = onClick,
                        ) {
                            QuickTileBody(
                                action.icon,
                                label,
                                c.primaryContainer,
                                c.primary,
                                box = 9.tw,
                                glyph = 5.tw,
                                shape = KptTheme.shapes.small,
                                gap = 1.5f.tw,
                                weight = FontWeight.Medium,
                            )
                        }
                        // 05: bordered tile · w-10 rounded-xl, tinted per action · text-xs semibold
                        else -> Surface(
                            Modifier.fillMaxSize().testTag(action.tag),
                            padding = 3.tw,
                            bordered = true,
                            onClick = onClick,
                        ) {
                            val (tint, glyph) = action.tint()
                            QuickTileBody(
                                action.icon,
                                label,
                                tint,
                                glyph,
                                box = 10.tw,
                                glyph = 5.tw,
                                shape = KptTheme.shapes.medium,
                                gap = 2.tw,
                                weight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun QuickTileBody(
    icon: ImageVector,
    label: String,
    tint: Color,
    glyphColor: Color,
    box: Dp,
    glyph: Dp,
    shape: Shape,
    gap: Dp,
    weight: FontWeight,
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IconBox(icon, tint, glyphColor, box = box, glyphSize = glyph, shape = shape)
        Spacer(Modifier.height(gap))
        Text(
            label,
            style = KptTheme.typography.labelMedium,
            fontWeight = weight,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = KptTheme.colorScheme.onSurface,
        )
    }
}

// ── #recent_card (02 content · 04 error · 05 offline) ──────────────────────────────────────────
