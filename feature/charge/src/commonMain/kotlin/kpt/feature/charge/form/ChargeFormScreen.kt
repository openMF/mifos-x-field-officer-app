/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge.form

import androidx.compose.foundation.background
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
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppProgressIndicator
import kpt.core.ui.components.AppProgressIndicatorOverlay
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.charge.generated.resources.Res
import kpt.feature.charge.generated.resources.feature_charge_add
import kpt.feature.charge.generated.resources.feature_charge_add_charge
import kpt.feature.charge.generated.resources.feature_charge_amount
import kpt.feature.charge.generated.resources.feature_charge_amount_invalid
import kpt.feature.charge.generated.resources.feature_charge_calculation
import kpt.feature.charge.generated.resources.feature_charge_cancel
import kpt.feature.charge.generated.resources.feature_charge_choose_charge
import kpt.feature.charge.generated.resources.feature_charge_created_failure_title
import kpt.feature.charge.generated.resources.feature_charge_created_success_message
import kpt.feature.charge.generated.resources.feature_charge_created_success_title
import kpt.feature.charge.generated.resources.feature_charge_dialog_continue
import kpt.feature.charge.generated.resources.feature_charge_due_date
import kpt.feature.charge.generated.resources.feature_charge_edit_charge
import kpt.feature.charge.generated.resources.feature_charge_failed_generic
import kpt.feature.charge.generated.resources.feature_charge_failed_offline
import kpt.feature.charge.generated.resources.feature_charge_none_available
import kpt.feature.charge.generated.resources.feature_charge_required
import kpt.feature.charge.generated.resources.feature_charge_select
import kpt.feature.charge.generated.resources.feature_charge_time
import kpt.feature.charge.generated.resources.feature_charge_types_failed
import kpt.feature.charge.generated.resources.feature_charge_update
import kpt.feature.charge.generated.resources.feature_charge_updated_failure_title
import kpt.feature.charge.generated.resources.feature_charge_updated_success_message
import kpt.feature.charge.generated.resources.feature_charge_updated_success_title
import kpt.feature.charge.ui.TestTags
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from the original's `ChargeAddFields` (client) and `LoanChargeFormSheet` (loan): the
 * charge dropdown, the read-only charge-time and calculation fields it fills in, the amount, and a
 * due date shown only for a specified-due-date charge — then Add, or Update when editing.
 *
 * A screen, where both originals were panels inside their list (the client one above it, the loan
 * one in a bottom sheet the list owned). The list needs no callback to learn of the write: it lands
 * in the table the list observes.
 */
