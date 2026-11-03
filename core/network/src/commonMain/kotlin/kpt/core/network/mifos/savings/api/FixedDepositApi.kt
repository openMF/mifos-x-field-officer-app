/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.savings.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.model.savings.FixedDepositPayload
import kpt.core.model.savings.FixedDepositTemplate

/** Fineract endpoints for fixed deposit. */
@ApiBinding("mifos")
interface FixedDepositApi {

    /** Reads the fixed-deposit template for a client. */
    @GET("fixeddepositaccounts/template")
    suspend fun fixedDepositProductTemplate(
        @Query("clientId") clientId: Int,
        @Query("productId") productId: Int?,
    ): FixedDepositTemplate

    /** Opens a fixed-deposit account. */
    @POST("fixeddepositaccounts")
    suspend fun createFixedDepositAccount(
        @Body fixedDepositPayload: FixedDepositPayload,
    ): HttpResponse
}
