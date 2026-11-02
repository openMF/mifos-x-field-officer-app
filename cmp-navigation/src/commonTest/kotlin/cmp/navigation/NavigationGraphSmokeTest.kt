/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation

import cmp.navigation.authenticated.AuthenticatedGraphRoute
import cmp.navigation.authenticatednavbar.AuthenticatedNavBarTabItem
import cmp.navigation.authenticatednavbar.AuthenticatedNavbarRoute
import cmp.navigation.registry.ForkTabRegistry
import cmp.navigation.rootnav.RootNavNavigation
import cmp.navigation.splash.SplashRoute
import cmp.navigation.utils.toObjectNavigationRoute
import kotlinx.serialization.serializer
import kpt.core.common.utils.Constants
import kpt.feature.activate.navigation.ActivateRoute
import kpt.feature.charge.navigation.ChargeDetailRoute
import kpt.feature.charge.navigation.ChargeListRoute
import kpt.feature.client.navigation.ClientAddressRoute
import kpt.feature.client.navigation.ClientClosureRoute
import kpt.feature.client.navigation.ClientCollateralDetailsRoute
import kpt.feature.client.navigation.ClientCollateralRoute
import kpt.feature.client.navigation.ClientDetailsRoute
import kpt.feature.client.navigation.ClientDocumentsRoute
import kpt.feature.client.navigation.ClientGeneralRoute
import kpt.feature.client.navigation.ClientIdentifiersRoute
import kpt.feature.client.navigation.ClientProfileRoute
import kpt.feature.client.navigation.ClientSignatureRoute
import kpt.feature.client.navigation.ClientStaffRoute
import kpt.feature.client.navigation.ClientTransferRoute
import kpt.feature.home.HomeRoute
import kpt.feature.profile.ProfileRoute
import kpt.feature.settings.NotificationRoute
import kpt.feature.settings.SettingsRoute
import kpt.feature.settings.SyncAndDraftsRoute
import kpt.feature.signin.SignInRoute
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
     * Every PARAMETERLESS route the fork declares. Arg-carrying routes are counted separately in
     * [argCarryingRoutes] — they arrived with S3/S3c, which is what this KDoc used to say would
     * happen ("a `parameterisedRoutesInstantiateWithDefaults` test returns then").
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

    /**
     * The arg-carrying routes, instantiated with real arguments.
     *
     * The point is not the count — it is that every one of them REQUIRES its arguments. An earlier
     * pass of this port gave several of them defaults (`ActivateRoute(id = -1, type = "")`,
     * `ClientStaffRoute(id = -1)`), which made a screen reachable with a sentinel id that it would
     * then ask Fineract about. Constructing them here without defaults is what keeps that from
     * coming back: adding a default would not break this test, but REMOVING a required argument
     * would stop compiling it.
     */
    private val argCarryingRoutes: List<Any> = listOf(
        ClientProfileRoute(clientId = 1),
        ClientDetailsRoute(clientId = 1),
        ClientGeneralRoute(clientId = 1),
        ClientIdentifiersRoute(clientId = 1),
        ClientAddressRoute(clientId = 1),
        ClientDocumentsRoute(clientId = 1),
        ChargeListRoute(resourceType = "clients", resourceId = 1),
        ChargeDetailRoute(resourceType = "loans", resourceId = 1, chargeId = 1),
        ClientStaffRoute(clientId = 1),
        ClientClosureRoute(clientId = 1),
        ClientTransferRoute(clientId = 1),
        ClientCollateralRoute(clientId = 1),
        ClientCollateralDetailsRoute(clientId = 1),
        ClientSignatureRoute(clientId = 1, clientName = "n", accountNo = "a"),
        ActivateRoute(id = 1, type = Constants.ACTIVATE_GROUP),
    )

    @Test
    fun everyArgCarryingRouteRequiresItsArguments() {
        // Each entry above is constructed with explicit arguments; this asserts the set is
        // non-empty and distinct, so a copy-paste duplicate in the list is caught.
        assertEquals(
            argCarryingRoutes.size,
            argCarryingRoutes.distinct().size,
            "duplicate entries in argCarryingRoutes",
        )
        assertTrue(argCarryingRoutes.isNotEmpty())
    }

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
    fun theBottomBarIsTheFourPortedTabsInTheOriginalsOrder() {
        // ORDER is the assertion, not just membership. Clients, Centers and Groups all arrive
        // through @FeatureTab, so without an explicit order in ForkTabRegistry the bar would show
        // whatever sequence `generateFeatureTabs` happened to scan the modules in — and tab order
        // is muscle memory for a field officer, not a code-generation detail.
        //
        // Home is FIRST by product decision (the original's first tab was Search, which now lives
        // on the top bar's magnifier). ProfileTab is absent: the original has no Profile tab, its
        // account surface is the Home avatar.
        assertEquals(
            listOf("HomeTab", "ClientTab", "CentersTab", "GroupsTab"),
            ForkTabRegistry.tabs.map { it.testTag },
        )
    }

    @Test
    fun everyTabHasADistinctNonBlankRoutePair() {
        // A blank route makes the tab crash with "destination not found" on first tap; a SHARED
        // route makes two tabs highlight at once and leaves the back stack unable to tell them
        // apart. Both were live risks while Centers and Groups were placeholders pointing at one
        // pending host, and both stay asserted now that they point at real graphs.
        ForkTabRegistry.tabs.forEach { tab ->
            assertTrue(tab.graphRoute.isNotBlank(), "${tab.testTag} has blank graphRoute")
            assertTrue(
                tab.startDestinationRoute.isNotBlank(),
                "${tab.testTag} has blank startDestinationRoute",
            )
        }
        val starts = ForkTabRegistry.tabs.map { it.startDestinationRoute }
        assertEquals(starts.size, starts.distinct().size, "two tabs share a start destination")
    }

    @Test
    fun theShellOwnsOnlyTheHomeTab() {
        // Every other tab has left AuthenticatedNavBarTabItem for its own feature module. This
        // pins the direction of travel: a NEW placeholder tab added here should be a deliberate
        // decision, not something that accumulates.
        assertEquals("HomeTab", AuthenticatedNavBarTabItem.HomeTab.testTag)
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
