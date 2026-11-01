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

import kotlinx.coroutines.test.runTest
import kpt.core.model.utils.ServerConfig
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteDemoAccountDto
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteServerInstanceDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InstancesConfigTest {

    @Test
    fun theShippedListLeadsWithTheAppProfileServerAndItsDemoSignIns() = runTest {
        val instances = BundledInstanceConfigLoader.load()
        assertEquals(AppServerConfig.DEFAULT, instances.first().config, "the bundled gateway drifted from app-profile")
        // Verified 2026-10-25 against the live gateway: these five authenticate. `teller` (also on the
        // community wiki) is rejected with 401, so it is not offered.
        assertEquals(
            listOf("fieldofficer", "supervisor", "capturist", "controlofficer", "treasurer"),
            instances.first().demoAccounts.map { it.username },
        )
        for (host in listOf("mifos-bank-2.mifos.community", "sandbox.mifos.community")) {
            assertEquals(
                listOf("mifos"),
                instances.single { it.config.endPoint == host }.demoAccounts.map { it.username },
                host,
            )
        }
        assertEquals(ServerConfig.LOCALHOST, instances.single { it.config.endPoint == "localhost" }.config)
        assertTrue(!instances.single { it.config.endPoint == "localhost" }.isDemo)
    }

    @Test
    fun anInstanceIsOfferedOnlyToTheAppsItNames() {
        // mifos-pay's existing rows name no apps — they must not leak into this one.
        val row = """
            {"instances": [
              {"endpoint": "pay.example.org", "protocol": "https://", "path": "/api/",
               "platformTenantId": "t", "label": "Pay", "interbankServers": []},
              {"endpoint": "both.example.org", "protocol": "https://", "path": "/api/",
               "platformTenantId": "t", "label": "Both", "supportedApps": ["mifos-pay", "field-officer"]},
              {"endpoint": "pay2.example.org", "protocol": "https://", "path": "/api/",
               "platformTenantId": "t", "label": "Pay 2", "supportedApps": ["mifos-pay"]}
            ]}
        """
        assertEquals(listOf("Both"), parseInstances(row).map { it.name })
        assertEquals(listOf("Both", "Pay 2"), parseInstances(row, app = "mifos-pay").map { it.name })
        assertTrue(!parseInstances(row).single().isDemo)
        assertEquals("443", parseInstances(row).single().config.port, "no port = the scheme's default")
    }

    @Test
    fun serverInstanceRowsReadAsTheSameDocumentTheParserReads() {
        val rows = listOf(
            RemoteServerInstanceDto(
                label = "Local",
                protocol = "http://",
                endpoint = "localhost",
                path = "/fineract-provider/api/v1/",
                port = "8080",
                platformTenantId = "default",
                supportedApps = listOf("field-officer"),
            ),
            RemoteServerInstanceDto(
                label = "Gateway",
                protocol = "https://",
                endpoint = "apis.mifos.community",
                path = "/1.0/field/v1/",
                platformTenantId = "mifos-bank-1",
                supportedApps = listOf("field-officer"),
                demoAccounts = listOf(RemoteDemoAccountDto("Field officer", "fieldofficer", "password")),
            ),
        )
        val parsed = parseInstances(instancesConfigJson(rows))
        assertEquals(listOf("Local", "Gateway"), parsed.map { it.name }, "row (sort_order) order is kept")
        assertEquals(ServerConfig.LOCALHOST, parsed.first().config)
        assertEquals(AppServerConfig.DEFAULT, parsed.last().config)
        assertEquals(listOf("fieldofficer"), parsed.last().demoAccounts.map { it.username })
    }
}
