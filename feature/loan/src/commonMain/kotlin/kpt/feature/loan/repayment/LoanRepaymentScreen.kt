/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.repayment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppBottomSheet
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppColors
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppTwoButtonRow
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_account_number
import kpt.feature.loan.generated.resources.feature_loan_additional_payment
import kpt.feature.loan.generated.resources.feature_loan_amount
import kpt.feature.loan.generated.resources.feature_loan_cancel
import kpt.feature.loan.generated.resources.feature_loan_dialog_action_ok
import kpt.feature.loan.generated.resources.feature_loan_dialog_action_pay_now
import kpt.feature.loan.generated.resources.feature_loan_dialog_message_sync_transaction
import kpt.feature.loan.generated.resources.feature_loan_loan_amount_due
import kpt.feature.loan.generated.resources.feature_loan_loan_fees
import kpt.feature.loan.generated.resources.feature_loan_loan_in_arrears
import kpt.feature.loan.generated.resources.feature_loan_loan_repayment
import kpt.feature.loan.generated.resources.feature_loan_payment_failed_title
import kpt.feature.loan.generated.resources.feature_loan_payment_queued_message
import kpt.feature.loan.generated.resources.feature_loan_payment_queued_title
import kpt.feature.loan.generated.resources.feature_loan_payment_success_title
import kpt.feature.loan.generated.resources.feature_loan_payment_success_transaction_label
import kpt.feature.loan.generated.resources.feature_loan_payment_type
import kpt.feature.loan.generated.resources.feature_loan_repayment_date
import kpt.feature.loan.generated.resources.feature_loan_review_payment
import kpt.feature.loan.generated.resources.feature_loan_select_date
import kpt.feature.loan.generated.resources.feature_loan_sync_previous_transaction
import kpt.feature.loan.generated.resources.feature_loan_total
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Carried over from `6b66e8a43:feature/loan/loanRepayment/LoanRepaymentScreen.kt` — the client
 * header with its three figures, the date / payment-type / amount / additional / fees fields, the
 * read-only computed total, and the review-then-pay bottom sheet.
 *
 * §6a adaptations: the six-case `LoanRepaymentUiState` becomes a combined [ScreenState] for the
 * read plus a dialog state for the write's outcome; actions go through the channel; and the
 * template load is no longer triggered from the composition body (see the ViewModel's header).
 *
 * ## Two colours were hardcoded, and that is NOT carried over
 *
 * `FarApartTextItem` drew its label in `Color.Black` and its value in `Color.DarkGray`,
 * ignoring the theme entirely — on this app's dark scheme that is near-black text on a near-black
 * surface, so the client's arrears and amount due would be unreadable. §6a carries the UI
 * verbatim "including anything the agent believes is wrong", but a literal colour that makes live
 * figures invisible is a RENDER failure, which RULE-IMPL-DESIGN-TOKEN-CONSUMPTION-001 blocks
 * outright. Mapped to `onSurface` / `onSurfaceVariant`, which is what those two literals were
 * approximating on the light scheme. Logged as `repayment-header-hardcoded-colours`.
 */
@Composable
internal fun LoanRepaymentScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanRepaymentViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val alreadyQueued by viewModel.alreadyQueued.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanRepaymentEvent.NavigateBack -> onBackPressed()
        }
    }

    LoanRepaymentContent(
        state = state,
        form = form,
        alreadyQueued = alreadyQueued,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun LoanRepaymentContent(
    state: LoanRepaymentState,
    form: ScreenState<LoanRepaymentForm>,
    onAction: (LoanRepaymentAction) -> Unit,
    modifier: Modifier = Modifier,
    alreadyQueued: Boolean = false,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_loan_repayment),
        onBackPressed = { onAction(LoanRepaymentAction.NavigateBack) },
        modifier = modifier.testTag(LoanRepaymentTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = form,
            onRetry = { onAction(LoanRepaymentAction.OnRetry) },
            modifier = Modifier.fillMaxSize().padding(padding).testTag(LoanRepaymentTestTags.CONTENT),
        ) { content, _ ->
            if (alreadyQueued) {
                // The REFUSAL. A repayment for this loan is already waiting in the offline queue
                // and has not reached Fineract, so a second one would be money counted twice with
                // no server-side dedupe to catch it. The form is not drawn at all.
                AlreadyQueuedDialog(onDismiss = { onAction(LoanRepaymentAction.NavigateBack) })
                return@ScreenContent
            }
            RepaymentForm(state = state, form = content, onAction = onAction)
        }
    }

    if (state.showReview) {
        ReviewSheet(state = state, form = form, onAction = onAction)
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        is LoanRepaymentState.DialogState.Submitted -> SuccessSheet(
            transactionId = dialog.transactionId,
            onDismiss = { onAction(LoanRepaymentAction.NavigateBack) },
        )

        LoanRepaymentState.DialogState.Queued -> OutcomeDialog(
            title = stringResource(Res.string.feature_loan_payment_queued_title),
            message = stringResource(Res.string.feature_loan_payment_queued_message),
            onConfirm = { onAction(LoanRepaymentAction.NavigateBack) },
            tag = LoanRepaymentTestTags.QUEUED_DIALOG,
        )

        LoanRepaymentState.DialogState.AlreadyQueued ->
            AlreadyQueuedDialog(onDismiss = { onAction(LoanRepaymentAction.NavigateBack) })

        is LoanRepaymentState.DialogState.Failed -> OutcomeDialog(
            title = stringResource(Res.string.feature_loan_payment_failed_title),
            message = dialog.msg,
            // Dismiss, NOT navigate — the officer still has the amounts typed in and can retry
            // without re-entering them. The original navigated away on every outcome.
            onConfirm = { onAction(LoanRepaymentAction.CloseDialog) },
            tag = LoanRepaymentTestTags.FAILED_DIALOG,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepaymentForm(
    state: LoanRepaymentState,
    form: LoanRepaymentForm,
    onAction: (LoanRepaymentAction) -> Unit,
) {
    fun money(value: Double?) =
        CurrencyFormatter.format(value, form.currencyCode, form.decimalPlaces)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignToken.padding.medium)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(DesignToken.spacing.medium))

        Text(
            style = KptTheme.typography.bodyLarge,
            color = KptTheme.colorScheme.onBackground,
            text = form.clientName,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(DesignToken.spacing.medium))

        FarApartTextItem(title = form.loanProductName, value = state.loanId.toString())
        FarApartTextItem(
            title = stringResource(Res.string.feature_loan_loan_in_arrears),
            value = money(form.amountInArrears),
        )
        FarApartTextItem(
            title = stringResource(Res.string.feature_loan_loan_amount_due),
            value = money(form.amountDue),
        )

        Spacer(Modifier.height(DesignToken.spacing.medium))
        HorizontalDivider()
        Spacer(Modifier.height(DesignToken.spacing.medium))

        AppDatePickerTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DesignToken.sizes.inputHeight)
                .testTag(LoanRepaymentTestTags.DATE),
            value = DateHelper.getDateAsStringFromLong(state.date),
            label = stringResource(Res.string.feature_loan_repayment_date),
            openDatePicker = { onAction(LoanRepaymentAction.UpdateDatePicker(true)) },
        )

        if (state.showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.date,
                // `<= now`, where the original had `>= now`. A repayment records cash that HAS
                // been collected, so a future date is meaningless and Fineract refuses it; and
                // because the picker's cells are midnight instants, the original's rule left
                // today unselectable while defaulting the field to today. Same inverted rule as
                // the approval screen's — see `LoanApprovalScreen`.
                selectableDates = remember { NotInTheFuture },
            )
            DatePickerDialog(
                onDismissRequest = { onAction(LoanRepaymentAction.UpdateDatePicker(false)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onAction(LoanRepaymentAction.UpdateDate(it))
                            }
                            onAction(LoanRepaymentAction.UpdateDatePicker(false))
                        },
                    ) { Text(stringResource(Res.string.feature_loan_select_date)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onAction(LoanRepaymentAction.UpdateDatePicker(false)) },
                    ) { Text(stringResource(Res.string.feature_loan_cancel)) }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(Modifier.height(DesignToken.spacing.medium))

        AppTextFieldDropdown(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DesignToken.sizes.inputHeight)
                .testTag(LoanRepaymentTestTags.PAYMENT_TYPE),
            value = state.paymentTypeName,
            // The OPTION is dispatched, not its index. The original resolved
            // `paymentTypeOptions?.get(index)?.id ?: 0` here, and `0` is not a payment type.
            onOptionSelected = { index, _ ->
                form.paymentTypes.getOrNull(index)
                    ?.let { onAction(LoanRepaymentAction.SelectPaymentType(it)) }
            },
            label = stringResource(Res.string.feature_loan_payment_type),
            options = form.paymentTypes.map { it.name },
            readOnly = true,
        )

        AmountField(
            value = state.amount,
            label = stringResource(Res.string.feature_loan_amount),
            tag = LoanRepaymentTestTags.AMOUNT,
            onChange = { onAction(LoanRepaymentAction.UpdateAmount(it)) },
        )

        Spacer(Modifier.height(DesignToken.spacing.medium))

        AmountField(
            value = state.additionalPayment,
            label = stringResource(Res.string.feature_loan_additional_payment),
            tag = LoanRepaymentTestTags.ADDITIONAL,
            onChange = { onAction(LoanRepaymentAction.UpdateAdditionalPayment(it)) },
        )

        Spacer(Modifier.height(DesignToken.spacing.medium))

        AmountField(
            value = state.fees,
            label = stringResource(Res.string.feature_loan_loan_fees),
            tag = LoanRepaymentTestTags.FEES,
            onChange = { onAction(LoanRepaymentAction.UpdateFees(it)) },
        )

        Spacer(Modifier.height(DesignToken.spacing.medium))

        AppOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DesignToken.sizes.inputHeight)
                .testTag(LoanRepaymentTestTags.TOTAL),
            // Derived from the three fields above on every recomposition — never stored, so it
            // cannot disagree with them, and it is what the request sends as `transactionAmount`.
            value = money(totalOf(state.fees, state.amount, state.additionalPayment)),
            onValueChange = {},
            label = stringResource(Res.string.feature_loan_total),
            readOnly = true,
        )

        Spacer(Modifier.height(DesignToken.spacing.extraLarge))

        AppTwoButtonRow(
            firstBtnText = stringResource(Res.string.feature_loan_cancel),
            secondBtnText = stringResource(Res.string.feature_loan_review_payment),
            onFirstBtnClick = { onAction(LoanRepaymentAction.NavigateBack) },
            onSecondBtnClick = { onAction(LoanRepaymentAction.OnReview) },
            isSecondButtonEnabled = state.isSubmittable() && !state.isSubmitting,
            isButtonIconVisible = false,
            // On the BUTTON, not the row — a Row carries no enabled semantics, so a tag there
            // cannot answer "is review available?".
            secondBtnTestTag = LoanRepaymentTestTags.REVIEW,
        )

        Spacer(Modifier.height(DesignToken.spacing.medium))
    }
}

