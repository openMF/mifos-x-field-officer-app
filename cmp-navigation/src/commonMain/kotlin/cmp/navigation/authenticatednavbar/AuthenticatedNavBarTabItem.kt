/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.authenticatednavbar

import androidx.compose.ui.graphics.vector.ImageVector
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.home
import cmp.navigation.utils.toObjectNavigationRoute
import kpt.core.designsystem.icon.ForkIcons
import kpt.core.ui.navigation.NavigationItem
import kpt.feature.home.HomeDestination
import kpt.feature.home.HomeRoute
import org.jetbrains.compose.resources.StringResource

/**
 * The authenticated bottom-nav tabs — ported from
 * `6b66e8a43:cmp-navigation/.../authenticated/AuthenticatedNavBarTabItem.kt`.
 *
 * ## Four tabs, declared centrally
 *
 * Home · Clients · Centers · Groups. The original declares all four HERE rather than letting each
 * feature contribute its own, and that is what lets the bar be complete before its features are:
 * the tab bar is part of the SHELL, not of the features it opens.
 *
 * Clients, Centers and Groups are each a `@FeatureTab` declared by their own module; `ForkTabRegistry`
 * places them. Every placeholder that once lived here has left as its feature landed, which is what
 * this file is for — it holds only tabs the SHELL itself owns, and [HomeTab] is now the only one.
 *
 * ## What changed from the template
 *
 * The template shipped `HomeTab` + `ProfileTab`. [HomeTab] stays first; the original's SearchTab is
 * replaced by the top bar's search icon (a deliberate product decision — see [HomeTab]).
 *
 * `ProfileTab` is REMOVED from the bar. The original has no Profile tab; the account surface is the
 * Home avatar. Keeping it would have made the fork's bar five tabs wide and wrong.
 */
sealed class AuthenticatedNavBarTabItem : NavigationItem {

    /**
     * Home — the first tab, hosted by `feature/home`.
     *
     * DELIBERATE DEVIATION from `6b66e8a43`, which made Search the first tab. Search is reached from
     * the top bar's search icon instead, which frees the first tab for a real home dashboard and
     * keeps the magnifier where a user now expects it. Recorded here rather than in a migration note
     * because the next person comparing the two bars will otherwise read it as a porting miss.
     */
    data object HomeTab : AuthenticatedNavBarTabItem() {
        override val selectedIcon: ImageVector get() = ForkIcons.HomeBoarder
        override val icon: ImageVector get() = ForkIcons.Home
        override val labelRes: StringResource get() = Res.string.home
        override val contentDescriptionRes: StringResource get() = Res.string.home
        override val graphRoute: String get() = HomeDestination.toObjectNavigationRoute()
        override val startDestinationRoute: String get() = HomeRoute.toObjectNavigationRoute()
        override val testTag: String get() = "HomeTab"
    }
}
