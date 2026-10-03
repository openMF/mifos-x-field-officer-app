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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.People
import androidx.compose.ui.graphics.vector.ImageVector
import kpt.core.ui.navigation.FeatureTab
import kpt.core.ui.navigation.NavigationItem
import kpt.core.ui.navigation.toObjectNavigationRoute
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_tab
import org.jetbrains.compose.resources.StringResource

/**
 * The Clients bottom-nav tab.
 *
 * Declared HERE rather than in `cmp-navigation`'s `AuthenticatedNavBarTabItem`: `@FeatureTab` is the
 * template's seam for exactly this, so a feature contributes its tab without the fork editing a module
 * the feature does not own. `:cmp-navigation:generateFeatureTabs` collects it into
 * `GeneratedFeatureTabs`, which `TabRegistry.extraTabs` exposes.
 *
 * This is what makes `feature/client` REACHABLE. Before it, the module built, its Koin module was in
 * the generated aggregate and `clientListDestination` was in `GeneratedFeatureDestinations` — and no
 * user could get to any of it, because nothing in the UI navigated there. A registered destination
 * with no entry point is the dead-end `RULE-IMPL-NAV-CONN` exists to catch.
 */
@FeatureTab
object ClientTab : NavigationItem {
    override val selectedIcon: ImageVector get() = Icons.Filled.People
    override val icon: ImageVector get() = Icons.Outlined.People
    override val labelRes: StringResource get() = Res.string.feature_client_tab
    override val contentDescriptionRes: StringResource get() = Res.string.feature_client_tab

    /**
     * The GRAPH, not the list screen — tapping the tab must land on the graph so the tab owns its own
     * back stack, the way `HomeTab` points at `HomeDestination`.
     */
    override val graphRoute: String get() = ClientDestination.toObjectNavigationRoute()
    override val startDestinationRoute: String get() = ClientListRoute.toObjectNavigationRoute()
    override val testTag: String get() = "ClientTab"
}
