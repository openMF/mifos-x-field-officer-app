/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.statuschange

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.savings.generated.resources.Res
import kpt.feature.savings.generated.resources.feature_savings_activate_failed
import kpt.feature.savings.generated.resources.feature_savings_activate_title
import kpt.feature.savings.generated.resources.feature_savings_activated_message
import kpt.feature.savings.generated.resources.feature_savings_activation_date
import kpt.feature.savings.generated.resources.feature_savings_approval_date
import kpt.feature.savings.generated.resources.feature_savings_approve_failed
import kpt.feature.savings.generated.resources.feature_savings_approve_title
import kpt.feature.savings.generated.resources.feature_savings_approved_message
import kpt.feature.savings.generated.resources.feature_savings_cancel
import kpt.feature.savings.generated.resources.feature_savings_close
import kpt.feature.savings.generated.resources.feature_savings_offline_status_change
import kpt.feature.savings.generated.resources.feature_savings_reason_for_approval
import kpt.feature.savings.generated.resources.feature_savings_save
import kpt.feature.savings.generated.resources.feature_savings_select_date
import kpt.feature.savings.generated.resources.feature_savings_status_change_failed
import kpt.feature.savings.generated.resources.feature_savings_succeeded
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Approve or activate — ONE screen for both, see [SavingsStatusChangeViewModel].
 *
 * ## §6a adaptations
 *
 * `SavingsAccountApprovalUiState`'s four cases collapse into the submit flags plus a dialog, for
 * the reason every other write screen in this port does: the originals replaced the FORM with a
 * progress bar or an error page, so a refused approval lost the date the officer had picked.
 *
 * On success the original called `onBackPressed()` straight from a `LaunchedEffect` in the
 * composition body, with a snackbar fired alongside it — so the officer saw the screen vanish and
 * might or might not catch the message. A dialog states the outcome and the officer dismisses it.
 */
@Composable
internal fun SavingsStatusChangeScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavingsStatusChangeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            SavingsStatusChangeEvent.NavigateBack -> onBackPressed()
            // Same destination, different meaning: Done leaves AFTER a successful write, so the
            // summary behind re-reads the account and shows its new status.
            SavingsStatusChangeEvent.Done -> onBackPressed()
        }
    }

    SavingsStatusChangeContent(
        state = state,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun SavingsStatusChangeContent(
    state: SavingsStatusChangeState,
    onAction: (SavingsStatusChangeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(
            when (state.change) {
                SavingsStatusChange.APPROVE -> Res.string.feature_savings_approve_title
                SavingsStatusChange.ACTIVATE -> Res.string.feature_savings_activate_title
            },
        ),
        onBackPressed = { onAction(SavingsStatusChangeAction.NavigateBack) },
        modifier = modifier.testTag(SavingsStatusChangeTestTags.SCREEN),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            Spacer(Modifier.height(KptTheme.spacing.md))

            AppDatePickerTextField(
                value = DateHelper.getDateAsStringFromLong(state.date),
                label = stringResource(
                    when (state.change) {
                        SavingsStatusChange.APPROVE -> Res.string.feature_savings_approval_date
                        SavingsStatusChange.ACTIVATE -> Res.string.feature_savings_activation_date
                    },
                ),
                openDatePicker = { onAction(SavingsStatusChangeAction.OpenDatePicker) },
                modifier = Modifier.fillMaxWidth().testTag(SavingsStatusChangeTestTags.DATE),
            )

            // Approval only. Fineract's activate command takes no note, and the original's activate
            // screen correctly had no field — this keeps that difference rather than offering a
            // field whose value would be dropped.
            if (state.change.hasNote) {
                Spacer(Modifier.height(KptTheme.spacing.md))
                AppOutlinedTextField(
                    value = state.note,
                    onValueChange = { onAction(SavingsStatusChangeAction.OnNoteChanged(it)) },
                    label = stringResource(Res.string.feature_savings_reason_for_approval),
                    modifier = Modifier.fillMaxWidth()
                        .testTag(SavingsStatusChangeTestTags.NOTE),
                )
            }

            Spacer(Modifier.height(KptTheme.spacing.lg))

            AppButton(
                onClick = { onAction(SavingsStatusChangeAction.OnSubmit) },
                enabled = !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(DesignToken.spacing.dp44)
                    .testTag(SavingsStatusChangeTestTags.SUBMIT),
            ) {
                Text(
                    text = stringResource(Res.string.feature_savings_save),
                    style = AppTypography.bodyLarge,
                )
            }
        }
    }

    if (state.isDatePickerOpen) {
        StatusChangeDatePicker(state = state, onAction = onAction)
    }

    StatusChangeDialog(state, onAction)
}

