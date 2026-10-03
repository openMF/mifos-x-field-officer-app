/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.client

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.HttpResponse
import kpt.core.base.store.mutation.MutationResult
import kpt.core.database.client.entity.ClientEntity
import kpt.core.database.client.entity.ClientPayloadEntity
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.clients.AssignStaffRequest
import kpt.core.model.objects.clients.ClientAddressRequest
import kpt.core.model.objects.clients.ClientCloseRequest
import kpt.core.model.objects.clients.CollateralPayload
import kpt.core.model.objects.clients.ProposeTransferRequest
import kpt.core.model.objects.clients.UpdateSavingsAccountRequest
import kpt.core.model.objects.noncoreobjects.IdentifierPayload
import kpt.core.model.shared.GenericResponse
import kpt.core.model.shared.PinpointLocationActionResponse
import kpt.core.network.mifos.client.dto.PostClientAddressRequest
import kpt.core.network.mifos.client.dto.PostClientAddressResponse
import kpt.core.network.mifos.client.dto.PostClientsClientIdRequest
import kpt.core.network.mifos.client.dto.PostClientsClientIdResponse

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

    suspend fun activate1(clientId: Long, postClientsClientIdRequest: PostClientsClientIdRequest, command: String? = null): MutationResult<PostClientsClientIdResponse>

    suspend fun uploadClientImage(clientId: Int, body: MultiPartFormDataContent): MutationResult<Unit>

    suspend fun deleteClientImage(clientId: Int): MutationResult<Unit>

    suspend fun createClient(clientPayload: ClientPayloadEntity?): MutationResult<ClientEntity>

    suspend fun updateClient(clientId: Int, clientPayload: ClientPayloadEntity?): MutationResult<ClientEntity>

    suspend fun addClientPinpointLocation(clientId: Int, clientAddressRequest: ClientAddressRequest?): MutationResult<PinpointLocationActionResponse>

    suspend fun deleteClientPinpointLocation(apptableId: Int, datatableId: Int): MutationResult<PinpointLocationActionResponse>

    suspend fun updateClientPinpointLocation(apptableId: Int, datatableId: Int, address: ClientAddressRequest?): MutationResult<PinpointLocationActionResponse>

    suspend fun activateClient(clientId: Int, clientActivate: ActivatePayload?): MutationResult<GenericResponse>

    suspend fun createClientAddress(clientId: Int, addressTypeId: Int, addressPayload: PostClientAddressRequest): MutationResult<PostClientAddressResponse>

    suspend fun assignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<HttpResponse>

    suspend fun unassignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<HttpResponse>

    suspend fun proposeTransfer(clientId: Int, payload: ProposeTransferRequest): MutationResult<HttpResponse>

    suspend fun updateSavingsAccount(clientId: Int, payload: UpdateSavingsAccountRequest): MutationResult<HttpResponse>

    suspend fun closeClient(clientId: Int, payload: ClientCloseRequest): MutationResult<HttpResponse>

    suspend fun createCollateral(clientId: Int, payload: CollateralPayload): MutationResult<HttpResponse>

    suspend fun deleteClientIdentifier(clientId: Long, identifierId: Long): MutationResult<GenericResponse>

    suspend fun createClientIdentifier(clientId: Long, identifierPayload: IdentifierPayload): MutationResult<HttpResponse>

    suspend fun updateClientIdentifier(clientId: Long, identifierId: Long, identifierPayload: IdentifierPayload): MutationResult<GenericResponse>
}
