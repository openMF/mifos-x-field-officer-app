/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.auth.impl.SignOutOnRejectedSession
import kpt.core.model.auth.PostAuthenticationRequest
import kpt.core.model.auth.PostAuthenticationResponse
import kpt.core.model.objects.users.User
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SignOutOnRejectedSessionTest {

    private class RecordingAuth(private val user: () -> User, private val clear: () -> Unit) : AuthCommandRepository {
        val signOuts = mutableListOf<Boolean>()
        override suspend fun authenticate(
            postAuthenticationRequest: PostAuthenticationRequest,
        ): MutationResult<PostAuthenticationResponse> =
            MutationResult.Failed(IllegalStateException(), rolledBack = false)
        override suspend fun resumeStoredSession() = MutationResult.Applied(Unit, synced = true)
        override suspend fun signOut(clearLocalData: Boolean): MutationResult<Unit> {
            signOuts += clearLocalData
            clear()
            return MutationResult.Applied(Unit, synced = true)
        }

        override suspend fun changePassword(
            newPassword: String,
            repeatPassword: String,
        ): MutationResult<Unit> = error("not used here")
    }

    private fun TestScope.handler(signedIn: Boolean): Pair<SignOutOnRejectedSession, RecordingAuth> {
        var user = User(username = "fieldofficer", isAuthenticated = signedIn)
        val auth = RecordingAuth({ user }, { user = User() })
        return SignOutOnRejectedSession(auth = { auth }, session = { user }, scope = this) to auth
    }

    @Test
    fun aRejectedSessionSignsOutOnceAndKeepsLocalData() = runTest {
        val (handler, auth) = handler(signedIn = true)
        // Parallel requests 401 together.
        repeat(3) { handler.onUnauthorized("mifos") }
        runCurrent()
        assertEquals(listOf(false), auth.signOuts, "one sign-out, keeping unsynced work")
    }

    @Test
    fun nothingHappensWhenNoOneIsSignedIn() = runTest {
        val (handler, auth) = handler(signedIn = false)
        handler.onUnauthorized("mifos")
        runCurrent()
        assertEquals(emptyList(), auth.signOuts)
    }
}
