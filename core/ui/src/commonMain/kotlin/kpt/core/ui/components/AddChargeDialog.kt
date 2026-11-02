/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import kpt.core.designsystem.fieldofficer.component.AppBottomSheet
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppTextFieldConfig
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.amount
import kpt.core.ui.generated.resources.cancel
import kpt.core.ui.generated.resources.collected_on
import kpt.core.ui.generated.resources.date
import kpt.core.ui.generated.resources.name
import kpt.core.ui.generated.resources.ok
import kpt.core.ui.generated.resources.type
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddChargeBottomSheet(
    title: String,
    confirmText: String,
    dismissText: String,
    selectedChargeName: String,
    chargeAmount: String,
    chargeType: String,
    chargeCollectedOn: String,
    chargeOptions: List<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onChargeSelected: (Int, String) -> Unit,
    onDatePick: (Boolean) -> Unit,
    onDateChange: (Long) -> Unit,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showDatePicker: Boolean = false,
    selectedDate: String? = null,
    amountError: String? = null,
) {
    AppBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
        content = {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds(),
            )

            Column(
                Modifier.padding(DesignToken.padding.large),
            ) {
                Text(text = title, style = AppTypography.titleMediumEmphasized)

                Spacer(Modifier.height(DesignToken.padding.large))
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { onDatePick(false) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    onDatePick(false)
                                    datePickerState.selectedDateMillis?.let {
                                        onDateChange(it)
                                    }
                                },
                            ) { Text(stringResource(Res.string.ok)) }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { onDatePick(false) },
                            ) { Text(stringResource(Res.string.cancel)) }
                        },
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                AppTextFieldDropdown(
                    value = selectedChargeName,
                    onOptionSelected = onChargeSelected,
                    options = chargeOptions,
                    label = stringResource(Res.string.name),
                )

                if (selectedDate != null) {
                    AppDatePickerTextField(
                        value = selectedDate,
                        label = stringResource(Res.string.date),
                        openDatePicker = { onDatePick(true) },
                    )

                    Spacer(Modifier.height(DesignToken.padding.large))
                }

                AppOutlinedTextField(
                    value = chargeAmount,
                    onValueChange = onAmountChange,
                    label = stringResource(Res.string.amount),
                    config = AppTextFieldConfig(
                        isError = amountError != null,
                        errorText = amountError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                        ),
                    ),
                )

                Spacer(Modifier.height(DesignToken.padding.large))

                AppOutlinedTextField(
                    value = chargeType,
                    onValueChange = {},
                    label = stringResource(Res.string.type),
                    config = AppTextFieldConfig(
                        readOnly = true,
                        enabled = false,
                    ),
                )

                Spacer(Modifier.height(DesignToken.padding.large))

                AppOutlinedTextField(
                    value = chargeCollectedOn,
                    onValueChange = {},
                    label = stringResource(Res.string.collected_on),
                    config = AppTextFieldConfig(
                        readOnly = true,
                        enabled = false,
                    ),
                )

                Spacer(Modifier.height(DesignToken.padding.large))

                AppTwoButtonRow(
                    firstBtnText = dismissText,
                    secondBtnText = confirmText,
                    onFirstBtnClick = onDismiss,
                    onSecondBtnClick = onConfirm,
                    isSecondButtonEnabled = chargeAmount.isNotEmpty() && amountError.isNullOrEmpty(),
                )
            }
        },
    )
}

@Preview
@Composable
private fun AddChargeBottomSheetPreview() {
    val sampleChargeOptions = listOf("Bank Fee", "Overdue Fee", "Processing Fee")

    AppTheme {
        AddChargeBottomSheet(
            title = "Add Charge",
            confirmText = "Confirm",
            dismissText = "Cancel",
            showDatePicker = false,
            selectedChargeName = sampleChargeOptions[0],
            selectedDate = "2025-09-06",
            chargeAmount = "1500",
            chargeType = "Flat",
            chargeCollectedOn = "2025-09-06",
            chargeOptions = sampleChargeOptions,
            onConfirm = { },
            onDismiss = { },
            onChargeSelected = { _, _ -> },
            onDatePick = { },
            onDateChange = { },
            amountError = null,
            onAmountChange = {},
        )
    }
}
