/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.feature.settings.signout.SignOutAction
import kpt.feature.settings.signout.SignOutDialog
import kpt.feature.settings.signout.SignOutViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * The STATEFUL settings body — the fork's replacement for the template's
 * `kpt.feature.settings.demo.SettingsDemoBody`, which `customizer --clean` strips.
 *
 * ## Why this file exists
 *
 * [SettingsScreenContent] is `internal` and stateless by design: it is the Roborazzi `@Preview`
 * render target, so it takes plain callbacks and holds nothing. Something has to own the dialog
 * state and hand it those callbacks. In the template that owner lived in the `demo` package — and
 * the strip took it, leaving `BackboneRegistry.settingsBody` as the empty `{ }` the customizer
 * documents the fork should fill.
 *
 * Nobody filled it. The result was a drawer entry declaring `isMigrated = true`
 * (`HomeDestinationsScreen.SettingsScreen`) that navigated to a destination rendering NOTHING:
 * `settingsDestination(settingsBody = …)` forwarded an empty lambda, so the route composed to a
 * blank screen — no crash, no log line, nothing to notice except by opening it. Found on device
 * 2026-10-22; it survived a full desktop test suite because every settings test drives
 * [SettingsScreenContent] directly and never goes through the seam.
 *
 * Being `internal`, [SettingsScreenContent] also cannot be called from `cmp-navigation` — which is
 * why the fix is a public composable HERE rather than a longer lambda in `BackboneRegistry`.
 *
 * ## What it wires
 *
 * Theme and language are local dialog state (`rememberSaveable`, so they survive rotation);
 * [SettingsDialog] and [LanguageDialog] both default to a `koinViewModel()`, so no ViewModel is
 * threaded through the seam.
 *
 * Two callbacks are deliberately left `null`:
 *  - `onRateAppClick` — the "Rate this app" row is ABSENT, not disabled, when no review flow can be
 *    reached. This fork has no configured store listing, and a greyed row would advertise a
 *    capability the build does not have (the content composable's own documented contract).
 *  - `onFooterLongClick` — with no dev-menu entries the version label is a plain footer rather than
 *    a long-press target that opens an empty menu.
 */
@Composable
fun SettingsBody(
    onBackClick: () -> Unit,
    onSyncAndDraftsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onServerConfigClick: (() -> Unit)? = null,
    onAboutClick: (() -> Unit)? = null,
    onOfflineSyncClick: (() -> Unit)? = null,
    signOutViewModel: SignOutViewModel = koinViewModel(),
) {
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    SettingsScreenContent(
        onBackClick = onBackClick,
        onThemeCardClick = { showThemeDialog = true },
        onLanguageCardClick = { showLanguageDialog = true },
        onSyncAndDraftsClick = onSyncAndDraftsClick,
        onServerConfigClick = onServerConfigClick,
        onAboutClick = onAboutClick,
        onOfflineSyncClick = onOfflineSyncClick,
        onSignOutClick = { signOutViewModel.trySendAction(SignOutAction.Requested) },
        modifier = modifier,
    )

    if (showThemeDialog) {
        SettingsDialog(onDismiss = { showThemeDialog = false })
    }
    if (showLanguageDialog) {
        LanguageDialog(onDismiss = { showLanguageDialog = false })
    }
    val signOut by signOutViewModel.stateFlow.collectAsStateWithLifecycle()
    if (signOut.confirming) {
        SignOutDialog(state = signOut, onAction = { signOutViewModel.trySendAction(it) })
    }
}
