/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.recurringdeposit

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.recurring.AccountChart
import kpt.core.model.objects.template.recurring.Currency
import kpt.core.model.objects.template.recurring.FieldOfficerOption
import kpt.core.model.objects.template.recurring.Timeline
import kpt.core.model.objects.template.recurring.WithdrawalFeeTypeOption
import kpt.core.model.objects.template.recurring.charge.ChargeOption
import kpt.core.model.objects.template.recurring.deposit.DepositType
import kpt.core.model.objects.template.recurring.deposit.InMultiplesOfDepositTermType
import kpt.core.model.objects.template.recurring.deposit.MaxDepositTermType
import kpt.core.model.objects.template.recurring.deposit.MinDepositTermType
import kpt.core.model.objects.template.recurring.interest.InterestCalculationDaysInYearType
import kpt.core.model.objects.template.recurring.interest.InterestCalculationDaysInYearTypeOption
import kpt.core.model.objects.template.recurring.interest.InterestCalculationType
import kpt.core.model.objects.template.recurring.interest.InterestCalculationTypeOption
import kpt.core.model.objects.template.recurring.interest.InterestCompoundingPeriodType
import kpt.core.model.objects.template.recurring.interest.InterestCompoundingPeriodTypeOption
import kpt.core.model.objects.template.recurring.interest.InterestPostingPeriodType
import kpt.core.model.objects.template.recurring.interest.InterestPostingPeriodTypeOption
import kpt.core.model.objects.template.recurring.interest.PreClosurePenalInterestOnTypeOption
import kpt.core.model.objects.template.recurring.period.LockinPeriodFrequencyType
import kpt.core.model.objects.template.recurring.period.LockinPeriodFrequencyTypeOption
import kpt.core.model.objects.template.recurring.period.PeriodFrequencyTypeOption
import kpt.core.model.objects.template.recurring.period.ProductOption
@Serializable
data class RecurringDepositAccountTemplate(
    val accountChart: AccountChart? = null,
    val adjustAdvanceTowardsFuturePayments: Boolean? = null,
    val allowWithdrawal: Boolean? = null,
    val chargeOptions: List<ChargeOption>? = null,
    val clientId: Int? = null,
    val clientName: String? = null,
    val currency: Currency? = null,
    val depositProductId: Int? = null,
    val depositProductName: String? = null,
    val depositType: DepositType? = null,
    val fieldOfficerOptions: List<FieldOfficerOption>? = null,
    val inMultiplesOfDepositTermType: InMultiplesOfDepositTermType? = null,
    val interestCalculationDaysInYearType: InterestCalculationDaysInYearType? = null,
    val interestCalculationDaysInYearTypeOptions: List<InterestCalculationDaysInYearTypeOption>? = null,
    val interestCalculationType: InterestCalculationType? = null,
    val interestCalculationTypeOptions: List<InterestCalculationTypeOption>? = null,
    val interestCompoundingPeriodType: InterestCompoundingPeriodType? = null,
    val interestCompoundingPeriodTypeOptions: List<InterestCompoundingPeriodTypeOption>? = null,
    val interestPostingPeriodType: InterestPostingPeriodType? = null,
    val interestPostingPeriodTypeOptions: List<InterestPostingPeriodTypeOption>? = null,
    val isCalendarInherited: Boolean? = null,
    val isMandatoryDeposit: Boolean? = null,
    val lockinPeriodFrequency: Int? = null,
    val lockinPeriodFrequencyType: LockinPeriodFrequencyType? = null,
    val lockinPeriodFrequencyTypeOptions: List<LockinPeriodFrequencyTypeOption>? = null,
    val maxDepositTermType: MaxDepositTermType? = null,
    val minDepositTerm: Int? = null,
    val minDepositTermType: MinDepositTermType? = null,
    val nominalAnnualInterestRate: Double? = null,
    val periodFrequencyTypeOptions: List<PeriodFrequencyTypeOption>? = null,
    val preClosurePenalApplicable: Boolean? = null,
    val preClosurePenalInterestOnTypeOptions: List<PreClosurePenalInterestOnTypeOption>? = null,
    val productOptions: List<ProductOption>? = null,
    val timeline: Timeline? = null,
    val withHoldTax: Boolean? = null,
    val withdrawalFeeForTransfers: Boolean? = null,
    val withdrawalFeeTypeOptions: List<WithdrawalFeeTypeOption>? = null,
)
