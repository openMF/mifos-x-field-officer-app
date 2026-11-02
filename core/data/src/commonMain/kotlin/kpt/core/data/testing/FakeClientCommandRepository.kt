/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import kotlinx.coroutines.CompletableDeferred
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.client.ClientCommandRepository
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
 * Test double for [ClientCommandRepository], owned by the module that owns the CONTRACT.
 *
 * ## Why it lives in `core/data`, and in commonMain
 *
 * `ClientCommandRepository` is a `core/data` interface and its 19 method signatures name
 * `core/network` DTOs. Faking it inside a feature module therefore drags `core:network` onto that
 * feature's test classpath — a feature talking to network DTOs, which inverts the layering. The
 * fake belongs with the interface.
 *
 * `commonMain` rather than `commonTest` because Gradle does not share a module's test source set
 * with its consumers, and this codebase already has the answer: `screenDataStreamForTesting` sits
 * in `core-base/store`'s commonMain behind [kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi]
 * for exactly this reason — "so feature modules opt-in explicitly, signalling test-only intent in
 * the call site". [ExperimentalDataTestingApi] is the same mechanism.
 *
 * ## Unimplemented methods THROW
 *
 * Only what a test exercises is implemented; everything else errors. A fake that quietly returns
 * success turns a miswired call into a passing test, which is the opposite of what a fake is for.
 *
 * [gate], when supplied, is awaited BEFORE returning — the only way to hold a mutation in flight
 * and exercise an in-flight guard, since otherwise the first call completes before a second action
 * is dequeued.
 */
