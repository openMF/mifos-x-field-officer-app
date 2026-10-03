/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.auth.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.network.mifos.auth.dto.PostAuthenticationRequest
import kpt.core.network.mifos.auth.dto.PostAuthenticationResponse

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

    @POST("authentication")
    suspend fun authenticate(
        @Body postAuthenticationRequest: PostAuthenticationRequest,
    ): PostAuthenticationResponse
}
