/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.ui.AppInfo
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.icon.ForkIcons
import kpt.core.designsystem.theme.spacing
import kpt.core.ui.scaffold.KptScaffold
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_about_row
import kpt.feature.settings.generated.resources.feature_settings_about_row_description
import kpt.feature.settings.generated.resources.feature_settings_change_language_placeholder_text
import kpt.feature.settings.generated.resources.feature_settings_change_language_text
import kpt.feature.settings.generated.resources.feature_settings_change_theme_placeholder_text
import kpt.feature.settings.generated.resources.feature_settings_change_theme_text
import kpt.feature.settings.generated.resources.feature_settings_offline_sync_row
import kpt.feature.settings.generated.resources.feature_settings_offline_sync_row_description
import kpt.feature.settings.generated.resources.feature_settings_rate_app_row
import kpt.feature.settings.generated.resources.feature_settings_rate_app_row_description
import kpt.feature.settings.generated.resources.feature_settings_server_config_row
import kpt.feature.settings.generated.resources.feature_settings_server_config_row_description
import kpt.feature.settings.generated.resources.feature_settings_sign_out
import kpt.feature.settings.generated.resources.feature_settings_sign_out_row_description
import kpt.feature.settings.generated.resources.feature_settings_sync_drafts_row
import kpt.feature.settings.generated.resources.feature_settings_sync_drafts_row_description
import kpt.feature.settings.generated.resources.feature_settings_title
import kpt.feature.settings.signout.SignOutTestTags
import org.jetbrains.compose.resources.stringResource

/**
 * The settings screen CONTENT — the template-owned Settings [KptScaffold] with the theme / language /
 * sync-and-drafts cards + version footer. This presentational composable is the reference `@Preview`
 * render target (see `SettingsScreenPreview.kt` → the desktop Roborazzi golden), so it takes plain
 * callbacks and no state.
 *
 * The stateful shell/seam split (WS01 base-feature seam, epic AC7): the dialog state + dev-menu now live
 * in the fork-owned [kpt.feature.settings.demo.SettingsDemoBody] (rendered via `cmp-navigation`'s
 * `ForkBackboneRegistry.settingsBody`), which wires those callbacks to THIS content. A template sync
 * full-copies this content shell while the fork's `settingsBody` seam survives.
 */
@Composable
internal fun SettingsScreenContent(
    onBackClick: () -> Unit,
    onThemeCardClick: () -> Unit,
    onLanguageCardClick: () -> Unit,
    onSyncAndDraftsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFooterLongClick: (() -> Unit)? = null,
    onRateAppClick: (() -> Unit)? = null,
    onServerConfigClick: (() -> Unit)? = null,
    onSignOutClick: (() -> Unit)? = null,
    onAboutClick: (() -> Unit)? = null,
    onOfflineSyncClick: (() -> Unit)? = null,
) {
    val sp = MaterialTheme.spacing
    KptScaffold(
        title = stringResource(Res.string.feature_settings_title),
        onNavigationIconClick = onBackClick,
        // TestTags.Settings.SCREEN existed as a constant but was applied by NO composable, so
        // SettingsScreenUiTest asserted a tag that was never set and could not have passed even
        // against the template's `SettingsDemoBody`. Applying it here is the constant's evident
        // intent and makes that test verify something real.
        modifier = modifier.testTag(TestTags.Settings.SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Scrolls: with About, Server config, Rate and Sign out the rows outgrow a short phone.
                .verticalScroll(rememberScrollState())
                .padding(horizontal = sp.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            ThemeCard(onClick = onThemeCardClick)
            LanguageCard(onClick = onLanguageCardClick)
            SyncAndDraftsCard(onClick = onSyncAndDraftsClick)
            // Keep centres / groups / clients on the device for the field — a separate feature from
            // Sync & Drafts (which uploads what was done offline). Null on a build without it.
            onOfflineSyncClick?.let {
                SettingsRowCard(
                    icon = AppIcons.CloudDownload,
                    title = stringResource(Res.string.feature_settings_offline_sync_row),
                    contentDescription = stringResource(Res.string.feature_settings_offline_sync_row_description),
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onClick = it,
                    modifier = Modifier.testTag(TestTags.Settings.OFFLINE_SYNC_ROW),
                )
            }
            // The original's ENDPOINT (Instance URL) and SERVER_CONFIG rows edited the same server two
            // ways; the Server config screen's instance picker now covers both. Null = no server choice.
            onServerConfigClick?.let {
                SettingsRowCard(
                    icon = AppIcons.Update,
                    title = stringResource(Res.string.feature_settings_server_config_row),
                    contentDescription = stringResource(Res.string.feature_settings_server_config_row_description),
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    onClick = it,
                )
            }
            // Absent, not disabled, when review is unreachable. `canRequestReview` is false on a
            // target with neither a native review flow nor a configured store listing, and a
            // greyed-out row there would advertise a capability the build genuinely does not have.
            onRateAppClick?.let { RateAppCard(onClick = it) }
            // The app's About page (version, help, community, licence) — moved here from the drawer.
            // Null on a build without feature/about.
            onAboutClick?.let {
                SettingsRowCard(
                    icon = AppIcons.Info,
                    title = stringResource(Res.string.feature_settings_about_row),
                    contentDescription = stringResource(Res.string.feature_settings_about_row_description),
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = it,
                    modifier = Modifier.testTag(TestTags.Settings.ABOUT_ROW),
                )
            }
            // Last: the one action that ends the session. Null on a build with no sign-in.
            onSignOutClick?.let {
                SettingsRowCard(
                    icon = AppIcons.Logout,
                    title = stringResource(Res.string.feature_settings_sign_out),
                    contentDescription = stringResource(Res.string.feature_settings_sign_out_row_description),
                    accentColor = MaterialTheme.colorScheme.error,
                    onClick = it,
                    modifier = Modifier.testTag(SignOutTestTags.ROW),
                )
            }
            Spacer(modifier = Modifier.fillMaxWidth().padding(sp.sm))
            VersionLabel(onLongClick = onFooterLongClick)
        }
    }
}

@Composable
internal fun SyncAndDraftsCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsRowCard(
        icon = ForkIcons.Payment,
        title = stringResource(Res.string.feature_settings_sync_drafts_row),
        contentDescription = stringResource(Res.string.feature_settings_sync_drafts_row_description),
        accentColor = MaterialTheme.colorScheme.secondary,
        onClick = onClick,
        modifier = modifier,
    )
}

