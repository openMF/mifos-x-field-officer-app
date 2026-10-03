/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.home.search.SearchBody
import kpt.feature.profile.identity.ProfileBody
import kpt.feature.settings.navigateToSettings
import kpt.feature.settings.notificationDestination
import kpt.feature.settings.settingsDestination
import kpt.feature.settings.syncAndDraftsDestination

/**
 * BackboneRegistry — the FORK-OWNED white-label seam for the app **backbone** (the home-tab body and,
 * as forks grow, backbone module overrides).
 *
 * It is the home-body twin of [FeatureRegistry] (which owns feature routes + DI). The template shell —
 * `feature/home`'s `HomeScreen` (top bar + settings) and the `cmp-navigation` navbar graph — carries ZERO
 * demo imports and simply renders whatever body this seam provides. A fork swaps its dashboard by editing
 * THIS ONE file, never the template shell. Ownership: `owner: fork` in customization-surface.yaml, so a
 * template sync full-copies the shell while this file (and its body) survives.
 *
 * S5 heal (epic pure-white-label-store5-network, T7): the 12 hardcoded `navigateToX` demo lambdas used to
 * thread through FIVE template layers (AuthenticatedNavigation → navbarGraph → navbarScreen → homeGraph →
 * HomeScreen) just to reach the fork-owned [HomeDashboard]. They now live here, in one fork-owned place —
 * the shell only forwards an opaque `homeBody`.
 */
object BackboneRegistry {
    /**
     * The home tab's body. `HomeScreen` renders this inside its framework-owned Scaffold. The template
     * default is the demo Money-Toolkit dashboard wired to its feature destinations; `customizer --clean`
     * strips the fenced block below, leaving `{ }` — an empty backbone body for the fork to fill.
     */
    val homeBody: @Composable (NavController) -> Unit = { _ ->
        // Fineract resource search — the original app's de-facto dashboard, ported in S2 from
        // 6b66e8a43:feature/search. This seam was EMPTY, which is why the Home tab rendered as a
        // blank screen on device.
        //
        // No NavController is used yet: result rows are not clickable until S3 lands client detail,
        // the first destination a result can open (RULE-IMPL-DEAD-CLICKABLE-001).
        SearchBody()
    }

    /**
     * The settings tab's INNER content. The template-owned `SettingsScreen` shell (via
     * `settingsDestination`) forwards this opaque seam; the fork owns this body. It wires the
     * back / sync-and-drafts / dev-menu callbacks into the demo [SettingsDemoBody], so the
     * merge-owned shell carries no `kpt.feature.settings.*` content wiring. `customizer --clean`
     * strips the fenced block, leaving `{ }` for the fork to fill (mirrors `homeBody`). WS01 / AC7.
     */
    val settingsBody: @Composable (NavController) -> Unit = { navController ->
    }

    /**
     * The profile tab's INNER content. `ProfileScreen` (the template shell) forwards this opaque
     * seam so the shell carries no `kpt.feature.profile.*` content imports; the fork owns this body.
     * `customizer --clean` strips the fenced block, leaving `{ }` for the fork to fill (mirrors
     * `homeBody`). WS01 / AC7.
     */
    val profileBody: @Composable (NavController) -> Unit = { _ ->
        // Signed-in officer identity + sign-out (S2). Another seam that was EMPTY.
        //
        // Sign-out needs no navigation callback: clearing the session flips
        // `UserData.isAuthenticated` to false, RootNavViewModel emits `RootNavState.Auth`, and the
        // root effect routes to sign-in. Navigating here as well would race the state machine.
        ProfileBody(onSignedOut = {})
    }

    /**
     * Route the authenticated navbar to the Settings backbone. Kept here so the
     * merge-owned `AuthenticatedNavigation.kt` shell carries no `kpt.feature.*`
     * imports (mirrors the `homeBody` seam pattern).
     */
    val navigateToSettings: (NavController) -> Unit = { it.navigateToSettings() }

    /**
     * Backbone (framework) nav destinations — settings, notification, sync-and-drafts.
     * The showcase dev entries hang off the generic `devMenuEntries` slot from
     * [ShowcaseRegistry.devSettingsEntries]; a `--clean` fork drops to `emptyList()`
     * and the Settings dev menu hides.
     */
    val backboneDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
        notificationDestination(onBackClick = { navController.popBackStackSafely() })
        syncAndDraftsDestination(onBackClick = { navController.popBackStackSafely() })
        // Settings inner content flows through the fork-owned `settingsBody` seam (WS01 / AC7); the
        // template `settingsDestination` shell forwards this opaque body, carrying no demo wiring.
        settingsDestination(settingsBody = { BackboneRegistry.settingsBody(navController) })
    }
}
