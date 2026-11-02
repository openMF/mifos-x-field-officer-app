/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.client.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse
import kpt.core.model.client.AddressConfiguration
import kpt.core.model.client.AddressTemplate
import kpt.core.model.client.ClientCloseTemplateResponse
import kpt.core.model.client.PostClientAddressRequest
import kpt.core.model.client.PostClientAddressResponse
import kpt.core.model.objects.clients.ClientAddressEntity
import kpt.core.model.objects.clients.ClientAddressRequest
import kpt.core.model.objects.clients.ClientAddressResponse
import kpt.core.model.objects.clients.CollateralPayload
import kpt.core.model.shared.CollateralItem
import kpt.core.model.shared.CollateralItemResult
import kpt.core.model.shared.PinpointLocationActionResponse

/** Pinpoint locations, addresses and collateral of a client — split from [ClientApi], which extends it. */
interface ClientDetailsApi {

    /**
     * This is the service for fetching the client pinpoint locations from the dataTable
     * "client_pinpoint_location". This DataTable entries are
     * 1. Place Id
     * 2. Place Address
     * 3. latitude
     * 4. longitude
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/datatables/client_pinpoint_location
     * /{appTableId}
     *
     * @param clientId Client Id
     * @return ClientAddressResponse
     */
    @GET("datatables/client_pinpoint_location/{clientId}")
    suspend fun getClientPinpointLocations(
        @Path("clientId") clientId: Int,
    ): List<ClientAddressResponse>

    /**
     * This is the service for adding the new Client Pinpoint Location in dataTable
     * "client_pinpoint_location".
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/datatables/client_pinpoint_location
     * /{appTableId}
     *
     * @param clientId             Client Id
     * @param clientAddressRequest ClientAddress
     * @return GenericResponse
     */
    @POST("datatables/client_pinpoint_location/{clientId}")
    suspend fun addClientPinpointLocation(
        @Path("clientId") clientId: Int,
        @Body clientAddressRequest: ClientAddressRequest?,
    ): PinpointLocationActionResponse

    /**
     * This is the service for deleting the pinpoint location from the DataTable
     * "client_pinpoint_location".
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/datatables/client_pinpoint_location
     * /{appTableId}/{datatableId}
     *
     * @param apptableId
     * @param datatableId
     * @return GenericResponse
     */
    @DELETE("datatables/client_pinpoint_location/{apptableId}/{datatableId}")
    suspend fun deleteClientPinpointLocation(
        @Path("apptableId") apptableId: Int,
        @Path("datatableId") datatableId: Int,
    ): PinpointLocationActionResponse

    /**
     * This is the service for updating the pinpoint location from DataTable
     * "client_pinpoint_location"
     * REST ENT POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/datatables/client_pinpoint_location
     * /{appTableId}/{datatableId}
     *
     * @param apptableId
     * @param datatableId
     * @param address     Client Address
     * @return GenericResponse
     */
    @PUT("datatables/client_pinpoint_location/{apptableId}/{datatableId}")
    suspend fun updateClientPinpointLocation(
        @Path("apptableId") apptableId: Int,
        @Path("datatableId") datatableId: Int,
        @Body address: ClientAddressRequest?,
    ): PinpointLocationActionResponse

    /**Add commentMore actions
     * Retrieves address configuration from Global Configuration.
     *
     * @return The AddressConfiguration object
     */
    @GET("configurations/name/enable-address")
    suspend fun getAddressConfiguration(): AddressConfiguration

    /**
     * Retrieves an address template.
     * This template can be used to pre-fill address forms or guide users in providing address information.
     *
     * @return An [AddressTemplate] object containing the structure for an address.
     */
    @GET("client/addresses/template")
    suspend fun getAddressTemplate(): AddressTemplate

    @GET("client/{clientId}/addresses")
    suspend fun getClientAddresses(@Path("clientId") clientId: Int): List<ClientAddressEntity>

    @POST("client/{clientId}/addresses?type={addressTypeId}")
    suspend fun createClientAddress(
        @Path("clientId") clientId: Int,
        @Path("addressTypeId") addressTypeId: Int,
        @Body addressPayload: PostClientAddressRequest,
    ): PostClientAddressResponse

    @GET("clients/template?commandParam=close")
    suspend fun getClientCloseTemplate(): ClientCloseTemplateResponse

    @GET("collateral-management")
    suspend fun getCollateralItems(): List<CollateralItem>

    @GET("clients/{clientId}/collaterals/template")
    suspend fun getClientCollateralItems(@Path("clientId") clientId: Int): List<CollateralItemResult>

    @POST("clients/{clientId}/collaterals")
    suspend fun createCollateral(
        @Path("clientId") clientId: Int,
        @Body payload: CollateralPayload,
    ): HttpResponse
}
