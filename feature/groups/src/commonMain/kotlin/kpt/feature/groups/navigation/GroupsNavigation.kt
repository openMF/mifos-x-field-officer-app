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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.composableWithStayTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.common.utils.Constants
import kpt.feature.activate.navigation.navigateToActivateGroup
import kpt.feature.datatable.navigation.navigateToDataTableList
import kpt.feature.document.navigation.navigateToDocumentList
import kpt.feature.groups.create.CreateGroupScreen
import kpt.feature.groups.detail.GroupsDetailScreen
import kpt.feature.groups.list.GroupsListScreen
import kpt.feature.loan.navigation.navigateToGroupLoanApplication
import kpt.feature.loan.navigation.navigateToLoanSummary
import kpt.feature.note.navigation.navigateToNotes
import kpt.feature.savings.navigation.navigateToSavingsApplication
import kpt.feature.savings.navigation.navigateToSavingsSummary

/** The tab's graph. Mirrors `ClientDestination` — the tab points at the graph, not at a screen. */
@Serializable
data object GroupsDestination

@Serializable
data object GroupsListRoute

/** Group detail. `groupId` is a type-safe route argument, not a SavedStateHandle default. */
@Serializable
data class GroupsDetailRoute(val groupId: Int)

/** Create a group. Reached from the groups list's FAB. */
@Serializable
data object CreateGroupRoute

fun NavController.navigateToGroupsList(navOptions: NavOptions? = null) =
    navigate(GroupsListRoute, navOptions)

fun NavController.navigateToGroups(navOptions: NavOptions? = null) =
    navigate(GroupsDestination, navOptions)

fun NavController.navigateToGroupsDetail(groupId: Int, navOptions: NavOptions? = null) =
    navigate(GroupsDetailRoute(groupId), navOptions)

fun NavController.navigateToCreateGroup(navOptions: NavOptions? = null) =
    navigate(CreateGroupRoute, navOptions)

/**
 * The group list — a TOP-LEVEL destination on the authenticated graph, collected into
 * `GeneratedFeatureDestinations` by `:cmp-navigation:generateFeatureDestinations`.
 *
 * This file MUST live in a `navigation` package: the generator calls `scan(roots, "navigation")`, so
 * an annotated destination anywhere else is silently skipped — a clean build and a screen that does
 * not exist in the app.
 *
 * ## Writes from this list
 *
 * `CreateNewGroup` → [CreateGroupRoute] (the list's create action). `SyncGroupDialog` became the
 * list's multi-select "Download for offline use". Activation lives on the detail screen.
 */
@FeatureDestination
fun NavGraphBuilder.groupsListDestination(navController: NavController) {
    composableWithStayTransitions<GroupsListRoute> {
        GroupsListScreen(
            onGroupClick = navController::navigateToGroupsDetail,
            onCreateGroup = navController::navigateToCreateGroup,
        )
    }

    // Nested drill-down, deliberately NOT annotated: only TOP-LEVEL entries carry
    // @FeatureDestination, and annotating this would register detail as its own root entry.
    // Create a group. Nested drill-down, NOT annotated.
    composableWithPushTransitions<CreateGroupRoute> {
        CreateGroupScreen(
            onNavigateBack = navController::popBackStackSafely,
            // The original's `onGroupCreated`: pop, and when the write reached Fineract open the new
            // group. A queued (offline) create has no server id yet, so it only pops.
            onCreated = { synced, groupId ->
                navController.popBackStackSafely()
                if (synced && groupId != null) navController.navigateToGroupsDetail(groupId)
            },
        )
    }

    composableWithPushTransitions<GroupsDetailRoute> {
        GroupsDetailScreen(
            onBackClick = navController::popBackStackSafely,
            onActivate = navController::navigateToActivateGroup,
            // The generic document list, scoped to this group — the original's `loadDocumentList`.
            onDocuments = { groupId ->
                navController.navigateToDocumentList(Constants.ENTITY_TYPE_GROUPS, groupId)
            },
            // The group's datatables. The APPLICATION table name is what selects them — the
            // original passed `Constants.DATA_TABLE_NAME_GROUP` here.
            onDataTables = { groupId ->
                navController.navigateToDataTableList(Constants.DATA_TABLE_NAME_GROUP, groupId)
            },
            // The group loan application form in `feature/loan` — the original's
            // `addGroupLoanAccount`. This edge is what makes `GroupLoanApplicationRoute`
            // reachable; it was registered and caller-less until S4k.
            onAddLoanAccount = navController::navigateToGroupLoanApplication,
            // The savings application in `feature/savings`, as a GROUP application — the original's
            // `addSavingsAccount` edge passed `(groupId, 0, true)` into a wizard that read only the
            // client id, so this path created an application against client 0.
            onAddSavingsAccount = { groupId ->
                navController.navigateToSavingsApplication(ownerId = groupId, isGroup = true)
            },
            // The original's `loadNotes = navigateToNoteScreen` with the group as owner.
            onNotes = { groupId -> navController.navigateToNotes(Constants.ENTITY_TYPE_GROUPS, groupId) },
            // The original's `loadLoanAccountSummary` / `loadSavingsAccountSummary`.
            onOpenLoan = navController::navigateToLoanSummary,
            onOpenSavings = { accountId, endpoint -> navController.navigateToSavingsSummary(accountId, endpoint) },
            // A member row opens that client's profile. feature/client depends on feature/groups (the
            // profile's Group chip opens this screen), so this edge goes by the route's NAME rather than
            // its class — `ClientProfileRouteNameTest` (feature/client) pins the name to the class.
            onOpenClient = { clientId -> navController.navigate(clientProfileRoute(clientId)) },
        )
    }
}

/**
 * The group graph for the bottom-nav tab's INNER NavHost.
 *
 * Same two-NavHost split as `clientTabGraph` / `centerTabGraph`: the list is the tab's start
 * destination so the bottom bar persists and the tab keeps its own back stack, while detail pushes
 * on the OUTER controller and covers the bar.
 *
 * Not annotated — `@FeatureDestination` collects entries for the outer graph, and registering the
 * same routes twice is a duplicate-destination crash at NavHost build.
 */
fun NavGraphBuilder.groupsTabGraph(outerNav: NavController) {
    navigation<GroupsDestination>(startDestination = GroupsListRoute) {
        composableWithStayTransitions<GroupsListRoute> {
            // The TAB-graph copy — the path a field officer actually takes from the bottom
            // bar. Wiring only the other call site would ship a FAB that exists on one entry
            // path and is missing on the one people use.
            GroupsListScreen(
                onGroupClick = outerNav::navigateToGroupsDetail,
                onCreateGroup = outerNav::navigateToCreateGroup,
            )
        }
    }
}

/**
 * `ClientProfileRoute(clientId)` by name — the class lives in feature/client, which depends on this
 * module. Type-safe routes register as `<serial name>/{clientId}`, so this string matches it.
 */
const val CLIENT_PROFILE_ROUTE_NAME = "kpt.feature.client.navigation.ClientProfileRoute"

fun clientProfileRoute(clientId: Int): String = "$CLIENT_PROFILE_ROUTE_NAME/$clientId"
