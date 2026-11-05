/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.onboarding

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithStayTransitions
import kpt.feature.onboarding.ui.OnboardingScreen

/** First-run onboarding — three intro pages and the privacy consent page. */
@Serializable
data object OnboardingRoute

/** Opens onboarding. */
fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) = navigate(OnboardingRoute, navOptions)

/**
 * Onboarding is NOT annotated `@FeatureDestination`, for the same reason sign-in is not: that annotation registers
 * onto the AUTHENTICATED graph, and onboarding runs before sign-in. The app shell installs it on the root graph
 * (`cmp-navigation` `authNavGraph`) and RootNav routes `RootNavState.ShowOnboarding` to it.
 *
 * There is no "finished" callback: finishing marks first run done, RootNavViewModel resolves to `Auth`, and the
 * shell navigates to sign-in on its own.
 */
fun NavGraphBuilder.onboardingDestination(onOpenServerConfig: () -> Unit) {
    composableWithStayTransitions<OnboardingRoute> {
        OnboardingScreen(onOpenServerConfig = onOpenServerConfig)
    }
}
