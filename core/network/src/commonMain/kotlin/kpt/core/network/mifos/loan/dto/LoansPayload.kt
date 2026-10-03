/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable
import kpt.core.database.datatable.entity.DataTablePayload

/**
 * Created by nellyk on 2/20/2016.
 */

@Serializable
data class LoansPayload(
    var allowPartialPeriodInterestCalculation: Boolean? = null,
    var amortizationType: Int? = null,
    var clientId: Int? = null,
    var dateFormat: String? = null,
    var expectedDisbursementDate: String? = null,
    var interestCalculationPeriodType: Int? = null,
    var interestRatePerPeriod: Double? = null,
    var interestType: Int? = null,
    var loanTermFrequency: Int? = null,
    var loanTermFrequencyType: Int? = null,
    var loanType: String? = null,
    var locale: String? = null,
    var numberOfRepayments: Int? = null,
    var principal: Double? = null,
    var productId: Int? = null,
    var repaymentEvery: Int? = null,
    var repaymentFrequencyType: Int? = null,
    var repaymentFrequencyDayOfWeekType: Int? = null,
    var repaymentFrequencyNthDayType: Int? = null,
    var submittedOnDate: String? = null,
    var transactionProcessingStrategyCode: String? = null,
    var loanPurposeId: Int? = null,
    var loanOfficerId: Int? = null,
    var fundId: Int? = null,
    var linkAccountId: Int? = null,
    var dataTables: ArrayList<DataTablePayload>? = null,
    var externalId: String? = null,
)
