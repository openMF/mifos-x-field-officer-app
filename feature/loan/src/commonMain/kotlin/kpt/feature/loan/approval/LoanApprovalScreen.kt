/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.approval

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_approval_amount_invalid
import kpt.feature.loan.generated.resources.feature_loan_approval_failure_title
import kpt.feature.loan.generated.resources.feature_loan_approval_note
import kpt.feature.loan.generated.resources.feature_loan_approval_success_message
import kpt.feature.loan.generated.resources.feature_loan_approval_success_title
import kpt.feature.loan.generated.resources.feature_loan_approve_loan
import kpt.feature.loan.generated.resources.feature_loan_approved_amount
import kpt.feature.loan.generated.resources.feature_loan_approved_on
import kpt.feature.loan.generated.resources.feature_loan_cancel
import kpt.feature.loan.generated.resources.feature_loan_dialog_continue
import kpt.feature.loan.generated.resources.feature_loan_expected_disbursement_on
import kpt.feature.loan.generated.resources.feature_loan_no_date_selected
import kpt.feature.loan.generated.resources.feature_loan_select_date
import kpt.feature.loan.generated.resources.feature_loan_submit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Carried over from `6b66e8a43:feature/loan/loanApproval/LoanAccountApprovalScreen.kt` — approved-on
 * and expected-disbursement date fields, approved amount, note, and the submit button, in that
 * order.
 *
 * ## §6a adaptations
 *
 * The four `LoanAccountApprovalUiState` cases become a [ScreenState] read for the PREFILL plus a
 * dialog for the WRITE's outcome. The original drove the write's outcome from the composition body:
 *
 * ```kotlin
 * is ShowLoanApproveSuccessfully -> { scope.launch { snackbar.showSnackbar(message) }; navigateBack() }
 * ```
 *
 * — a `launch` and a navigation performed during composition, so every recomposition re-fired both,
 * and the officer was navigated away before the snackbar could be read. The outcome is now a state
 * the ViewModel owns and a dialog whose button navigates, which is this codebase's established
 * shape (identical to `clientClosure`).
 *
 * ## The TRANSACTION AMOUNT field is not ported
 *
 * The original rendered a second amount field, validated it, and refused to submit when it was
 * blank or non-numeric — then **discarded it**. `LoanApproval` has no transaction-amount field and
 * the request never carried one. It was a required field with no effect, so it is absent rather
 * than drawn. Logged as `loan-approval-transaction-amount-discarded`.
 */
