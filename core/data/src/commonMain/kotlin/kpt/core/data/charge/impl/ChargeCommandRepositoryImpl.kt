/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.charge.impl

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.charge.ChargeCommandRepository
import kpt.core.model.charge.Charges
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.payloads.ChargesPayload
import kpt.core.network.mifos.charge.api.ChargeApi
import kpt.core.store.charge.ChargeItemKey
import kpt.core.store.charge.ChargeKey
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/**
 * Charge writes, through the single write door ([MutationGateway.command]).
 *
 * [CommandSpec.localApply] records the server's acceptance through the charge stores, whose rows
 * the charge list observes:
 *  - a DELETE is known exactly, so the row is cleared through the local item store;
 *  - a CREATE or UPDATE lands a row whose name, type and outstanding amount Fineract derives from
 *    the charge definition, so the owner's list is ingested through its read store (`fresh`).
 *
 * One implementation for clients, loans and savings: the endpoint is shared, and the stores key on
 * the owner pair. It used to branch on two owners and record nothing for savings.
 */
@RepositoryBinding(binds = ChargeCommandRepository::class)
internal class ChargeCommandRepositoryImpl(
    private val chargeApi: ChargeApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Charges) private val charges: Store<ChargeKey, List<Charges>>,
    @FromStore(AppStoreIds.ChargeItem) private val chargeItem: MutableStore<ChargeItemKey, Charges>,
) : ChargeCommandRepository {

    override suspend fun createCharge(
        resource: ChargeResource,
        resourceId: Int,
        payload: ChargesPayload,
    ): MutationResult<Unit> = online(
        payload = payload,
        endpoint = {
            chargeApi.createCharges(resourceType = resource.path, resourceId = resourceId, chargesPayload = it)
            Unit
        },
    ) { ingest(resource, resourceId) }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteCharge(resource: ChargeResource, resourceId: Int, chargeId: Int) = online(
        payload = chargeId,
        endpoint = { chargeApi.deleteCharge(resourceType = resource.path, resourceId = resourceId, chargeId = it) },
    ) { chargeItem.clear(ChargeItemKey(resource.path, resourceId, chargeId)) }

    override suspend fun updateCharge(
        resource: ChargeResource,
        resourceId: Int,
        chargeId: Int,
        payload: ChargesPayload,
    ) = online(
        payload = payload,
        endpoint = {
            chargeApi.updateCharge(
                resourceType = resource.path,
                resourceId = resourceId,
                chargeId = chargeId,
                payload = it,
            )
        },
    ) { ingest(resource, resourceId) }

    /**
     * Commands run [MutationPolicy.OnlineRequired]: they await the server and write nothing
     * offline, yielding `Blocked(OFFLINE)` rather than a local success the officer would act on.
     * The gateway runs [localApply] only with the server's result.
     */
    private suspend fun <P : Any, R : Any> online(
        payload: P,
        endpoint: suspend (P) -> R,
        localApply: suspend () -> Unit,
    ): MutationResult<R> = gateway.command(
        CommandSpec(
            payload = payload,
            endpoint = endpoint,
            localApply = { result -> if (result != null) localApply() },
        ),
        policy = MutationPolicy.OnlineRequired,
    )

    /**
     * Ingest the owner's charge list. Runs after the server committed, so a failed re-read is logged,
     * not thrown — escaping, it would report a levied charge as failed and invite a second one. The
     * list's own fetch corrects the row the next time it is opened.
     */
    private suspend fun ingest(resource: ChargeResource, resourceId: Int) {
        try {
            charges.fresh(ChargeKey(resource.path, resourceId))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Logger.w(
                tag = TAG,
                throwable = e,
            ) { "server accepted the charge; ingesting ${resource.path}/$resourceId failed" }
        }
    }

    private companion object {
        const val TAG = "ChargeCommandRepository"
    }
}
