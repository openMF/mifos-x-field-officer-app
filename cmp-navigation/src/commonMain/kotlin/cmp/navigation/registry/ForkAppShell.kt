/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navOptions
import cmp.navigation.authenticated.AuthenticatedGraphRoute
import cmp.navigation.rootnav.RootNavScreen
import kpt.feature.settings.serverconfig.navigateToServerConfig
import kpt.feature.settings.serverconfig.serverConfigDestination
import kpt.feature.signin.SignInRoute
import kpt.feature.signin.signInDestination

/**
 * ForkAppShell — the FORK-OWNED seam for the app root, so a fork never edits the template's
 * `ComposeApp.kt`.
 *
 * Measured across 21 forks (2026-11-02): 9 edited `ComposeApp.kt`, always for one of two reasons —
 * forcing the theme mode (a dark-only game) or replacing the root content (an app root beside the nav
 * graph, extra root callbacks). Both live here. Brand COLOURS do not: they are in
 * `core/designsystem/.../theme/ForkTheme.kt`, which `KptTheme` applies.
 *
 * Ownership: `owner: fork` — a template sync full-copies `ComposeApp.kt` and never touches this file.
 */
object ForkAppShell {
    /** Forces dark (`true`) or light (`false`) regardless of the user's setting; `null` follows it. */
    val darkThemeOverride: Boolean? = null

    /** Forces dynamic (wallpaper) colour on or off; `null` follows the user's setting. */
    val dynamicColorOverride: Boolean? = null

    /**
     * Wraps the whole app, outside the theme: the place for an SDK's composition-time init (ads, a
     * consent UI) and app-wide `CompositionLocalProvider`s. Forks did this by editing `SharedApp.kt`
     * (5 of 21); the default renders the app unchanged.
     */
    val wrap: @Composable (content: @Composable () -> Unit) -> Unit = { content -> content() }

    /**
     * The app's root content inside the theme. The default is the template's navigation root; a fork
     * that needs its own root (or extra root callbacks) renders it here and calls
     * `onSplashScreenRemoved` once its first frame is ready.
     */
    val root: @Composable (modifier: Modifier, onSplashScreenRemoved: () -> Unit) -> Unit =
        { modifier, onSplashScreenRemoved ->
            RootNavScreen(modifier = modifier, onSplashScreenRemoved = onSplashScreenRemoved)
        }

    /**
     * The signed-out part of the root graph: sign-in and server config, installed beside splash and
     * onboarding and OUTSIDE the signed-in graph. `feature/signin` does not annotate `signInDestination`
     * with `@FeatureDestination` because that registers onto the AUTHENTICATED graph — the one place the
     * gate in front of it must not be. The feature owns the screen; this seam owns its placement.
     */
    val rootDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
        // First-run onboarding is installed by RootNavScreen (the template's root-graph install); installing it here
        // too would register one route twice on the same NavHost.
        signInDestination(
            onLoggedIn = {
                navController.navigate(
                    route = AuthenticatedGraphRoute,
                    navOptions = navOptions {
                        // Drop login off the back stack: pressing back from the authenticated graph must
                        // not return a signed-in user to the sign-in screen.
                        popUpTo(SignInRoute) { inclusive = true }
                        launchSingleTop = true
                    },
                )
            },
            onUpdateServerConfig = navController::navigateToServerConfig,
        )

        // Server config sits on the ROOT graph, not inside the authenticated one, because a first-run
        // user has no Fineract URL and so cannot sign in until they have set one — the screen has to be
        // reachable while signed OUT. Registered here exactly once: `navigate()` resolves across the
        // nested boundary, so Settings reaches the same destination once authenticated, whereas a second
        // registration inside `authenticatedGraph` would be a duplicate destination on one NavHost.
        serverConfigDestination(onBackClick = navController::popBackStack)
    }
}
