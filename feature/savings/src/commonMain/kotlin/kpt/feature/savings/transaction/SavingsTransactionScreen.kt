/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.savings.generated.resources.Res
import kpt.feature.savings.generated.resources.feature_savings_amount
import kpt.feature.savings.generated.resources.feature_savings_cancel
import kpt.feature.savings.generated.resources.feature_savings_close
import kpt.feature.savings.generated.resources.feature_savings_confirm
import kpt.feature.savings.generated.resources.feature_savings_date
import kpt.feature.savings.generated.resources.feature_savings_deposit_failed
import kpt.feature.savings.generated.resources.feature_savings_deposit_title
import kpt.feature.savings.generated.resources.feature_savings_error_amount_not_a_number
import kpt.feature.savings.generated.resources.feature_savings_error_amount_not_positive
import kpt.feature.savings.generated.resources.feature_savings_error_amount_required
import kpt.feature.savings.generated.resources.feature_savings_note
import kpt.feature.savings.generated.resources.feature_savings_offline_transaction
import kpt.feature.savings.generated.resources.feature_savings_payment_type
import kpt.feature.savings.generated.resources.feature_savings_payment_types_retry
import kpt.feature.savings.generated.resources.feature_savings_review
import kpt.feature.savings.generated.resources.feature_savings_review_title
import kpt.feature.savings.generated.resources.feature_savings_select_date
import kpt.feature.savings.generated.resources.feature_savings_succeeded
import kpt.feature.savings.generated.resources.feature_savings_transaction_failed
import kpt.feature.savings.generated.resources.feature_savings_transaction_recorded
import kpt.feature.savings.generated.resources.feature_savings_transaction_reference
import kpt.feature.savings.generated.resources.feature_savings_withdraw_failed
import kpt.feature.savings.generated.resources.feature_savings_withdraw_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Deposit or withdraw — ported from
 * `6b66e8a43:feature/savings/savingsAccountTransaction/SavingsAccountTransactionScreen.kt`
 * (532 lines). See [SavingsTransactionViewModel] for the `toFloat()`-money defect, the negative
 * amount that passed validation, the `0` payment type and the logged request body.
 *
 * ## The review sheet's confirm button said "Review Transaction"
 *
 * The original opened a review dialog whose confirm button was labelled
 * `feature_savings_review_transaction` — so the sheet that WAS the review asked the officer to
 * "Review Transaction" in order to submit it, on a screen where the next tap moves money. Renamed
 * to Confirm, and the review itself is headed "Review".
 *
 * ## §6a adaptations
 *
 * `SavingsAccountTransactionUiState`'s cases become the submit flags plus a dialog, for the usual
 * reason: the original replaced the FORM with a progress bar, so a refused transaction lost the
 * amount the officer had typed — the one value they cannot re-derive, because the cash is already
 * in their hand.
 */
