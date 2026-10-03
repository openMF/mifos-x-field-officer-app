/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.sync.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.sync.OfflineQueueRepository
import kpt.core.database.center.entity.CenterPayloadEntity
import kpt.core.database.client.entity.ClientPayloadEntity
import kpt.core.database.group.entity.GroupPayloadEntity
import kpt.core.database.loan.entity.LoanRepaymentRequestEntity
import kpt.core.database.savings.entity.SavingsAccountTransactionRequestEntity
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = OfflineQueueRepository::class)
internal class OfflineQueueRepositoryImpl(
    @FromStore(AppStoreIds.PendingClientPayloads) private val pendingClients: Store<Unit, List<ClientPayloadEntity>>,
    @FromStore(AppStoreIds.PendingCenterPayloads) private val pendingCenters: Store<Unit, List<CenterPayloadEntity>>,
    @FromStore(AppStoreIds.PendingGroupPayloads) private val pendingGroups: Store<Unit, List<GroupPayloadEntity>>,
    @FromStore(AppStoreIds.PendingLoanRepayments) private val pendingLoanRepayments: Store<Unit, List<LoanRepaymentRequestEntity>>,
    @FromStore(AppStoreIds.PendingSavingsTransactions) private val pendingSavings: Store<Unit, List<SavingsAccountTransactionRequestEntity>>,
    @FromStore(AppStoreIds.ClientPayloadQueue) private val clientQueue: MutableStore<Int, ClientPayloadEntity>,
    @FromStore(AppStoreIds.CenterPayloadQueue) private val centerQueue: MutableStore<Int, CenterPayloadEntity>,
    @FromStore(AppStoreIds.GroupPayloadQueue) private val groupQueue: MutableStore<Int, GroupPayloadEntity>,
    @FromStore(AppStoreIds.LoanRepaymentQueue) private val loanRepaymentQueue: MutableStore<Int, LoanRepaymentRequestEntity>,
    @FromStore(AppStoreIds.SavingsTransactionQueue) private val savingsQueue: MutableStore<Int, SavingsAccountTransactionRequestEntity>,
    private val gateway: MutationGateway,
) : OfflineQueueRepository {

    // CACHE_ONLY, not the CACHE_FIRST_SWR default: these rows exist ONLY on this device until the
    // sync drains them, so there is no server view to revalidate against and a fetch would be a
    // guaranteed-failed network call on every queue read.
    override fun pendingClientsStream(scope: CoroutineScope) =
        pendingClients.queueStream(AppCacheKeys.PendingClientPayloads.LIST, scope)

    override fun pendingCentersStream(scope: CoroutineScope) =
        pendingCenters.queueStream(AppCacheKeys.PendingCenterPayloads.LIST, scope)

    override fun pendingGroupsStream(scope: CoroutineScope) =
        pendingGroups.queueStream(AppCacheKeys.PendingGroupPayloads.LIST, scope)

    override fun pendingLoanRepaymentsStream(scope: CoroutineScope) =
        pendingLoanRepayments.queueStream(AppCacheKeys.PendingLoanRepayments.LIST, scope)

    override fun pendingSavingsTransactionsStream(scope: CoroutineScope) =
        pendingSavings.queueStream(AppCacheKeys.PendingSavingsTransactions.LIST, scope)

    override suspend fun enqueueClient(id: Int, payload: ClientPayloadEntity) =
        gateway.upsert(clientQueue, id, payload, MutationPolicy.Optimistic)

    override suspend fun enqueueCenter(id: Int, payload: CenterPayloadEntity) =
        gateway.upsert(centerQueue, id, payload, MutationPolicy.Optimistic)

    override suspend fun enqueueGroup(id: Int, payload: GroupPayloadEntity) =
        gateway.upsert(groupQueue, id, payload, MutationPolicy.Optimistic)

    override suspend fun enqueueLoanRepayment(loanId: Int, request: LoanRepaymentRequestEntity) =
        gateway.upsert(loanRepaymentQueue, loanId, request, MutationPolicy.Optimistic)

    override suspend fun enqueueSavingsTransaction(
        accountId: Int,
        request: SavingsAccountTransactionRequestEntity,
    ) = gateway.upsert(savingsQueue, accountId, request, MutationPolicy.Optimistic)

    override suspend fun discardClient(id: Int) = clientQueue.clear(id)
    override suspend fun discardCenter(id: Int) = centerQueue.clear(id)
    override suspend fun discardGroup(id: Int) = groupQueue.clear(id)
    override suspend fun discardLoanRepayment(loanId: Int) = loanRepaymentQueue.clear(loanId)
    override suspend fun discardSavingsTransaction(accountId: Int) = savingsQueue.clear(accountId)

    private fun <T : Any> Store<Unit, List<T>>.queueStream(
        cacheKey: String,
        scope: CoroutineScope,
    ): ScreenDataStream<List<T>> = asScreenStream(
        key = Unit,
        cacheKey = cacheKey,
        scope = scope,
        isEmpty = { it.isEmpty() },
        fetchPolicy = FetchPolicy.CACHE_ONLY,
    )
}
