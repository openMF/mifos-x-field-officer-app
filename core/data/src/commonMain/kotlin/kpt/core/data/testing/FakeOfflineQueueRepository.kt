/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.sync.OfflineQueueRepository
import kpt.core.model.center.CenterPayload
import kpt.core.model.client.ClientPayload
import kpt.core.model.group.GroupPayload
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.savings.SavingsAccountTransactionRequest

/**
 * Test double for [OfflineQueueRepository], beside the interface it fakes — same reasoning as
 * [FakeClientRepository].
 *
 * Only the LOAN-REPAYMENT half is implemented, because that is the only queue a ported screen
 * reads so far: the repayment form refuses a second capture while one is pending, and queues the
 * request itself when the device is offline. The client / centre / group / savings members throw.
 *
 * [enqueueResult] exists so a test can model the queue write FAILING, which is the one outcome
 * that must never be reported as success — cash has been collected and nothing holds the record.
 */
@ExperimentalDataTestingApi
class FakeOfflineQueueRepository(
    private val pendingLoanRepayments: MutableStateFlow<ScreenState<List<LoanRepaymentRequest>>> =
        MutableStateFlow(ScreenState.Content(emptyList())),
    private val enqueueResult: (LoanRepaymentRequest) -> MutationResult<LoanRepaymentRequest> =
        { MutationResult.Applied(it, synced = false) },
) : OfflineQueueRepository {

    /** Every repayment handed to the queue, in order — a test asserts WHAT was saved. */
    val enqueued: MutableList<Pair<Int, LoanRepaymentRequest>> = mutableListOf()

    /** Every stream's refresh signal. */
    val refreshes: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 8)

    override fun pendingLoanRepaymentsStream(scope: CoroutineScope) =
        screenDataStreamForTesting(pendingLoanRepayments, refreshTrigger = refreshes)

    override suspend fun enqueueLoanRepayment(
        loanId: Int,
        request: LoanRepaymentRequest,
    ): MutationResult<LoanRepaymentRequest> {
        enqueued += loanId to request
        return enqueueResult(request)
    }

    override suspend fun discardLoanRepayment(loanId: Int) = unused()

    override fun pendingClientsStream(scope: CoroutineScope) = unused()
    override fun pendingCentersStream(scope: CoroutineScope) = unused()
    override fun pendingGroupsStream(scope: CoroutineScope) = unused()
    override fun pendingSavingsTransactionsStream(scope: CoroutineScope) = unused()

    override suspend fun enqueueClient(id: Int, payload: ClientPayload) = unused()
    override suspend fun enqueueCenter(id: Int, payload: CenterPayload) = unused()
    override suspend fun enqueueGroup(id: Int, payload: GroupPayload) = unused()
    override suspend fun enqueueSavingsTransaction(
        accountId: Int,
        request: SavingsAccountTransactionRequest,
    ) = unused()

    override suspend fun discardClient(id: Int) = unused()
    override suspend fun discardCenter(id: Int) = unused()
    override suspend fun discardGroup(id: Int) = unused()
    override suspend fun discardSavingsTransaction(accountId: Int) = unused()

    /** `Nothing`, so one stub satisfies every unused member's return type and still fails loudly. */
    private fun unused(): Nothing =
        error("FakeOfflineQueueRepository: only the loan-repayment queue is exercised so far")
}
