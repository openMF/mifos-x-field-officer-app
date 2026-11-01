/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.reschedules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.designsystem.fieldofficer.component.AppCard
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppColors
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppEmptyUi
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_cancel
import kpt.feature.loan.generated.resources.feature_loan_dialog_action_ok
import kpt.feature.loan.generated.resources.feature_loan_reschedule_approve
import kpt.feature.loan.generated.resources.feature_loan_reschedule_approve_confirm
import kpt.feature.loan.generated.resources.feature_loan_reschedule_approve_title
import kpt.feature.loan.generated.resources.feature_loan_reschedule_failed_title
import kpt.feature.loan.generated.resources.feature_loan_reschedule_from
import kpt.feature.loan.generated.resources.feature_loan_reschedule_none
import kpt.feature.loan.generated.resources.feature_loan_reschedule_reason
import kpt.feature.loan.generated.resources.feature_loan_reschedule_reject
import kpt.feature.loan.generated.resources.feature_loan_reschedule_reject_confirm
import kpt.feature.loan.generated.resources.feature_loan_reschedule_reject_title
import kpt.feature.loan.generated.resources.feature_loan_reschedule_status_approved
import kpt.feature.loan.generated.resources.feature_loan_reschedule_status_pending
import kpt.feature.loan.generated.resources.feature_loan_reschedule_status_rejected
import kpt.feature.loan.generated.resources.feature_loan_reschedule_status_unknown
import kpt.feature.loan.generated.resources.feature_loan_reschedule_submitted_by
import kpt.feature.loan.generated.resources.feature_loan_reschedule_submitted_on
import kpt.feature.loan.generated.resources.feature_loan_reschedules
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/loan/loanReschedules/LoanReschedulesScreen.kt` — one card
 * per proposed reschedule with its status, dates and reason, and approve / reject on a pending one.
 *
 * §6a adaptations: the five-case `DialogState` splits into a [ScreenState] read plus three dialogs
 * (two confirmations and a failure), so an approve in flight no longer replaces the list with a
 * loading dialog; and both actions carry the ROW rather than only its id, so the PENDING
 * precondition can be checked before a confirmation is even offered.
 *
 * ## Approve and reject are drawn only on a PENDING row
 *
 * ABSENT on an approved or rejected one, not greyed — there is nothing to do to a reschedule the
 * server has already decided, and the original's handlers returned silently for those rows, so the
 * icons were live and inert.
 */
@Composable
internal fun LoanReschedulesScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onPropose: ((Int) -> Unit)? = null,
    viewModel: LoanReschedulesViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanReschedulesEvent.NavigateBack -> onBackPressed()
            is LoanReschedulesEvent.Propose -> onPropose?.invoke(event.loanId)
        }
    }

    LoanReschedulesContent(
        state = state,
        history = history,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        canPropose = onPropose != null,
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun LoanReschedulesContent(
    state: LoanReschedulesState,
    history: ScreenState<List<RescheduleRow>>,
    onAction: (LoanReschedulesAction) -> Unit,
    modifier: Modifier = Modifier,
    canPropose: Boolean = false,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_reschedules),
        onBackPressed = { onAction(LoanReschedulesAction.NavigateBack) },
        modifier = modifier.testTag(LoanReschedulesTestTags.SCREEN),
        actions = {
            if (canPropose) {
                IconButton(
                    onClick = { onAction(LoanReschedulesAction.OnPropose) },
                    modifier = Modifier.testTag(LoanReschedulesTestTags.PROPOSE),
                ) {
                    Icon(imageVector = AppIcons.Add, contentDescription = null)
                }
            }
        },
    ) { padding ->
        // `RefreshableScreenContent`: the same renderer inside a `PullToRefreshBox`, with the M3
        // indicator driven off the stream's own `freshnessSignal.isRefreshing` rather than a
        // boolean this screen would have to own. The original hand-rolled that boolean and set it
        // true -> false around a call that only LAUNCHED a coroutine, so its spinner could never
        // be observed true (`LoanChargeViewModel.refreshLoanChargeList`, `6b66e8a43`).
        //
        // Usable here only since the `empty` slot became forwardable — the wrapper used to pass
        // the inner `ScreenContent` nothing but `state`/`onRetry`/`content`, so a screen with a
        // branded empty state had to choose between it and the pull gesture
        // (openMF/kmp-project-template#310).
        RefreshableScreenContent(
            state = history,
            onRefresh = { onAction(LoanReschedulesAction.OnRefresh) },
            modifier = Modifier.fillMaxSize().padding(padding)
                .testTag(LoanReschedulesTestTags.CONTENT),
            empty = {
                AppEmptyUi(
                    text = stringResource(Res.string.feature_loan_reschedule_none),
                    icon = AppIcons.Calendar,
                    modifier = Modifier.testTag(LoanReschedulesTestTags.EMPTY),
                )
            },
        ) { rows, _ ->
            LazyColumn(modifier = Modifier.testTag(LoanReschedulesTestTags.LIST)) {
                items(rows, key = { it.id }) { row ->
                    RescheduleCard(
                        row = row,
                        // The list stays on screen while one row is being acted on; only THAT
                        // row's actions are held.
                        isBusy = state.actingOn == row.id,
                        onAction = onAction,
                    )
                }
            }
        }
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        is LoanReschedulesState.DialogState.ConfirmApprove -> ConfirmDialog(
            title = stringResource(Res.string.feature_loan_reschedule_approve_title),
            message = stringResource(Res.string.feature_loan_reschedule_approve_confirm),
            onConfirm = { onAction(LoanReschedulesAction.ConfirmApprove(dialog.rescheduleId)) },
            onDismiss = { onAction(LoanReschedulesAction.DismissDialog) },
            tag = LoanReschedulesTestTags.CONFIRM_APPROVE,
        )

        is LoanReschedulesState.DialogState.ConfirmReject -> ConfirmDialog(
            title = stringResource(Res.string.feature_loan_reschedule_reject_title),
            message = stringResource(Res.string.feature_loan_reschedule_reject_confirm),
            onConfirm = { onAction(LoanReschedulesAction.ConfirmReject(dialog.rescheduleId)) },
            onDismiss = { onAction(LoanReschedulesAction.DismissDialog) },
            tag = LoanReschedulesTestTags.CONFIRM_REJECT,
        )

        is LoanReschedulesState.DialogState.Failed -> AlertDialog(
            onDismissRequest = { onAction(LoanReschedulesAction.DismissDialog) },
            confirmButton = {
                TextButton(onClick = { onAction(LoanReschedulesAction.DismissDialog) }) {
                    Text(stringResource(Res.string.feature_loan_dialog_action_ok))
                }
            },
            title = {
                Text(
                    text = stringResource(Res.string.feature_loan_reschedule_failed_title),
                    style = KptTheme.typography.titleLarge,
                )
            },
            text = { Text(text = dialog.msg) },
            modifier = Modifier.testTag(LoanReschedulesTestTags.FAILED_DIALOG),
        )
    }
}

@Composable
private fun RescheduleCard(
    row: RescheduleRow,
    isBusy: Boolean,
    onAction: (LoanReschedulesAction) -> Unit,
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(KptTheme.spacing.sm)
            .testTag(LoanReschedulesTestTags.row(row.id)),
    ) {
        Column(
            modifier = Modifier.padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(row.status.labelRes()),
                    style = AppTypography.labelMediumEmphasized,
                    color = row.status.color(),
                    modifier = Modifier.testTag(LoanReschedulesTestTags.status(row.id)),
                )

                Spacer(Modifier.weight(1f))

                // ABSENT on a decided row, not greyed — there is nothing to do to a reschedule
                // the server has already approved or rejected. The original drew both icons on
                // every row and its handlers returned silently for the non-pending ones.
                if (row.isActionable) {
                    IconButton(
                        onClick = { onAction(LoanReschedulesAction.OnApproveClick(row)) },
                        enabled = !isBusy,
                        modifier = Modifier.testTag(LoanReschedulesTestTags.approve(row.id)),
                    ) {
                        Icon(
                            imageVector = AppIcons.Check,
                            contentDescription = stringResource(
                                Res.string.feature_loan_reschedule_approve,
                            ),
                            modifier = Modifier.size(DesignToken.sizes.iconAverage),
                        )
                    }
                    IconButton(
                        onClick = { onAction(LoanReschedulesAction.OnRejectClick(row)) },
                        enabled = !isBusy,
                        modifier = Modifier.testTag(LoanReschedulesTestTags.reject(row.id)),
                    ) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = stringResource(
                                Res.string.feature_loan_reschedule_reject,
                            ),
                            modifier = Modifier.size(DesignToken.sizes.iconAverage),
                        )
                    }
                }
            }

            LabelledRow(stringResource(Res.string.feature_loan_reschedule_from), row.rescheduleFrom)
            LabelledRow(stringResource(Res.string.feature_loan_reschedule_reason), row.reason)
            LabelledRow(
                stringResource(Res.string.feature_loan_reschedule_submitted_on),
                row.submittedOn,
            )
            LabelledRow(
                stringResource(Res.string.feature_loan_reschedule_submitted_by),
                row.submittedBy,
            )
        }
    }
}

@Composable
private fun LabelledRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = AppTypography.bodySmall,
            color = KptTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = AppTypography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

/**
 * Both confirmations, which are NOT dismissable by tapping outside.
 *
 * Approving or rejecting a reschedule is irreversible from this screen, so the officer has to
 * choose one of the two buttons — the original's `onDismissRequest = { }` on these, kept.
 */
@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    tag: String,
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.feature_loan_dialog_action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.feature_loan_cancel))
            }
        },
        title = { Text(text = title, style = KptTheme.typography.titleLarge) },
        text = { Text(text = message) },
        modifier = Modifier.testTag(tag),
    )
}

internal fun RescheduleStatusCode.labelRes(): StringResource = when (this) {
    RescheduleStatusCode.PENDING -> Res.string.feature_loan_reschedule_status_pending
    RescheduleStatusCode.APPROVED -> Res.string.feature_loan_reschedule_status_approved
    RescheduleStatusCode.REJECTED -> Res.string.feature_loan_reschedule_status_rejected
    RescheduleStatusCode.UNKNOWN -> Res.string.feature_loan_reschedule_status_unknown
}

/** Reuses the loan-status palette: pending is the same amber, approved the same green. */
internal fun RescheduleStatusCode.color(): Color = when (this) {
    RescheduleStatusCode.PENDING -> AppColors.loanPendingStatus
    RescheduleStatusCode.APPROVED -> AppColors.loanActiveStatus
    RescheduleStatusCode.REJECTED -> AppColors.loanOverpaidStatus
    RescheduleStatusCode.UNKNOWN -> AppColors.loanUnknownStatus
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanReschedulesTestTags {
    const val SCREEN = "loanReschedules:screen"
    const val CONTENT = "loanReschedules:content"
    const val LIST = "loanReschedules:list"
    const val EMPTY = "loanReschedules:empty"
    const val PROPOSE = "loanReschedules:propose"
    const val CONFIRM_APPROVE = "loanReschedules:confirmApprove"
    const val CONFIRM_REJECT = "loanReschedules:confirmReject"
    const val FAILED_DIALOG = "loanReschedules:failedDialog"
    fun row(id: Int): String = "loanReschedules:row:$id"
    fun status(id: Int): String = "loanReschedules:status:$id"
    fun approve(id: Int): String = "loanReschedules:approve:$id"
    fun reject(id: Int): String = "loanReschedules:reject:$id"
}