@Composable
internal fun SavingsTransactionScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavingsTransactionViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            SavingsTransactionEvent.NavigateBack -> onBackPressed()
            // Leaves AFTER the write, so the summary behind re-reads its balance.
            SavingsTransactionEvent.Done -> onBackPressed()
        }
    }

    SavingsTransactionContent(
        state = state,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun SavingsTransactionContent(
    state: SavingsTransactionState,
    onAction: (SavingsTransactionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(
            when (state.kind) {
                TransactionKind.DEPOSIT -> Res.string.feature_savings_deposit_title
                TransactionKind.WITHDRAWAL -> Res.string.feature_savings_withdraw_title
            },
        ),
        onBackPressed = { onAction(SavingsTransactionAction.NavigateBack) },
        modifier = modifier.testTag(SavingsTransactionTestTags.SCREEN),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = KptTheme.spacing.md)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(KptTheme.spacing.md))

            AppOutlinedTextField(
                value = state.amount,
                onValueChange = { onAction(SavingsTransactionAction.OnAmountChanged(it)) },
                label = stringResource(Res.string.feature_savings_amount),
                keyboardType = KeyboardType.Decimal,
                // Marked ON THE FIELD. The original showed one of three snackbars and left the
                // field unmarked, so the officer had to remember which input the message meant.
                isError = state.amountError != null,
                errorText = state.amountError?.let { stringResource(it.label()) },
                modifier = Modifier.fillMaxWidth().testTag(SavingsTransactionTestTags.AMOUNT),
            )

            Spacer(Modifier.height(KptTheme.spacing.md))

            AppDatePickerTextField(
                value = DateHelper.getDateAsStringFromLong(state.date),
                label = stringResource(Res.string.feature_savings_date),
                openDatePicker = { onAction(SavingsTransactionAction.OpenDatePicker) },
                modifier = Modifier.fillMaxWidth().testTag(SavingsTransactionTestTags.DATE),
            )

            // Drawn only when the tenant configures payment types. The original rendered a free
            // TEXT field for the payment type whose value went nowhere, while `paymentTypeId`
            // stayed `0` — the label and the id were unrelated values.
            if (state.paymentTypes.isNotEmpty()) {
                Spacer(Modifier.height(KptTheme.spacing.md))
                AppTextFieldDropdown(
                    value = state.paymentType?.name.orEmpty(),
                    onOptionSelected = { index, _ ->
                        state.paymentTypes.getOrNull(index)
                            ?.let { onAction(SavingsTransactionAction.OnPaymentTypeSelected(it)) }
                    },
                    options = state.paymentTypes.map { it.name },
                    label = stringResource(Res.string.feature_savings_payment_type),
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                        .testTag(SavingsTransactionTestTags.PAYMENT_TYPE),
                )
            }

            // Stated, and explicitly NOT a blocker: the transaction can still be recorded. The
            // notice IS the retry — a separate button would compete with Review for attention on
            // a screen whose primary action moves money, and a notice with no way to act on it
            // would leave `OnRetryTemplate` undispatched (the defect the action-dispatch sweep
            // catches, which is exactly how this was found).
            if (state.templateFailed) {
                Spacer(Modifier.height(KptTheme.spacing.sm))
                Text(
                    text = stringResource(Res.string.feature_savings_payment_types_retry),
                    style = AppTypography.labelSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !state.isLoadingTemplate) {
                            onAction(SavingsTransactionAction.OnRetryTemplate)
                        }
                        .testTag(SavingsTransactionTestTags.TEMPLATE_NOTICE),
                )
            }

            Spacer(Modifier.height(KptTheme.spacing.md))

            AppOutlinedTextField(
                value = state.note,
                onValueChange = { onAction(SavingsTransactionAction.OnNoteChanged(it)) },
                label = stringResource(Res.string.feature_savings_note),
                modifier = Modifier.fillMaxWidth().testTag(SavingsTransactionTestTags.NOTE),
            )

            Spacer(Modifier.height(KptTheme.spacing.lg))

            AppButton(
                onClick = { onAction(SavingsTransactionAction.OnReview) },
                // Enabled, so tapping it REPORTS what is wrong with the amount.
                enabled = !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(DesignToken.spacing.dp44)
                    .testTag(SavingsTransactionTestTags.REVIEW),
            ) {
                Text(
                    text = stringResource(Res.string.feature_savings_review),
                    style = AppTypography.bodyLarge,
                )
            }

            Spacer(Modifier.height(KptTheme.spacing.md))
        }
    }

    if (state.isDatePickerOpen) {
        TransactionDatePicker(state = state, onAction = onAction)
    }

    if (state.isReviewing) {
        ReviewSheet(state = state, onAction = onAction)
    }

    TransactionStatusDialog(state, onAction)
}

