/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.loan

import io.ktor.client.statement.HttpResponse
import kpt.core.base.store.mutation.MutationResult
import kpt.core.database.loan.entity.Loan
import kpt.core.database.loan.entity.LoanRepaymentRequestEntity
import kpt.core.database.loan.entity.LoanRepaymentResponseEntity
import kpt.core.model.objects.account.loan.LoanApproval
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleApprovalRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRejectionRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRequest
import kpt.core.model.objects.account.loan.transfer.AccountTransferRequest
import kpt.core.model.objects.payloads.GroupLoanPayload
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.loan.dto.AssignLoanOfficerRequestDto
import kpt.core.network.mifos.loan.dto.AssignLoanOfficerResponseDto
import kpt.core.network.mifos.loan.dto.CreateGuarantorResponseDto
import kpt.core.network.mifos.loan.dto.GuarantorRequestDto
import kpt.core.network.mifos.loan.dto.LoanChargeOffRequestDto
import kpt.core.network.mifos.loan.dto.LoanChargeOffResponseDto
import kpt.core.network.mifos.loan.dto.LoanDisburseRequestDto
import kpt.core.network.mifos.loan.dto.LoanDisburseResponseDto
import kpt.core.network.mifos.loan.dto.LoansPayload
import kpt.core.network.mifos.loan.dto.RejectLoanRequestDto
import kpt.core.network.mifos.loan.dto.RejectLoanResponseDto
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
 * charge-off, reschedule, officer reassignment — is [OnlineRequired][kpt.core.base.store.mutation.MutationPolicy.OnlineRequired]:
 * it awaits the server and writes NOTHING offline, surfacing `Blocked(OFFLINE)` instead. An
 * optimistic approval is a lie the officer would act on — telling them a loan is approved when the
 * server never saw it is worse than telling them to find signal.
 *
 * Offline CAPTURE of a repayment is a different operation and lives on
 * [kpt.core.data.sync.OfflineQueueRepository]: it queues the intent locally and is explicit that
 * nothing has been sent. [submitPayment] here is the online, immediate path.
 *
 * Commands take the API's request DTO rather than a parallel domain type. A command payload has no
 * cached domain form — it exists only to be sent — so a second shape would be a copy with nothing
 * to keep it honest.
 */
interface LoanCommandRepository {

    suspend fun approveLoanApplication(loanId: Int, approval: LoanApproval): MutationResult<GenericResponse>

    suspend fun rejectLoan(loanId: Int, request: RejectLoanRequestDto): MutationResult<RejectLoanResponseDto>

    suspend fun disburse(loanId: Int, request: LoanDisburseRequestDto): MutationResult<LoanDisburseResponseDto>

    suspend fun submitPayment(
        loanId: Int,
        request: LoanRepaymentRequestEntity,
    ): MutationResult<LoanRepaymentResponseEntity>

    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>

    suspend fun assignLoanOfficer(loanId: Int, request: AssignLoanOfficerRequestDto): MutationResult<AssignLoanOfficerResponseDto>

    suspend fun createLoanAccount(payload: LoansPayload): MutationResult<HttpResponse>

    suspend fun createGroupLoanAccount(payload: GroupLoanPayload): MutationResult<Loan>

    suspend fun calculateLoanSchedule(payload: LoansPayload): MutationResult<HttpResponse>

    suspend fun createGuarantor(loanId: Int, request: GuarantorRequestDto): MutationResult<CreateGuarantorResponseDto>

    suspend fun submitAccountTransfer(request: AccountTransferRequest): MutationResult<HttpResponse>

    suspend fun submitLoanReschedule(request: LoanRescheduleRequest): MutationResult<HttpResponse>

    suspend fun approveLoanReschedule(scheduleId: Int, request: LoanRescheduleApprovalRequest): MutationResult<HttpResponse>

    suspend fun rejectLoanReschedule(scheduleId: Int, request: LoanRescheduleRejectionRequest): MutationResult<HttpResponse>
}
