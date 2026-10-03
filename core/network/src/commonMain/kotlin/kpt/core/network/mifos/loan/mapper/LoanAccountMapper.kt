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

import kpt.core.database.loan.entity.ActualDisbursementDateEntity
import kpt.core.database.loan.entity.LoanAccountSummaryEntity
import kpt.core.database.loan.entity.LoanStatusEntity
import kpt.core.database.loan.entity.LoanTimelineEntity
import kpt.core.database.loan.entity.LoanTypeEntity
import kpt.core.database.loan.entity.LoanWithAssociationsEntity
import kpt.core.database.savings.entity.SavingAccountCurrencyEntity
import kpt.core.model.objects.account.loan.AmortizationType
import kpt.core.model.objects.account.loan.InterestCalculationPeriodType
import kpt.core.model.objects.account.loan.InterestRateFrequencyType
import kpt.core.model.objects.account.loan.InterestType
import kpt.core.model.objects.account.loan.RepaymentFrequencyType
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.TermPeriodFrequencyType
import kpt.core.model.objects.account.loan.loanWithAssociations.ActualDisbursementDate
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanAccountSummary
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanStatus
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanTimeline
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanType
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.loanWithAssociations.SavingAccountCurrency
import kpt.core.network.data.AbstractMapper

object LoanAccountMapper : AbstractMapper<LoanWithAssociationsEntity, LoanWithAssociations>() {

    override fun mapFromEntity(entity: LoanWithAssociationsEntity): LoanWithAssociations {
        return LoanWithAssociations(
            id = entity.id,
            accountNo = entity.accountNo,
            status = entity.status.toDomain(),
            clientId = entity.clientId,
            clientName = entity.clientName,
            clientOfficeId = entity.clientOfficeId,
            loanProductId = entity.loanProductId,
            loanProductName = entity.loanProductName,
            loanProductDescription = entity.loanProductDescription,
            fundId = entity.fundId,
            fundName = entity.fundName,
            loanPurposeId = entity.loanPurposeId,
            loanPurposeName = entity.loanPurposeName,
            loanOfficerId = entity.loanOfficerId,
            loanOfficerName = entity.loanOfficerName,
            loanType = entity.loanType.toDomain(),
            currency = entity.currency.toDomain(),
            principal = entity.principal,
            approvedPrincipal = entity.approvedPrincipal,
            proposedPrincipal = entity.proposedPrincipal,
            termFrequency = entity.termFrequency,
            termPeriodFrequencyType = entity.termPeriodFrequencyType,
            numberOfRepayments = entity.numberOfRepayments,
            repaymentEvery = entity.repaymentEvery,
            repaymentFrequencyType = entity.repaymentFrequencyType,
            interestRatePerPeriod = entity.interestRatePerPeriod,
            interestRateFrequencyType = entity.interestRateFrequencyType,
            annualInterestRate = entity.annualInterestRate,
            amortizationType = entity.amortizationType,
            interestType = entity.interestType,
            interestCalculationPeriodType = entity.interestCalculationPeriodType,
            transactionProcessingStrategyId = entity.transactionProcessingStrategyId,
            transactionProcessingStrategyName = entity.transactionProcessingStrategyName,
            syncDisbursementWithMeeting = entity.syncDisbursementWithMeeting,
            timeline = entity.timeline.toDomain(),
            summary = entity.summary.toDomain(),
            repaymentSchedule = entity.repaymentSchedule,
            transactions = entity.transactions,
            feeChargesAtDisbursementCharged = entity.feeChargesAtDisbursementCharged,
            totalOverpaid = entity.totalOverpaid,
            loanCounter = entity.loanCounter,
            loanProductCounter = entity.loanProductCounter,
            multiDisburseLoan = entity.multiDisburseLoan,
            canDisburse = entity.canDisburse,
            inArrears = entity.inArrears,
            isNPA = entity.isNPA,
        )
    }

