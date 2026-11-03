/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
/** Server template for a recurring deposit account: the options its form offers. */
@Serializable
data class RecurringDepositAccountTemplate(
    /** The interest-rate chart of the deposit. */
    val accountChart: AccountChart? = null,
    /** True when an advance deposit is applied to future installments. */
    val adjustAdvanceTowardsFuturePayments: Boolean? = null,
    /** True when withdrawals are allowed before maturity. */
    val allowWithdrawal: Boolean? = null,
    /** The charges that may be added. */
    val chargeOptions: List<ChargeOption>? = null,
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The client's display name. */
    val clientName: String? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** The server id of the deposit product. */
    val depositProductId: Int? = null,
    /** The name of the deposit product. */
    val depositProductName: String? = null,
    /** The deposit product type (savings, fixed deposit or recurring deposit). */
    val depositType: DepositType? = null,
    /** The field officers that may be assigned. */
    val fieldOfficerOptions: List<FieldOfficerOption>? = null,
    /** The unit the deposit term is counted in multiples of. */
    val inMultiplesOfDepositTermType: InMultiplesOfDepositTermType? = null,
    /** How many days a year has for interest calculation (360, 365, actual). */
    val interestCalculationDaysInYearType: InterestCalculationDaysInYearType? = null,
    /** The days-in-year choices for interest calculation. */
    val interestCalculationDaysInYearTypeOptions: List<InterestCalculationDaysInYearTypeOption>? = null,
    /** How interest is calculated on the balance (daily or average daily balance). */
    val interestCalculationType: InterestCalculationType? = null,
    /** The interest calculation choices. */
    val interestCalculationTypeOptions: List<InterestCalculationTypeOption>? = null,
    /** How often interest is compounded. */
    val interestCompoundingPeriodType: InterestCompoundingPeriodType? = null,
    /** The compounding period choices. */
    val interestCompoundingPeriodTypeOptions: List<InterestCompoundingPeriodTypeOption>? = null,
    /** How often interest is posted to the account. */
    val interestPostingPeriodType: InterestPostingPeriodType? = null,
    /** The interest posting period choices. */
    val interestPostingPeriodTypeOptions: List<InterestPostingPeriodTypeOption>? = null,
    /** True when the deposit follows the group's meeting calendar. */
    val isCalendarInherited: Boolean? = null,
    /** True when the recurring deposit is mandatory. */
    val isMandatoryDeposit: Boolean? = null,
    /** The length of the lock-in period, in [lockinPeriodFrequencyType] units. */
    val lockinPeriodFrequency: Int? = null,
    /** The unit of the lock-in period. */
    val lockinPeriodFrequencyType: LockinPeriodFrequencyType? = null,
    /** The units the lock-in period may be set in. */
    val lockinPeriodFrequencyTypeOptions: List<LockinPeriodFrequencyTypeOption>? = null,
    /** The unit of the maximum deposit term. */
    val maxDepositTermType: MaxDepositTermType? = null,
    /** The shortest deposit term. */
    val minDepositTerm: Int? = null,
    /** The unit of the minimum deposit term. */
    val minDepositTermType: MinDepositTermType? = null,
    /** The nominal annual interest rate, in percent. */
    val nominalAnnualInterestRate: Double? = null,
    /** The period units that may be chosen. */
    val periodFrequencyTypeOptions: List<PeriodFrequencyTypeOption>? = null,
    /** True when a penalty applies if the deposit is closed early. */
    val preClosurePenalApplicable: Boolean? = null,
    /** What the early-closure penalty interest may be applied to. */
    val preClosurePenalInterestOnTypeOptions: List<PreClosurePenalInterestOnTypeOption>? = null,
    /** The products that may be chosen. */
    val productOptions: List<ProductOption>? = null,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: Timeline? = null,
    /** True when tax is withheld on interest. */
    val withHoldTax: Boolean? = null,
    /** True when the withdrawal fee also applies to transfers. */
    val withdrawalFeeForTransfers: Boolean? = null,
    /** The withdrawal fee choices. */
    val withdrawalFeeTypeOptions: List<WithdrawalFeeTypeOption>? = null,
)
