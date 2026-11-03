/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.dashboard.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.network.mifos.dashboard.dto.AppUserDto
import kpt.core.network.mifos.dashboard.dto.BatchRequestDto
import kpt.core.network.mifos.dashboard.dto.BatchResponseDto
import kpt.core.network.mifos.dashboard.dto.BusinessDateDto
import kpt.core.network.mifos.dashboard.dto.LoanListPageDto
import kpt.core.network.mifos.dashboard.dto.SavingsListPageDto

/**
 * Reads behind the home dashboard that no other feature needed. Every figure is computed from these,
 * never from a report.
 */
@ApiBinding("mifos")
interface DashboardApi {

    /** A user may always read their own record — it carries the staff link that scopes the dashboard. */
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: Long): AppUserDto

    /** The date every "today" on the dashboard means — never the device clock. */
    @GET("businessdate/BUSINESS_DATE")
    suspend fun getBusinessDate(): BusinessDateDto

    /** Loans by status (100 pending approval, 200 approved, 300 active). No officer filter exists on this endpoint. */
    @GET("loans")
    suspend fun getLoans(
        @Query("status") status: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("orderBy") orderBy: String = "id",
        @Query("sortOrder") sortOrder: String = "DESC",
    ): LoanListPageDto

    /**
     * Fineract's batch API: many reads in ONE round-trip, each answered independently
     * (`enclosingTransaction=false`). Used for the loans' repayment schedules behind Today's collections
     * when the role may not read the collection sheet — 60 schedules took 34 s as single requests
     * (OkHttp's 5-per-host queue × ~1.3 s gateway latency) and 4.4 s as one batch (measured 2026-10-27).
     */
    @POST("batches")
    suspend fun batch(
        @Body requests: List<BatchRequestDto>,
        @Query("enclosingTransaction") enclosingTransaction: Boolean = false,
    ): List<BatchResponseDto>

    /** Reads one page of savings accounts. */
    @GET("savingsaccounts")
    suspend fun getSavingsAccounts(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): SavingsListPageDto
}
