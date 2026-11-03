/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable

/** Wire shape of a loan with associations. */
@Serializable
data class LoanWithAssociationsDto(
    /** The server id of this record. */
    val id: Long,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The record's Fineract status. */
    val status: LoanStatusDto? = null,
    // Absent on a GROUP loan (it carries `group` instead) — required, every group loan failed to
    // decode and its summary showed "Something went wrong" (device, 2026-10-25).
    /** The server id of the client. */
    val clientId: Long = 0,
    /** A group loan's owner (`{id, name, …}`) — null on a client loan. */
    val group: LoanGroupDto? = null,
    /** The client's account number. */
    val clientAccountNo: String? = null,
    /** The client's display name. */
    val clientName: String? = null,
    /** The server id of the loan product. */
    val loanProductId: Long,
    /** The name of the loan product. */
    val loanProductName: String? = null,
    /** The description of the loan product. */
    val loanProductDescription: String? = null,
    /** The server id of the loan officer. */
    val loanOfficerId: Long? = null,
    /** The loan officer's display name. */
    val loanOfficerName: String? = null,
    /** The loan type (individual, group or JLG). */
    val loanType: LoanTypeDto? = null,
    /** The currency the amounts are in. */
    val currency: LoanCurrencyDto? = null,
    /** The loan principal. */
    val principal: Double,
    /** The principal that was approved. */
    val approvedPrincipal: Double,
    /** The amount disbursed after the charges deducted at disbursement. */
    val netDisbursalAmount: Double,
    /** The loan term, in [termPeriodFrequencyType] units. */
    val termFrequency: Int,
    /** The number of installments. */
    val numberOfRepayments: Int,
    /** The nominal annual interest rate, in percent. */
    val annualInterestRate: Double,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: LoanTimelineDto? = null,
    /** The account's running totals. */
    val summary: LoanSummaryDto? = null,
    /** The repayment schedule. */
    val repaymentSchedule: LoanRepaymentScheduleDto? = null,
    /** The transactions. */
    val transactions: List<LoanTransactionDto>? = emptyList(),
    /** The charges. */
    val charges: List<LoanChargeDto>? = emptyList(),
    /** The notes on the loan. */
    val notes: List<LoanNoteDto>? = emptyList(),
    /** The loan's delinquency figures. */
    val delinquent: LoanDelinquentDto? = null,
    /** True when the disbursement date follows the meeting calendar. */
    val syncDisbursementWithMeeting: Boolean? = null,
    /** The fees charged at disbursement. */
    val feeChargesAtDisbursementCharged: Double? = null,
    /** The total overpaid. */
    val totalOverpaid: Double? = null,
    /** The client's loan cycle: how many loans they have had. */
    val loanCounter: Int? = null,
    /** How many loans of this product the client has had. */
    val loanProductCounter: Int? = null,
    /** True when the loan can be disbursed in several tranches. */
    val multiDisburseLoan: Boolean? = null,
    /** True when the loan can be disbursed now. */
    val canDisburse: Boolean? = null,
    /** True when an installment is overdue. */
    val inArrears: Boolean? = null,
    /** True when the loan is a non-performing asset. */
    val isNPA: Boolean? = null,
    /** The date the loan was overpaid. */
    val overpaidOnDate: List<Int>? = null,
    /** True when the loan amortises in equal installments. */
    val isEqualAmortization: Boolean? = null,
    /** True when interest is calculated for a partial first period. */
    val allowPartialPeriodInterestCalculation: Boolean? = null,
    /** True when interest is recognised from the disbursement date. */
    val interestRecognitionOnDisbursementDate: Boolean? = null,
    /** True when the loan takes a down payment. */
    val enableDownPayment: Boolean? = null,
    /** True when income can be capitalised into the principal. */
    val enableIncomeCapitalization: Boolean? = null,
    /** True when the loan supports a buy-down fee. */
    val enableBuyDownFee: Boolean? = null,
    /** True when delinquency is tracked per installment. */
    val enableInstallmentLevelDelinquency: Boolean? = null,
    /** True when interest is recalculated as repayments arrive. */
    val isInterestRecalculationEnabled: Boolean? = null,
    /** True when the loan has been charged off. */
    val chargedOff: Boolean? = null,
)

