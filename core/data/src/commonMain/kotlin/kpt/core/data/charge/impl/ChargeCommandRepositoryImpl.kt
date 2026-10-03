/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.charge.impl

import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.charge.ChargeCommandRepository
import kpt.core.model.objects.payloads.ChargesPayload
import kpt.core.network.mifos.charge.api.ChargeApi

@RepositoryBinding(binds = ChargeCommandRepository::class)
internal class ChargeCommandRepositoryImpl(
    private val chargeApi: ChargeApi,
    private val gateway: MutationGateway,
) : ChargeCommandRepository {

    override suspend fun createCharges(resourceType: String, resourceId: Int, chargesPayload: ChargesPayload) =
        online { chargeApi.createCharges(resourceType = resourceType, resourceId = resourceId, chargesPayload = chargesPayload) }

    override suspend fun deleteCharge(resourceType: String, resourceId: Int, chargeId: Int) =
        online { chargeApi.deleteCharge(resourceType = resourceType, resourceId = resourceId, chargeId = chargeId) }

    override suspend fun updateCharge(resourceType: String, resourceId: Int, chargeId: Int, payload: ChargesPayload) =
        online { chargeApi.updateCharge(resourceType = resourceType, resourceId = resourceId, chargeId = chargeId, payload = payload) }

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
