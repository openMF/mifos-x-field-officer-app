/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.center.impl

import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.center.CenterCommandRepository
import kpt.core.database.center.entity.CenterPayloadEntity
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.shared.Payload
import kpt.core.network.mifos.center.api.CenterApi
import kpt.core.network.mifos.center.dto.PostCentersCenterIdRequest
import kpt.core.network.mifos.collectionsheet.dto.CollectionSheetPayload

@RepositoryBinding(binds = CenterCommandRepository::class)
internal class CenterCommandRepositoryImpl(
    private val centerApi: CenterApi,
    private val gateway: MutationGateway,
) : CenterCommandRepository {

    override suspend fun activate2(centerId: Long, postCentersCenterIdRequest: PostCentersCenterIdRequest, command: String?) =
        online { centerApi.activate2(centerId = centerId, postCentersCenterIdRequest = postCentersCenterIdRequest, command = command) }

    override suspend fun getCollectionSheet(centerId: Long, payload: Payload?) =
        online { centerApi.getCollectionSheet(centerId = centerId, payload = payload) }

    override suspend fun saveCollectionSheet(centerId: Int, collectionSheetPayload: CollectionSheetPayload?) =
        online { centerApi.saveCollectionSheet(centerId = centerId, collectionSheetPayload = collectionSheetPayload) }

    override suspend fun saveCollectionSheetAsync(centerId: Int, collectionSheetPayload: CollectionSheetPayload?) =
        online { centerApi.saveCollectionSheetAsync(centerId = centerId, collectionSheetPayload = collectionSheetPayload) }

    override suspend fun createCenter(centerPayload: CenterPayloadEntity?) =
        online { centerApi.createCenter(centerPayload = centerPayload) }

    override suspend fun activateCenter(centerId: Int, activatePayload: ActivatePayload?) =
        online { centerApi.activateCenter(centerId = centerId, activatePayload = activatePayload) }

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
