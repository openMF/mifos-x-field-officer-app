/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.group.impl

import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.group.GroupCommandRepository
import kpt.core.database.group.entity.GroupPayloadEntity
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.network.mifos.group.api.GroupApi

@RepositoryBinding(binds = GroupCommandRepository::class)
internal class GroupCommandRepositoryImpl(
    private val groupApi: GroupApi,
    private val gateway: MutationGateway,
) : GroupCommandRepository {

    override suspend fun createGroup(groupPayload: GroupPayloadEntity?) =
        online { groupApi.createGroup(groupPayload = groupPayload) }

    override suspend fun activateGroup(groupId: Int, activatePayload: ActivatePayload) =
        online { groupApi.activateGroup(groupId = groupId, activatePayload = activatePayload) }

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
