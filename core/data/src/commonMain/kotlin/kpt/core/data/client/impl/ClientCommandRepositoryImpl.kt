/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.client.impl

import kotlinx.coroutines.CancellationException
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.client.ClientCommandRepository
import kpt.core.model.client.Client
import kpt.core.model.client.ClientPayload
import kpt.core.model.client.PostClientAddressRequest
import kpt.core.model.client.PostClientsClientIdRequest
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.clients.AssignStaffRequest
import kpt.core.model.objects.clients.ClientAddressEntity
import kpt.core.model.objects.clients.ClientAddressRequest
import kpt.core.model.objects.clients.ClientAddressResponse
import kpt.core.model.objects.clients.ClientCloseRequest
import kpt.core.model.objects.clients.CollateralPayload
import kpt.core.model.objects.clients.ProposeTransferRequest
import kpt.core.model.objects.clients.UpdateSavingsAccountRequest
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.model.objects.noncoreobjects.Identifier
import kpt.core.model.objects.noncoreobjects.IdentifierPayload
import kpt.core.model.shared.CollateralItemResult
import kpt.core.network.mifos.client.api.ClientApi
import kpt.core.network.mifos.client.api.ClientIdentifierApi
import kpt.core.network.mifos.document.toMultipart
import kpt.core.store.client.IdentifierItemKey
import kpt.core.store.client.PinpointItemKey
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

/**
 * Client writes, through the single write door ([MutationGateway.command]).
 *
 * Network → store → database: [CommandSpec.endpoint] is the Fineract call (`core/network`), and
 * [CommandSpec.localApply] records the server's acceptance THROUGH the client stores (`core/store`,
 * which persist to `core/database`). Every store written here shares its row or table with a read
 * store `ClientRepository` streams, so the screens see the change the moment this returns.
 *
 * It used to end at the endpoint with `payload = Unit` and no local side: a client closed, staffed
 * or re-defaulted seconds earlier still read the old way on every screen until its TTL lapsed.
 *
 * Two shapes of local side:
 * - **The change is known from the request** (unassign staff, default savings account, an edited or
 *   deleted pinpoint, a deleted identifier, a deleted photo): written directly through a local store.
 * - **The new state is computed by the server** (status cascades on activate / close / transfer,
 *   names resolved from ids on assign-staff, addresses, identifiers and collateral, ids the response
 *   does not carry): the real server record is ingested through the entity's own read store
 *   (`fresh` → source-of-truth writer), as the template's "ingest the server record". Guessing it
 *   locally would show a status or name the server never chose.
 *
 * `OnlineRequired`, as before — none of these is queued offline — so `localApply` runs only with the
 * server's result and an offline write is `Blocked` with nothing written.
 */
