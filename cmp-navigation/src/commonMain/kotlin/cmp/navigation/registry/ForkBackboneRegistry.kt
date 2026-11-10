/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.model.recent.RecentKind
import kpt.feature.about.navigation.navigateToAbout
import kpt.feature.center.navigation.navigateToCenterDetail
import kpt.feature.center.navigation.navigateToCreateCenter
import kpt.feature.checkerinbox.navigation.navigateToCheckerInbox
import kpt.feature.client.navigation.navigateToClientList
import kpt.feature.client.navigation.navigateToClientProfile
import kpt.feature.client.navigation.navigateToCreateNewClient
import kpt.feature.collectionsheet.navigation.navigateToIndividualCollectionSheet
import kpt.feature.groups.navigation.navigateToCreateGroup
import kpt.feature.groups.navigation.navigateToGroupsDetail
import kpt.feature.home.dashboard.HomeDashboardScreen
import kpt.feature.home.dashboard.HomeNavigation
import kpt.feature.home.dashboard.QuickAction
import kpt.feature.home.dashboard.loanQueueDestination
import kpt.feature.home.dashboard.navigateToLoanQueue
import kpt.feature.loan.navigation.navigateToLoanProfile
import kpt.feature.notification.navigation.navigateToNotifications
import kpt.feature.offlinesync.navigation.navigateToOfflineSync
import kpt.feature.pathtracking.navigation.navigateToPathTracking
import kpt.feature.profile.identity.ProfileBody
import kpt.feature.profile.navigateToProfile
import kpt.feature.profile.profileDestination
import kpt.feature.report.navigation.navigateToReports
import kpt.feature.settings.SettingsBody
import kpt.feature.settings.navigateToSettings
import kpt.feature.settings.navigateToSyncAndDrafts
import kpt.feature.settings.notificationDestination
import kpt.feature.settings.serverconfig.navigateToServerConfig
import kpt.feature.settings.settingsDestination
import kpt.feature.settings.signout.SignOutAction
import kpt.feature.settings.signout.SignOutDialog
import kpt.feature.settings.signout.SignOutViewModel
import kpt.feature.settings.syncAndDraftsDestination
import org.koin.compose.viewmodel.koinViewModel

/**
 * ForkBackboneRegistry — the FORK-OWNED white-label seam for the app **backbone** (the home-tab body and,
 * as forks grow, backbone module overrides).
 *
 * It is the home-body twin of [ForkFeatureRegistry] (which owns feature routes + DI). The template shell —
 * `feature/home`'s `HomeScreen` (top bar + settings) and the `cmp-navigation` navbar graph — carries ZERO
 * demo imports and simply renders whatever body this seam provides. A fork swaps its dashboard by editing
 * THIS ONE file, never the template shell. Ownership: `owner: fork` in customization-surface.yaml, so a
 * template sync full-copies the shell while this file (and its body) survives.
 *
 * S5 heal (epic pure-white-label-store5-network, T7): the 12 hardcoded `navigateToX` demo lambdas used to
 * thread through FIVE template layers (AuthenticatedNavigation → navbarGraph → navbarScreen → homeGraph →
 * HomeScreen) just to reach the fork-owned [HomeDashboard]. They now live here, in one fork-owned place —
 * the shell only forwards an opaque `homeBody`.
 */
object ForkBackboneRegistry {
    /**
     * The home tab's body. `HomeScreen` renders this inside its framework-owned Scaffold. The template
     * default is the demo Money-Toolkit dashboard wired to its feature destinations; `customizer --clean`
     * strips the fenced block below, leaving `{ }` — an empty backbone body for the fork to fill.
     */
    val homeBody: @Composable (NavController) -> Unit = { navController ->
        // The field officer's day (feature/home dashboard). Search moved to its own destination behind
        // the top bar's magnifier; every tap here leaves through one HomeNavigation mapping.
        HomeDashboardScreen(onNavigate = { target -> navController.navigateHome(target) })
    }

    /**
     * The settings tab's INNER content. The template-owned `SettingsScreen` shell (via
     * `settingsDestination`) forwards this opaque seam; the fork owns this body. It wires the
     * back / sync-and-drafts / dev-menu callbacks into the demo [SettingsDemoBody], so the
     * merge-owned shell carries no `kpt.feature.settings.*` content wiring. `customizer --clean`
     * strips the fenced block, leaving `{ }` for the fork to fill (mirrors `homeBody`). WS01 / AC7.
     */
    val settingsBody: @Composable (NavController) -> Unit = { navController ->
        // Settings content (S7). This seam was EMPTY — the same class as `profileBody` below, and
        // with a worse symptom: `HomeDestinationsScreen.SettingsScreen` declares `isMigrated = true`,
        // so the drawer routed to a destination that rendered NOTHING. No crash, no log, a blank
        // screen. Found on device 2026-10-22; the desktop suite missed it because every settings
        // test drives `SettingsScreenContent` directly and never passes through this seam.
        //
        // `SettingsBody` is the fork's stand-in for the stripped `demo.SettingsDemoBody`: it owns the
        // theme/language dialog state that the stateless, `internal` content composable cannot.
        SettingsBody(
            onBackClick = { navController.popBackStackSafely() },
            onSyncAndDraftsClick = { navController.navigateToSyncAndDrafts() },
            onServerConfigClick = { navController.navigateToServerConfig() },
            onAboutClick = { navController.navigateToAbout() },
            onOfflineSyncClick = { navController.navigateToOfflineSync() },
        )
    }