/**
 * "Rate this app" row. Rendered only when the caller resolved that a review can actually be
 * requested — see [kpt.feature.settings.demo.SettingsDemoBody].
 */
@Composable
internal fun RateAppCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsRowCard(
        icon = ForkIcons.Star,
        title = stringResource(Res.string.feature_settings_rate_app_row),
        contentDescription = stringResource(Res.string.feature_settings_rate_app_row_description),
        accentColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
        modifier = modifier,
    )
}

/**
 * Static version footer label. When the caller supplies a non-empty dev-menu list (via
 * [kpt.feature.settings.demo.SettingsDemoBody]), long-pressing opens the Dev tools menu. When the list
 * is empty (release builds / neutralized forks) the long-press handler is `null`, so the label behaves
 * as a plain text footer.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun VersionLabel(onLongClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    // App-name footer rendered from the common AppInfo.appDisplayName accessor (BuildKonfig →
    // gradle/fork.properties#app.display.name), not a hardcoded string resource — so a fork rebrands
    // in app-profile in one place (S9/T10).
    val rowModifier = if (onLongClick != null) {
        modifier
            .fillMaxWidth()
            .combinedClickable(
                // intentional-noop: this row acts on long-press only; combinedClickable requires an onClick.
                onClick = {},
                onLongClick = onLongClick,
            )
    } else {
        modifier.fillMaxWidth()
    }
    Text(
        text = AppInfo.appDisplayName,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = rowModifier,
    )
}

@Composable
internal fun ThemeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsRowCard(
        icon = ForkIcons.Sun,
        title = stringResource(Res.string.feature_settings_change_theme_text),
        contentDescription = stringResource(Res.string.feature_settings_change_theme_placeholder_text),
        accentColor = MaterialTheme.colorScheme.tertiary,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
internal fun LanguageCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsRowCard(
        icon = ForkIcons.Language,
        title = stringResource(Res.string.feature_settings_change_language_text),
        contentDescription = stringResource(Res.string.feature_settings_change_language_placeholder_text),
        accentColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsRowCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    contentDescription: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    AppCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        accentColor = accentColor,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sp.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor)
            Text(text = title, modifier = Modifier.weight(1f))
            IconButton(onClick = onClick) {
                Icon(imageVector = ForkIcons.ArrowRight, contentDescription = contentDescription)
            }
        }
    }
}
