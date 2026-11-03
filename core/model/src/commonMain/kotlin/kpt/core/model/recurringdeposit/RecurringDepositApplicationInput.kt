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
    /** The server id of the client. */
    val clientId: Int,
    /** The recurring-deposit product. */
    val productId: Int,
    /** The date the record was submitted. */
    val submittedOnDate: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,

    /** How much is deposited each period. */
    val mandatoryRecommendedDepositAmount: Double?,
    /** How often — a count plus the unit from the template's period-frequency options. */
    val recurringFrequency: Int?,
    /** The unit of the deposit frequency. */
    val recurringFrequencyType: Int?,
    /** How long the deposit runs, same count + unit shape. */
    val depositPeriod: Int?,
    /** The unit of the deposit term. */
    val depositPeriodFrequencyId: Int?,

    /** The server id of the field officer. */
    val fieldOfficerId: Int? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** True when the recurring deposit is mandatory. */
    val isMandatoryDeposit: Boolean = false,
    /** True when an advance deposit is applied to future installments. */
    val adjustAdvanceTowardsFuturePayments: Boolean = false,
    /** True when withdrawals are allowed before maturity. */
    val allowWithdrawal: Boolean = false,

    /** How often interest is compounded. */
    val interestCompoundingPeriodType: Int? = null,
    /** How often interest is posted to the account. */
    val interestPostingPeriodType: Int? = null,
    /** How interest is calculated on the balance (daily or average daily balance). */
    val interestCalculationType: Int? = null,
    /** How many days a year has for interest calculation (360, 365, actual). */
    val interestCalculationDaysInYearType: Int? = null,
    /** The length of the lock-in period, in [lockinPeriodFrequencyType] units. */
    val lockinPeriodFrequency: Int? = null,
    /** The unit of the lock-in period. */
    val lockinPeriodFrequencyType: Int? = null,

    /** True when a penalty applies if the deposit is closed early. */
    val preClosurePenalApplicable: Boolean = false,
    /** The penalty interest rate applied on early closure. */
    val preClosurePenalInterest: Double? = null,
    /** What the early-closure penalty interest is applied to. */
    val preClosurePenalInterestOnTypeId: Int? = null,

    /**
     * Charges attached to the application.
     *
     * `RecurringDepositAccountPayload` carries them, so this is real — unlike the LOAN
     * application, whose charges page was discarded on submit and had to be removed.
     */
    val charges: List<RecurringDepositChargeInput> = emptyList(),
)

/**
 * One charge on a recurring-deposit application.
 *
 * @property amount the amount, in the record's currency
 * @property chargeId the server id of the charge
 */
@Serializable
data class RecurringDepositChargeInput(val chargeId: Int, val amount: String)
