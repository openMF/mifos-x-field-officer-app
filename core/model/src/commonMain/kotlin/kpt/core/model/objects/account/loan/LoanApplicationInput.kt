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
    val clientId: Int,
    val productId: Int,
    val loanType: String,
    val submittedOnDate: String,
    val expectedDisbursementDate: String,
    val locale: String,
    val dateFormat: String,

    val principal: Double?,
    val numberOfRepayments: Int?,
    val interestRatePerPeriod: Double?,
    val repaymentEvery: Int?,
    val loanTermFrequency: Int?,
    val loanTermFrequencyType: Int?,
    val repaymentFrequencyType: Int?,
    val amortizationType: Int?,
    val interestType: Int?,
    val interestCalculationPeriodType: Int?,

    /** A CODE, not an id — the one option Fineract identifies that way on this endpoint. */
    val transactionProcessingStrategyCode: String?,
    val allowPartialPeriodInterestCalculation: Boolean = false,

    /** Null when the form offers no control for it, never `0` — `0` is an id that names no row. */
    val repaymentFrequencyDayOfWeekType: Int? = null,
    val repaymentFrequencyNthDayType: Int? = null,
    val loanPurposeId: Int? = null,
    val fundId: Int? = null,
    val loanOfficerId: Int? = null,
    val linkAccountId: Int? = null,
    val externalId: String? = null,
)
