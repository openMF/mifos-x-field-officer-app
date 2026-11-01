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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.fieldofficer.infra.OkHttpResponse
import kpt.core.data.infra.testMutationGateway
import kpt.core.data.loan.impl.LoanCommandRepositoryImpl
import kpt.core.model.charge.Charges
import kpt.core.model.loan.AssignLoanOfficerRequestDto
import kpt.core.model.loan.AssignLoanOfficerResponseDto
import kpt.core.model.loan.CreateGuarantorResponseDto
import kpt.core.model.loan.GuarantorRequestDto
import kpt.core.model.loan.LoanChargeOffRequestDto
import kpt.core.model.loan.LoanChargeOffResponseDto
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.loan.LoanRepaymentResponse
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.loan.LoanTemplate
import kpt.core.model.loan.LoanTransactionTemplate
import kpt.core.model.loan.RejectLoanRequestDto
import kpt.core.model.loan.RejectLoanResponseDto
import kpt.core.model.objects.account.loan.LoanApproval
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseInput
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleApprovalRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRejectionRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleTemplate
import kpt.core.model.objects.account.loan.transfer.AccountTransferRequest
import kpt.core.model.objects.account.loan.transfer.AccountTransferTemplate
import kpt.core.model.objects.clients.Page
import kpt.core.model.objects.organisations.LoanProducts
import kpt.core.model.objects.payloads.GroupLoanPayload
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.objects.template.loan.GroupLoanTemplate
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.loan.api.LoanApi
import kpt.core.network.mifos.loan.dto.GuarantorAccountTemplateDto
import kpt.core.network.mifos.loan.dto.GuarantorTemplateDto
import kpt.core.network.mifos.loan.dto.LoanChargeOffTemplateDto
import kpt.core.network.mifos.loan.dto.LoanDisburseRequestDto
import kpt.core.network.mifos.loan.dto.LoanDisburseResponseDto
import kpt.core.network.mifos.loan.dto.LoanDisburseTemplateDto
import kpt.core.network.mifos.loan.dto.LoanOfficerOptionsTemplateDto
import kpt.core.network.mifos.loan.dto.LoanWithAssociationsDto
import kpt.core.network.mifos.loan.dto.LoansPayload
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kpt.core.model.loan.Loan as LoanModel

/**
 * The loan commands over REAL Store5 read stores wired as `core/store` wires them, each over an
 * in-memory "table". A command's local side is the server record ingested through those stores, so
 * every assertion reads the TABLE the profile observes — nothing re-reads after the write.
 */
class LoanCommandRepositoryTest {

    /** The server. Its loan changes only when an endpoint below says so, as Fineract's would. */
    private var online = true
    private var ingestFails = false
    private val serverLoans = mutableMapOf(1 to loan(1, principal = 5_000.0), 2 to loan(2, principal = 800.0))
    private val serverReschedules = mutableMapOf(1 to listOf(LoanRescheduleResponse(id = 30, loanId = 1)))
    private val sent = mutableListOf<String>()

    /** `LoanWithAssociations` and the reschedules cache row. */
    private val loans = MutableStateFlow<Map<Int, LoanWithAssociations>>(emptyMap())
    private val reschedules = MutableStateFlow<Map<Int, List<LoanRescheduleResponse>>>(emptyMap())

    // ---- core/store, as provided in LoanStore.kt / LoanKeyedReadStores.kt ----

