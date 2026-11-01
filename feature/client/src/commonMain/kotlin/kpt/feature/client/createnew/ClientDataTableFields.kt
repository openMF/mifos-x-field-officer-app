/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.createnew

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.datatable.ColumnHeader
import kpt.core.model.datatable.DataTable
import kpt.core.ui.components.AppCheckBox
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_createnew_datatable_cancel
import kpt.feature.client.generated.resources.feature_client_createnew_datatable_not_a_number
import kpt.feature.client.generated.resources.feature_client_createnew_datatable_required
import kpt.feature.client.generated.resources.feature_client_createnew_datatable_select
import org.jetbrains.compose.resources.stringResource

/**
 * One section per client datatable, one field per column the officer fills in — see
 * ClientDataTables.kt for which columns, how each type is encoded, and what the original got wrong.
 */
@Composable
internal fun DataTableSections(
    tables: List<DataTable>,
    state: CreateNewClientState,
    onAction: (CreateNewClientAction) -> Unit,
) {
    for (table in tables) {
        val name = table.registeredTableName ?: continue
        HorizontalDivider(modifier = Modifier.padding(vertical = KptTheme.spacing.md))
        Text(
            text = name.humanize(),
            style = AppTypography.titleMedium,
            modifier = Modifier.testTag(CreateNewClientTestTags.dataTable(name)),
        )
        for (column in table.entryColumns()) {
            val columnName = column.dataTableColumnName ?: continue
            val field = DataTableField(name, columnName)
            Spacer(Modifier.height(KptTheme.spacing.sm))
            DataTableFieldInput(
                column = column,
                value = state.dataTableValues[field].orEmpty(),
                error = state.dataTableErrors[field],
                onChange = { onAction(CreateNewClientAction.DataTableValueChanged(field, it)) },
                modifier = Modifier.fillMaxWidth().testTag(CreateNewClientTestTags.dataTableField(name, columnName)),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataTableFieldInput(
    column: ColumnHeader,
    value: String,
    error: DataTableFieldError?,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // No "*" on a checkbox: it cannot be missing (unchecked is false), so "required" would mislead.
    val required = column.isRequired && column.columnDisplayType != ColumnType.BOOLEAN
    val label = column.dataTableColumnName.orEmpty().humanize() + if (required) " *" else ""
    val errorText = when (error) {
        DataTableFieldError.REQUIRED -> stringResource(Res.string.feature_client_createnew_datatable_required)
        DataTableFieldError.NOT_A_NUMBER -> stringResource(Res.string.feature_client_createnew_datatable_not_a_number)
        null -> null
    }
    when (column.columnDisplayType) {
        ColumnType.BOOLEAN -> AppCheckBox(
            text = label,
            checked = value == "true",
            onCheckChanged = { onChange(it.toString()) },
            modifier = modifier,
        )

        ColumnType.CODELOOKUP, ColumnType.CODEVALUE -> AppTextFieldDropdown(
            value = column.columnValues.firstOrNull { it.id?.toString() == value }?.value.orEmpty(),
            // The code value's ID is what Fineract stores — the label is only for the officer.
            onOptionSelected = { index, _ ->
                column.columnValues.getOrNull(index)?.id?.let { onChange(it.toString()) }
            },
            options = column.columnValues.map { it.value.orEmpty() },
            label = label,
            errorMessage = errorText,
            modifier = modifier,
        )

        ColumnType.DATE -> DataTableDateInput(value, label, errorText, onChange, modifier)

        else -> AppOutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = label,
            keyboardType = keyboardTypeFor(column.columnDisplayType),
            singleLine = column.columnDisplayType != ColumnType.TEXT,
            maxLines = if (column.columnDisplayType == ColumnType.TEXT) TEXT_MAX_LINES else 1,
            isError = error != null,
            errorText = errorText,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataTableDateInput(
    value: String,
    label: String,
    errorText: String?,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    AppDatePickerTextField(
        value = value.toLongOrNull()?.let(DateHelper::getDateAsStringFromLong).orEmpty(),
        label = label,
        errorMessage = errorText,
        openDatePicker = { open = true },
        modifier = modifier,
    )
    if (open) {
        // Any date: a datatable column is the tenant's own data (a birth date, an expiry), so
        // there is no "not in the future" rule to impose — the original's future-only was wrong too.
        val picker = rememberDatePickerState(initialSelectedDateMillis = value.toLongOrNull())
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let { onChange(it.toString()) }
                    open = false
                }) { Text(stringResource(Res.string.feature_client_createnew_datatable_select)) }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) {
                    Text(stringResource(Res.string.feature_client_createnew_datatable_cancel))
                }
            },
        ) { DatePicker(state = picker) }
    }
}

private fun keyboardTypeFor(type: String?): KeyboardType = when (type) {
    ColumnType.INTEGER -> KeyboardType.Number
    ColumnType.DECIMAL -> KeyboardType.Decimal
    else -> KeyboardType.Text
}

/** `loan_officer_notes` → `Loan officer notes`: datatable and column names are snake_case identifiers. */
private fun String.humanize(): String =
    replace('_', ' ').trim().replaceFirstChar { it.uppercase() }

private const val TEXT_MAX_LINES = 4
