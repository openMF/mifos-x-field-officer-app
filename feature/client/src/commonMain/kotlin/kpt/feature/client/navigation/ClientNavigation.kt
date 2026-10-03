/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithStayTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.client.charges.ClientChargesScreen
import kpt.feature.client.detail.ClientDetailScreen
import kpt.feature.client.list.ClientListScreen

/** The tab's graph. Mirrors `HomeDestination` — the tab points at the graph, not at a screen. */
@Serializable
data object ClientDestination

@Serializable
data object ClientListRoute

/** Client detail. `clientId` is a type-safe route argument, not a SavedStateHandle default. */
@Serializable
data class ClientDetailRoute(val clientId: Int)

/**
 * Charges levied on a client.
 *
 * Typed `clientId`, NOT the original's `resourceId: Int` + `resourceType: String`. Every caller of
 * that route passed "clients", so the type parameter was a hole rather than flexibility; loan and
 * savings charges get their own routes when those slices land.
 */
@Serializable
data class ClientChargesRoute(val clientId: Int)

fun NavController.navigateToClientList(navOptions: NavOptions? = null) =
    navigate(ClientListRoute, navOptions)

fun NavController.navigateToClients(navOptions: NavOptions? = null) =
    navigate(ClientDestination, navOptions)

fun NavController.navigateToClientDetail(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDetailRoute(clientId), navOptions)

fun NavController.navigateToClientCharges(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientChargesRoute(clientId), navOptions)

/**
 * The client list — a TOP-LEVEL destination on the authenticated graph, so it is annotated and
 * collected into `GeneratedFeatureDestinations` by `:cmp-navigation:generateFeatureDestinations`.
 *
 * This file MUST live in a `navigation` package: the generator calls `scan(roots, "navigation")`, so
 * an annotated destination anywhere else is silently skipped and the screen simply does not exist in
 * the app — with a clean build. (A wrong SIGNATURE throws loudly; a wrong PACKAGE does not. Found the
 * hard way in S3a, when `ClientModule` appeared in the Koin aggregate and the destination did not.)
 *
 * The original registered ~38 client destinations through one `clientNavGraph`. Those arrive with
 * their screens across S3a–S3d; nested drill-downs stay nested and are NOT annotated — only
 * top-level entries are (spec §4).
 *
 * Create-client does not exist yet, so it is passed as `null` rather than as an empty lambda — the
 * screen then omits the FAB entirely instead of drawing one that does nothing. S3c supplies it.
 */
@FeatureDestination
fun NavGraphBuilder.clientListDestination(navController: NavController) {
    composableWithStayTransitions<ClientListRoute> {
        ClientListScreen(
            // Detail exists as of S3b, so rows are clickable. Create-client is still null — S3c —
            // and null (not an empty lambda) means the FAB is not drawn rather than drawn inert.
            onClientClick = navController::navigateToClientDetail,
            onCreateClient = null,
        )
    }

    // Nested drill-down: registered inside the feature graph and deliberately NOT annotated. Only
    // TOP-LEVEL entries carry @FeatureDestination (spec §4) — annotating this one would register the
    // detail screen as its own root entry.
    composableWithStayTransitions<ClientDetailRoute> {
        ClientDetailScreen(
            onBackClick = navController::popBackStackSafely,
            // Charges is live as of this slice. Loans, savings, notes and documents arrive in
            // S3d/S4/S5 — null until then, so the affordance is absent rather than inert.
            onChargesClick = navController::navigateToClientCharges,
            onLoanClick = null,
            onSavingsClick = null,
            onNotesClick = null,
            onDocumentsClick = null,
        )
    }

    // Charges list. Nested drill-down — NOT annotated: only TOP-LEVEL entries carry
    // @FeatureDestination (spec §4), and annotating this would register it as its own root entry.
    composableWithStayTransitions<ClientChargesRoute> {
        ClientChargesScreen(onBackClick = navController::popBackStackSafely)
    }
}

/**
 * The client graph for the bottom-nav tab's INNER NavHost.
 *
 * Two NavHosts are in play and they want different things. [clientListDestination] registers the list
 * on the OUTER authenticated graph, where another feature can push it full-screen. This graph is what
 * the Clients TAB renders: the list is the tab's start destination, so the bottom bar stays visible and
 * the tab keeps its own back stack, while the detail drill-down pushes on the OUTER controller and
 * covers the bar — the standard affordance, and the shape `TabRegistry.extraInlineTabDestinations`
 * documents.
 *
 * Not annotated: `@FeatureDestination` collects entries for the outer graph, and registering the same
 * routes there twice is a duplicate-destination crash at NavHost build.
 */
fun NavGraphBuilder.clientTabGraph(outerNav: NavController) {
    navigation<ClientDestination>(startDestination = ClientListRoute) {
        composableWithStayTransitions<ClientListRoute> {
            ClientListScreen(
                onClientClick = outerNav::navigateToClientDetail,
                onCreateClient = null,
            )
        }
    }
}
