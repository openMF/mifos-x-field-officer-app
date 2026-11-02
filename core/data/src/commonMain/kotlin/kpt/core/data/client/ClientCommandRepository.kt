/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.client

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.client.Client
import kpt.core.model.client.ClientPayload
import kpt.core.model.client.PostClientAddressRequest
import kpt.core.model.client.PostClientAddressResponse
import kpt.core.model.client.PostClientsClientIdRequest
import kpt.core.model.client.PostClientsClientIdResponse
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.clients.AssignStaffRequest
import kpt.core.model.objects.clients.ClientAddressRequest
import kpt.core.model.objects.clients.ClientCloseRequest
import kpt.core.model.objects.clients.CollateralPayload
import kpt.core.model.objects.clients.ProposeTransferRequest
import kpt.core.model.objects.clients.UpdateSavingsAccountRequest
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.model.objects.noncoreobjects.IdentifierPayload
import kpt.core.model.shared.GenericResponse
import kpt.core.model.shared.PinpointLocationActionResponse

/**
 * Every client write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * These are commands, so they carry no Store and no cache. Offline CAPTURE is a different
 * operation with its own explicit queue ([kpt.core.data.sync.OfflineQueueRepository]); it is never
 * a silent fallback from here.
 */
interface ClientCommandRepository {
    /*
     * The seven commands below return `MutationResult<Unit>`, not `MutationResult<HttpResponse>`.
     *
     * Their Fineract endpoints are declared `: HttpResponse` because the response bodies differ
     * per endpoint and nothing reads them — so the raw Ktor response was passed straight through
     * this contract to the callers. Three consequences, all bad: `core/data`'s own contract named
     * a Ktor type, every caller could in principle read a consumed body, and the interface was
     * untestable — a fake cannot construct an `HttpResponse` without standing up a MockEngine.
     *
     * The status check these results used to need is already done: `setupDefaultHttpClient` sets
     * `expectSuccess`, so a non-2xx throws and the gateway returns `Failed`. `Applied` therefore
     * already means "the server accepted it", and the response object carries nothing beyond that.
     */

    suspend fun activate1(
        clientId: Long,
        postClientsClientIdRequest: PostClientsClientIdRequest,
        command: String? = null,
    ): MutationResult<PostClientsClientIdResponse>

    /** The photo as bytes + name + MIME type; the multipart encoding is core/data's job, not the caller's. */
    suspend fun uploadClientImage(clientId: Int, image: DocumentUpload): MutationResult<Unit>

    suspend fun deleteClientImage(clientId: Int): MutationResult<Unit>

    suspend fun createClient(clientPayload: ClientPayload?): MutationResult<Client>

    suspend fun updateClient(clientId: Int, clientPayload: ClientPayload?): MutationResult<Client>

    suspend fun addClientPinpointLocation(
        clientId: Int,
        clientAddressRequest: ClientAddressRequest?,
    ): MutationResult<PinpointLocationActionResponse>

    suspend fun deleteClientPinpointLocation(
        apptableId: Int,
        datatableId: Int,
    ): MutationResult<PinpointLocationActionResponse>

    suspend fun updateClientPinpointLocation(
        apptableId: Int,
        datatableId: Int,
        address: ClientAddressRequest?,
    ): MutationResult<PinpointLocationActionResponse>

    suspend fun activateClient(clientId: Int, clientActivate: ActivatePayload?): MutationResult<GenericResponse>

    suspend fun createClientAddress(
        clientId: Int,
        addressTypeId: Int,
        addressPayload: PostClientAddressRequest,
    ): MutationResult<PostClientAddressResponse>

    suspend fun assignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit>

    suspend fun unassignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit>

    suspend fun proposeTransfer(clientId: Int, payload: ProposeTransferRequest): MutationResult<Unit>

    suspend fun updateSavingsAccount(clientId: Int, payload: UpdateSavingsAccountRequest): MutationResult<Unit>

    suspend fun closeClient(clientId: Int, payload: ClientCloseRequest): MutationResult<Unit>

    suspend fun createCollateral(clientId: Int, payload: CollateralPayload): MutationResult<Unit>

    suspend fun deleteClientIdentifier(clientId: Long, identifierId: Long): MutationResult<GenericResponse>

    suspend fun createClientIdentifier(clientId: Long, identifierPayload: IdentifierPayload): MutationResult<Unit>

    suspend fun updateClientIdentifier(
        clientId: Long,
        identifierId: Long,
        identifierPayload: IdentifierPayload,
    ): MutationResult<GenericResponse>
}
