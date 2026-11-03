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

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.group.GroupRepository
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupWithAssociations
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = GroupRepository::class)
internal class GroupRepositoryImpl(
    @FromStore(AppStoreIds.Groups) private val store: Store<Unit, List<Group>>,
    @FromStore(AppStoreIds.GroupGetGroupWithAssociations)
    private val withMembersStore: Store<Int, GroupWithAssociations>,
    @FromStore(AppStoreIds.GroupGetGroupAccounts) private val accountsStore: Store<Int, GroupAccounts>,
) : GroupRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
    // until something HAS been fetched, so on a clean install nothing ever loads. Room is still
    // served first, so the offline-first behaviour is unchanged. The ttl stays the declared
    // freshness bound — it drives the "updated N ago" indicator, never whether cache is served.
    override fun groupsStream(scope: CoroutineScope): ScreenDataStream<List<Group>> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.Groups.LIST,
            scope = scope,
            // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.GROUPS,
        )

    // Both keyed by group id over the shared read cache: offline after one visit (or after the group's
    // offline download, which fills the same entries), refreshed on open.
    override fun groupWithMembersStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupWithAssociations> =
        withMembersStore.asScreenStream(
            key = groupId,
            cacheKey = AppCacheKeys.GroupGetGroupWithAssociations.forKey(groupId.toString()),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.GROUP_GET_GROUP_WITH_ASSOCIATIONS,
        )

    override fun groupAccountsStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupAccounts> =
        accountsStore.asScreenStream(
            key = groupId,
            cacheKey = AppCacheKeys.GroupGetGroupAccounts.forKey(groupId.toString()),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.GROUP_GET_GROUP_ACCOUNTS,
        )
}
