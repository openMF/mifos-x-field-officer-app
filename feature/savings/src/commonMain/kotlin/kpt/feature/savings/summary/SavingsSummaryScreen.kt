/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.summary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.designsystem.fieldofficer.component.AppMenuDropDownItem
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.feature.savings.generated.resources.Res
import kpt.feature.savings.generated.resources.feature_savings_account_balance
import kpt.feature.savings.generated.resources.feature_savings_activate
import kpt.feature.savings.generated.resources.feature_savings_approve
import kpt.feature.savings.generated.resources.feature_savings_charges
import kpt.feature.savings.generated.resources.feature_savings_client_name_unknown
import kpt.feature.savings.generated.resources.feature_savings_closed
import kpt.feature.savings.generated.resources.feature_savings_deposit
import kpt.feature.savings.generated.resources.feature_savings_documents
import kpt.feature.savings.generated.resources.feature_savings_interest_earned
import kpt.feature.savings.generated.resources.feature_savings_more_options
import kpt.feature.savings.generated.resources.feature_savings_more_savings_account_info
import kpt.feature.savings.generated.resources.feature_savings_no_transactions
import kpt.feature.savings.generated.resources.feature_savings_product_name_unknown
import kpt.feature.savings.generated.resources.feature_savings_reversed
import kpt.feature.savings.generated.resources.feature_savings_summary_title
import kpt.feature.savings.generated.resources.feature_savings_total_deposits
import kpt.feature.savings.generated.resources.feature_savings_total_withdrawals
import kpt.feature.savings.generated.resources.feature_savings_transactions
import kpt.feature.savings.generated.resources.feature_savings_withdraw
import kpt.feature.savings.transaction.TransactionKind
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The savings account summary — ported from
 * `6b66e8a43:feature/savings/savingsAccountSummary/SavingsAccountSummaryScreen.kt` (681 lines).
 *
 * See [SavingsSummaryViewModel] for the four `!status?.active!!` dereferences this replaces, the
 * `String as StringResource` cast, and why the transaction rows' dates used to crash.
 *
 * ## §6a adaptations
 *
 * `SavingsAccountSummaryUiState`'s three cases → [ScreenState] / [ScreenContent]; the menu and the
 * action buttons dispatch through `onAction`; the destinations this slice has not landed yet
 * (approval, activation, deposit/withdraw, documents) arrive as `null` callbacks, so their controls
 * are **ABSENT rather than inert** — which is also why this screen ships before them.
 *
 * ## The visibility booleans were `rememberSaveable`, so they never updated
 *
 * ```kotlin
 * val isSavingsButtonVisible by rememberSaveable {
 *     mutableStateOf(savingsButtonVisibilityStatus(savingsAccountWithAssociations.status))
 * }
 * ```
 *
 * Computed once from the FIRST account emitted and then *saved*, so after approving an account the
 * button kept its old label and its old action until the screen was recreated. Derived state does
 * not belong in `rememberSaveable`; it is read straight off the account here.
 */
