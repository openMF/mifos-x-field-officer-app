/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.disbursement

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
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_approval_amount_invalid
import kpt.feature.loan.generated.resources.feature_loan_approval_disbursement_date
import kpt.feature.loan.generated.resources.feature_loan_cancel
import kpt.feature.loan.generated.resources.feature_loan_dialog_continue
import kpt.feature.loan.generated.resources.feature_loan_disburse_loan
import kpt.feature.loan.generated.resources.feature_loan_disbursement_failure_title
import kpt.feature.loan.generated.resources.feature_loan_disbursement_note
import kpt.feature.loan.generated.resources.feature_loan_disbursement_success_message
import kpt.feature.loan.generated.resources.feature_loan_disbursement_success_title
import kpt.feature.loan.generated.resources.feature_loan_loan_amount_disbursed
import kpt.feature.loan.generated.resources.feature_loan_payment_type
import kpt.feature.loan.generated.resources.feature_loan_select_date
import kpt.feature.loan.generated.resources.feature_loan_submit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Carried over from `6b66e8a43:feature/loan/loanDisbursement/LoanAccountDisbursementScreen.kt` —
 * the disbursement date, the amount prefilled from Fineract's expected net disbursal, the payment
 * type dropdown, a note, and submit.
 *
 * ## §6a adaptations
 *
 * The template read becomes [ScreenState] through [ScreenContent]; the write's outcome becomes a
 * dialog. The original drove both through one `uiState`, which had two consequences: a
 * disbursement in flight replaced the form with a progress bar (losing nothing, but also showing
 * nothing), and on success it did this from the composition body —
 *
 * ```kotlin
 * is ShowDisburseLoanSuccessfully -> { scope.launch { snackbar.showSnackbar(message) }; navigateBack() }
 * ```
 *
 * — a launch and a navigation during composition, re-fired on every recomposition, with the
 * officer navigated away before the message could be read. Same shape as the approval screen's.
 *
 * ## The date rule is inverted here too, for the fourth time
 *
 * The original allowed FUTURE dates only. `actualDisbursementDate` is the date money actually left
 * the branch, so a future value is meaningless and Fineract refuses it — and because the picker's
 * cells are midnight instants, today was not selectable either. Restricted to `<= now`, as on the
 * approval and repayment screens.
 */
