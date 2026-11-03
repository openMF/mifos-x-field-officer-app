/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.mapping

import kpt.core.model.loan.ActualDisbursementDate
import kpt.core.model.loan.Loan
import kpt.core.model.loan.LoanAccount
import kpt.core.model.loan.LoanAccountSummary
import kpt.core.model.loan.LoanAccountType
import kpt.core.model.loan.LoanStatus
import kpt.core.model.loan.LoanTimeline
import kpt.core.database.loan.entity.ActualDisbursementDateEntity as ActualDisbursementDateRow
import kpt.core.database.loan.entity.Loan as LoanRow
import kpt.core.database.loan.entity.LoanAccountEntity as LoanAccountRow
import kpt.core.database.loan.entity.LoanAccountSummaryEntity as LoanAccountSummaryRow
import kpt.core.database.loan.entity.LoanStatusEntity as LoanStatusRow
import kpt.core.database.loan.entity.LoanTimelineEntity as LoanTimelineRow
import kpt.core.database.loan.entity.LoanTypeEntity as LoanAccountTypeRow

/** Maps the stored row to its `ActualDisbursementDate` model. */
fun ActualDisbursementDateRow.toModel(): ActualDisbursementDate = ActualDisbursementDate(
    loanId = loanId,
    year = year,
    month = month,
    date = date,
)

/** Maps the model to its stored `ActualDisbursementDateRow`. */
fun ActualDisbursementDate.toEntity(): ActualDisbursementDateRow = ActualDisbursementDateRow(
    loanId = loanId,
    year = year,
    month = month,
    date = date,
)

/** Maps the stored row to its `LoanAccount` model. */
fun LoanAccountRow.toModel(): LoanAccount = LoanAccount(
    id = id,
    clientId = clientId,
    groupId = groupId,
    centerId = centerId,
    accountNo = accountNo,
    externalId = externalId,
    productId = productId,
    productName = productName,
    currency = currency,
    status = status?.toModel(),
    loanType = loanType?.toModel(),
    loanCycle = loanCycle,
    inArrears = inArrears,
    originalLoan = originalLoan,
    loanBalance = loanBalance,
    amountPaid = amountPaid,
)

/** Maps the model to its stored `LoanAccountRow`. */
fun LoanAccount.toEntity(): LoanAccountRow = LoanAccountRow(
    id = id,
    clientId = clientId,
    groupId = groupId,
    centerId = centerId,
    accountNo = accountNo,
    externalId = externalId,
    productId = productId,
    productName = productName,
    currency = currency,
    status = status?.toEntity(),
    loanType = loanType?.toEntity(),
    loanCycle = loanCycle,
    inArrears = inArrears,
    originalLoan = originalLoan,
    loanBalance = loanBalance,
    amountPaid = amountPaid,
)

/** Maps the stored row to its `LoanStatus` model. */
fun LoanStatusRow.toModel(): LoanStatus = LoanStatus(
    id = id,
    code = code,
    value = value,
    pendingApproval = pendingApproval,
    waitingForDisbursal = waitingForDisbursal,
    active = active,
    closedObligationsMet = closedObligationsMet,
    closedWrittenOff = closedWrittenOff,
    closedRescheduled = closedRescheduled,
    closed = closed,
    overpaid = overpaid,
)

/** Maps the model to its stored `LoanStatusRow`. */
fun LoanStatus.toEntity(): LoanStatusRow = LoanStatusRow(
    id = id,
    code = code,
    value = value,
    pendingApproval = pendingApproval,
    waitingForDisbursal = waitingForDisbursal,
    active = active,
    closedObligationsMet = closedObligationsMet,
    closedWrittenOff = closedWrittenOff,
    closedRescheduled = closedRescheduled,
    closed = closed,
    overpaid = overpaid,
)

/** Maps the stored row to its `LoanTimeline` model. */
fun LoanTimelineRow.toModel(): LoanTimeline = LoanTimeline(
    loanId = loanId,
    submittedOnDate = submittedOnDate,
    submittedByUsername = submittedByUsername,
    submittedByFirstname = submittedByFirstname,
    submittedByLastname = submittedByLastname,
    approvedOnDate = approvedOnDate,
    approvedByUsername = approvedByUsername,
    approvedByFirstname = approvedByFirstname,
    approvedByLastname = approvedByLastname,
    expectedDisbursementDate = expectedDisbursementDate,
    actualDisburseDate = actualDisburseDate?.toModel(),
    actualDisbursementDate = actualDisbursementDate,
    disbursedByUsername = disbursedByUsername,
    disbursedByFirstname = disbursedByFirstname,
    disbursedByLastname = disbursedByLastname,
    closedOnDate = closedOnDate,
    expectedMaturityDate = expectedMaturityDate,
    withdrawnOnDate = withdrawnOnDate,
)