/** Wire shape of a loan status. */
@Serializable
data class LoanStatusDto(
    /** The server id of this record. */
    val id: Int,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,
    /** The human-readable label Fineract sends for this value. */
    val value: String? = null,
    /** True while the loan waits for approval. */
    val pendingApproval: Boolean = false,
    /** True while the loan waits to be disbursed. */
    val waitingForDisbursal: Boolean = false,
    /** True when the record is active. */
    val active: Boolean = false,
    /** True when the loan closed with every obligation met. */
    val closedObligationsMet: Boolean = false,
    /** True when the loan closed by being written off. */
    val closedWrittenOff: Boolean = false,
    /** True when the loan closed because it was rescheduled. */
    val closedRescheduled: Boolean = false,
    /** True when the account is closed. */
    val closed: Boolean = false,
    /** True when the loan has been overpaid. */
    val overpaid: Boolean = false,
)

/**
 * Wire shape of a loan type.
 *
 * @property code the Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
 *   currency code)
 * @property id the server id of this record
 * @property value the human-readable label Fineract sends for this value
 */
@Serializable
data class LoanTypeDto(val id: Int, val code: String? = null, val value: String? = null)

/** Wire shape of a loan currency. */
@Serializable
data class LoanCurrencyDto(
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,
    /** The display name. */
    val name: String? = null,
    /** The number of decimal places amounts in this currency carry. */
    val decimalPlaces: Int,
    /** The currency symbol to display (e.g. `$`). */
    val displaySymbol: String? = null,
    /** The currency's display label (name and symbol). */
    val displayLabel: String? = null,
)

/** Wire shape of a loan timeline. */
@Serializable
data class LoanTimelineDto(
    /** The date the record was submitted. */
    val submittedOnDate: List<Int>? = null,
    /** The date the record was approved. */
    val approvedOnDate: List<Int>? = null,
    /** The date the loan is expected to be disbursed. */
    val expectedDisbursementDate: List<Int>? = null,
    /** The date the loan was actually disbursed. */
    val actualDisbursementDate: List<Int>? = null,
    /** The date the loan is expected to mature. */
    val expectedMaturityDate: List<Int>? = null,
    /** The date the record was closed. */
    val closedOnDate: List<Int>? = null,
    /** The date the application was withdrawn. */
    val withdrawnOnDate: List<Int>? = null,
)

/** Wire shape of a loan summary. */
@Serializable
data class LoanSummaryDto(
    /** The server id of the loan. */
    val loanId: Int? = null,
    /** The currency the amounts are in. */
    val currency: LoanCurrencyDto? = null,
    /** The principal disbursed. */
    val principalDisbursed: Double? = null,
    /** The principal already paid. */
    val principalPaid: Double? = null,
    /** The principal waived. */
    val principalWaived: Double? = null,
    /** The principal written off. */
    val principalWrittenOff: Double? = null,
    /** The principal still outstanding. */
    val principalOutstanding: Double? = null,
    /** The principal overdue. */
    val principalOverdue: Double? = null,
    /** The interest charged. */
    val interestCharged: Double? = null,
    /** The interest already paid. */
    val interestPaid: Double? = null,
    /** The interest waived. */
    val interestWaived: Double? = null,
    /** The interest written off. */
    val interestWrittenOff: Double? = null,
    /** The interest still outstanding. */
    val interestOutstanding: Double? = null,
    /** The interest overdue. */
    val interestOverdue: Double? = null,
    /** The fees charged. */
    val feeChargesCharged: Double? = null,
    /** The fees charged at disbursement. */
    val feeChargesDueAtDisbursementCharged: Double? = null,
    /** The fees already paid. */
    val feeChargesPaid: Double? = null,
    /** The fees waived. */
    val feeChargesWaived: Double? = null,
    /** The fees written off. */
    val feeChargesWrittenOff: Double? = null,
    /** The fees still outstanding. */
    val feeChargesOutstanding: Double? = null,
    /** The fees overdue. */
    val feeChargesOverdue: Double? = null,
    /** The penalties charged. */
    val penaltyChargesCharged: Double? = null,
    /** The penalties already paid. */
    val penaltyChargesPaid: Double? = null,
    /** The penalties waived. */
    val penaltyChargesWaived: Double? = null,
    /** The penalties written off. */
    val penaltyChargesWrittenOff: Double? = null,
    /** The penalties still outstanding. */
    val penaltyChargesOutstanding: Double? = null,
    /** The penalties overdue. */
    val penaltyChargesOverdue: Double? = null,
    /** The total expected to be repaid. */
    val totalExpectedRepayment: Double? = null,
    /** The total repaid. */
    val totalRepayment: Double? = null,
    /** The expected total cost of the loan. */
    val totalExpectedCostOfLoan: Double? = null,
    /** The total cost of the loan: interest, fees and penalties. */
    val totalCostOfLoan: Double? = null,
    /** The total waived. */
    val totalWaived: Double? = null,
    /** The total written off. */
    val totalWrittenOff: Double? = null,
    /** The total amount outstanding. */
    val totalOutstanding: Double? = null,
    /** The total amount overdue. */
    val totalOverdue: Double? = null,
    /** The date the loan has been overdue since. */
    val overdueSinceDate: List<Int>? = null,
)