@RepositoryBinding(binds = ClientCommandRepository::class)
internal class ClientCommandRepositoryImpl(
    private val clientApi: ClientApi,
    private val clientIdentifierApi: ClientIdentifierApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.ClientGetClient) private val clientStore: Store<Int, Client>,
    @FromStore(AppStoreIds.ClientLocal) private val clientLocalStore: MutableStore<Int, Client>,
    @FromStore(AppStoreIds.ClientRow) private val clientRowStore: MutableStore<Int, Client>,
    @FromStore(AppStoreIds.ClientImage) private val imageStore: Store<Int, String>,
    @FromStore(AppStoreIds.ClientGetClientPinpointLocations)
    private val pinpointStore: Store<Int, List<ClientAddressResponse>>,
    @FromStore(AppStoreIds.ClientPinpointItem)
    private val pinpointItemStore: MutableStore<PinpointItemKey, ClientAddressResponse>,
    @FromStore(AppStoreIds.ClientGetClientAddresses)
    private val addressesStore: Store<Int, List<ClientAddressEntity>>,
    @FromStore(AppStoreIds.ClientGetClientCollateralItems)
    private val collateralStore: Store<Int, List<CollateralItemResult>>,
    @FromStore(AppStoreIds.ClientGetClientListIdentifiers)
    private val identifiersStore: Store<Long, List<Identifier>>,
    @FromStore(AppStoreIds.ClientIdentifierItem)
    private val identifierItemStore: MutableStore<IdentifierItemKey, Identifier>,
) : ClientCommandRepository {

    override suspend fun activate1(
        clientId: Long,
        postClientsClientIdRequest: PostClientsClientIdRequest,
        command: String?,
    ) =
        online(
            payload = postClientsClientIdRequest,
            endpoint = { clientApi.activate1(clientId = clientId, postClientsClientIdRequest = it, command = command) },
            localApply = { ingestClient(clientId.toInt()) },
        )

    // The stored data URL comes back from the server, so the image store is re-read after the upload.
    override suspend fun uploadClientImage(clientId: Int, image: DocumentUpload) =
        online(
            payload = image,
            endpoint = { clientApi.uploadClientImage(clientId = clientId, body = it.toMultipart()) },
            localApply = { ingest { imageStore.fresh(clientId) } },
        )

    // Through the READ store's own clear: it drops the in-memory copy as well as the row, so the
    // next `clientImage` cannot answer with the photo that was just deleted.
    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteClientImage(clientId: Int) =
        online(
            payload = clientId,
            endpoint = { clientApi.deleteClientImage(clientId = it) },
            localApply = { imageStore.clear(clientId) },
        )

    // `clientId` is null on a maker-checker tenant: the client is pending approval and readable by
    // no id yet, so there is nothing to record.
    override suspend fun createClient(clientPayload: ClientPayload?) =
        online(
            payload = Created,
            endpoint = { clientApi.createClient(clientPayload = clientPayload)!! },
            localApply = { response -> response.clientId?.let { ingestClient(it) } },
        )

    override suspend fun updateClient(clientId: Int, clientPayload: ClientPayload?) =
        online(
            payload = clientId,
            endpoint = { clientApi.updateClient(clientId = it, clientPayload = clientPayload)!! },
            localApply = { ingestClient(clientId) },
        )

    // The new datatable row's id is not in the response (`resourceId` is the client's).
    override suspend fun addClientPinpointLocation(clientId: Int, clientAddressRequest: ClientAddressRequest?) =
        online(
            payload = clientId,
            endpoint = {
                clientApi.addClientPinpointLocation(
                    clientId = it,
                    clientAddressRequest = clientAddressRequest,
                )
            },
            localApply = { ingest { pinpointStore.fresh(clientId) } },
        )

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteClientPinpointLocation(apptableId: Int, datatableId: Int) =
        online(
            payload = PinpointItemKey(clientId = apptableId, datatableId = datatableId),
            endpoint = {
                clientApi.deleteClientPinpointLocation(
                    apptableId = it.clientId,
                    datatableId = it.datatableId,
                )
            },
            localApply = { pinpointItemStore.clear(PinpointItemKey(apptableId, datatableId)) },
        )

    override suspend fun updateClientPinpointLocation(
        apptableId: Int,
        datatableId: Int,
        address: ClientAddressRequest?,
    ) =
        online(
            payload = PinpointItemKey(clientId = apptableId, datatableId = datatableId),
            endpoint = {
                clientApi.updateClientPinpointLocation(
                    apptableId = it.clientId,
                    datatableId = it.datatableId,
                    address = address,
                )
            },
            localApply = {
                if (address != null) {
                    pinpointItemStore.write(
                        StoreWriteRequest.of<PinpointItemKey, ClientAddressResponse, Any>(
                            key = PinpointItemKey(apptableId, datatableId),
                            value = ClientAddressResponse(
                                id = datatableId,
                                clientId = apptableId,
                                latitude = address.latitude,
                                longitude = address.longitude,
                                placeAddress = address.placeAddress,
                                placeId = address.placeId,
                            ),
                        ),
                    )
                }
            },
        )

    override suspend fun activateClient(clientId: Int, clientActivate: ActivatePayload?) =
        online(
            payload = clientId,
            endpoint = { clientApi.activateClient(clientId = it, clientActivate = clientActivate) },
            localApply = { ingestClient(clientId) },
        )

    // The address row carries names the server resolves from the ids sent (type, state, country).
    override suspend fun createClientAddress(
        clientId: Int,
        addressTypeId: Int,
        addressPayload: PostClientAddressRequest,
    ) =
        online(
            payload = addressPayload,
            endpoint = {
                clientApi.createClientAddress(
                    clientId = clientId,
                    addressTypeId = addressTypeId,
                    addressPayload = it,
                )
            },
            localApply = { ingest { addressesStore.fresh(clientId) } },
        )

    // `staffName` is the server's, resolved from the id sent.
    override suspend fun assignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit> =
        online(
            payload = payload,
            endpoint = {
                clientApi.assignStaff(clientId = clientId, payload = it)
                Unit
            },
            localApply = { ingestClient(clientId) },
        )

    override suspend fun unassignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit> =
        online(
            payload = payload,
            endpoint = {
                clientApi.unassignStaff(clientId = clientId, payload = it)
                Unit
            },
            localApply = { patchClient(clientId) { it.copy(staffId = 0, staffName = null) } },
        )

    override suspend fun proposeTransfer(clientId: Int, payload: ProposeTransferRequest): MutationResult<Unit> =
        online(
            payload = payload,
            endpoint = {
                clientApi.proposeTransfer(clientId = clientId, payload = it)
                Unit
            },
            localApply = { ingestClient(clientId) },
        )

    override suspend fun updateSavingsAccount(
        clientId: Int,
        payload: UpdateSavingsAccountRequest,
    ): MutationResult<Unit> =
        online(
            payload = payload,
            endpoint = {
                clientApi.updateSavingsAccount(clientId = clientId, payload = it)
                Unit
            },
            localApply = { patchClient(clientId) { it.copy(savingsAccountId = payload.savingsAccountId) } },
        )

    override suspend fun closeClient(clientId: Int, payload: ClientCloseRequest): MutationResult<Unit> =
        online(
            payload = payload,
            endpoint = {
                clientApi.closeClient(clientId = clientId, payload = it)
                Unit
            },
            localApply = { ingestClient(clientId) },
        )

    // The pledged row's name and pricing come from the tenant's catalog, server-side.
    override suspend fun createCollateral(clientId: Int, payload: CollateralPayload): MutationResult<Unit> =
        online(
            payload = payload,
            endpoint = {
                clientApi.createCollateral(clientId = clientId, payload = it)
                Unit
            },
            localApply = { ingest { collateralStore.fresh(clientId) } },
        )

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteClientIdentifier(clientId: Long, identifierId: Long) =
        online(
            payload = IdentifierItemKey(clientId = clientId, identifierId = identifierId),
            endpoint = {
                clientIdentifierApi.deleteClientIdentifier(
                    clientId = it.clientId,
                    identifierId = it.identifierId,
                )
            },
            localApply = { identifierItemStore.clear(IdentifierItemKey(clientId, identifierId)) },
        )

    // The row's document-type NAME and status label are the server's, resolved from the ids sent.
    override suspend fun createClientIdentifier(
        clientId: Long,
        identifierPayload: IdentifierPayload,
    ): MutationResult<Unit> =
        online(
            payload = identifierPayload,
            endpoint = {
                clientIdentifierApi.createClientIdentifier(clientId = clientId, identifierPayload = it)
                Unit
            },
            localApply = { ingest { identifiersStore.fresh(clientId) } },
        )

    override suspend fun updateClientIdentifier(
        clientId: Long,
        identifierId: Long,
        identifierPayload: IdentifierPayload,
    ) =
        online(
            payload = identifierPayload,
            endpoint = {
                clientIdentifierApi.updateClientIdentifier(
                    clientId = clientId,
                    identifierId = identifierId,
                    identifierPayload = it,
                )
            },
            localApply = { ingest { identifiersStore.fresh(clientId) } },
        )

    /**
     * The client's server record, into BOTH tables a client is read from: `fresh` writes the detail
     * row through `clientGetClient`'s source of truth, and the roster row is written through
     * `clientRow` (the roster is its own `Client` table, not the detail cache).
     */
    private suspend fun ingestClient(clientId: Int) = ingest {
        val client = clientStore.fresh(clientId)
        clientRowStore.write(StoreWriteRequest.of<Int, Client, Any>(key = clientId, value = client))
    }

    /** A change known from the request, applied to the client wherever it is read from. */
    private suspend fun patchClient(clientId: Int, change: (Client) -> Client) = ingest {
        val client = change(clientStore.get(clientId))
        clientLocalStore.write(StoreWriteRequest.of<Int, Client, Any>(key = clientId, value = client))
        clientRowStore.write(StoreWriteRequest.of<Int, Client, Any>(key = clientId, value = client))
    }

    /**
     * Recording the outcome must not turn an accepted write into `Failed`. The gateway reports any
     * `localApply` throw as a failure, and here the server HAS applied the command — an officer told
     * "failed" retries it, and a second close or activate is refused by Fineract with an error about
     * state they never saw change. A record that cannot be read back now is re-read by the next
     * stream refresh.
     */
    private suspend inline fun ingest(block: () -> Unit) {
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Left for the next refresh — see above.
        }
    }

    /**
     * `OnlineRequired`: awaits the server and writes nothing offline, yielding `Blocked(OFFLINE)`
     * rather than a local success the officer would act on. [localApply] gets the server's result
     * only once it accepted the write.
     */
    private suspend fun <P : Any, R : Any> online(
        payload: P,
        endpoint: suspend (P) -> R,
        localApply: suspend (R) -> Unit,
    ): MutationResult<R> =
        gateway.command(
            CommandSpec(
                payload = payload,
                endpoint = endpoint,
                localApply = { result -> if (result != null) localApply(result) },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    /** Payload marker for a create, whose body may be null and whose key does not exist yet. */
    private object Created
}
