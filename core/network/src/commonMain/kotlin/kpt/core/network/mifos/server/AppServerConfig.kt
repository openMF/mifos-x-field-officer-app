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

import kpt.core.base.network.NetworkConfig
import kpt.core.base.network.toNetworkConfig
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.serverConfigOf
import kpt.core.network.config.AppAccessPoints

/**
 * The server the app ships pointed at, as the server-config SCREEN describes one — DERIVED from the
 * generated `mifos` access point, never written a second time.
 *
 * `app-profile/app.yaml#network.access_points` is the one place the server is declared. The HTTP
 * client reads it through [AppAccessPoints]; the screen's "Use defaults" and the stored preference's
 * fallback read it through here. Before this, `ServerConfig.DEFAULT` was a hand-kept copy that a parity
 * test had to police — and on 2026-10-01 the two disagreed, every call 404'd, and the screen showed the
 * "right" values throughout.
 *
 * Fork-owned (`owner: fork`), beside the generated [AppAccessPoints]: `ServerConfig` is this app's
 * runtime-server feature, not the template's.
 */
object AppServerConfig {

    /** The access point the field officer signs in to. */
    const val ACCESS_POINT = "mifos"

    /** The header that carries Fineract's tenant on [ACCESS_POINT]. */
    const val TENANT_HEADER = "Fineract-Platform-TenantId"

    /** The shipped server as the screen shows it — the declared main access point's config. */
    val DEFAULT: ServerConfig =
        AppAccessPoints.points.single { it.id == ACCESS_POINT }.toNetworkConfig().toServerConfig()

    /** This app's id in the shared `app_config` instance list's `supportedApps` (see [InstanceConfigLoader]). */
    const val APP_ID = "field-officer"
}

/**
 * A runtime network config as the screen's five fields: its URL split into protocol / host / port /
 * API path, and the tenant from its [AppServerConfig.TENANT_HEADER] value.
 */
fun NetworkConfig.toServerConfig(): ServerConfig = requireNotNull(
    serverConfigOf(
        url = baseUrl,
        tenant = headers.entries
            .firstOrNull { it.key.equals(AppServerConfig.TENANT_HEADER, ignoreCase = true) }
            ?.value
            .orEmpty(),
    ),
) { "network config '$id' base URL '$baseUrl' is not scheme://host[:port][/path]" }

/** The header values a [ServerConfig] sets on its network config — the tenant. */
fun ServerConfig.networkHeaders(): Map<String, String> = mapOf(AppServerConfig.TENANT_HEADER to tenant.trim())
