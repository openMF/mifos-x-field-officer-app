/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.loan.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.loan.LoanRepository
import kpt.core.data.util.freshOrCached
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.loan.LoanTemplate
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseTemplate
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleTemplate
import kpt.core.model.objects.account.loan.transfer.AccountTransferTemplate
import kpt.core.model.objects.organisations.LoanProducts
import kpt.core.model.objects.template.loan.GroupLoanTemplate
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import kpt.core.store.loan.GetAccountTransferTemplateKey
import kpt.core.store.loan.GetGroupLoansAccountTemplateKey
import kpt.core.store.loan.LoanTemplateKey
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = LoanRepository::class)
internal class LoanRepositoryImpl(
    @FromStore(AppStoreIds.Loans) private val store: Store<Int, LoanWithAssociations>,
    @FromStore(AppStoreIds.LoanTemplates) private val templateStore: Store<LoanTemplateKey, LoanTemplate>,
    @FromStore(AppStoreIds.LoanRepaymentTemplates)
    private val repaymentTemplateStore: Store<Int, LoanRepaymentTemplate>,
    @FromStore(AppStoreIds.LoanDisburseTemplates) private val disburseTemplateStore: Store<Int, LoanDisburseTemplate>,
    @FromStore(AppStoreIds.LoanGetAccountTransferTemplate)
    private val transferTemplateStore: Store<GetAccountTransferTemplateKey, AccountTransferTemplate>,
    @FromStore(AppStoreIds.LoanGetLoanReschedules)
    private val reschedulesStore: Store<Int, List<LoanRescheduleResponse>>,
    @FromStore(AppStoreIds.LoanRescheduleTemplate)
    private val rescheduleTemplateStore: Store<Unit, LoanRescheduleTemplate>,
    @FromStore(AppStoreIds.LoanProducts) private val loanProductsStore: Store<Unit, List<LoanProducts>>,
    @FromStore(AppStoreIds.LoanGetGroupLoansAccountTemplate)
    private val groupLoanTemplateStore: Store<GetGroupLoansAccountTemplateKey, GroupLoanTemplate>,
) : LoanRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
    // until something HAS been fetched, so on a clean install nothing ever loads. Room is still
    // served first, so the offline-first behaviour is unchanged. The ttl stays the declared
    // freshness bound — it drives the "updated N ago" indicator, never whether cache is served.
    override fun loanStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<LoanWithAssociations> =
        store.asScreenStream(
            key = loanId,
            cacheKey = AppCacheKeys.Loans.byId(loanId),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.LOANS,
        )

    override fun loanTemplateStream(
        clientId: Int,
        productId: Int?,
        scope: CoroutineScope,
    ): ScreenDataStream<LoanTemplate> =
        templateStore.asScreenStream(
            key = LoanTemplateKey(clientId, productId),
            cacheKey = AppCacheKeys.LoanTemplates.forClient(clientId, productId ?: NO_PRODUCT),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.LOAN_TEMPLATES,
        )

    override fun repaymentTemplateStream(
        loanId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<LoanRepaymentTemplate> =
        repaymentTemplateStore.asScreenStream(
            key = loanId,
            cacheKey = AppCacheKeys.LoanRepaymentTemplates.forLoan(loanId),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.LOAN_REPAYMENT_TEMPLATES,
        )

    override fun disburseTemplateStream(
        loanId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<LoanDisburseTemplate> =
        disburseTemplateStore.asScreenStream(
            key = loanId,
            cacheKey = AppCacheKeys.LoanDisburseTemplates.forLoan(loanId),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.LOAN_DISBURSE_TEMPLATES,
        )

    override suspend fun accountTransferTemplate(
        fromClientId: Int,
        fromAccountId: Int,
        fromAccountType: Int,
        fromOfficeId: Int?,
        toOfficeId: Int?,
        toClientId: Int?,
        toAccountType: Int?,
    ): AccountTransferTemplate? {
        val key = GetAccountTransferTemplateKey(
            fromClientId = fromClientId,
            fromAccountType = fromAccountType,
            fromAccountId = fromAccountId,
            fromOfficeId = fromOfficeId,
            toOfficeId = toOfficeId,
            toClientId = toClientId,
            toAccountType = toAccountType,
        )
        // Each cascade step is its OWN store key. Stepping back up the cascade re-asks the server
        // rather than replaying a cached answer: the cache is persistent, so "served from cache"
        // also meant "never refreshed again, across restarts". Offline still answers from it.
        return transferTemplateStore.freshOrCached(key)
    }

    override fun loanProductsStream(scope: CoroutineScope): ScreenDataStream<List<LoanProducts>> =
        loanProductsStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.LoanProducts.LIST,
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.LOAN_PRODUCTS,
        )

    override suspend fun groupLoanTemplate(groupId: Int, productId: Int): GroupLoanTemplate? =
        groupLoanTemplateStore.freshOrCached(
            GetGroupLoansAccountTemplateKey(groupId = groupId, productId = productId),
        )

    override suspend fun loanTemplate(clientId: Int, productId: Int): LoanTemplate? =
        templateStore.freshOrCached(LoanTemplateKey(clientId = clientId, productId = productId))

    override fun loanReschedulesStream(
        loanId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<List<LoanRescheduleResponse>> = reschedulesStore.asScreenStream(
        key = loanId,
        cacheKey = AppCacheKeys.LoanGetLoanReschedules.forKey(loanId.toString()),
        scope = scope,
        // No `isEmpty` predicate: the reschedules ViewModel declares the empty decision with
        // `emptyIfContent`, so stating it here too would be the same rule in two places, free to
        // drift. The ViewModel's is the one a test can prove.
        fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        ttl = AppStoreRegistry.Ttl.LOAN_GET_LOAN_RESCHEDULES,
    )

    override suspend fun loanRescheduleReasons(): LoanRescheduleTemplate? =
        rescheduleTemplateStore.freshOrCached(Unit)

    private companion object {
        /** Mirrors the store's sentinel for "no product chosen yet". */
        const val NO_PRODUCT = -1
    }
}
