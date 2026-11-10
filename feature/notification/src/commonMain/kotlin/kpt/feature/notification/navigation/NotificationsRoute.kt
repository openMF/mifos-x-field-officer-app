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
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.model.notification.AppNotification
import kpt.feature.notification.ui.NotificationTarget
import kpt.feature.notification.ui.NotificationsScreen
import kpt.feature.notification.ui.target

/** The top bar's bell. */
@Serializable
data object NotificationsRoute

/**
 * Where a screen in this feature opens a record owned by another feature. A feature never depends on
 * another, so these are callbacks; `ForkFeatureRegistry` fills each with the owning feature's `navigateTo…`.
 *
 * @property openLoan opens a loan's profile
 * @property openClient opens a client's profile
 * @property openGroup opens a group's details
 * @property openCenter opens a centre's details
 * @property openSavings opens a savings account's summary
 */
data class NotificationLinks(
    val openLoan: (loanId: Int) -> Unit,
    val openClient: (clientId: Int) -> Unit,
    val openGroup: (groupId: Int) -> Unit,
    val openCenter: (centerId: Int) -> Unit,
    val openSavings: (accountId: Int) -> Unit,
)

/**
 * Registers the notifications screen in the nav graph. Not `@FeatureDestination`: a notification
 * opens the record it is about, in another feature, so `ForkFeatureRegistry` registers it with
 * [NotificationLinks].
 */
fun NavGraphBuilder.notificationsDestination(navController: NavController, links: NotificationLinks) {
    composableWithPushTransitions<NotificationsRoute> {
        NotificationsScreen(
            onNavigateBack = { navController.popBackStack() },
            onOpen = { links.openRecordOf(it) },
        )
    }
}

/** Opens the notifications screen. */
fun NavController.navigateToNotifications() = navigate(NotificationsRoute)

/** Opens the record a notification is about; the screen only offers the tap for these kinds. */
private fun NotificationLinks.openRecordOf(notification: AppNotification) {
    val id = notification.objectId?.toInt() ?: return
    when (notification.target) {
        NotificationTarget.LOAN -> openLoan(id)
        NotificationTarget.CLIENT -> openClient(id)
        NotificationTarget.GROUP -> openGroup(id)
        NotificationTarget.CENTER -> openCenter(id)
        NotificationTarget.SAVINGS -> openSavings(id)
        null -> Unit
    }
}
