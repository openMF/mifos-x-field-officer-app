/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.feature.standingInstruction.viewStandingInstructions

import androidclient.feature.standing_instruction.generated.resources.Res
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_amount
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_cancel
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_confirm
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_delete
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_delete_confirm_message
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_delete_instruction
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_edit
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_edit_instruction
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_no_standing_instructions_found_message
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_ok
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_standing_instructions
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_table_header_amount
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_table_header_beneficiary
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_table_header_client
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_table_header_client_id
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_table_header_from_account
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_table_header_to_account
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_table_header_validity
import androidclient.feature.standing_instruction.generated.resources.feature_standing_instructions_valid_from
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.core.common.utils.ApiDateFormatter
import com.mifos.core.common.utils.DataState
import com.mifos.core.common.utils.DateFormatPattern
import com.mifos.core.designsystem.component.BasicDialogState
import com.mifos.core.designsystem.component.MifosBasicDialog
import com.mifos.core.designsystem.component.MifosDatePickerTextField
import com.mifos.core.designsystem.component.MifosMenuDropDownItem
import com.mifos.core.designsystem.component.MifosOutlinedTextField
import com.mifos.core.designsystem.component.MifosScaffold
import com.mifos.core.designsystem.component.MifosSweetError
import com.mifos.core.designsystem.component.MifosTableRow
import com.mifos.core.designsystem.component.MifosTextFieldConfig
import com.mifos.core.designsystem.icon.MifosIcons
import com.mifos.core.designsystem.theme.DesignToken
import com.mifos.core.designsystem.theme.MifosTypography
import com.mifos.core.ui.components.MifosEmptyCard
import com.mifos.core.ui.components.MifosProgressIndicator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
internal fun ViewStandingInstructionsScreen(
    viewModel: ViewStandingInstructionsViewModel = koinViewModel(),
    navigateBack: () -> Unit,
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                ViewStandingInstructionsEvent.NavigateBack -> navigateBack()
            }
        }
    }

    ViewStandingInstructionsContent(
        state = state,
        onAction = viewModel::trySendAction,
    )

    ViewStandingInstructionsDialogs(
        dialogState = state.dialogState,
        onAction = viewModel::trySendAction,
    )
}

