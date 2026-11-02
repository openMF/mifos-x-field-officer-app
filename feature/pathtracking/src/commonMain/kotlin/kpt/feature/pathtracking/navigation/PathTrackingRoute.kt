/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.pathtracking.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.feature.pathtracking.ui.PathTrackingScreen

/** The drawer's Path Tracker — the original's `PathTrackerRoute`. */
@Serializable
data object PathTrackingRoute

@FeatureDestination
fun NavGraphBuilder.pathTrackingDestination(navController: NavController) {
    composableWithPushTransitions<PathTrackingRoute> {
        PathTrackingScreen(onNavigateBack = { navController.popBackStack() })
    }
}

fun NavController.navigateToPathTracking() = navigate(PathTrackingRoute)
