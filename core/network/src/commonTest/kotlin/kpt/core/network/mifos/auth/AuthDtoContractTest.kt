/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.auth

import kotlinx.serialization.json.Json
import kpt.core.network.mifos.auth.dto.PostAuthenticationRequest
import kpt.core.network.mifos.auth.dto.PostAuthenticationResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * LAYER 1 of the sign-in vertical (core/network → core/data → feature).
 *
 * The DTOs had no test. That matters more here than it looks: this endpoint is the ONLY one whose
 * failure mode is a 200, so a serialization mistake does not surface as an error — it surfaces as a
 * user who cannot sign in with correct credentials, which is indistinguishable from a wrong
 * password.
 *
 * The JSON below is the REAL response captured from `apis.mifos.community` on 2026-10-02 (values
 * shortened), not an invented fixture.
 */
class AuthDtoContractTest {

    /** The client's own config — `ignoreUnknownKeys` + `explicitNulls=false` are load-bearing. */
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun theRealServerResponseDeserializes() {
        // Captured live. `roles[]` and a 600-entry `permissions[]` are present on every real
        // response; a DTO that chokes on them breaks sign-in for every user.
        val wire = """
            {"username":"fieldofficer","userId":32,
             "base64EncodedAuthenticationKey":"ZmllbGRvZmZpY2Vy",
             "authenticated":true,"officeId":1,"officeName":"CENTRAL",
             "roles":[{"id":12,"name":"OFICIAL DE CAMPO","description":"OFICIAL DE CAMPO","disabled":false}],
             "permissions":["READ_CLIENT","UPDATE_CLIENT"]}
        """.trimIndent()

        val res = json.decodeFromString<PostAuthenticationResponse>(wire)

        assertEquals("fieldofficer", res.username)
        assertEquals(32L, res.userId)
        assertEquals(true, res.authenticated)
        assertEquals("CENTRAL", res.officeName)
        assertEquals("ZmllbGRvZmZpY2Vy", res.base64EncodedAuthenticationKey)
        assertEquals(2, res.permissions?.size)
        assertEquals(1, res.roles?.size)
    }

    @Test
    fun authenticatedFalseSurvivesAsFalseNotAsNull() {
        // Fineract answers HTTP 200 with authenticated=false for a BAD credential. If this field
        // deserialized to null the ViewModel's `authenticated != true` check would still reject —
        // but for the wrong reason, and a future refactor to `== false` would silently admit a
        // failed login. Pin the value, not the truthiness.
        val res = json.decodeFromString<PostAuthenticationResponse>(
            """{"username":"x","authenticated":false}""",
        )
        assertEquals(false, res.authenticated)
    }

    @Test
    fun anUnknownServerFieldDoesNotBreakSignIn() {
        // Fineract adds fields across versions. Without ignoreUnknownKeys a single new key would
        // fail deserialization and lock every user out of an app that was working yesterday.
        val res = json.decodeFromString<PostAuthenticationResponse>(
            """{"username":"x","authenticated":true,"someFutureFineractField":{"a":1}}""",
        )
        assertEquals(true, res.authenticated)
        assertEquals("x", res.username)
    }

    @Test
    fun anErrorBodyDoesNotMasqueradeAsASuccessfulLogin() {
        // THE IMPORTANT ONE. The shared client does not set expectSuccess, so a non-2xx body is
        // deserialized onto this DTO's defaults. A 401 error payload therefore parses "fine" —
        // every field null. The guard is that `authenticated` is NOT true, which is exactly what
        // the ViewModel checks. If anyone ever gives `authenticated` a default of `true`, sign-in
        // silently accepts failures.
        val errorBody = """{"timestamp":"2026-10-01T18:38:51Z","status":401,"error":"Unauthorized","path":"/clients"}"""

        val res = json.decodeFromString<PostAuthenticationResponse>(errorBody)

        assertTrue(res.authenticated != true, "an error body must never read as an authenticated session")
        assertNull(res.base64EncodedAuthenticationKey)
        assertNull(res.username)
    }

    @Test
    fun theRequestSerializesTheTwoFieldsTheGatewayExpects() {
        val body = json.encodeToString(
            PostAuthenticationRequest.serializer(),
            PostAuthenticationRequest(username = "fieldofficer", password = "secret"),
        )
        // The gateway rejects anything else; order is irrelevant but presence is not.
        assertTrue(body.contains("\"username\":\"fieldofficer\""), body)
        assertTrue(body.contains("\"password\":\"secret\""), body)
    }
}
