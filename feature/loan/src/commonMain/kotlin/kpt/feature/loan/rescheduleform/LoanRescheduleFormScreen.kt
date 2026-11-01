/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.rescheduleform

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppSweetError
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppCheckBox
import kpt.core.ui.components.AppProgressIndicator
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_cancel
import kpt.feature.loan.generated.resources.feature_loan_dialog_continue
import kpt.feature.loan.generated.resources.feature_loan_reschedule_adjust_interest
import kpt.feature.loan.generated.resources.feature_loan_reschedule_change_repayment_date
import kpt.feature.loan.generated.resources.feature_loan_reschedule_comments
import kpt.feature.loan.generated.resources.feature_loan_reschedule_error_invalid_number
import kpt.feature.loan.generated.resources.feature_loan_reschedule_error_pick_a_date
import kpt.feature.loan.generated.resources.feature_loan_reschedule_error_select_reason
import kpt.feature.loan.generated.resources.feature_loan_reschedule_extend_repayment
import kpt.feature.loan.generated.resources.feature_loan_reschedule_failed_title
import kpt.feature.loan.generated.resources.feature_loan_reschedule_from_date
import kpt.feature.loan.generated.resources.feature_loan_reschedule_installment_rescheduled_to
import kpt.feature.loan.generated.resources.feature_loan_reschedule_interest_grace
import kpt.feature.loan.generated.resources.feature_loan_reschedule_introduce_grace_periods
import kpt.feature.loan.generated.resources.feature_loan_reschedule_new_interest_rate
import kpt.feature.loan.generated.resources.feature_loan_reschedule_new_repayments
import kpt.feature.loan.generated.resources.feature_loan_reschedule_principal_grace
import kpt.feature.loan.generated.resources.feature_loan_reschedule_proposed_message
import kpt.feature.loan.generated.resources.feature_loan_reschedule_proposed_title
import kpt.feature.loan.generated.resources.feature_loan_reschedule_reason
import kpt.feature.loan.generated.resources.feature_loan_reschedule_reasons_failed
import kpt.feature.loan.generated.resources.feature_loan_reschedule_submitted_on
import kpt.feature.loan.generated.resources.feature_loan_reschedule_waive_penalties
import kpt.feature.loan.generated.resources.feature_loan_reschedules_new_title
import kpt.feature.loan.generated.resources.feature_loan_retry
import kpt.feature.loan.generated.resources.feature_loan_select_date
import kpt.feature.loan.generated.resources.feature_loan_submit
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/loan/createLoanReschedules/LoanRescheduleFormScreen.kt` —
 * the two required dates, the reason dropdown, comments, and the five checkbox-gated optional
 * changes with the fields each reveals.
 *
 * §6a adaptations: the template read's Loading/Error come out of `DialogState` (so the submit's
 * outcome has the dialog to itself), the four numeric fields are keyed by a `RescheduleField`
 * enum rather than four separate state properties plus four separate actions, and each error sits
 * ON its field rather than in a single `reasonIdError`.
 */
@Composable
internal fun LoanRescheduleFormScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanRescheduleFormViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanRescheduleFormEvent.NavigateBack -> onBackPressed()
        }
    }

    LoanRescheduleFormContent(
        state = state,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun LoanRescheduleFormContent(
    state: LoanRescheduleFormState,
    onAction: (LoanRescheduleFormAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_reschedules_new_title),
        onBackPressed = { onAction(LoanRescheduleFormAction.NavigateBack) },
        modifier = modifier.testTag(LoanRescheduleFormTestTags.SCREEN),
    ) { padding ->
        when {
            state.isLoadingReasons -> AppProgressIndicator(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .testTag(LoanRescheduleFormTestTags.LOADING),
            )

            // Without the reasons there is no proposal to make — the reason is required.
            state.reasonsError -> AppSweetError(
                message = stringResource(Res.string.feature_loan_reschedule_reasons_failed),
                onclick = { onAction(LoanRescheduleFormAction.OnRetryFetching) },
                modifier = Modifier.fillMaxSize().padding(padding)
                    .testTag(LoanRescheduleFormTestTags.REASONS_ERROR),
            )

            else -> RescheduleForm(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        LoanRescheduleFormState.DialogState.Submitted -> AppStatusDialog(
            status = ResultStatus.SUCCESS,
            btnText = stringResource(Res.string.feature_loan_dialog_continue),
            // Back to the list, which re-reads and shows the new proposal as Pending.
            onConfirm = { onAction(LoanRescheduleFormAction.NavigateBack) },
            onDismissRequest = { onAction(LoanRescheduleFormAction.NavigateBack) },
            successTitle = stringResource(Res.string.feature_loan_reschedule_proposed_title),
            successMessage = stringResource(Res.string.feature_loan_reschedule_proposed_message),
            failureTitle = stringResource(Res.string.feature_loan_reschedule_failed_title),
            failureMessage = "",
            modifier = Modifier.fillMaxSize().testTag(LoanRescheduleFormTestTags.STATUS_DIALOG),
        )

        is LoanRescheduleFormState.DialogState.Failed -> AppStatusDialog(
            status = ResultStatus.FAILURE,
            // The dialog's single action IS the retry when the failure is retryable:
            // `OnRetrySubmit` re-sends the proven draft. It was handled by the ViewModel and
            // dispatched by nothing — found by the action-dispatch sweep.
            btnText = stringResource(
                if (dialog.retryable) {
                    Res.string.feature_loan_retry
                } else {
                    Res.string.feature_loan_dialog_continue
                },
            ),
            onConfirm = {
                onAction(
                    if (dialog.retryable) {
                        LoanRescheduleFormAction.OnRetrySubmit
                    } else {
                        LoanRescheduleFormAction.CloseDialog
                    },
                )
            },
            onDismissRequest = { onAction(LoanRescheduleFormAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_loan_reschedule_proposed_title),
            successMessage = stringResource(Res.string.feature_loan_reschedule_proposed_message),
            failureTitle = stringResource(Res.string.feature_loan_reschedule_failed_title),
            failureMessage = dialog.msg,
            modifier = Modifier.fillMaxSize().testTag(LoanRescheduleFormTestTags.STATUS_DIALOG),
        )
    }
}

@Composable
private fun RescheduleForm(
    state: LoanRescheduleFormState,
    onAction: (LoanRescheduleFormAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = KptTheme.spacing.md)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(KptTheme.spacing.md))

        AppDatePickerTextField(
            value = DateHelper.getDateAsStringFromLong(state.rescheduleFrom),
            label = stringResource(Res.string.feature_loan_reschedule_from_date),
            openDatePicker = {
                onAction(LoanRescheduleFormAction.OpenDatePicker(RescheduleDate.FROM))
            },
            modifier = Modifier.fillMaxWidth().testTag(LoanRescheduleFormTestTags.FROM_DATE),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppTextFieldDropdown(
            value = state.reason?.name.orEmpty(),
            onOptionSelected = { index, _ ->
                state.reasons.getOrNull(index)
                    ?.let { onAction(LoanRescheduleFormAction.OnReasonSelected(it)) }
            },
            options = state.reasons.map { it.name },
            label = stringResource(Res.string.feature_loan_reschedule_reason),
            readOnly = true,
            errorMessage = state.errorFor(RescheduleField.REASON),
            modifier = Modifier.fillMaxWidth().testTag(LoanRescheduleFormTestTags.REASON),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppDatePickerTextField(
            value = DateHelper.getDateAsStringFromLong(state.submittedOn),
            label = stringResource(Res.string.feature_loan_reschedule_submitted_on),
            openDatePicker = {
                onAction(LoanRescheduleFormAction.OpenDatePicker(RescheduleDate.SUBMITTED_ON))
            },
            modifier = Modifier.fillMaxWidth().testTag(LoanRescheduleFormTestTags.SUBMITTED_ON),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppOutlinedTextField(
            value = state.comments,
            onValueChange = { onAction(LoanRescheduleFormAction.OnCommentsChanged(it)) },
            label = stringResource(Res.string.feature_loan_reschedule_comments),
            modifier = Modifier.fillMaxWidth().testTag(LoanRescheduleFormTestTags.COMMENTS),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        // The five optional changes, each revealing its own fields. Unchecking clears them.
        OptionCheckBox(
            option = RescheduleOption.CHANGE_REPAYMENT_DATE,
            label = stringResource(Res.string.feature_loan_reschedule_change_repayment_date),
            checked = state.changeRepaymentDate,
            onAction = onAction,
        )
        if (state.changeRepaymentDate) {
            AppDatePickerTextField(
                value = state.adjustedDueDate
                    ?.let { DateHelper.getDateAsStringFromLong(it) }
                    .orEmpty(),
                label = stringResource(Res.string.feature_loan_reschedule_installment_rescheduled_to),
                errorMessage = state.errorFor(RescheduleField.ADJUSTED_DUE_DATE),
                openDatePicker = {
                    onAction(LoanRescheduleFormAction.OpenDatePicker(RescheduleDate.ADJUSTED_DUE))
                },
                modifier = Modifier.fillMaxWidth()
                    .testTag(LoanRescheduleFormTestTags.ADJUSTED_DUE_DATE),
            )
        }

        OptionCheckBox(
            option = RescheduleOption.INTRODUCE_GRACE_PERIODS,
            label = stringResource(Res.string.feature_loan_reschedule_introduce_grace_periods),
            checked = state.introduceGracePeriods,
            onAction = onAction,
        )
        if (state.introduceGracePeriods) {
            NumberField(
                field = RescheduleField.GRACE_ON_PRINCIPAL,
                label = stringResource(Res.string.feature_loan_reschedule_principal_grace),
                state = state,
                onAction = onAction,
            )
            NumberField(
                field = RescheduleField.GRACE_ON_INTEREST,
                label = stringResource(Res.string.feature_loan_reschedule_interest_grace),
                state = state,
                onAction = onAction,
            )
        }

        OptionCheckBox(
            option = RescheduleOption.EXTEND_REPAYMENT_PERIOD,
            label = stringResource(Res.string.feature_loan_reschedule_extend_repayment),
            checked = state.extendRepaymentPeriod,
            onAction = onAction,
        )
        if (state.extendRepaymentPeriod) {
            NumberField(
                field = RescheduleField.EXTRA_TERMS,
                label = stringResource(Res.string.feature_loan_reschedule_new_repayments),
                state = state,
                onAction = onAction,
            )
        }

        OptionCheckBox(
            option = RescheduleOption.ADJUST_INTEREST_RATE,
            label = stringResource(Res.string.feature_loan_reschedule_adjust_interest),
            checked = state.adjustInterestRate,
            onAction = onAction,
        )
        if (state.adjustInterestRate) {
            NumberField(
                field = RescheduleField.NEW_INTEREST_RATE,
                label = stringResource(Res.string.feature_loan_reschedule_new_interest_rate),
                state = state,
                onAction = onAction,
            )
        }

        // No field of its own — the checkbox IS the value.
        OptionCheckBox(
            option = RescheduleOption.WAIVE_PENALTIES,
            label = stringResource(Res.string.feature_loan_reschedule_waive_penalties),
            checked = state.waivePenalties,
            onAction = onAction,
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppButton(
            onClick = { onAction(LoanRescheduleFormAction.OnSubmit) },
            // Always enabled while not submitting, so tapping it REPORTS what is missing. The
            // original's `isSubmitEnabled` disabled the button whenever any required field was
            // blank — which leaves an officer looking at a dead button with no indication of
            // which of six fields it is waiting for.
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(DesignToken.spacing.dp44)
                .testTag(LoanRescheduleFormTestTags.SUBMIT),
        ) {
            Text(text = stringResource(Res.string.feature_loan_submit))
        }

        Spacer(Modifier.height(KptTheme.spacing.md))
    }

    state.activeDatePicker?.let { which -> RescheduleDatePicker(which, state, onAction) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RescheduleDatePicker(
    which: RescheduleDate,
    state: LoanRescheduleFormState,
    onAction: (LoanRescheduleFormAction) -> Unit,
) {
    val initial = when (which) {
        RescheduleDate.FROM -> state.rescheduleFrom
        RescheduleDate.SUBMITTED_ON -> state.submittedOn
        // Opens at the from-date rather than today: the installment is being moved relative to
        // the one being rescheduled, so that is the useful starting point.
        RescheduleDate.ADJUSTED_DUE -> state.adjustedDueDate ?: state.rescheduleFrom
    }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initial)

    DatePickerDialog(
        onDismissRequest = { onAction(LoanRescheduleFormAction.CloseDatePicker) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onAction(LoanRescheduleFormAction.OnDateChanged(which, it))
                    }
                    onAction(LoanRescheduleFormAction.CloseDatePicker)
                },
            ) { Text(stringResource(Res.string.feature_loan_select_date)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(LoanRescheduleFormAction.CloseDatePicker) }) {
                Text(stringResource(Res.string.feature_loan_cancel))
            }
        },
    ) {
        // All three UNRESTRICTED. A reschedule moves a FUTURE installment and is submitted today,
        // so neither a past-only nor a future-only rule fits — and the original restricted none of
        // them either.
        DatePicker(state = pickerState)
    }
}

@Composable
private fun OptionCheckBox(
    option: RescheduleOption,
    label: String,
    checked: Boolean,
    onAction: (LoanRescheduleFormAction) -> Unit,
) {
    AppCheckBox(
        text = label,
        checked = checked,
        onCheckChanged = { onAction(LoanRescheduleFormAction.OnToggle(option, it)) },
        modifier = Modifier.fillMaxWidth().testTag(LoanRescheduleFormTestTags.option(option)),
    )
}

@Composable
private fun NumberField(
    field: RescheduleField,
    label: String,
    state: LoanRescheduleFormState,
    onAction: (LoanRescheduleFormAction) -> Unit,
) {
    AppOutlinedTextField(
        value = state.number(field),
        onValueChange = { onAction(LoanRescheduleFormAction.OnNumberChanged(field, it)) },
        label = label,
        keyboardType = KeyboardType.Number,
        isError = field in state.errors,
        errorText = state.errorFor(field),
        modifier = Modifier.fillMaxWidth().testTag(LoanRescheduleFormTestTags.number(field)),
    )
}

/**
 * The error message for one field, or null.
 *
 * `@Composable` because the copy is a string resource; the ViewModel names WHICH field failed and
 * the screen owns how it reads — the same split as the transfer screen's `TransferFieldError`.
 */
@Composable
internal fun LoanRescheduleFormState.errorFor(field: RescheduleField): String? =
    if (field in errors) stringResource(field.messageRes()) else null

internal fun RescheduleField.messageRes(): StringResource = when (this) {
    RescheduleField.REASON -> Res.string.feature_loan_reschedule_error_select_reason
    RescheduleField.ADJUSTED_DUE_DATE -> Res.string.feature_loan_reschedule_error_pick_a_date
    RescheduleField.GRACE_ON_PRINCIPAL,
    RescheduleField.GRACE_ON_INTEREST,
    RescheduleField.EXTRA_TERMS,
    RescheduleField.NEW_INTEREST_RATE,
    -> Res.string.feature_loan_reschedule_error_invalid_number
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanRescheduleFormTestTags {
    const val SCREEN = "loanRescheduleForm:screen"
    const val LOADING = "loanRescheduleForm:loading"
    const val REASONS_ERROR = "loanRescheduleForm:reasonsError"
    const val FROM_DATE = "loanRescheduleForm:fromDate"
    const val REASON = "loanRescheduleForm:reason"
    const val SUBMITTED_ON = "loanRescheduleForm:submittedOn"
    const val COMMENTS = "loanRescheduleForm:comments"
    const val ADJUSTED_DUE_DATE = "loanRescheduleForm:adjustedDueDate"
    const val SUBMIT = "loanRescheduleForm:submit"
    const val STATUS_DIALOG = "loanRescheduleForm:statusDialog"
    fun option(option: RescheduleOption): String = "loanRescheduleForm:option:${option.name}"
    fun number(field: RescheduleField): String = "loanRescheduleForm:number:${field.name}"
}
