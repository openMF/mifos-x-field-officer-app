/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.auth

import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.auth.impl.AuthCommandRepositoryImpl
import kpt.core.data.infra.testMutationGateway
import kpt.core.network.mifos.auth.api.AuthApi
import kpt.core.network.mifos.auth.dto.PostAuthenticationRequest
import kpt.core.network.mifos.auth.dto.PostAuthenticationResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * LAYER 3 of the sign-in vertical (core/network → core/data → feature).
 *
 * This layer had NO test. It is the layer that decides the single most consequential property of
 * authentication — that it is `OnlineRequired` and can never be queued — and nothing asserted it.
 * A future edit flipping the policy to `Optimistic` would compile, pass every other test in the
 * repository, and admit users the server never agreed to.
 */
class AuthCommandRepositoryTest {

    private class FakeAuthApi(
        private val response: PostAuthenticationResponse = PostAuthenticationResponse(authenticated = true),
        private val throws: Throwable? = null,
    ) : AuthApi {
        var calls = 0
            private set
        var lastRequest: PostAuthenticationRequest? = null
            private set

        override suspend fun authenticate(
            postAuthenticationRequest: PostAuthenticationRequest,
        ): PostAuthenticationResponse {
            calls++
            lastRequest = postAuthenticationRequest
            throws?.let { throw it }
            return response
        }
    }

    private val credentials = PostAuthenticationRequest(username = "fieldofficer", password = "secret")

    @Test
    fun onlineSuccessAppliesAndIsMarkedSynced() = runTest {
        val api = FakeAuthApi(PostAuthenticationResponse(authenticated = true, username = "fieldofficer"))
        val repo = AuthCommandRepositoryImpl(api, testMutationGateway(isOnline = true))

        val result = repo.authenticate(credentials)

        val applied = assertIs<MutationResult.Applied<PostAuthenticationResponse>>(result)
        assertEquals("fieldofficer", applied.value.username)
        // A credential check has no local leg — it is synced or it did not happen.
        assertTrue(applied.synced)
        assertEquals(1, api.calls)
    }

    @Test
    fun offlineIsBlockedAndTheEndpointIsNeverCalled() = runTest {
        val api = FakeAuthApi()
        val repo = AuthCommandRepositoryImpl(api, testMutationGateway(isOnline = false))

        val result = repo.authenticate(credentials)

        val blocked = assertIs<MutationResult.Blocked>(result)
        assertEquals(BlockReason.OFFLINE, blocked.reason)
        // THE POINT OF THE POLICY: an authentication must never be queued for later replay. A
        // credential check that "succeeds" locally and syncs afterwards would admit a user the
        // server never agreed to. Zero calls proves the gateway short-circuited rather than
        // attempting and failing.
        assertEquals(0, api.calls)
    }

    @Test
    fun aTransportFailureIsFailedNotAppliedWithNull() = runTest {
        val api = FakeAuthApi(throws = IllegalStateException("connection reset"))
        val repo = AuthCommandRepositoryImpl(api, testMutationGateway(isOnline = true))

        val result = repo.authenticate(credentials)

        val failed = assertIs<MutationResult.Failed>(result)
        assertTrue(failed.cause is IllegalStateException)
        // Nothing to roll back — there is no local write to undo.
        assertTrue(!failed.rolledBack)
    }

    @Test
    fun theCredentialReachesTheEndpointUnmodified() = runTest {
        val api = FakeAuthApi()
        val repo = AuthCommandRepositoryImpl(api, testMutationGateway(isOnline = true))

        repo.authenticate(credentials)

        // The repository is a pass-through: no trimming, no case-folding, no re-encoding. A
        // password silently transformed here would fail against a server that accepts it.
        assertEquals("fieldofficer", api.lastRequest?.username)
        assertEquals("secret", api.lastRequest?.password)
    }

    @Test
    fun authenticatedFalseStillReachesTheCallerAsApplied() = runTest {
        // Deliberate division of responsibility: the HTTP call SUCCEEDED, so the repository reports
        // Applied. Deciding that `authenticated == false` means "rejected" belongs to the caller,
        // which is where the user-facing message lives. This test pins that split so nobody
        // "helpfully" converts it to Failed here and leaves the ViewModel's check unreachable.
        val api = FakeAuthApi(PostAuthenticationResponse(authenticated = false))
        val repo = AuthCommandRepositoryImpl(api, testMutationGateway(isOnline = true))

        val applied = assertIs<MutationResult.Applied<PostAuthenticationResponse>>(repo.authenticate(credentials))
        assertEquals(false, applied.value.authenticated)
    }
}
