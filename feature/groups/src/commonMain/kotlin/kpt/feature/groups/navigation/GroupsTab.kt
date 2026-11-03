/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.ui.graphics.vector.ImageVector
import kpt.core.ui.navigation.FeatureTab
import kpt.core.ui.navigation.NavigationItem
import kpt.core.ui.navigation.toObjectNavigationRoute
import kpt.feature.groups.generated.resources.Res
import kpt.feature.groups.generated.resources.feature_groups_tab
import org.jetbrains.compose.resources.StringResource

/**
 * The Groups bottom-nav tab.
 *
 * Declared here via `@FeatureTab` rather than in `cmp-navigation`, the same way `ClientTab` and
 * `CenterTab` are. `:cmp-navigation:generateFeatureTabs` collects it into `GeneratedFeatureTabs`,
 * which `ForkTabRegistry.extraTabs` exposes.
 *
 * Uses `Icons.Groups` rather than the `People` the placeholder borrowed — `People` is the Clients
 * tab's icon, and two tabs sharing a glyph is the kind of thing a monochrome bar makes genuinely
 * confusing. With this tab landing, `AuthenticatedNavBarTabItem` holds only `HomeTab`.
 */
@FeatureTab
object GroupsTab : NavigationItem {
    override val selectedIcon: ImageVector get() = Icons.Filled.Groups
    override val icon: ImageVector get() = Icons.Outlined.Groups
    override val labelRes: StringResource get() = Res.string.feature_groups_tab
    override val contentDescriptionRes: StringResource get() = Res.string.feature_groups_tab

    /** The GRAPH, not the list screen — so the tab owns its own back stack. */
    override val graphRoute: String get() = GroupsDestination.toObjectNavigationRoute()
    override val startDestinationRoute: String get() = GroupsListRoute.toObjectNavigationRoute()
    override val testTag: String get() = "GroupsTab"
}
