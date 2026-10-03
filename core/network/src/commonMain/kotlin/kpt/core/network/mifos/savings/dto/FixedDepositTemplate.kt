/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.savings.dto

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

@Serializable
data class FixedDepositTemplate(

    @SerialName("clientId")
    val clientId: Int? = null,

    @SerialName(value = "clientName")
    val clientName: String? = null,

    @SerialName(value = "currency")
    val currency: Currency? = null,

    @SerialName("productOptions")
    val productOptions: List<FixedDepositProductOption>? = null,

    @SerialName("fieldOfficerOptions")
    val fieldOfficerOptions: List<FieldOfficerOptions>? = null,

    @SerialName("periodFrequencyTypeOptions")
    val periodFrequencyTypeOptions: List<PeriodFrequencyTypeOption>? = null,

    @SerialName("interestCompoundingPeriodTypeOptions")
    val interestCompoundingPeriodTypeOptions: List<InterestCompoundingPeriodTypeOption>? = null,

    @SerialName("interestPostingPeriodTypeOptions")
    val interestPostingPeriodTypeOptions: List<InterestPostingPeriodTypeOption>? = null,

    @SerialName("interestCalculationDaysInYearTypeOptions")
    val interestCalculationDaysInYearTypeOptions: List<InterestCalculationDaysInYearTypeOption>? = null,

    @SerialName("interestCalculationTypeOptions")
    val interestCalculationTypeOptions: List<InterestCalculationTypeOption>? = null,

    val lockinPeriodFrequencyTypeOptions: List<LockinPeriodFrequencyTypeOption>? = null,
    val maturityInstructionOptions: List<MaturityInstructionOption>? = null,
    val accountChart: AccountChart? = null,

    val chargeOptions: List<ChargeOption>? = null,
    val savingsAccounts: List<SavingsAccountOption>? = null,
)
