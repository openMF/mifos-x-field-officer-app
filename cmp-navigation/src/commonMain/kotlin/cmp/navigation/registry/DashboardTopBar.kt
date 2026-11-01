/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.cd_notifications
import cmp.navigation.generated.resources.cd_search
import cmp.navigation.generated.resources.shell_status_offline
import cmp.navigation.generated.resources.shell_status_online
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.AppInfo
import kpt.core.designsystem.fieldofficer.component.AppCard
import kpt.core.designsystem.generated.resources.topbarlogo
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.finance
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kpt.core.designsystem.generated.resources.Res as DesignRes

/**
 * The dashboard top bar — ported from `6b66e8a43:cmp-navigation/components/MifosTopAppBar.kt`.
 *
 * ## Ported, not redesigned
 *
 * An earlier pass rebuilt this bar from the template's idiom instead of porting the original:
 * `containerColor = colorScheme.primary` with `onPrimary` content, and the app name stacked under
 * the logo as `labelMedium`. That produced a dark-blue bar — legible, themed, and not the app. The
 * original's bar is LIGHT (`surfaceContainerLowest`) with `onSurface` icons and the wordmark beside
 * the logo in its own brand blue, sitting in a card with rounded bottom corners. Every value below
 * comes from that file; none of them are a judgement call made here.
 *
 * The one deliberate difference is the SEARCH action, which the original also had — it just led to
 * the first TAB rather than to a destination, because Search was tab one. See
 * `AuthenticatedNavBarTabItem.HomeTab` for why that moved.
 *
 * ## Brand-neutral on purpose
 *
 * The original hardcodes a "Mifos" string resource and a literal `AppColors.titleColorTopAbbBar`
 * (0xFF0773BB). This shell is white-label, so neither is copied: the name comes from
 * `AppInfo.appDisplayName` (app-profile → BuildKonfig) and the colour from `colorScheme.primary`.
 * The brand hex still exists — it lives in this fork's palette, which is where a brand value goes.
 * The LOGO is a fork-supplied drawable, carried across from `6b66e8a43`;
 * another fork replaces the file, not this code.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardTopBar(
    onSearchIconClick: () -> Unit,
    onNotificationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Live connectivity, from the app's NetworkMonitor at the call site (kept a value so previews need no DI). */
    online: Boolean = true,
) {
    AppCard(
        shape = RoundedCornerShape(
            bottomStart = DesignToken.spacing.large,
            bottomEnd = DesignToken.spacing.large,
        ),
        elevation = DesignToken.elevation.appBar,
        modifier = Modifier.fillMaxWidth().testTag(DASHBOARD_TOP_BAR_TAG),
    ) {
        // Stitch shell (mockups/home/stitch/*): the brand sits LEFT, beside it the live connection pill
        // (Online / Offline) — the one shell element the officer reads before trusting a figure.
        TopAppBar(
            modifier = modifier,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(DesignRes.drawable.topbarlogo),
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(DesignToken.spacing.mediumSmall))
                    Text(
                        // The FORK's display name, from app-profile via BuildKonfig — not a
                        // hardcoded wordmark. The original read a literal "Mifos" string resource,
                        // which is the one thing in that file a white-label shell must not copy.
                        text = AppInfo.appDisplayName,
                        style = KptTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        // `primary`, not the original's literal 0xFF0773BB. The hex is a brand
                        // value and belongs in the palette, where `Color.kt` already carries it for
                        // this fork; reading it from the theme is what lets another fork's name
                        // come out in that fork's colour.
                        color = KptTheme.colorScheme.primary,
                    )
                }
            },
            actions = {
                // In the actions slot, not the title: the title is what shrinks on a narrow / large-font
                // phone, and a squeezed pill wrapped to one letter per line on device (2026-10-27).
                ConnectionPill(online)
                IconButton(
                    onClick = onSearchIconClick,
                    modifier = Modifier.testTag(DASHBOARD_SEARCH_ACTION_TAG),
                ) {
                    Icon(
                        AppIcons.Search,
                        contentDescription = stringResource(Res.string.cd_search),
                    )
                }
                IconButton(onClick = onNotificationIconClick) {
                    Icon(
                        AppIcons.Notifications,
                        contentDescription = stringResource(Res.string.cd_notifications),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = KptTheme.colorScheme.surfaceContainerLowest,
                actionIconContentColor = KptTheme.colorScheme.onSurface,
            ),
            expandedHeight = DesignToken.sizes.topAppBarHeight,
        )
    }
}

/** "● Online" / "● Offline". */
@Composable
private fun ConnectionPill(online: Boolean) {
    val f = MaterialTheme.finance
    val (container, content) = if (online) {
        f.moneyPositiveContainer to f.moneyPositive
    } else {
        f.moneyNegativeContainer to f.moneyNegative
    }
    Row(
        modifier = Modifier
            .background(container, CircleShape)
            .padding(horizontal = KptTheme.spacing.sm, vertical = KptTheme.spacing.xs)
            .testTag(DASHBOARD_CONNECTION_TAG),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
    ) {
        Box(Modifier.size(KptTheme.spacing.sm - KptTheme.spacing.xs / 2).background(content, CircleShape))
        Text(
            stringResource(if (online) Res.string.shell_status_online else Res.string.shell_status_offline),
            maxLines = 1,
            softWrap = false,
            style = KptTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
    }
}

internal const val DASHBOARD_CONNECTION_TAG: String = "shell:top-bar:connection"
internal const val DASHBOARD_TOP_BAR_TAG: String = "shell:top-bar"
internal const val DASHBOARD_SEARCH_ACTION_TAG: String = "shell:top-bar:search"
