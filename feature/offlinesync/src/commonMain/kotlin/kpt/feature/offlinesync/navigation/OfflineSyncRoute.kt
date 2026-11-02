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
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.model.offline.RecordKind
import kpt.feature.center.navigation.navigateToCenterDetail
import kpt.feature.client.navigation.navigateToClientProfile
import kpt.feature.groups.navigation.navigateToGroupsDetail
import kpt.feature.offlinesync.ui.OfflineSyncScreen
import kpt.feature.settings.navigateToSyncAndDrafts

/** Settings → Offline sync. */
@Serializable
data object OfflineSyncRoute

@FeatureDestination
fun NavGraphBuilder.offlineSyncDestination(navController: NavController) {
    composableWithPushTransitions<OfflineSyncRoute> {
        OfflineSyncScreen(
            onNavigateBack = { navController.popBackStack() },
            onOpenRecord = { kind, id ->
                when (kind) {
                    RecordKind.CENTRE -> navController.navigateToCenterDetail(id)
                    RecordKind.GROUP -> navController.navigateToGroupsDetail(id)
                    RecordKind.CLIENT -> navController.navigateToClientProfile(id)
                }
            },
            onOpenDrafts = { navController.navigateToSyncAndDrafts() },
        )
    }
}

fun NavController.navigateToOfflineSync() = navigate(OfflineSyncRoute)
