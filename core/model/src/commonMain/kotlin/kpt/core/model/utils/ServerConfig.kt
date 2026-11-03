/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A server config. */
@Serializable
data class ServerConfig(
    /** `http` or `https`. */
    val protocol: String,
    /** The server's host name or address. */
    @SerialName("end_point")
    val endPoint: String,
    /** The Fineract API path on the server. */
    @SerialName("api_path")
    val apiPath: String,
    /** The TCP port. */
    val port: String,
    /** The Fineract tenant identifier. */
    val tenant: String,
) {
    /** Constants and factories of `ServerConfig`. */
    companion object {
        // No `DEFAULT` here, deliberately. The shipped server is DERIVED from the generated access point
        // (`kpt.core.network.mifos.server.AppServerConfig.DEFAULT`, from `app-profile/app.yaml`) — a constant
        // in this module could only ever be a second copy of it. Read it through
        // `ServerConfigRepository.defaultConfig`.

        /** A server on this machine, for development. */
        val LOCALHOST = ServerConfig(
            protocol = "http://",
            endPoint = "localhost",
            apiPath = "/fineract-provider/api/v1/",
            port = "8080",
            tenant = "default",
        )
    }
}

/** The API base URL: protocol, endpoint and API path, without the port. */
fun ServerConfig.getInstanceUrl(): String {
    return "$protocol$endPoint$apiPath"
}

/**
 * The full URL this config names — the one requests resolve against. The port is written only when it
 * is not the scheme's default, so a config split from a declared URL rebuilds EXACTLY that URL.
 */
fun ServerConfig.baseUrl(): String {
    val defaultPort = if (protocol == "https://") "443" else "80"
    val port = port.trim().takeIf { it.isNotEmpty() && it != defaultPort }?.let { ":$it" }.orEmpty()
    val path = apiPath.trim().trim('/').let { if (it.isEmpty()) "/" else "/$it/" }
    return "$protocol${endPoint.trim()}$port$path"
}

private val URL_PARTS = Regex("^(https?://)([^/:]+)(?::(\\d+))?(/.*)?$")

/**
 * Splits a full server URL (`https://host[:port][/path]`) and a tenant into a [ServerConfig] — the
 * original's Instance URL dialog took exactly these two. Null when [url] is not an http(s) URL.
 */
fun serverConfigOf(url: String, tenant: String): ServerConfig? {
    val parts = URL_PARTS.find(url.trim())?.destructured ?: return null
    val (protocol, host, port) = parts
    val apiPath = parts.component4().trim('/')
    return ServerConfig(
        protocol = protocol,
        endPoint = host,
        apiPath = if (apiPath.isEmpty()) "/" else "/$apiPath/",
        port = port.ifEmpty { if (protocol == "https://") "443" else "80" },
        tenant = tenant.trim(),
    )
}

/**
 * A server the officer picks by name instead of typing five fields. [demoAccounts] is non-empty for a
 * PUBLIC demo instance — the Mifos community publishes those sign-ins, so showing them is the point.
 */
data class ServerInstance(
    /** The server id of this record. */
    val id: String,
    /** The display name. */
    val name: String,
    /** The server's connection settings. */
    val config: ServerConfig,
    val demoAccounts: List<DemoCredentials> = emptyList(),
) {
    /** True for a published demo server. */
    val isDemo: Boolean get() = demoAccounts.isNotEmpty()
}

/**
 * One published sign-in of a demo [ServerInstance] — [role] says what it can do. Never a real account.
 *
 * @property password the password
 * @property username the username
 */
@Serializable
data class DemoCredentials(val role: String, val username: String, val password: String)

/** The instance whose config is exactly [config], or null for a custom server. */
fun List<ServerInstance>.matching(config: ServerConfig): ServerInstance? = firstOrNull { it.config == config }
