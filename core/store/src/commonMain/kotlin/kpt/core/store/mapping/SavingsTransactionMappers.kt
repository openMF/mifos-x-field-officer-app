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

import kpt.core.model.client.SavingProductOptions
import kpt.core.model.savings.SavingProductsTemplate
import kpt.core.model.savings.SavingsAccountTransaction
import kpt.core.model.savings.SavingsAccountTransactionRequest
import kpt.core.model.savings.SavingsAccountTransactionTemplate
import kpt.core.model.savings.SavingsTransactionDate
import kpt.core.model.savings.SavingsTransactionType
import kpt.core.database.client.entity.SavingProductOptionsEntity as SavingProductOptionsRow
import kpt.core.database.savings.entity.SavingProductsTemplate as SavingProductsTemplateRow
import kpt.core.database.savings.entity.SavingsAccountTransactionEntity as SavingsAccountTransactionRow
import kpt.core.database.savings.entity.SavingsAccountTransactionRequestEntity as SavingsAccountTransactionRequestRow
import kpt.core.database.savings.entity.SavingsAccountTransactionTemplateEntity as SavingsAccountTransactionTemplateRow
import kpt.core.database.savings.entity.SavingsTransactionDateEntity as SavingsTransactionDateRow
import kpt.core.database.savings.entity.SavingsTransactionTypeEntity as SavingsTransactionTypeRow

/** Maps the stored row to its `SavingProductOptions` model. */
fun SavingProductOptionsRow.toModel(): SavingProductOptions = SavingProductOptions(
    id = id,
    name = name,
    withdrawalFeeForTransfers = withdrawalFeeForTransfers,
    allowOverdraft = allowOverdraft,
)

/** Maps the model to its stored `SavingProductOptionsRow`. */
fun SavingProductOptions.toEntity(): SavingProductOptionsRow = SavingProductOptionsRow(
    id = id,
    name = name,
    withdrawalFeeForTransfers = withdrawalFeeForTransfers,
    allowOverdraft = allowOverdraft,
)

/** Maps the stored row to its `SavingProductsTemplate` model. */
fun SavingProductsTemplateRow.toModel(): SavingProductsTemplate = SavingProductsTemplate(
    currency = currency,
    interestCompoundingPeriodType = interestCompoundingPeriodType,
    interestPostingPeriodType = interestPostingPeriodType,
    interestCalculationType = interestCalculationType,
    interestCalculationDaysInYearType = interestCalculationDaysInYearType,
    accountingRule = accountingRule,
    currencyOptions = currencyOptions,
    interestCompoundingPeriodTypeOptions = interestCompoundingPeriodTypeOptions,
    interestPostingPeriodTypeOptions = interestPostingPeriodTypeOptions,
    interestCalculationTypeOptions = interestCalculationTypeOptions,
    interestCalculationDaysInYearTypeOptions = interestCalculationDaysInYearTypeOptions,
    lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions,
    withdrawalFeeTypeOptions = withdrawalFeeTypeOptions,
    paymentTypeOptions = paymentTypeOptions?.map { it.toModel() },
    accountingRuleOptions = accountingRuleOptions,
    liabilityAccountOptions = liabilityAccountOptions,
    assetAccountOptions = assetAccountOptions,
    expenseAccountOptions = expenseAccountOptions,
    incomeAccountOptions = incomeAccountOptions,
    fieldOfficerOptions = fieldOfficerOptions,
    chargeOptions = chargeOptions,
)

/** Maps the model to its stored `SavingProductsTemplateRow`. */
fun SavingProductsTemplate.toEntity(): SavingProductsTemplateRow = SavingProductsTemplateRow(
    currency = currency,
    interestCompoundingPeriodType = interestCompoundingPeriodType,
    interestPostingPeriodType = interestPostingPeriodType,
    interestCalculationType = interestCalculationType,
    interestCalculationDaysInYearType = interestCalculationDaysInYearType,
    accountingRule = accountingRule,
    currencyOptions = currencyOptions,
    interestCompoundingPeriodTypeOptions = interestCompoundingPeriodTypeOptions,
    interestPostingPeriodTypeOptions = interestPostingPeriodTypeOptions,
    interestCalculationTypeOptions = interestCalculationTypeOptions,
    interestCalculationDaysInYearTypeOptions = interestCalculationDaysInYearTypeOptions,
    lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions,
    withdrawalFeeTypeOptions = withdrawalFeeTypeOptions,
    paymentTypeOptions = paymentTypeOptions?.map { it.toEntity() },
    accountingRuleOptions = accountingRuleOptions,
    liabilityAccountOptions = liabilityAccountOptions,
    assetAccountOptions = assetAccountOptions,
    expenseAccountOptions = expenseAccountOptions,
    incomeAccountOptions = incomeAccountOptions,
    fieldOfficerOptions = fieldOfficerOptions,
    chargeOptions = chargeOptions,
)