/** Maps the model to its stored `LoanTimelineRow`. */
fun LoanTimeline.toEntity(): LoanTimelineRow = LoanTimelineRow(
    loanId = loanId,
    submittedOnDate = submittedOnDate,
    submittedByUsername = submittedByUsername,
    submittedByFirstname = submittedByFirstname,
    submittedByLastname = submittedByLastname,
    approvedOnDate = approvedOnDate,
    approvedByUsername = approvedByUsername,
    approvedByFirstname = approvedByFirstname,
    approvedByLastname = approvedByLastname,
    expectedDisbursementDate = expectedDisbursementDate,
    actualDisburseDate = actualDisburseDate?.toEntity(),
    actualDisbursementDate = actualDisbursementDate,
    disbursedByUsername = disbursedByUsername,
    disbursedByFirstname = disbursedByFirstname,
    disbursedByLastname = disbursedByLastname,
    closedOnDate = closedOnDate,
    expectedMaturityDate = expectedMaturityDate,
    withdrawnOnDate = withdrawnOnDate,
)

/** Maps the stored row to its `LoanAccountType` model. */
fun LoanAccountTypeRow.toModel(): LoanAccountType = LoanAccountType(
    id = id,
    code = code,
    value = value,
)

/** Maps the model to its stored `LoanAccountTypeRow`. */
fun LoanAccountType.toEntity(): LoanAccountTypeRow = LoanAccountTypeRow(
    id = id,
    code = code,
    value = value,
)

/** Maps the stored row to its `Loan` model. */
fun LoanRow.toModel(): Loan = Loan(
    id = id,
    accountNo = accountNo,
    status = status?.toModel(),
    clientId = clientId,
    clientName = clientName,
    clientOfficeId = clientOfficeId,
    loanProductId = loanProductId,
    loanProductName = loanProductName,
    loanProductDescription = loanProductDescription,
    fundId = fundId,
    fundName = fundName,
    loanOfficerId = loanOfficerId,
    loanOfficerName = loanOfficerName,
    loanType = loanType?.toModel(),
    currency = currency,
    principal = principal,
    approvedPrincipal = approvedPrincipal,
    termFrequency = termFrequency,
    termPeriodFrequencyType = termPeriodFrequencyType,
    numberOfRepayments = numberOfRepayments,
    repaymentEvery = repaymentEvery,
    repaymentFrequencyType = repaymentFrequencyType,
    interestRatePerPeriod = interestRatePerPeriod,
    interestRateFrequencyType = interestRateFrequencyType,
    annualInterestRate = annualInterestRate,
    amortizationType = amortizationType,
    interestType = interestType,
    interestCalculationPeriodType = interestCalculationPeriodType,
    transactionProcessingStrategyId = transactionProcessingStrategyId,
    transactionProcessingStrategyName = transactionProcessingStrategyName,
    syncDisbursementWithMeeting = syncDisbursementWithMeeting,
    timeline = timeline?.toModel(),
    summary = summary?.toModel(),
    feeChargesAtDisbursementCharged = feeChargesAtDisbursementCharged,
    loanCounter = loanCounter,
    loanProductCounter = loanProductCounter,
    multiDisburseLoan = multiDisburseLoan,
    canDisburse = canDisburse,
    inArrears = inArrears,
    isNPA = isNPA,
)

/** Maps the model to its stored `LoanRow`. */
fun Loan.toEntity(): LoanRow = LoanRow(
    id = id,
    accountNo = accountNo,
    status = status?.toEntity(),
    clientId = clientId,
    clientName = clientName,
    clientOfficeId = clientOfficeId,
    loanProductId = loanProductId,
    loanProductName = loanProductName,
    loanProductDescription = loanProductDescription,
    fundId = fundId,
    fundName = fundName,
    loanOfficerId = loanOfficerId,
    loanOfficerName = loanOfficerName,
    loanType = loanType?.toEntity(),
    currency = currency,
    principal = principal,
    approvedPrincipal = approvedPrincipal,
    termFrequency = termFrequency,
    termPeriodFrequencyType = termPeriodFrequencyType,
    numberOfRepayments = numberOfRepayments,
    repaymentEvery = repaymentEvery,
    repaymentFrequencyType = repaymentFrequencyType,
    interestRatePerPeriod = interestRatePerPeriod,
    interestRateFrequencyType = interestRateFrequencyType,
    annualInterestRate = annualInterestRate,
    amortizationType = amortizationType,
    interestType = interestType,
    interestCalculationPeriodType = interestCalculationPeriodType,
    transactionProcessingStrategyId = transactionProcessingStrategyId,
    transactionProcessingStrategyName = transactionProcessingStrategyName,
    syncDisbursementWithMeeting = syncDisbursementWithMeeting,
    timeline = timeline?.toEntity(),
    summary = summary?.toEntity(),
    feeChargesAtDisbursementCharged = feeChargesAtDisbursementCharged,
    loanCounter = loanCounter,
    loanProductCounter = loanProductCounter,
    multiDisburseLoan = multiDisburseLoan,
    canDisburse = canDisburse,
    inArrears = inArrears,
    isNPA = isNPA,
)

