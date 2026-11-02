/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.schedule

import kpt.core.common.utils.CurrencyFormatter
import kpt.core.common.utils.DateHelper
import kpt.core.model.objects.account.loan.Period
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.RepaymentScheduleRowData
import kpt.core.model.objects.account.loan.RepaymentScheduleTableData
import kpt.core.model.objects.account.loan.RepaymentScheduleTotalsData

/**
 * `RepaymentSchedule` -> the table the schedule UI renders.
 *
 * Shared by TWO screens, which is why it is not private to either: the existing loan schedule
 * (`LoanScheduleViewModel`, reading a loan that exists) and the new-application wizard's preview
 * step (`newloan`, reading a schedule the server CALCULATED for an application not yet submitted).
 * Both render the same table from the same shape, so duplicating the mapping would be the
 * divergence this port already found twice — see the two drifted payload builders the new-loan
 * ViewModel replaced.
 */
internal fun toScheduleTable(
    allPeriods: List<Period>,
    actualPeriods: List<Period>,
    currencyCode: String,
    maxDigits: Int,
): RepaymentScheduleTableData = RepaymentScheduleTableData(
    // Fineract's first period is the DISBURSEMENT, identified by a null `period` number.
    disbursementRow = allPeriods.firstOrNull()
        ?.takeIf { it.period == null }
        ?.let { scheduleRow(it, number = "", currencyCode, maxDigits) },
    rows = actualPeriods.mapIndexed { index, period ->
        scheduleRow(period, number = (index + 1).toString(), currencyCode, maxDigits)
    },
    totals = scheduleTotals(actualPeriods, currencyCode, maxDigits),
    completeCount = RepaymentSchedule.getNumberOfRepaymentsComplete(actualPeriods),
    overdueCount = RepaymentSchedule.getNumberOfRepaymentsOverDue(actualPeriods),
    pendingCount = RepaymentSchedule.getNumberOfRepaymentsPending(actualPeriods),
)

/** Convenience for a whole schedule, which is how the preview step holds it. */
internal fun RepaymentSchedule.toScheduleTable(
    currencyCode: String,
    maxDigits: Int,
): RepaymentScheduleTableData = toScheduleTable(
    allPeriods = periods.orEmpty(),
    actualPeriods = getListOfActualPeriods(),
    currencyCode = currencyCode,
    maxDigits = maxDigits,
)

internal fun scheduleRow(
    period: Period,
    number: String,
    currencyCode: String,
    maxDigits: Int,
) = RepaymentScheduleRowData(
    number = number,
    days = period.daysInPeriod?.toString().orEmpty(),
    date = scheduleDate(period.dueDate),
    paidDate = scheduleDate(period.obligationsMetOnDate),
    balance = scheduleAmount(period.principalLoanBalanceOutstanding, currencyCode, maxDigits),
    principal = scheduleAmount(period.principalDue, currencyCode, maxDigits),
    interest = scheduleAmount(period.interestDue, currencyCode, maxDigits),
    fees = scheduleAmount(period.feeChargesDue, currencyCode, maxDigits),
    penalties = scheduleAmount(period.penaltyChargesDue, currencyCode, maxDigits),
    due = scheduleAmount(period.totalDueForPeriod, currencyCode, maxDigits),
    paid = scheduleAmount(period.totalPaidForPeriod, currencyCode, maxDigits),
    inAdvance = scheduleAmount(period.totalPaidInAdvanceForPeriod, currencyCode, maxDigits),
    late = scheduleAmount(period.totalPaidLateForPeriod, currencyCode, maxDigits),
    outstanding = scheduleAmount(period.totalOutstandingForPeriod, currencyCode, maxDigits),
)

internal fun scheduleTotals(
    periods: List<Period>,
    currencyCode: String,
    maxDigits: Int,
) = RepaymentScheduleTotalsData(
    principal = scheduleAmount(periods.sumOf { it.principalDue ?: 0.0 }, currencyCode, maxDigits),
    interest = scheduleAmount(periods.sumOf { it.interestDue ?: 0.0 }, currencyCode, maxDigits),
    fees = scheduleAmount(periods.sumOf { it.feeChargesDue ?: 0.0 }, currencyCode, maxDigits),
    penalties = scheduleAmount(periods.sumOf { it.penaltyChargesDue ?: 0.0 }, currencyCode, maxDigits),
    due = scheduleAmount(periods.sumOf { it.totalDueForPeriod ?: 0.0 }, currencyCode, maxDigits),
    paid = scheduleAmount(periods.sumOf { it.totalPaidForPeriod ?: 0.0 }, currencyCode, maxDigits),
    inAdvance = scheduleAmount(periods.sumOf { it.totalPaidInAdvanceForPeriod ?: 0.0 }, currencyCode, maxDigits),
    late = scheduleAmount(periods.sumOf { it.totalPaidLateForPeriod ?: 0.0 }, currencyCode, maxDigits),
    outstanding = scheduleAmount(periods.sumOf { it.totalOutstandingForPeriod ?: 0.0 }, currencyCode, maxDigits),
)

/** A null amount renders as an EMPTY cell, not as "0.00" — the original's distinction, kept. */
internal fun scheduleAmount(value: Double?, currencyCode: String, maxDigits: Int): String {
    value ?: return ""
    return CurrencyFormatter.format(value, currencyCode, maxDigits)
}

/**
 * A `[year, month, day]` triple → "14 Apr 2016". This is the DISPLAY format, so [DateHelper] is
 * the right helper here (unlike a request payload, where it would contradict the declared
 * `dateFormat` — see [kpt.feature.loan.approval.LoanApprovalViewModel]).
 *
 * Guarded on size: `getDateAsString` indexes `[2]` directly and Fineract omits `obligationsMetOnDate`
 * entirely on an unpaid period, which arrives here as a null list rather than a short one — but a
 * truncated list would crash it, and an unpaid row showing a blank paid-date is the correct render.
 */
internal fun scheduleDate(value: List<Int>?): String =
    value?.takeIf { it.size >= 3 }?.let { DateHelper.getDateAsString(it) }.orEmpty()