@ExperimentalDataTestingApi
class FakeClientCommandRepository(
    private val gate: CompletableDeferred<Unit>? = null,
    /**
     * Result for the seven `MutationResult<Unit>` commands (assign/unassign staff, propose
     * transfer, update default savings, close client, create collateral, create identifier).
     *
     * They became fakeable only when the contract stopped returning Ktor's `HttpResponse`: a test
     * cannot construct one without standing up a MockEngine, so all seven were `error(...)` here
     * and no S3c write path could be tested at all.
     *
     * Declared BEFORE [result] deliberately. Several call sites pass [result] as a trailing lambda
     * (`FakeClientCommandRepository { Blocked(OFFLINE) }`); adding a parameter after it silently
     * rebinds every one of them to the new parameter, and the test then exercises the default
     * result and passes for the wrong reason. Caught here because one such test failed loudly —
     * it would not have if the two results happened to agree.
     */
    private val unitResult: () -> MutationResult<Unit> = { MutationResult.Applied(Unit, synced = true) },
    /**
     * Result for the two commands that return the CLIENT they wrote (create, update).
     *
     * Declared BEFORE [result] for the reason [unitResult]'s KDoc gives: [result] is passed as a
     * trailing lambda at several call sites, and a parameter added after it would silently capture
     * them.
     */
    private val clientResult: () -> MutationResult<Client> =
        { MutationResult.Applied(Client(), synced = true) },
    private val result: () -> MutationResult<GenericResponse> =
        { MutationResult.Applied(GenericResponse(), synced = true) },
) : ClientCommandRepository {

    /** (clientId, identifierId) per delete — a test asserts HOW MANY were sent, not just that one was. */
    val deletedIdentifiers = mutableListOf<Pair<Long, Long>>()

    override suspend fun deleteClientIdentifier(
        clientId: Long,
        identifierId: Long,
    ): MutationResult<GenericResponse> {
        deletedIdentifiers += clientId to identifierId
        gate?.await()
        return result()
    }

    override suspend fun activate1(
        clientId: Long,
        postClientsClientIdRequest: PostClientsClientIdRequest,
        command: String?,
    ): MutationResult<PostClientsClientIdResponse> =
        error("activate1 is not faked — add it when a test needs it")

    /** Client ids whose photo was uploaded, in order, and the image each upload carried. */
    val uploadedImages = mutableListOf<Int>()
    val uploadedImageFiles = mutableListOf<DocumentUpload>()

    override suspend fun uploadClientImage(clientId: Int, image: DocumentUpload): MutationResult<Unit> {
        uploadedImages += clientId
        uploadedImageFiles += image
        gate?.await()
        return unitResult()
    }

    /** Client ids whose photo was deleted, in order. */
    val deletedImages = mutableListOf<Int>()

    override suspend fun deleteClientImage(clientId: Int): MutationResult<Unit> {
        deletedImages += clientId
        gate?.await()
        return unitResult()
    }

    /** Payloads passed to create, in order — a test asserts WHAT was sent, not merely that it was. */
    val createdClients = mutableListOf<ClientPayload?>()

    override suspend fun createClient(clientPayload: ClientPayload?): MutationResult<Client> {
        createdClients += clientPayload
        gate?.await()
        return clientResult()
    }

    /** (clientId, payload) per update — the pair matters: an edit form must target the client it read. */
    val updatedClients = mutableListOf<Pair<Int, ClientPayload?>>()

    override suspend fun updateClient(clientId: Int, clientPayload: ClientPayload?): MutationResult<Client> {
        updatedClients += clientId to clientPayload
        gate?.await()
        return clientResult()
    }

    override suspend fun addClientPinpointLocation(
        clientId: Int,
        clientAddressRequest: ClientAddressRequest?,
    ): MutationResult<PinpointLocationActionResponse> =
        error("addClientPinpointLocation is not faked — add it when a test needs it")

    override suspend fun deleteClientPinpointLocation(
        apptableId: Int,
        datatableId: Int,
    ): MutationResult<PinpointLocationActionResponse> =
        error("deleteClientPinpointLocation is not faked — add it when a test needs it")

    override suspend fun updateClientPinpointLocation(
        apptableId: Int,
        datatableId: Int,
        address: ClientAddressRequest?,
    ): MutationResult<PinpointLocationActionResponse> =
        error("updateClientPinpointLocation is not faked — add it when a test needs it")

    /** (clientId, payload) per activation — the shared activate screen's client branch. */
    val activatedClients: MutableList<Pair<Int, ActivatePayload?>> = mutableListOf()

    override suspend fun activateClient(
        clientId: Int,
        clientActivate: ActivatePayload?,
    ): MutationResult<GenericResponse> {
        activatedClients += clientId to clientActivate
        gate?.await()
        return result()
    }

    override suspend fun createClientAddress(
        clientId: Int,
        addressTypeId: Int,
        addressPayload: PostClientAddressRequest,
    ): MutationResult<PostClientAddressResponse> =
        error("createClientAddress is not faked — add it when a test needs it")

    /** (clientId, staffId) per assign — a test asserts HOW MANY were sent, not just that one was. */
    val assignedStaff: MutableList<Pair<Int, Int>> = mutableListOf()

    override suspend fun assignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit> {
        assignedStaff += clientId to payload.staffId
        gate?.await()
        return unitResult()
    }

    /** (clientId, staffId) per unassign. */
    val unassignedStaff: MutableList<Pair<Int, Int>> = mutableListOf()

    override suspend fun unassignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit> {
        unassignedStaff += clientId to payload.staffId
        gate?.await()
        return unitResult()
    }

    /** (clientId, payload) per proposed transfer. */
    val proposedTransfers: MutableList<Pair<Int, ProposeTransferRequest>> = mutableListOf()

    override suspend fun proposeTransfer(clientId: Int, payload: ProposeTransferRequest): MutationResult<Unit> {
        proposedTransfers += clientId to payload
        gate?.await()
        return unitResult()
    }

    /** (clientId, payload) per default-savings update. */
    val updatedSavingsAccounts: MutableList<Pair<Int, UpdateSavingsAccountRequest>> = mutableListOf()

    override suspend fun updateSavingsAccount(
        clientId: Int,
        payload: UpdateSavingsAccountRequest,
    ): MutationResult<Unit> {
        updatedSavingsAccounts += clientId to payload
        gate?.await()
        return unitResult()
    }

    /** (clientId, payload) per closure. */
    val closedClients: MutableList<Pair<Int, ClientCloseRequest>> = mutableListOf()

    override suspend fun closeClient(clientId: Int, payload: ClientCloseRequest): MutationResult<Unit> {
        closedClients += clientId to payload
        gate?.await()
        return unitResult()
    }

    /** (clientId, payload) per collateral create. */
    val createdCollateral: MutableList<Pair<Int, CollateralPayload>> = mutableListOf()

    override suspend fun createCollateral(clientId: Int, payload: CollateralPayload): MutationResult<Unit> {
        createdCollateral += clientId to payload
        gate?.await()
        return unitResult()
    }

    /** (clientId, payload) per identifier create. */
    val createdIdentifiers: MutableList<Pair<Long, IdentifierPayload>> = mutableListOf()

    override suspend fun createClientIdentifier(
        clientId: Long,
        identifierPayload: IdentifierPayload,
    ): MutationResult<Unit> {
        createdIdentifiers += clientId to identifierPayload
        gate?.await()
        return unitResult()
    }

    override suspend fun updateClientIdentifier(
        clientId: Long,
        identifierId: Long,
        identifierPayload: IdentifierPayload,
    ): MutationResult<GenericResponse> =
        error("updateClientIdentifier is not faked — add it when a test needs it")
}