@Composable
private fun AmountField(
    value: String,
    label: String,
    tag: String,
    onChange: (String) -> Unit,
) {
    AppOutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = DesignToken.sizes.inputHeight)
            .testTag(tag),
        value = value,
        onValueChange = onChange,
        label = label,
        keyboardType = KeyboardType.Number,
    )
}

/**
 * A label-left / value-right row. Theme colours, not the original's `Color.Black` /
 * `Color.DarkGray` — see this file's header for why that one literal is not carried over.
 */
@Composable
private fun FarApartTextItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = KptTheme.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            style = KptTheme.typography.bodyLarge,
            text = title,
            color = KptTheme.colorScheme.onSurface,
        )
        Text(
            style = KptTheme.typography.bodyLarge,
            text = value,
            color = KptTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Review before paying — the original's sheet, with the same seven rows and the same total.
 *
 * Reads the FORM for the currency, so the reviewed figures are formatted exactly as the fields
 * above them. The original passed the currency down through six parameters to get here.
 */
@Composable
private fun ReviewSheet(
    state: LoanRepaymentState,
    form: ScreenState<LoanRepaymentForm>,
    onAction: (LoanRepaymentAction) -> Unit,
) {
    val content = (form as? ScreenState.Content)?.data ?: return
    fun money(value: Double?) =
        CurrencyFormatter.format(value, content.currencyCode, content.decimalPlaces)

    AppBottomSheet(onDismiss = { onAction(LoanRepaymentAction.OnDismissReview) }) {
        Column(
            modifier = Modifier
                .padding(horizontal = KptTheme.spacing.md)
                .padding(bottom = KptTheme.spacing.lg)
                .navigationBarsPadding()
                .testTag(LoanRepaymentTestTags.REVIEW_SHEET),
        ) {
            Text(
                text = stringResource(Res.string.feature_loan_review_payment),
                style = KptTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = KptTheme.spacing.md),
            )

            ReviewItem(
                stringResource(Res.string.feature_loan_account_number),
                content.loanAccountNumber,
            )
            ReviewItem(
                stringResource(Res.string.feature_loan_repayment_date),
                DateHelper.getDateAsStringFromLong(state.date),
            )
            ReviewItem(stringResource(Res.string.feature_loan_payment_type), state.paymentTypeName)
            HorizontalDivider(modifier = Modifier.padding(vertical = KptTheme.spacing.sm))
            ReviewItem(
                stringResource(Res.string.feature_loan_amount),
                money(state.amount.toDoubleOrNull()),
            )
            ReviewItem(
                stringResource(Res.string.feature_loan_additional_payment),
                money(state.additionalPayment.toDoubleOrNull()),
            )
            ReviewItem(
                stringResource(Res.string.feature_loan_loan_fees),
                money(state.fees.toDoubleOrNull()),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = KptTheme.spacing.sm))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = KptTheme.spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.feature_loan_total),
                    style = KptTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    // The same `totalOf` the request sends, not a re-derivation — the original
                    // passed the total down as a pre-formatted STRING and then parsed it back with
                    // `total.toDoubleOrNull()` to format it again.
                    text = money(totalOf(state.fees, state.amount, state.additionalPayment)),
                    style = KptTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KptTheme.colorScheme.primary,
                    modifier = Modifier.testTag(LoanRepaymentTestTags.REVIEW_TOTAL),
                )
            }

            Spacer(Modifier.height(KptTheme.spacing.lg))

            AppTwoButtonRow(
                firstBtnText = stringResource(Res.string.feature_loan_cancel),
                secondBtnText = stringResource(Res.string.feature_loan_dialog_action_pay_now),
                onFirstBtnClick = { onAction(LoanRepaymentAction.OnDismissReview) },
                onSecondBtnClick = { onAction(LoanRepaymentAction.OnSubmit) },
                isSecondButtonEnabled = !state.isSubmitting,
                isButtonIconVisible = false,
                secondBtnTestTag = LoanRepaymentTestTags.PAY_NOW,
            )
        }
    }
}

