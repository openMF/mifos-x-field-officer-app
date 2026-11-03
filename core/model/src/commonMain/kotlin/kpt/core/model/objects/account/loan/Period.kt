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
data class Period(
    /** True when the installment is fully paid. */
    var complete: Boolean? = null,

    /** The number of days in the installment. */
    var daysInPeriod: Int? = null,

    /** The date the amount is due. */
    var dueDate: List<Int>? = null,

    /** The fees due for the installment. */
    var feeChargesDue: Double? = null,

    /** The fees still outstanding. */
    var feeChargesOutstanding: Double? = null,

    /** The fees already paid. */
    var feeChargesPaid: Double? = null,

    /** The fees waived. */
    var feeChargesWaived: Double? = null,

    /** The fees written off. */
    var feeChargesWrittenOff: Double? = null,

    /** The date this starts from. */
    var fromDate: List<Int>? = null,

    /** The interest due. */
    var interestDue: Double? = null,

    /** The interest originally due for the installment. */
    var interestOriginalDue: Double? = null,

    /** The interest still outstanding. */
    var interestOutstanding: Double? = null,

    /** The interest already paid. */
    var interestPaid: Double? = null,

    /** The interest waived. */
    var interestWaived: Double? = null,

    /** The interest written off. */
    var interestWrittenOff: Double? = null,

    /** The date the installment was fully paid. */
    var obligationsMetOnDate: List<Int>? = null,

    /** The penalties due for the installment. */
    var penaltyChargesDue: Double? = null,

    /** The penalties still outstanding. */
    var penaltyChargesOutstanding: Double? = null,

    /** The penalties already paid. */
    var penaltyChargesPaid: Double? = null,

    /** The penalties waived. */
    var penaltyChargesWaived: Double? = null,

    /** The penalties written off. */
    var penaltyChargesWrittenOff: Double? = null,

    /** The installment number. */
    var period: Int? = null,

    /** The principal due. */
    var principalDue: Double? = null,

    /** The principal balance outstanding after the installment. */
    var principalLoanBalanceOutstanding: Double? = null,

    /** The principal originally due for the installment. */
    var principalOriginalDue: Double? = null,

    /** The principal still outstanding. */
    var principalOutstanding: Double? = null,

    /** The principal already paid. */
    var principalPaid: Double? = null,

    /** The principal written off. */
    var principalWrittenOff: Double? = null,

    /** The actual cost of the loan for the installment. */
    var totalActualCostOfLoanForPeriod: Double? = null,

    /** The total due for the installment. */
    var totalDueForPeriod: Double? = null,

    /** The total originally due for the installment. */
    var totalOriginalDueForPeriod: Double? = null,

    /** The total outstanding for the installment. */
    var totalOutstandingForPeriod: Double? = null,

    /** The total amount overdue. */
    var totalOverdue: Double? = null,

    /** The total paid for the installment. */
    var totalPaidForPeriod: Double? = null,

    /** The amount paid in advance for the installment. */
    var totalPaidInAdvanceForPeriod: Double? = null,

    /** The amount paid late for the installment. */
    var totalPaidLateForPeriod: Double? = null,

    /** The amount waived for the installment. */
    var totalWaivedForPeriod: Double? = null,

    /** The amount written off for the installment. */
    var totalWrittenOffForPeriod: Double? = null,
)
