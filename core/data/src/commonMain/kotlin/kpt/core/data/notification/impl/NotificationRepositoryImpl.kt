/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.notification.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.notification.NotificationRepository
import kpt.core.model.notification.AppNotification
import kpt.core.network.mifos.notification.api.NotificationApi
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = NotificationRepository::class)
internal class NotificationRepositoryImpl(
    private val notificationApi: NotificationApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Notifications) private val store: Store<Unit, List<AppNotification>>,
) : NotificationRepository {

    // NETWORK_WITH_CACHE: the cached list shows at once, a fetch always runs behind it.
    override fun notificationsStream(scope: CoroutineScope): ScreenDataStream<List<AppNotification>> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.Notifications.LIST,
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.NOTIFICATIONS,
        )

    override suspend fun markAllRead(): MutationResult<Unit> = gateway.command(
        CommandSpec(payload = Unit, endpoint = { notificationApi.markAllRead() }),
        policy = MutationPolicy.OnlineRequired,
    )
}
