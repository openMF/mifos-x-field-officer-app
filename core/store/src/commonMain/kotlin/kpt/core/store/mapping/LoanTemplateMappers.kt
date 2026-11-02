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

import kpt.core.model.client.InterestTypeOption
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.loan.LoanRepaymentResponse
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.loan.LoanTemplate
import kpt.core.model.loan.LoanTransactionTemplate
import kpt.core.database.client.entity.InterestTypeEntity as InterestTypeOptionRow
import kpt.core.database.loan.entity.LoanRepaymentRequestEntity as LoanRepaymentRequestRow
import kpt.core.database.loan.entity.LoanRepaymentResponseEntity as LoanRepaymentResponseRow
import kpt.core.database.loan.entity.LoanRepaymentTemplateEntity as LoanRepaymentTemplateRow
import kpt.core.database.loan.entity.LoanTemplate as LoanTemplateRow
import kpt.core.database.loan.entity.LoanTransactionTemplate as LoanTransactionTemplateRow

fun InterestTypeOptionRow.toModel(): InterestTypeOption = InterestTypeOption(
    id = id,
    code = code,
    value = value,
)

fun InterestTypeOption.toEntity(): InterestTypeOptionRow = InterestTypeOptionRow(
    id = id,
    code = code,
    value = value,
)

fun LoanRepaymentRequestRow.toModel(): LoanRepaymentRequest = LoanRepaymentRequest(
    timeStamp = timeStamp,
    loanId = loanId,
    errorMessage = errorMessage,
    dateFormat = dateFormat,
    locale = locale,
    transactionDate = transactionDate,
    transactionAmount = transactionAmount,
    paymentTypeId = paymentTypeId,
    note = note,
    accountNumber = accountNumber,
    checkNumber = checkNumber,
    routingCode = routingCode,
    receiptNumber = receiptNumber,
    bankNumber = bankNumber,
)

fun LoanRepaymentRequest.toEntity(): LoanRepaymentRequestRow = LoanRepaymentRequestRow(
    timeStamp = timeStamp,
    loanId = loanId,
    errorMessage = errorMessage,
    dateFormat = dateFormat,
    locale = locale,
    transactionDate = transactionDate,
    transactionAmount = transactionAmount,
    paymentTypeId = paymentTypeId,
    note = note,
    accountNumber = accountNumber,
    checkNumber = checkNumber,
    routingCode = routingCode,
    receiptNumber = receiptNumber,
    bankNumber = bankNumber,
)

fun LoanRepaymentTemplateRow.toModel(): LoanRepaymentTemplate = LoanRepaymentTemplate(
    loanId = loanId,
    type = type,
    date = date,
    currency = currency?.toModel(),
    amount = amount,
    principalPortion = principalPortion,
    interestPortion = interestPortion,
    feeChargesPortion = feeChargesPortion,
    penaltyChargesPortion = penaltyChargesPortion,
    paymentTypeOptions = paymentTypeOptions?.map { it.toModel() },
)

fun LoanRepaymentTemplate.toEntity(): LoanRepaymentTemplateRow = LoanRepaymentTemplateRow(
    loanId = loanId,
    type = type,
    date = date,
    currency = currency?.toEntity(),
    amount = amount,
    principalPortion = principalPortion,
    interestPortion = interestPortion,
    feeChargesPortion = feeChargesPortion,
    penaltyChargesPortion = penaltyChargesPortion,
    paymentTypeOptions = paymentTypeOptions?.map { it.toEntity() },
)

fun LoanTemplateRow.toModel(): LoanTemplate = LoanTemplate(
    clientId = clientId,
    clientAccountNo = clientAccountNo,
    clientName = clientName,
    clientOfficeId = clientOfficeId,
    loanProductId = loanProductId,
    loanProductName = loanProductName,
    isLoanProductLinkedToFloatingRate = isLoanProductLinkedToFloatingRate,
    fundId = fundId,
    fundName = fundName,
    currency = currency,
    principal = principal,
    approvedPrincipal = approvedPrincipal,
    proposedPrincipal = proposedPrincipal,
    termFrequency = termFrequency,
    termPeriodFrequencyType = termPeriodFrequencyType,
    numberOfRepayments = numberOfRepayments,
    repaymentEvery = repaymentEvery,
    repaymentFrequencyType = repaymentFrequencyType,
    interestRatePerPeriod = interestRatePerPeriod,
    interestRateFrequencyType = interestRateFrequencyType,
    annualInterestRate = annualInterestRate,
    isFloatingInterestRate = isFloatingInterestRate,
    amortizationType = amortizationType,
    interestType = interestType,
    interestCalculationPeriodType = interestCalculationPeriodType,
    allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation,
    transactionProcessingStrategyId = transactionProcessingStrategyId,
    graceOnArrearsAgeing = graceOnArrearsAgeing,
    timeline = timeline,
    productOptions = productOptions,
    dataTables = dataTables.mapTo(ArrayList()) { it.toModel() },
    loanOfficerOptions = loanOfficerOptions,
    loanPurposeOptions = loanPurposeOptions,
    fundOptions = fundOptions,
    termFrequencyTypeOptions = termFrequencyTypeOptions,
    repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions,
    repaymentFrequencyNthDayTypeOptions = repaymentFrequencyNthDayTypeOptions,
    repaymentFrequencyDaysOfWeekTypeOptions = repaymentFrequencyDaysOfWeekTypeOptions,
    interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions,
    amortizationTypeOptions = amortizationTypeOptions,
    interestTypeOptions = interestTypeOptions,
    interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions,
    transactionProcessingStrategyOptions = transactionProcessingStrategyOptions,
    chargeOptions = chargeOptions,
    loanCollateralOptions = loanCollateralOptions,
    multiDisburseLoan = multiDisburseLoan,
    transactionProcessingStrategyCode = transactionProcessingStrategyCode,
    canDefineInstallmentAmount = canDefineInstallmentAmount,
    canDisburse = canDisburse,
    product = product,
    daysInMonthType = daysInMonthType,
    daysInYearType = daysInYearType,
    isInterestRecalculationEnabled = isInterestRecalculationEnabled,
    isvaliableInstallmentsAllowed = isvaliableInstallmentsAllowed,
    minimumGap = minimumGap,
    maximumGap = maximumGap,
    accountLinkingOptions = accountLinkingOptions,
    loanScheduleType = loanScheduleType,
    overdueCharges = overdueCharges.map { it.toModel() },
)