    /**
     * The profile tab's INNER content. `ProfileScreen` (the template shell) forwards this opaque
     * seam so the shell carries no `kpt.feature.profile.*` content imports; the fork owns this body.
     * `customizer --clean` strips the fenced block, leaving `{ }` for the fork to fill (mirrors
     * `homeBody`). WS01 / AC7.
     */
    val profileBody: @Composable (NavController) -> Unit = { navController ->
        // The officer's account (screens/profile). Sign-out is the Settings flow (its dialog and
        // ViewModel); it is supplied here because a feature never depends on another.
        val signOutViewModel: SignOutViewModel = koinViewModel()
        val signOut by signOutViewModel.stateFlow.collectAsStateWithLifecycle()
        ProfileBody(
            onBack = { navController.popBackStackSafely() },
            onServerSettings = { navController.navigateToServerConfig() },
            onSignOut = { signOutViewModel.trySendAction(SignOutAction.Requested) },
            signOutDialog = {
                if (signOut.confirming) SignOutDialog(state = signOut, onAction = signOutViewModel::trySendAction)
            },
        )
    }

    /**
     * Route the authenticated navbar to the Settings backbone. Kept here so the
     * merge-owned `AuthenticatedNavigation.kt` shell carries no `kpt.feature.*`
     * imports (mirrors the `homeBody` seam pattern).
     */
    val navigateToSettings: (NavController) -> Unit = { it.navigateToSettings() }

    /**
     * Route the dashboard top bar's bell to the Notification backbone. Same seam shape as
     * [navigateToSettings] and for the same reason: it keeps the `kpt.feature.settings` import out
     * of the merge-owned shell, which `customization-surface.yaml` has as `owner: merge`.
     */
    // The bell opens Fineract's notifications (feature/notification), not the template's static
    // placeholder in feature/settings — that destination stays registered but nothing routes to it.
    val navigateToNotification: (NavController) -> Unit = { it.navigateToNotifications() }

    /**
     * Backbone (framework) nav destinations — settings, notification, sync-and-drafts.
     * The showcase dev entries hang off the generic `devMenuEntries` slot from
     * [ForkShowcaseRegistry.devSettingsEntries]; a `--clean` fork drops to `emptyList()`
     * and the Settings dev menu hides.
     */
    val backboneDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
        notificationDestination(onBackClick = { navController.popBackStackSafely() })
        syncAndDraftsDestination(
            onBackClick = { navController.popBackStackSafely() },
            // Names each create draft and reopens its form (fork routing; template screen unchanged).
            draftExtras = { rememberCreateDraftExtras(navController) },
        )
        // Settings inner content flows through the fork-owned `settingsBody` seam (WS01 / AC7); the
        // template `settingsDestination` shell forwards this opaque body, carrying no demo wiring.
        settingsDestination(settingsBody = { ForkBackboneRegistry.settingsBody(navController) })
        // Profile opens from the Home avatar (the drawer header did, until the drawer was removed
        // 2026-10-27), and Home taps leave through this outer controller.
        profileDestination(profileBody = { ForkBackboneRegistry.profileBody(navController) })
        // Search on the OUTER graph too. The top bar's magnifier opens it inside the inner (bottom-bar)
        // host, but `homeBody` only receives this outer controller, and navigating to a route the
        // graph lacks crashes ("Destination with route SearchRoute cannot be found" — device,
        // 2026-10-27, Home's Search quick action). Same screen and callbacks, opened full-screen.
        searchDestination(
            onBackClick = { navController.popBackStackSafely() },
            onResultClick = navController::navigateToSearchResult,
            onCreate = navController::navigateToCreate,
        )
        loanQueueDestination(
            onBack = { navController.popBackStackSafely() },
            onLoanClick = { loanId -> navController.navigateToLoanProfile(loanId.toInt()) },
        )
    }
}

/** Where each Home dashboard tap goes. Reschedules are approved from the checker inbox's own tab. */
private fun NavController.navigateHome(target: HomeNavigation) = when (target) {
    HomeNavigation.CollectionSheet -> navigateToIndividualCollectionSheet()
    HomeNavigation.CheckerInbox, HomeNavigation.Reschedules -> navigateToCheckerInbox()
    is HomeNavigation.LoanQueue -> navigateToLoanQueue(target.status)
    // ponytail: the client list has no status filter yet, so this opens the full list.
    HomeNavigation.PendingClients -> navigateToClientList()
    HomeNavigation.Drafts -> navigateToSyncAndDrafts()
    HomeNavigation.Settings -> navigateToSettings()
    HomeNavigation.Profile -> navigateToProfile()
    is HomeNavigation.Center -> navigateToCenterDetail(target.centerId)
    is HomeNavigation.Recent -> navigateToRecent(target)
    is HomeNavigation.Quick -> navigateToQuickAction(target.action)
}

private fun NavController.navigateToRecent(target: HomeNavigation.Recent) = when (target.kind) {
    RecentKind.CLIENT -> navigateToClientProfile(target.id.toInt())
    RecentKind.GROUP -> navigateToGroupsDetail(target.id.toInt())
    RecentKind.CENTER -> navigateToCenterDetail(target.id.toInt())
    RecentKind.LOAN -> navigateToLoanProfile(target.id.toInt())
}

private fun NavController.navigateToQuickAction(action: QuickAction) = when (action) {
    QuickAction.NEW_CLIENT -> navigateToCreateNewClient()
    QuickAction.NEW_GROUP -> navigateToCreateGroup()
    QuickAction.NEW_CENTER -> navigateToCreateCenter()
    QuickAction.COLLECTION_SHEET -> navigateToIndividualCollectionSheet()
    QuickAction.RUN_REPORTS -> navigateToReports()
    QuickAction.SEARCH -> navigateToSearch()
    QuickAction.PATH_TRACKER -> navigateToPathTracking()
}
