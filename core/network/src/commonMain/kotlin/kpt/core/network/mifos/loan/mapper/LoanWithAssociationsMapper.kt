/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.mapper

import kpt.core.model.objects.account.loan.Currency
import kpt.core.model.objects.account.loan.PaymentDetailData
import kpt.core.model.objects.account.loan.PaymentType
import kpt.core.model.objects.account.loan.Period
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.Transaction
import kpt.core.model.objects.account.loan.Type
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanAccountSummary
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanStatus
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanTimeline
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanType
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.loanWithAssociations.SavingAccountCurrency
import kpt.core.network.mifos.loan.dto.LoanCurrencyDto
import kpt.core.network.mifos.loan.dto.LoanRepaymentScheduleDto
import kpt.core.network.mifos.loan.dto.LoanStatusDto
import kpt.core.network.mifos.loan.dto.LoanSummaryDto
import kpt.core.network.mifos.loan.dto.LoanTimelineDto
import kpt.core.network.mifos.loan.dto.LoanTransactionDto
import kpt.core.network.mifos.loan.dto.LoanTypeDto
import kpt.core.network.mifos.loan.dto.LoanWithAssociationsDto

fun LoanWithAssociationsDto.toDomain(): LoanWithAssociations = LoanWithAssociations(
    id = this.id.toInt(),
    accountNo = this.accountNo,
    status = this.status?.toDomain(),
    clientId = this.clientId.toInt(),
    clientName = this.clientName,
    loanProductId = this.loanProductId.toInt(),
    loanProductName = this.loanProductName,
    loanProductDescription = this.loanProductDescription,
    loanOfficerId = this.loanOfficerId?.toInt(),
    loanOfficerName = this.loanOfficerName,
    loanType = this.loanType?.toDomain(),
    currency = this.currency?.toDomain(),
    principal = this.principal,
    approvedPrincipal = this.approvedPrincipal,
    termFrequency = this.termFrequency,
    numberOfRepayments = this.numberOfRepayments,
    annualInterestRate = this.annualInterestRate,
    timeline = this.timeline?.toDomain(),
    summary = this.summary?.toDomain(),
    repaymentSchedule = this.repaymentSchedule?.toDomain(),
    transactions = this.transactions?.map { it.toDomain() },
    syncDisbursementWithMeeting = this.syncDisbursementWithMeeting,
    feeChargesAtDisbursementCharged = this.feeChargesAtDisbursementCharged,
    totalOverpaid = this.totalOverpaid,
    loanCounter = this.loanCounter,
    loanProductCounter = this.loanProductCounter,
    multiDisburseLoan = this.multiDisburseLoan,
    canDisburse = this.canDisburse,
    inArrears = this.inArrears,
    isNPA = this.isNPA,
    overpaidOnDate = this.overpaidOnDate,
    isEqualAmortization = this.isEqualAmortization,
    allowPartialPeriodInterestCalculation = this.allowPartialPeriodInterestCalculation,
    interestRecognitionOnDisbursementDate = this.interestRecognitionOnDisbursementDate,
    enableDownPayment = this.enableDownPayment,
    enableIncomeCapitalization = this.enableIncomeCapitalization,
    enableBuyDownFee = this.enableBuyDownFee,
    enableInstallmentLevelDelinquency = this.enableInstallmentLevelDelinquency,
    isInterestRecalculationEnabled = this.isInterestRecalculationEnabled,
    chargedOff = this.chargedOff,
)

fun LoanStatusDto.toDomain() = LoanStatus(
    id = this.id,
    code = this.code,
    value = this.value,
    pendingApproval = this.pendingApproval,
    waitingForDisbursal = this.waitingForDisbursal,
    active = this.active,
    closedObligationsMet = this.closedObligationsMet,
    closedWrittenOff = this.closedWrittenOff,
    closedRescheduled = this.closedRescheduled,
    closed = this.closed,
    overpaid = this.overpaid,
)

fun LoanTypeDto.toDomain() = LoanType(
    id = this.id,
    code = this.code,
    value = this.value,
)

fun LoanCurrencyDto.toDomain() = SavingAccountCurrency(
    code = this.code,
    name = this.name,
    decimalPlaces = this.decimalPlaces,
    displaySymbol = this.displaySymbol,
    displayLabel = this.displayLabel,
)

fun LoanTimelineDto.toDomain() = LoanTimeline(
    submittedOnDate = this.submittedOnDate,
    approvedOnDate = this.approvedOnDate,
    expectedDisbursementDate = this.expectedDisbursementDate,
    actualDisbursementDate = this.actualDisbursementDate,
    expectedMaturityDate = this.expectedMaturityDate,
    closedOnDate = this.closedOnDate,
    withdrawnOnDate = this.withdrawnOnDate,
)

