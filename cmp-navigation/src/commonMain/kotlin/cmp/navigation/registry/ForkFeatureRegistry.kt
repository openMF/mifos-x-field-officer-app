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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.data.di.ForkRepositoryModule
import kpt.core.database.di.ForkDatabaseModule
import kpt.core.datastore.di.ForkDatastoreModule
import kpt.core.network.di.ForkNetworkModule
import kpt.core.store.di.ForkStoreModule
import kpt.feature.activate.navigation.navigateToActivateCenter
import kpt.feature.activate.navigation.navigateToActivateClient
import kpt.feature.activate.navigation.navigateToActivateGroup
import kpt.feature.center.navigation.CenterLinks
import kpt.feature.center.navigation.centerListDestination
import kpt.feature.center.navigation.navigateToCenterDetail
import kpt.feature.charge.navigation.navigateToAddCharge
import kpt.feature.charge.navigation.navigateToCharges
import kpt.feature.checkerinbox.navigation.CheckerInboxLinks
import kpt.feature.checkerinbox.navigation.checkerInboxDestinations
import kpt.feature.client.navigation.ClientLinks
import kpt.feature.client.navigation.clientListDestination
import kpt.feature.client.navigation.navigateToClientProfile
import kpt.feature.client.navigation.openClientRecord
import kpt.feature.datatable.navigation.navigateToDataTableList
import kpt.feature.document.navigation.navigateToDocumentList
import kpt.feature.groups.navigation.GroupLinks
import kpt.feature.groups.navigation.groupsListDestination
import kpt.feature.groups.navigation.navigateToGroupsDetail
import kpt.feature.loan.navigation.LoanLinks
import kpt.feature.loan.navigation.loanDestinations
import kpt.feature.loan.navigation.navigateToGroupLoanApplication
import kpt.feature.loan.navigation.navigateToLoanProfile
import kpt.feature.loan.navigation.navigateToLoanRepayment
import kpt.feature.loan.navigation.navigateToLoanReschedules
import kpt.feature.loan.navigation.navigateToLoanSummary
import kpt.feature.loan.navigation.navigateToNewLoanApplication
import kpt.feature.note.navigation.navigateToClientNotes
import kpt.feature.note.navigation.navigateToNotes
import kpt.feature.notification.navigation.NotificationLinks
import kpt.feature.notification.navigation.notificationsDestination
import kpt.feature.offlinesync.navigation.OfflineSyncLinks
import kpt.feature.offlinesync.navigation.offlineSyncDestination
import kpt.feature.savings.navigation.SavingsLinks
import kpt.feature.savings.navigation.navigateToRecurringDepositApplication
import kpt.feature.savings.navigation.navigateToSavingsApplication
import kpt.feature.savings.navigation.navigateToSavingsApproval
import kpt.feature.savings.navigation.navigateToSavingsSummary
import kpt.feature.savings.navigation.savingsDestination
import kpt.feature.searchrecord.navigation.navigateToSearchRecord
import kpt.feature.searchrecord.navigation.searchRecordDestination
import kpt.feature.settings.navigateToSyncAndDrafts
import org.koin.core.module.Module

/**
 * ForkFeatureRegistry — the FORK-OWNED white-label seam for feature contributions.
 *
 * The template infra modules READ from this registry; a fork extends the app by editing THIS ONE file
 * (+ its build.gradle deps + settings.gradle include), never the template infra files:
 *   - `cmp-navigation/di/KoinModules.kt` includes [featureKoinModules] into the app DI graph.
 *   - `cmp-navigation/.../AuthenticatedNavigation.kt` invokes [featureDestinations] to register routes.
 *
 * Ownership: `owner: fork` in customization-surface.yaml — `sync-dirs`/`white-label-doctor` NEVER
 * overwrite it, so a template sync full-copies the infra modules while your features survive. The
 * template ships this file pre-populated with its demo feature set as the default; a fork replaces the
 * contents with its own (the customizer `--clean` empties both lists).
 */
object ForkFeatureRegistry {
    /**
     * Feature Koin modules the app installs. The framework SHELL modules (Home, Settings) live in
     * [cmp.navigation.di.KoinModules] and are always present; this is the fork's own features.
     */
    /**
     * The four per-layer fork seams, plus every `feature/<f>/di` Koin module.
     *
     * The feature half is DERIVED — `:cmp-navigation:generateFeatureKoinBindings` reads each
     * feature module's `di` package and emits [GeneratedFeatureKoinBindings]. Adding a
     * feature no longer means editing this file: previously it took an import AND a list entry
     * here, and forgetting either compiled cleanly while the feature's ViewModels failed to
     * resolve at runtime.
     *
     * The `Project*Module` seams stay listed BY HAND on purpose — they are core-layer fork seams,
     * not features, and nothing under `feature/` declares them.
     */
    val featureKoinModules: List<Module> = listOf(
        ForkRepositoryModule,
        ForkNetworkModule,
        ForkDatabaseModule,
        ForkDatastoreModule,
        ForkStoreModule,
        GeneratedFeatureKoinBindings,
    )

