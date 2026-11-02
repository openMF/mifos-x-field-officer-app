/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.notification.NotificationRepository
import kpt.core.model.notification.AppNotification

/** Scripted notifications; [markAllReadResult] is what the write answers, [markedRead] counts the calls. */
@ExperimentalDataTestingApi
class FakeNotificationRepository(
    notifications: List<AppNotification> = emptyList(),
    var markAllReadResult: MutationResult<Unit> = MutationResult.Applied(Unit, synced = true),
) : NotificationRepository {

    val notifications = MutableStateFlow<ScreenState<List<AppNotification>>>(ScreenState.Content(notifications))
    var markedRead = 0

    override fun notificationsStream(scope: CoroutineScope) = screenDataStreamForTesting(notifications)

    override suspend fun markAllRead(): MutationResult<Unit> {
        markedRead++
        if (markAllReadResult is MutationResult.Applied) {
            val current = (notifications.value as? ScreenState.Content)?.data.orEmpty()
            notifications.value = ScreenState.Content(current.map { it.copy(isRead = true) })
        }
        return markAllReadResult
    }
}