fun LoanSummaryDto.toDomain() = LoanAccountSummary(
    loanId = this.loanId,
    currency = this.currency?.toDomain(),
    principalDisbursed = this.principalDisbursed,
    principalPaid = this.principalPaid,
    principalWaived = this.principalWaived,
    principalWrittenOff = this.principalWrittenOff,
    principalOutstanding = this.principalOutstanding,
    principalOverdue = this.principalOverdue,
    interestCharged = this.interestCharged,
    interestPaid = this.interestPaid,
    interestWaived = this.interestWaived,
    interestWrittenOff = this.interestWrittenOff,
    interestOutstanding = this.interestOutstanding,
    interestOverdue = this.interestOverdue,
    feeChargesCharged = this.feeChargesCharged,
    feeChargesDueAtDisbursementCharged = this.feeChargesDueAtDisbursementCharged,
    feeChargesPaid = this.feeChargesPaid,
    feeChargesWaived = this.feeChargesWaived,
    feeChargesWrittenOff = this.feeChargesWrittenOff,
    feeChargesOutstanding = this.feeChargesOutstanding,
    feeChargesOverdue = this.feeChargesOverdue,
    penaltyChargesCharged = this.penaltyChargesCharged,
    penaltyChargesPaid = this.penaltyChargesPaid,
    penaltyChargesWaived = this.penaltyChargesWaived,
    penaltyChargesWrittenOff = this.penaltyChargesWrittenOff,
    penaltyChargesOutstanding = this.penaltyChargesOutstanding,
    penaltyChargesOverdue = this.penaltyChargesOverdue,
    totalExpectedRepayment = this.totalExpectedRepayment,
    totalRepayment = this.totalRepayment,
    totalExpectedCostOfLoan = this.totalExpectedCostOfLoan,
    totalCostOfLoan = this.totalCostOfLoan,
    totalWaived = this.totalWaived,
    totalWrittenOff = this.totalWrittenOff,
    totalOutstanding = this.totalOutstanding,
    totalOverdue = this.totalOverdue,
    overdueSinceDate = this.overdueSinceDate,
)

fun LoanRepaymentScheduleDto.toDomain() = RepaymentSchedule(
    loanTermInDays = this.loanTermInDays,
    totalPrincipalDisbursed = this.totalPrincipalDisbursed,
    totalOutstanding = this.totalOutstanding,
    periods = this.periods?.map {
        Period(
            period = it.period,
            dueDate = it.dueDate,
            principalDue = it.principalDue,
            principalPaid = it.principalPaid,
            principalOutstanding = it.principalOutstanding,
            totalDueForPeriod = it.totalDueForPeriod,
            totalPaidForPeriod = it.totalPaidForPeriod,
            totalOutstandingForPeriod = it.totalOutstandingForPeriod,
            complete = it.complete,
        )
    },
)

fun LoanTransactionDto.toDomain() = Transaction(
    id = this.id.toInt(),
    officeId = this.officeId?.toInt(),
    officeName = this.officeName,
    date = this.date?.let { ArrayList(it) } ?: ArrayList(),
    currency = this.currency?.let {
        Currency(
            code = it.code,
            name = it.name,
            decimalPlaces = it.decimalPlaces,
            displaySymbol = it.displaySymbol,
            displayLabel = it.displayLabel,
        )
    },
    paymentDetailData = this.paymentDetailData?.let {
        PaymentDetailData(
            id = it.id?.toInt(),
            paymentType = it.paymentType?.let { pt ->
                PaymentType(id = pt.id, name = pt.name)
            },
            accountNumber = it.accountNumber,
            checkNumber = it.checkNumber,
            routingCode = it.routingCode,
            receiptNumber = it.receiptNumber,
            bankNumber = it.bankNumber,
        )
    },
    amount = this.amount,
    netDisbursalAmount = this.netDisbursalAmount,
    principalPortion = this.principalPortion,
    interestPortion = this.interestPortion,
    feeChargesPortion = this.feeChargesPortion,
    penaltyChargesPortion = this.penaltyChargesPortion,
    overpaymentPortion = this.overpaymentPortion,
    unrecognizedIncomePortion = this.unrecognizedIncomePortion,
    outstandingLoanBalance = this.outstandingLoanBalance,
    submittedOnDate = this.submittedOnDate,
    manuallyReversed = this.manuallyReversed,
    type = this.type?.let {
        Type(
            id = it.id,
            code = it.code,
            value = it.value,
            disbursement = it.disbursement,
            repaymentAtDisbursement = it.repaymentAtDisbursement,
            repayment = it.repayment,
            merchantIssuedRefund = it.merchantIssuedRefund,
            payoutRefund = it.payoutRefund,
            goodwillCredit = it.goodwillCredit,
            interestPaymentWaiver = it.interestPaymentWaiver,
            chargeoff = it.chargeoff,
            contra = it.contra,
            waiveInterest = it.waiveInterest,
            waiveCharges = it.waiveCharges,
            accrual = it.accrual,
            writeOff = it.writeOff,
            recoveryRepayment = it.recoveryRepayment,
            initiateTransfer = it.initiateTransfer,
            approveTransfer = it.approveTransfer,
            withdrawTransfer = it.withdrawTransfer,
            rejectTransfer = it.rejectTransfer,
            chargePayment = it.chargePayment,
            refund = it.refund,
            reAge = it.reAge,
            reAmortize = it.reAmortize,
            contractTermination = it.contractTermination,
            buyDownFee = it.buyDownFee,
            capitalizedIncome = it.capitalizedIncome,
        )
    },
)
