/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.checkerinbox

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.objects.checkerinboxtask.CheckerInboxSearchTemplate
import kpt.core.model.objects.checkerinboxtask.RescheduleLoansTask
import kpt.core.network.mifos.checkerinbox.api.CheckerInboxApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Catalogue and template reads for checkerinbox — option lists and form templates with no table of their
 * own, cached through the shared read cache so they still render with no signal.
 */

@StoreProvider(id = "rescheduleLoanTasks", ttl = "12h")
@CacheKey(name = "LIST", key = "rescheduleLoanTasks")
fun provideRescheduleLoanTasksStore(
    checkerInboxApi: CheckerInboxApi,
    cache: ApiResponseCacheDao,
): Store<Unit, List<RescheduleLoansTask>> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.RescheduleLoanTasks.LIST },
    fetch = { checkerInboxApi.getRescheduleLoansTaskList() },
)

@StoreProvider(id = "checkerSearchTemplate", ttl = "12h")
@CacheKey(name = "LIST", key = "checkerSearchTemplate")
fun provideCheckerSearchTemplateStore(
    checkerInboxApi: CheckerInboxApi,
    cache: ApiResponseCacheDao,
): Store<Unit, CheckerInboxSearchTemplate> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.CheckerSearchTemplate.LIST },
    fetch = { checkerInboxApi.getCheckerInboxSearchTemplate() },
)
