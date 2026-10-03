/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation

import cmp.navigation.authenticated.AuthenticatedGraphRoute
import cmp.navigation.authenticatednavbar.AuthenticatedNavBarTabItem
import cmp.navigation.authenticatednavbar.AuthenticatedNavbarRoute
import cmp.navigation.rootnav.RootNavNavigation
import cmp.navigation.splash.SplashRoute
import cmp.navigation.utils.toObjectNavigationRoute
import kotlinx.serialization.serializer
import kpt.feature.signin.SignInRoute
import kpt.feature.home.HomeRoute
import kpt.feature.profile.ProfileRoute
import kpt.feature.settings.NotificationRoute
import kpt.feature.settings.SettingsRoute
import kpt.feature.settings.SyncAndDraftsRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Smoke test for the app's navigation graph.
 *
 * REWRITTEN for the fork's own surface (S1, Phase 10). Every route this file previously named —
 * `BillsGraphRoute`, `LoansGraphRoute`, `RatesGraphRoute`, `CalculatorsGraphRoute`,
 * `MacroGraphRoute`, `EmiCalculatorRoute` and the rest — belonged to the kmp-project-template DEMO
 * features that `remove-demo.sh` stripped during the fork standup. The file therefore failed to
 * compile with **95 unresolved references**, and had done so since demo removal: `commonTest` only
 * breaks the build when someone runs it, and nobody had. It was found by running
 * `:cmp-navigation:desktopTest` for the first time while wiring the entry graph.
 *
 * What it guards now:
 *
 *  1. Every route is `@Serializable` AND its producing module applies the kotlinx-serialization
 *     plugin — enforced at COMPILE time by `serializer<T>()` resolving at all.
 *  2. No two routes collapse onto the same navigation route string. This one can genuinely fail at
 *     runtime: the route string is the serializer's `serialName`, so two `data object`s with the
 *     same qualified name in different modules, or a hand-set `@SerialName`, silently alias and the
 *     NavHost resolves the wrong destination.
 *  3. Backbone bottom-nav tabs keep stable testTags and non-blank routes.
 *  4. A route-count canary, so a module silently dropping out of the graph is noticed.
 *
 * Adding a route → add it to [allRoutes] and bump [EXPECTED_ROUTE_COUNT].
 */
class NavigationGraphSmokeTest {

    /**
     * Every route the fork declares today. All are parameterless `data object`s — the original app's
     * arg-carrying routes (`ClientDetailRoute`, `LoanDetailRoute`, `ActivateRoute(id, type)` …)
     * arrive with their slices, and a `parameterisedRoutesInstantiateWithDefaults` test returns then.
     * There is deliberately no such test now: there is nothing to instantiate, and a test asserting
     * that an empty list is empty is worse than no test.
     */
    private val allRoutes: List<Any> = listOf(
        // shell
        RootNavNavigation,
        SplashRoute,
        AuthenticatedGraphRoute,
        AuthenticatedNavbarRoute,
        // features
        SignInRoute,
        HomeRoute,
        ProfileRoute,
        SettingsRoute,
        NotificationRoute,
        SyncAndDraftsRoute,
    )

    // vacuous-ok: `serializer<T>()` returns a non-null KSerializer by signature, so these
    // assertions cannot fail at RUNTIME. They are kept because the guarantee is at COMPILE time —
    // `serializer<T>()` stops resolving the moment a route loses `@Serializable` or its module drops
    // the serialization plugin, which is exactly the bug class, and is caught by this file compiling
    // at all. The asserts remain so the intent is legible in the test report rather than implicit.
    @Test
    fun everyRouteIsKotlinxSerializable() {
        assertNotNull(serializer<RootNavNavigation>())
        assertNotNull(serializer<SplashRoute>())
        assertNotNull(serializer<AuthenticatedGraphRoute>())
        assertNotNull(serializer<AuthenticatedNavbarRoute>())
        assertNotNull(serializer<SignInRoute>())
        assertNotNull(serializer<HomeRoute>())
        assertNotNull(serializer<ProfileRoute>())
        assertNotNull(serializer<SettingsRoute>())
        assertNotNull(serializer<NotificationRoute>())
        assertNotNull(serializer<SyncAndDraftsRoute>())
    }

    /**
     * Two routes must never resolve to the same navigation route string.
     *
     * Unlike the serializer checks above, this CAN fail at runtime. `toObjectNavigationRoute()`
     * returns the serializer's `serialName`, which defaults to the fully-qualified class name but is
     * overridable with `@SerialName`. An alias means `navigate(A)` can land on B, and the NavHost
     * reports nothing — it resolved a real destination, just the wrong one.
     */
    @Test
    fun noTwoRoutesShareANavigationRouteString() {
        val byRouteString = allRoutes.groupBy { it.toObjectNavigationRoute() }
        val collisions = byRouteString.filterValues { it.size > 1 }
        assertEquals(
            emptyMap(),
            collisions,
            "routes aliased onto one route string — navigate(A) would resolve to B",
        )
    }

    /** Every route string is non-blank; a blank one makes the destination unreachable. */
    @Test
    fun everyRouteStringIsNonBlank() {
        val blank = allRoutes.filter { it.toObjectNavigationRoute().isBlank() }
        assertEquals(emptyList(), blank, "a blank route string is an unreachable destination")
    }

    @Test
    fun bottomNavTabsHaveStableTestTagsAndNonBlankRoutes() {
        val tabs = listOf(
            AuthenticatedNavBarTabItem.HomeTab,
            AuthenticatedNavBarTabItem.ProfileTab,
        )

        // Locked test tags — instrumentation tests + analytics depend on these.
        // Renaming requires updating both the test suite and any dashboards.
        assertEquals("HomeTab", AuthenticatedNavBarTabItem.HomeTab.testTag)
        assertEquals("ProfileTab", AuthenticatedNavBarTabItem.ProfileTab.testTag)

        // Every tab declares non-blank graphRoute + startDestinationRoute so the
        // bottom bar doesn't crash with "destination not found" on first tap.
        tabs.forEach { tab ->
            assertTrue(
                tab.graphRoute.isNotBlank(),
                "${tab::class.simpleName} has blank graphRoute",
            )
            assertTrue(
                tab.startDestinationRoute.isNotBlank(),
                "${tab::class.simpleName} has blank startDestinationRoute",
            )
            assertTrue(
                tab.testTag.isNotBlank(),
                "${tab::class.simpleName} has blank testTag",
            )
        }
    }

    @Test
    fun routeCountMatchesExpectedSurface() {
        // Canary: the fork ships these N routes as of S1. A module silently dropping out of the
        // graph shows up here. Intentional change → update the count AND [allRoutes].
        //
        // This grows per slice: S2 adds the Client/Centers/Groups tab routes, S3 the client
        // vertical's arg-carrying routes, and so on to the original's full surface.
        assertEquals(
            EXPECTED_ROUTE_COUNT,
            allRoutes.size,
            "expected $EXPECTED_ROUTE_COUNT routes, found ${allRoutes.size}. " +
                "If intentional, update both EXPECTED_ROUTE_COUNT and allRoutes.",
        )
    }

    private companion object {
        /** Route count at S1 (Phase 10). Bumped by each slice that adds destinations. */
        const val EXPECTED_ROUTE_COUNT = 10
    }
}
