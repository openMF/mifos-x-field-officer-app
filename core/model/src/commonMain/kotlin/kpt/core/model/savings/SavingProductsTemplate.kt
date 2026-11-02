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

import kotlinx.serialization.Serializable
import kpt.core.model.objects.account.loan.Currency
import kpt.core.model.objects.account.saving.FieldOfficerOptions
import kpt.core.model.objects.commonfiles.InterestType
import kpt.core.model.objects.template.client.ChargeOptions
import kpt.core.model.objects.template.saving.AccountOptions
import kpt.core.model.payment.PaymentTypeOption

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingProductsTemplate`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class SavingProductsTemplate(
    val currency: Currency? = null,
    val interestCompoundingPeriodType: InterestType? = null,
    val interestPostingPeriodType: InterestType? = null,
    val interestCalculationType: InterestType? = null,
    val interestCalculationDaysInYearType: InterestType? = null,
    val accountingRule: InterestType? = null,
    val currencyOptions: List<Currency>? = null,
    val interestCompoundingPeriodTypeOptions: List<InterestType>? = null,
    val interestPostingPeriodTypeOptions: List<InterestType>? = null,
    val interestCalculationTypeOptions: List<InterestType>? = null,
    val interestCalculationDaysInYearTypeOptions: List<InterestType>? = null,
    val lockinPeriodFrequencyTypeOptions: List<InterestType>? = null,
    val withdrawalFeeTypeOptions: List<InterestType>? = null,
    val paymentTypeOptions: List<PaymentTypeOption>? = null,
    val accountingRuleOptions: List<InterestType>? = null,
    val liabilityAccountOptions: AccountOptions? = null,
    val assetAccountOptions: List<AccountOptions>? = null,
    val expenseAccountOptions: List<AccountOptions>? = null,
    val incomeAccountOptions: List<AccountOptions>? = null,
    val fieldOfficerOptions: List<FieldOfficerOptions>? = null,
    val chargeOptions: List<ChargeOptions>? = null,
)
