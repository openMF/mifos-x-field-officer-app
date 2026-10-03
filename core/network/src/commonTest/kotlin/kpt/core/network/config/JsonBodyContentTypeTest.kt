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

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kpt.core.base.network.AccessPointKind
import kpt.core.base.network.RuntimeHeaderStore
import kpt.core.base.network.setupDefaultHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every Fineract call declares JSON — so a typed `@Body` is serialized rather than refused.
 *
 * The headers come from `app-profile/app.yaml#network.access_points[].headers[]` (→ generated into
 * [AppAccessPoints]) rather than from the HTTP client: being a JSON API is a property of THIS
 * endpoint, not of every client the app builds. This test reads the SHIPPED declaration, so deleting
 * the row in app-profile fails here.
 *
 * What it locks, found on device 2026-10-01: `ContentNegotiation` only converts a body whose request
 * declares a Content-Type it has a converter for, and a Ktorfit `@Body` parameter sets none. With no
 * declared Content-Type, Ktor threw before opening a socket —
 *
 * ```
 * IllegalStateException: Fail to prepare request body for sending.
 * The body type is: …PostAuthenticationRequest, with Content-Type: null.
 * ```
 *
 * — which the sign-in screen rendered as "Login failed. Check your credentials and try again", with
 * nothing in the HTTP log because no request was ever made.
 */
class JsonBodyContentTypeTest {

    @Serializable
    private data class Credentials(val username: String, val password: String)

    private val mifos = AppAccessPoints.points.single { it.id == "mifos" }

    @Test
    fun theMifosAccessPointDeclaresJsonOnBothSides() {
        val declared = mifos.headers.mapNotNull { spec -> spec.value?.let { spec.name to it } }.toMap()
        assertEquals("application/json", declared[HttpHeaders.ContentType])
        assertEquals("application/json", declared[HttpHeaders.Accept])
        assertEquals(AccessPointKind.REST, mifos.kind)
    }

    @Test
    fun aTypedBodyIsSerializedRatherThanRejected() = runTest {
        val seen = mutableListOf<String?>()
        val client = HttpClient(
            MockEngine { request ->
                seen += request.headers[HttpHeaders.ContentType] ?: request.body.contentType?.toString()
                respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        ) {
            setupDefaultHttpClient(
                baseUrl = mifos.effectiveUrl,
                // Exactly how `ktorfitFor` wires the point's declared headers in production.
                dynamicHeaders = { RuntimeHeaderStore().resolve(mifos.headers) },
            )()
        }

        // The call itself is the assertion: with no declared Content-Type this throws inside Ktor
        // before the engine is reached.
        client.post("authentication") {
            setBody(Credentials(username = "fieldofficer", password = "unused-by-the-mock"))
        }

        assertTrue(
            seen.single()?.startsWith("application/json") == true,
            "expected a JSON content type on the request, got ${seen.single()}",
        )
    }
}
