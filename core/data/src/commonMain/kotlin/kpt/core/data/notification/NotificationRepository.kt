/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.notification

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.notification.AppNotification

/** The bell's notifications (unread first, then recent history) and the one write it offers. */
interface NotificationRepository {

    fun notificationsStream(scope: CoroutineScope): ScreenDataStream<List<AppNotification>>

    /** Marks every notification read on the server. Online only — the list it changes is the server's. */
    suspend fun markAllRead(): MutationResult<Unit>
}
