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

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.model.loan.AssignLoanOfficerRequestDto
import kpt.core.model.loan.GuarantorRequestDto
import kpt.core.model.loan.Loan
import kpt.core.model.loan.LoanChargeOffRequestDto
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.loan.RejectLoanRequestDto
import kpt.core.model.objects.account.loan.LoanApplicationInput
import kpt.core.model.objects.account.loan.LoanApproval
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseInput
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleApprovalRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRejectionRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse
import kpt.core.model.objects.account.loan.transfer.AccountTransferRequest
import kpt.core.model.objects.payloads.GroupLoanPayload
import kpt.core.network.mifos.loan.api.LoanApi
import kpt.core.network.mifos.loan.dto.LoanDisburseRequestDto
import kpt.core.network.mifos.loan.dto.LoansPayload
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/**
 * Loan writes, through the single write door ([MutationGateway.command]).
 *
 * Network → store → database: [CommandSpec.endpoint] is the Fineract call, and
 * [CommandSpec.localApply] records the server's acceptance by INGESTING the authoritative record
 * through the loan's read store (`fresh` → source-of-truth writer → `LoanWithAssociations`). Ingest,
 * not a local patch: every one of these writes changes state Fineract computes — balances, the
 * repayment schedule, the status cascade, the officer's name — so the request alone cannot be
 * replayed onto the cached row. The profile, details and reschedule streams observe those rows and
 * re-emit by themselves.
 *
 * It used to end at the endpoint with no local side, and three ViewModels re-read after a success to
 * paper over it — the repayment screen, approval and reschedules. Those re-reads are gone.
 */
@RepositoryBinding(binds = LoanCommandRepository::class)
internal class LoanCommandRepositoryImpl(
    private val api: LoanApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Loans) private val loanStore: Store<Int, LoanWithAssociations>,
    @FromStore(AppStoreIds.LoanGetLoanReschedules)
    private val reschedulesStore: Store<Int, List<LoanRescheduleResponse>>,
) : LoanCommandRepository {

    override suspend fun approveLoanApplication(loanId: Int, approval: LoanApproval) =
        online(approval, endpoint = { api.approveLoanApplication(loanId, it) }) { ingestLoan(loanId) }

    override suspend fun rejectLoan(loanId: Int, request: RejectLoanRequestDto) =
        online(request, endpoint = { api.rejectLoan(loanId, it) }) { ingestLoan(loanId) }

    /**
     * Maps the domain input to the wire DTO. This mapping is the whole reason the contract takes a
     * `core/model` type: it keeps `feature/loan` off `core:network`'s classpath.
     */
    override suspend fun disburse(loanId: Int, request: LoanDisburseInput): MutationResult<Unit> =
        online(request.toDto(), endpoint = {
            api.disburse(loanId, it)
            Unit
        }) { ingestLoan(loanId) }

    override suspend fun submitPayment(loanId: Int, request: LoanRepaymentRequest) =
        online(request, endpoint = { api.submitPayment(loanId, it) }) { ingestLoan(loanId) }

    override suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto) =
        online(request, endpoint = { api.chargeOff(loanId, it) }) { ingestLoan(loanId) }

    // The request carries only the new officer's id; the name the profile renders comes back with
    // the loan.
    override suspend fun assignLoanOfficer(loanId: Int, request: AssignLoanOfficerRequestDto) =
        online(request, endpoint = { api.assignLoanOfficer(loanId = loanId, request = it) }) { ingestLoan(loanId) }

    /** The new loan exists only on the server until it is ingested under the id Fineract assigned. */
    override suspend fun createLoanAccount(input: LoanApplicationInput): MutationResult<Unit> =
        online(input, endpoint = { api.createLoansAccount(it.toPayload()) }) { response ->
            response.resourceId?.let { ingestLoan(it) }
        }.mapValue { }

    /**
     * Still answers a [Loan] for the caller's "created loan N" dialog, now built from Fineract's
     * `resourceId` — the body was decoded straight into a `Loan` before, which has no field for it, so
     * the id the dialog showed was always null.
     */
    override suspend fun createGroupLoanAccount(payload: GroupLoanPayload): MutationResult<Loan> =
        online(payload, endpoint = { api.createGroupLoansAccount(it) }) { response ->
            response.resourceId?.let { ingestLoan(it) }
        }.mapValue { Loan(id = it.resourceId) }

    // A schedule preview changes nothing on the server, so it has no local side. It is a POST and
    // needs the network — OnlineRequired keeps it from being queued as if it were a mutation.

    override suspend fun calculateLoanSchedule(
        input: LoanApplicationInput,
    ): MutationResult<RepaymentSchedule> = online(input, endpoint = { api.calculateLoanSchedule(it.toPayload()) })

    // No local side: no table holds guarantors — the loan store's read excludes them
    // (`exclude=guarantors`), and there is no guarantor list read to record the new one in.
    override suspend fun createGuarantor(loanId: Int, request: GuarantorRequestDto) =
        online(request, endpoint = { api.createGuarantor(loanId, it) })

    /**
     * Both ends' balances change. A LOAN end is ingested here; a savings end belongs to the savings
     * stores and is not this repository's to write.
     */
    override suspend fun submitAccountTransfer(request: AccountTransferRequest): MutationResult<Unit> =
        online(request, endpoint = {
            api.submitAccountTransfer(it)
            Unit
        }) {
            if (request.fromAccountType == LOAN_ACCOUNT_TYPE) ingestLoan(request.fromAccountId)
            if (request.toAccountType == LOAN_ACCOUNT_TYPE) ingestLoan(request.toAccountId)
        }

    /** A proposal leaves the loan untouched until it is approved; only the reschedule list changes. */
    override suspend fun submitLoanReschedule(request: LoanRescheduleRequest): MutationResult<Unit> =
        online(request, endpoint = {
            api.submitLoanReschedule(it)
            Unit
        }) { ingestReschedules(request.loanId) }

    /** Approval rewrites the loan's schedule as well as the reschedule's status. */
    override suspend fun approveLoanReschedule(
        loanId: Int,
        scheduleId: Int,
        request: LoanRescheduleApprovalRequest,
    ): MutationResult<Unit> = online(request, endpoint = {
        api.approveLoanReschedule(scheduleId, it)
        Unit
    }) {
        ingestReschedules(loanId)
        ingestLoan(loanId)
    }

    override suspend fun rejectLoanReschedule(
        loanId: Int,
        scheduleId: Int,
        request: LoanRescheduleRejectionRequest,
    ): MutationResult<Unit> = online(request, endpoint = {
        api.rejectLoanReschedule(scheduleId, it)
        Unit
    }) {
        ingestReschedules(loanId)
    }

    /**
     * Every loan command runs [MutationPolicy.OnlineRequired]: it awaits the server and writes
     * nothing offline, yielding `Blocked(OFFLINE)` instead of a local success the officer would act
     * on. Offline capture is a separate, explicit queue — never a silent fallback from here.
     *
     * [ingest] is the command's `localApply`; for this policy the gateway runs it only with the
     * server's result, never before the call.
     */
    private suspend fun <P : Any, R : Any> online(
        payload: P,
        endpoint: suspend (P) -> R,
        ingest: (suspend (R) -> Unit)? = null,
    ): MutationResult<R> = gateway.command(
        CommandSpec(
            payload = payload,
            endpoint = endpoint,
            localApply = ingest?.let { apply -> { result: R? -> if (result != null) apply(result) } },
        ),
        policy = MutationPolicy.OnlineRequired,
    )

    private suspend fun ingestLoan(loanId: Int) = ingest("loan $loanId") { loanStore.fresh(loanId) }

    private suspend fun ingestReschedules(loanId: Int) =
        ingest("reschedules of loan $loanId") { reschedulesStore.fresh(loanId) }

    /**
     * The ingest re-reads over the network, and it runs AFTER the server has committed. Letting its
     * failure escape would turn the gateway's answer into `Failed` for money that already moved, and
     * an officer told a repayment failed submits it again. So it is logged, not thrown: the row stays
     * as it was, and the read stream's own fetch corrects it the next time it is opened.
     */
    private suspend inline fun ingest(what: String, block: () -> Unit) {
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Logger.w(tag = TAG, throwable = e) { "server accepted the write; ingesting $what failed" }
        }
    }

    private companion object {
        const val TAG = "LoanCommandRepository"

        /** Fineract's `portfolioAccountType` for a loan; 2 is savings. */
        const val LOAN_ACCOUNT_TYPE = 1
    }
}