    /**
     * Every destination of the signed-in graph.
     *
     * A feature whose screens only open each other declares a `@FeatureDestination` graph in its
     * `navigation` package; `:cmp-navigation:generateFeatureDestinations` collects those into
     * [GeneratedFeatureDestinations]. A graph that opens ANOTHER feature's screen takes those links as a
     * `<Feature>Links` of callbacks and is registered below — features never depend on each other, so
     * this registry is the one place that connects them. Each link is the target feature's own
     * `navigateTo…`.
     */
    val featureDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
        GeneratedFeatureDestinations(navController)
        centerListDestination(
            navController = navController,
            links = CenterLinks(
                openGroup = { navController.navigateToGroupsDetail(it) },
                activateCenter = { navController.navigateToActivateCenter(it) },
                addSavingsApplication = { navController.navigateToSavingsApplication(ownerId = it, isGroup = true) },
            ),
        )
        checkerInboxDestinations(
            navController = navController,
            links = CheckerInboxLinks(openLoanReschedules = { navController.navigateToLoanReschedules(it) }),
        )
        groupsListDestination(
            navController = navController,
            links = GroupLinks(
                activateGroup = { navController.navigateToActivateGroup(it) },
                openDocumentList = { type, id -> navController.navigateToDocumentList(type, id) },
                openDataTableList = { table, id -> navController.navigateToDataTableList(table, id) },
                openGroupLoanApplication = { navController.navigateToGroupLoanApplication(it) },
                openSavingsApplication = { owner, isGroup ->
                    navController.navigateToSavingsApplication(owner, isGroup)
                },
                openNotes = { type, id -> navController.navigateToNotes(type, id) },
                openLoanSummary = { navController.navigateToLoanSummary(it) },
                openSavingsSummary = { id, type -> navController.navigateToSavingsSummary(id, type) },
                openClientProfile = { navController.navigateToClientProfile(it) },
            ),
        )
        loanDestinations(
            navController = navController,
            links = LoanLinks(
                openCharges = { resource, id -> navController.navigateToCharges(resource, id) },
                openDocumentList = { type, id -> navController.navigateToDocumentList(type, id) },
                openDataTableList = { table, id -> navController.navigateToDataTableList(table, id) },
                openNotes = { type, id -> navController.navigateToNotes(type, id) },
            ),
        )
        savingsDestination(
            navController = navController,
            links = SavingsLinks(
                openCharges = { resource, id -> navController.navigateToCharges(resource, id) },
                openDocumentList = { type, id -> navController.navigateToDocumentList(type, id) },
                openDataTableList = { table, id -> navController.navigateToDataTableList(table, id) },
            ),
        )
        notificationsDestination(
            navController = navController,
            links = NotificationLinks(
                openLoan = { navController.navigateToLoanProfile(it) },
                openClient = { navController.navigateToClientProfile(it) },
                openGroup = { navController.navigateToGroupsDetail(it) },
                openCenter = { navController.navigateToCenterDetail(it) },
                openSavings = { navController.navigateToSavingsSummary(it) },
            ),
        )
        offlineSyncDestination(
            navController = navController,
            links = OfflineSyncLinks(
                openCenter = { navController.navigateToCenterDetail(it) },
                openGroup = { navController.navigateToGroupsDetail(it) },
                openClient = { navController.navigateToClientProfile(it) },
                openDrafts = { navController.navigateToSyncAndDrafts() },
            ),
        )
        clientListDestination(
            navController = navController,
            links = ClientLinks(
                openClientNotes = { navController.navigateToClientNotes(it) },
                openGroup = { navController.navigateToGroupsDetail(it) },
                openCharges = { resource, id -> navController.navigateToCharges(resource, id) },
                openAddCharge = { resource, id -> navController.navigateToAddCharge(resource, id) },
                openSearchRecord = { navController.navigateToSearchRecord(it) },
                openNewLoanApplication = { navController.navigateToNewLoanApplication(it) },
                openSavingsApplication = { owner, isGroup ->
                    navController.navigateToSavingsApplication(owner, isGroup)
                },
                openRecurringDepositApplication = { navController.navigateToRecurringDepositApplication(it) },
                activateClient = { navController.navigateToActivateClient(it) },
                openLoanProfile = { navController.navigateToLoanProfile(it) },
                openLoanRepayment = { navController.navigateToLoanRepayment(it) },
                openSavingsSummary = { id, type -> navController.navigateToSavingsSummary(id, type) },
                openSavingsApproval = { navController.navigateToSavingsApproval(it) },
            ),
        )
        // Record search belongs to feature/searchrecord; both ends of a search are client screens.
        searchRecordDestination(
            onBackClick = { navController.popBackStackSafely() },
            onRecordSelected = { navController.openClientRecord(it) },
        )
    }
}
