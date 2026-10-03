/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navOptions
import cmp.navigation.authenticated.AuthenticatedGraphRoute
import kpt.feature.signin.SignInRoute
import kpt.feature.signin.signInDestination
import kpt.feature.settings.serverconfig.navigateToServerConfig
import kpt.feature.settings.serverconfig.serverConfigDestination

/**
 * Installs the unauthenticated entry destinations on the ROOT graph.
 *
 * The shell owns where sign-in sits, which is why `feature/signin` deliberately does NOT annotate
 * `signInDestination` with `@FeatureDestination`: that annotation registers onto the AUTHENTICATED
 * graph, the one place the gate in front of it must not be. The feature owns the screen; this file
 * owns its placement.
 *
 * Before this existed, `signInDestination` had zero callers anywhere in the tree — `feature/signin`
 * compiled, was included in `settings.local.gradle.kts`, and was absent from the navigation graph,
 * so a logged-out user could not reach a login screen at all.
 */
internal fun NavGraphBuilder.authNavGraph(navController: NavController) {
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
