/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.authenticatednavbar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import cmp.navigation.registry.BackboneRegistry
import cmp.navigation.registry.DashboardTopBar
import cmp.navigation.registry.navigateToCreate
import cmp.navigation.registry.navigateToSearch
import cmp.navigation.registry.navigateToSearchResult
import cmp.navigation.registry.searchDestination
import cmp.navigation.registry.TabRegistry
import cmp.navigation.ui.KptRootScaffold
import cmp.navigation.ui.ScaffoldNavigationData
import cmp.navigation.ui.logDestinationChanged
import cmp.navigation.ui.rememberKptNavController
import io.github.mobilebytelabs.kmptoolkit.firebase.compose.rememberAnalyticsHelper
import kotlinx.collections.immutable.toImmutableList
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.designsystem.theme.motion
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.base.ui.util.RootTransitionProviders
import kpt.feature.home.HomeDestination
import kpt.feature.home.homeGraph
import kpt.feature.home.navigateToHome
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AuthenticatedNavbarNavigationScreen(
    navigateToSettingsScreen: () -> Unit,
    homeBody: @Composable () -> Unit,
    // The OUTER authenticated-graph controller. Used to (a) push a FULL-SCREEN tab (inlineTab = false,
    // e.g. an immersive focus modal) above the scaffold, and (b) hand INLINE fork tabs a controller for
    // their drill-downs via TabRegistry.extraInlineTabDestinations. Threaded from authenticatedGraph so
    // this merge-owned shell stays free of feature imports.
    outerNavController: NavController,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberKptNavController(
        name = "AuthenticatedNavbarScreen",
    ),
    viewModel: AuthenticatedNavbarNavigationViewModel = koinViewModel(),
) {
    val analyticsHelper = rememberAnalyticsHelper()

    EventsEffect(eventFlow = viewModel.eventFlow) { event ->
        navController.apply {
            when (event) {
                AuthenticatedNavBarEvent.NavigateToHomeScreen -> {
                    analyticsHelper.logDestinationChanged(event.tab.startDestinationRoute)
                    navigateToTabOrRoot(tabToNavigateTo = event.tab) {
                        navigateToHome(navOptions = it)
                    }
                }
            }
        }
    }

    AuthenticatedNavbarNavigationScreenContent(
        navController = navController,
        outerNavController = outerNavController,
        modifier = modifier,
        navigateToSettingsScreen = navigateToSettingsScreen,
        homeBody = homeBody,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
@Suppress("LongMethod")
internal fun AuthenticatedNavbarNavigationScreenContent(
    navController: NavHostController,
    outerNavController: NavController,
    navigateToSettingsScreen: () -> Unit,
    homeBody: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (AuthenticatedNavBarAction) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // Tabs from the fork-owned TabRegistry seam (backbone Home/Profile + fork extraTabs) — the
    // navbar renders the list generically instead of a hardcoded item list (S6 heal, T7).
    val navigationItems = TabRegistry.tabs.toImmutableList()
    // The top bar's connection pill reads the app's own NetworkMonitor (Stitch shell: "● Online").
    val online by koinInject<NetworkMonitor>().isOnline.collectAsState(initial = true)

    // The dashboard chrome belongs to TAB ROOTS, and to exactly the destinations the bottom bar
    // also serves — one condition for both bars, so they can never disagree. A drill-down (client
    // detail, search) draws its own bar with a back arrow, and would
    // otherwise stack two.
    val isTabRoot = navigationItems.any { navBackStackEntry.isCurrentRoute(it.startDestinationRoute) }

    KptRootScaffold(
        contentWindowInsets = WindowInsets(0.dp),
        navigationData = ScaffoldNavigationData(
            navigationItems = navigationItems,
            selectedNavigationItem = navigationItems.find {
                navBackStackEntry.isCurrentRoute(route = it.graphRoute)
            },
            onNavigationClick = { navigationItem ->
                when (navigationItem) {
                    is AuthenticatedNavBarTabItem.HomeTab -> {
                        onAction(AuthenticatedNavBarAction.HomeTabClick)
                    }

                    // Fork extra tabs. INLINE tabs (inlineTab = true — the default) swap content inside
                    // THIS inner NavHost so the bottom bar persists and the tab keeps its own back stack
                    // (their top screen is registered by TabRegistry.extraInlineTabDestinations below).
                    // FULL-SCREEN tabs (inlineTab = false, e.g. an immersive focus modal that declares
                    // bottom_navigation_visible: false) push their route on the OUTER graph, hiding the bar.
                    else -> if (navigationItem.inlineTab) {
                        navController.navigateToInlineTab(navigationItem.startDestinationRoute)
                    } else {
                        outerNavController.navigate(
                            navigationItem.startDestinationRoute,
                            navOptions { launchSingleTop = true },
                        )
                    }
                }
            },
            shouldShowNavigation = isTabRoot,
            // Named tabs. The app being ported labels all four (Home · Clients · Centers · Groups)
            // and a field officer picks between them by name, not by a monochrome glyph.
            showLabels = true,
        ),
        topBar = {
            if (!isTabRoot) return@KptRootScaffold
            DashboardTopBar(
                online = online,
                onSearchIconClick = { navController.navigateToSearch() },
                // Notifications is a migrated backbone destination (feature/settings ships
                // notificationDestination), but it hangs off the OUTER graph, so it is reached the
                // same way Settings is.
                onNotificationIconClick = { BackboneRegistry.navigateToNotification(outerNavController) },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier,
    ) {
        // Because this Scaffold has a bottom navigation bar, the NavHost will:
        // - consume the vertical navigation bar insets.
        // - consume the IME insets.
        // Snapshot motion tokens once so the non-Composable enterTransition lambdas capture
        // theme-resolved values rather than the hardcoded fallbacks.
        val motion = MaterialTheme.motion
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
            // Sibling navigation (bottom-nav tab switch) uses M3 fade-through pattern.
            enterTransition = RootTransitionProviders.Kpt.Enter.fadeThrough(motion),
            exitTransition = RootTransitionProviders.Kpt.Exit.fadeThrough(motion),
            popEnterTransition = RootTransitionProviders.Kpt.Enter.fadeThrough(motion),
            popExitTransition = RootTransitionProviders.Kpt.Exit.fadeThrough(motion),
        ) {
            // TOP LEVEL DESTINATIONS
            // topBar = { } — the shell draws ONE bar above every tab (search +
            // notifications), so HomeScreen must not draw a second. That slot was added to
            // feature/home for exactly this case; see its note.
            homeGraph(
                onSettingsClick = navigateToSettingsScreen,
                homeBody = homeBody,
                topBar = { },
            )

            // Search, reached from the top bar's magnifier rather than from a tab.
            searchDestination(
                onBackClick = { navController.popBackStackSafely() },
                // Both on the OUTER controller: search sits in this INNER (bottom-bar) host, but every
                // destination it opens — the five entity details and the three create forms — is a
                // feature destination registered on the outer graph (`FeatureRegistry`). On the
                // inner controller `navigate()` threw "Destination … cannot be found" and crashed
                // the app (device, 2026-10-25: the FAB's Create centre).
                onResultClick = outerNavController::navigateToSearchResult,
                onCreate = outerNavController::navigateToCreate,
            )

            // Fork INLINE extra-tab top screens (TabRegistry.extraInlineTabDestinations). Registered in
            // THIS inner NavHost so tapping the tab swaps content inside the scaffold (bottom bar stays)
            // — the fork wires each screen's drill-downs on outerNavController (full-screen). Template
            // default = no-op. Keeps the merge-owned shell feature-import-free (the seam lives in the fork).
            TabRegistry.extraInlineTabDestinations.invoke(this, navController, outerNavController)
        }
    }
}

/**
 * Switch to an INLINE extra tab (its top screen registered in the inner NavHost via
 * [TabRegistry.extraInlineTabDestinations]). Mirrors [navigateToTabOrRoot]'s bottom-nav semantics for a
 * plain route string: no-op if already on the tab; otherwise pop up to the graph start saving state,
 * single-top, and restore the tab's saved back stack — so each tab keeps its own state across switches.
 */
private fun NavHostController.navigateToInlineTab(route: String) {
    if (currentDestination?.hierarchy?.any { it.route == route } == true) return
    navigate(
        route,
        navOptions {
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        },
    )
}

private fun NavController.navigateToTabOrRoot(
    tabToNavigateTo: AuthenticatedNavBarTabItem,
    navigate: (NavOptions) -> Unit,
) {
    if (tabToNavigateTo.startDestinationRoute == currentDestination?.route) {
        return
    } else if (currentDestination?.parent?.route == tabToNavigateTo.graphRoute) {
        popBackStack(route = tabToNavigateTo.startDestinationRoute, inclusive = false)
    } else {
        navigate(
            navOptions {
                popUpTo(graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            },
        )
    }
}

private fun NavBackStackEntry?.isCurrentRoute(route: String): Boolean = this
    ?.destination
    ?.hierarchy
    ?.any { it.route == route } == true
