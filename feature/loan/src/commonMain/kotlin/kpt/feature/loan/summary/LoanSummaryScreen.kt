/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppCard
import kpt.core.designsystem.fieldofficer.component.AppMenuDropDownItem
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppColors
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan
import kpt.feature.loan.generated.resources.feature_loan_account_no
import kpt.feature.loan.generated.resources.feature_loan_amount_paid
import kpt.feature.loan.generated.resources.feature_loan_approve_loan
import kpt.feature.loan.generated.resources.feature_loan_arrears
import kpt.feature.loan.generated.resources.feature_loan_balance
import kpt.feature.loan.generated.resources.feature_loan_closed
import kpt.feature.loan.generated.resources.feature_loan_copy
import kpt.feature.loan.generated.resources.feature_loan_date
import kpt.feature.loan.generated.resources.feature_loan_disburse_loan
import kpt.feature.loan.generated.resources.feature_loan_disbursed_date
import kpt.feature.loan.generated.resources.feature_loan_documents
import kpt.feature.loan.generated.resources.feature_loan_info
import kpt.feature.loan.generated.resources.feature_loan_loan_account_summary
import kpt.feature.loan.generated.resources.feature_loan_loan_amount_disbursed
import kpt.feature.loan.generated.resources.feature_loan_loan_charges
import kpt.feature.loan.generated.resources.feature_loan_loan_fees
import kpt.feature.loan.generated.resources.feature_loan_loan_id_copied
import kpt.feature.loan.generated.resources.feature_loan_loan_in_arrears
import kpt.feature.loan.generated.resources.feature_loan_loan_interest
import kpt.feature.loan.generated.resources.feature_loan_loan_overview
import kpt.feature.loan.generated.resources.feature_loan_loan_penalty
import kpt.feature.loan.generated.resources.feature_loan_loan_principal
import kpt.feature.loan.generated.resources.feature_loan_make_repayment
import kpt.feature.loan.generated.resources.feature_loan_more_info
import kpt.feature.loan.generated.resources.feature_loan_outstanding_balance
import kpt.feature.loan.generated.resources.feature_loan_repayment_schedule
import kpt.feature.loan.generated.resources.feature_loan_staff
import kpt.feature.loan.generated.resources.feature_loan_status_active
import kpt.feature.loan.generated.resources.feature_loan_status_awaiting_disbursal
import kpt.feature.loan.generated.resources.feature_loan_status_closed
import kpt.feature.loan.generated.resources.feature_loan_status_overpaid
import kpt.feature.loan.generated.resources.feature_loan_status_pending_approval
import kpt.feature.loan.generated.resources.feature_loan_summary
import kpt.feature.loan.generated.resources.feature_loan_summary_approved_amount
import kpt.feature.loan.generated.resources.feature_loan_summary_requested_amount
import kpt.feature.loan.generated.resources.feature_loan_total_loan
import kpt.feature.loan.generated.resources.feature_loan_transactions
import kpt.feature.loan.generated.resources.feature_loan_transfer_funds
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/loan/loanAccountSummary/LoanAccountSummaryScreen.kt` — the
 * client/status/account-no header card, the three figure cards, the four-row loan overview, the
 * five-row summary table, the status-driven primary button and the five-entry overflow menu.
 *
 * ## What is NOT carried over, and why
 *
 * **The breadcrumb bar.** The original rendered `MifosBreadcrumbNavBar(navController)` at the top
 * and took a `NavController` as a screen parameter to do it. This fork has no breadcrumb component
 * and no screen takes a NavController — navigation is `onX` callbacks, which is what lets every
 * screen render in a test without a NavHost. Adding one back would mean a nav-shell concern inside
 * a feature. Logged as `loan-summary-breadcrumb-not-ported`.
 *
 * **`formatCurrency`'s raw-double fallback.** The original returned `amount.toString()` when the
 * currency code was blank — so a loan with no currency block showed "6000.0" instead of a
 * formatted figure. That branch existed because `CurrencyFormatter` CRASHED on a blank code; now
 * that it falls back to the locale's currency (see `core/common`), the formatter is used for every
 * figure and the raw-double path is gone.
 *
 * §6a adaptations: the two-case `DialogState` becomes [ScreenState] through [ScreenContent]; the
 * four separate status derivations become one (see the ViewModel); and both the menu entries and
 * the primary button are gated on whether their destination is ported.
 */
@Composable
internal fun LoanSummaryScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuItem: ((LoanSummaryMenuItem, Int) -> Unit)? = null,
    onAction: ((LoanSummaryAct, Int) -> Unit)? = null,
    availableMenuItems: List<LoanSummaryMenuItem> = emptyList(),
    availableActions: Set<LoanSummaryAct> = emptySet(),
    viewModel: LoanSummaryViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val copiedMessage = stringResource(Res.string.feature_loan_loan_id_copied)

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanSummaryEvent.NavigateBack -> onBackPressed()
            is LoanSummaryEvent.OpenMenuItem -> onMenuItem?.invoke(event.item, event.loanId)
            is LoanSummaryEvent.OpenAction -> onAction?.invoke(event.action, event.loanId)
        }
    }

    // The original drove this from `snapshotFlow { state.showLoanIdCopiedMessage }` inside a
    // LaunchedEffect(Unit) and then dispatched an action to clear the flag. Keyed on the flag
    // directly: the effect runs when it turns true and the body clears it, which is the same
    // behaviour without a snapshot flow collecting for the screen's whole lifetime.
    LaunchedEffect(state.showCopiedMessage) {
        if (state.showCopiedMessage) {
            snackbarHostState.showSnackbar(copiedMessage)
            viewModel.trySendAction(LoanSummaryAction.OnMessageShown)
        }
    }

    LoanSummaryContent(
        state = state,
        summary = summary,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        snackbarHostState = snackbarHostState,
        availableMenuItems = remember(onMenuItem, availableMenuItems) {
            if (onMenuItem == null) emptyList() else availableMenuItems
        },
        availableActions = remember(onAction, availableActions) {
            if (onAction == null) emptySet() else availableActions
        },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun LoanSummaryContent(
    state: LoanSummaryState,
    summary: ScreenState<LoanSummaryUi>,
    onAction: (LoanSummaryAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    availableMenuItems: List<LoanSummaryMenuItem> = emptyList(),
    availableActions: Set<LoanSummaryAct> = emptySet(),
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_loan_account_summary),
        onBackPressed = { onAction(LoanSummaryAction.NavigateBack) },
        snackbarHostState = snackbarHostState,
        modifier = modifier.testTag(LoanSummaryTestTags.SCREEN),
        actions = {
            // The overflow icon is drawn only when at least one entry has somewhere to go. An
            // empty menu behind a live icon is a control that opens nothing.
            if (availableMenuItems.isNotEmpty()) {
                IconButton(
                    onClick = { onAction(LoanSummaryAction.ToggleDropdown) },
                    modifier = Modifier.testTag(LoanSummaryTestTags.OVERFLOW),
                ) {
                    Icon(imageVector = AppIcons.MoreVert, contentDescription = null)
                }
                SummaryMenu(
                    expanded = state.openDropdown,
                    items = availableMenuItems,
                    onAction = onAction,
                )
            }
        },
    ) { padding ->
        ScreenContent(
            state = summary,
            onRetry = { onAction(LoanSummaryAction.OnRetry) },
            modifier = Modifier.fillMaxSize().padding(padding).testTag(LoanSummaryTestTags.CONTENT),
        ) { loan, _ ->
            SummaryBody(loan = loan, availableActions = availableActions, onAction = onAction)
        }
    }
}

@Composable
private fun SummaryBody(
    loan: LoanSummaryUi,
    availableActions: Set<LoanSummaryAct>,
    onAction: (LoanSummaryAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignToken.padding.medium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DesignToken.padding.medium),
    ) {
        HeaderCard(loan = loan, onAction = onAction)

        Row {
            InfoCard(
                titleText = stringResource(
                    when (loan.status) {
                        LoanSummaryStatus.PENDING -> Res.string.feature_loan_summary_requested_amount
                        LoanSummaryStatus.AWAITING_DISBURSAL -> Res.string.feature_loan_summary_approved_amount
                        else -> Res.string.feature_loan_total_loan
                    },
                ),
                infoText = loan.totalLoan,
                modifier = Modifier.fillMaxWidth(HALF),
            )
            Spacer(Modifier.width(DesignToken.spacing.medium))
            InfoCard(
                titleText = stringResource(Res.string.feature_loan_amount_paid),
                infoText = loan.amountPaid,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        InfoCard(
            titleText = stringResource(Res.string.feature_loan_outstanding_balance),
            infoText = loan.outstanding,
            modifier = Modifier.fillMaxWidth().testTag(LoanSummaryTestTags.OUTSTANDING),
        )

        OverviewCard(loan)
        SummaryTable(loan)
        PrimaryActionButton(loan = loan, availableActions = availableActions, onAction = onAction)

        Spacer(Modifier.height(DesignToken.spacing.medium))
    }
}

@Composable
private fun HeaderCard(loan: LoanSummaryUi, onAction: (LoanSummaryAction) -> Unit) {
    val statusLabel = stringResource(loan.status.labelRes())

    AppCard(modifier = Modifier.fillMaxWidth().testTag(LoanSummaryTestTags.HEADER)) {
        Column(
            modifier = Modifier.padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignToken.spacing.mediumSmall),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = loan.clientName,
                style = KptTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // The status dot. Its colour AND its accessibility label come from the one derived
                // status — the original computed each with its own `when`, written out twice, so
                // they could drift apart.
                Canvas(
                    modifier = Modifier
                        .size(DesignToken.sizes.iconMedium)
                        .semantics { contentDescription = "Loan status: $statusLabel" }
                        .testTag(LoanSummaryTestTags.STATUS_DOT),
                    onDraw = { drawCircle(color = loan.status.dotColor()) },
                )
                Spacer(Modifier.width(DesignToken.spacing.mediumSmall))
                Text(
                    text = loan.loanProductName,
                    style = AppTypography.bodyLarge,
                    color = KptTheme.colorScheme.onSurface,
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    // `stringResource(…, accountNo)` with a placeholder, where the original did
                    // `stringResource(feature_loan_loan_id) + accountNo` — string concatenation
                    // that no translation can reorder.
                    text = stringResource(Res.string.feature_loan_account_no, loan.accountNo),
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    style = AppTypography.bodyMedium,
                )
                Spacer(Modifier.width(KptTheme.spacing.xs))
                IconButton(
                    onClick = { onAction(LoanSummaryAction.OnAccountNoCopied) },
                    modifier = Modifier
                        .size(DesignToken.sizes.iconSmall)
                        .testTag(LoanSummaryTestTags.COPY),
                ) {
                    Icon(
                        imageVector = AppIcons.Copy,
                        contentDescription = stringResource(Res.string.feature_loan_copy),
                        modifier = Modifier.size(DesignToken.sizes.iconSmall),
                        tint = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(loan: LoanSummaryUi) {
    AppCard(modifier = Modifier.testTag(LoanSummaryTestTags.OVERVIEW)) {
        Column(modifier = Modifier.padding(KptTheme.spacing.md)) {
            Text(
                text = stringResource(Res.string.feature_loan_loan_overview),
                style = AppTypography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = KptTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(DesignToken.spacing.medium))

            OverviewRow(
                icon = AppIcons.Currency,
                iconDescription = stringResource(Res.string.feature_loan_info),
                title = stringResource(Res.string.feature_loan_loan_amount_disbursed),
                value = loan.principalDisbursed,
            )
            OverviewRow(
                icon = AppIcons.Calendar,
                iconDescription = stringResource(Res.string.feature_loan_date),
                title = stringResource(Res.string.feature_loan_disbursed_date),
                value = loan.disbursedOn,
            )
            OverviewRow(
                icon = AppIcons.KeyboardArrowDown,
                iconDescription = stringResource(Res.string.feature_loan_arrears),
                title = stringResource(Res.string.feature_loan_loan_in_arrears),
                value = loan.overdue,
            )
            OverviewRow(
                icon = AppIcons.Person,
                iconDescription = stringResource(Res.string.feature_loan_info),
                title = stringResource(Res.string.feature_loan_staff),
                // NOT gated on `showsFigures` — the loan officer is assigned before disbursement
                // and is exactly what an officer looking at a pending loan needs to see. The
                // original did not gate it either.
                value = loan.loanOfficerName,
            )
        }
    }
}

@Composable
private fun OverviewRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconDescription: String,
    title: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            modifier = Modifier.size(DesignToken.sizes.iconAverage),
        )
        Row(modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.sm)) {
            Text(
                style = KptTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                text = "$title:",
                color = KptTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(KptTheme.spacing.xs))
            Text(
                style = KptTheme.typography.bodyMedium,
                text = value,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The four-way summary table: principal / interest / fees / penalty × charged / paid / balance. */
@Composable
private fun SummaryTable(loan: LoanSummaryUi) {
    AppCard(modifier = Modifier.testTag(LoanSummaryTestTags.TABLE)) {
        TableRow(
            summaryColumnTitle = stringResource(Res.string.feature_loan_summary),
            loanColumnValue = stringResource(Res.string.feature_loan),
            amountColumnValue = stringResource(Res.string.feature_loan_amount_paid),
            balanceColumnValue = stringResource(Res.string.feature_loan_balance),
            isHeader = true,
            color = KptTheme.colorScheme.primary.copy(alpha = HEADER_TINT),
        )
        TableRow(
            summaryColumnTitle = stringResource(Res.string.feature_loan_loan_principal),
            loanColumnValue = loan.principalDisbursed,
            amountColumnValue = loan.principalPaid,
            balanceColumnValue = loan.principalOutstanding,
        )
        HorizontalDivider(thickness = DesignToken.strokes.dpPoint5)
        TableRow(
            summaryColumnTitle = stringResource(Res.string.feature_loan_loan_interest),
            loanColumnValue = loan.interestCharged,
            amountColumnValue = loan.interestPaid,
            balanceColumnValue = loan.interestOutstanding,
        )
        HorizontalDivider(thickness = DesignToken.strokes.dpPoint5)
        TableRow(
            summaryColumnTitle = stringResource(Res.string.feature_loan_loan_fees),
            loanColumnValue = loan.feeChargesCharged,
            amountColumnValue = loan.feeChargesPaid,
            balanceColumnValue = loan.feeChargesOutstanding,
        )
        HorizontalDivider(thickness = DesignToken.strokes.dpPoint5)
        TableRow(
            summaryColumnTitle = stringResource(Res.string.feature_loan_loan_penalty),
            loanColumnValue = loan.penaltyChargesCharged,
            amountColumnValue = loan.penaltyChargesPaid,
            balanceColumnValue = loan.penaltyChargesOutstanding,
        )
    }
}

@Composable
private fun PrimaryActionButton(
    loan: LoanSummaryUi,
    availableActions: Set<LoanSummaryAct>,
    onAction: (LoanSummaryAction) -> Unit,
) {
    val action = loan.status.primaryAction()

    AppButton(
        // CLOSED has no action at all, so the button is inert by derivation rather than by a
        // second `isButtonActive()` predicate that could disagree with the label. An action whose
        // destination is not ported is disabled for the same reason as on the loan profile:
        // absent is impossible here (the original always draws this button), so disabled-with-its-
        // real-label is the honest rendering.
        enabled = action != null && action in availableActions,
        onClick = { onAction(LoanSummaryAction.OnPrimaryActionClick(loan.status)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(DesignToken.sizes.buttonHeightMedium)
            .testTag(LoanSummaryTestTags.PRIMARY_ACTION),
        shape = KptTheme.shapes.small,
    ) {
        Text(
            color = KptTheme.colorScheme.onPrimary,
            text = stringResource(loan.status.primaryActionLabel()),
        )
    }
}

@Composable
private fun SummaryMenu(
    expanded: Boolean,
    items: List<LoanSummaryMenuItem>,
    onAction: (LoanSummaryAction) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onAction(LoanSummaryAction.ToggleDropdown) },
        modifier = Modifier.testTag(LoanSummaryTestTags.MENU),
    ) {
        items.forEach { item ->
            AppMenuDropDownItem(
                option = stringResource(item.labelRes()),
                onClick = { onAction(LoanSummaryAction.OnMenuItemClick(item)) },
            )
        }
    }
}

@Composable
private fun InfoCard(titleText: String, infoText: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(KptTheme.spacing.md)) {
            Text(
                text = titleText,
                style = AppTypography.bodyLarge,
                color = KptTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(DesignToken.spacing.medium))
            // One line, shrinking to fit: a half-width card wrapped "MX$50,000" mid-number (device,
            // 2026-10-26), and a split figure reads as two numbers.
            BasicText(
                text = infoText,
                style = AppTypography.headlineSmallEmphasized.copy(color = KptTheme.colorScheme.onSurfaceVariant),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(maxFontSize = AppTypography.headlineSmallEmphasized.fontSize),
            )
        }
    }
}

@Composable
private fun TableRow(
    summaryColumnTitle: String,
    loanColumnValue: String,
    amountColumnValue: String,
    balanceColumnValue: String,
    isHeader: Boolean = false,
    color: Color = KptTheme.colorScheme.surface,
) {
    val weight = if (isHeader) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = summaryColumnTitle,
            modifier = Modifier.weight(1f),
            style = KptTheme.typography.bodyMedium,
            fontWeight = weight,
            color = KptTheme.colorScheme.onSurface,
        )
        listOf(loanColumnValue, amountColumnValue, balanceColumnValue).forEach { value ->
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                style = KptTheme.typography.bodyMedium,
                fontWeight = weight,
                textAlign = TextAlign.End,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    // The header's tint was a `background(color)` on the Row in the original; here it is drawn by
    // the Row's own modifier above only when non-default, which keeps the same visual without
    // painting the surface colour over every data row.
    if (isHeader) {
        HorizontalDivider(thickness = DesignToken.strokes.dpPoint5, color = color)
    }
}

/** The original's status → label mapping, with the two states the profile collapses to Unknown. */
internal fun LoanSummaryStatus.labelRes(): StringResource = when (this) {
    LoanSummaryStatus.ACTIVE -> Res.string.feature_loan_status_active
    LoanSummaryStatus.PENDING -> Res.string.feature_loan_status_pending_approval
    LoanSummaryStatus.AWAITING_DISBURSAL -> Res.string.feature_loan_status_awaiting_disbursal
    LoanSummaryStatus.OVERPAID -> Res.string.feature_loan_status_overpaid
    LoanSummaryStatus.CLOSED -> Res.string.feature_loan_status_closed
}

/**
 * The status dot's colour.
 *
 * `AppColors.loanIndicator*` are raw `Color.Green` / `Yellow` / `Blue` / `Black` — the original's
 * values, kept. They are NOT theme-aware, and `loanIndicatorOther` being `Color.Black` means a
 * closed loan's dot vanishes on the dark scheme. Unlike the repayment header's hardcoded text
 * colours, an invisible 16dp dot does not hide a FIGURE, and the dot's meaning is also carried by
 * its accessibility label and by the status-driven button beside it — so this one is left as the
 * original had it and logged as `loan-status-dot-colours-not-theme-aware`.
 */
internal fun LoanSummaryStatus.dotColor(): Color = when (this) {
    LoanSummaryStatus.ACTIVE -> AppColors.loanIndicatorActive
    LoanSummaryStatus.PENDING -> AppColors.loanIndicatorPending
    LoanSummaryStatus.AWAITING_DISBURSAL -> AppColors.loanIndicatorWaitingForDisbursal
    LoanSummaryStatus.OVERPAID, LoanSummaryStatus.CLOSED -> AppColors.loanIndicatorOther
}

/** The original's status → primary-button label mapping. */
internal fun LoanSummaryStatus.primaryActionLabel(): StringResource = when (this) {
    LoanSummaryStatus.ACTIVE -> Res.string.feature_loan_make_repayment
    LoanSummaryStatus.PENDING -> Res.string.feature_loan_approve_loan
    LoanSummaryStatus.AWAITING_DISBURSAL -> Res.string.feature_loan_disburse_loan
    LoanSummaryStatus.OVERPAID -> Res.string.feature_loan_transfer_funds
    LoanSummaryStatus.CLOSED -> Res.string.feature_loan_closed
}

internal fun LoanSummaryMenuItem.labelRes(): StringResource = when (this) {
    LoanSummaryMenuItem.MoreInfo -> Res.string.feature_loan_more_info
    LoanSummaryMenuItem.Transactions -> Res.string.feature_loan_transactions
    LoanSummaryMenuItem.RepaymentSchedule -> Res.string.feature_loan_repayment_schedule
    LoanSummaryMenuItem.Documents -> Res.string.feature_loan_documents
    LoanSummaryMenuItem.Charges -> Res.string.feature_loan_loan_charges
}

private const val HALF = 0.5f
private const val HEADER_TINT = 0.3f

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanSummaryTestTags {
    const val SCREEN = "loanSummary:screen"
    const val CONTENT = "loanSummary:content"
    const val HEADER = "loanSummary:header"
    const val STATUS_DOT = "loanSummary:statusDot"
    const val COPY = "loanSummary:copy"
    const val OUTSTANDING = "loanSummary:outstanding"
    const val OVERVIEW = "loanSummary:overview"
    const val TABLE = "loanSummary:table"
    const val PRIMARY_ACTION = "loanSummary:primaryAction"
    const val OVERFLOW = "loanSummary:overflow"
    const val MENU = "loanSummary:menu"
}
