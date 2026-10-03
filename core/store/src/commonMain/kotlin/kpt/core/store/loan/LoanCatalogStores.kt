/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.loan

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleTemplate
import kpt.core.model.objects.organisations.LoanProducts
import kpt.core.network.mifos.loan.api.LoanApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Catalogue and template reads for loan — option lists and form templates with no table of their
 * own, cached through the shared read cache so they still render with no signal.
 */

@StoreProvider(id = "loanProducts", ttl = "12h")
@CacheKey(name = "LIST", key = "loanProducts")
fun provideLoanProductsStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Unit, List<LoanProducts>> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.LoanProducts.LIST },
    fetch = { loanApi.getAllLoans() },
)

@StoreProvider(id = "loanRescheduleTemplate", ttl = "12h")
@CacheKey(name = "LIST", key = "loanRescheduleTemplate")
fun provideLoanRescheduleTemplateStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Unit, LoanRescheduleTemplate> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.LoanRescheduleTemplate.LIST },
    fetch = { loanApi.getLoanRescheduleTemplate() },
)
