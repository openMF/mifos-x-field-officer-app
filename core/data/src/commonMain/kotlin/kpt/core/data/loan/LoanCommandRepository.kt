/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.loan

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.loan.AssignLoanOfficerRequestDto
import kpt.core.model.loan.AssignLoanOfficerResponseDto
import kpt.core.model.loan.CreateGuarantorResponseDto
import kpt.core.model.loan.GuarantorRequestDto
import kpt.core.model.loan.Loan
import kpt.core.model.loan.LoanChargeOffRequestDto
import kpt.core.model.loan.LoanChargeOffResponseDto
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.loan.LoanRepaymentResponse
import kpt.core.model.loan.RejectLoanRequestDto
import kpt.core.model.loan.RejectLoanResponseDto
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
 * Every loan write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit` — a field officer has to be told whether an
 * approval reached the server, was refused because the device is offline, or failed outright, and
 * that distinction cannot be reconstructed above this layer.
 *
 * ## Policy is per method, not per repository
 *
 * Anything that moves money or changes a loan's legal state — approve, reject, disburse, repay,
 * charge-off, reschedule, officer reassignment — is
 * [OnlineRequired][kpt.core.base.store.mutation.MutationPolicy.OnlineRequired]: it awaits the server and
 * writes NOTHING offline, surfacing `Blocked(OFFLINE)` instead. An
 * optimistic approval is a lie the officer would act on — telling them a loan is approved when the
 * server never saw it is worse than telling them to find signal.
 *
 * Offline CAPTURE of a repayment is a different operation and lives on
 * [kpt.core.data.sync.OfflineQueueRepository]: it queues the intent locally and is explicit that
 * nothing has been sent. [submitPayment] here is the online, immediate path.
 *
 * ## The local side
 *
 * A command lands locally by ingesting the server's record THROUGH the loan's read store inside the
 * gateway (`CommandSpec.localApply`) — Fineract computes the balances, schedules and statuses a loan
 * write changes, so the request cannot be replayed onto the cached row. A caller never re-reads after
 * a write: the streams over those rows have already re-emitted.
 *
 * Commands take the API's request DTO rather than a parallel domain type. A command payload has no
 * cached domain form — it exists only to be sent — so a second shape would be a copy with nothing
 * to keep it honest.
 */
interface LoanCommandRepository {

    /** Approves a loan application. */
    suspend fun approveLoanApplication(loanId: Int, approval: LoanApproval): MutationResult<GenericResponse>

    /** Rejects a loan application. */
    suspend fun rejectLoan(loanId: Int, request: RejectLoanRequestDto): MutationResult<RejectLoanResponseDto>

    /**
     * Disburse an approved loan.
     *
     * Takes [LoanDisburseInput] — a `core/model` type — rather than the network DTO, because
     * `feature/loan` must not depend on `core:network`. Returns `Unit`: Fineract's response carries
     * office / client / loan / resource ids the disbursement screen does not render, and a feature
     * cannot name `LoanDisburseResponseDto` either.
     */
    suspend fun disburse(loanId: Int, request: LoanDisburseInput): MutationResult<Unit>

    /** Records a loan repayment. */
    suspend fun submitPayment(
        loanId: Int,
        request: LoanRepaymentRequest,
    ): MutationResult<LoanRepaymentResponse>

    /** Charges off a loan. */
    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>

    /** Assigns or changes a loan's officer. */
    suspend fun assignLoanOfficer(
        loanId: Int,
        request: AssignLoanOfficerRequestDto,
    ): MutationResult<AssignLoanOfficerResponseDto>

    /**
     * Submit a new INDIVIDUAL loan application.
     *
     * `MutationResult<Unit>`, not `MutationResult<HttpResponse>`: a feature module must not depend
     * on `core:network`, and the caller has no use for the raw response — Fineract returns the new
     * loan id, which this screen does not navigate to.
     */
    suspend fun createLoanAccount(input: LoanApplicationInput): MutationResult<Unit>

    /** Opens a group loan account. */
    suspend fun createGroupLoanAccount(payload: GroupLoanPayload): MutationResult<Loan>

    /**
     * Preview the repayment schedule for an application that has NOT been submitted.
     *
     * Returns the schedule itself — the preview page renders it, so handing back an `HttpResponse`
     * would make the one caller parse a body from a feature module.
     */
    suspend fun calculateLoanSchedule(input: LoanApplicationInput): MutationResult<RepaymentSchedule>

    /** Adds a guarantor to a loan. */
    suspend fun createGuarantor(loanId: Int, request: GuarantorRequestDto): MutationResult<CreateGuarantorResponseDto>

    /**
     * Move money between two accounts.
     *
     * `Unit`, not `HttpResponse`: the endpoint's Ktorfit signature returns a raw response (so the
     * original could read its status code), and a feature cannot name a Ktor type —
     * `feature/loan` must not depend on `core:network`. The gateway already turns a non-2xx into
     * `MutationResult.Failed`, which is the distinction the screen actually renders.
     */
    suspend fun submitAccountTransfer(request: AccountTransferRequest): MutationResult<Unit>

    /**
     * Propose a reschedule. `Unit` for the same reason as [submitAccountTransfer] — the endpoint
     * returns a raw Ktor response and a feature cannot name one.
     */
    suspend fun submitLoanReschedule(request: LoanRescheduleRequest): MutationResult<Unit>

    /**
     * Approve a proposed reschedule. [loanId] is the loan it belongs to: the wire command is keyed by
     * the reschedule alone, but its outcome — the reschedule's status and the loan's new schedule —
     * is recorded under the loan.
     */
    suspend fun approveLoanReschedule(
        loanId: Int,
        scheduleId: Int,
        request: LoanRescheduleApprovalRequest,
    ): MutationResult<Unit>

    /**
     * REJECT a proposed reschedule.
     *
     * The UI calls this "delete", which is what the original's icon and action were named — but
     * the wire command is `?command=reject` and the payload is a
     * [LoanRescheduleRejectionRequest], so the server records a rejection with a date and a user,
     * not a deletion. Named for what it does. See `loan-reschedule-delete-is-really-reject`.
     */
    suspend fun rejectLoanReschedule(
        loanId: Int,
        scheduleId: Int,
        request: LoanRescheduleRejectionRequest,
    ): MutationResult<Unit>
}
