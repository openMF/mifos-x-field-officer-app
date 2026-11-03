/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.dashboard

import kpt.core.model.dashboard.DueTodayLoan
import kpt.core.network.mifos.dashboard.dto.CurrencyRefDto
import kpt.core.network.mifos.dashboard.dto.LoanScheduleDto
import kpt.core.network.mifos.dashboard.dto.RepaymentScheduleDto
import kpt.core.network.mifos.dashboard.dto.SchedulePeriodDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DueTodayTest {

    private fun loan(vararg periods: SchedulePeriodDto) = LoanScheduleDto(
        id = 7,
        clientId = 42,
        currency = CurrencyRefDto(code = "MXN", displaySymbol = "$"),
        repaymentSchedule = RepaymentScheduleDto(periods.toList()),
    )

    @Test
    fun theInstallmentDueOnTheDateIsWhatIsOwedAndPaidToday() {
        val schedule = loan(
            // the disbursement row carries no period and is never an installment
            SchedulePeriodDto(period = null, dueDate = listOf(2026, 3, 5), totalOutstandingForPeriod = 999.0),
            SchedulePeriodDto(period = 1, dueDate = listOf(2026, 2, 5), totalPaidForPeriod = 300.0),
            SchedulePeriodDto(
                period = 2,
                dueDate = listOf(2026, 3, 5),
                totalPaidForPeriod = 120.0,
                totalOutstandingForPeriod = 180.0,
            ),
            SchedulePeriodDto(period = 3, dueDate = listOf(2026, 4, 5), totalOutstandingForPeriod = 300.0),
        )
        assertEquals(
            DueTodayLoan(
                loanId = 7,
                clientId = 42,
                currencyCode = "MXN",
                currencySymbol = "$",
                due = 180.0,
                paid = 120.0,
            ),
            schedule.dueOn("2026-03-05"),
        )
    }

    @Test
    fun aLoanWithNothingDueThatDayIsLeftOut() {
        val schedule = loan(
            SchedulePeriodDto(period = 1, dueDate = listOf(2026, 3, 6), totalOutstandingForPeriod = 300.0),
        )
        assertNull(schedule.dueOn("2026-03-05"))
    }
}
