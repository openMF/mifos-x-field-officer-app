/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.activate.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.common.utils.Constants
import kpt.feature.activate.ui.ActivateScreen

/**
 * Activate a client, centre or group.
 *
 * `type` is the entity kind — one of `Constants.ACTIVATE_CLIENT` / `_CENTER` / `_GROUP`, resolved
 * to a closed enum by the ViewModel. Neither field has a default: the original's route defaulted
 * `id` to `-1` and `type` to `""`, so a missing argument produced a screen that would try to
 * activate entity `-1` of no known kind.
 */
@Serializable
data class ActivateRoute(val id: Int, val type: String)

fun NavController.navigateToActivateClient(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(clientId, Constants.ACTIVATE_CLIENT), navOptions)

fun NavController.navigateToActivateCenter(centerId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(centerId, Constants.ACTIVATE_CENTER), navOptions)

fun NavController.navigateToActivateGroup(groupId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(groupId, Constants.ACTIVATE_GROUP), navOptions)

/**
 * Annotated `@FeatureDestination`, so the generator registers it ONCE on the authenticated graph.
 *
 * That is deliberate and is the exception spec §4 allows for: activate is reached from TWO parent
 * graphs (centre and group — client's only caller is the dead `clientDetailRoute`). Registering it
 * inside each parent the way a single-parent drill-down is registered would declare `ActivateRoute`
 * twice in one NavHost, which is a duplicate-destination crash at graph build — the same failure
 * `ClientNavigation.clientTabGraph` warns about.
 *
 * The signature is the generator's contract: `NavGraphBuilder.(NavController) -> Unit`. A different
 * shape is silently skipped — the screen simply would not exist in the app, with a clean build.
 */
@FeatureDestination
fun NavGraphBuilder.activateDestination(navController: NavController) {
    composableWithPushTransitions<ActivateRoute> {
        ActivateScreen(onBackPressed = navController::popBackStackSafely)
    }
}
