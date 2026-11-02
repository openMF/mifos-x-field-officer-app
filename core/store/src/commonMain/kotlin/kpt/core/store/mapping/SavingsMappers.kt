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

import kpt.core.model.savings.SavingAccountCurrency
import kpt.core.model.savings.SavingAccountDepositType
import kpt.core.model.savings.SavingsAccount
import kpt.core.model.savings.SavingsAccountStatus
import kpt.core.model.savings.SavingsAccountSummary
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.core.database.savings.entity.SavingAccountCurrencyEntity as SavingAccountCurrencyRow
import kpt.core.database.savings.entity.SavingAccountDepositTypeEntity as SavingAccountDepositTypeRow
import kpt.core.database.savings.entity.SavingsAccountEntity as SavingsAccountRow
import kpt.core.database.savings.entity.SavingsAccountStatusEntity as SavingsAccountStatusRow
import kpt.core.database.savings.entity.SavingsAccountSummaryEntity as SavingsAccountSummaryRow
import kpt.core.database.savings.entity.SavingsAccountWithAssociationsEntity as SavingsAccountWithAssociationsRow

fun SavingAccountCurrencyRow.toModel(): SavingAccountCurrency = SavingAccountCurrency(
    id = id,
    code = code,
    name = name,
    decimalPlaces = decimalPlaces,
    inMultiplesOf = inMultiplesOf,
    displaySymbol = displaySymbol,
    nameCode = nameCode,
    displayLabel = displayLabel,
)

fun SavingAccountCurrency.toEntity(): SavingAccountCurrencyRow = SavingAccountCurrencyRow(
    id = id,
    code = code,
    name = name,
    decimalPlaces = decimalPlaces,
    inMultiplesOf = inMultiplesOf,
    displaySymbol = displaySymbol,
    nameCode = nameCode,
    displayLabel = displayLabel,
)

fun SavingAccountDepositTypeRow.toModel(): SavingAccountDepositType = SavingAccountDepositType(
    id = id,
    code = code,
    value = value,
)

fun SavingAccountDepositType.toEntity(): SavingAccountDepositTypeRow = SavingAccountDepositTypeRow(
    id = id,
    code = code,
    value = value,
)

fun SavingsAccountRow.toModel(): SavingsAccount = SavingsAccount(
    id = id,
    clientId = clientId,
    groupId = groupId,
    centerId = centerId,
    accountNo = accountNo,
    productId = productId,
    shortProductName = shortProductName,
    productName = productName,
    status = status?.toModel(),
    currency = currency?.toModel(),
    accountBalance = accountBalance,
    depositType = depositType?.toModel(),
    lastActiveTransactionDate = lastActiveTransactionDate,
)

fun SavingsAccount.toEntity(): SavingsAccountRow = SavingsAccountRow(
    id = id,
    clientId = clientId,
    groupId = groupId,
    centerId = centerId,
    accountNo = accountNo,
    productId = productId,
    shortProductName = shortProductName,
    productName = productName,
    status = status?.toEntity(),
    currency = currency?.toEntity(),
    accountBalance = accountBalance,
    depositType = depositType?.toEntity(),
    lastActiveTransactionDate = lastActiveTransactionDate,
)

fun SavingsAccountStatusRow.toModel(): SavingsAccountStatus = SavingsAccountStatus(
    id = id,
    code = code,
    value = value,
    submittedAndPendingApproval = submittedAndPendingApproval,
    approved = approved,
    rejected = rejected,
    withdrawnByApplicant = withdrawnByApplicant,
    active = active,
    closed = closed,
)

fun SavingsAccountStatus.toEntity(): SavingsAccountStatusRow = SavingsAccountStatusRow(
    id = id,
    code = code,
    value = value,
    submittedAndPendingApproval = submittedAndPendingApproval,
    approved = approved,
    rejected = rejected,
    withdrawnByApplicant = withdrawnByApplicant,
    active = active,
    closed = closed,
)