fun LoanTemplate.toEntity(): LoanTemplateRow = LoanTemplateRow(
    clientId = clientId,
    clientAccountNo = clientAccountNo,
    clientName = clientName,
    clientOfficeId = clientOfficeId,
    loanProductId = loanProductId,
    loanProductName = loanProductName,
    isLoanProductLinkedToFloatingRate = isLoanProductLinkedToFloatingRate,
    fundId = fundId,
    fundName = fundName,
    currency = currency,
    principal = principal,
    approvedPrincipal = approvedPrincipal,
    proposedPrincipal = proposedPrincipal,
    termFrequency = termFrequency,
    termPeriodFrequencyType = termPeriodFrequencyType,
    numberOfRepayments = numberOfRepayments,
    repaymentEvery = repaymentEvery,
    repaymentFrequencyType = repaymentFrequencyType,
    interestRatePerPeriod = interestRatePerPeriod,
    interestRateFrequencyType = interestRateFrequencyType,
    annualInterestRate = annualInterestRate,
    isFloatingInterestRate = isFloatingInterestRate,
    amortizationType = amortizationType,
    interestType = interestType,
    interestCalculationPeriodType = interestCalculationPeriodType,
    allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation,
    transactionProcessingStrategyId = transactionProcessingStrategyId,
    graceOnArrearsAgeing = graceOnArrearsAgeing,
    timeline = timeline,
    productOptions = productOptions,
    dataTables = dataTables.mapTo(ArrayList()) { it.toEntity() },
    loanOfficerOptions = loanOfficerOptions,
    loanPurposeOptions = loanPurposeOptions,
    fundOptions = fundOptions,
    termFrequencyTypeOptions = termFrequencyTypeOptions,
    repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions,
    repaymentFrequencyNthDayTypeOptions = repaymentFrequencyNthDayTypeOptions,
    repaymentFrequencyDaysOfWeekTypeOptions = repaymentFrequencyDaysOfWeekTypeOptions,
    interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions,
    amortizationTypeOptions = amortizationTypeOptions,
    interestTypeOptions = interestTypeOptions,
    interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions,
    transactionProcessingStrategyOptions = transactionProcessingStrategyOptions,
    chargeOptions = chargeOptions,
    loanCollateralOptions = loanCollateralOptions,
    multiDisburseLoan = multiDisburseLoan,
    transactionProcessingStrategyCode = transactionProcessingStrategyCode,
    canDefineInstallmentAmount = canDefineInstallmentAmount,
    canDisburse = canDisburse,
    product = product,
    daysInMonthType = daysInMonthType,
    daysInYearType = daysInYearType,
    isInterestRecalculationEnabled = isInterestRecalculationEnabled,
    isvaliableInstallmentsAllowed = isvaliableInstallmentsAllowed,
    minimumGap = minimumGap,
    maximumGap = maximumGap,
    accountLinkingOptions = accountLinkingOptions,
    loanScheduleType = loanScheduleType,
    overdueCharges = overdueCharges.map { it.toEntity() },
)

fun LoanRepaymentResponseRow.toModel(): LoanRepaymentResponse = LoanRepaymentResponse(
    id = id,
    officeId = officeId,
    clientId = clientId,
    loanId = loanId,
    resourceId = resourceId,
    changes = changes,
)

fun LoanRepaymentResponse.toEntity(): LoanRepaymentResponseRow = LoanRepaymentResponseRow(
    id = id,
    officeId = officeId,
    clientId = clientId,
    loanId = loanId,
    resourceId = resourceId,
    changes = changes,
)

fun LoanTransactionTemplateRow.toModel(): LoanTransactionTemplate = LoanTransactionTemplate(
    type = type,
    date = date,
    amount = amount,
    manuallyReversed = manuallyReversed,
    possibleNextRepaymentDate = possibleNextRepaymentDate,
    paymentTypeOptions = paymentTypeOptions.map { it.toModel() },
)

fun LoanTransactionTemplate.toEntity(): LoanTransactionTemplateRow = LoanTransactionTemplateRow(
    type = type,
    date = date,
    amount = amount,
    manuallyReversed = manuallyReversed,
    possibleNextRepaymentDate = possibleNextRepaymentDate,
    paymentTypeOptions = paymentTypeOptions.map { it.toEntity() },
)
