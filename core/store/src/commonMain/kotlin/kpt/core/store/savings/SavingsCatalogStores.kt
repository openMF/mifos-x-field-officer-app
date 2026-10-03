/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.savings

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.savings.entity.SavingProductsTemplate
import kpt.core.model.objects.organisations.ProductSavings
import kpt.core.network.mifos.savings.api.SavingsAccountApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Catalogue and template reads for savings — option lists and form templates with no table of their
 * own, cached through the shared read cache so they still render with no signal.
 */

@StoreProvider(id = "savingsProducts", ttl = "12h")
@CacheKey(name = "LIST", key = "savingsProducts")
fun provideSavingsProductsStore(
    savingsAccountApi: SavingsAccountApi,
    cache: ApiResponseCacheDao,
): Store<Unit, List<ProductSavings>> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.SavingsProducts.LIST },
    fetch = { savingsAccountApi.allSavingsAccounts() },
)

@StoreProvider(id = "savingsProductTemplate", ttl = "12h")
@CacheKey(name = "LIST", key = "savingsProductTemplate")
fun provideSavingsProductTemplateStore(
    savingsAccountApi: SavingsAccountApi,
    cache: ApiResponseCacheDao,
): Store<Unit, SavingProductsTemplate> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.SavingsProductTemplate.LIST },
    fetch = { savingsAccountApi.savingsAccountTemplate() },
)