@Composable
private fun TransactionStatusDialog(
    state: SavingsTransactionState,
    onAction: (SavingsTransactionAction) -> Unit,
) {
    when (val dialog = state.dialogState) {
        null -> Unit
        is TransactionDialog.Succeeded -> AppStatusDialog(
            status = ResultStatus.SUCCESS,
            btnText = stringResource(Res.string.feature_savings_close),
            onConfirm = { onAction(SavingsTransactionAction.Done) },
            onDismissRequest = { onAction(SavingsTransactionAction.Done) },
            successTitle = stringResource(Res.string.feature_savings_succeeded),
            successMessage = dialog.transactionId
                // Fineract's transaction id, so the officer has a reference for the person whose
                // cash they just took. The original showed a bare "transaction done" snackbar.
                ?.let { stringResource(Res.string.feature_savings_transaction_reference, it) }
                ?: stringResource(Res.string.feature_savings_transaction_recorded),
            failureTitle = stringResource(Res.string.feature_savings_transaction_failed),
            failureMessage = "",
            modifier = Modifier.fillMaxSize()
                .testTag(SavingsTransactionTestTags.STATUS_DIALOG),
        )

        is TransactionDialog.Failed -> AppStatusDialog(
            status = ResultStatus.FAILURE,
            btnText = stringResource(Res.string.feature_savings_close),
            // Dismiss, keeping the amount. The cash is already in the officer's hand.
            onConfirm = { onAction(SavingsTransactionAction.CloseDialog) },
            onDismissRequest = { onAction(SavingsTransactionAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_savings_succeeded),
            successMessage = "",
            failureTitle = stringResource(
                when (state.kind) {
                    TransactionKind.DEPOSIT -> Res.string.feature_savings_deposit_failed
                    TransactionKind.WITHDRAWAL -> Res.string.feature_savings_withdraw_failed
                },
            ),
            failureMessage = when {
                dialog.offline -> stringResource(Res.string.feature_savings_offline_transaction)
                else -> dialog.msg ?: stringResource(Res.string.feature_savings_transaction_failed)
            },
            modifier = Modifier.fillMaxSize()
                .testTag(SavingsTransactionTestTags.STATUS_DIALOG),
        )
    }
}

/**
 * What is about to be sent, before it is sent.
 *
 * Kept from the original because it is the right pattern for a money-moving write — only its
 * confirm button's LABEL changes, from "Review Transaction" to Confirm.
 */
@Composable
private fun ReviewSheet(
    state: SavingsTransactionState,
    onAction: (SavingsTransactionAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(SavingsTransactionAction.CloseReview) },
        modifier = Modifier.testTag(SavingsTransactionTestTags.REVIEW_SHEET),
        title = { Text(stringResource(Res.string.feature_savings_review_title)) },
        confirmButton = {
            TextButton(
                onClick = { onAction(SavingsTransactionAction.OnConfirm) },
                modifier = Modifier.testTag(SavingsTransactionTestTags.CONFIRM),
            ) { Text(stringResource(Res.string.feature_savings_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(SavingsTransactionAction.CloseReview) }) {
                Text(stringResource(Res.string.feature_savings_cancel))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs)) {
                ReviewRow(stringResource(Res.string.feature_savings_amount), state.amount)
                ReviewRow(
                    stringResource(Res.string.feature_savings_date),
                    DateHelper.getDateAsStringFromLong(state.date),
                )
                // Only when one was chosen — the original always printed the row, showing an empty
                // value when nothing had been picked.
                state.paymentType?.let {
                    ReviewRow(stringResource(Res.string.feature_savings_payment_type), it.name)
                }
            }
        },
    )
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = AppTypography.bodyMedium)
        Text(text = value, style = AppTypography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDatePicker(
    state: SavingsTransactionState,
    onAction: (SavingsTransactionAction) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.date,
        // A transaction cannot be recorded in the future: the money moves when it moves. The
        // original restricted this picker to `>= now`, so TODAY was not selectable — on the one
        // screen where today is almost always the answer.
        selectableDates = remember { NotInTheFuture },
    )

    DatePickerDialog(
        onDismissRequest = { onAction(SavingsTransactionAction.CloseDatePicker) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis
                        ?.let { onAction(SavingsTransactionAction.OnDateChanged(it)) }
                    onAction(SavingsTransactionAction.CloseDatePicker)
                },
            ) { Text(stringResource(Res.string.feature_savings_select_date)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(SavingsTransactionAction.CloseDatePicker) }) {
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

private fun AmountError.label() = when (this) {
    AmountError.MISSING -> Res.string.feature_savings_error_amount_required
    AmountError.NOT_A_NUMBER -> Res.string.feature_savings_error_amount_not_a_number
    AmountError.NOT_POSITIVE -> Res.string.feature_savings_error_amount_not_positive
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object SavingsTransactionTestTags {
    const val SCREEN = "savingsTransaction:screen"
    const val AMOUNT = "savingsTransaction:amount"
    const val DATE = "savingsTransaction:date"
    const val PAYMENT_TYPE = "savingsTransaction:paymentType"
    const val TEMPLATE_NOTICE = "savingsTransaction:templateNotice"
    const val NOTE = "savingsTransaction:note"
    const val REVIEW = "savingsTransaction:review"
    const val REVIEW_SHEET = "savingsTransaction:reviewSheet"
    const val CONFIRM = "savingsTransaction:confirm"
    const val STATUS_DIALOG = "savingsTransaction:statusDialog"
}