@Composable
internal fun LoanApprovalScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanApprovalViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val prefill by viewModel.prefill.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanApprovalEvent.NavigateBack -> onBackPressed()
        }
    }

    LoanApprovalContent(
        state = state,
        prefill = prefill,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoanApprovalContent(
    state: LoanApprovalState,
    prefill: ScreenState<LoanApprovalPrefill>,
    onAction: (LoanApprovalAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_approve_loan),
        onBackPressed = { onAction(LoanApprovalAction.NavigateBack) },
        modifier = modifier.testTag(LoanApprovalTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = prefill,
            onRetry = { onAction(LoanApprovalAction.OnRetry) },
            modifier = Modifier.fillMaxSize().padding(padding).testTag(LoanApprovalTestTags.CONTENT),
        ) { loan, _ ->
            // ONCE, on the first Content. Re-seeding on every emission would overwrite what the
            // officer has typed the moment a background refresh lands.
            LaunchedEffect(state.prefilled) {
                if (!state.prefilled) {
                    onAction(
                        LoanApprovalAction.Prefilled(
                            // `?.toString() ?: ""`, not `.toString()`. The original wrote
                            // `approvedPrincipal.toString()` on a nullable Double, so a loan
                            // without one put the literal text "null" in the amount field — which
                            // then failed its own numeric validation and produced a dead button.
                            amount = loan.approvedAmount?.toString().orEmpty(),
                            disbursementOn = loan.expectedDisbursementOn?.toStartOfDayMillis(),
                        ),
                    )
                }
            }

            ApprovalForm(state = state, onAction = onAction)
        }
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        is LoanApprovalState.DialogState.ShowStatusDialog -> AppStatusDialog(
            status = dialog.status,
            btnText = stringResource(Res.string.feature_loan_dialog_continue),
            // Back on success — the loan is approved and the profile behind this screen has
            // already been told to re-read. Dismiss on failure, so the officer can correct the
            // amount Fineract objected to without re-navigating.
            onConfirm = {
                onAction(
                    if (dialog.status == ResultStatus.SUCCESS) {
                        LoanApprovalAction.NavigateBack
                    } else {
                        LoanApprovalAction.CloseDialog
                    },
                )
            },
            onDismissRequest = { onAction(LoanApprovalAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_loan_approval_success_title),
            successMessage = stringResource(Res.string.feature_loan_approval_success_message),
            failureTitle = stringResource(Res.string.feature_loan_approval_failure_title),
            failureMessage = dialog.msg,
            modifier = Modifier.fillMaxSize().testTag(LoanApprovalTestTags.STATUS_DIALOG),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApprovalForm(
    state: LoanApprovalState,
    onAction: (LoanApprovalAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(KptTheme.spacing.md))

        AppDatePickerTextField(
            value = DateHelper.getDateAsStringFromLong(state.approvedOn),
            label = stringResource(Res.string.feature_loan_approved_on),
            openDatePicker = {
                onAction(LoanApprovalAction.OpenDatePicker(LoanApprovalState.DateField.APPROVED_ON))
            },
            modifier = Modifier.fillMaxWidth().testTag(LoanApprovalTestTags.APPROVED_ON),
        )
        Spacer(Modifier.height(KptTheme.spacing.md))

        AppDatePickerTextField(
            // The original rendered the literal string "null" here when the loan had no expected
            // disbursement date (`disbursementDate ?: "null"`).
            value = state.disbursementOn
                ?.let { DateHelper.getDateAsStringFromLong(it) }
                ?: stringResource(Res.string.feature_loan_no_date_selected),
            label = stringResource(Res.string.feature_loan_expected_disbursement_on),
            openDatePicker = {
                onAction(LoanApprovalAction.OpenDatePicker(LoanApprovalState.DateField.DISBURSEMENT_ON))
            },
            modifier = Modifier.fillMaxWidth().testTag(LoanApprovalTestTags.DISBURSEMENT_ON),
        )
        Spacer(Modifier.height(KptTheme.spacing.md))

        AppOutlinedTextField(
            value = state.approvedAmount,
            onValueChange = { onAction(LoanApprovalAction.UpdateApprovedAmount(it)) },
            label = stringResource(Res.string.feature_loan_approved_amount),
            keyboardType = KeyboardType.Number,
            // The original passed `error = null` always, so a bad amount silently did nothing.
            isError = state.amountError,
            errorText = stringResource(Res.string.feature_loan_approval_amount_invalid)
                .takeIf { state.amountError },
            modifier = Modifier.fillMaxWidth().testTag(LoanApprovalTestTags.AMOUNT),
        )
        Spacer(Modifier.height(KptTheme.spacing.md))

        AppOutlinedTextField(
            value = state.note,
            onValueChange = { onAction(LoanApprovalAction.UpdateNote(it)) },
            label = stringResource(Res.string.feature_loan_approval_note),
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth().testTag(LoanApprovalTestTags.NOTE),
        )
        Spacer(Modifier.height(KptTheme.spacing.md))

        AppButton(
            onClick = { onAction(LoanApprovalAction.OnSubmit) },
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.md)
                .heightIn(DesignToken.spacing.dp46)
                .testTag(LoanApprovalTestTags.SUBMIT),
        ) {
            Text(text = stringResource(Res.string.feature_loan_submit))
        }
    }

    state.datePicker?.let { field ->
        val initial = when (field) {
            LoanApprovalState.DateField.APPROVED_ON -> state.approvedOn
            LoanApprovalState.DateField.DISBURSEMENT_ON -> state.disbursementOn ?: state.approvedOn
        }
        // One picker per field, rebuilt when the field changes. The original kept two picker
        // states alive and chose between them with `if (pickApproveDate)`, which is equivalent —
        // except that it gave BOTH of them the same selectable-date rule, and that rule was wrong
        // in opposite directions for the two fields.
        //
        // ## The original allowed FUTURE dates only, on both fields
        //
        // `isSelectableDate { utcTimeMillis >= Clock.System.now() }`. For the APPROVED-ON date
        // that is backwards twice over: the picker's own cells are midnight instants, so today's
        // cell is already in the past by the time the dialog opens and **today was not
        // selectable** — and Fineract rejects an approval dated in the future outright, so every
        // date the officer COULD pick was one the server would refuse. The field defaulted to now
        // (valid) and any interaction with the picker made it invalid.
        //
        // So approved-on is restricted the other way, `<= now`, which is both what Fineract
        // accepts and what `clientClosure` already does for the same kind of field. Retroactive
        // approval is a normal operation — a loan approved on paper last week is entered today.
        //
        // Expected disbursement is left UNRESTRICTED: a disbursement scheduled for next month is
        // the ordinary case, and a past one is legitimate when the paperwork is catching up.
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initial,
            selectableDates = remember(field) {
                when (field) {
                    LoanApprovalState.DateField.APPROVED_ON -> NotInTheFuture
                    LoanApprovalState.DateField.DISBURSEMENT_ON -> AnyDate
                }
            },
        )

        DatePickerDialog(
            onDismissRequest = { onAction(LoanApprovalAction.CloseDatePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onAction(
                                when (field) {
                                    LoanApprovalState.DateField.APPROVED_ON ->
                                        LoanApprovalAction.UpdateApprovedOn(millis)
                                    LoanApprovalState.DateField.DISBURSEMENT_ON ->
                                        LoanApprovalAction.UpdateDisbursementOn(millis)
                                },
                            )
                        }
                        onAction(LoanApprovalAction.CloseDatePicker)
                    },
                ) { Text(stringResource(Res.string.feature_loan_select_date)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { onAction(LoanApprovalAction.CloseDatePicker) },
                ) { Text(stringResource(Res.string.feature_loan_cancel)) }
            },
        ) {
            DatePicker(state = pickerState, modifier = Modifier.testTag(LoanApprovalTestTags.DATE_PICKER))
        }
    }
}

/**
 * Approval cannot be dated in the future — Fineract refuses it. See the picker's comment for why
 * the original's rule was the inverse of this one.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
private object NotInTheFuture : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

/** Expected disbursement may be any date — past when catching up, future when scheduling. */
@OptIn(ExperimentalMaterial3Api::class)
private object AnyDate : SelectableDates

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanApprovalTestTags {
    const val SCREEN = "loanApproval:screen"
    const val CONTENT = "loanApproval:content"
    const val APPROVED_ON = "loanApproval:approvedOn"
    const val DISBURSEMENT_ON = "loanApproval:disbursementOn"
    const val AMOUNT = "loanApproval:amount"
    const val NOTE = "loanApproval:note"
    const val SUBMIT = "loanApproval:submit"
    const val DATE_PICKER = "loanApproval:datePicker"
    const val STATUS_DIALOG = "loanApproval:statusDialog"
}
