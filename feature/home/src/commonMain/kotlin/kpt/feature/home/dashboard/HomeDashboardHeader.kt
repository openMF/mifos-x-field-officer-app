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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kpt.core.base.designsystem.component.KptShimmerLoadingBox
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.finance
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_dashboard_greeting_afternoon
import kpt.feature.home.generated.resources.feature_home_dashboard_greeting_evening
import kpt.feature.home.generated.resources.feature_home_dashboard_greeting_morning
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_label
import kpt.feature.home.generated.resources.feature_home_dashboard_profile
import kpt.feature.home.generated.resources.feature_home_dashboard_settings
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

/** "Mon 26 Oct 2026" — English names, as every date in the app is today (DateHelper). */
internal val headerDate = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
}

/** "Oct 26" — the meetings header. */
internal val shortDate = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
}

internal fun String.headerDate(): String = runCatching { headerDate.format(LocalDate.parse(this)) }.getOrDefault(this)
internal fun String.shortDate(): String = runCatching { shortDate.format(LocalDate.parse(this)) }.getOrDefault(this)

internal enum class HeaderStyle { Loading, Content, Error, Offline }

@Composable
internal fun Header(header: HomeHeader, style: HeaderStyle, onSettings: () -> Unit, onProfile: () -> Unit) {
    // A preview renders a fixed hour, so its golden does not change with the time of day it was recorded.
    val hour = if (LocalInspectionMode.current) {
        PREVIEW_HOUR
    } else {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    }
    val greeting = when {
        hour < NOON -> Res.string.feature_home_dashboard_greeting_morning
        hour < EVENING -> Res.string.feature_home_dashboard_greeting_afternoon
        else -> Res.string.feature_home_dashboard_greeting_evening
    }
    val c = KptTheme.colorScheme
    val parts = listOfNotNull(
        header.officeName.takeIf { it.isNotBlank() },
        header.businessDate?.headerDate(),
        header.instanceName?.takeUnless { style == HeaderStyle.Offline || it.isBlank() },
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 1.tw).testTag(TestTags.Home.HEADER),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(if (style == HeaderStyle.Loading) 1.5f.tw else 1.tw),
        ) {
            Text(
                text = stringResource(greeting, header.name.toNameCase()),
                // 01 text-xl bold · 02/04 text-2xl bold · 05 text-2xl extrabold
                style = if (style == HeaderStyle.Loading) {
                    KptTheme.typography.titleLarge
                } else {
                    KptTheme.typography.headlineSmall
                },
                fontWeight = if (style == HeaderStyle.Offline) FontWeight.ExtraBold else FontWeight.Bold,
                color = c.onSurface,
                letterSpacing = TIGHT,
            )
            HeaderMeta(parts, style)
        }
        HeaderAvatar(header, style, onProfile)
        // Settings sits inline with the greeting — the app has no drawer. Hidden while loading.
        if (style != HeaderStyle.Loading) {
            IconButton(onClick = onSettings, modifier = Modifier.testTag(TestTags.Home.SETTINGS)) {
                Icon(
                    AppIcons.Settings,
                    contentDescription = stringResource(Res.string.feature_home_dashboard_settings),
                    tint = c.onSurfaceVariant,
                )
            }
        }
    }
}

/** The meta line under the greeting — office, business date and instance, styled per state. */
@Composable
private fun HeaderMeta(parts: List<String>, style: HeaderStyle) {
    val c = KptTheme.colorScheme
    when (style) {
        // 01: one line, text-[11px] medium
        HeaderStyle.Loading -> Text(
            parts.joinToString(META_SEPARATOR),
            style = KptTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = c.onSurfaceVariant,
        )
        // 02: text-xs medium, parts split by 1-unit slate dots
        HeaderStyle.Content -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.tw),
        ) {
            parts.forEachIndexed { i, part ->
                if (i > 0) Box(Modifier.size(1.tw).background(c.outlineVariant, CircleShape))
                Text(
                    part,
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = c.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (i == parts.lastIndex) Modifier.weight(1f, fill = false) else Modifier,
                )
            }
        }
        // 04: a cell-tower glyph in amber leads the meta line
        HeaderStyle.Error -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.5f.tw),
        ) {
            Icon(
                Icons.Outlined.CellTower,
                contentDescription = null,
                tint = c.tertiary,
                modifier = Modifier.size(3.5f.tw),
            )
            Text(
                parts.joinToString(META_SEPARATOR),
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = c.onSurfaceVariant,
            )
        }
        // 05: "· Offline" in rose semibold where the instance name would be
        HeaderStyle.Offline -> {
            val offlineLabel = stringResource(Res.string.feature_home_dashboard_offline_label)
            val negative = MaterialTheme.finance.moneyNegative
            Text(
                buildAnnotatedString {
                    append(parts.joinToString(META_SEPARATOR))
                    append(META_SEPARATOR)
                    withStyle(
                        SpanStyle(color = negative, fontWeight = FontWeight.SemiBold),
                    ) { append(offlineLabel) }
                },
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = c.onSurfaceVariant,
            )
        }
    }
}

/** The trailing avatar: shimmering while loading, initials once loaded, absent on the error states. */
@Composable
private fun HeaderAvatar(header: HomeHeader, style: HeaderStyle, onProfile: () -> Unit) {
    val c = KptTheme.colorScheme
    val profileLabel = stringResource(Res.string.feature_home_dashboard_profile)
    when (style) {
        // 01: the avatar shimmers like everything else that hasn't loaded
        HeaderStyle.Loading -> KptShimmerLoadingBox(
            Modifier.size(11.tw).border(HAIRLINE, c.surfaceContainerHigh, CircleShape),
            shape = CircleShape,
        )
        // 02: w-11 h-11 primary-container, ring-2 white, shadow-card
        HeaderStyle.Content -> Box(
            modifier = Modifier
                .size(11.tw)
                .shadow(CARD_SHADOW, CircleShape)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onProfile)
                .semantics { contentDescription = profileLabel }
                .testTag(TestTags.Home.PROFILE)
                .background(c.primaryContainer, CircleShape)
                .border(0.5f.tw, c.surfaceContainerLowest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                header.initials.ifEmpty {
                    header.name.take(1).uppercase()
                },
                style = KptTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = c.primary,
            )
        }
        // 04 / 05 carry no avatar
        else -> Unit
    }
}

/** Fineract usernames are often upper-case ("IRVING"); the greeting reads them as a name. */
internal fun String.toNameCase(): String = if (this == uppercase()) {
    lowercase().split(
        ' ',
    ).joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
} else {
    this
}

// ── Section chrome (02: h2 text-lg bold · summary text-xs · trailing action) ───────────────────
