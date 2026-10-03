/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.serverconfig

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions

@Serializable
data object ServerConfigRoute

fun NavController.navigateToServerConfig(navOptions: NavOptions? = null) =
    navigate(ServerConfigRoute, navOptions)

/**
 * Server-config destination, installed on the **ROOT** graph — deliberately NOT annotated
 * `@FeatureDestination`.
 *
 * That annotation registers onto the authenticated graph, and this screen must be reachable while
 * SIGNED OUT: a first-run user has no Fineract URL, so sign-in cannot succeed until they have set
 * one, and the login screen links straight here. Registering it inside the authenticated subgraph
 * would make it unreachable at exactly the moment it is needed.
 *
 * It is registered ONCE, on the root graph, rather than twice. A route registered on both the root
 * and the nested authenticated graph is a duplicate destination on the same `NavHost`; one
 * root-level registration is reachable from the login screen and from Settings alike, because
 * `navigate()` resolves across the nested boundary.
 *
 * This resolves the open question in `feature/settings/MIGRATION.md` §6.
 */
fun NavGraphBuilder.serverConfigDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<ServerConfigRoute> {
        ServerConfigScreen(onBackClick = onBackClick)
    }
}
