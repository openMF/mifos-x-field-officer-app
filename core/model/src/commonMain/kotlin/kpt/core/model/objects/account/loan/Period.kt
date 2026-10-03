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
data class Period(
    var complete: Boolean? = null,

    var daysInPeriod: Int? = null,

    var dueDate: List<Int>? = null,

    var feeChargesDue: Double? = null,

    var feeChargesOutstanding: Double? = null,

    var feeChargesPaid: Double? = null,

    var feeChargesWaived: Double? = null,

    var feeChargesWrittenOff: Double? = null,

    var fromDate: List<Int>? = null,

    var interestDue: Double? = null,

    var interestOriginalDue: Double? = null,

    var interestOutstanding: Double? = null,

    var interestPaid: Double? = null,

    var interestWaived: Double? = null,

    var interestWrittenOff: Double? = null,

    var obligationsMetOnDate: List<Int>? = null,

    var penaltyChargesDue: Double? = null,

    var penaltyChargesOutstanding: Double? = null,

    var penaltyChargesPaid: Double? = null,

    var penaltyChargesWaived: Double? = null,

    var penaltyChargesWrittenOff: Double? = null,

    var period: Int? = null,

    var principalDue: Double? = null,

    var principalLoanBalanceOutstanding: Double? = null,

    var principalOriginalDue: Double? = null,

    var principalOutstanding: Double? = null,

    var principalPaid: Double? = null,

    var principalWrittenOff: Double? = null,

    var totalActualCostOfLoanForPeriod: Double? = null,

    var totalDueForPeriod: Double? = null,

    var totalOriginalDueForPeriod: Double? = null,

    var totalOutstandingForPeriod: Double? = null,

    var totalOverdue: Double? = null,

    var totalPaidForPeriod: Double? = null,

    var totalPaidInAdvanceForPeriod: Double? = null,

    var totalPaidLateForPeriod: Double? = null,

    var totalWaivedForPeriod: Double? = null,

    var totalWrittenOffForPeriod: Double? = null,
)
