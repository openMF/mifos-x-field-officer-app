/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

import kotlinx.serialization.Serializable

/**
 * Created by ishankhanna on 19/06/14.
 */
@Serializable
data class RepaymentSchedule(
    var currency: Currency? = null,

    var loanTermInDays: Int? = null,

    var periods: List<Period>? = null,

    var totalFeeChargesCharged: Double? = null,

    var totalInterestCharged: Double? = null,

    var totalOutstanding: Double? = null,

    var totalPaidInAdvance: Double? = null,

    var totalPaidLate: Double? = null,

    var totalPenaltyChargesCharged: Double? = null,

    var totalPrincipalDisbursed: Double? = null,

    var totalPrincipalExpected: Double? = null,

    var totalPrincipalPaid: Double? = null,

    var totalRepayment: Double? = null,

    var totalRepaymentExpected: Double? = null,

    var totalWaived: Double? = null,

    var totalWrittenOff: Double? = null,
) {

    fun getListOfActualPeriods(): List<Period> {
        val list = periods ?: return emptyList()
        return if (list.size > 1) list.subList(1, list.size).toList() else emptyList()
    }

    companion object {

        // Helper Method to get total completed repayments
        fun getNumberOfRepaymentsComplete(periodList: List<Period>): Int {
            var count = 0
            for (period in periodList) {
                if (period.complete == true) count++
            }
            return count
        }

        // Helper Method to get total pending/upcoming repayments
        fun getNumberOfRepaymentsPending(periodList: List<Period>): Int {
            var count = 0
            for (period in periodList) {
                if (!period.complete!!) count++
            }
            return count
        }

        // Helper Method to get total repayments overdue
        fun getNumberOfRepaymentsOverDue(periodList: List<Period>): Int {
            var count = 0
            for (period in periodList) {
                if (period.totalOverdue != null && period.totalOverdue!! > 0 && !period.complete!!) {
                    count++
                }
            }
            return count
        }
    }
}
