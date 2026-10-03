/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * Reference @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` via `verifyRoborazziDesktop` — no emulator, no Robolectric. Every fork's
 * `kmp-screen-gen` emits these per screen; this is the template's demonstrator.
 *
 * `SettingsScreen` itself is not previewed: it resolves its ViewModel through Koin. Literals below
 * are PREVIEW FIXTURE DATA — never reachable from the running app, so G-SOURCE-I18N excludes
 * `*Preview.kt` from its scan rather than asking for them to be translated.
 */

@Preview
@Composable
internal fun SettingsScreenContentPreview() {
    KptTheme {
        SettingsScreenContent(
            onBackClick = {},
            onThemeCardClick = {},
            onLanguageCardClick = {},
            onSyncAndDraftsClick = {},
        )
    }
}

@Preview
@Composable
internal fun SettingsScreenContentWithRateAppPreview() {
    // The same screen with review REACHABLE. `onRateAppClick` is null in the preview above, which is
    // the honest default (a target with no native review flow and no configured store listing hides
    // the row) — but it means the shipped row renders in no golden at all unless a second preview
    // passes it. Both states are real, so both are captured.
    KptTheme {
        SettingsScreenContent(
            onBackClick = {},
            onThemeCardClick = {},
            onLanguageCardClick = {},
            onSyncAndDraftsClick = {},
            onRateAppClick = {},
        )
    }
}

@Preview
@Composable
internal fun SettingsRowsPreview() {
    // The three shipped rows side by side. Each passes its own accent colour into the shared
    // `SettingsRowCard`, so rendering them together is what shows the accents actually differ
    // rather than all falling back to one theme colour.
    KptTheme {
        Column {
            LanguageCard(onClick = {})
            SyncAndDraftsCard(onClick = {})
            RateAppCard(onClick = {})
        }
    }
}

@Preview
@Composable
internal fun ThemeCardPreview() {
    KptTheme {
        ThemeCard(onClick = {})
    }
}

@Preview
@Composable
internal fun VersionLabelPreview() {
    // The footer renders the fork's app display name from BuildKonfig, not a string resource, so a
    // rebrand shows up here. Both variants matter: the long-press affordance (the hidden dev-menu
    // entry point) is only wired when `onLongClick` is non-null.
    KptTheme {
        Column {
            VersionLabel(onLongClick = null)
            VersionLabel(onLongClick = {})
        }
    }
}

@Preview
@Composable
internal fun SettingsRowCardLongTitlePreview() {
    // The row primitive on its own, with a title long enough to wrap — the case the two shipped
    // rows (both short) never exercise, and where icon/title/chevron alignment breaks first.
    KptTheme {
        SettingsRowCard(
            icon = AppIcons.Language,
            title = "Change the application display language and region format",
            contentDescription = "Opens the language picker",
            accentColor = MaterialTheme.colorScheme.tertiary,
            onClick = {},
        )
    }
}
