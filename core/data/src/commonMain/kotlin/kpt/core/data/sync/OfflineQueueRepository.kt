/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.sync

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.database.center.entity.CenterPayloadEntity
import kpt.core.database.client.entity.ClientPayloadEntity
import kpt.core.database.group.entity.GroupPayloadEntity
import kpt.core.database.loan.entity.LoanRepaymentRequestEntity
import kpt.core.database.savings.entity.SavingsAccountTransactionRequestEntity

/**
 * Work captured with no connectivity, and the queue the sync screen drains.
 *
 * Enqueue is `MutationPolicy.Optimistic` by construction — capture happens against Room and MUST
 * succeed offline, which is the entire point of the app. The network leg is a separate, explicit
 * sync of these queues, so the officer can tell "saved" from "sent"; nothing here silently posts.
 *
 * Every enqueue returns [MutationResult] rather than Unit: the caller has to be able to say whether
 * the row landed, and a `Failed` here means the capture itself was lost — the one outcome a field
 * officer must never be left guessing about.
 */
interface OfflineQueueRepository {

    fun pendingClientsStream(scope: CoroutineScope): ScreenDataStream<List<ClientPayloadEntity>>
    fun pendingCentersStream(scope: CoroutineScope): ScreenDataStream<List<CenterPayloadEntity>>
    fun pendingGroupsStream(scope: CoroutineScope): ScreenDataStream<List<GroupPayloadEntity>>
    fun pendingLoanRepaymentsStream(scope: CoroutineScope): ScreenDataStream<List<LoanRepaymentRequestEntity>>
    fun pendingSavingsTransactionsStream(scope: CoroutineScope): ScreenDataStream<List<SavingsAccountTransactionRequestEntity>>

    suspend fun enqueueClient(id: Int, payload: ClientPayloadEntity): MutationResult<ClientPayloadEntity>
    suspend fun enqueueCenter(id: Int, payload: CenterPayloadEntity): MutationResult<CenterPayloadEntity>
    suspend fun enqueueGroup(id: Int, payload: GroupPayloadEntity): MutationResult<GroupPayloadEntity>
    suspend fun enqueueLoanRepayment(loanId: Int, request: LoanRepaymentRequestEntity): MutationResult<LoanRepaymentRequestEntity>
    suspend fun enqueueSavingsTransaction(accountId: Int, request: SavingsAccountTransactionRequestEntity): MutationResult<SavingsAccountTransactionRequestEntity>

    /** Drop a queued row — the sync accepted it, or the officer discarded it. */
    suspend fun discardClient(id: Int)
    suspend fun discardCenter(id: Int)
    suspend fun discardGroup(id: Int)
    suspend fun discardLoanRepayment(loanId: Int)
    suspend fun discardSavingsTransaction(accountId: Int)
}
