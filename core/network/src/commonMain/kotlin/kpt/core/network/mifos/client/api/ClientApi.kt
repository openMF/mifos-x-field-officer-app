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
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.HttpResponse
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.model.client.Client
import kpt.core.model.client.ClientAccounts
import kpt.core.model.client.ClientPayload
import kpt.core.model.client.ClientsTemplate
import kpt.core.model.client.PostClientsClientIdRequest
import kpt.core.model.client.PostClientsClientIdResponse
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.clients.AssignStaffRequest
import kpt.core.model.objects.clients.ClientCloseRequest
import kpt.core.model.objects.clients.Page
import kpt.core.model.objects.clients.ProposeTransferRequest
import kpt.core.model.objects.clients.UpdateSavingsAccountRequest
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.client.dto.GetClientsClientIdAccountsResponse
import kpt.core.network.mifos.client.dto.GetClientsPageItemsResponse
import kpt.core.network.mifos.client.dto.GetClientsResponse

/** Fineract endpoints for client. */
@ApiBinding("mifos")
interface ClientApi : ClientDetailsApi {

    /**
     * Activate a Client | Close a Client | Reject a Client | Withdraw a Client | Reactivate a Client | UndoReject a
     * Client | UndoWithdraw a Client | Assign a Staff | Unassign a Staff | Update Default Savings Account | Propose a
     * Client Transfer | Withdraw a Client Transfer | Reject a Client Transfer | Accept a Client Transfer | Propose and
     * Accept a Client Transfer
     * Activate a Client:  Clients can be created in a Pending state. This API exists to enable client activation (for
     * when a client becomes an approved member of the financial Institution).  If the client happens to be already
     * active this API will result in an error.  Close a Client:  Clients can be closed if they do not have any
     * non-closed loans/savingsAccount. This API exists to close a client .  If the client have any active
     * loans/savingsAccount this API will result in an error.  Reject a Client:  Clients can be rejected when client is
     * in pending for activation status.  If the client is any other status, this API throws an error.  Mandatory
     * Fields: rejectionDate, rejectionReasonId  Withdraw a Client:  Client applications can be withdrawn when client
     * is in a pending for activation status.  If the client is any other status, this API throws an error.  Mandatory
     * Fields: withdrawalDate, withdrawalReasonId  Reactivate a Client: Clients can be reactivated after they have been
     * closed.  Trying to reactivate a client in any other state throws an error.  Mandatory Fields: reactivationDate
     * UndoReject a Client:  Clients can be reactivated after they have been rejected.  Trying to reactivate a client
     * in any other state throws an error.  Mandatory Fields: reopenedDateUndoWithdraw a Client:  Clients can be
     * reactivated after they have been withdrawn.  Trying to reactivate a client in any other state throws an error.
     * Mandatory Fields: reopenedDate  Assign a Staff:  Allows you to assign a Staff for existed Client.  The selected
     * Staff should belong to the same office (or an officer higher up in the hierarchy) as the Client he manages.
     * Unassign a Staff:  Allows you to unassign the Staff assigned to a Client.  Update Default Savings Account:
     * Allows you to modify or assign a default savings account for an existing Client.  The selected savings account
     * should be one among the existing savings account for a particular customer.  Propose a Client Transfer:  Allows
     * you to propose the transfer of a Client to a different Office.  Withdraw a Client Transfer:  Allows you to
     * withdraw the proposed transfer of a Client to a different Office.  Withdrawal can happen only if the destination
     * Branch (to which the transfer was proposed) has not already accepted the transfer proposal  Reject a Client
     * Transfer:  Allows the Destination Branch to reject the proposed Client Transfer.  Accept a Client Transfer:
     * Allows the Destination Branch to accept the proposed Client Transfer.  The destination branch may also choose to
     * link this client to a group (in which case, any existing active JLG loan of the client is rescheduled to match
     * the meeting frequency of the group) and loan Officer at the time of accepting the transfer  Propose and Accept a
     * Client Transfer:  Abstraction over the Propose and Accept Client Transfer API&#39;s which enable a user with
     * Data Scope over both the Target and Destination Branches to directly transfer a Client to the destination
     * Office.  Showing request/response for &#39;Reject a Client Transfer&#39;
     * Responses:
     *  - 200: OK
     *
     * @param clientId clientId
     * @param postClientsClientIdRequest
     * @param command command (optional)
     * @return [PostClientsClientIdResponse]
     */
    @POST("clients/{clientId}")
    suspend fun activate1(
        @Path("clientId") clientId: Long,
        @Body postClientsClientIdRequest: PostClientsClientIdRequest,
        @Query("command") command: String? = null,
    ): PostClientsClientIdResponse

