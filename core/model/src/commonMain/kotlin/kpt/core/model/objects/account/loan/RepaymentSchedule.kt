/*
 * Copyright 2024 Mifos Initiative
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
 * Created by ishankhanna on 19/06/14.
 */
@Serializable
data class RepaymentSchedule(
    /** The currency the amounts are in. */
    var currency: Currency? = null,

    /** The loan term, in days. */
    var loanTermInDays: Int? = null,

    /** The installments. */
    var periods: List<Period>? = null,

    /** The total fees charged. */
    var totalFeeChargesCharged: Double? = null,

    /** The total interest charged. */
    var totalInterestCharged: Double? = null,

    /** The total amount outstanding. */
    var totalOutstanding: Double? = null,

    /** The total paid in advance. */
    var totalPaidInAdvance: Double? = null,

    /** The total paid late. */
    var totalPaidLate: Double? = null,

    /** The total penalties charged. */
    var totalPenaltyChargesCharged: Double? = null,

    /** The total principal disbursed. */
    var totalPrincipalDisbursed: Double? = null,

    /** The total principal expected. */
    var totalPrincipalExpected: Double? = null,

    /** The total principal paid. */
    var totalPrincipalPaid: Double? = null,

    /** The total repaid. */
    var totalRepayment: Double? = null,

    /** The total repayment expected. */
    var totalRepaymentExpected: Double? = null,

    /** The total waived. */
    var totalWaived: Double? = null,

    /** The total written off. */
    var totalWrittenOff: Double? = null,
) {

    /** The repayment periods, without the leading disbursement period. */
    fun getListOfActualPeriods(): List<Period> {
        val list = periods ?: return emptyList()
        return if (list.size > 1) list.subList(1, list.size).toList() else emptyList()
    }

    /** Constants and factories of `RepaymentSchedule`. */
    companion object {

        // Helper Method to get total completed repayments
        /** How many of the periods are fully repaid. */
        fun getNumberOfRepaymentsComplete(periodList: List<Period>): Int {
            var count = 0
            for (period in periodList) {
                if (period.complete == true) count++
            }
            return count
        }

        /**
         * Helper Method to get total pending/upcoming repayments.
         *
         * `complete != true` rather than `!complete!!`: the flag is nullable and the `!!` crashed
         * the whole schedule screen on a period Fineract sent without it. The two agree on every
         * non-null value, so the counts are unchanged — a period with an absent flag is counted
         * as not-complete, which is what the `!!` would have meant had it not thrown.
         */
        fun getNumberOfRepaymentsPending(periodList: List<Period>): Int {
            var count = 0
            for (period in periodList) {
                if (period.complete != true) count++
            }
            return count
        }

        // Helper Method to get total repayments overdue
        /** How many of the periods are unpaid with an amount overdue. */
        fun getNumberOfRepaymentsOverDue(periodList: List<Period>): Int {
            var count = 0
            for (period in periodList) {
                val overdue = period.totalOverdue
                if (overdue != null && overdue > 0 && period.complete != true) {
                    count++
                }
            }
            return count
        }
    }
}
