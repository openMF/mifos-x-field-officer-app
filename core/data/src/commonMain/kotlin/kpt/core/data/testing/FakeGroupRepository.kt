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

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.group.GroupRepository
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupWithAssociations

/** Scripted group list; the per-group reads return an empty record for the id asked. */
@ExperimentalDataTestingApi
class FakeGroupRepository(groups: List<Group> = emptyList()) : GroupRepository {

    /** The groups the stream emits. */
    val groups = MutableStateFlow<ScreenState<List<Group>>>(ScreenState.Content(groups))

    override fun groupsStream(scope: CoroutineScope) = screenDataStreamForTesting(groups)

    override fun groupWithMembersStream(groupId: Int, scope: CoroutineScope) =
        screenDataStreamForTesting(
            MutableStateFlow<ScreenState<GroupWithAssociations>>(
                ScreenState.Content(GroupWithAssociations(id = groupId)),
            ),
        )

    override fun groupAccountsStream(groupId: Int, scope: CoroutineScope) =
        screenDataStreamForTesting(MutableStateFlow<ScreenState<GroupAccounts>>(ScreenState.Content(GroupAccounts())))
}
