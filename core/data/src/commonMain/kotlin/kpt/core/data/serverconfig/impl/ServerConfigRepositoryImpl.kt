/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.serverconfig.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.network.NetworkConfigRepository
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.serverconfig.ServerConfigRepository
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.ServerInstance
import kpt.core.model.utils.baseUrl
import kpt.core.network.fieldofficer.AppServerConfig
import kpt.core.network.fieldofficer.networkHeaders
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = ServerConfigRepository::class)
internal class ServerConfigRepositoryImpl(
    private val gateway: MutationGateway,
    private val networkConfigs: NetworkConfigRepository,
    @FromStore(AppStoreIds.ServerConfig) private val store: Store<Unit, ServerConfig>,
    @FromStore(AppStoreIds.ServerInstances) private val instancesStore: Store<Unit, List<ServerInstance>>,
) : ServerConfigRepository {

    override val defaultConfig: ServerConfig = AppServerConfig.DEFAULT

    // Store-backed like every other read: cached instances render at once, the remote list refreshes
    // behind them, and with nothing cached the screen shows its loading state until the fetch lands.
    override fun instancesStream(scope: CoroutineScope): ScreenDataStream<List<ServerInstance>> =
        instancesStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.ServerInstances.KEY,
            scope = scope,
            isEmpty = { it.isEmpty() },
        )

    override fun serverConfigStream(scope: CoroutineScope): ScreenDataStream<ServerConfig> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.ServerConfig.KEY,
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )

    // Through the gateway's local-mutation door, as `AuthCommandRepository.resumeStoredSession` is.
    // The write edits the MAIN runtime network config (core-base) — persisted there, applied to the
    // very next request, and re-emitted to the store's reader, so the stream follows on its own.
    override suspend fun updateServerConfig(config: ServerConfig) =
        gateway.localMutation(table = TABLE) {
            val main = requireNotNull(networkConfigs.mainId) { "app-profile marks no main access point" }
            networkConfigs.update(main, baseUrl = config.baseUrl(), headers = config.networkHeaders())
        }

    private companion object {
        const val TABLE = "server_config"
    }
}
