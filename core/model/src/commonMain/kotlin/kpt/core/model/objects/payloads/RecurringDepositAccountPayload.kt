/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.payloads

import kotlinx.serialization.Serializable

/** Request body for a recurring deposit account. */
@Serializable
data class RecurringDepositAccountPayload(
    /** The charges. */
    val charges: List<ChargeItem>? = null,
    /** True when an advance deposit is applied to future installments. */
    val adjustAdvanceTowardsFuturePayments: Boolean? = null,
    /** True when withdrawals are allowed before maturity. */
    val allowWithdrawal: Boolean? = null,
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String? = null,
    /** The length of the deposit term, in [depositPeriodFrequencyId] units. */
    val depositPeriod: Int? = null,
    /** The unit of the deposit term. */
    val depositPeriodFrequencyId: Int? = null,
    /** The date the first deposit is expected. */
    val expectedFirstDepositOnDate: String? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** The server id of the field officer. */
    val fieldOfficerId: Int? = null,
    /** How many days a year has for interest calculation (360, 365, actual). */
    val interestCalculationDaysInYearType: Int? = null,
    /** How interest is calculated on the balance (daily or average daily balance). */
    val interestCalculationType: Int? = null,
    /** How often interest is compounded. */
    val interestCompoundingPeriodType: Int? = null,
    /** How often interest is posted to the account. */
    val interestPostingPeriodType: Int? = null,
    /** True when the deposit follows the group's meeting calendar. */
    val isCalendarInherited: Boolean? = null,
    /** True when the recurring deposit is mandatory. */
    val isMandatoryDeposit: Boolean? = null,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String? = null,
    /** The length of the lock-in period, in [lockinPeriodFrequencyType] units. */
    val lockinPeriodFrequency: Int? = null,
    /** The unit of the lock-in period. */
    val lockinPeriodFrequencyType: Int? = null,
    /** The deposit amount each period, mandatory or recommended. */
    val mandatoryRecommendedDepositAmount: Double? = null,
    /** The pattern Fineract parses month-day values with. */
    val monthDayFormat: String? = null,
    /** The server id of the product. */
    val productId: Int? = null,
    /** How many frequency units between deposits. */
    val recurringFrequency: Int? = null,
    /** The unit of the deposit frequency. */
    val recurringFrequencyType: Int? = null,
    /** The date the record was submitted. */
    val submittedOnDate: String? = null,
    /** True when a penalty applies if the deposit is closed early. */
    val preClosurePenalApplicable: Boolean? = null,
    /** The penalty interest rate applied on early closure. */
    val preClosurePenalInterest: Double? = null,
    /** What the early-closure penalty interest is applied to. */
    val preClosurePenalInterestOnTypeId: Int? = null,
)

/** A charge item. */
@Serializable
data class ChargeItem(
    /** The server id of the charge. */
    val chargeId: Int? = null,
    /** The amount, in the record's currency. */
    val amount: Double? = null,
)