@Composable
internal fun SavingsSummaryScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onTransaction: ((String, Int, TransactionKind) -> Unit)? = null,
    onApprove: ((Int) -> Unit)? = null,
    onActivate: ((Int) -> Unit)? = null,
    onDocuments: ((Int) -> Unit)? = null,
    onCharges: ((Int) -> Unit)? = null,
    onMoreInfo: ((Int) -> Unit)? = null,
    viewModel: SavingsSummaryViewModel = koinViewModel(),
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    var detail by remember { mutableStateOf<SavingsTransactionRow?>(null) }

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            SavingsSummaryEvent.NavigateBack -> onBackPressed()
            is SavingsSummaryEvent.OpenTransaction ->
                onTransaction?.invoke(event.accountType, event.accountId, event.kind)
            is SavingsSummaryEvent.OpenApproval -> onApprove?.invoke(event.accountId)
            is SavingsSummaryEvent.OpenActivation -> onActivate?.invoke(event.accountId)
            is SavingsSummaryEvent.OpenDocuments -> onDocuments?.invoke(event.accountId)
            is SavingsSummaryEvent.OpenMoreInfo -> onMoreInfo?.invoke(event.accountId)
            is SavingsSummaryEvent.OpenCharges -> onCharges?.invoke(event.accountId)
            is SavingsSummaryEvent.ShowTransaction -> detail = event.transaction
        }
    }

    SavingsSummaryContent(
        account = account,
        // Each control needs BOTH a permitting status and somewhere to send it.
        canTransact = onTransaction != null,
        canApprove = onApprove != null,
        canActivate = onActivate != null,
        canOpenDocuments = onDocuments != null,
        canOpenCharges = onCharges != null,
        canOpenMoreInfo = onMoreInfo != null,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )

    detail?.let { row ->
        SavingsTransactionDialog(
            row = row,
            onDismiss = { detail = null },
            // So the dialog's amounts are formatted with the same currency as the rows behind it.
            summary = (account as? ScreenState.Content)?.data,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun SavingsSummaryContent(
    account: ScreenState<SavingsSummary>,
    onAction: (SavingsSummaryAction) -> Unit,
    modifier: Modifier = Modifier,
    canTransact: Boolean = false,
    canApprove: Boolean = false,
    canActivate: Boolean = false,
    canOpenDocuments: Boolean = false,
    canOpenCharges: Boolean = false,
    canOpenMoreInfo: Boolean = false,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_savings_summary_title),
        onBackPressed = { onAction(SavingsSummaryAction.NavigateBack) },
        modifier = modifier.testTag(SavingsSummaryTestTags.SCREEN),
    ) { padding ->
        // `RefreshableScreenContent`, not plain `ScreenContent`: a balance is the figure an officer
        // most wants to re-check in the field, and this primitive already exists in core-base/ui —
        // it wraps `ScreenContent` in a `PullToRefreshBox` and drives the M3 indicator off the
        // stream's own `freshnessSignal.isRefreshing`. It had ZERO callers in the fork until now,
        // which is also why `OnRefresh` would otherwise have been a declared-but-undispatched
        // action (the defect the action-dispatch sweep catches).
        RefreshableScreenContent(
            state = account,
            onRefresh = { onAction(SavingsSummaryAction.OnRefresh) },
            modifier = Modifier.fillMaxSize().padding(padding)
                .testTag(SavingsSummaryTestTags.CONTENT),
        ) { summary, _ ->
            Column(Modifier.fillMaxSize().padding(horizontal = KptTheme.spacing.md)) {
                AccountHeader(summary, canOpenDocuments, canOpenCharges, canOpenMoreInfo, onAction)

                // `weight(1f)`, so the action row below is always reachable. The original stacked
                // the buttons in a `Box(align = BottomStart)` OVER a `fillMaxSize` column, which
                // overlays them on the last transactions on a short screen.
                TransactionList(
                    summary = summary,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )

                ActionRow(
                    summary = summary,
                    canTransact = canTransact,
                    canApprove = canApprove,
                    canActivate = canActivate,
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
private fun AccountHeader(
    summary: SavingsSummary,
    canOpenDocuments: Boolean,
    canOpenCharges: Boolean,
    canOpenMoreInfo: Boolean,
    onAction: (SavingsSummaryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = summary.clientName
                        ?: stringResource(Res.string.feature_savings_client_name_unknown),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = summary.productName
                        ?: stringResource(Res.string.feature_savings_product_name_unknown),
                    style = AppTypography.labelLarge,
                )
                Text(text = summary.accountNo, style = AppTypography.bodyMedium)
            }

            // The original's two-item overflow menu — "More savings account info" (the account's
            // datatables, `m_savings_account`) and Documents — plus Charges (feature/charge; the original
            // had no savings charge screen at all). Each entry needs a destination, and the icon is
            // absent when none has one.
            if (canOpenMoreInfo || canOpenDocuments || canOpenCharges) {
                var expanded by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.testTag(SavingsSummaryTestTags.OVERFLOW),
                ) {
                    Icon(
                        AppIcons.MoreVert,
                        contentDescription = stringResource(Res.string.feature_savings_more_options),
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (canOpenMoreInfo) {
                        AppMenuDropDownItem(
                            option = stringResource(Res.string.feature_savings_more_savings_account_info),
                            onClick = {
                                expanded = false
                                onAction(SavingsSummaryAction.MoreInfoClicked)
                            },
                            modifier = Modifier.testTag(SavingsSummaryTestTags.MORE_INFO),
                        )
                    }
                    if (canOpenDocuments) {
                        AppMenuDropDownItem(
                            option = stringResource(Res.string.feature_savings_documents),
                            onClick = {
                                expanded = false
                                onAction(SavingsSummaryAction.DocumentsClicked)
                            },
                            modifier = Modifier.testTag(SavingsSummaryTestTags.DOCUMENTS),
                        )
                    }
                    if (canOpenCharges) {
                        AppMenuDropDownItem(
                            option = stringResource(Res.string.feature_savings_charges),
                            onClick = {
                                expanded = false
                                onAction(SavingsSummaryAction.ChargesClicked)
                            },
                            modifier = Modifier.testTag(SavingsSummaryTestTags.CHARGES),
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().testTag(SavingsSummaryTestTags.TOTALS),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            // Every figure formatted with the ACCOUNT's currency — see the ViewModel on why the
            // original's two currency sources could disagree.
            Figure(stringResource(Res.string.feature_savings_account_balance), summary.accountBalance, summary)
            Figure(stringResource(Res.string.feature_savings_total_deposits), summary.totalDeposits, summary)
            Figure(stringResource(Res.string.feature_savings_total_withdrawals), summary.totalWithdrawals, summary)
            Figure(stringResource(Res.string.feature_savings_interest_earned), summary.interestEarned, summary)
        }
    }
}

@Composable
private fun Figure(label: String, value: Double?, summary: SavingsSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = AppTypography.bodyMedium)
        Text(text = summary.money(value), style = AppTypography.labelLarge)
    }
}

@Composable
private fun TransactionList(
    summary: SavingsSummary,
    onAction: (SavingsSummaryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = stringResource(Res.string.feature_savings_transactions),
            style = AppTypography.labelLarge,
            modifier = Modifier.padding(vertical = KptTheme.spacing.sm),
        )

        if (summary.transactions.isEmpty()) {
            Text(
                text = stringResource(Res.string.feature_savings_no_transactions),
                style = AppTypography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
                    .testTag(SavingsSummaryTestTags.NO_TRANSACTIONS),
            )
            return@Column
        }

        LazyColumn(modifier = Modifier.testTag(SavingsSummaryTestTags.TRANSACTIONS)) {
            items(summary.transactions, key = { it.id ?: it.hashCode() }) { row ->
                TransactionRow(row = row, summary = summary, onAction = onAction)
            }
        }
    }
}

@Composable
private fun TransactionRow(
    row: SavingsTransactionRow,
    summary: SavingsSummary,
    onAction: (SavingsSummaryAction) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction(SavingsSummaryAction.TransactionClicked(row)) }
            .padding(vertical = KptTheme.spacing.sm)
            .testTag(SavingsSummaryTestTags.transaction(row.id)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Blank rather than a crash when the row carries no usable date — see the ViewModel.
        Text(text = row.date, style = AppTypography.bodyMedium, modifier = Modifier.weight(4f))
        Text(
            text = row.typeLabel.orEmpty(),
            style = AppTypography.bodyMedium,
            modifier = Modifier.weight(3f),
        )
        Text(
            text = summary.money(row.amount),
            style = AppTypography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(3f),
            // Theme roles, not the original's `Color.Green` / `Color.Red` literals — those are
            // near-invisible on this app's dark scheme, the same defect the repayment header had.
            color = when {
                row.isReversed -> MaterialTheme.colorScheme.outline
                row.isWithdrawal -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
    }

    if (row.isReversed) {
        // Stated, not merely greyed. A reversed transaction still shows an amount, and colour alone
        // is not an accessible way to say "this did not happen".
        Text(
            text = stringResource(Res.string.feature_savings_reversed),
            style = AppTypography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

/**
 * Deposit / Withdraw / Approve / Activate — exactly what the status permits.
 *
 * One `when` over [SavingsSummary.available], the single derivation. [SavingsAction.NONE] draws
 * NOTHING: the original rendered an enabled Button labelled "Savings account closed" whose
 * `onClick` was `{ }`.
 */
@Composable
private fun ActionRow(
    summary: SavingsSummary,
    canTransact: Boolean,
    canApprove: Boolean,
    canActivate: Boolean,
    onAction: (SavingsSummaryAction) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = KptTheme.spacing.sm)
            .testTag(SavingsSummaryTestTags.ACTIONS),
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        when (summary.available) {
            SavingsAction.TRANSACT -> if (canTransact) {
                OutlinedButton(
                    onClick = { onAction(SavingsSummaryAction.WithdrawClicked) },
                    modifier = Modifier.weight(1f).testTag(SavingsSummaryTestTags.WITHDRAW),
                ) { Text(stringResource(Res.string.feature_savings_withdraw)) }

                Button(
                    onClick = { onAction(SavingsSummaryAction.DepositClicked) },
                    modifier = Modifier.weight(1f).testTag(SavingsSummaryTestTags.DEPOSIT),
                ) { Text(stringResource(Res.string.feature_savings_deposit)) }
            }

            SavingsAction.APPROVE -> if (canApprove) {
                Button(
                    onClick = { onAction(SavingsSummaryAction.ApproveClicked) },
                    modifier = Modifier.weight(1f).testTag(SavingsSummaryTestTags.APPROVE),
                ) { Text(stringResource(Res.string.feature_savings_approve)) }
            }

            SavingsAction.ACTIVATE -> if (canActivate) {
                Button(
                    onClick = { onAction(SavingsSummaryAction.ActivateClicked) },
                    modifier = Modifier.weight(1f).testTag(SavingsSummaryTestTags.ACTIVATE),
                ) { Text(stringResource(Res.string.feature_savings_activate)) }
            }

            // Closed / rejected / withdrawn. The status as TEXT, not as a button that does nothing.
            SavingsAction.NONE -> Text(
                text = stringResource(Res.string.feature_savings_closed),
                style = AppTypography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(SavingsSummaryTestTags.CLOSED_NOTICE),
            )
        }
    }
}

/** Formatted with the account's own currency and precision. */
internal fun SavingsSummary.money(value: Double?): String {
    value ?: return ""
    return CurrencyFormatter.format(value, currencyCode, decimalPlaces)
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object SavingsSummaryTestTags {
    const val SCREEN = "savingsSummary:screen"
    const val CONTENT = "savingsSummary:content"
    const val TOTALS = "savingsSummary:totals"
    const val TRANSACTIONS = "savingsSummary:transactions"
    const val NO_TRANSACTIONS = "savingsSummary:noTransactions"
    const val ACTIONS = "savingsSummary:actions"
    const val DEPOSIT = "savingsSummary:deposit"
    const val WITHDRAW = "savingsSummary:withdraw"
    const val APPROVE = "savingsSummary:approve"
    const val ACTIVATE = "savingsSummary:activate"
    const val CLOSED_NOTICE = "savingsSummary:closedNotice"
    const val OVERFLOW = "savingsSummary:overflow"
    const val DOCUMENTS = "savingsSummary:documents"
    const val MORE_INFO = "savings:summary:more-info"
    const val CHARGES = "savingsSummary:charges"
    const val DETAIL_DIALOG = "savingsSummary:detailDialog"
    fun transaction(id: Int?): String = "savingsSummary:transaction:$id"
}
