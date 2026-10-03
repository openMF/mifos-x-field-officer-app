/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.signin

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithStayTransitions

@Serializable
data object SignInRoute

fun NavController.navigateToSignIn(navOptions: NavOptions? = null) = navigate(SignInRoute, navOptions)

/**
 * Login is NOT annotated `@FeatureDestination`.
 *
 * That annotation registers a destination on the AUTHENTICATED graph, which is exactly where login
 * must not be — it is the gate in front of it. The app shell owns where sign-in sits in the
 * navigation graph, so this stays a plain builder the shell installs deliberately.
 */
fun NavGraphBuilder.signInDestination(
    onLoggedIn: () -> Unit,
    onUpdateServerConfig: () -> Unit,
) {
    composableWithStayTransitions<SignInRoute> {
        SignInScreen(
            onLoggedIn = onLoggedIn,
            onUpdateServerConfig = onUpdateServerConfig,
        )
    }
}
