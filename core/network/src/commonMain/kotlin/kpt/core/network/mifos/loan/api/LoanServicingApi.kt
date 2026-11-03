/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse
import kpt.core.model.loan.AssignLoanOfficerRequestDto
import kpt.core.model.loan.AssignLoanOfficerResponseDto
import kpt.core.model.loan.CreateGuarantorResponseDto
import kpt.core.model.loan.GuarantorRequestDto
import kpt.core.model.loan.LoanChargeOffRequestDto
import kpt.core.model.loan.LoanChargeOffResponseDto
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleApprovalRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRejectionRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleTemplate
import kpt.core.network.mifos.loan.dto.GuarantorAccountTemplateDto
import kpt.core.network.mifos.loan.dto.GuarantorTemplateDto
import kpt.core.network.mifos.loan.dto.LoanChargeOffTemplateDto
import kpt.core.network.mifos.loan.dto.LoanDisburseRequestDto
import kpt.core.network.mifos.loan.dto.LoanDisburseResponseDto
import kpt.core.network.mifos.loan.dto.LoanDisburseTemplateDto
import kpt.core.network.mifos.loan.dto.LoanOfficerOptionsTemplateDto

/**
 * Guarantors, reschedules, officer assignment, disbursement and charge-off — split from [LoanApi],
 * which extends it.
 */
interface LoanServicingApi {

    /** Reads the charge-off template of a loan. */
    @GET("loans/{loanId}/transactions/template?command=charge-off")
    suspend fun getChargeOffTemplate(
        @Path("loanId") loanId: Int,
    ): LoanChargeOffTemplateDto

    /** Charges off a loan. */
    @POST("loans/{loanId}/transactions?command=charge-off")
    suspend fun chargeOff(
        @Path("loanId") loanId: Int,
        @Body loanChargeOffRequest: LoanChargeOffRequestDto,
    ): LoanChargeOffResponseDto

    /** Reads the guarantor template of a loan. */
    @GET("loans/{loanId}/guarantors/template")
    suspend fun getGuarantorTemplate(@Path("loanId") loanId: Int): GuarantorTemplateDto

    /** Adds a guarantor to a loan. */
    @POST("loans/{loanId}/guarantors")
    suspend fun createGuarantor(
        @Path("loanId") loanId: Int,
        @Body request: GuarantorRequestDto,
    ): CreateGuarantorResponseDto

    /** Reads the accounts a guarantor may pledge funds from. */
    @GET("loans/{loanId}/guarantors/accounts/template")
    suspend fun getGuarantorAccountTemplate(
        @Path("loanId") loanId: Int,
        @Query("clientId") clientId: Int,
    ): GuarantorAccountTemplateDto

    /** Reads the reschedule requests of a loan. */
    @GET("rescheduleloans")
    suspend fun getLoanReschedules(
        @Query("loanId") loanId: Int,
    ): List<LoanRescheduleResponse>

    /** Reads the reschedule template: the reschedule reasons. */
    @GET("rescheduleloans/template")
    suspend fun getLoanRescheduleTemplate(): LoanRescheduleTemplate

    /** Proposes a loan reschedule. */
    @POST("rescheduleloans")
    suspend fun submitLoanReschedule(
        @Body request: LoanRescheduleRequest,
    ): HttpResponse

    /** Approves a loan reschedule. */
    @POST("rescheduleloans/{scheduleId}?command=approve")
    suspend fun approveLoanReschedule(
        @Path("scheduleId") scheduleId: Int,
        @Body request: LoanRescheduleApprovalRequest,
    ): HttpResponse

    /** Rejects a loan reschedule. */
    @POST("rescheduleloans/{scheduleId}?command=reject")
    suspend fun rejectLoanReschedule(
        @Path("scheduleId") scheduleId: Int,
        @Body request: LoanRescheduleRejectionRequest,
    ): HttpResponse

    /** Reads the loan-officer template of a loan. */
    @GET("loans/{loanId}")
    suspend fun getLoanOfficerTemplate(
        @Path("loanId") loanId: Int,
        @Query("fields") fields: String = "id,loanOfficerId,loanOfficerOptions",
        @Query("staffInSelectedOfficeOnly") staffInSelectedOfficeOnly: Boolean = true,
        @Query("template") template: Boolean = true,
    ): LoanOfficerOptionsTemplateDto

    /** Assigns or changes a loan's officer. */
    @POST("loans/{loanId}")
    suspend fun assignLoanOfficer(
        @Path("loanId") loanId: Int,
        @Query("command") command: String = "assignLoanOfficer",
        @Body request: AssignLoanOfficerRequestDto,
    ): AssignLoanOfficerResponseDto

    /** Reads the disbursement template of a loan. */
    @GET("loans/{loanId}/transactions/template?command=disburse")
    suspend fun getDisburseTemplate(
        @Path("loanId") loanId: Int,
    ): LoanDisburseTemplateDto

    /** Disburses a loan. */
    @POST("loans/{loanId}?command=disburse")
    suspend fun disburse(
        @Path("loanId") loanId: Int,
        @Body loanDisburseRequest: LoanDisburseRequestDto,
    ): LoanDisburseResponseDto
}
