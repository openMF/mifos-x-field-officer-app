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
import kpt.core.database.loan.entity.LoanTransactionTemplate
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse
import kpt.core.model.objects.account.loan.transfer.AccountTransferTemplate
import kpt.core.model.objects.template.loan.GroupLoanTemplate
import kpt.core.network.mifos.loan.api.LoanApi
import kpt.core.network.mifos.loan.dto.GuarantorAccountTemplateDto
import kpt.core.network.mifos.loan.dto.GuarantorTemplateDto
import kpt.core.network.mifos.loan.dto.LoanChargeOffTemplateDto
import kpt.core.network.mifos.loan.dto.LoanOfficerOptionsTemplateDto
import kpt.core.network.mifos.loan.dto.LoanWithAssociationsDto
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for loan with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `getGuarantorAccountTemplate` read. */
data class GetGuarantorAccountTemplateKey(
    val loanId: Int,
    val clientId: Int,
)

/** Addresses one `getLoanTransactionTemplate` read. */
data class GetLoanTransactionTemplateKey(
    val loanId: Int,
    val command: String?,
)

/** Addresses one `getGroupLoansAccountTemplate` read. */
data class GetGroupLoansAccountTemplateKey(
    val groupId: Int,
    val productId: Int,
)

/** Addresses one `getAccountTransferTemplate` read. */
data class GetAccountTransferTemplateKey(
    val fromClientId: Int,
    val fromAccountType: Int,
    val fromAccountId: Int,
    val fromOfficeId: Int? = null,
    val toOfficeId: Int? = null,
    val toClientId: Int? = null,
    val toAccountType: Int? = null,
    val toAccountId: Int? = null,
)

@StoreProvider(id = "loanGetChargeOffTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetChargeOffTemplate:{key}", params = ["key:String"])
fun provideGetChargeOffTemplateStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Int, LoanChargeOffTemplateDto> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetChargeOffTemplate.forKey(key.toString()) },
    fetch = { key -> loanApi.getChargeOffTemplate(loanId = key) },
)

@StoreProvider(id = "loanGetLoanRepaymentSchedule", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetLoanRepaymentSchedule:{key}", params = ["key:String"])
fun provideGetLoanRepaymentScheduleStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Int, LoanWithAssociationsDto> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetLoanRepaymentSchedule.forKey(key.toString()) },
    fetch = { key -> loanApi.getLoanRepaymentSchedule(loanId = key) },
)

@StoreProvider(id = "loanGetLoanWithTransactions", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetLoanWithTransactions:{key}", params = ["key:String"])
fun provideGetLoanWithTransactionsStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Int, LoanWithAssociationsDto> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetLoanWithTransactions.forKey(key.toString()) },
    fetch = { key -> loanApi.getLoanWithTransactions(loanId = key) },
)

@StoreProvider(id = "loanGetGuarantorTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetGuarantorTemplate:{key}", params = ["key:String"])
fun provideGetGuarantorTemplateStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Int, GuarantorTemplateDto> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetGuarantorTemplate.forKey(key.toString()) },
    fetch = { key -> loanApi.getGuarantorTemplate(loanId = key) },
)

@StoreProvider(id = "loanGetGuarantorAccountTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetGuarantorAccountTemplate:{key}", params = ["key:String"])
fun provideGetGuarantorAccountTemplateStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<GetGuarantorAccountTemplateKey, GuarantorAccountTemplateDto> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetGuarantorAccountTemplate.forKey("${key.loanId}:${key.clientId}") },
    fetch = { key -> loanApi.getGuarantorAccountTemplate(loanId = key.loanId, clientId = key.clientId) },
)

@StoreProvider(id = "loanGetLoanTransactionTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetLoanTransactionTemplate:{key}", params = ["key:String"])
fun provideGetLoanTransactionTemplateStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<GetLoanTransactionTemplateKey, LoanTransactionTemplate> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetLoanTransactionTemplate.forKey("${key.loanId}:${key.command}") },
    fetch = { key -> loanApi.getLoanTransactionTemplate(loanId = key.loanId, command = key.command) },
)

@StoreProvider(id = "loanGetGroupLoansAccountTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetGroupLoansAccountTemplate:{key}", params = ["key:String"])
fun provideGetGroupLoansAccountTemplateStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<GetGroupLoansAccountTemplateKey, GroupLoanTemplate> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetGroupLoansAccountTemplate.forKey("${key.groupId}:${key.productId}") },
    fetch = { key -> loanApi.getGroupLoansAccountTemplate(groupId = key.groupId, productId = key.productId) },
)

@StoreProvider(id = "loanGetAccountTransferTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetAccountTransferTemplate:{key}", params = ["key:String"])
fun provideGetAccountTransferTemplateStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<GetAccountTransferTemplateKey, AccountTransferTemplate> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetAccountTransferTemplate.forKey("${key.fromClientId}:${key.fromAccountType}:${key.fromAccountId}:${key.fromOfficeId}:${key.toOfficeId}:${key.toClientId}:${key.toAccountType}:${key.toAccountId}") },
    fetch = { key -> loanApi.getAccountTransferTemplate(fromClientId = key.fromClientId, fromAccountType = key.fromAccountType, fromAccountId = key.fromAccountId, fromOfficeId = key.fromOfficeId, toOfficeId = key.toOfficeId, toClientId = key.toClientId, toAccountType = key.toAccountType, toAccountId = key.toAccountId) },
)

@StoreProvider(id = "loanGetLoanReschedules", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetLoanReschedules:{key}", params = ["key:String"])
fun provideGetLoanReschedulesStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Int, List<LoanRescheduleResponse>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetLoanReschedules.forKey(key.toString()) },
    fetch = { key -> loanApi.getLoanReschedules(loanId = key) },
)

@StoreProvider(id = "loanGetLoanOfficerTemplate", ttl = "12h")
@CacheKey(fn = "forKey", key = "loanGetLoanOfficerTemplate:{key}", params = ["key:String"])
fun provideGetLoanOfficerTemplateStore(
    loanApi: LoanApi,
    cache: ApiResponseCacheDao,
): Store<Int, LoanOfficerOptionsTemplateDto> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.LoanGetLoanOfficerTemplate.forKey(key.toString()) },
    fetch = { key -> loanApi.getLoanOfficerTemplate(loanId = key) },
)
