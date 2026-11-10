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
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.composableWithStayTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.common.utils.Constants
import kpt.feature.groups.create.CreateGroupScreen
import kpt.feature.groups.detail.GroupsDetailScreen
import kpt.feature.groups.list.GroupsListScreen

/** The tab's graph. Mirrors `ClientDestination` — the tab points at the graph, not at a screen. */
@Serializable
data object GroupsDestination

/** The group list. */
@Serializable
data object GroupsListRoute

/**
 * Group detail. `groupId` is a type-safe route argument, not a SavedStateHandle default.
 *
 * @property groupId the Fineract group id
 */
@Serializable
data class GroupsDetailRoute(val groupId: Int)

/** Create a group. Reached from the groups list's FAB. */
@Serializable
data object CreateGroupRoute

/** Opens the group list. */
fun NavController.navigateToGroupsList(navOptions: NavOptions? = null) =
    navigate(GroupsListRoute, navOptions)

/** Opens the groups graph (the tab's destination). */
fun NavController.navigateToGroups(navOptions: NavOptions? = null) =
    navigate(GroupsDestination, navOptions)

/** Opens group [groupId]'s details. */
fun NavController.navigateToGroupsDetail(groupId: Int, navOptions: NavOptions? = null) =
    navigate(GroupsDetailRoute(groupId), navOptions)

/** Opens the new-group form. */
fun NavController.navigateToCreateGroup(navOptions: NavOptions? = null) =
    navigate(CreateGroupRoute, navOptions)

/**
 * Where the group screens lead outside this feature. A feature never depends on another, so these are
 * callbacks; `ForkFeatureRegistry` fills each with the owning feature's `navigateTo…` of the same shape.
 *
 * @property activateGroup opens the activation form for a group
 * @property openDocumentList opens an entity's documents (`entityType`, `entityId`)
 * @property openDataTableList opens an entity's datatables (`appTable`, `entityId`)
 * @property openGroupLoanApplication opens the group loan application
 * @property openSavingsApplication opens a savings application (`ownerId`, `isGroup`)
 * @property openNotes opens an entity's notes (`resourceType`, `resourceId`)
 * @property openLoanSummary opens a loan account's summary
 * @property openSavingsSummary opens a savings account's summary (`accountId`, deposit-type endpoint)
 * @property openClientProfile opens a client's profile
 */
data class GroupLinks(
    val activateGroup: (groupId: Int) -> Unit,
    val openDocumentList: (entityType: String, entityId: Int) -> Unit,
    val openDataTableList: (appTable: String, entityId: Int) -> Unit,
    val openGroupLoanApplication: (groupId: Int) -> Unit,
    val openSavingsApplication: (ownerId: Int, isGroup: Boolean) -> Unit,
    val openNotes: (resourceType: String, resourceId: Int) -> Unit,
    val openLoanSummary: (loanId: Int) -> Unit,
    val openSavingsSummary: (accountId: Int, accountType: String) -> Unit,
    val openClientProfile: (clientId: Int) -> Unit,
)

/**
 * The group list — a TOP-LEVEL destination on the authenticated graph. Not `@FeatureDestination`: the
 * detail screen opens other features, so `ForkFeatureRegistry` registers it with [GroupLinks].
 *
 * ## Writes from this list
 *
 * `CreateNewGroup` → [CreateGroupRoute] (the list's create action). `SyncGroupDialog` became the
 * list's multi-select "Download for offline use". Activation lives on the detail screen.
 */
fun NavGraphBuilder.groupsListDestination(navController: NavController, links: GroupLinks) {
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
            onActivate = links.activateGroup,
            // The generic document list, scoped to this group — the original's `loadDocumentList`.
            onDocuments = { groupId ->
                links.openDocumentList(Constants.ENTITY_TYPE_GROUPS, groupId)
            },
            // The group's datatables. The APPLICATION table name is what selects them — the
            // original passed `Constants.DATA_TABLE_NAME_GROUP` here.
            onDataTables = { groupId ->
                links.openDataTableList(Constants.DATA_TABLE_NAME_GROUP, groupId)
            },
            // The group loan application form in `feature/loan` — the original's
            // `addGroupLoanAccount`. This edge is what makes `GroupLoanApplicationRoute`
            // reachable; it was registered and caller-less until S4k.
            onAddLoanAccount = links.openGroupLoanApplication,
            // The savings application in `feature/savings`, as a GROUP application — the original's
            // `addSavingsAccount` edge passed `(groupId, 0, true)` into a wizard that read only the
            // client id, so this path created an application against client 0.
            onAddSavingsAccount = { groupId ->
                links.openSavingsApplication(groupId, true)
            },
            // The original's `loadNotes = navigateToNoteScreen` with the group as owner.
            onNotes = { groupId -> links.openNotes(Constants.ENTITY_TYPE_GROUPS, groupId) },
            // The original's `loadLoanAccountSummary` / `loadSavingsAccountSummary`.
            onOpenLoan = links.openLoanSummary,
            onOpenSavings = links.openSavingsSummary,
            // A member row opens that client's profile.
            onOpenClient = links.openClientProfile,
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
