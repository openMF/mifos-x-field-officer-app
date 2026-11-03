/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.group.impl

import kotlinx.coroutines.CancellationException
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.group.GroupCommandRepository
import kpt.core.model.group.Group
import kpt.core.model.group.GroupPayload
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.network.mifos.group.api.GroupApi
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/**
 * Group writes, through the single write door ([MutationGateway.command]).
 *
 * Network → store → database: [CommandSpec.endpoint] is the Fineract call, and
 * [CommandSpec.localApply] records the server's acceptance THROUGH [itemStore] into `GroupTable`,
 * the table the group list and detail read. 6b66e8a43 ended at the endpoint with no local write, so
 * a group activated a moment ago still read as pending on its own detail screen.
 *
 * The new state is the server's — account number, status, activation date — and neither the request
 * nor Fineract's `{resourceId}` reply carries it, so `localApply` ingests the server record read
 * through [groupStore] (network → read cache) once the write is accepted.
 *
 * Both stay `OnlineRequired` (offline capture is `OfflineQueueRepository`, never a fallback here).
 */
@RepositoryBinding(binds = GroupCommandRepository::class)
internal class GroupCommandRepositoryImpl(
    private val groupApi: GroupApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.GroupGetGroup) private val groupStore: Store<Int, Group>,
    @FromStore(AppStoreIds.GroupItem) private val itemStore: MutableStore<Int, Group>,
) : GroupCommandRepository {

    /**
     * Fineract answers with the new group's id only. Should the record read fail, the row is
     * written from what was sent — the group exists on the server and must not vanish locally.
     */
    override suspend fun createGroup(groupPayload: GroupPayload?) =
        gateway.command(
            CommandSpec(
                payload = groupPayload ?: GroupPayload(),
                endpoint = { groupApi.createGroup(groupPayload = groupPayload) },
                localApply = { response ->
                    response?.resourceId?.let { id ->
                        record(id) {
                            Group(
                                id = id,
                                name = groupPayload?.name,
                                officeId = groupPayload?.officeId,
                                active = groupPayload?.active,
                                externalId = groupPayload?.externalId,
                            )
                        }
                    }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    override suspend fun activateGroup(groupId: Int, activatePayload: ActivatePayload): MutationResult<Unit> =
        gateway.command(
            CommandSpec(
                payload = activatePayload,
                endpoint = {
                    groupApi.activateGroup(groupId = groupId, activatePayload = it)
                    Unit
                },
                localApply = { response -> if (response != null) record(groupId) },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    /**
     * Ingest the server's record of [groupId] into `GroupTable`. If reading it fails, write
     * [fallback] instead, or leave the row for the next list refresh: the server has ALREADY
     * accepted the write, and a throw here would report it `Failed` and invite a duplicate.
     */
    private suspend fun record(groupId: Int, fallback: (() -> Group)? = null) {
        val group = orNull { groupStore.fresh(groupId) } ?: fallback?.invoke() ?: return
        itemStore.write(StoreWriteRequest.of<Int, Group, Any>(key = groupId, value = group.copy(id = groupId)))
    }

    private inline fun <T> orNull(block: () -> T): T? = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        null
    }
}
