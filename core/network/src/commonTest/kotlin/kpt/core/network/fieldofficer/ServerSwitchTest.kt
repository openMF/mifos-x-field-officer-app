/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.fieldofficer

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.NetworkConfigRepository
import kpt.core.base.network.restClientConfig
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.baseUrl
import kpt.core.network.config.AppAccessPoints
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The officer's server choice (settings → Instance URL / Server config) reaching requests — on the
 * client configuration `ktorfitFor` actually builds (`restClientConfig`), against this app's real
 * access points. Before 2026-10-25 it reached nothing: requests always went to the build-time server.
 */
class ServerSwitchTest {

    private val mifos = AppAccessPoints.points.single { it.id == AppServerConfig.ACCESS_POINT }

    @Test
    fun anEditedMainConfigChangesTheNextRequestsHostAndTenant() = runTest {
        val configs = NetworkConfigRepository(AccessPointRegistry(AppAccessPoints.points))
        val seen = mutableListOf<Pair<String, String?>>()
        val client = HttpClient(
            MockEngine { r ->
                seen += r.url.toString() to r.headers[AppServerConfig.TENANT_HEADER]
                respond("[]", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
            },
            restClientConfig(mifos, networkConfigs = configs),
        )

        client.get("offices")
        assertEquals(
            "https://apis.mifos.community/1.0/field/v1/offices" to "mifos-bank-1",
            seen.last(),
            "the default is unchanged",
        )

        val other = ServerConfig("https://", "branch.example.org", "/fineract-provider/api/v1/", "8443", "branch-7")
        configs.update("mifos", baseUrl = other.baseUrl(), headers = other.networkHeaders())
        client.get("offices")
        assertEquals("https://branch.example.org:8443/fineract-provider/api/v1/offices" to "branch-7", seen.last())

        configs.reset("mifos")
        client.get("offices")
        assertEquals(
            "https://apis.mifos.community/1.0/field/v1/offices" to "mifos-bank-1",
            seen.last(),
            "reset returns to app-profile",
        )
    }
}
