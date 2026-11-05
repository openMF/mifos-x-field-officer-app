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
import kotlinx.coroutines.flow.first
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
import kpt.core.data.savings.impl.ingest
import kpt.core.model.notification.AppNotification
import kpt.core.network.mifos.notification.api.NotificationApi
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest

/**
 * The bell: reads through the notifications store; "mark all read" goes through the write door, and
 * its local effect — every cached notification read — is written through [localStore], the same
 * cache row [store] reads, so the list re-emits at once.
 */
@RepositoryBinding(binds = NotificationRepository::class)
internal class NotificationRepositoryImpl(
    private val notificationApi: NotificationApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Notifications) private val store: Store<Unit, List<AppNotification>>,
    @FromStore(AppStoreIds.NotificationsLocal) private val localStore: MutableStore<Unit, List<AppNotification>>,
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
        CommandSpec(
            payload = Unit,
            endpoint = { notificationApi.markAllRead() },
            // Fully known from the request: nothing the server returns is needed. A failure here must
            // not turn the accepted command into Failed — the next fetch corrects the row.
            localApply = { response -> if (response != null) ingest { markCachedRead() } },
        ),
        policy = MutationPolicy.OnlineRequired,
    )

    /** Rewrites the cached list with every notification read; nothing cached means nothing to change. */
    private suspend fun markCachedRead() {
        val cached = store.stream(StoreReadRequest.localOnly(Unit))
            .first { it !is StoreReadResponse.Loading }
            .dataOrNull() ?: return
        if (cached.any { !it.isRead }) {
            localStore.write(
                StoreWriteRequest.of<Unit, List<AppNotification>, Any>(
                    key = Unit,
                    value = cached.map { it.copy(isRead = true) },
                ),
            )
        }
    }
}