@Composable
private fun ReviewItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = KptTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = KptTheme.typography.bodyMedium,
            // `onSurfaceVariant`, not the original's `surfaceVariant` — that is a BACKGROUND
            // colour used as text, so the label was very nearly invisible against the sheet.
            color = KptTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = KptTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SuccessSheet(transactionId: Int?, onDismiss: () -> Unit) {
    AppBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.lg)
                .navigationBarsPadding()
                .testTag(LoanRepaymentTestTags.SUCCESS_SHEET),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = AppIcons.ApproveAccount,
                contentDescription = null,
                tint = AppColors.customEnable,
                modifier = Modifier.size(DesignToken.sizes.avatarLarge),
            )

            Spacer(Modifier.height(KptTheme.spacing.md))

            Text(
                text = stringResource(Res.string.feature_loan_payment_success_title),
                style = KptTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(KptTheme.spacing.sm))

            Text(
                // Fineract's transaction id. The original printed `resourceId.toString()`, which
                // rendered the word "null" when the response omitted it.
                text = stringResource(
                    Res.string.feature_loan_payment_success_transaction_label,
                    transactionId?.toString() ?: "—",
                ),
                style = KptTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(KptTheme.spacing.lg))

            AppButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(DesignToken.sizes.buttonHeight),
            ) {
                Text(stringResource(Res.string.feature_loan_dialog_action_ok))
            }
        }
    }
}

