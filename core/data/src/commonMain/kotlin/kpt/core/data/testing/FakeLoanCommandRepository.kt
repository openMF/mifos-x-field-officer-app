/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import kotlinx.coroutines.CompletableDeferred
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.model.loan.AssignLoanOfficerRequestDto
import kpt.core.model.loan.GuarantorRequestDto
import kpt.core.model.loan.Loan
import kpt.core.model.loan.LoanChargeOffRequestDto
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.loan.LoanRepaymentResponse
import kpt.core.model.loan.RejectLoanRequestDto
import kpt.core.model.objects.account.loan.LoanApplicationInput
import kpt.core.model.objects.account.loan.LoanApproval
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseInput
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleApprovalRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRejectionRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRequest
import kpt.core.model.objects.account.loan.transfer.AccountTransferRequest
import kpt.core.model.objects.payloads.GroupLoanPayload
import kpt.core.model.shared.GenericResponse

/**
 * Test double for [LoanCommandRepository] — the loan-side twin of [FakeClientCommandRepository],
 * for the same reason (the interface names `core/network` DTOs, so the fake belongs beside it).
 *
 * [approveLoanApplication], [submitPayment], [disburse], [submitAccountTransfer],
 * [createGroupLoanAccount] and the three reschedule commands are implemented; the remaining six
 * THROW. The remaining loan write screens land with the rest of S4 and will implement what they
 * exercise — a fake that returned success for all fourteen would let a miswired command pass.
 *
 * [gate], when supplied, is awaited before returning, so a test can hold the approval in flight
 * and prove the double-submit guard.
 */
@ExperimentalDataTestingApi
class FakeLoanCommandRepository(
    private val gate: CompletableDeferred<Unit>? = null,
    private val result: () -> MutationResult<GenericResponse> =
        { MutationResult.Applied(GenericResponse(), synced = true) },
    private val paymentResult: () -> MutationResult<LoanRepaymentResponse> =
        { MutationResult.Applied(LoanRepaymentResponse(resourceId = 9_001), synced = true) },
    private val disburseResult: () -> MutationResult<Unit> =
        { MutationResult.Applied(Unit, synced = true) },
    private val transferResult: () -> MutationResult<Unit> =
        { MutationResult.Applied(Unit, synced = true) },
    private val rescheduleResult: () -> MutationResult<Unit> =
        { MutationResult.Applied(Unit, synced = true) },
    private val groupLoanResult: () -> MutationResult<Loan> =
        { MutationResult.Applied(Loan(id = 7_700), synced = true) },
    private val createLoanResult: () -> MutationResult<Unit> =
        { MutationResult.Applied(Unit, synced = true) },
    private val scheduleResult: () -> MutationResult<RepaymentSchedule> =
        { MutationResult.Applied(RepaymentSchedule(), synced = true) },
) : LoanCommandRepository {

    /** Every approval sent, in order — a test asserts the PAYLOAD, not merely that one was sent. */
    val approvals: MutableList<Pair<Int, LoanApproval>> = mutableListOf()

    override suspend fun approveLoanApplication(
        loanId: Int,
        approval: LoanApproval,
    ): MutationResult<GenericResponse> {
        approvals += loanId to approval
        gate?.await()
        return result()
    }

    override suspend fun rejectLoan(loanId: Int, request: RejectLoanRequestDto) = unused()

    /** Every disbursement sent, in order — a test asserts the PAYLOAD, date format included. */
    val disbursements: MutableList<Pair<Int, LoanDisburseInput>> = mutableListOf()

    override suspend fun disburse(
        loanId: Int,
        request: LoanDisburseInput,
    ): MutationResult<Unit> {
        disbursements += loanId to request
        gate?.await()
        return disburseResult()
    }

    /** Every repayment sent, in order — a test asserts the PAYLOAD, including its date format. */
    val payments: MutableList<Pair<Int, LoanRepaymentRequest>> = mutableListOf()

    override suspend fun submitPayment(
        loanId: Int,
        request: LoanRepaymentRequest,
    ): MutationResult<LoanRepaymentResponse> {
        payments += loanId to request
        gate?.await()
        return paymentResult()
    }

    override suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto) = unused()

    override suspend fun assignLoanOfficer(loanId: Int, request: AssignLoanOfficerRequestDto) = unused()

    /** Every individual loan application sent — a test asserts the PAYLOAD, not merely a send. */
    val loanApplications: MutableList<LoanApplicationInput> = mutableListOf()

    override suspend fun createLoanAccount(input: LoanApplicationInput): MutationResult<Unit> {
        loanApplications += input
        gate?.await()
        return createLoanResult()
    }

    /** Every group loan application sent — a test asserts the PAYLOAD, not merely a send. */
    val groupLoanApplications: MutableList<GroupLoanPayload> = mutableListOf()

    override suspend fun createGroupLoanAccount(payload: GroupLoanPayload): MutationResult<Loan> {
        groupLoanApplications += payload
        gate?.await()
        return groupLoanResult()
    }

    /** Every schedule preview asked for, so a test can prove the cascade re-asked. */
    val schedulePreviews: MutableList<LoanApplicationInput> = mutableListOf()

    override suspend fun calculateLoanSchedule(
        input: LoanApplicationInput,
    ): MutationResult<RepaymentSchedule> {
        schedulePreviews += input
        return scheduleResult()
    }

    override suspend fun createGuarantor(loanId: Int, request: GuarantorRequestDto) = unused()

    /** Every transfer sent, in order — a test asserts the full thirteen-field payload. */
    val transfers: MutableList<AccountTransferRequest> = mutableListOf()

    override suspend fun submitAccountTransfer(
        request: AccountTransferRequest,
    ): MutationResult<Unit> {
        transfers += request
        gate?.await()
        return transferResult()
    }

    /** Every reschedule proposed, approved and rejected, in order. */
    val proposedReschedules: MutableList<LoanRescheduleRequest> = mutableListOf()

    /** The approved reschedules, as (loan id, request), in order. */
    val approvedReschedules: MutableList<Pair<Int, LoanRescheduleApprovalRequest>> = mutableListOf()

    /** The rejected reschedules, as (loan id, request), in order. */
    val rejectedReschedules: MutableList<Pair<Int, LoanRescheduleRejectionRequest>> = mutableListOf()

    override suspend fun submitLoanReschedule(
        request: LoanRescheduleRequest,
    ): MutationResult<Unit> {
        proposedReschedules += request
        gate?.await()
        return rescheduleResult()
    }

    override suspend fun approveLoanReschedule(
        loanId: Int,
        scheduleId: Int,
        request: LoanRescheduleApprovalRequest,
    ): MutationResult<Unit> {
        approvedReschedules += scheduleId to request
        gate?.await()
        return rescheduleResult()
    }

    override suspend fun rejectLoanReschedule(
        loanId: Int,
        scheduleId: Int,
        request: LoanRescheduleRejectionRequest,
    ): MutationResult<Unit> {
        rejectedReschedules += scheduleId to request
        gate?.await()
        return rescheduleResult()
    }

    /**
     * The thirteen commands no ported screen sends yet.
     *
     * `Nothing` rather than a fabricated success: it satisfies every one of their distinct return
     * types without a per-method stub, and it makes a miswired call a loud failure instead of a
     * green test.
     */
    private fun unused(): Nothing =
        error("FakeLoanCommandRepository: this command is not exercised by any ported screen yet")
}