/** Maps the stored row to its `LoanAccountSummary` model. */
fun LoanAccountSummaryRow.toModel(): LoanAccountSummary = LoanAccountSummary(
    loanId = loanId,
    currency = currency?.toModel(),
    principalDisbursed = principalDisbursed,
    principalPaid = principalPaid,
    principalWaived = principalWaived,
    principalWrittenOff = principalWrittenOff,
    principalOutstanding = principalOutstanding,
    principalOverdue = principalOverdue,
    interestCharged = interestCharged,
    interestPaid = interestPaid,
    interestWaived = interestWaived,
    interestWrittenOff = interestWrittenOff,
    interestOutstanding = interestOutstanding,
    interestOverdue = interestOverdue,
    feeChargesCharged = feeChargesCharged,
    feeChargesDueAtDisbursementCharged = feeChargesDueAtDisbursementCharged,
    feeChargesPaid = feeChargesPaid,
    feeChargesWaived = feeChargesWaived,
    feeChargesWrittenOff = feeChargesWrittenOff,
    feeChargesOutstanding = feeChargesOutstanding,
    feeChargesOverdue = feeChargesOverdue,
    penaltyChargesCharged = penaltyChargesCharged,
    penaltyChargesPaid = penaltyChargesPaid,
    penaltyChargesWaived = penaltyChargesWaived,
    penaltyChargesWrittenOff = penaltyChargesWrittenOff,
    penaltyChargesOutstanding = penaltyChargesOutstanding,
    penaltyChargesOverdue = penaltyChargesOverdue,
    totalExpectedRepayment = totalExpectedRepayment,
    totalRepayment = totalRepayment,
    totalExpectedCostOfLoan = totalExpectedCostOfLoan,
    totalCostOfLoan = totalCostOfLoan,
    totalWaived = totalWaived,
    totalWrittenOff = totalWrittenOff,
    totalOutstanding = totalOutstanding,
    totalOverdue = totalOverdue,
    overdueSinceDate = overdueSinceDate,
)

/** Maps the model to its stored `LoanAccountSummaryRow`. */
fun LoanAccountSummary.toEntity(): LoanAccountSummaryRow = LoanAccountSummaryRow(
    loanId = loanId,
    currency = currency?.toEntity(),
    principalDisbursed = principalDisbursed,
    principalPaid = principalPaid,
    principalWaived = principalWaived,
    principalWrittenOff = principalWrittenOff,
    principalOutstanding = principalOutstanding,
    principalOverdue = principalOverdue,
    interestCharged = interestCharged,
    interestPaid = interestPaid,
    interestWaived = interestWaived,
    interestWrittenOff = interestWrittenOff,
    interestOutstanding = interestOutstanding,
    interestOverdue = interestOverdue,
    feeChargesCharged = feeChargesCharged,
    feeChargesDueAtDisbursementCharged = feeChargesDueAtDisbursementCharged,
    feeChargesPaid = feeChargesPaid,
    feeChargesWaived = feeChargesWaived,
    feeChargesWrittenOff = feeChargesWrittenOff,
    feeChargesOutstanding = feeChargesOutstanding,
    feeChargesOverdue = feeChargesOverdue,
    penaltyChargesCharged = penaltyChargesCharged,
    penaltyChargesPaid = penaltyChargesPaid,
    penaltyChargesWaived = penaltyChargesWaived,
    penaltyChargesWrittenOff = penaltyChargesWrittenOff,
    penaltyChargesOutstanding = penaltyChargesOutstanding,
    penaltyChargesOverdue = penaltyChargesOverdue,
    totalExpectedRepayment = totalExpectedRepayment,
    totalRepayment = totalRepayment,
    totalExpectedCostOfLoan = totalExpectedCostOfLoan,
    totalCostOfLoan = totalCostOfLoan,
    totalWaived = totalWaived,
    totalWrittenOff = totalWrittenOff,
    totalOutstanding = totalOutstanding,
    totalOverdue = totalOverdue,
    overdueSinceDate = overdueSinceDate,
)
