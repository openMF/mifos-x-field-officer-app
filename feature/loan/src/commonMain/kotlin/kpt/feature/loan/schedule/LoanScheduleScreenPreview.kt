/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.schedule

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.account.loan.RepaymentScheduleRowData
import kpt.core.model.objects.account.loan.RepaymentScheduleTableData
import kpt.core.model.objects.account.loan.RepaymentScheduleTotalsData
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screen is not
 * previewed; its stateless `*Content` half is. Values are server content, not app copy —
 * `// i18n:skip`.
 */

// i18n:skip — preview fixtures below
private fun previewRow(n: Int, date: String, paid: String, outstanding: String) = RepaymentScheduleRowData(
    number = "$n", days = "30", date = date, paidDate = if (paid == "0.00") "" else date,
    balance = "${25_000 - n * 2_000}.00",
    principal = "2000.00", interest = "450.00", fees = "0.00", penalties = "0.00", due = "2450.00",
    paid = paid, inAdvance = "0.00", late = "0.00", outstanding = outstanding,
)

private val previewSchedule = RepaymentScheduleTableData(
    disbursementRow = null,
    rows = listOf(
        previewRow(1, "15 Nov 2025", paid = "2450.00", outstanding = "0.00"),
        previewRow(2, "15 Dec 2025", paid = "0.00", outstanding = "2450.00"),
        previewRow(3, "15 Jan 2026", paid = "0.00", outstanding = "2450.00"),
    ),
    totals = RepaymentScheduleTotalsData(
        principal = "6000.00", interest = "1350.00", fees = "0.00", penalties = "0.00", due = "7350.00",
        paid = "2450.00", inAdvance = "0.00", late = "0.00", outstanding = "4900.00",
    ),
    completeCount = 1,
    overdueCount = 1,
    pendingCount = 1,
)

@Preview
@Composable
internal fun LoanScheduleContentPreview() {
    KptTheme {
        LoanScheduleContent(schedule = ScreenState.Content(previewSchedule), onAction = {})
    }
}