/** Re-type a command's value, keeping its outcome. */
private inline fun <T, U> MutationResult<T>.mapValue(transform: (T) -> U): MutationResult<U> = when (this) {
    is MutationResult.Applied -> MutationResult.Applied(transform(value), synced)
    is MutationResult.Conflicted -> MutationResult.Conflicted(conflictId, transform(server))
    is MutationResult.Blocked -> this
    is MutationResult.Failed -> this
}

/** The one place the disbursement's domain input becomes a wire payload. */
private fun LoanDisburseInput.toDto() = LoanDisburseRequestDto(
    actualDisbursementDate = actualDisbursementDate,
    transactionAmount = transactionAmount,
    paymentTypeId = paymentTypeId,
    note = note,
    externalId = externalId,
    accountNumber = accountNumber,
    checkNumber = checkNumber,
    routingCode = routingCode,
    receiptNumber = receiptNumber,
    bankNumber = bankNumber,
    locale = locale,
    dateFormat = dateFormat,
)

/**
 * The `core/model` input -> network DTO boundary.
 *
 * Lives here rather than on the input type: `core/model` must not know about `core/network` either,
 * and this is the only place the two meet. `dataTables` is deliberately unset — no caller collects
 * them, and the original's `addLoanAccountScreen` left its datatable callback commented out.
 */
private fun LoanApplicationInput.toPayload() = LoansPayload(
    clientId = clientId,
    productId = productId,
    loanType = loanType,
    submittedOnDate = submittedOnDate,
    expectedDisbursementDate = expectedDisbursementDate,
    locale = locale,
    dateFormat = dateFormat,
    principal = principal,
    numberOfRepayments = numberOfRepayments,
    interestRatePerPeriod = interestRatePerPeriod,
    repaymentEvery = repaymentEvery,
    loanTermFrequency = loanTermFrequency,
    loanTermFrequencyType = loanTermFrequencyType,
    repaymentFrequencyType = repaymentFrequencyType,
    repaymentFrequencyDayOfWeekType = repaymentFrequencyDayOfWeekType,
    repaymentFrequencyNthDayType = repaymentFrequencyNthDayType,
    amortizationType = amortizationType,
    interestType = interestType,
    interestCalculationPeriodType = interestCalculationPeriodType,
    transactionProcessingStrategyCode = transactionProcessingStrategyCode,
    allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation,
    loanPurposeId = loanPurposeId,
    fundId = fundId,
    loanOfficerId = loanOfficerId,
    linkAccountId = linkAccountId,
    externalId = externalId,
)
