/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.recurringdeposit

import kotlinx.serialization.Serializable

/**
 * A new recurring-deposit application.
 *
 * A `core/model` input rather than the `RecurringDepositAccountPayload` DTO, for the reason every
 * other write in this migration has one: a feature module must not depend on `core:network`, and
 * the impl maps at the boundary.
 *
 * ## [locale] and [dateFormat] are REQUIRED
 *
 * The original's pairing here was actually CORRECT — `DateHelper.SHORT_MONTH` alongside a date
 * built by `DateHelper.getDateAsStringFromLong`, both `dd-MM-yyyy`. It was correct by coincidence
 * of both halves coming from the same object, which is exactly the fragility this type removes:
 * the pair is one decision and a caller cannot make half of it. (Eight other screens in this
 * migration got the same pairing wrong; two sent raw epoch millis.)
 *
 * ## [productId] is required, not an index
 *
 * The original read it as `template.productOptions?.get(state.loanProductSelected)?.id` with
 * `loanProductSelected: Int = -1` and **no guard** — `get(-1)` throws, so submitting before
 * choosing a product crashed. Here the product is a value the form must have.
 */
@Serializable
data class RecurringDepositApplicationInput(
    val clientId: Int,
    val productId: Int,
    val submittedOnDate: String,
    val locale: String,
    val dateFormat: String,

    /** How much is deposited each period. */
    val mandatoryRecommendedDepositAmount: Double?,
    /** How often — a count plus the unit from the template's period-frequency options. */
    val recurringFrequency: Int?,
    val recurringFrequencyType: Int?,
    /** How long the deposit runs, same count + unit shape. */
    val depositPeriod: Int?,
    val depositPeriodFrequencyId: Int?,

    val fieldOfficerId: Int? = null,
    val externalId: String? = null,
    val isMandatoryDeposit: Boolean = false,
    val adjustAdvanceTowardsFuturePayments: Boolean = false,
    val allowWithdrawal: Boolean = false,

    val interestCompoundingPeriodType: Int? = null,
    val interestPostingPeriodType: Int? = null,
    val interestCalculationType: Int? = null,
    val interestCalculationDaysInYearType: Int? = null,
    val lockinPeriodFrequency: Int? = null,
    val lockinPeriodFrequencyType: Int? = null,

    val preClosurePenalApplicable: Boolean = false,
    val preClosurePenalInterest: Double? = null,
    val preClosurePenalInterestOnTypeId: Int? = null,

    /**
     * Charges attached to the application.
     *
     * `RecurringDepositAccountPayload` carries them, so this is real — unlike the LOAN
     * application, whose charges page was discarded on submit and had to be removed.
     */
    val charges: List<RecurringDepositChargeInput> = emptyList(),
)

/** One charge on a recurring-deposit application. */
@Serializable
data class RecurringDepositChargeInput(val chargeId: Int, val amount: String)
