/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.charge.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.database.charge.entity.ChargesEntity
import kpt.core.model.objects.clients.ChargeCreationResponse
import kpt.core.model.objects.clients.Page
import kpt.core.model.objects.payloads.ChargesPayload
import kpt.core.model.objects.template.client.ChargeTemplate

/**
 * Service interface that defines all API endpoints related to charges.
 *
 * This includes fetching charge templates, listing charges for different resource types
 * (clients, loans, savings accounts, etc.), creating, updating, and deleting charges.
 *
 * Each endpoint is compatible with Ktorfit and uses Flow-based or suspend-based calls
 * depending on whether continuous or single responses are expected.
 *
 * @author Arin Yadav
 */
@ApiBinding("mifos")
interface ChargeApi {

    /**
     * Retrieves a list of all available charges in the system.
     *
     * **Endpoint:** `GET /charges`
     *
     * @return [Flow] emitting an [HttpResponse] containing the list of charges.
     */
    @GET("charges")
    suspend fun listAllCharges(): HttpResponse

    /**
     * Retrieves a charge template for a specific resource type and ID.
     *
     * **Endpoint:** `GET /{resourceType}/{resourceId}/charges/template`
     *
     * @param resourceType The type of resource (e.g. `"clients"`, `"loans"`, `"savings"`).
     * @param resourceId The unique ID of the resource.
     * @return A [ChargeTemplate] describing available charges and configuration fields.
     */
    @GET("{resourceType}/{resourceId}/charges/template")
    suspend fun getChargeTemplate(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Int,
    ): ChargeTemplate

    /**
     * Retrieves a paginated list of charges for a given resource type and ID.
     *
     * **Endpoint:** `GET /{resourceType}/{resourceId}/charges`
     *
     * @param resourceType The type of resource (e.g. `"clients"`, `"loans"`, `"savings"`).
     * @param resourceId The unique ID of the resource.
     * @param offset The page offset (zero-based index).
     * @param limit The maximum number of items to return.
     * @return [Flow] emitting a [Page] of [ChargesEntity] objects.
     */
    @GET("{resourceType}/{resourceId}/charges")
    suspend fun getListOfPagingCharges(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): Page<ChargesEntity>

    /**
     * Retrieves all client-specific charges.
     *
     * **Endpoint:** `GET /{resourceType}/{resourceId}/charges?offset=0&limit=0`
     *
     * @param resourceType The resource type (e.g. `"clients"`).
     * @param resourceId The client ID.
     * @return [Flow] emitting a [Page] of [ChargesEntity] objects.
     */
    @GET("{resourceType}/{resourceId}/charges?offset=0&limit=0")
    suspend fun getListOfClientCharges(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Int,
    ): Page<ChargesEntity>

    /**
     * Retrieves charges for other account types (e.g. loans, savings).
     *
     * **Endpoint:** `GET /{resourceType}/{resourceId}/charges`
     *
     * @param resourceType The resource type (e.g. `"loans"`, `"savings"`).
     * @param resourceId The account ID.
     * @return [Flow] emitting a [List] of [ChargesEntity].
     */
    @GET("{resourceType}/{resourceId}/charges")
    suspend fun getListOfOtherAccountCharge(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Int,
    ): List<ChargesEntity>

    /**
     * Retrieves a single charge by its ID.
     *
     * **Endpoint:** `GET /{resourceType}/{resourceId}/charges/{chargeId}`
     *
     * @param resourceType The resource type (e.g. `"clients"`, `"loans"`, `"savings"`).
     * @param resourceId The ID of the resource.
     * @param chargeId The ID of the charge.
     * @return [Flow] emitting a [ChargesEntity] representing the charge.
     */
    @GET("{resourceType}/{resourceId}/charges/{chargeId}")
    suspend fun getCharge(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Int,
        @Path("chargeId") chargeId: Int,
    ): ChargesEntity

    /**
     * Creates a new charge for the given resource type and ID.
     *
     * **Endpoint:** `POST /{resourceType}/{resourceId}/charges`
     *
     * @param resourceType The type of resource (e.g. `"clients"`, `"loans"`, `"savings"`).
     * @param resourceId The resource ID.
     * @param chargesPayload The [ChargesPayload] containing the charge creation data.
     * @return [ChargeCreationResponse] containing the created charge information.
     */
    @POST("{resourceType}/{resourceId}/charges")
    suspend fun createCharges(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Int,
        @Body chargesPayload: ChargesPayload,
    ): HttpResponse

    /**
     * Deletes an existing charge.
     *
     * **Endpoint:** `DELETE /{resourceType}/{resourceId}/charges/{chargeId}`
     *
     * @param resourceType The type of resource.
     * @param resourceId The resource ID.
     * @param chargeId The charge ID to delete.
     */
    @DELETE("{resourceType}/{resourceId}/charges/{chargeId}")
    suspend fun deleteCharge(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Int,
        @Path("chargeId") chargeId: Int,
    )

    /**
     * Updates an existing charge.
     *
     * **Endpoint:** `PUT /{resourceType}/{resourceId}/charges/{chargeId}`
     *
     * @param resourceType The type of resource.
     * @param resourceId The resource ID.
     * @param chargeId The charge ID to update.
     */
    @PUT("{resourceType}/{resourceId}/charges/{chargeId}")
    suspend fun updateCharge(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Int,
        @Path("chargeId") chargeId: Int,
        @Body payload: ChargesPayload,
    )
}
