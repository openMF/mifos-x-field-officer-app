/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.notification

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.notification.AppNotification
import kpt.core.network.mifos.notification.api.NotificationApi
import kpt.core.network.mifos.notification.dto.NotificationDto
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

// Read history kept on screen. Unread rows are always all fetched — they are the point of the bell.
private const val READ_HISTORY = 50
private const val UNREAD_CAP = 200

/**
 * The signed-in user's notifications, unread first then the recent read history — two reads,
 * because Fineract's `GET notifications` returns only one side of `isRead` per call.
 */
@StoreProvider(id = "notifications", ttl = "5m")
@CacheKey(name = "LIST", key = "notifications")
fun provideNotificationsStore(
    notificationApi: NotificationApi,
    cache: ApiResponseCacheDao,
): Store<Unit, List<AppNotification>> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.Notifications.LIST },
    fetch = {
        val unread = notificationApi.getNotifications(isRead = false, limit = UNREAD_CAP).pageItems
        val read = notificationApi.getNotifications(isRead = true, limit = READ_HISTORY).pageItems
        (unread + read).distinctBy { it.id }.map(NotificationDto::toModel)
    },
)

internal fun NotificationDto.toModel() = AppNotification(
    id = id,
    objectType = objectType.orEmpty(),
    objectId = objectId,
    action = action.orEmpty(),
    content = content.orEmpty(),
    isRead = isRead,
    createdAt = createdAt.orEmpty(),
)
