/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoanWithAssociationsDto(
    val id: Long,
    val accountNo: String? = null,
    val status: LoanStatusDto? = null,
    val clientId: Long,
    val clientAccountNo: String? = null,
    val clientName: String? = null,
    val loanProductId: Long,
    val loanProductName: String? = null,
    val loanProductDescription: String? = null,
    val loanOfficerId: Long? = null,
    val loanOfficerName: String? = null,
    val loanType: LoanTypeDto? = null,
    val currency: LoanCurrencyDto? = null,
    val principal: Double,
    val approvedPrincipal: Double,
    val netDisbursalAmount: Double,
    val termFrequency: Int,
    val numberOfRepayments: Int,
    val annualInterestRate: Double,
    val timeline: LoanTimelineDto? = null,
    val summary: LoanSummaryDto? = null,
    val repaymentSchedule: LoanRepaymentScheduleDto? = null,
    val transactions: List<LoanTransactionDto>? = emptyList(),
    val charges: List<LoanChargeDto>? = emptyList(),
    val notes: List<LoanNoteDto>? = emptyList(),
    val delinquent: LoanDelinquentDto? = null,
    val syncDisbursementWithMeeting: Boolean? = null,
    val feeChargesAtDisbursementCharged: Double? = null,
    val totalOverpaid: Double? = null,
    val loanCounter: Int? = null,
    val loanProductCounter: Int? = null,
    val multiDisburseLoan: Boolean? = null,
    val canDisburse: Boolean? = null,
    val inArrears: Boolean? = null,
    val isNPA: Boolean? = null,
    val overpaidOnDate: List<Int>? = null,
    val isEqualAmortization: Boolean? = null,
    val allowPartialPeriodInterestCalculation: Boolean? = null,
    val interestRecognitionOnDisbursementDate: Boolean? = null,
    val enableDownPayment: Boolean? = null,
    val enableIncomeCapitalization: Boolean? = null,
    val enableBuyDownFee: Boolean? = null,
    val enableInstallmentLevelDelinquency: Boolean? = null,
    val isInterestRecalculationEnabled: Boolean? = null,
    val chargedOff: Boolean? = null,
)

@Serializable
data class LoanStatusDto(
    val id: Int,
    val code: String? = null,
    val value: String? = null,
    val pendingApproval: Boolean = false,
    val waitingForDisbursal: Boolean = false,
    val active: Boolean = false,
    val closedObligationsMet: Boolean = false,
    val closedWrittenOff: Boolean = false,
    val closedRescheduled: Boolean = false,
    val closed: Boolean = false,
    val overpaid: Boolean = false,
)

@Serializable
data class LoanTypeDto(val id: Int, val code: String? = null, val value: String? = null)

@Serializable
data class LoanCurrencyDto(val code: String? = null, val name: String? = null, val decimalPlaces: Int, val displaySymbol: String? = null, val displayLabel: String? = null)

@Serializable
data class LoanTimelineDto(
    val submittedOnDate: List<Int>? = null,
    val approvedOnDate: List<Int>? = null,
    val expectedDisbursementDate: List<Int>? = null,
    val actualDisbursementDate: List<Int>? = null,
    val expectedMaturityDate: List<Int>? = null,
    val closedOnDate: List<Int>? = null,
    val withdrawnOnDate: List<Int>? = null,
)

@Serializable
data class LoanSummaryDto(
    val loanId: Int? = null,
    val currency: LoanCurrencyDto? = null,
    val principalDisbursed: Double? = null,
    val principalPaid: Double? = null,
    val principalWaived: Double? = null,
    val principalWrittenOff: Double? = null,
    val principalOutstanding: Double? = null,
    val principalOverdue: Double? = null,
    val interestCharged: Double? = null,
    val interestPaid: Double? = null,
    val interestWaived: Double? = null,
    val interestWrittenOff: Double? = null,
    val interestOutstanding: Double? = null,
    val interestOverdue: Double? = null,
    val feeChargesCharged: Double? = null,
    val feeChargesDueAtDisbursementCharged: Double? = null,
    val feeChargesPaid: Double? = null,
    val feeChargesWaived: Double? = null,
    val feeChargesWrittenOff: Double? = null,
    val feeChargesOutstanding: Double? = null,
    val feeChargesOverdue: Double? = null,
    val penaltyChargesCharged: Double? = null,
    val penaltyChargesPaid: Double? = null,
    val penaltyChargesWaived: Double? = null,
    val penaltyChargesWrittenOff: Double? = null,
    val penaltyChargesOutstanding: Double? = null,
    val penaltyChargesOverdue: Double? = null,
    val totalExpectedRepayment: Double? = null,
    val totalRepayment: Double? = null,
    val totalExpectedCostOfLoan: Double? = null,
    val totalCostOfLoan: Double? = null,
    val totalWaived: Double? = null,
    val totalWrittenOff: Double? = null,
    val totalOutstanding: Double? = null,
    val totalOverdue: Double? = null,
    val overdueSinceDate: List<Int>? = null,
)

