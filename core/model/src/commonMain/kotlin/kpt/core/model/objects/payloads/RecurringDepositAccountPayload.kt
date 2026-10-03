/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.payloads

import kotlinx.serialization.Serializable

@Serializable
data class RecurringDepositAccountPayload(
    val charges: List<ChargeItem>? = null,
    val adjustAdvanceTowardsFuturePayments: Boolean? = null,
    val allowWithdrawal: Boolean? = null,
    val clientId: Int? = null,
    val dateFormat: String? = null,
    val depositPeriod: Int? = null,
    val depositPeriodFrequencyId: Int? = null,
    val expectedFirstDepositOnDate: String? = null,
    val externalId: String? = null,
    val fieldOfficerId: Int? = null,
    val interestCalculationDaysInYearType: Int? = null,
    val interestCalculationType: Int? = null,
    val interestCompoundingPeriodType: Int? = null,
    val interestPostingPeriodType: Int? = null,
    val isCalendarInherited: Boolean? = null,
    val isMandatoryDeposit: Boolean? = null,
    val locale: String? = null,
    val lockinPeriodFrequency: Int? = null,
    val lockinPeriodFrequencyType: Int? = null,
    val mandatoryRecommendedDepositAmount: Double? = null,
    val monthDayFormat: String? = null,
    val productId: Int? = null,
    val recurringFrequency: Int? = null,
    val recurringFrequencyType: Int? = null,
    val submittedOnDate: String? = null,
    val preClosurePenalApplicable: Boolean? = null,
    val preClosurePenalInterest: Double? = null,
    val preClosurePenalInterestOnTypeId: Int? = null,
)

@Serializable
data class ChargeItem(
    val chargeId: Int? = null,
    val amount: Double? = null,
)
