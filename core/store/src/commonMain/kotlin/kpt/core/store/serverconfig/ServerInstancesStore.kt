/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.serverconfig

import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.serverconfig.dao.ServerInstanceDao
import kpt.core.database.serverconfig.entity.ServerInstanceEntity
import kpt.core.database.utils.getCurrentTimeInMillis
import kpt.core.model.utils.DemoCredentials
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.ServerInstance
import kpt.core.network.fieldofficer.AppServerConfig
import kpt.core.network.fieldofficer.BundledInstanceConfigLoader
import kpt.core.network.fieldofficer.instancesConfigJson
import kpt.core.network.fieldofficer.parseInstances
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.api.ServerInstanceApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

private val demoJson = Json { ignoreUnknownKeys = true }

/**
 * The server picker's instance list — network -> database -> store.
 *
 * Fetcher: Supabase `app_server_config`, only this app's rows (the server filters on `supported_apps`),
 * parsed by core/network's ONE instance parser. Source of truth: the `server_instances` table, so the
 * list survives offline and a relaunch.
 *
 * With NO Supabase key the app has no remote list by design, so the fetcher answers with the list
 * shipped in the app; the same when the table holds no row for this app. A transport failure is NOT
 * masked — it is a fetch error, so the screen shows the cached list (or the error and a retry).
 *
 * `logout = false`: public configuration, and sign-in needs it right after a sign-out.
 */
// logout-exempt: public configuration, and sign-in needs it right after a sign-out
@StoreProvider(id = "serverInstances", ttl = "12h", logout = false)
@CacheKey(name = "KEY", key = "serverInstances")
fun provideServerInstancesStore(api: ServerInstanceApi, dao: ServerInstanceDao): Store<Unit, List<ServerInstance>> =
    StoreFactory.createStore(
        fetcher = Fetcher.of { _: Unit ->
            if (!api.isConfigured) return@of BundledInstanceConfigLoader.load()
            parseInstances(instancesConfigJson(api.fetchInstances(AppServerConfig.APP_ID)))
                .ifEmpty { BundledInstanceConfigLoader.load() }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit ->
                // Empty = never fetched: null makes Store5 fetch instead of serving "no servers".
                dao.observeAll().map { rows -> rows.takeIf { it.isNotEmpty() }?.map { it.toModel() } }
            },
            writer = { _: Unit, instances: List<ServerInstance> ->
                val now = getCurrentTimeInMillis()
                dao.replaceAll(instances.mapIndexed { position, instance -> instance.toEntity(position, now) })
            },
        ),
    )

private fun ServerInstanceEntity.toModel() = ServerInstance(
    id = id,
    name = name,
    config = ServerConfig(protocol = protocol, endPoint = endPoint, apiPath = apiPath, port = port, tenant = tenant),
    demoAccounts = demoJson.decodeFromString<List<DemoCredentials>>(demoAccounts),
)

private fun ServerInstance.toEntity(position: Int, fetchedAtMs: Long) = ServerInstanceEntity(
    id = id,
    name = name,
    protocol = config.protocol,
    endPoint = config.endPoint,
    apiPath = config.apiPath,
    port = config.port,
    tenant = config.tenant,
    demoAccounts = demoJson.encodeToString(demoAccounts),
    position = position,
    fetchedAtMs = fetchedAtMs,
)
