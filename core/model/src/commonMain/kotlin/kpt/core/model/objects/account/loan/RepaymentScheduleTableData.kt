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

data class RepaymentScheduleTableData(
    val disbursementRow: RepaymentScheduleRowData?,
    val rows: List<RepaymentScheduleRowData>,
    val totals: RepaymentScheduleTotalsData,
    val completeCount: Int,
    val overdueCount: Int,
    val pendingCount: Int,
)

data class RepaymentScheduleRowData(
    val number: String,
    val days: String,
    val date: String,
    val paidDate: String,
    val balance: String,
    val principal: String,
    val interest: String,
    val fees: String,
    val penalties: String,
    val due: String,
    val paid: String,
    val inAdvance: String,
    val late: String,
    val outstanding: String,
)

data class RepaymentScheduleTotalsData(
    val principal: String,
    val interest: String,
    val fees: String,
    val penalties: String,
    val due: String,
    val paid: String,
    val inAdvance: String,
    val late: String,
    val outstanding: String,
)