@Composable
private fun StatusChangeDialog(
    state: SavingsStatusChangeState,
    onAction: (SavingsStatusChangeAction) -> Unit,
) {
    when (val dialog = state.dialogState) {
        null -> Unit
        DialogState.Succeeded -> AppStatusDialog(
            status = ResultStatus.SUCCESS,
            btnText = stringResource(Res.string.feature_savings_close),
            onConfirm = { onAction(SavingsStatusChangeAction.Done) },
            onDismissRequest = { onAction(SavingsStatusChangeAction.Done) },
            successTitle = stringResource(Res.string.feature_savings_succeeded),
            successMessage = stringResource(
                when (state.change) {
                    SavingsStatusChange.APPROVE -> Res.string.feature_savings_approved_message
                    SavingsStatusChange.ACTIVATE -> Res.string.feature_savings_activated_message
                },
            ),
            failureTitle = stringResource(Res.string.feature_savings_status_change_failed),
            failureMessage = "",
            modifier = Modifier.fillMaxSize()
                .testTag(SavingsStatusChangeTestTags.STATUS_DIALOG),
        )

        is DialogState.Failed -> AppStatusDialog(
            status = ResultStatus.FAILURE,
            btnText = stringResource(Res.string.feature_savings_close),
            // Dismiss, keeping the date the officer picked.
            onConfirm = { onAction(SavingsStatusChangeAction.CloseDialog) },
            onDismissRequest = { onAction(SavingsStatusChangeAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_savings_succeeded),
            successMessage = "",
            failureTitle = stringResource(
                when (state.change) {
                    SavingsStatusChange.APPROVE -> Res.string.feature_savings_approve_failed
                    SavingsStatusChange.ACTIVATE -> Res.string.feature_savings_activate_failed
                },
            ),
            failureMessage = when {
                // Offline is its own message: a status change cannot be queued, because the
                // account's status IS the server's answer.
                dialog.offline -> stringResource(Res.string.feature_savings_offline_status_change)
                else ->
                    dialog.msg
                        ?: stringResource(Res.string.feature_savings_status_change_failed)
            },
            modifier = Modifier.fillMaxSize()
                .testTag(SavingsStatusChangeTestTags.STATUS_DIALOG),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusChangeDatePicker(
    state: SavingsStatusChangeState,
    onAction: (SavingsStatusChangeAction) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.date,
        // Neither an approval nor an activation can happen in the future. The original restricted
        // both pickers to `>= now` — future-only — so TODAY was not selectable on either, the same
        // inverted rule as six other screens in this migration.
        selectableDates = remember { NotInTheFuture },
    )

    DatePickerDialog(
        onDismissRequest = { onAction(SavingsStatusChangeAction.CloseDatePicker) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis
                        ?.let { onAction(SavingsStatusChangeAction.OnDateChanged(it)) }
                    onAction(SavingsStatusChangeAction.CloseDatePicker)
                },
            ) { Text(stringResource(Res.string.feature_savings_select_date)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(SavingsStatusChangeAction.CloseDatePicker) }) {
                Text(stringResource(Res.string.feature_savings_cancel))
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
private object NotInTheFuture : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object SavingsStatusChangeTestTags {
    const val SCREEN = "savingsStatusChange:screen"
    const val DATE = "savingsStatusChange:date"
    const val NOTE = "savingsStatusChange:note"
    const val SUBMIT = "savingsStatusChange:submit"
    const val STATUS_DIALOG = "savingsStatusChange:statusDialog"
}
