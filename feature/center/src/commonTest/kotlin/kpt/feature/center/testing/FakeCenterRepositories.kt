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

package kpt.feature.center.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.center.CenterRepository
import kpt.core.data.group.GroupRepository
import kpt.core.model.center.Center
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupWithAssociations
import kpt.core.model.objects.databaseobjects.CollectionSheet
import kpt.core.model.objects.groups.CenterInfo
import kpt.core.model.shared.Payload

/**
 * In-memory repositories for ViewModel- and screen-level tests.
 *
 * Both hand back REAL [ScreenDataStream]s via the sanctioned `screenDataStreamForTesting` factory
 * rather than a parallel fake of the state machine — so a test exercises the same Loading /
 * Content / Empty / Error transitions production does. Mirrors `FakeNoteRepository`.
 */
internal class FakeCenterRepository(
    initial: ScreenState<List<Center>> = ScreenState.Loading,
) : CenterRepository {
    val state = MutableStateFlow(initial)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    fun emit(vararg centers: Center) {
        // An empty list is Content, as the real stream emits it (see ChargeRepositoryImpl).
        state.value = ScreenState.Content(centers.toList())
    }

    override fun centersStream(scope: CoroutineScope): ScreenDataStream<List<Center>> =
        screenDataStreamForTesting(state, refreshTrigger = refreshes)

    override suspend fun collectionSheet(centerId: Long, payload: Payload): CollectionSheet? = null

    val meetingState = MutableStateFlow<ScreenState<CenterWithAssociations>>(ScreenState.Loading)
    val summaryState = MutableStateFlow<ScreenState<List<CenterInfo>>>(ScreenState.Loading)

    override fun centerWithMeetingStream(
        centerId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<CenterWithAssociations> =
        screenDataStreamForTesting(meetingState)

    override fun centerSummaryStream(centerId: Int, scope: CoroutineScope): ScreenDataStream<List<CenterInfo>> =
        screenDataStreamForTesting(summaryState)
}

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

internal fun center(
    id: Int,
    name: String? = "Center $id",
    accountNo: String? = "C$id",
    officeName: String? = "Head Office",
    active: Boolean? = true,
) = Center(
    id = id,
    name = name,
    accountNo = accountNo,
    officeName = officeName,
    active = active,
)

internal fun group(
    id: Int,
    centerId: Int?,
    name: String? = "Group $id",
) = Group(id = id, centerId = centerId, name = name, accountNo = "G$id")
