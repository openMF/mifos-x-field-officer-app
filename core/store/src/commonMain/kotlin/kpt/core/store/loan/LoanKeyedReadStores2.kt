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
import kpt.core.database.charge.entity.ChargesEntity
import kpt.core.model.objects.clients.Page
import kpt.core.network.mifos.loan.api.LoanApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for loan with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

@StoreProvider(id = "loanGetListOfLoanCharges", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetListOfLoanCharges:{key}", params = ["key:String"])
fun provideGetListOfLoanChargesStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Int, List<ChargesEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetListOfLoanCharges.forKey(key.toString()) },
    fetch = { key -> loanApi.getListOfLoanCharges(loanId = key) },
)

@StoreProvider(id = "loanGetListOfCharges", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetListOfCharges:{key}", params = ["key:String"])
fun provideGetListOfChargesStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Int, Page<ChargesEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetListOfCharges.forKey(key.toString()) },
    fetch = { key -> loanApi.getListOfCharges(clientId = key) },
)