    private val loanStore = StoreBuilder.from(
        fetcher = Fetcher.of { id: Int ->
            check(online && !ingestFails) { "offline" }
            serverLoans.getValue(id)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: Int -> loans.map { it[id] } },
            writer = { id: Int, loan: LoanWithAssociations -> loans.value += id to loan },
        ),
    ).build()

    private val reschedulesStore = StoreBuilder.from(
        fetcher = Fetcher.of { loanId: Int ->
            check(online) { "offline" }
            serverReschedules[loanId].orEmpty()
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { loanId: Int -> reschedules.map { it[loanId] } },
            writer = { loanId: Int, rows: List<LoanRescheduleResponse> -> reschedules.value += loanId to rows },
        ),
    ).build()

    // ---- core/network ----

    private val api = object : LoanApi {
        override suspend fun approveLoanApplication(loanId: Int, loanApproval: LoanApproval?): GenericResponse {
            sent += "approve"
            serverLoans[loanId] = serverLoans.getValue(loanId).copy(approvedPrincipal = 4_500.0)
            return GenericResponse()
        }

        override suspend fun disburse(
            loanId: Int,
            loanDisburseRequest: LoanDisburseRequestDto,
        ): LoanDisburseResponseDto {
            sent += "disburse"
            serverLoans[loanId] = serverLoans.getValue(loanId).copy(loanOfficerName = "disbursed")
            return LoanDisburseResponseDto(1, 1, loanId.toLong(), 50, 0)
        }

        override suspend fun submitPayment(
            loanId: Int,
            loanRepaymentRequest: LoanRepaymentRequest?,
        ): LoanRepaymentResponse {
            sent += "repay"
            // Fineract computes the new outstanding; the request only carries what was paid.
            serverLoans[loanId] = serverLoans.getValue(loanId).copy(principal = 4_000.0)
            return LoanRepaymentResponse(loanId = loanId, resourceId = 9_001)
        }

        override suspend fun createGroupLoansAccount(loansPayload: GroupLoanPayload?): SaveResponse {
            serverLoans[77] = loan(77, principal = 1_200.0)
            return SaveResponse(resourceId = 77)
        }

        override suspend fun createLoansAccount(loansPayload: LoansPayload?): SaveResponse {
            serverLoans[78] = loan(78, principal = 300.0)
            return SaveResponse(resourceId = 78)
        }

        override suspend fun approveLoanReschedule(
            scheduleId: Int,
            request: LoanRescheduleApprovalRequest,
        ): HttpResponse {
            serverReschedules[1] = listOf(
                LoanRescheduleResponse(
                    id = scheduleId,
                    loanId = 1,
                    rescheduleFromDate = listOf(2026, 1, 1),
                ),
            )
            serverLoans[1] = serverLoans.getValue(1).copy(numberOfRepayments = 24)
            return OkHttpResponse
        }

        override suspend fun submitAccountTransfer(request: AccountTransferRequest): HttpResponse {
            serverLoans[request.fromAccountId] = serverLoans.getValue(request.fromAccountId).copy(principal = 0.0)
            return OkHttpResponse
        }

        override suspend fun getLoanByIdWithAllAssociations(loanId: Int): LoanWithAssociationsDto = unused()
        override suspend fun getLoanRepaymentTemplate(loanId: Int): LoanRepaymentTemplate = unused()
        override suspend fun getChargeOffTemplate(loanId: Int): LoanChargeOffTemplateDto = unused()
        override suspend fun chargeOff(
            loanId: Int,
            loanChargeOffRequest: LoanChargeOffRequestDto,
        ): LoanChargeOffResponseDto = unused()
        override suspend fun rejectLoan(loanId: Int, request: RejectLoanRequestDto): RejectLoanResponseDto = unused()
        override suspend fun getLoanRepaymentSchedule(loanId: Int): LoanWithAssociationsDto = unused()
        override suspend fun getLoanWithTransactions(loanId: Int): LoanWithAssociationsDto = unused()
        override suspend fun getGuarantorTemplate(loanId: Int): GuarantorTemplateDto = unused()
        override suspend fun createGuarantor(
            loanId: Int,
            request: GuarantorRequestDto,
        ): CreateGuarantorResponseDto = unused()
        override suspend fun getGuarantorAccountTemplate(
            loanId: Int,
            clientId: Int,
        ): GuarantorAccountTemplateDto = unused()
        override suspend fun getAllLoans(): List<LoanProducts> = unused()
        override suspend fun calculateLoanSchedule(loansPayload: LoansPayload?): RepaymentSchedule = unused()
        override suspend fun getLoansAccountTemplate(clientId: Int, productId: Int): LoanTemplate = unused()
        override suspend fun getLoanTransactionTemplate(
            loanId: Int,
            command: String?,
        ): LoanTransactionTemplate = unused()
        override suspend fun getGroupLoansAccountTemplate(groupId: Int, productId: Int): GroupLoanTemplate = unused()
        override suspend fun getListOfLoanCharges(loanId: Int): List<Charges> = unused()
        override suspend fun getListOfCharges(clientId: Int): Page<Charges> = unused()
        override suspend fun getAccountTransferTemplate(
            fromClientId: Int,
            fromAccountType: Int,
            fromAccountId: Int,
            fromOfficeId: Int?,
            toOfficeId: Int?,
            toClientId: Int?,
            toAccountType: Int?,
            toAccountId: Int?,
        ): AccountTransferTemplate = unused()
        override suspend fun getLoanReschedules(loanId: Int): List<LoanRescheduleResponse> = unused()
        override suspend fun getLoanRescheduleTemplate(): LoanRescheduleTemplate = unused()
        override suspend fun submitLoanReschedule(request: LoanRescheduleRequest): HttpResponse = unused()
        override suspend fun rejectLoanReschedule(
            scheduleId: Int,
            request: LoanRescheduleRejectionRequest,
        ): HttpResponse = unused()
        override suspend fun getLoanOfficerTemplate(
            loanId: Int,
            fields: String,
            staffInSelectedOfficeOnly: Boolean,
            template: Boolean,
        ): LoanOfficerOptionsTemplateDto = unused()
        override suspend fun assignLoanOfficer(
            loanId: Int,
            command: String,
            request: AssignLoanOfficerRequestDto,
        ): AssignLoanOfficerResponseDto = unused()
        override suspend fun getDisburseTemplate(loanId: Int): LoanDisburseTemplateDto = unused()

        private fun unused(): Nothing = error("not exercised; reads go through the stores")
    }

    private fun commands(isOnline: Boolean = true) =
        LoanCommandRepositoryImpl(api, testMutationGateway(isOnline = isOnline), loanStore, reschedulesStore)

    // ---- each write lands in the table the read store observes ----

    @Test
    fun anApprovalIngestsTheServersRecordIntoTheLoanTable() = runTest {
        assertIs<MutationResult.Applied<GenericResponse>>(commands().approveLoanApplication(1, approval))
        assertEquals(4_500.0, loans.value.getValue(1).approvedPrincipal)
    }

    @Test
    fun aRepaymentLandsTheServerComputedBalance() = runTest {
        val result = commands().submitPayment(1, LoanRepaymentRequest(transactionAmount = "1000"))

        assertEquals(9_001, assertIs<MutationResult.Applied<LoanRepaymentResponse>>(result).value.resourceId)
        assertEquals(4_000.0, loans.value.getValue(1).principal)
    }

    @Test
    fun aDisbursementIngestsTheDisbursedLoan() = runTest {
        assertIs<MutationResult.Applied<Unit>>(commands().disburse(1, disbursement))
        assertEquals("disbursed", loans.value.getValue(1).loanOfficerName)
    }

    @Test
    fun aCreatedGroupLoanAnswersFineractsResourceIdAndIsIngestedUnderIt() = runTest {
        val result = commands().createGroupLoanAccount(GroupLoanPayload(groupId = 3))

        // Decoded straight into a `Loan`, Fineract's `{"resourceId": N}` used to yield a null id.
        assertEquals(77, assertIs<MutationResult.Applied<LoanModel>>(result).value.id)
        assertEquals(1_200.0, loans.value.getValue(77).principal)
    }

    @Test
    fun approvingARescheduleRecordsBothTheListAndTheLoansNewSchedule() = runTest {
        commands().approveLoanReschedule(
            loanId = 1,
            scheduleId = 30,
            request = LoanRescheduleApprovalRequest("1 Jan 2026", "dd MMMM yyyy", "en"),
        )

        assertEquals(listOf(2026, 1, 1), reschedules.value.getValue(1).single().rescheduleFromDate)
        assertEquals(24, loans.value.getValue(1).numberOfRepayments)
    }

    @Test
    fun aTransferIngestsTheLoanEndOnly() = runTest {
        // From loan 2 (type 1) to a savings account (type 2) — the savings side is not this table's.
        commands().submitAccountTransfer(transfer(fromAccountId = 2, toAccountType = 2, toAccountId = 2))

        assertEquals(0.0, loans.value.getValue(2).principal)
        assertEquals(setOf(2), loans.value.keys)
    }

    // ---- the gateway's guarantees ----

    @Test
    fun offlineEveryWriteIsBlockedAndWritesNothing() = runTest {
        online = false
        val offline = commands(isOnline = false)

        assertIs<MutationResult.Blocked>(offline.approveLoanApplication(1, approval))
        assertIs<MutationResult.Blocked>(offline.disburse(1, disbursement))
        assertIs<MutationResult.Blocked>(offline.submitPayment(1, LoanRepaymentRequest()))

        assertEquals(emptyList(), sent)
        assertEquals(emptyMap(), loans.value)
    }

    @Test
    fun aFailedIngestAfterTheServerCommittedStillReportsApplied() = runTest {
        // The money moved. Reporting `Failed` here would have the officer submit the repayment again.
        ingestFails = true

        assertIs<MutationResult.Applied<LoanRepaymentResponse>>(commands().submitPayment(1, LoanRepaymentRequest()))
        assertNull(loans.value[1])
    }

    private companion object {
        fun loan(id: Int, principal: Double) = LoanWithAssociations(id = id, principal = principal)

        val approval = LoanApproval(approvedOnDate = "1 Jan 2026", locale = "en", dateFormat = "dd MMMM yyyy")

        val disbursement = LoanDisburseInput(
            actualDisbursementDate = "1 Jan 2026",
            transactionAmount = 5_000.0,
            locale = "en",
            dateFormat = "dd MMMM yyyy",
        )

        fun transfer(fromAccountId: Int, toAccountType: Int, toAccountId: Int) = AccountTransferRequest(
            fromOfficeId = 1,
            fromClientId = 1,
            fromAccountType = 1,
            fromAccountId = fromAccountId,
            toOfficeId = 1,
            toClientId = 1,
            toAccountType = toAccountType,
            toAccountId = toAccountId,
            transferDate = "1 Jan 2026",
            transferAmount = 800.0,
            transferDescription = "close",
            dateFormat = "dd MMMM yyyy",
            locale = "en",
        )
    }
}
