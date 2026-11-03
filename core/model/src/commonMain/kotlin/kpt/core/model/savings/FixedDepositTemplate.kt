/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.model.objects.account.saving.FieldOfficerOptions
import kpt.core.model.objects.template.recurring.AccountChart
import kpt.core.model.objects.template.recurring.Currency
import kpt.core.model.objects.template.recurring.MaturityInstructionOption
import kpt.core.model.objects.template.recurring.charge.ChargeOption
import kpt.core.model.objects.template.recurring.interest.InterestCalculationDaysInYearTypeOption
import kpt.core.model.objects.template.recurring.interest.InterestCalculationTypeOption
import kpt.core.model.objects.template.recurring.interest.InterestCompoundingPeriodTypeOption
import kpt.core.model.objects.template.recurring.interest.InterestPostingPeriodTypeOption
import kpt.core.model.objects.template.recurring.period.LockinPeriodFrequencyTypeOption
import kpt.core.model.objects.template.recurring.period.PeriodFrequencyTypeOption

/** Server template for a fixed deposit: the options its form offers. */
@Serializable
data class FixedDepositTemplate(

    /** The server id of the client. */
    @SerialName("clientId")
    val clientId: Int? = null,

    /** The client's display name. */
    @SerialName(value = "clientName")
    val clientName: String? = null,

    /** The currency the amounts are in. */
    @SerialName(value = "currency")
    val currency: Currency? = null,

    /** The products that may be chosen. */
    @SerialName("productOptions")
    val productOptions: List<FixedDepositProductOption>? = null,

    /** The field officers that may be assigned. */
    @SerialName("fieldOfficerOptions")
    val fieldOfficerOptions: List<FieldOfficerOptions>? = null,

    /** The period units that may be chosen. */
    @SerialName("periodFrequencyTypeOptions")
    val periodFrequencyTypeOptions: List<PeriodFrequencyTypeOption>? = null,

    /** The compounding period choices. */
    @SerialName("interestCompoundingPeriodTypeOptions")
    val interestCompoundingPeriodTypeOptions: List<InterestCompoundingPeriodTypeOption>? = null,

    /** The interest posting period choices. */
    @SerialName("interestPostingPeriodTypeOptions")
    val interestPostingPeriodTypeOptions: List<InterestPostingPeriodTypeOption>? = null,

    /** The days-in-year choices for interest calculation. */
    @SerialName("interestCalculationDaysInYearTypeOptions")
    val interestCalculationDaysInYearTypeOptions: List<InterestCalculationDaysInYearTypeOption>? = null,

    /** The interest calculation choices. */
    @SerialName("interestCalculationTypeOptions")
    val interestCalculationTypeOptions: List<InterestCalculationTypeOption>? = null,

    /** The units the lock-in period may be set in. */
    val lockinPeriodFrequencyTypeOptions: List<LockinPeriodFrequencyTypeOption>? = null,
    /** The maturity instructions that may be chosen. */
    val maturityInstructionOptions: List<MaturityInstructionOption>? = null,
    /** The interest-rate chart of the deposit. */
    val accountChart: AccountChart? = null,

    /** The charges that may be added. */
    val chargeOptions: List<ChargeOption>? = null,
    /** The savings accounts. */
    val savingsAccounts: List<SavingsAccountOption>? = null,
)
