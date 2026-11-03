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
import kpt.core.model.objects.payloads.ChargeItem

/** Request body for a fixed deposit. */
@Serializable
data class FixedDepositPayload(
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The server id of the product. */
    val productId: Int? = null,
    /** The server id of the field officer. */
    val fieldOfficerId: Int? = null,

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String? = null,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String? = null,
    /** The date the record was submitted. */
    val submittedOnDate: String? = null,

    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** The server id of the savings account interest is transferred to. */
    val transferToSavingsId: Int? = null,
    /** The server id of the savings account linked for repayments. */
    val linkAccountId: Int? = null,

    /** How often interest is compounded. */
    val interestCompoundingPeriodType: Int? = null,
    /** How often interest is posted to the account. */
    val interestPostingPeriodType: Int? = null,
    /** How interest is calculated on the balance (daily or average daily balance). */
    val interestCalculationType: Int? = null,
    /** How many days a year has for interest calculation (360, 365, actual). */
    val interestCalculationDaysInYearType: Int? = null,

    /** The deposit amount. */
    val depositAmount: Double? = null,
    /** The length of the deposit term, in [depositPeriodFrequencyId] units. */
    val depositPeriod: Int? = null,
    /** The unit of the deposit term. */
    val depositPeriodFrequencyId: Int? = null,

    /** The length of the lock-in period, in [lockinPeriodFrequencyType] units. */
    val lockinPeriodFrequency: Int? = null,
    /** The unit of the lock-in period. */
    val lockinPeriodFrequencyType: Int? = null,

    /** The charges. */
    val charges: List<ChargeItem> = emptyList(),

    /** True when interest is transferred to a savings account. */
    val transferInterestToSavings: Boolean = false,

    /** True when a penalty applies if the deposit is closed early. */
    val preClosurePenalApplicable: Boolean = false,
    /** The penalty interest rate applied on early closure. */
    val preClosurePenalInterest: Double? = null,
    /** What the early-closure penalty interest is applied to. */
    val preClosurePenalInterestOnTypeId: Int? = null,

    /** What happens to the deposit at maturity. */
    val maturityInstructionId: Int? = null,
)
