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
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.model.charge.Charges
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.loan.LoanRepaymentResponse
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.loan.LoanTemplate
import kpt.core.model.loan.LoanTransactionTemplate
import kpt.core.model.loan.RejectLoanRequestDto
import kpt.core.model.loan.RejectLoanResponseDto
import kpt.core.model.objects.account.loan.LoanApproval
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.transfer.AccountTransferRequest
import kpt.core.model.objects.account.loan.transfer.AccountTransferTemplate
import kpt.core.model.objects.clients.Page
import kpt.core.model.objects.organisations.LoanProducts
import kpt.core.model.objects.payloads.GroupLoanPayload
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.objects.template.loan.GroupLoanTemplate
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.loan.dto.LoanWithAssociationsDto
import kpt.core.network.mifos.loan.dto.LoansPayload

/**
 * @author fomenkoo
 */
@ApiBinding("mifos")
interface LoanApi : LoanServicingApi {

    @GET("loans/{loanId}?associations=all&exclude=guarantors,futureSchedule")
    suspend fun getLoanByIdWithAllAssociations(@Path("loanId") loanId: Int): LoanWithAssociationsDto

    @GET("loans/{loanId}/transactions/template?command=repayment")
    suspend fun getLoanRepaymentTemplate(@Path("loanId") loanId: Int): LoanRepaymentTemplate

    //  Mandatory Fields
    //  1. String approvedOnDate
    @POST("loans/{loanId}?command=approve")
    suspend fun approveLoanApplication(
        @Path("loanId") loanId: Int,
        @Body loanApproval: LoanApproval?,
    ): GenericResponse

    @POST("loans/{loanId}/transactions?command=repayment")
    suspend fun submitPayment(
        @Path("loanId") loanId: Int,
        @Body loanRepaymentRequest: LoanRepaymentRequest?,
    ): LoanRepaymentResponse

    @POST("loans/{loanId}?command=reject")
    suspend fun rejectLoan(
        @Path("loanId") loanId: Int,
        @Body request: RejectLoanRequestDto,
    ): RejectLoanResponseDto

    @GET("loans/{loanId}?associations=repaymentSchedule")
    suspend fun getLoanRepaymentSchedule(@Path("loanId") loanId: Int): LoanWithAssociationsDto

    @GET("loans/{loanId}?associations=transactions")
    suspend fun getLoanWithTransactions(@Path("loanId") loanId: Int): LoanWithAssociationsDto

    @GET("loanproducts")
    suspend fun getAllLoans(): List<LoanProducts>

    /**
     * Typed as [SaveResponse], not `HttpResponse`: Fineract answers `{"loanId": N, "resourceId": N,
     * ...}`, and the new id is what the command repository ingests the created loan by.
     */
    @POST("loans")
    suspend fun createLoansAccount(@Body loansPayload: LoansPayload?): SaveResponse

    /**
     * Calculate loan repayment schedule without creating the loan.
     * Used to preview the schedule before submitting the loan application.
     */
    @POST("loans?command=calculateLoanSchedule")
    suspend fun calculateLoanSchedule(@Body loansPayload: LoansPayload?): RepaymentSchedule

    @GET("loans/template?templateType=individual")
    suspend fun getLoansAccountTemplate(
        @Query("clientId") clientId: Int,
        @Query("productId") productId: Int,
    ): LoanTemplate

    /**
     * For fetching any type of loan template.
     * Example:
     * 1. repayment
     * 2. disburse
     * 3. waiver
     * 4. refundbycash
     * 5. foreclosure
     *
     * @param loanId Loan Id
     * @param command Template Type
     * @return
     */
    @GET("loans/{loanId}/transactions/template")
    suspend fun getLoanTransactionTemplate(
        @Path("loanId") loanId: Int,
        @Query("command") command: String?,
    ): LoanTransactionTemplate

    /**
     * [SaveResponse], not `Loan`: the body is Fineract's command result, whose new id is
     * `resourceId`. Decoded as a `Loan` it had no `id` key, so the created loan's id was always null.
     */
    @POST("loans")
    suspend fun createGroupLoansAccount(@Body loansPayload: GroupLoanPayload?): SaveResponse

    @GET("loans/template?templateType=group")
    suspend fun getGroupLoansAccountTemplate(
        @Query("groupId") groupId: Int,
        @Query("productId") productId: Int,
    ): GroupLoanTemplate

    @GET("loans/{loanId}/charges")
    suspend fun getListOfLoanCharges(@Path("loanId") loanId: Int): List<Charges>

    @GET("clients/{clientId}/charges")
    suspend fun getListOfCharges(@Path("clientId") clientId: Int): Page<Charges>

    /**
     * Account Transfer API Endpoints
     */

    /**
     * Retrieve account transfer template for populating UI dropdowns
     *
     * @param fromOfficeId Source office ID
     * @param fromClientId Source client ID
     * @param fromAccountType Source account type ID
     * @param fromAccountId Source account ID
     * @return AccountTransferTemplate with available options
     */
    @GET("accounttransfers/template")
    suspend fun getAccountTransferTemplate(
        @Query("fromClientId") fromClientId: Int,
        @Query("fromAccountType") fromAccountType: Int,
        @Query("fromAccountId") fromAccountId: Int,
        @Query("fromOfficeId") fromOfficeId: Int? = null,
        @Query("toOfficeId") toOfficeId: Int? = null,
        @Query("toClientId") toClientId: Int? = null,
        @Query("toAccountType") toAccountType: Int? = null,
        @Query("toAccountId") toAccountId: Int? = null,
    ): AccountTransferTemplate

    /**
     * Submit an account transfer
     *
     * @param request Account transfer request payload
     * @return HttpResponse to check status and handle error/success appropriately
     */
    @POST("accounttransfers")
    suspend fun submitAccountTransfer(
        @Body request: AccountTransferRequest,
    ): HttpResponse

    /**
     * Loan Reschedule API Endpoints
     */
}
