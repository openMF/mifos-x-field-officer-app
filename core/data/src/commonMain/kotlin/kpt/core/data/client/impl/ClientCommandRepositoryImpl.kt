/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.client.impl

import io.ktor.client.request.forms.MultiPartFormDataContent
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.client.ClientCommandRepository
import kpt.core.database.client.entity.ClientPayloadEntity
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.clients.AssignStaffRequest
import kpt.core.model.objects.clients.ClientAddressRequest
import kpt.core.model.objects.clients.ClientCloseRequest
import kpt.core.model.objects.clients.CollateralPayload
import kpt.core.model.objects.clients.ProposeTransferRequest
import kpt.core.model.objects.clients.UpdateSavingsAccountRequest
import kpt.core.model.objects.noncoreobjects.IdentifierPayload
import kpt.core.network.mifos.client.api.ClientApi
import kpt.core.network.mifos.client.api.ClientIdentifierApi
import kpt.core.network.mifos.client.dto.PostClientAddressRequest
import kpt.core.network.mifos.client.dto.PostClientsClientIdRequest

@RepositoryBinding(binds = ClientCommandRepository::class)
internal class ClientCommandRepositoryImpl(
    private val clientApi: ClientApi,
    private val clientIdentifierApi: ClientIdentifierApi,
    private val gateway: MutationGateway,
) : ClientCommandRepository {

    override suspend fun activate1(clientId: Long, postClientsClientIdRequest: PostClientsClientIdRequest, command: String?) =
        online { clientApi.activate1(clientId = clientId, postClientsClientIdRequest = postClientsClientIdRequest, command = command) }

    override suspend fun uploadClientImage(clientId: Int, body: MultiPartFormDataContent) =
        online { clientApi.uploadClientImage(clientId = clientId, body = body) }

    override suspend fun deleteClientImage(clientId: Int) =
        online { clientApi.deleteClientImage(clientId = clientId) }

    override suspend fun createClient(clientPayload: ClientPayloadEntity?) =
        online { clientApi.createClient(clientPayload = clientPayload)!! }

    override suspend fun updateClient(clientId: Int, clientPayload: ClientPayloadEntity?) =
        online { clientApi.updateClient(clientId = clientId, clientPayload = clientPayload)!! }

    override suspend fun addClientPinpointLocation(clientId: Int, clientAddressRequest: ClientAddressRequest?) =
        online { clientApi.addClientPinpointLocation(clientId = clientId, clientAddressRequest = clientAddressRequest) }

    override suspend fun deleteClientPinpointLocation(apptableId: Int, datatableId: Int) =
        online { clientApi.deleteClientPinpointLocation(apptableId = apptableId, datatableId = datatableId) }

    override suspend fun updateClientPinpointLocation(apptableId: Int, datatableId: Int, address: ClientAddressRequest?) =
        online { clientApi.updateClientPinpointLocation(apptableId = apptableId, datatableId = datatableId, address = address) }

    override suspend fun activateClient(clientId: Int, clientActivate: ActivatePayload?) =
        online { clientApi.activateClient(clientId = clientId, clientActivate = clientActivate) }

    override suspend fun createClientAddress(clientId: Int, addressTypeId: Int, addressPayload: PostClientAddressRequest) =
        online { clientApi.createClientAddress(clientId = clientId, addressTypeId = addressTypeId, addressPayload = addressPayload) }

    override suspend fun assignStaff(clientId: Int, payload: AssignStaffRequest) =
        online { clientApi.assignStaff(clientId = clientId, payload = payload) }

    override suspend fun unassignStaff(clientId: Int, payload: AssignStaffRequest) =
        online { clientApi.unassignStaff(clientId = clientId, payload = payload) }

    override suspend fun proposeTransfer(clientId: Int, payload: ProposeTransferRequest) =
        online { clientApi.proposeTransfer(clientId = clientId, payload = payload) }

    override suspend fun updateSavingsAccount(clientId: Int, payload: UpdateSavingsAccountRequest) =
        online { clientApi.updateSavingsAccount(clientId = clientId, payload = payload) }

    override suspend fun closeClient(clientId: Int, payload: ClientCloseRequest) =
        online { clientApi.closeClient(clientId = clientId, payload = payload) }

    override suspend fun createCollateral(clientId: Int, payload: CollateralPayload) =
        online { clientApi.createCollateral(clientId = clientId, payload = payload) }

    override suspend fun deleteClientIdentifier(clientId: Long, identifierId: Long) =
        online { clientIdentifierApi.deleteClientIdentifier(clientId = clientId, identifierId = identifierId) }

    override suspend fun createClientIdentifier(clientId: Long, identifierPayload: IdentifierPayload) =
        online { clientIdentifierApi.createClientIdentifier(clientId = clientId, identifierPayload = identifierPayload) }

    override suspend fun updateClientIdentifier(clientId: Long, identifierId: Long, identifierPayload: IdentifierPayload) =
        online { clientIdentifierApi.updateClientIdentifier(clientId = clientId, identifierId = identifierId, identifierPayload = identifierPayload) }

    /**
     * Commands run [MutationPolicy.OnlineRequired]: they await the server and write nothing
     * offline, yielding `Blocked(OFFLINE)` rather than a local success the officer would act on.
     *
     * The call is closed over rather than threaded through [CommandSpec.payload]: the payload hook
     * exists for `localApply` / `rollback` / `conflictOf`, none of which apply to a write that never
     * lands locally. It also keeps the 28 endpoints whose body is nullable usable, which
     * `CommandSpec<P : Any>` would otherwise reject.
     */
    private suspend fun <R : Any> online(endpoint: suspend () -> R): MutationResult<R> =
        gateway.command(
            CommandSpec(payload = Unit, endpoint = { endpoint() }),
            policy = MutationPolicy.OnlineRequired,
        )
}