@Composable
internal fun ViewStandingInstructionsContent(
    state: ViewStandingInstructionsState,
    onAction: (ViewStandingInstructionsAction) -> Unit,
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    MifosScaffold(
        title = stringResource(Res.string.feature_standing_instructions_standing_instructions),
        snackbarHostState = snackbarHostState,
        onBackPressed = { onAction(ViewStandingInstructionsAction.OnNavigateBack) },
        actions = {
            IconButton(onClick = { onAction(ViewStandingInstructionsAction.Retry) }) {
                Icon(
                    imageVector = MifosIcons.Refresh,
                    contentDescription = null,
                )
            }
        },
    ) {
        Box(modifier = Modifier.padding(it)) {
            when (state.dataState) {
                is DataState.Error -> {
                    MifosSweetError(
                        message = state.dataState.message,
                        onclick = { onAction(ViewStandingInstructionsAction.Retry) },
                    )
                }

                is DataState.Success -> {
                    state.tableData?.let { data ->
                        ViewStandingInstructionsData(
                            tableData = data,
                            dialogState = state.dialogState,
                            onAction = onAction,
                        )
                    }
                }

                DataState.Loading -> {
                    MifosProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ViewStandingInstructionsDialogs(
    dialogState: ViewStandingInstructionsState.DialogState?,
    onAction: (ViewStandingInstructionsAction) -> Unit,
) {
    when (dialogState) {
        is ViewStandingInstructionsState.DialogState.Error -> {
            MifosBasicDialog(
                visibilityState = BasicDialogState.Shown(
                    message = dialogState.message,
                    title = dialogState.title,
                ),
                onDismissRequest = { onAction(ViewStandingInstructionsAction.DismissErrorDialog) },
            )
        }

        is ViewStandingInstructionsState.DialogState.Options -> Unit

        is ViewStandingInstructionsState.DialogState.ConfirmDelete -> {
            MifosBasicDialog(
                visibilityState = BasicDialogState.Shown(
                    title = stringResource(Res.string.feature_standing_instructions_delete_instruction),
                    message = stringResource(Res.string.feature_standing_instructions_delete_confirm_message),
                ),
                onConfirm = { onAction(ViewStandingInstructionsAction.OnConfirmDelete(dialogState.rowData.id.toLong())) },
                onDismissRequest = { onAction(ViewStandingInstructionsAction.OnDismissDialog) },
                confirmText = stringResource(Res.string.feature_standing_instructions_delete),
                dismissText = stringResource(Res.string.feature_standing_instructions_cancel),
            )
        }
        is ViewStandingInstructionsState.DialogState.Edit -> {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds(),
            )
            MifosBasicDialog(
                onConfirm = {
                    onAction(
                        ViewStandingInstructionsAction.OnConfirmEdit(
                            instructionId = dialogState.rowData.id.toLong(),
                            amount = dialogState.amount,
                            validFrom = dialogState.validFrom,
                            beneficiary = dialogState.beneficiary,
                            fromAccount = dialogState.fromAccount,
                            toAccount = dialogState.toAccount,
                        ),
                    )
                },
                onDismissRequest = { onAction(ViewStandingInstructionsAction.OnDismissDialog) },
                confirmText = stringResource(Res.string.feature_standing_instructions_confirm),
                dismissText = stringResource(Res.string.feature_standing_instructions_cancel),
                isConfirmEnabled = dialogState.amount.isNotEmpty() && dialogState.amountError == null,
                title = stringResource(Res.string.feature_standing_instructions_edit_instruction),
                content = {
                    Column {
                        if (dialogState.isDatePickerShown) {
                            DatePickerDialog(
                                onDismissRequest = { onAction(ViewStandingInstructionsAction.OnToggleDatePicker(false)) },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onAction(ViewStandingInstructionsAction.OnToggleDatePicker(false))
                                            datePickerState.selectedDateMillis?.let {
                                                val formattedDate = ApiDateFormatter.formatForApi(it, DateFormatPattern.ISO)
                                                onAction(ViewStandingInstructionsAction.OnEditFieldChanged(EditField.VALID_FROM, formattedDate))
                                            }
                                        },
                                    ) { Text(stringResource(Res.string.feature_standing_instructions_ok)) }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { onAction(ViewStandingInstructionsAction.OnToggleDatePicker(false)) },
                                    ) { Text(stringResource(Res.string.feature_standing_instructions_cancel)) }
                                },
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        MifosOutlinedTextField(
                            value = dialogState.amount,
                            onValueChange = { onAction(ViewStandingInstructionsAction.OnEditFieldChanged(EditField.AMOUNT, it)) },
                            label = stringResource(Res.string.feature_standing_instructions_amount),
                            config = MifosTextFieldConfig(
                                isError = dialogState.amountError != null,
                                errorText = dialogState.amountError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                ),
                            ),
                        )

                        Spacer(modifier = Modifier.height(DesignToken.padding.medium))

                        MifosDatePickerTextField(
                            value = dialogState.validFrom,
                            label = stringResource(Res.string.feature_standing_instructions_valid_from),
                            openDatePicker = { onAction(ViewStandingInstructionsAction.OnToggleDatePicker(true)) },
                        )
                    }
                },
            )
        }
        is ViewStandingInstructionsState.DialogState.Loading -> {
            MifosProgressIndicator()
        }
        null -> Unit
    }
}

@Composable
private fun ViewStandingInstructionsData(
    tableData: StandingInstructionTableData,
    dialogState: ViewStandingInstructionsState.DialogState?,
    onAction: (ViewStandingInstructionsAction) -> Unit,
) {
    val scrollState = rememberScrollState()

    val smallWidth = DesignToken.sizes.tableCellWidthSmall
    val mediumWidth = DesignToken.sizes.tableCellWidthMedium
    val largeWidth = DesignToken.sizes.tableCellWidthLarge

    val columnWidths = remember(smallWidth, mediumWidth, largeWidth) {
        listOf(
            smallWidth,
            largeWidth,
            mediumWidth,
            largeWidth,
            mediumWidth,
            mediumWidth,
            largeWidth,
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = DesignToken.padding.large),
        horizontalAlignment = Alignment.Start,
    ) {
        if (tableData.rows.isNotEmpty()) {
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                ) {
                    StandingInstructionTableHeader(columnWidths)
                }
            }
            items(tableData.rows) { row ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                ) {
                    StandingInstructionRow(
                        row = row,
                        widths = columnWidths,
                        onClick = { onAction(ViewStandingInstructionsAction.OnRowClick(row)) },
                        showDropdown = dialogState is ViewStandingInstructionsState.DialogState.Options && dialogState.rowData.id == row.id,
                        onDismissDropdown = { onAction(ViewStandingInstructionsAction.OnDismissDialog) },
                        onEditClick = { onAction(ViewStandingInstructionsAction.OnEditClick(row)) },
                        onDeleteClick = { onAction(ViewStandingInstructionsAction.OnDeleteClick(row)) },
                    )
                }
            }
        } else {
            item {
                MifosEmptyCard(
                    msg = stringResource(Res.string.feature_standing_instructions_no_standing_instructions_found_message),
                    title = stringResource(Res.string.feature_standing_instructions_no_standing_instructions_found_message),
                )
            }
        }
    }
}

