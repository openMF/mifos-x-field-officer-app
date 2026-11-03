/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.checkerinbox.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.checkerinbox.CheckerInboxRepository
import kpt.core.data.util.freshOrCached
import kpt.core.model.objects.checkerinboxtask.CheckerInboxSearchTemplate
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.model.objects.checkerinboxtask.RescheduleLoansTask
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = CheckerInboxRepository::class)
internal class CheckerInboxRepositoryImpl(
    @FromStore(AppStoreIds.CheckerTasks) private val store: Store<Unit, List<CheckerTask>>,
    @FromStore(AppStoreIds.RescheduleLoanTasks) private val rescheduleStore: Store<Unit, List<RescheduleLoansTask>>,
    @FromStore(AppStoreIds.CheckerSearchTemplate) private val templateStore: Store<Unit, CheckerInboxSearchTemplate>,
) : CheckerInboxRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
    // until something HAS been fetched, so on a clean install nothing ever loads. Room is still
    // served first, so the offline-first behaviour is unchanged. The ttl stays the declared
    // freshness bound — it drives the "updated N ago" indicator, never whether cache is served.
    override fun checkerTasksStream(scope: CoroutineScope): ScreenDataStream<List<CheckerTask>> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.CheckerTasks.LIST,
            scope = scope,
            // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CHECKER_TASKS,
        )

    override fun rescheduleRequestsStream(scope: CoroutineScope): ScreenDataStream<List<RescheduleLoansTask>> =
        rescheduleStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.RescheduleLoanTasks.LIST,
            scope = scope,
            // No `isEmpty`: nothing pending is real content. NETWORK_WITH_CACHE for the reason above.
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.RESCHEDULE_LOAN_TASKS,
        )

    override suspend fun searchTemplate(): CheckerInboxSearchTemplate? = templateStore.freshOrCached(Unit)
}
