/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.savings

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.savings.FixedDepositTemplate
import kpt.core.model.savings.SavingsAccountTransactionTemplate
import kpt.core.model.savings.ShareTemplate
import kpt.core.network.mifos.savings.api.FixedDepositApi
import kpt.core.network.mifos.savings.api.SavingsAccountApi
import kpt.core.network.mifos.savings.api.ShareAccountApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store
import kpt.core.model.savings.SavingProductsTemplate as SavingProductsTemplateModel

/**
 * Keyed reads for savings with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `fixedDepositProductTemplate` read. */
data class FixedDepositProductTemplateKey(
    /** The client's server id. */
    val clientId: Int,
    /** The product's server id. */
    val productId: Int?,
)

/** Addresses one `getClientSavingsAccountTemplateByProduct` read. */
data class GetClientSavingsAccountTemplateByProductKey(
    /** The client's server id. */
    val clientId: Int,
    /** The product's server id. */
    val productId: Int,
)

/** Addresses one `getGroupSavingsAccountTemplateByProduct` read. */
data class GetGroupSavingsAccountTemplateByProductKey(
    /** The group's server id. */
    val groupId: Int,
    /** The product's server id. */
    val productId: Int,
)

/** Addresses one `shareProductTemplate` read. */
data class ShareProductTemplateKey(
    /** The client's server id. */
    val clientId: Int,
    /** The product's server id. */
    val productId: Int?,
)

/** Store reading the fixed deposit product template. */
@StoreProvider(id = "savingsFixedDepositProductTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "savingsFixedDepositProductTemplate:{key}", params = ["key:String"])
fun provideFixedDepositProductTemplateStore(
    fixedDepositApi: FixedDepositApi,
    cache: ApiResponseCacheDao,
): Store<FixedDepositProductTemplateKey, FixedDepositTemplate> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.SavingsFixedDepositProductTemplate.forKey("${key.clientId}:${key.productId}") },
    fetch = { key -> fixedDepositApi.fixedDepositProductTemplate(clientId = key.clientId, productId = key.productId) },
)

/** Store reading the client savings account template by product. */
@StoreProvider(id = "savingsGetClientSavingsAccountTemplateByProduct", ttl = "12h")
@CacheKey(fn = "forKey", key = "savingsGetClientSavingsAccountTemplateByProduct:{key}", params = ["key:String"])
fun provideGetClientSavingsAccountTemplateByProductStore(
    savingsAccountApi: SavingsAccountApi,
    cache: ApiResponseCacheDao,
): Store<GetClientSavingsAccountTemplateByProductKey, SavingProductsTemplateModel> = cachedRead(
    dao = cache,
    keyOf = { key ->
        AppCacheKeys.SavingsGetClientSavingsAccountTemplateByProduct.forKey("${key.clientId}:${key.productId}")
    },
    fetch = { key ->
        savingsAccountApi.getClientSavingsAccountTemplateByProduct(
            clientId = key.clientId,
            productId = key.productId,
        )
    },
)

/** Store reading the group savings account template by product. */
@StoreProvider(id = "savingsGetGroupSavingsAccountTemplateByProduct", ttl = "12h")
@CacheKey(fn = "forKey", key = "savingsGetGroupSavingsAccountTemplateByProduct:{key}", params = ["key:String"])
fun provideGetGroupSavingsAccountTemplateByProductStore(
    savingsAccountApi: SavingsAccountApi,
    cache: ApiResponseCacheDao,
): Store<GetGroupSavingsAccountTemplateByProductKey, SavingProductsTemplateModel> = cachedRead(
    dao = cache,
    keyOf = { key ->
        AppCacheKeys.SavingsGetGroupSavingsAccountTemplateByProduct.forKey("${key.groupId}:${key.productId}")
    },
    fetch = { key ->
        savingsAccountApi.getGroupSavingsAccountTemplateByProduct(
            groupId = key.groupId,
            productId = key.productId,
        )
    },
)

/** Store reading the share product template. */
@StoreProvider(id = "savingsShareProductTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "savingsShareProductTemplate:{key}", params = ["key:String"])
fun provideShareProductTemplateStore(
    shareAccountApi: ShareAccountApi,
    cache: ApiResponseCacheDao,
): Store<ShareProductTemplateKey, ShareTemplate> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.SavingsShareProductTemplate.forKey("${key.clientId}:${key.productId}") },
    fetch = { key -> shareAccountApi.shareProductTemplate(clientId = key.clientId, productId = key.productId) },
)

/** Addresses one `getSavingsAccountTransactionTemplate` read. */
data class GetSavingsAccountTransactionTemplateKey(
    /** The savings account kind (`savingsaccounts`, `recurringdepositaccounts`, …). */
    val savingsAccountType: String,
    /** The savings account's server id. */
    val savingsAccountId: Int,
    /** The transaction type (`deposit`, `withdrawal`), or null for the generic template. */
    val transactionType: String?,
)

/** Store reading the savings account transaction template. */
@StoreProvider(id = "savingsGetSavingsAccountTransactionTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "savingsGetSavingsAccountTransactionTemplate:{key}", params = ["key:String"])
fun provideGetSavingsAccountTransactionTemplateStore(
    savingsAccountApi: SavingsAccountApi,
    cache: ApiResponseCacheDao,
): Store<GetSavingsAccountTransactionTemplateKey, SavingsAccountTransactionTemplate> = cachedRead(
    dao = cache,
    keyOf = { key ->
        AppCacheKeys.SavingsGetSavingsAccountTransactionTemplate.forKey(
            "${key.savingsAccountType}:${key.savingsAccountId}:${key.transactionType}",
        )
    },
    fetch = { key ->
        savingsAccountApi.getSavingsAccountTransactionTemplate(
            savingsAccountType = key.savingsAccountType,
            savingsAccountId = key.savingsAccountId,
            transactionType = key.transactionType,
        )
    },
)