@Composable
private fun StandingInstructionTableHeader(widths: List<Dp>) {
    val headers = listOf(
        stringResource(Res.string.feature_standing_instructions_table_header_client_id),
        stringResource(Res.string.feature_standing_instructions_table_header_client),
        stringResource(Res.string.feature_standing_instructions_table_header_from_account),
        stringResource(Res.string.feature_standing_instructions_table_header_beneficiary),
        stringResource(Res.string.feature_standing_instructions_table_header_to_account),
        stringResource(Res.string.feature_standing_instructions_table_header_amount),
        stringResource(Res.string.feature_standing_instructions_table_header_validity),
    )

    MifosTableRow(
        cells = headers.map { header ->
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = DesignToken.padding.small,
                            horizontal = DesignToken.padding.extraSmall,
                        ),
                ) {
                    Text(
                        text = header,
                        style = MifosTypography.labelMediumEmphasized,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
        widths = widths,
        backgroundColor = MaterialTheme.colorScheme.primary,
        edgeOffset = DesignToken.spacing.medium,
        cornerShape = DesignToken.shapes.topMedium,
    )
}

@Composable
private fun StandingInstructionRow(
    row: StandingInstructionRowData,
    widths: List<Dp>,
    onClick: () -> Unit,
    showDropdown: Boolean,
    onDismissDropdown: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val rowValues = listOf(
        row.id.toString(),
        row.client,
        row.fromAccount,
        row.beneficiary,
        row.toAccount,
        row.amount,
        row.validity,
    )

    Box {
        MifosTableRow(
            cells = rowValues.map { value ->
                {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = DesignToken.padding.small,
                                horizontal = DesignToken.padding.extraSmall,
                            ),
                    ) {
                        Text(text = value, style = MifosTypography.labelMedium)
                    }
                }
            },
            widths = widths,
            backgroundColor = Color.Transparent,
            edgeOffset = DesignToken.spacing.medium,
            onClick = onClick,
        )

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = onDismissDropdown,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        ) {
            MifosMenuDropDownItem(
                option = stringResource(Res.string.feature_standing_instructions_edit),
                onClick = onEditClick,
            )
            MifosMenuDropDownItem(
                option = stringResource(Res.string.feature_standing_instructions_delete),
                onClick = onDeleteClick,
            )
        }
    }
}
