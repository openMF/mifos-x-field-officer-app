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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.outlined.Business
import androidx.compose.ui.graphics.vector.ImageVector
import kpt.core.ui.navigation.FeatureTab
import kpt.core.ui.navigation.NavigationItem
import kpt.core.ui.navigation.toObjectNavigationRoute
import kpt.feature.center.generated.resources.Res
import kpt.feature.center.generated.resources.feature_center_tab
import org.jetbrains.compose.resources.StringResource

/**
 * The Centers bottom-nav tab.
 *
 * Declared here via `@FeatureTab` rather than in `cmp-navigation`'s `AuthenticatedNavBarTabItem`,
 * the same way `ClientTab` is — a feature contributes its own tab without the fork editing a module
 * it does not own. `:cmp-navigation:generateFeatureTabs` collects it into `GeneratedFeatureTabs`,
 * which `ForkTabRegistry.extraTabs` exposes.
 *
 * This REPLACES the placeholder `AuthenticatedNavBarTabItem.CentersTab`, which pointed at the
 * pending host. That declaration is removed in the same change — leaving both would put two Centers
 * tabs in the bar, one of them dead.
 */
@FeatureTab
object CenterTab : NavigationItem {
    override val selectedIcon: ImageVector get() = Icons.Filled.Business
    override val icon: ImageVector get() = Icons.Outlined.Business
    override val labelRes: StringResource get() = Res.string.feature_center_tab
    override val contentDescriptionRes: StringResource get() = Res.string.feature_center_tab

    /** The GRAPH, not the list screen — so the tab owns its own back stack. */
    override val graphRoute: String get() = CenterDestination.toObjectNavigationRoute()
    override val startDestinationRoute: String get() = CenterListRoute.toObjectNavigationRoute()
    override val testTag: String get() = "CentersTab"
}
