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

import kpt.core.base.network.AccessPointKind
import kpt.core.base.network.NetworkConfig
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.baseUrl
import kpt.core.model.utils.getInstanceUrl
import kpt.core.network.config.AppAccessPoints
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * The server-config screen's default is DERIVED from the access point the HTTP client uses, so the
 * two can no longer describe different servers (on 2026-10-01 two hand-kept copies did: every call
 * 404'd while the screen showed the "right" values). These pin the derivation, and the shipped value.
 */
class AppServerConfigTest {

    private val mifos = AppAccessPoints.points.single { it.id == AppServerConfig.ACCESS_POINT }

    @Test
    fun theScreensDefaultIsTheServerTheClientCalls() {
        // Whatever app-profile names: the screen's URL IS the client's URL.
        assertEquals(mifos.effectiveUrl, AppServerConfig.DEFAULT.getInstanceUrl())
        assertEquals(
            mifos.headers.single { it.name == AppServerConfig.TENANT_HEADER }.value,
            AppServerConfig.DEFAULT.tenant,
        )
    }

    @Test
    fun theShippedServerIsTheFieldOfficerGateway() {
        // Back on the gateway 2026-10-24 (probed live: `1.0/field/v1/authentication` answers
        // Fineract's not-authenticated error). Pinned so a repoint is a deliberate, reviewed change.
        assertEquals(
            ServerConfig("https://", "apis.mifos.community", "/1.0/field/v1/", "443", "mifos-bank-1"),
            AppServerConfig.DEFAULT,
        )
    }

    @Test
    fun theShippedTenantPassesTheScreensOwnRule() {
        // The ORIGINAL validated tenants against ^[a-zA-Z0-9]+$, which REJECTS `mifos-bank-1` — so the
        // app could not save the configuration it ships with. Pins the invariant, not the regex.
        val tenant = AppServerConfig.DEFAULT.tenant
        assertTrue(tenant.isNotBlank() && tenant.all { it.isLetterOrDigit() || it == '-' || it == '_' }, tenant)
    }

    private fun config(baseUrl: String, tenant: String? = null) = NetworkConfig(
        id = "x",
        kind = AccessPointKind.REST,
        baseUrl = baseUrl,
        headers = listOfNotNull(tenant?.let { AppServerConfig.TENANT_HEADER to it }).toMap(),
    )

    @Test
    fun portsAndPathsSplitTheWayTheScreenShowsThem() {
        assertEquals(
            ServerConfig("http://", "localhost", "/fineract-provider/api/v1/", "8080", "default"),
            config("http://localhost:8080/fineract-provider/api/v1/", "default").toServerConfig(),
        )
        // No explicit port → the scheme's; no path → "/"; no tenant header → blank (the screen flags it).
        assertEquals(
            ServerConfig("https://", "h.example", "/", "443", ""),
            config("https://h.example").toServerConfig(),
        )
        assertEquals("80", config("http://h.example/").toServerConfig().port)
    }

    @Test
    fun aConfigRoundTripsThroughTheScreensFields() {
        val other = ServerConfig("https://", "branch.example.org", "/fineract-provider/api/v1/", "8443", "branch-7")
        assertEquals(other, config(other.baseUrl(), "branch-7").toServerConfig())
        assertEquals(mapOf(AppServerConfig.TENANT_HEADER to "branch-7"), other.networkHeaders())
        assertEquals(AppServerConfig.DEFAULT.baseUrl(), AppAccessPoints.points.single { it.isMain }.effectiveUrl)
    }

    @Test
    fun aMalformedUrlIsRefusedNotMisread() {
        assertFailsWith<IllegalArgumentException> { config("ftp://h.example/").toServerConfig() }
    }
}
