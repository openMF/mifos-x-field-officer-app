/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.auth.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.model.auth.ChangePasswordRequest
import kpt.core.model.auth.PostAuthenticationRequest
import kpt.core.model.auth.PostAuthenticationResponse

/**
 * Fineract authentication.
 *
 * `POST /authentication` previously lived on `ClientService` and was reached through
 * `DataManagerAuth` — an auth endpoint hanging off the client RESOURCE api, which is why the
 * `auth` context had DTOs but no API of its own. It belongs here: the credential exchange is not
 * a client operation, and the response seeds the Authorization header every other api then sends.
 *
 * The response carries the token; `app-profile` declares `auth: basic` on the `mifos` access point
 * with `Authorization` as a runtime header, so the login flow writes it to the runtime store and
 * the transport attaches it from there.
 */
@ApiBinding("mifos")
interface AuthApi {

    /** Signs in with a username and password; the response carries the user, their roles and the Basic-auth key. */
    @POST("authentication")
    suspend fun authenticate(
        @Body postAuthenticationRequest: PostAuthenticationRequest,
    ): PostAuthenticationResponse

    /**
     * The signed-in user's own password. A validation refusal (policy, mismatch) is a 400 whose
     * `errors[]` carry the reasons; the success body (a command result) is not read.
     */
    @POST("users/{userId}/pwd")
    suspend fun changePassword(
        @Path("userId") userId: Long,
        @Body request: ChangePasswordRequest,
    ): Unit
}
