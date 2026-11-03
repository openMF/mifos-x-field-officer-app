/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.group

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupWithAssociations

/** Reads groups, their members and accounts, from the cache and the server. */
interface GroupRepository {
    /** Groups the officer services. */
    fun groupsStream(scope: CoroutineScope): ScreenDataStream<List<Group>>

    /** One group with its member clients (`groups/{id}?associations=all`) — the detail's Clients section. */
    fun groupWithMembersStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupWithAssociations>

    /** The group's loan and savings accounts (`groups/{id}/accounts`) — the detail's Accounts section. */
    fun groupAccountsStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupAccounts>
}
