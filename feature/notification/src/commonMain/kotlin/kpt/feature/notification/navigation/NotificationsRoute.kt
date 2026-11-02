/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.notification.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.model.notification.AppNotification
import kpt.feature.center.navigation.navigateToCenterDetail
import kpt.feature.client.navigation.navigateToClientProfile
import kpt.feature.groups.navigation.navigateToGroupsDetail
import kpt.feature.loan.navigation.navigateToLoanProfile
import kpt.feature.notification.ui.NotificationTarget
import kpt.feature.notification.ui.NotificationsScreen
import kpt.feature.notification.ui.target
import kpt.feature.savings.navigation.navigateToSavingsSummary

/** The top bar's bell. */
@Serializable
data object NotificationsRoute

@FeatureDestination
fun NavGraphBuilder.notificationsDestination(navController: NavController) {
    composableWithPushTransitions<NotificationsRoute> {
        NotificationsScreen(
            onNavigateBack = { navController.popBackStack() },
            onOpen = { navController.openRecordOf(it) },
        )
    }
}

fun NavController.navigateToNotifications() = navigate(NotificationsRoute)

/** Opens the record a notification is about; the screen only offers the tap for these kinds. */
private fun NavController.openRecordOf(notification: AppNotification) {
    val id = notification.objectId?.toInt() ?: return
    when (notification.target) {
        NotificationTarget.LOAN -> navigateToLoanProfile(id)
        NotificationTarget.CLIENT -> navigateToClientProfile(id)
        NotificationTarget.GROUP -> navigateToGroupsDetail(id)
        NotificationTarget.CENTER -> navigateToCenterDetail(id)
        NotificationTarget.SAVINGS -> navigateToSavingsSummary(id)
        null -> Unit
    }
}