/** Maps the stored row to its `SavingsAccountTransaction` model. */
fun SavingsAccountTransactionRow.toModel(): SavingsAccountTransaction = SavingsAccountTransaction(
    id = id,
    savingsAccountId = savingsAccountId,
    transactionType = transactionType?.toModel(),
    accountId = accountId,
    accountNo = accountNo,
    savingsTransactionDate = savingsTransactionDate?.toModel(),
    date = date,
    currency = currency?.toModel(),
    amount = amount,
    runningBalance = runningBalance,
    reversed = reversed,
)

/** Maps the model to its stored `SavingsAccountTransactionRow`. */
fun SavingsAccountTransaction.toEntity(): SavingsAccountTransactionRow = SavingsAccountTransactionRow(
    id = id,
    savingsAccountId = savingsAccountId,
    transactionType = transactionType?.toEntity(),
    accountId = accountId,
    accountNo = accountNo,
    savingsTransactionDate = savingsTransactionDate?.toEntity(),
    date = date,
    currency = currency?.toEntity(),
    amount = amount,
    runningBalance = runningBalance,
    reversed = reversed,
)

/** Maps the stored row to its `SavingsAccountTransactionTemplate` model. */
fun SavingsAccountTransactionTemplateRow.toModel(): SavingsAccountTransactionTemplate =
    SavingsAccountTransactionTemplate(
        accountId = accountId,
        accountNo = accountNo,
        date = date,
        reversed = reversed,
        paymentTypeOptions = paymentTypeOptions.map { it.toModel() },
    )

/** Maps the model to its stored `SavingsAccountTransactionTemplateRow`. */
fun SavingsAccountTransactionTemplate.toEntity(): SavingsAccountTransactionTemplateRow =
    SavingsAccountTransactionTemplateRow(
        accountId = accountId,
        accountNo = accountNo,
        date = date,
        reversed = reversed,
        paymentTypeOptions = paymentTypeOptions.map { it.toEntity() },
    )

/** Maps the stored row to its `SavingsTransactionDate` model. */
fun SavingsTransactionDateRow.toModel(): SavingsTransactionDate = SavingsTransactionDate(
    transactionId = transactionId,
    year = year,
    month = month,
    day = day,
)

/** Maps the model to its stored `SavingsTransactionDateRow`. */
fun SavingsTransactionDate.toEntity(): SavingsTransactionDateRow = SavingsTransactionDateRow(
    transactionId = transactionId,
    year = year,
    month = month,
    day = day,
)

/** Maps the stored row to its `SavingsTransactionType` model. */
fun SavingsTransactionTypeRow.toModel(): SavingsTransactionType = SavingsTransactionType(
    id = id,
    code = code,
    value = value,
    deposit = deposit,
    withdrawal = withdrawal,
    interestPosting = interestPosting,
    feeDeduction = feeDeduction,
    initiateTransfer = initiateTransfer,
    approveTransfer = approveTransfer,
    withdrawTransfer = withdrawTransfer,
    rejectTransfer = rejectTransfer,
    overdraftInterest = overdraftInterest,
    writtenoff = writtenoff,
    overdraftFee = overdraftFee,
)

/** Maps the model to its stored `SavingsTransactionTypeRow`. */
fun SavingsTransactionType.toEntity(): SavingsTransactionTypeRow = SavingsTransactionTypeRow(
    id = id,
    code = code,
    value = value,
    deposit = deposit,
    withdrawal = withdrawal,
    interestPosting = interestPosting,
    feeDeduction = feeDeduction,
    initiateTransfer = initiateTransfer,
    approveTransfer = approveTransfer,
    withdrawTransfer = withdrawTransfer,
    rejectTransfer = rejectTransfer,
    overdraftInterest = overdraftInterest,
    writtenoff = writtenoff,
    overdraftFee = overdraftFee,
)

/** Maps the stored row to its `SavingsAccountTransactionRequest` model. */
fun SavingsAccountTransactionRequestRow.toModel(): SavingsAccountTransactionRequest = SavingsAccountTransactionRequest(
    id = id,
    savingAccountId = savingAccountId,
    savingsAccountType = savingsAccountType,
    transactionType = transactionType,
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
    errorMessage = errorMessage,
)

/** Maps the model to its stored `SavingsAccountTransactionRequestRow`. */
fun SavingsAccountTransactionRequest.toEntity(): SavingsAccountTransactionRequestRow =
    SavingsAccountTransactionRequestRow(
        id = id,
        savingAccountId = savingAccountId,
        savingsAccountType = savingsAccountType,
        transactionType = transactionType,
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
        errorMessage = errorMessage,
    )