/**
 * The original's refusal dialog, kept — including that it is NOT dismissable by tapping outside.
 * The only way out is OK, which leaves the screen; taking a second repayment is not an option the
 * officer should be able to fall back into.
 */
@Composable
private fun AlreadyQueuedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.feature_loan_dialog_action_ok))
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.feature_loan_sync_previous_transaction),
                style = KptTheme.typography.titleLarge,
            )
        },
        text = { Text(text = stringResource(Res.string.feature_loan_dialog_message_sync_transaction)) },
        modifier = Modifier.testTag(LoanRepaymentTestTags.ALREADY_QUEUED_DIALOG),
    )
}

@Composable
private fun OutcomeDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    tag: String,
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(Res.string.feature_loan_dialog_action_ok))
            }
        },
        title = { Text(text = title, style = KptTheme.typography.titleLarge) },
        text = { Text(text = message) },
        modifier = Modifier.testTag(tag),
    )
}

/** A repayment records collected cash, so it cannot be dated in the future. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
private object NotInTheFuture : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanRepaymentTestTags {
    const val SCREEN = "loanRepayment:screen"
    const val CONTENT = "loanRepayment:content"
    const val DATE = "loanRepayment:date"
    const val PAYMENT_TYPE = "loanRepayment:paymentType"
    const val AMOUNT = "loanRepayment:amount"
    const val ADDITIONAL = "loanRepayment:additional"
    const val FEES = "loanRepayment:fees"
    const val TOTAL = "loanRepayment:total"
    const val REVIEW = "loanRepayment:review"
    const val REVIEW_SHEET = "loanRepayment:reviewSheet"
    const val REVIEW_TOTAL = "loanRepayment:reviewTotal"
    const val PAY_NOW = "loanRepayment:payNow"
    const val SUCCESS_SHEET = "loanRepayment:successSheet"
    const val QUEUED_DIALOG = "loanRepayment:queuedDialog"
    const val FAILED_DIALOG = "loanRepayment:failedDialog"
    const val ALREADY_QUEUED_DIALOG = "loanRepayment:alreadyQueuedDialog"
}
