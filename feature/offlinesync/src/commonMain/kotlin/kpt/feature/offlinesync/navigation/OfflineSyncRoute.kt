/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.offlinesync.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.model.offline.RecordKind
import kpt.feature.offlinesync.ui.OfflineSyncScreen

/** Settings → Offline sync. */
@Serializable
data object OfflineSyncRoute

/**
 * Where the offline-sync screen leads outside this feature — callbacks `ForkFeatureRegistry` fills,
 * since a feature never depends on another.
 *
 * @property openCenter opens a centre's details
 * @property openGroup opens a group's details
 * @property openClient opens a client's profile
 * @property openDrafts opens Settings → Sync and drafts
 */
data class OfflineSyncLinks(
    val openCenter: (centerId: Int) -> Unit,
    val openGroup: (groupId: Int) -> Unit,
    val openClient: (clientId: Int) -> Unit,
    val openDrafts: () -> Unit,
)

/**
 * Registers the offline-sync screen in the nav graph. Not `@FeatureDestination`: it opens records and
 * drafts owned by other features, so `ForkFeatureRegistry` registers it with [OfflineSyncLinks].
 */
fun NavGraphBuilder.offlineSyncDestination(navController: NavController, links: OfflineSyncLinks) {
    composableWithPushTransitions<OfflineSyncRoute> {
        OfflineSyncScreen(
            onNavigateBack = { navController.popBackStack() },
            onOpenRecord = { kind, id ->
                when (kind) {
                    RecordKind.CENTRE -> links.openCenter(id)
                    RecordKind.GROUP -> links.openGroup(id)
                    RecordKind.CLIENT -> links.openClient(id)
                }
            },
            onOpenDrafts = links.openDrafts,
        )
    }
}

/** Opens the offline-sync screen. */
fun NavController.navigateToOfflineSync() = navigate(OfflineSyncRoute)
