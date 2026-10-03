/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.config

import kpt.core.model.utils.ServerConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `ServerConfig.DEFAULT` and the shipped `mifos` access point MUST describe the same server.
 *
 * They are two independent declarations of one fact:
 *
 *  - `ServerConfig.DEFAULT` (core/model) is what the server-config SCREEN shows, and what its
 *    "Use defaults" button restores.
 *  - `AppAccessPoints.points["mifos"]` (generated from `app-profile/app.yaml`) is where requests
 *    ACTUALLY go.
 *
 * Nothing structural keeps them in step, and on 2026-10-01 they diverged: the access point declared
 * `fineract-provider/api/v1/` + tenant `default` while `ServerConfig.DEFAULT` declared
 * `/1.0/field/v1/` + `mifos-bank-1`. Every call 404'd, sign-in reported "Login failed. Check your
 * credentials and try again", and the cause took a packet-level probe to find — because the screen
 * showed the right values the whole time.
 *
 * The user-facing consequence of a future divergence is worse than a 404: a field officer who
 * repairs a broken configuration with "Use defaults" would be restored to a server the app cannot
 * reach, with the UI insisting the settings are correct.
 */
class ServerConfigAccessPointParityTest {

    private val mifos = AppAccessPoints.points.single { it.id == "mifos" }

    @Test
    fun defaultServerConfigPointsAtTheShippedAccessPoint() {
        // Origin: ServerConfig splits protocol/host/port; the access point carries one baseUrl.
        val expectedOrigin = "${ServerConfig.DEFAULT.protocol}${ServerConfig.DEFAULT.endPoint}/"
        assertEquals(
            expectedOrigin,
            mifos.baseUrl,
            "ServerConfig.DEFAULT origin and the mifos access point baseUrl have diverged — " +
                "'Use defaults' would restore a server the app does not call",
        )
    }

    @Test
    fun defaultApiPathMatchesTheAccessPointBasePath() {
        // ServerConfig stores the path with leading+trailing slashes ("/1.0/field/v1/"); the access
        // point stores it bare ("1.0/field/v1/"). Compare on the trimmed form so the test asserts
        // the PATH rather than the slash convention.
        assertEquals(
            ServerConfig.DEFAULT.apiPath.trim('/'),
            mifos.basePath.trim('/'),
            "api path diverged — this is the exact 2026-10-01 defect (gateway path vs plain Fineract)",
        )
    }

    @Test
    fun defaultTenantMatchesTheDeclaredTenantHeader() {
        val declaredTenant = mifos.headers
            .firstOrNull { it.name == "Fineract-Platform-TenantId" }
            ?.value
        assertEquals(
            ServerConfig.DEFAULT.tenant,
            declaredTenant,
            "tenant diverged — the screen would show one tenant while requests send another",
        )
    }

    @Test
    fun defaultTenantSatisfiesTheScreensOwnValidationRule() {
        // The ORIGINAL validated tenants against ^[a-zA-Z0-9]+$, which REJECTS `mifos-bank-1` — so
        // the app could not save the configuration it ships with. The rule was widened to allow
        // `-` and `_`; this pins the invariant rather than the regex: whatever the default is, the
        // screen must accept it.
        val tenant = ServerConfig.DEFAULT.tenant
        assertTrue(
            tenant.isNotBlank() && tenant.all { it.isLetterOrDigit() || it == '-' || it == '_' },
            "the shipped default tenant '$tenant' must be accepted by ServerConfigRules.tenant",
        )
    }

    @Test
    fun theEffectiveUrlIsTheOneThatAnswers() {
        // Probed 2026-10-01: this URL + /authentication answers 401 (exists, credential rejected),
        // while the plain-Fineract path answers 404. Pinning the composed value catches a
        // divergence introduced on EITHER side of the join.
        assertEquals("https://apis.mifos.community/1.0/field/v1/", mifos.effectiveUrl)
    }
}
