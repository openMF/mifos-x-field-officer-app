/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kpt.core.model.utils.DemoCredentials
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.ServerInstance
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteServerInstanceDto

/**
 * The server instances the officer can pick — the same shape as mifos-pay's Supabase `app_config`
 * row (`instances[]`), extended with three optional fields every app ignores unless it uses them:
 *
 *  - `supportedApps` — the apps that offer this instance. One `app_config` list serves every Mifos
 *    app; each shows only its own. This app is [AppServerConfig.APP_ID].
 *  - `demoAccounts` — the PUBLISHED sign-ins of a public demo instance, one per role, shown so a
 *    tester can sign in. Source: the Mifos Mobile Apps Community wiki (mifosforge Confluence, MP space).
 *  - `port` — omitted means the scheme's default.
 */
@Serializable
internal data class InstancesConfigDto(val instances: List<InstanceDto> = emptyList())

@Serializable
internal data class InstanceDto(
    val endpoint: String,
    val protocol: String,
    val path: String,
    val platformTenantId: String,
    val label: String,
    val isDefault: Boolean = false,
    val port: String? = null,
    val supportedApps: List<String> = emptyList(),
    val demoAccounts: List<DemoDto> = emptyList(),
)

@Serializable
internal data class DemoDto(val role: String, val username: String, val password: String)

private val json = Json { ignoreUnknownKeys = true }

/**
 * [config] parsed and filtered to the instances that list [app] in `supportedApps`. Strict: an
 * instance that names no apps is NOT offered here, so a row written for another app (mifos-pay's,
 * which predates the field) never shows up in this one.
 */
fun parseInstances(config: String, app: String = AppServerConfig.APP_ID): List<ServerInstance> =
    json.decodeFromString<InstancesConfigDto>(config).instances
        // Row order, not `isDefault`: the shared row's `isDefault` is mifos-pay's default (it takes the first
        // one), and this app's default server is app-profile's main access point, not the list's.
        .filter { app in it.supportedApps }
        .map { dto ->
            ServerInstance(
                id = "${dto.endpoint}/${dto.platformTenantId}",
                name = dto.label,
                config = ServerConfig(
                    protocol = dto.protocol,
                    endPoint = dto.endpoint,
                    apiPath = dto.path,
                    port = dto.port ?: if (dto.protocol == "https://") "443" else "80",
                    tenant = dto.platformTenantId,
                ),
                demoAccounts = dto.demoAccounts.map { DemoCredentials(it.role, it.username, it.password) },
            )
        }

/**
 * `app_server_config` rows as the `{"instances": [...]}` document [parseInstances] reads — so the
 * Supabase list, its device cache and the bundled list all go through ONE parser.
 */
fun instancesConfigJson(rows: List<RemoteServerInstanceDto>): String = json.encodeToString(
    InstancesConfigDto.serializer(),
    InstancesConfigDto(
        rows.map { r ->
            InstanceDto(
                endpoint = r.endpoint,
                protocol = r.protocol,
                path = r.path,
                platformTenantId = r.platformTenantId,
                label = r.label,
                isDefault = r.isDefault,
                port = r.port,
                supportedApps = r.supportedApps,
                demoAccounts = r.demoAccounts.map { DemoDto(it.role, it.username, it.password) },
            )
        },
    ),
)

/**
 * Where the instance list comes from when nothing better is available. The live list is the Supabase
 * `app_server_config` table (core/store `ServerInstancesStore`), parsed by the SAME [parseInstances];
 * core/data falls back to [BundledInstanceConfigLoader] on any failure — mifos-pay's
 * `SupabaseInstanceConfigLoader` contract.
 */
fun interface InstanceConfigLoader {
    /** Loads the published server instances. */
    suspend fun load(): List<ServerInstance>
}

/** The list that ships in the app — also the fallback a remote loader must use. */
object BundledInstanceConfigLoader : InstanceConfigLoader {
    override suspend fun load(): List<ServerInstance> = parseInstances(DEFAULT_INSTANCES_CONFIG)
}

/**
 * The shipped list, as JSON in the `app_config` shape. The gateway entry must stay equal to
 * [AppServerConfig.DEFAULT] (app-profile) — `InstancesConfigTest` fails if they drift.
 */
internal const val DEFAULT_INSTANCES_CONFIG = """
{
  "instances": [
    {
      "endpoint": "apis.mifos.community",
      "protocol": "https://",
      "path": "/1.0/field/v1/",
      "platformTenantId": "mifos-bank-1",
      "label": "Mifos field officer gateway",
      "isDefault": true,
      "supportedApps": ["field-officer"],
      "demoAccounts": [
        { "role": "Field officer", "username": "fieldofficer", "password": "password" },
        { "role": "Supervisor", "username": "supervisor", "password": "password" },
        { "role": "Capturist", "username": "capturist", "password": "password" },
        { "role": "Control officer", "username": "controlofficer", "password": "password" },
        { "role": "Treasurer", "username": "treasurer", "password": "password" }
      ]
    },
    {
      "endpoint": "mifos-bank-2.mifos.community",
      "protocol": "https://",
      "path": "/fineract-provider/api/v1/",
      "platformTenantId": "mifos-bank-2",
      "label": "Mifos bank 2",
      "supportedApps": ["field-officer", "mifos-pay"],
      "demoAccounts": [
        { "role": "Administrator", "username": "mifos", "password": "password" }
      ]
    },
    {
      "endpoint": "sandbox.mifos.community",
      "protocol": "https://",
      "path": "/fineract-provider/api/v1/",
      "platformTenantId": "default",
      "label": "Mifos sandbox",
      "supportedApps": ["field-officer"],
      "demoAccounts": [
        { "role": "Administrator", "username": "mifos", "password": "password" }
      ]
    },
    {
      "endpoint": "localhost",
      "protocol": "http://",
      "path": "/fineract-provider/api/v1/",
      "port": "8080",
      "platformTenantId": "default",
      "label": "Localhost",
      "supportedApps": ["field-officer"]
    }
  ]
}
"""
