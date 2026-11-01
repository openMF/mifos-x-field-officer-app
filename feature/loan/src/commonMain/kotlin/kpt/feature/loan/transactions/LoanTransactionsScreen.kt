/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTableRow
import kpt.core.designsystem.theme.DesignToken
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_break_down
import kpt.feature.loan.generated.resources.feature_loan_loan_transactions
import kpt.feature.loan.generated.resources.feature_loan_table_header_amount
import kpt.feature.loan.generated.resources.feature_loan_table_header_external_id
import kpt.feature.loan.generated.resources.feature_loan_table_header_fees
import kpt.feature.loan.generated.resources.feature_loan_table_header_interest
import kpt.feature.loan.generated.resources.feature_loan_table_header_loan_balance
import kpt.feature.loan.generated.resources.feature_loan_table_header_number
import kpt.feature.loan.generated.resources.feature_loan_table_header_office
import kpt.feature.loan.generated.resources.feature_loan_table_header_penalties
import kpt.feature.loan.generated.resources.feature_loan_table_header_principal
import kpt.feature.loan.generated.resources.feature_loan_table_header_transaction_date
import kpt.feature.loan.generated.resources.feature_loan_table_header_transaction_id
import kpt.feature.loan.generated.resources.feature_loan_table_header_transaction_type
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/loan/loanTransaction/LoanTransactionsScreen.kt` — the
 * two-tier header with the "Break down" group spanning the five money columns, and the reversed-row
 * strike-through in the error colour.
 *
 * Two §6a adaptations plus one removal:
 * - the three UI-state cases become [ScreenState] through [ScreenContent]; the no-transactions case
 *   is now the stream's Empty rather than a check in the screen body;
 * - back-navigation goes through the action channel;
 * - the row-tap bottom sheet is GONE because none of its four actions did anything — see
 *   [LoanTransactionsViewModel]'s header.
 */
@Composable
internal fun LoanTransactionsScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanTransactionsViewModel = koinViewModel(),
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanTransactionsEvent.NavigateBack -> onBackPressed()
        }
    }

    LoanTransactionsContent(
        transactions = transactions,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun LoanTransactionsContent(
    transactions: ScreenState<List<TransactionRow>>,
    onAction: (LoanTransactionsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_loan_transactions),
        onBackPressed = { onAction(LoanTransactionsAction.NavigateBack) },
        modifier = modifier.testTag(LoanTransactionsTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = transactions,
            onRetry = { onAction(LoanTransactionsAction.OnRetry) },
            modifier = Modifier.fillMaxSize().padding(padding).testTag(LoanTransactionsTestTags.CONTENT),
        ) { rows, _ ->
            TransactionsTable(rows)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionsTable(rows: List<TransactionRow>) {
    val scrollState = rememberScrollState()
    val smallWidth = DesignToken.sizes.tableCellWidthSmall
    val mediumWidth = DesignToken.sizes.tableCellWidthMedium
    val largeWidth = DesignToken.sizes.tableCellWidthLarge

    val columnWidths = listOf(
        smallWidth, smallWidth,
        mediumWidth, mediumWidth,
        largeWidth, largeWidth,
        mediumWidth, mediumWidth, mediumWidth, mediumWidth, mediumWidth,
        mediumWidth,
    )

    // Tier 1: "Break down" spans the five money columns, hence eight header cells for twelve
    // columns and its own width list.
    val groupHeaders = listOf(
        Res.string.feature_loan_table_header_number,
        Res.string.feature_loan_table_header_transaction_id,
        Res.string.feature_loan_table_header_office,
        Res.string.feature_loan_table_header_external_id,
        Res.string.feature_loan_table_header_transaction_date,
        Res.string.feature_loan_table_header_transaction_type,
        Res.string.feature_loan_break_down,
        Res.string.feature_loan_table_header_loan_balance,
    )
    val groupWidths = listOf(
        smallWidth,
        smallWidth,
        mediumWidth,
        mediumWidth,
        largeWidth,
        largeWidth,
        mediumWidth * BREAKDOWN_COLUMNS,
        mediumWidth,
    )

    // Tier 2: only the five breakdown columns carry a label; the rest sit blank under tier 1.
    val breakdownHeaders = listOf(
        "", "", "", "", "", "",
        stringResource(Res.string.feature_loan_table_header_amount),
        stringResource(Res.string.feature_loan_table_header_principal),
        stringResource(Res.string.feature_loan_table_header_interest),
        stringResource(Res.string.feature_loan_table_header_fees),
        stringResource(Res.string.feature_loan_table_header_penalties),
        "",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(vertical = DesignToken.padding.medium),
    ) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
            ) {
                AppTableRow(
                    cells = groupHeaders.mapIndexed { index, header ->
                        {
                            HeaderCell(
                                text = stringResource(header),
                                backgroundColor = Color.Transparent,
                                textAlign = if (index == BREAKDOWN_INDEX) TextAlign.Center else TextAlign.Left,
                            )
                        }
                    },
                    widths = groupWidths,
                    backgroundColor = lerp(
                        KptTheme.colorScheme.surface,
                        KptTheme.colorScheme.primary,
                        0.3f,
                    ),
                    edgeOffset = DesignToken.padding.medium,
                    cornerShape = DesignToken.shapes.topMedium,
                )

                AppTableRow(
                    cells = breakdownHeaders.map { header ->
                        {
                            HeaderCell(
                                text = header,
                                backgroundColor = if (header.isNotBlank()) {
                                    lerp(KptTheme.colorScheme.surface, KptTheme.colorScheme.primary, 0.08f)
                                } else {
                                    KptTheme.colorScheme.background
                                },
                                textAlign = TextAlign.Left,
                            )
                        }
                    },
                    widths = columnWidths,
                    backgroundColor = Color.Transparent,
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
                rows.forEach { row -> TransactionTableRow(row, columnWidths) }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, backgroundColor: Color, textAlign: TextAlign) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(vertical = KptTheme.spacing.sm, horizontal = KptTheme.spacing.xs),
    ) {
        Text(
            text = text,
            style = KptTheme.typography.titleSmall,
            modifier = Modifier.fillMaxSize(),
            textAlign = textAlign,
        )
    }
}

@Composable
private fun TransactionTableRow(row: TransactionRow, widths: List<Dp>) {
    val values = listOf(
        row.number,
        row.id,
        row.office,
        row.externalId,
        row.transactionDate,
        row.transactionType.value,
        row.amount,
        row.principal,
        row.interest,
        row.fees,
        row.penalties,
        row.loanBalance,
    )

    // A reversed transaction is struck through in the error colour — the whole row, so it reads as
    // undone at a glance rather than needing the amount compared against something.
    val textColor =
        if (row.manuallyReversed) KptTheme.colorScheme.error else KptTheme.colorScheme.onBackground
    val textDecoration = if (row.manuallyReversed) TextDecoration.LineThrough else null

    AppTableRow(
        cells = values.mapIndexed { index, value ->
            {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (index in BREAKDOWN_FIRST_COLUMN..BREAKDOWN_LAST_COLUMN) {
                                lerp(KptTheme.colorScheme.surface, KptTheme.colorScheme.primary, 0.08f)
                            } else {
                                KptTheme.colorScheme.surface
                            },
                        )
                        .padding(vertical = KptTheme.spacing.sm, horizontal = KptTheme.spacing.xs),
                ) {
                    Text(
                        text = value,
                        style = KptTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Left,
                        color = textColor,
                        textDecoration = textDecoration,
                    )
                }
            }
        },
        widths = widths,
        backgroundColor = Color.Transparent,
        edgeOffset = DesignToken.padding.medium,
        // NOT clickable. The original opened a sheet whose every action was a no-op.
        modifier = Modifier.testTag(LoanTransactionsTestTags.row(row.number)),
    )
}

/** The "Break down" group spans Amount · Principal · Interest · Fees · Penalties. */
private const val BREAKDOWN_COLUMNS = 5
private const val BREAKDOWN_INDEX = 6
private const val BREAKDOWN_FIRST_COLUMN = 6
private const val BREAKDOWN_LAST_COLUMN = 10

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanTransactionsTestTags {
    const val SCREEN = "loanTransactions:screen"
    const val CONTENT = "loanTransactions:content"
    fun row(number: String): String = "loanTransactions:row:$number"
}
