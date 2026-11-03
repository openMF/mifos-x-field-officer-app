/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable
import kpt.core.model.datatable.DataTablePayload

/**
 * Created by nellyk on 2/20/2016.
 */

@Serializable
data class LoansPayload(
    /** True when interest is calculated for a partial first period. */
    var allowPartialPeriodInterestCalculation: Boolean? = null,
    /** How the principal is amortised (equal installments or equal principal). */
    var amortizationType: Int? = null,
    /** The server id of the client. */
    var clientId: Int? = null,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat: String? = null,
    /** The date the loan is expected to be disbursed. */
    var expectedDisbursementDate: String? = null,
    /** How interest is calculated over a period (daily or same as the repayment period). */
    var interestCalculationPeriodType: Int? = null,
    /** The interest rate per interest period, in percent. */
    var interestRatePerPeriod: Double? = null,
    /** The interest method (declining balance or flat). */
    var interestType: Int? = null,
    /** The loan term, in [loanTermFrequencyType] units. */
    var loanTermFrequency: Int? = null,
    /** The unit of the loan term. */
    var loanTermFrequencyType: Int? = null,
    /** The loan type (individual, group or JLG). */
    var loanType: String? = null,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    var locale: String? = null,
    /** The number of installments. */
    var numberOfRepayments: Int? = null,
    /** The loan principal. */
    var principal: Double? = null,
    /** The server id of the product. */
    var productId: Int? = null,
    /** How many repayment-frequency units fall between two installments. */
    var repaymentEvery: Int? = null,
    /** The unit of the repayment frequency (days, weeks, months). */
    var repaymentFrequencyType: Int? = null,
    /** The weekday repayments fall on. */
    var repaymentFrequencyDayOfWeekType: Int? = null,
    /** Which occurrence in the month the repayment falls on (first, second, …). */
    var repaymentFrequencyNthDayType: Int? = null,
    /** The date the record was submitted. */
    var submittedOnDate: String? = null,
    /** The code of the repayment strategy. */
    var transactionProcessingStrategyCode: String? = null,
    /** The server id of the loan purpose. */
    var loanPurposeId: Int? = null,
    /** The server id of the loan officer. */
    var loanOfficerId: Int? = null,
    /** The server id of the fund the loan is financed from. */
    var fundId: Int? = null,
    /** The server id of the savings account linked for repayments. */
    var linkAccountId: Int? = null,
    /** The data tables registered for this entity. */
    var dataTables: ArrayList<DataTablePayload>? = null,
    /** The id an external system uses for this record, if one was set. */
    var externalId: String? = null,
)
