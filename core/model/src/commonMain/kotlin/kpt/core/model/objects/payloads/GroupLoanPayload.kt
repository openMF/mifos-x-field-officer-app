/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.payloads

import kotlinx.serialization.Serializable

/**
 * Created by nellyk on 2/20/2016.
 */
@Serializable
data class GroupLoanPayload(
    var isAllowPartialPeriodInterestCalculation: Boolean? = null,
    var amortizationType: Int? = null,
    var groupId: Int? = null,
    var dateFormat: String? = null,

    /* public String getDisbursementData() {
       return disbursementData;
   }

   public void setDisbursementData(String disbursementData) {
       this.disbursementData = disbursementData;
   }*/
    var expectedDisbursementDate: String? = null,
    var interestCalculationPeriodType: Int? = null,
    var interestRatePerPeriod: Double? = null,
    var interestType: Int? = null,
    var loanTermFrequency: Int? = null,
    var loanTermFrequencyType: Int? = null,
    var repaymentFrequencyDayOfWeekType: Int? = null,
    var repaymentFrequencyNthDayType: Int? = null,
    var loanType: String? = null,
    var locale: String? = null,
    var numberOfRepayments: String? = null,
    var principal: String? = null,
    var productId: Int? = null,
    var repaymentEvery: String? = null,
    var repaymentFrequencyType: Int? = null,
    var submittedOnDate: String? = null,
    var transactionProcessingStrategyId: Int? = null,
    var loanPurposeId: Int? = null,
    var linkAccountId: Int? = null,
)
