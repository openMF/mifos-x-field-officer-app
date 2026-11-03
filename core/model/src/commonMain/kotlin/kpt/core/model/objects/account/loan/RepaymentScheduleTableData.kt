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

/** A repayment schedule table data. */
data class RepaymentScheduleTableData(
    /** The schedule's disbursement row. */
    val disbursementRow: RepaymentScheduleRowData?,
    /** The schedule's rows. */
    val rows: List<RepaymentScheduleRowData>,
    /** The schedule's totals row. */
    val totals: RepaymentScheduleTotalsData,
    /** The number of fully paid installments. */
    val completeCount: Int,
    /** The number of overdue installments. */
    val overdueCount: Int,
    /** The number of installments still to pay. */
    val pendingCount: Int,
)

/** A repayment schedule row data. */
data class RepaymentScheduleRowData(
    /** The installment number, formatted for display. */
    val number: String,
    /** The days in the installment, formatted for display. */
    val days: String,
    /** The date. */
    val date: String,
    /** The date the installment was paid, formatted for display. */
    val paidDate: String,
    /** The balance. */
    val balance: String,
    /** The loan principal. */
    val principal: String,
    /** The interest, formatted for display. */
    val interest: String,
    /** The fees, formatted for display. */
    val fees: String,
    /** The penalties, formatted for display. */
    val penalties: String,
    /** The amount due, formatted for display. */
    val due: String,
    /** True when it has been paid. */
    val paid: String,
    /** The amount paid in advance, formatted for display. */
    val inAdvance: String,
    /** The amount paid late, formatted for display. */
    val late: String,
    /** The amount outstanding. */
    val outstanding: String,
)

/** A repayment schedule totals data. */
data class RepaymentScheduleTotalsData(
    /** The loan principal. */
    val principal: String,
    /** The interest, formatted for display. */
    val interest: String,
    /** The fees, formatted for display. */
    val fees: String,
    /** The penalties, formatted for display. */
    val penalties: String,
    /** The amount due, formatted for display. */
    val due: String,
    /** True when it has been paid. */
    val paid: String,
    /** The amount paid in advance, formatted for display. */
    val inAdvance: String,
    /** The amount paid late, formatted for display. */
    val late: String,
    /** The amount outstanding. */
    val outstanding: String,
)
