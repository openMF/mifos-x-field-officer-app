/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.dashboard.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.model.dashboard.DashboardLoan
import kpt.core.model.dashboard.DashboardSavings
import kpt.core.model.dashboard.DashboardUser
import kpt.core.model.dashboard.DueTodayLoan
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import kpt.core.store.dashboard.ClientCountKey
import kpt.core.store.dashboard.DueTodayKey
import org.mobilenativefoundation.store.store5.Store

// NETWORK_WITH_CACHE throughout: Room is served first (offline-first), and a fetch always runs behind
// it — the SWR default never fetches on a clean install (see CheckerInboxRepositoryImpl). Empty lists
// are real content: "nothing awaiting approval" is an answer, not a gap.
@RepositoryBinding(binds = DashboardRepository::class)
internal class DashboardRepositoryImpl(
    @FromStore(AppStoreIds.DashboardUser) private val userStore: Store<Long, DashboardUser>,
    @FromStore(AppStoreIds.DashboardBusinessDate) private val businessDateStore: Store<Unit, String>,
    @FromStore(AppStoreIds.DashboardLoans) private val loansStore: Store<Int, List<DashboardLoan>>,
    @FromStore(AppStoreIds.DashboardClientCount) private val clientCountStore: Store<ClientCountKey, Int>,
    @FromStore(AppStoreIds.DashboardSavings) private val savingsStore: Store<Unit, List<DashboardSavings>>,
    @FromStore(AppStoreIds.DashboardDueToday) private val dueTodayStore: Store<DueTodayKey, List<DueTodayLoan>>,
) : DashboardRepository {

    override fun userStream(userId: Long, scope: CoroutineScope): ScreenDataStream<DashboardUser> =
        userStore.asScreenStream(
            key = userId,
            cacheKey = AppCacheKeys.DashboardUser.forKey("$userId"),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.DASHBOARD_USER,
        )

    override fun businessDateStream(scope: CoroutineScope): ScreenDataStream<String> =
        businessDateStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.DashboardBusinessDate.CURRENT,
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.DASHBOARD_BUSINESS_DATE,
        )

    override fun loansStream(status: Int, scope: CoroutineScope): ScreenDataStream<List<DashboardLoan>> =
        loansStore.asScreenStream(
            key = status,
            cacheKey = AppCacheKeys.DashboardLoans.forKey("$status"),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.DASHBOARD_LOANS,
        )

    override fun clientCountStream(officeId: Long, status: String, scope: CoroutineScope): ScreenDataStream<Int> =
        clientCountStore.asScreenStream(
            key = ClientCountKey(officeId, status),
            cacheKey = AppCacheKeys.DashboardClientCount.forKey("$officeId:$status"),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.DASHBOARD_CLIENT_COUNT,
        )

    override fun savingsStream(scope: CoroutineScope): ScreenDataStream<List<DashboardSavings>> =
        savingsStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.DashboardSavings.LIST,
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.DASHBOARD_SAVINGS,
        )

    override fun dueTodayStream(
        date: String,
        loanIds: List<Long>,
        scope: CoroutineScope,
    ): ScreenDataStream<List<DueTodayLoan>> =
        dueTodayStore.asScreenStream(
            key = DueTodayKey(date, loanIds),
            cacheKey = AppCacheKeys.DashboardDueToday.forKey("$date:${loanIds.sorted().hashCode()}"),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.DASHBOARD_DUE_TODAY,
        )
}
