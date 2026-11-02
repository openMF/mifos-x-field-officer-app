/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.center.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.composableWithStayTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.activate.navigation.navigateToActivateCenter
import kpt.feature.center.create.CreateCenterScreen
import kpt.feature.center.detail.CenterDetailScreen
import kpt.feature.center.list.CenterListScreen
import kpt.feature.groups.navigation.navigateToGroupsDetail
import kpt.feature.savings.navigation.navigateToSavingsApplication

/** The tab's graph. Mirrors `ClientDestination` — the tab points at the graph, not at a screen. */
@Serializable
data object CenterDestination

@Serializable
data object CenterListRoute

/** Center detail. `centerId` is a type-safe route argument, not a SavedStateHandle default. */
@Serializable
data class CenterDetailRoute(val centerId: Int)

fun NavController.navigateToCenterList(navOptions: NavOptions? = null) =
    navigate(CenterListRoute, navOptions)

fun NavController.navigateToCenters(navOptions: NavOptions? = null) =
    navigate(CenterDestination, navOptions)

fun NavController.navigateToCenterDetail(centerId: Int, navOptions: NavOptions? = null) =
    navigate(CenterDetailRoute(centerId), navOptions)

@Serializable
data object CreateCenterRoute

fun NavController.navigateToCreateCenter(navOptions: NavOptions? = null) = navigate(CreateCenterRoute, navOptions)

/**
 * The center list — a TOP-LEVEL destination on the authenticated graph, collected into
 * `GeneratedFeatureDestinations` by `:cmp-navigation:generateFeatureDestinations`.
 *
 * This file MUST live in a `navigation` package: the generator calls `scan(roots, "navigation")`, so
 * an annotated destination anywhere else is silently skipped — a clean build and a screen that does
 * not exist in the app.
 *
 * Create center (2026-10-24) is the list's FAB → [CreateCenterRoute], on `CenterCommandRepository.createCenter`.
 */
@FeatureDestination
fun NavGraphBuilder.centerListDestination(navController: NavController) {
    composableWithStayTransitions<CenterListRoute> {
        CenterListScreen(
            onCenterClick = navController::navigateToCenterDetail,
            onCreateCenter = navController::navigateToCreateCenter,
        )
    }

    composableWithPushTransitions<CreateCenterRoute> {
        CreateCenterScreen(
            onNavigateBack = navController::popBackStackSafely,
            // Back to the list, which re-reads: the command already wrote the new center into it.
            onCreated = navController::popBackStackSafely,
        )
    }

    // Nested drill-down, deliberately NOT annotated: only TOP-LEVEL entries carry
    // @FeatureDestination, and annotating this would register detail as its own root entry.
    composableWithPushTransitions<CenterDetailRoute> {
        CenterDetailScreen(
            onBackClick = navController::popBackStackSafely,
            // Group detail is owned by feature/groups; live now that it has landed.
            onGroupClick = { groupId -> navController.navigateToGroupsDetail(groupId) },
            onActivate = navController::navigateToActivateCenter,
            // The original's "Add savings account". A centre is a Fineract GROUP (level 1), so the
            // application goes as a group one. The original passed the centre's id as a CLIENT id
            // (`navigateToAddSavingsAccount(0, centerId, false)`), creating it against a client.
            onAddSavingsAccount = { centerId ->
                navController.navigateToSavingsApplication(ownerId = centerId, isGroup = true)
            },
        )
    }
}

/**
 * The center graph for the bottom-nav tab's INNER NavHost.
 *
 * Same two-NavHost split as `clientTabGraph`: the list is the tab's start destination so the bottom
 * bar persists and the tab keeps its own back stack, while detail pushes on the OUTER controller and
 * covers the bar.
 *
 * Not annotated — `@FeatureDestination` collects entries for the outer graph, and registering the
 * same routes twice is a duplicate-destination crash at NavHost build.
 */
fun NavGraphBuilder.centerTabGraph(outerNav: NavController) {
    navigation<CenterDestination>(startDestination = CenterListRoute) {
        composableWithStayTransitions<CenterListRoute> {
            CenterListScreen(
                onCenterClick = outerNav::navigateToCenterDetail,
                // The TAB copy of the list — the one officers actually use. Wiring only the other
                // registration would ship a FAB on one entry path and not on the one people take.
                onCreateCenter = outerNav::navigateToCreateCenter,
            )
        }
    }
}
