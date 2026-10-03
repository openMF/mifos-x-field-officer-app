/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.collectionsheet.impl

import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.collectionsheet.CollectionSheetCommandRepository
import kpt.core.model.collectionsheet.CollectionSheetPayload
import kpt.core.model.collectionsheet.ProductiveCollectionSheetPayload
import kpt.core.model.objects.collectionsheets.CollectionSheetRequestPayload
import kpt.core.network.mifos.collectionsheet.api.CollectionSheetApi
import kpt.core.network.mifos.collectionsheet.dto.IndividualCollectionSheetPayload
import kpt.core.network.mifos.collectionsheet.dto.RequestCollectionSheetPayload

@RepositoryBinding(binds = CollectionSheetCommandRepository::class)
internal class CollectionSheetCommandRepositoryImpl(
    private val collectionSheetApi: CollectionSheetApi,
    private val gateway: MutationGateway,
) : CollectionSheetCommandRepository {

    override suspend fun getIndividualCollectionSheet(payload: RequestCollectionSheetPayload?) =
        online { collectionSheetApi.getIndividualCollectionSheet(payload = payload) }

    override suspend fun saveIndividualCollectionSheet(payload: IndividualCollectionSheetPayload?) =
        online { collectionSheetApi.saveIndividualCollectionSheet(payload = payload) }

    override suspend fun fetchProductiveSheet(centerId: Int, payload: CollectionSheetRequestPayload?) =
        online { collectionSheetApi.fetchProductiveSheet(centerId = centerId, payload = payload) }

    override suspend fun submitProductiveSheet(centerId: Int, payload: ProductiveCollectionSheetPayload?) =
        online { collectionSheetApi.submitProductiveSheet(centerId = centerId, payload = payload) }

    override suspend fun fetchCollectionSheet(groupId: Int, payload: CollectionSheetRequestPayload?) =
        online { collectionSheetApi.fetchCollectionSheet(groupId = groupId, payload = payload) }

    override suspend fun submitCollectionSheet(groupId: Int, payload: CollectionSheetPayload?) =
        online { collectionSheetApi.submitCollectionSheet(groupId = groupId, payload = payload) }

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
