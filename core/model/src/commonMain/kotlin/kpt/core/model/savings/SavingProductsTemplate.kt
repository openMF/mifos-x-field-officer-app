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
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** How often interest is compounded. */
    val interestCompoundingPeriodType: InterestType? = null,
    /** How often interest is posted to the account. */
    val interestPostingPeriodType: InterestType? = null,
    /** How interest is calculated on the balance (daily or average daily balance). */
    val interestCalculationType: InterestType? = null,
    /** How many days a year has for interest calculation (360, 365, actual). */
    val interestCalculationDaysInYearType: InterestType? = null,
    /** The accounting rule (none, cash or accrual). */
    val accountingRule: InterestType? = null,
    /** The currencies that may be chosen. */
    val currencyOptions: List<Currency>? = null,
    /** The compounding period choices. */
    val interestCompoundingPeriodTypeOptions: List<InterestType>? = null,
    /** The interest posting period choices. */
    val interestPostingPeriodTypeOptions: List<InterestType>? = null,
    /** The interest calculation choices. */
    val interestCalculationTypeOptions: List<InterestType>? = null,
    /** The days-in-year choices for interest calculation. */
    val interestCalculationDaysInYearTypeOptions: List<InterestType>? = null,
    /** The units the lock-in period may be set in. */
    val lockinPeriodFrequencyTypeOptions: List<InterestType>? = null,
    /** The withdrawal fee choices. */
    val withdrawalFeeTypeOptions: List<InterestType>? = null,
    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentTypeOption>? = null,
    /** The accounting rules that may be chosen. */
    val accountingRuleOptions: List<InterestType>? = null,
    /** The liability GL account that may be chosen. */
    val liabilityAccountOptions: AccountOptions? = null,
    /** The asset GL accounts that may be chosen. */
    val assetAccountOptions: List<AccountOptions>? = null,
    /** The expense GL accounts that may be chosen. */
    val expenseAccountOptions: List<AccountOptions>? = null,
    /** The income GL accounts that may be chosen. */
    val incomeAccountOptions: List<AccountOptions>? = null,
    /** The field officers that may be assigned. */
    val fieldOfficerOptions: List<FieldOfficerOptions>? = null,
    /** The charges that may be added. */
    val chargeOptions: List<ChargeOptions>? = null,
)
