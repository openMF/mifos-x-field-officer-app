/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.about.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.feature.about.ui.AboutScreen

@Serializable
data object AboutRoute

/**
 * Top-level: Settings → About opens it (moved out of the drawer 2026-10-27).
 * It is reached through `BackboneRegistry.settingsBody`, so the merge-owned shell never imports
 * this module.
 */
@FeatureDestination
fun NavGraphBuilder.aboutDestination(navController: NavController) {
    composableWithPushTransitions<AboutRoute> {
        AboutScreen(onNavigateBack = { navController.popBackStack() })
    }
}

fun NavController.navigateToAbout() = navigate(AboutRoute)