@Serializable
data class LoanRepaymentScheduleDto(val loanTermInDays: Int, val totalPrincipalDisbursed: Double, val totalOutstanding: Double, val periods: List<LoanPeriodDto>? = emptyList())

@Serializable
data class LoanPeriodDto(val period: Int? = null, val dueDate: List<Int>? = null, val principalDue: Double? = null, val principalPaid: Double? = null, val principalOutstanding: Double? = null, val totalDueForPeriod: Double? = null, val totalPaidForPeriod: Double? = null, val totalOutstandingForPeriod: Double? = null, val complete: Boolean? = null)

@Serializable
data class LoanTransactionDto(
    val id: Long,
    val officeId: Long? = null,
    val officeName: String? = null,
    val type: LoanTransactionTypeDto? = null,
    val date: List<Int>? = null,
    val currency: LoanCurrencyDto? = null,
    val paymentDetailData: PaymentDetailDataDto? = null,
    val amount: Double,
    val netDisbursalAmount: Double? = null,
    val principalPortion: Double? = null,
    val interestPortion: Double? = null,
    val feeChargesPortion: Double? = null,
    val penaltyChargesPortion: Double? = null,
    val overpaymentPortion: Double? = null,
    val unrecognizedIncomePortion: Double? = null,
    val outstandingLoanBalance: Double? = null,
    val submittedOnDate: List<Int>? = null,
    val manuallyReversed: Boolean = false,
)

@Serializable
data class LoanTransactionTypeDto(
    val id: Int,
    val code: String? = null,
    val value: String? = null,
    val disbursement: Boolean = false,
    val repaymentAtDisbursement: Boolean = false,
    val repayment: Boolean = false,
    val merchantIssuedRefund: Boolean = false,
    val payoutRefund: Boolean = false,
    val goodwillCredit: Boolean = false,
    val interestPaymentWaiver: Boolean = false,
    val chargeRefund: Boolean = false,
    val contra: Boolean = false,
    val waiveInterest: Boolean = false,
    val waiveCharges: Boolean = false,
    val accrual: Boolean = false,
    val writeOff: Boolean = false,
    val recoveryRepayment: Boolean = false,
    val initiateTransfer: Boolean = false,
    val approveTransfer: Boolean = false,
    val withdrawTransfer: Boolean = false,
    val rejectTransfer: Boolean = false,
    val chargePayment: Boolean = false,
    val refund: Boolean = false,
    val refundForActiveLoans: Boolean = false,
    val creditBalanceRefund: Boolean = false,
    val chargeAdjustment: Boolean = false,
    val chargeback: Boolean = false,
    val chargeoff: Boolean = false,
    val downPayment: Boolean = false,
    val reAge: Boolean = false,
    val reAmortize: Boolean = false,
    val accrualActivity: Boolean = false,
    val interestRefund: Boolean = false,
    val accrualAdjustment: Boolean = false,
    val capitalizedIncome: Boolean = false,
    val capitalizedIncomeAmortization: Boolean = false,
    val capitalizedIncomeAdjustment: Boolean = false,
    val capitalizedIncomeAmortizationAdjustment: Boolean = false,
    val contractTermination: Boolean = false,
    val buyDownFee: Boolean = false,
    val buyDownFeeAdjustment: Boolean = false,
    val buyDownFeeAmortization: Boolean = false,
    val buyDownFeeAmortizationAdjustment: Boolean = false,
)

@Serializable
data class PaymentDetailDataDto(
    val id: Long? = null,
    val paymentType: PaymentTypeDto? = null,
    val accountNumber: String? = null,
    val checkNumber: String? = null,
    val routingCode: String? = null,
    val receiptNumber: String? = null,
    val bankNumber: String? = null,
)

@Serializable
data class PaymentTypeDto(
    val id: Int? = null,
    val name: String? = null,
)

@Serializable
data class LoanChargeDto(val id: Long, val name: String? = null, val amount: Double, val amountPaid: Double, val amountOutstanding: Double, val paid: Boolean = false, val waived: Boolean = false)

@Serializable
data class LoanNoteDto(val id: Long, val note: String? = null, val createdOn: String? = null)

@Serializable
data class LoanDelinquentDto(val pastDueDays: Int, val delinquentAmount: Double, val nextPaymentDueDate: List<Int>? = null)
data class NoteDto(
    val id: Long,
    val note: String?,
    val createdOn: String?,
)

data class DelinquentDto(
    val pastDueDays: Int,
    val delinquentAmount: Double,
    val nextPaymentDueDate: List<Int>?,
)