    /**
     * List Clients
     * The list capability of clients can support pagination and sorting.  Example Requests:  clients
     * clients?fields&#x3D;displayName,officeName,timeline  clients?offset&#x3D;10&amp;limit&#x3D;50
     * clients?orderBy&#x3D;displayName&amp;sortOrder&#x3D;DESC
     * Responses:
     *  - 200: OK
     *
     * @param officeId officeId (optional)
     * @param externalId externalId (optional)
     * @param displayName displayName (optional)
     * @param firstName firstName (optional)
     * @param lastName lastName (optional)
     * @param status status (optional)
     * @param underHierarchy underHierarchy (optional)
     * @param offset offset (optional)
     * @param limit limit (optional)
     * @param orderBy orderBy (optional)
     * @param sortOrder sortOrder (optional)
     * @param orphansOnly orphansOnly (optional)
     * @return [GetClientsResponse]
     */
    @GET("clients")
    suspend fun retrieveAll21(
        @Query("officeId") officeId: Long? = null,
        @Query("externalId") externalId: String? = null,
        @Query("displayName") displayName: String? = null,
        @Query("firstName") firstName: String? = null,
        @Query("lastName") lastName: String? = null,
        @Query("status") status: String? = null,
        @Query("underHierarchy") underHierarchy: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("orderBy") orderBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
        @Query("orphansOnly") orphansOnly: Boolean? = null,
    ): GetClientsResponse

    /**
     * Retrieve client accounts overview
     * An example of how a loan portfolio summary can be provided. This is requested in a specific use case of the
     * community application. It is quite reasonable to add resources like this to simplify User Interface development.
     * Example Requests:   clients/1/accounts  clients/1/accounts?fields&#x3D;loanAccounts,savingsAccounts
     * Responses:
     *  - 200: OK
     *  - 400: Bad Request
     *
     * @param clientId clientId
     * @return [GetClientsClientIdAccountsResponse]
     */
    @GET("clients/{clientId}/accounts")
    suspend fun retrieveAssociatedAccounts(@Path("clientId") clientId: Long): GetClientsClientIdAccountsResponse

    /**
     * @param b      True Enabling the Pagination of the API
     * @param offset Value give from which position Fetch ClientList
     * @param limit  Maximum size of the Client
     * @return List of Clients
     */
    @GET("clients")
    suspend fun getAllClients(
        @Query("paged") b: Boolean,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): Page<Client>

    /** Reads a client. */
    @GET("clients/{clientId}")
    suspend fun getClient(@Path("clientId") clientId: Int): Client

    /** Uploads the client's photo. */
    @POST("clients/{clientId}/images")
    suspend fun uploadClientImage(
        @Path("clientId") clientId: Int,
        @Body body: MultiPartFormDataContent,
    ): Unit

    /** Deletes the client's photo. */
    @DELETE("clients/{clientId}/images")
    suspend fun deleteClientImage(@Path("clientId") clientId: Int)

    /** Downloads the client's photo. */
    @GET("clients/{clientId}/images")
    suspend fun getClientImage(@Path("clientId") clientId: Int): HttpResponse

    /** Creates a client. */
    @POST("clients")
    suspend fun createClient(@Body clientPayload: ClientPayload?): Client?

    /** Updates a client's details. */
    @PUT("clients/{clientId}")
    suspend fun updateClient(
        @Path("clientId") clientId: Int,
        @Body clientPayload: ClientPayload?,
    ): Client?

    /** Reads the new-client template: offices, staff, genders, types and classifications. */
    @GET("clients/template")
    suspend fun getClientTemplate(): ClientsTemplate

    /** Reads the client's accounts. */
    @GET("clients/{clientId}/accounts")
    suspend fun getClientAccounts(@Path("clientId") clientId: Int): ClientAccounts

    /**
     * This is the service to activate the client
     * REST ENT POINT
     * https://demo.openmf.org/fineract-provider/api/v1/clients/{clientId}?command=activate
     *
     * @param clientId
     * @return GenericResponse
     */
    @POST("clients/{clientId}?command=activate")
    suspend fun activateClient(
        @Path("clientId") clientId: Int,
        @Body clientActivate: ActivatePayload?,
    ): GenericResponse

    /** Reads the edit template of a client, with staff from the client's office only. */
    @GET("clients/{clientId}?template=true&staffInSelectedOfficeOnly=true")
    suspend fun getClientTemplate(@Path("clientId") clientId: Int): GetClientsPageItemsResponse

    /** Assigns a loan officer to a client. */
    @POST("clients/{clientId}?command=assignStaff")
    suspend fun assignStaff(
        @Path("clientId") clientId: Int,
        @Body payload: AssignStaffRequest,
    ): HttpResponse

    /** Removes the client's loan officer. */
    @POST("clients/{clientId}?command=unassignStaff")
    suspend fun unassignStaff(
        @Path("clientId") clientId: Int,
        @Body payload: AssignStaffRequest,
    ): HttpResponse

    /** Proposes transferring the client to another office. */
    @POST("clients/{clientId}?command=proposeTransfer")
    suspend fun proposeTransfer(
        @Path("clientId") clientId: Int,
        @Body payload: ProposeTransferRequest,
    ): HttpResponse

    /** Sets the client's default savings account. */
    @POST("clients/{clientId}?command=updateSavingsAccount")
    suspend fun updateSavingsAccount(
        @Path("clientId") clientId: Int,
        @Body payload: UpdateSavingsAccountRequest,
    ): HttpResponse

    /** Closes a client. */
    @POST("clients/{clientId}?command=close")
    suspend fun closeClient(
        @Path("clientId") clientId: Int,
        @Body payload: ClientCloseRequest,
    ): HttpResponse
}