/** Wire shape of a loan repayment schedule. */
@Serializable
data class LoanRepaymentScheduleDto(
    /** The loan term, in days. */
    val loanTermInDays: Int,
    /** The total principal disbursed. */
    val totalPrincipalDisbursed: Double,
    /** The total amount outstanding. */
    val totalOutstanding: Double,
    /** The installments. */
    val periods: List<LoanPeriodDto>? = emptyList(),
)

/** Wire shape of a loan period. */
@Serializable
data class LoanPeriodDto(
    /** The installment number. */
    val period: Int? = null,
    /** The date the amount is due. */
    val dueDate: List<Int>? = null,
    /** The principal due. */
    val principalDue: Double? = null,
    /** The principal already paid. */
    val principalPaid: Double? = null,
    /** The principal still outstanding. */
    val principalOutstanding: Double? = null,
    /** The total due for the installment. */
    val totalDueForPeriod: Double? = null,
    /** The total paid for the installment. */
    val totalPaidForPeriod: Double? = null,
    /** The total outstanding for the installment. */
    val totalOutstandingForPeriod: Double? = null,
    /** True when the installment is fully paid. */
    val complete: Boolean? = null,
)

/** Wire shape of a loan transaction. */
@Serializable
data class LoanTransactionDto(
    /** The server id of this record. */
    val id: Long,
    /** The server id of the office. */
    val officeId: Long? = null,
    /** The name of the office. */
    val officeName: String? = null,
    /** The record's type. */
    val type: LoanTransactionTypeDto? = null,
    /** The date. */
    val date: List<Int>? = null,
    /** The currency the amounts are in. */
    val currency: LoanCurrencyDto? = null,
    /** The payment details of the transaction. */
    val paymentDetailData: PaymentDetailDataDto? = null,
    /** The amount, in the record's currency. */
    val amount: Double,
    /** The amount disbursed after the charges deducted at disbursement. */
    val netDisbursalAmount: Double? = null,
    /** The part of the transaction that went to principal. */
    val principalPortion: Double? = null,
    /** The part of the transaction that went to interest. */
    val interestPortion: Double? = null,
    /** The part of the transaction that went to fees. */
    val feeChargesPortion: Double? = null,
    /** The part of the transaction that went to penalties. */
    val penaltyChargesPortion: Double? = null,
    /** The part of the transaction that overpaid the loan. */
    val overpaymentPortion: Double? = null,
    /** The part of the transaction not yet recognised as income. */
    val unrecognizedIncomePortion: Double? = null,
    /** The loan balance outstanding after the transaction. */
    val outstandingLoanBalance: Double? = null,
    /** The date the record was submitted. */
    val submittedOnDate: List<Int>? = null,
    /** True when the transaction was reversed by hand. */
    val manuallyReversed: Boolean = false,
)

