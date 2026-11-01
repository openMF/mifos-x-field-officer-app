/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTableRow
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.objects.account.loan.RepaymentScheduleRowData
import kpt.core.model.objects.account.loan.RepaymentScheduleTableData
import kpt.core.model.objects.account.loan.RepaymentScheduleTotalsData
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_balance_of_loan
import kpt.feature.loan.generated.resources.feature_loan_complete_count
import kpt.feature.loan.generated.resources.feature_loan_date
import kpt.feature.loan.generated.resources.feature_loan_days
import kpt.feature.loan.generated.resources.feature_loan_due_short
import kpt.feature.loan.generated.resources.feature_loan_fees_short
import kpt.feature.loan.generated.resources.feature_loan_in_advance
import kpt.feature.loan.generated.resources.feature_loan_installment_totals
import kpt.feature.loan.generated.resources.feature_loan_interest_short
import kpt.feature.loan.generated.resources.feature_loan_late
import kpt.feature.loan.generated.resources.feature_loan_loan_amount_and_balance
import kpt.feature.loan.generated.resources.feature_loan_loan_repayment_schedule
import kpt.feature.loan.generated.resources.feature_loan_number
import kpt.feature.loan.generated.resources.feature_loan_outstanding
import kpt.feature.loan.generated.resources.feature_loan_overdue_count
import kpt.feature.loan.generated.resources.feature_loan_paid_date
import kpt.feature.loan.generated.resources.feature_loan_paid_short
import kpt.feature.loan.generated.resources.feature_loan_penalties_short
import kpt.feature.loan.generated.resources.feature_loan_pending_count
import kpt.feature.loan.generated.resources.feature_loan_principal_due
import kpt.feature.loan.generated.resources.feature_loan_total
import kpt.feature.loan.generated.resources.feature_loan_total_cost_of_loan
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/loan/loanRepaymentSchedule/LoanRepaymentScheduleScreen.kt` —
 * the two-tier grouped header, the disbursement row, the period rows, the totals row and the
 * three-counter bottom bar, all at the original's widths and tints.
 *
 * §6a's adaptations: the three `LoanRepaymentScheduleUiState` cases become [ScreenState] through
 * [ScreenContent] (so no-network and unauthenticated are distinguished rather than collapsed into
 * one error string), and back-navigation goes through the action channel.
 */
@Composable
internal fun LoanScheduleScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanScheduleViewModel = koinViewModel(),
) {
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanScheduleEvent.NavigateBack -> onBackPressed()
        }
    }

    LoanScheduleContent(
        schedule = schedule,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun LoanScheduleContent(
    schedule: ScreenState<RepaymentScheduleTableData>,
    onAction: (LoanScheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_loan_repayment_schedule),
        onBackPressed = { onAction(LoanScheduleAction.NavigateBack) },
        modifier = modifier.testTag(LoanScheduleTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = schedule,
            onRetry = { onAction(LoanScheduleAction.OnRetry) },
            modifier = Modifier.fillMaxSize().padding(padding).testTag(LoanScheduleTestTags.CONTENT),
        ) { tableData, _ ->
            ScheduleTable(tableData)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ScheduleTable(tableData: RepaymentScheduleTableData) {
    val scrollState = rememberScrollState()

    val smallWidth = DesignToken.sizes.tableCellWidthSmall
    val mediumWidth = DesignToken.sizes.tableCellWidthMedium
    val largeWidth = DesignToken.sizes.tableCellWidthLarge
    val divider = DesignToken.strokes.dpPoint5
    val columnWidths = listOf(
        smallWidth, smallWidth,
        largeWidth, largeWidth,
        largeWidth,
        mediumWidth, mediumWidth, mediumWidth, mediumWidth, mediumWidth,
        mediumWidth, mediumWidth, mediumWidth, mediumWidth,
    )

    // The group header spans are computed from the column widths PLUS the dividers they swallow —
    // which is why a tweak to one width has to be mirrored here or the two tiers drift apart.
    val groupHeaders = listOf(
        "" to smallWidth,
        "" to smallWidth,
        "" to largeWidth,
        "" to largeWidth,
        stringResource(Res.string.feature_loan_loan_amount_and_balance) to (largeWidth + mediumWidth + divider),
        stringResource(Res.string.feature_loan_total_cost_of_loan) to (mediumWidth * 3 + divider * 2),
        stringResource(Res.string.feature_loan_installment_totals) to (mediumWidth * 5 + divider * 4),
    )

    val columnHeaders = listOf(
        stringResource(Res.string.feature_loan_number),
        stringResource(Res.string.feature_loan_days),
        stringResource(Res.string.feature_loan_date),
        stringResource(Res.string.feature_loan_paid_date),
        stringResource(Res.string.feature_loan_balance_of_loan),
        stringResource(Res.string.feature_loan_principal_due),
        stringResource(Res.string.feature_loan_interest_short),
        stringResource(Res.string.feature_loan_fees_short),
        stringResource(Res.string.feature_loan_penalties_short),
        stringResource(Res.string.feature_loan_due_short),
        stringResource(Res.string.feature_loan_paid_short),
        stringResource(Res.string.feature_loan_in_advance),
        stringResource(Res.string.feature_loan_late),
        stringResource(Res.string.feature_loan_outstanding),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = DesignToken.padding.medium),
        ) {
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                ) {
                    AppTableRow(
                        cells = groupHeaders.mapIndexed { index, (text, _) ->
                            {
                                TableHeaderCell(
                                    text = text,
                                    backgroundColor = if (text.isNotBlank()) {
                                        lerp(KptTheme.colorScheme.surface, KptTheme.colorScheme.primary, 0.3f)
                                    } else {
                                        KptTheme.colorScheme.background
                                    },
                                    textAlign = if (index in 4..6) TextAlign.Center else TextAlign.Left,
                                    style = KptTheme.typography.titleSmall,
                                )
                            }
                        },
                        widths = groupHeaders.map { it.second },
                        backgroundColor = KptTheme.colorScheme.background,
                        edgeOffset = DesignToken.padding.medium,
                        cornerShape = DesignToken.shapes.topMedium,
                    )

                    AppTableRow(
                        cells = columnHeaders.map { text ->
                            {
                                TableHeaderCell(
                                    text = text,
                                    backgroundColor = Color.Transparent,
                                    textAlign = TextAlign.Left,
                                    style = KptTheme.typography.titleSmall,
                                )
                            }
                        },
                        widths = columnWidths,
                        backgroundColor = lerp(KptTheme.colorScheme.surface, KptTheme.colorScheme.primary, 0.08f),
                        edgeOffset = DesignToken.padding.medium,
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                ) {
                    // Period 0 — the disbursement. Rendered above the schedule and excluded from
                    // the rows, the totals and the counters; see the ViewModel's header.
                    tableData.disbursementRow?.let { disbursement ->
                        ScheduleTableRow(
                            values = disbursement.toDisplayValues(),
                            widths = columnWidths,
                            backgroundColor = lerp(KptTheme.colorScheme.surface, KptTheme.colorScheme.primary, 0.05f),
                            fontWeight = FontWeight.SemiBold,
                            textStyle = KptTheme.typography.bodySmall,
                        )
                    }

                    tableData.rows.forEach { row ->
                        ScheduleTableRow(
                            values = row.toDisplayValues(),
                            widths = columnWidths,
                            backgroundColor = KptTheme.colorScheme.surface,
                            fontWeight = FontWeight.Normal,
                            textStyle = KptTheme.typography.bodySmall,
                        )
                    }

                    ScheduleTableRow(
                        values = tableData.totals.toDisplayValues(
                            totalLabel = stringResource(Res.string.feature_loan_total),
                        ),
                        widths = columnWidths,
                        backgroundColor = lerp(KptTheme.colorScheme.surface, KptTheme.colorScheme.primary, 0.15f),
                        fontWeight = FontWeight.Bold,
                        textStyle = KptTheme.typography.bodyMedium,
                        testTag = LoanScheduleTestTags.TOTALS,
                    )
                }
            }
        }

        HorizontalDivider(color = KptTheme.colorScheme.outlineVariant)
        ScheduleCounters(
            completeCount = tableData.completeCount.toString(),
            overdueCount = tableData.overdueCount.toString(),
            pendingCount = tableData.pendingCount.toString(),
            modifier = Modifier.background(color = KptTheme.colorScheme.surfaceVariant),
        )
    }
}

private fun RepaymentScheduleRowData.toDisplayValues() = listOf(
    number, days, date, paidDate, balance,
    principal, interest, fees, penalties,
    due, paid, inAdvance, late, outstanding,
)

private fun RepaymentScheduleTotalsData.toDisplayValues(totalLabel: String) = listOf(
    "", "", totalLabel, "", "",
    principal, interest, fees, penalties,
    due, paid, inAdvance, late, outstanding,
)

@Composable
private fun TableHeaderCell(
    text: String,
    backgroundColor: Color,
    textAlign: TextAlign,
    style: TextStyle,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = KptTheme.spacing.sm, horizontal = KptTheme.spacing.xs),
    ) {
        Text(
            text = text,
            style = style,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = textAlign,
        )
    }
}

@Composable
private fun ScheduleTableRow(
    values: List<String>,
    widths: List<Dp>,
    backgroundColor: Color,
    fontWeight: FontWeight,
    textStyle: TextStyle,
    testTag: String? = null,
) {
    AppTableRow(
        cells = values.map { value ->
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(vertical = KptTheme.spacing.sm, horizontal = KptTheme.spacing.xs),
                ) {
                    Text(
                        text = value,
                        style = textStyle,
                        fontWeight = fontWeight,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Left,
                        color = KptTheme.colorScheme.onBackground,
                    )
                }
            }
        },
        widths = widths,
        backgroundColor = backgroundColor,
        edgeOffset = DesignToken.padding.medium,
        modifier = if (testTag == null) Modifier else Modifier.testTag(testTag),
    )
}

@Composable
private fun ScheduleCounters(
    completeCount: String,
    overdueCount: String,
    pendingCount: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignToken.padding.medium, vertical = DesignToken.padding.small)
            .testTag(LoanScheduleTestTags.COUNTERS),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.feature_loan_complete_count, completeCount),
            style = KptTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.feature_loan_pending_count, pendingCount),
            style = KptTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.feature_loan_overdue_count, overdueCount),
            style = KptTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanScheduleTestTags {
    const val SCREEN = "loanSchedule:screen"
    const val CONTENT = "loanSchedule:content"
    const val TOTALS = "loanSchedule:totals"
    const val COUNTERS = "loanSchedule:counters"
}
