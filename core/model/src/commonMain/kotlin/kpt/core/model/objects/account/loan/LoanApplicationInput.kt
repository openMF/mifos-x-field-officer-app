/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

import kotlinx.serialization.Serializable

/**
 * A new INDIVIDUAL loan application, as the new-application wizard assembles it.
 *
 * A `core/model` type rather than the `LoansPayload` network DTO, for the same reason the disburse,
 * transfer, charge and three reschedule inputs are: **a feature module must not depend on
 * `core:network`.** The impl maps this to `LoansPayload` at the boundary, which is also where the
 * `dataTables` member lives that no caller sets.
 *
 * Used for BOTH the schedule preview and the submit — the original built two near-identical
 * `LoansPayload`s and they had drifted (the preview copy guarded against a -1 option index and a
 * non-numeric rate; the submit copy did not). One input type makes that divergence unexpressible.
 *
 * ## [locale] and [dateFormat] are REQUIRED, not defaulted
 *
 * Deliberate, and the structural fix for a defect that recurred five times across this migration:
 * a date string formatted one way sent with a `dateFormat` declaring another. Fineract parses with
 * the declared format, so the pair is one decision and the type refuses to let a caller make half
 * of it. Build both with [kpt.core.model.utils.ApiDateFormatter].
 */
@Serializable
data class LoanApplicationInput(
    /** The server id of the client. */
    val clientId: Int,
    /** The server id of the product. */
    val productId: Int,
    /** The loan type (individual, group or JLG). */
    val loanType: String,
    /** The date the record was submitted. */
    val submittedOnDate: String,
    /** The date the loan is expected to be disbursed. */
    val expectedDisbursementDate: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,

    /** The loan principal. */
    val principal: Double?,
    /** The number of installments. */
    val numberOfRepayments: Int?,
    /** The interest rate per interest period, in percent. */
    val interestRatePerPeriod: Double?,
    /** How many repayment-frequency units fall between two installments. */
    val repaymentEvery: Int?,
    /** The loan term, in [loanTermFrequencyType] units. */
    val loanTermFrequency: Int?,
    /** The unit of the loan term. */
    val loanTermFrequencyType: Int?,
    /** The unit of the repayment frequency (days, weeks, months). */
    val repaymentFrequencyType: Int?,
    /** How the principal is amortised (equal installments or equal principal). */
    val amortizationType: Int?,
    /** The interest method (declining balance or flat). */
    val interestType: Int?,
    /** How interest is calculated over a period (daily or same as the repayment period). */
    val interestCalculationPeriodType: Int?,

    /** A CODE, not an id — the one option Fineract identifies that way on this endpoint. */
    val transactionProcessingStrategyCode: String?,
    /** True when interest is calculated for a partial first period. */
    val allowPartialPeriodInterestCalculation: Boolean = false,

    /** Null when the form offers no control for it, never `0` — `0` is an id that names no row. */
    val repaymentFrequencyDayOfWeekType: Int? = null,
    /** Which occurrence in the month the repayment falls on (first, second, …). */
    val repaymentFrequencyNthDayType: Int? = null,
    /** The server id of the loan purpose. */
    val loanPurposeId: Int? = null,
    /** The server id of the fund the loan is financed from. */
    val fundId: Int? = null,
    /** The server id of the loan officer. */
    val loanOfficerId: Int? = null,
    /** The server id of the savings account linked for repayments. */
    val linkAccountId: Int? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
)