@Composable
internal fun LoanDisbursementScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanDisbursementViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val template by viewModel.template.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanDisbursementEvent.NavigateBack -> onBackPressed()
        }
    }

    LoanDisbursementContent(
        state = state,
        template = template,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun LoanDisbursementContent(
    state: LoanDisbursementState,
    template: ScreenState<LoanDisbursementTemplate>,
    onAction: (LoanDisbursementAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_disburse_loan),
        onBackPressed = { onAction(LoanDisbursementAction.NavigateBack) },
        modifier = modifier.testTag(LoanDisbursementTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = template,
            onRetry = { onAction(LoanDisbursementAction.OnRetry) },
            modifier = Modifier.fillMaxSize().padding(padding).testTag(LoanDisbursementTestTags.CONTENT),
        ) { content, _ ->
            // ONCE. Re-seeding on every emission would overwrite an amount the officer had
            // adjusted the moment a background refresh landed.
            LaunchedEffect(state.prefilled) {
                if (!state.prefilled) {
                    // `netDisbursalAmount` is non-null in the fork's template, so this cannot
                    // render the word "null" — the original wrote
                    // `loanTransactionTemplate.amount.toString()` on a nullable Double.
                    onAction(LoanDisbursementAction.Prefilled(content.netDisbursalAmount.toString()))
                }
            }

            DisbursementForm(state = state, template = content, onAction = onAction)
        }
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        is LoanDisbursementState.DialogState.ShowStatusDialog -> AppStatusDialog(
            status = dialog.status,
            btnText = stringResource(Res.string.feature_loan_dialog_continue),
            onConfirm = {
                onAction(
                    if (dialog.status == ResultStatus.SUCCESS) {
                        LoanDisbursementAction.NavigateBack
                    } else {
                        // Dismiss on failure, so the officer can correct the date Fineract
                        // objected to without re-entering the amount and note.
                        LoanDisbursementAction.CloseDialog
                    },
                )
            },
            onDismissRequest = { onAction(LoanDisbursementAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_loan_disbursement_success_title),
            successMessage = stringResource(Res.string.feature_loan_disbursement_success_message),
            failureTitle = stringResource(Res.string.feature_loan_disbursement_failure_title),
            failureMessage = dialog.msg,
            modifier = Modifier.fillMaxSize().testTag(LoanDisbursementTestTags.STATUS_DIALOG),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisbursementForm(
    state: LoanDisbursementState,
    template: LoanDisbursementTemplate,
    onAction: (LoanDisbursementAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(KptTheme.spacing.md))

        AppDatePickerTextField(
            value = DateHelper.getDateAsStringFromLong(state.date),
            label = stringResource(Res.string.feature_loan_approval_disbursement_date),
            openDatePicker = { onAction(LoanDisbursementAction.UpdateDatePicker(true)) },
            modifier = Modifier.fillMaxWidth().testTag(LoanDisbursementTestTags.DATE),
        )

        if (state.showDatePicker) {
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.date,
                selectableDates = remember { NotInTheFuture },
            )
            DatePickerDialog(
                onDismissRequest = { onAction(LoanDisbursementAction.UpdateDatePicker(false)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pickerState.selectedDateMillis?.let {
                                onAction(LoanDisbursementAction.UpdateDate(it))
                            }
                            onAction(LoanDisbursementAction.UpdateDatePicker(false))
                        },
                    ) { Text(stringResource(Res.string.feature_loan_select_date)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onAction(LoanDisbursementAction.UpdateDatePicker(false)) },
                    ) { Text(stringResource(Res.string.feature_loan_cancel)) }
                },
            ) {
                DatePicker(state = pickerState)
            }
        }

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppOutlinedTextField(
            value = state.amount,
            onValueChange = { onAction(LoanDisbursementAction.UpdateAmount(it)) },
            label = stringResource(Res.string.feature_loan_loan_amount_disbursed),
            keyboardType = KeyboardType.Number,
            // The original passed `error = null` always, so an invalid amount silently refused to
            // submit with nothing on screen to say why.
            isError = state.amountError,
            errorText = stringResource(Res.string.feature_loan_approval_amount_invalid)
                .takeIf { state.amountError },
            modifier = Modifier.fillMaxWidth().testTag(LoanDisbursementTestTags.AMOUNT),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppTextFieldDropdown(
            value = state.paymentTypeName,
            // The chosen OPTION is dispatched. The original resolved
            // `paymentTypeOptions[index].id` at selection time — an index into a list whose
            // nameless entries had already shifted it.
            onOptionSelected = { index, _ ->
                template.paymentTypes.getOrNull(index)
                    ?.let { onAction(LoanDisbursementAction.SelectPaymentType(it)) }
            },
            label = stringResource(Res.string.feature_loan_payment_type),
            options = template.paymentTypes.map { it.name },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().testTag(LoanDisbursementTestTags.PAYMENT_TYPE),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppOutlinedTextField(
            value = state.note,
            onValueChange = { onAction(LoanDisbursementAction.UpdateNote(it)) },
            label = stringResource(Res.string.feature_loan_disbursement_note),
            modifier = Modifier.fillMaxWidth().testTag(LoanDisbursementTestTags.NOTE),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppButton(
            onClick = { onAction(LoanDisbursementAction.OnSubmit) },
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.md)
                .heightIn(DesignToken.spacing.dp44)
                .testTag(LoanDisbursementTestTags.SUBMIT),
        ) {
            Text(text = stringResource(Res.string.feature_loan_submit))
        }

        Spacer(Modifier.height(KptTheme.spacing.md))
    }
}

/**
 * `actualDisbursementDate` is the date money LEFT — it cannot be in the future, and Fineract
 * refuses one that is. The original allowed future dates only.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
private object NotInTheFuture : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanDisbursementTestTags {
    const val SCREEN = "loanDisbursement:screen"
    const val CONTENT = "loanDisbursement:content"
    const val DATE = "loanDisbursement:date"
    const val AMOUNT = "loanDisbursement:amount"
    const val PAYMENT_TYPE = "loanDisbursement:paymentType"
    const val NOTE = "loanDisbursement:note"
    const val SUBMIT = "loanDisbursement:submit"
    const val STATUS_DIALOG = "loanDisbursement:statusDialog"
}