fun SavingsAccountSummaryRow.toModel(): SavingsAccountSummary = SavingsAccountSummary(
    savingsId = savingsId,
    currency = currency?.toModel(),
    totalDeposits = totalDeposits,
    accountBalance = accountBalance,
    totalWithdrawals = totalWithdrawals,
    totalInterestEarned = totalInterestEarned,
)

fun SavingsAccountSummary.toEntity(): SavingsAccountSummaryRow = SavingsAccountSummaryRow(
    savingsId = savingsId,
    currency = currency?.toEntity(),
    totalDeposits = totalDeposits,
    accountBalance = accountBalance,
    totalWithdrawals = totalWithdrawals,
    totalInterestEarned = totalInterestEarned,
)

fun SavingsAccountWithAssociationsRow.toModel(): SavingsAccountWithAssociations = SavingsAccountWithAssociations(
    id = id,
    accountNo = accountNo,
    clientId = clientId,
    clientName = clientName,
    savingsProductId = savingsProductId,
    savingsProductName = savingsProductName,
    fieldOfficerId = fieldOfficerId,
    status = status?.toModel(),
    timeline = timeline?.toModel(),
    currency = currency?.toModel(),
    nominalAnnualInterestRate = nominalAnnualInterestRate,
    interestCompoundingPeriodType = interestCompoundingPeriodType,
    interestPostingPeriodType = interestPostingPeriodType,
    interestCalculationType = interestCalculationType,
    interestCalculationDaysInYearType = interestCalculationDaysInYearType,
    minRequiredOpeningBalance = minRequiredOpeningBalance,
    lockinPeriodFrequency = lockinPeriodFrequency,
    lockinPeriodFrequencyType = lockinPeriodFrequencyType,
    withdrawalFeeForTransfers = withdrawalFeeForTransfers,
    allowOverdraft = allowOverdraft,
    enforceMinRequiredBalance = enforceMinRequiredBalance,
    withHoldTax = withHoldTax,
    lastActiveTransactionDate = lastActiveTransactionDate,
    dormancyTrackingActive = dormancyTrackingActive,
    overdraftLimit = overdraftLimit,
    summary = summary?.toModel(),
    transactions = transactions.map { it.toModel() },
    charges = charges.map { it?.toModel() },
)

fun SavingsAccountWithAssociations.toEntity(): SavingsAccountWithAssociationsRow = SavingsAccountWithAssociationsRow(
    id = id,
    accountNo = accountNo,
    clientId = clientId,
    clientName = clientName,
    savingsProductId = savingsProductId,
    savingsProductName = savingsProductName,
    fieldOfficerId = fieldOfficerId,
    status = status?.toEntity(),
    timeline = timeline?.toEntity(),
    currency = currency?.toEntity(),
    nominalAnnualInterestRate = nominalAnnualInterestRate,
    interestCompoundingPeriodType = interestCompoundingPeriodType,
    interestPostingPeriodType = interestPostingPeriodType,
    interestCalculationType = interestCalculationType,
    interestCalculationDaysInYearType = interestCalculationDaysInYearType,
    minRequiredOpeningBalance = minRequiredOpeningBalance,
    lockinPeriodFrequency = lockinPeriodFrequency,
    lockinPeriodFrequencyType = lockinPeriodFrequencyType,
    withdrawalFeeForTransfers = withdrawalFeeForTransfers,
    allowOverdraft = allowOverdraft,
    enforceMinRequiredBalance = enforceMinRequiredBalance,
    withHoldTax = withHoldTax,
    lastActiveTransactionDate = lastActiveTransactionDate,
    dormancyTrackingActive = dormancyTrackingActive,
    overdraftLimit = overdraftLimit,
    summary = summary?.toEntity(),
    transactions = transactions.map { it.toEntity() },
    charges = charges.map { it?.toEntity() },
)