/** Wire shape of a loan transaction type. */
@Serializable
data class LoanTransactionTypeDto(
    /** The server id of this record. */
    val id: Int,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,
    /** The human-readable label Fineract sends for this value. */
    val value: String? = null,
    /** True for a disbursement transaction. */
    val disbursement: Boolean = false,
    /** True for a repayment taken at disbursement. */
    val repaymentAtDisbursement: Boolean = false,
    /** True for a repayment. */
    val repayment: Boolean = false,
    /** True for a merchant-issued refund. */
    val merchantIssuedRefund: Boolean = false,
    /** True for a payout refund. */
    val payoutRefund: Boolean = false,
    /** True for a goodwill credit. */
    val goodwillCredit: Boolean = false,
    /** True for an interest-payment waiver. */
    val interestPaymentWaiver: Boolean = false,
    /** True for a charge refund. */
    val chargeRefund: Boolean = false,
    /** True for a contra transaction. */
    val contra: Boolean = false,
    /** True for an interest waiver. */
    val waiveInterest: Boolean = false,
    /** True for a charge waiver. */
    val waiveCharges: Boolean = false,
    /** True for an accrual transaction. */
    val accrual: Boolean = false,
    /** True for a write-off. */
    val writeOff: Boolean = false,
    /** True for a recovery repayment. */
    val recoveryRepayment: Boolean = false,
    /** True for an initiate-transfer transaction. */
    val initiateTransfer: Boolean = false,
    /** True for an approve-transfer transaction. */
    val approveTransfer: Boolean = false,
    /** True for a withdraw-transfer transaction. */
    val withdrawTransfer: Boolean = false,
    /** True for a reject-transfer transaction. */
    val rejectTransfer: Boolean = false,
    /** True for a charge-payment transaction. */
    val chargePayment: Boolean = false,
    /** True for a refund. */
    val refund: Boolean = false,
    /** True for a refund on an active loan. */
    val refundForActiveLoans: Boolean = false,
    /** True for a credit-balance refund. */
    val creditBalanceRefund: Boolean = false,
    /** True for a charge adjustment. */
    val chargeAdjustment: Boolean = false,
    /** True for a chargeback. */
    val chargeback: Boolean = false,
    /** True for a charge-off transaction. */
    val chargeoff: Boolean = false,
    /** True for a down payment. */
    val downPayment: Boolean = false,
    /** True for a re-age transaction. */
    val reAge: Boolean = false,
    /** True for a re-amortise transaction. */
    val reAmortize: Boolean = false,
    /** True for an accrual-activity transaction. */
    val accrualActivity: Boolean = false,
    /** True for an interest refund. */
    val interestRefund: Boolean = false,
    /** True for an accrual adjustment. */
    val accrualAdjustment: Boolean = false,
    /** True for a capitalised-income transaction. */
    val capitalizedIncome: Boolean = false,
    /** True for a capitalised-income amortisation. */
    val capitalizedIncomeAmortization: Boolean = false,
    /** True for a capitalised-income adjustment. */
    val capitalizedIncomeAdjustment: Boolean = false,
    /** True for a capitalised-income amortisation adjustment. */
    val capitalizedIncomeAmortizationAdjustment: Boolean = false,
    /** True for a contract-termination transaction. */
    val contractTermination: Boolean = false,
    /** True for a buy-down fee transaction. */
    val buyDownFee: Boolean = false,
    /** True for a buy-down fee adjustment. */
    val buyDownFeeAdjustment: Boolean = false,
    /** True for a buy-down fee amortisation. */
    val buyDownFeeAmortization: Boolean = false,
    /** True for a buy-down fee amortisation adjustment. */
    val buyDownFeeAmortizationAdjustment: Boolean = false,
)

/** Wire shape of a payment detail data. */
@Serializable
data class PaymentDetailDataDto(
    /** The server id of this record. */
    val id: Long? = null,
    /** The payment type. */
    val paymentType: PaymentTypeDto? = null,
    /** The account number the payment was made from or to. */
    val accountNumber: String? = null,
    /** The cheque number of the payment. */
    val checkNumber: String? = null,
    /** The routing code of the payment. */
    val routingCode: String? = null,
    /** The receipt number of the payment. */
    val receiptNumber: String? = null,
    /** The bank number of the payment. */
    val bankNumber: String? = null,
)

/** Wire shape of a payment type. */
@Serializable
data class PaymentTypeDto(
    /** The server id of this record. */
    val id: Int? = null,
    /** The display name. */
    val name: String? = null,
)

/** Wire shape of a loan charge. */
@Serializable
data class LoanChargeDto(
    /** The server id of this record. */
    val id: Long,
    /** The display name. */
    val name: String? = null,
    /** The amount, in the record's currency. */
    val amount: Double,
    /** The amount already paid. */
    val amountPaid: Double,
    /** The amount still outstanding. */
    val amountOutstanding: Double,
    /** True when it has been paid. */
    val paid: Boolean = false,
    /** True when it has been waived. */
    val waived: Boolean = false,
)

/**
 * Wire shape of a loan note.
 *
 * @property createdOn when the record was created
 * @property id the server id of this record
 * @property note A free-text note
 */
@Serializable
data class LoanNoteDto(val id: Long, val note: String? = null, val createdOn: String? = null)

/** Wire shape of a loan delinquent. */
@Serializable
data class LoanDelinquentDto(
    /** The number of days past due. */
    val pastDueDays: Int,
    /** The amount in delinquency. */
    val delinquentAmount: Double,
    /** The date the next payment is due. */
    val nextPaymentDueDate: List<Int>? = null,
)

/** Wire shape of a note. */
data class NoteDto(
    /** The server id of this record. */
    val id: Long,
    /** A free-text note. */
    val note: String?,
    /** When the record was created. */
    val createdOn: String?,
)

/** Wire shape of a delinquent. */
data class DelinquentDto(
    /** The number of days past due. */
    val pastDueDays: Int,
    /** The amount in delinquency. */
    val delinquentAmount: Double,
    /** The date the next payment is due. */
    val nextPaymentDueDate: List<Int>?,
)

/**
 * The owner of a group loan, as `loans/{id}` returns it.
 *
 * @property id the server id of this record
 * @property name the display name
 */
@Serializable
data class LoanGroupDto(val id: Long? = null, val name: String? = null)
