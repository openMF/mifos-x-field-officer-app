/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.auth.AuthCommandRepository
import kpt.core.model.auth.PostAuthenticationRequest
import kpt.core.model.auth.PostAuthenticationResponse

/** Records sign-outs (the clearLocalData flag of each) and password changes; sign-in is not scripted. */
@ExperimentalDataTestingApi
class FakeAuthCommandRepository : AuthCommandRepository {

    /** The `signOut` calls, in order. */
    val signOuts = mutableListOf<Boolean>()

    /** What [signOut] returns — set a `Failed` to model a purge that could not complete. */
    var signOutResult: MutationResult<Unit> = MutationResult.Applied(Unit, synced = true)

    override suspend fun authenticate(
        postAuthenticationRequest: PostAuthenticationRequest,
    ): MutationResult<PostAuthenticationResponse> =
        MutationResult.Failed(IllegalStateException("not scripted"), rolledBack = false)

    override suspend fun resumeStoredSession(): MutationResult<Unit> = MutationResult.Applied(Unit, synced = true)

    override suspend fun signOut(clearLocalData: Boolean): MutationResult<Unit> {
        signOuts += clearLocalData
        return signOutResult
    }

    /** Each `(newPassword, repeatPassword)` asked for. */
    val passwordChanges = mutableListOf<Pair<String, String>>()

    /** What [changePassword] returns. */
    var changePasswordResult: MutationResult<Unit> = MutationResult.Applied(Unit, synced = true)

    override suspend fun changePassword(newPassword: String, repeatPassword: String): MutationResult<Unit> {
        passwordChanges += newPassword to repeatPassword
        return changePasswordResult
    }
}
