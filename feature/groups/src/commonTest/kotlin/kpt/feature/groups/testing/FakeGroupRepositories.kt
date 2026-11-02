/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.feature.groups.testing

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.group.GroupCommandRepository
import kpt.core.data.group.GroupRepository
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupPayload
import kpt.core.model.group.GroupWithAssociations
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.responses.SaveResponse

/**
 * In-memory repositories for ViewModel- and screen-level tests. Real [ScreenDataStream]s via the
 * sanctioned `screenDataStreamForTesting` factory, so tests exercise production's own transitions.
 */
internal class FakeGroupRepository(
    initial: ScreenState<List<Group>> = ScreenState.Loading,
) : GroupRepository {
    val state = MutableStateFlow(initial)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    fun emit(vararg groups: Group) {
        // An empty list is Content, as the real stream emits it (see ChargeRepositoryImpl).
        state.value = ScreenState.Content(groups.toList())
    }

    override fun groupsStream(scope: CoroutineScope): ScreenDataStream<List<Group>> =
        screenDataStreamForTesting(state, refreshTrigger = refreshes)

    /** Members / accounts — Loading until a test sets them. */
    val membersState = MutableStateFlow<ScreenState<GroupWithAssociations>>(ScreenState.Loading)
    val accountsState = MutableStateFlow<ScreenState<GroupAccounts>>(ScreenState.Loading)

    override fun groupWithMembersStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupWithAssociations> =
        screenDataStreamForTesting(membersState)

    override fun groupAccountsStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupAccounts> =
        screenDataStreamForTesting(accountsState)
}

/**
 * Records every activation so a test can assert HOW MANY were sent, not just that one was.
 *
 * [gate], when supplied, is awaited BEFORE returning — which is what lets a test hold a mutation
 * "in flight" and tap again. Without it the first call completes before the second action is even
 * dequeued, and a double-tap test would pass against a ViewModel with no guard at all.
 */
internal class FakeGroupCommandRepository(
    private val gate: CompletableDeferred<Unit>? = null,
    private val result: () -> MutationResult<Unit> =
        { MutationResult.Failed(RuntimeException("test"), rolledBack = false) },
) : GroupCommandRepository {

    val activations = mutableListOf<Int>()

    override suspend fun createGroup(groupPayload: GroupPayload?): MutationResult<SaveResponse> =
        error("not used by these tests")

    override suspend fun activateGroup(
        groupId: Int,
        activatePayload: ActivatePayload,
    ): MutationResult<Unit> {
        activations += groupId
        gate?.await()
        return result()
    }
}

internal fun group(
    id: Int,
    name: String? = "Group $id",
    active: Boolean? = true,
    centerId: Int? = null,
    officeName: String? = "Head Office",
    accountNo: String? = "G$id",
) = Group(
    id = id,
    name = name,
    active = active,
    centerId = centerId,
    officeName = officeName,
    accountNo = accountNo,
)