    override fun mapToEntity(domainModel: LoanWithAssociations): LoanWithAssociationsEntity {
        return LoanWithAssociationsEntity(
            id = domainModel.id ?: 0,
            accountNo = domainModel.accountNo ?: "",
            status = domainModel.status?.toEntity() ?: LoanStatusEntity(),
            clientId = domainModel.clientId ?: 0,
            clientName = domainModel.clientName ?: "",
            clientOfficeId = domainModel.clientOfficeId ?: 0,
            loanProductId = domainModel.loanProductId ?: 0,
            loanProductName = domainModel.loanProductName ?: "",
            loanProductDescription = domainModel.loanProductDescription ?: "",
            fundId = domainModel.fundId ?: 0,
            fundName = domainModel.fundName ?: "",
            loanPurposeId = domainModel.loanPurposeId ?: 0,
            loanPurposeName = domainModel.loanPurposeName ?: "",
            loanOfficerId = domainModel.loanOfficerId ?: 0,
            loanOfficerName = domainModel.loanOfficerName ?: "",
            loanType = domainModel.loanType?.toEntity() ?: LoanTypeEntity(),
            currency = domainModel.currency?.toEntity() ?: SavingAccountCurrencyEntity(),
            principal = domainModel.principal ?: 0.0,
            approvedPrincipal = domainModel.approvedPrincipal ?: 0.0,
            proposedPrincipal = domainModel.proposedPrincipal ?: 0.0,
            termFrequency = domainModel.termFrequency ?: 0,
            termPeriodFrequencyType = domainModel.termPeriodFrequencyType ?: TermPeriodFrequencyType(),
            numberOfRepayments = domainModel.numberOfRepayments ?: 0,
            repaymentEvery = domainModel.repaymentEvery ?: 0,
            repaymentFrequencyType = domainModel.repaymentFrequencyType ?: RepaymentFrequencyType(),
            interestRatePerPeriod = domainModel.interestRatePerPeriod ?: 0.0,
            interestRateFrequencyType = domainModel.interestRateFrequencyType ?: InterestRateFrequencyType(),
            annualInterestRate = domainModel.annualInterestRate ?: 0.0,
            amortizationType = domainModel.amortizationType ?: AmortizationType(),
            interestType = domainModel.interestType ?: InterestType(),
            interestCalculationPeriodType = domainModel.interestCalculationPeriodType ?: InterestCalculationPeriodType(),
            transactionProcessingStrategyId = domainModel.transactionProcessingStrategyId ?: 0,
            transactionProcessingStrategyName = domainModel.transactionProcessingStrategyName ?: "",
            syncDisbursementWithMeeting = domainModel.syncDisbursementWithMeeting ?: false,
            timeline = domainModel.timeline?.toEntity() ?: LoanTimelineEntity(),
            summary = domainModel.summary?.toEntity() ?: LoanAccountSummaryEntity(),
            repaymentSchedule = domainModel.repaymentSchedule ?: RepaymentSchedule(),
            transactions = domainModel.transactions ?: emptyList(),
            feeChargesAtDisbursementCharged = domainModel.feeChargesAtDisbursementCharged ?: 0.0,
            totalOverpaid = domainModel.totalOverpaid ?: 0.0,
            loanCounter = domainModel.loanCounter ?: 0,
            loanProductCounter = domainModel.loanProductCounter ?: 0,
            multiDisburseLoan = domainModel.multiDisburseLoan ?: false,
            canDisburse = domainModel.canDisburse ?: false,
            inArrears = domainModel.inArrears ?: false,
            isNPA = domainModel.isNPA ?: false,
        )
    }
}

private fun LoanStatusEntity.toDomain(): LoanStatus {
    return LoanStatus(
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
}

private fun LoanTypeEntity.toDomain(): LoanType {
    return LoanType(
        id = id,
        code = code,
        value = value,
    )
}

private fun SavingAccountCurrencyEntity.toDomain(): SavingAccountCurrency {
    return SavingAccountCurrency(
        id = id,
        code = code,
        name = name,
        decimalPlaces = decimalPlaces,
        inMultiplesOf = inMultiplesOf,
        displaySymbol = displaySymbol,
        nameCode = nameCode,
        displayLabel = displayLabel,
    )
}

private fun LoanTimelineEntity.toDomain(): LoanTimeline {
    return LoanTimeline(
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
        actualDisburseDate = actualDisburseDate?.toDomain(),
        actualDisbursementDate = actualDisbursementDate,
        disbursedByUsername = disbursedByUsername,
        disbursedByFirstname = disbursedByFirstname,
        disbursedByLastname = disbursedByLastname,
        closedOnDate = closedOnDate,
        expectedMaturityDate = expectedMaturityDate,
    )
}

private fun ActualDisbursementDateEntity.toDomain(): ActualDisbursementDate {
    return ActualDisbursementDate(
        loanId = loanId,
        year = year,
        month = month,
        date = date,
    )
}

private fun LoanAccountSummaryEntity.toDomain(): LoanAccountSummary {
    return LoanAccountSummary(
        loanId = loanId,
        currency = currency?.toDomain(),
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
}
private fun LoanStatus.toEntity(): LoanStatusEntity {
    return LoanStatusEntity(
        id = id ?: 0,
        code = code,
        value = value,
        pendingApproval = pendingApproval ?: false,
        waitingForDisbursal = waitingForDisbursal ?: false,
        active = active ?: false,
        closedObligationsMet = closedObligationsMet ?: false,
        closedWrittenOff = closedWrittenOff ?: false,
        closedRescheduled = closedRescheduled ?: false,
        closed = closed ?: false,
        overpaid = overpaid ?: false,
    )
}

private fun LoanType.toEntity(): LoanTypeEntity {
    return LoanTypeEntity(
        id = id ?: 0,
        code = code,
        value = value,
    )
}

private fun SavingAccountCurrency.toEntity(): SavingAccountCurrencyEntity {
    return SavingAccountCurrencyEntity(
        id = id,
        code = code,
        name = name,
        decimalPlaces = decimalPlaces,
        inMultiplesOf = inMultiplesOf,
        displaySymbol = displaySymbol,
        nameCode = nameCode,
        displayLabel = displayLabel,
    )
}

private fun LoanTimeline.toEntity(): LoanTimelineEntity {
    return LoanTimelineEntity(
        loanId = loanId ?: 0,
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
    )
}

private fun ActualDisbursementDate.toEntity(): ActualDisbursementDateEntity {
    return ActualDisbursementDateEntity(
        loanId = loanId ?: 0,
        year = year,
        month = month,
        date = date,
    )
}

private fun LoanAccountSummary.toEntity(): LoanAccountSummaryEntity {
    return LoanAccountSummaryEntity(
        loanId = loanId ?: 0,
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
}