@Composable
internal fun ChargeFormScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChargeFormViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ChargeFormEvent.NavigateBack -> onBackPressed()
        }
    }

    ChargeFormContent(
        state = state,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ChargeFormContent(
    state: ChargeFormState,
    onAction: (ChargeFormAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(
            if (state.isEdit) Res.string.feature_charge_edit_charge else Res.string.feature_charge_add_charge,
        ),
        onBackPressed = { onAction(ChargeFormAction.NavigateBack) },
        modifier = modifier.testTag(TestTags.ChargeForm.SCREEN),
    ) { padding ->
        when {
            state.isLoadingTypes -> AppProgressIndicator(
                modifier = Modifier.fillMaxSize().padding(padding).testTag(TestTags.ChargeForm.LOADING),
            )

            state.typesError -> AppSweetError(
                message = stringResource(Res.string.feature_charge_types_failed),
                onclick = { onAction(ChargeFormAction.OnRetry) },
                modifier = Modifier.fillMaxSize().padding(padding).testTag(TestTags.ChargeForm.TYPES_ERROR),
            )

            // The tenant allows no chargeable type this form can handle. Stated rather than left as
            // an empty dropdown over a live submit button.
            !state.isEdit && state.chargeTypes.isEmpty() -> Text(
                text = stringResource(Res.string.feature_charge_none_available),
                style = AppTypography.bodyMedium,
                modifier = Modifier.fillMaxSize().padding(padding).padding(KptTheme.spacing.md)
                    .testTag(TestTags.ChargeForm.NO_TYPES),
            )

            else -> ChargeForm(state = state, onAction = onAction, modifier = Modifier.fillMaxSize().padding(padding))
        }
    }

    // The original's `MifosProgressIndicatorOverlay` while the write is in flight.
    if (state.isSubmitting) {
        AppProgressIndicatorOverlay(Modifier.fillMaxSize().testTag(TestTags.ChargeForm.SUBMITTING))
    }

    state.dialogState?.let { dialog -> StatusDialog(dialog, state.isEdit, onAction) }
}

@Composable
private fun StatusDialog(
    dialog: ChargeFormState.DialogState,
    isEdit: Boolean,
    onAction: (ChargeFormAction) -> Unit,
) {
    val failed = dialog as? ChargeFormState.DialogState.Failed
    val closeAction = if (failed == null) ChargeFormAction.NavigateBack else ChargeFormAction.CloseDialog
    AppStatusDialog(
        status = if (failed == null) ResultStatus.SUCCESS else ResultStatus.FAILURE,
        btnText = stringResource(Res.string.feature_charge_dialog_continue),
        // Success goes back to the list, which already shows the change; failure keeps the form.
        onConfirm = { onAction(closeAction) },
        onDismissRequest = { onAction(closeAction) },
        successTitle = stringResource(
            if (isEdit) {
                Res.string.feature_charge_updated_success_title
            } else {
                Res.string.feature_charge_created_success_title
            },
        ),
        successMessage = stringResource(
            if (isEdit) {
                Res.string.feature_charge_updated_success_message
            } else {
                Res.string.feature_charge_created_success_message
            },
        ),
        failureTitle = stringResource(
            if (isEdit) {
                Res.string.feature_charge_updated_failure_title
            } else {
                Res.string.feature_charge_created_failure_title
            },
        ),
        failureMessage = when {
            failed == null -> ""
            failed.offline -> stringResource(Res.string.feature_charge_failed_offline)
            else -> failed.serverMessage ?: stringResource(Res.string.feature_charge_failed_generic)
        },
        // OPAQUE, as the original's `MifosStatusDialog` was (`.background(surface)`). The status
        // screen is a full-size layer, not a dialog window — without its own background the form
        // behind showed through every line of text (device, 2026-10-23).
        modifier = Modifier.fillMaxSize().background(KptTheme.colorScheme.surface)
            .testTag(TestTags.ChargeForm.STATUS_DIALOG),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChargeForm(
    state: ChargeFormState,
    onAction: (ChargeFormAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = KptTheme.spacing.md)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(KptTheme.spacing.md))

        AppTextFieldDropdown(
            value = state.selectedCharge?.name.orEmpty(),
            // The chosen CHARGE is dispatched, not its index.
            onOptionSelected = { index, _ ->
                state.chargeTypes.getOrNull(index)?.let { onAction(ChargeFormAction.OnChargeSelected(it)) }
            },
            // Editing cannot change WHICH charge it is — Fineract's update takes amount + date only.
            options = if (state.isEdit) emptyList() else state.chargeTypes.map { it.name },
            label = stringResource(Res.string.feature_charge_choose_charge),
            readOnly = true,
            errorMessage = if (state.chargeError) stringResource(Res.string.feature_charge_required) else null,
            modifier = Modifier.fillMaxWidth().testTag(TestTags.ChargeForm.CHARGE_TYPE),
        )

        // The two fields the chosen charge DETERMINES — read-only, so the officer sees what the
        // charge will do before adding it.
        state.selectedCharge?.let { charge ->
            Spacer(Modifier.height(KptTheme.spacing.md))
            AppOutlinedTextField(
                value = charge.time,
                onValueChange = {},
                label = stringResource(Res.string.feature_charge_time),
                readOnly = true,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.ChargeForm.CHARGE_TIME),
            )

            Spacer(Modifier.height(KptTheme.spacing.md))
            AppOutlinedTextField(
                value = charge.calculation,
                onValueChange = {},
                label = stringResource(Res.string.feature_charge_calculation),
                readOnly = true,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.ChargeForm.CHARGE_CALCULATION),
            )
        }

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppOutlinedTextField(
            value = state.amount,
            onValueChange = { onAction(ChargeFormAction.OnAmountChanged(it)) },
            label = stringResource(Res.string.feature_charge_amount),
            keyboardType = KeyboardType.Number,
            isError = state.amountError,
            errorText = stringResource(Res.string.feature_charge_amount_invalid).takeIf { state.amountError },
            modifier = Modifier.fillMaxWidth().testTag(TestTags.ChargeForm.AMOUNT),
        )

        if (state.needsDueDate) {
            Spacer(Modifier.height(KptTheme.spacing.md))

            AppDatePickerTextField(
                value = DateHelper.getDateAsStringFromLong(state.dueDate),
                label = stringResource(Res.string.feature_charge_due_date),
                openDatePicker = { onAction(ChargeFormAction.UpdateDatePicker(true)) },
                modifier = Modifier.fillMaxWidth().testTag(TestTags.ChargeForm.DUE_DATE),
            )

            if (state.showDatePicker) {
                val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.dueDate)
                DatePickerDialog(
                    onDismissRequest = { onAction(ChargeFormAction.UpdateDatePicker(false)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                pickerState.selectedDateMillis?.let { onAction(ChargeFormAction.OnDueDateChanged(it)) }
                                onAction(ChargeFormAction.UpdateDatePicker(false))
                            },
                        ) { Text(stringResource(Res.string.feature_charge_select)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { onAction(ChargeFormAction.UpdateDatePicker(false)) }) {
                            Text(stringResource(Res.string.feature_charge_cancel))
                        }
                    },
                ) {
                    // Unrestricted: a charge's due date is when it falls due, normally in the FUTURE.
                    DatePicker(state = pickerState)
                }
            }
        }

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppButton(
            onClick = { onAction(ChargeFormAction.OnSubmit) },
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(DesignToken.spacing.dp44)
                .testTag(TestTags.ChargeForm.SUBMIT),
        ) {
            Text(
                text = stringResource(
                    if (state.isEdit) Res.string.feature_charge_update else Res.string.feature_charge_add,
                ),
            )
        }

        Spacer(Modifier.height(KptTheme.spacing.md))
    }
}
