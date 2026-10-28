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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mifos.core.common.utils.ApiDateFormatter
import com.mifos.core.common.utils.CurrencyFormatter
import com.mifos.core.common.utils.DataState
import com.mifos.core.common.utils.DataState.Loading
import com.mifos.core.common.utils.DataState.Success
import com.mifos.core.common.utils.DateFormatPattern
import com.mifos.core.common.utils.Page
import com.mifos.core.data.repository.StandingInstructionsRepository
import com.mifos.core.model.objects.standingInstructions.StandingInstruction
import com.mifos.core.model.objects.standingInstructions.StandingInstructionUpdate
import com.mifos.core.ui.util.BaseViewModel
import com.mifos.feature.standingInstructions.viewStandingInstructions.ViewStandingInstructionsScreenRoute
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewStandingInstructionsViewModel(
    private val repository: StandingInstructionsRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ViewStandingInstructionsState, ViewStandingInstructionsEvent, ViewStandingInstructionsAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<ViewStandingInstructionsScreenRoute>()
        ViewStandingInstructionsState(
            fromAccountId = route.fromAccountId,
            fromAccountType = route.fromAccountType,
            clientId = route.clientId,
            clientName = route.clientName,
            currencyCode = route.currencyCode,
        )
    },
) {

    init {
        viewModelScope.launch {
            loadStandingInstructions()
        }
    }

    override fun handleAction(action: ViewStandingInstructionsAction) {
        when (action) {
            ViewStandingInstructionsAction.OnNavigateBack -> {
                sendEvent(ViewStandingInstructionsEvent.NavigateBack)
            }

            ViewStandingInstructionsAction.Retry -> {
                retry()
            }

            ViewStandingInstructionsAction.DismissErrorDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is ViewStandingInstructionsAction.OnRowClick -> {
                mutableStateFlow.update {
                    it.copy(dialogState = ViewStandingInstructionsState.DialogState.Options(action.rowData))
                }
            }

            is ViewStandingInstructionsAction.OnDeleteClick -> {
                mutableStateFlow.update {
                    it.copy(dialogState = ViewStandingInstructionsState.DialogState.ConfirmDelete(action.rowData))
                }
            }

            is ViewStandingInstructionsAction.OnEditClick -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = ViewStandingInstructionsState.DialogState.Edit(
                            rowData = action.rowData,
                            amount = action.rowData.amount,
                            validFrom = action.rowData.validity,
                            beneficiary = action.rowData.beneficiary,
                            fromAccount = action.rowData.fromAccount,
                            toAccount = action.rowData.toAccount,
                        ),
                    )
                }
            }

            is ViewStandingInstructionsAction.OnEditFieldChanged -> {
                val currentEdit = state.dialogState as? ViewStandingInstructionsState.DialogState.Edit
                if (currentEdit != null) {
                    mutableStateFlow.update {
                        val updatedEdit = when (action.field) {
                            EditField.AMOUNT -> {
                                val amountError = if (action.value.toDoubleOrNull() == null || action.value.toDouble() <= 0) {
                                    "Invalid amount"
                                } else {
                                    null
                                }
                                currentEdit.copy(amount = action.value, amountError = amountError)
                            }
                            EditField.VALID_FROM -> currentEdit.copy(validFrom = action.value)
                            EditField.BENEFICIARY -> currentEdit.copy(beneficiary = action.value)
                            EditField.FROM_ACCOUNT -> currentEdit.copy(fromAccount = action.value)
                            EditField.TO_ACCOUNT -> currentEdit.copy(toAccount = action.value)
                        }
                        it.copy(dialogState = updatedEdit)
                    }
                }
            }

            is ViewStandingInstructionsAction.OnToggleDatePicker -> {
                val currentEdit = state.dialogState as? ViewStandingInstructionsState.DialogState.Edit
                if (currentEdit != null) {
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = currentEdit.copy(
                                isDatePickerShown = action.show,
                            ),
                        )
                    }
                }
            }

            is ViewStandingInstructionsAction.OnConfirmDelete -> {
                deleteStandingInstruction(action.instructionId)
            }

            is ViewStandingInstructionsAction.OnConfirmEdit -> {
                updateStandingInstruction(action.instructionId, action.amount, action.validFrom)
            }

            ViewStandingInstructionsAction.OnDismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }
        }
    }

    private fun deleteStandingInstruction(instructionId: Long) {
        mutableStateFlow.update {
            it.copy(dialogState = ViewStandingInstructionsState.DialogState.Loading)
        }
        viewModelScope.launch {
            val result = repository.deleteStandingInstruction(instructionId)
            when (result) {
                is DataState.Error -> {
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = ViewStandingInstructionsState.DialogState.Error(
                                title = "Error deleting instruction",
                                message = result.message,
                            ),
                        )
                    }
                }
                is DataState.Success -> {
                    mutableStateFlow.update {
                        it.copy(dialogState = null)
                    }
                    loadStandingInstructions()
                }
                DataState.Loading -> Unit
            }
        }
    }

    private fun updateStandingInstruction(instructionId: Long, amount: String, validFrom: String) {
        mutableStateFlow.update {
            it.copy(dialogState = ViewStandingInstructionsState.DialogState.Loading)
        }
        viewModelScope.launch {
            val update = StandingInstructionUpdate(
                amount = amount,
                validFrom = validFrom,
                dateFormat = DateFormatPattern.ISO.pattern,
                locale = ApiDateFormatter.LOCALE,
            )
            val result = repository.updateStandingInstruction(instructionId, update)
            when (result) {
                is DataState.Error -> {
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = ViewStandingInstructionsState.DialogState.Error(
                                title = "Error updating instruction",
                                message = result.message,
                            ),
                        )
                    }
                }
                is DataState.Success -> {
                    mutableStateFlow.update {
                        it.copy(dialogState = null)
                    }
                    loadStandingInstructions()
                }
                DataState.Loading -> Unit
            }
        }
    }

    private fun retry() {
        viewModelScope.launch {
            loadStandingInstructions()
        }
    }

    private fun loadStandingInstructions() {
        mutableStateFlow.update {
            it.copy(dataState = DataState.Loading)
        }

        viewModelScope.launch {
            repository.getStandingInstructionList(
                clientId = state.clientId,
                clientName = state.clientName,
                fromAccountId = state.fromAccountId,
                fromAccountType = state.fromAccountType,
                limit = 14,
                offset = 0,
            ).collect { dataState ->
                when (dataState) {
                    is DataState.Error -> {
                        mutableStateFlow.update {
                            it.copy(
                                dataState = dataState,
                            )
                        }
                    }

                    Loading -> {
                        mutableStateFlow.update {
                            it.copy(dataState = DataState.Loading)
                        }
                    }

                    is Success -> {
                        val tableData = mapToTableData(dataState.data)
                        mutableStateFlow.update {
                            it.copy(
                                tableData = tableData,
                                dataState = DataState.Success(dataState.data),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun mapToTableData(page: Page<StandingInstruction>): StandingInstructionTableData {
        val rows = page.pageItems.map { instruction ->
            StandingInstructionRowData(
                id = instruction.id,
                client = instruction.fromClient?.displayName ?: "--",
                fromAccount = instruction.fromAccount?.accountNo ?: "--",
                beneficiary = instruction.toClient?.displayName ?: "--",
                toAccount = instruction.toAccount?.accountNo ?: "--",
                amount = if (state.currencyCode.isNotEmpty()) {
                    CurrencyFormatter.format(
                        balance = instruction.amount,
                        currencyCode = state.currencyCode,
                        maximumFractionDigits = 2,
                    )
                } else {
                    instruction.amount?.toString() ?: "--"
                },
                validity = instruction.validFrom ?: "--",
            )
        }

        return StandingInstructionTableData(rows = rows)
    }
}

data class ViewStandingInstructionsState(
    val fromAccountId: Long,
    val fromAccountType: Int,
    val clientId: Long,
    val clientName: String,
    val currencyCode: String,
    val dataState: DataState<Page<StandingInstruction>> = Loading,
    val tableData: StandingInstructionTableData? = null,
    val dialogState: DialogState? = null,
) {
    sealed interface DialogState {
        data class Error(val title: String, val message: String) : DialogState
        data class Options(val rowData: StandingInstructionRowData) : DialogState
        data class ConfirmDelete(val rowData: StandingInstructionRowData) : DialogState
        data class Edit(
            val rowData: StandingInstructionRowData,
            val amount: String,
            val validFrom: String,
            val beneficiary: String,
            val fromAccount: String,
            val toAccount: String,
            val amountError: String? = null,
            val isDatePickerShown: Boolean = false,
        ) : DialogState
        data object Loading : DialogState
    }
}

data class StandingInstructionTableData(
    val rows: List<StandingInstructionRowData>,
)

data class StandingInstructionRowData(
    val id: Int,
    val client: String,
    val fromAccount: String,
    val beneficiary: String,
    val toAccount: String,
    val amount: String,
    val validity: String,
)

sealed interface ViewStandingInstructionsEvent {
    data object NavigateBack : ViewStandingInstructionsEvent
}

enum class EditField {
    AMOUNT,
    VALID_FROM,
    BENEFICIARY,
    FROM_ACCOUNT,
    TO_ACCOUNT,
}

sealed interface ViewStandingInstructionsAction {
    data object OnNavigateBack : ViewStandingInstructionsAction
    data object Retry : ViewStandingInstructionsAction
    data object DismissErrorDialog : ViewStandingInstructionsAction
    data class OnRowClick(val rowData: StandingInstructionRowData) : ViewStandingInstructionsAction
    data class OnDeleteClick(val rowData: StandingInstructionRowData) : ViewStandingInstructionsAction
    data class OnEditClick(val rowData: StandingInstructionRowData) : ViewStandingInstructionsAction
    data class OnEditFieldChanged(val field: EditField, val value: String) : ViewStandingInstructionsAction
    data class OnToggleDatePicker(val show: Boolean) : ViewStandingInstructionsAction
    data class OnConfirmDelete(val instructionId: Long) : ViewStandingInstructionsAction
    data class OnConfirmEdit(
        val instructionId: Long,
        val amount: String,
        val validFrom: String,
        val beneficiary: String,
        val fromAccount: String,
        val toAccount: String,
    ) : ViewStandingInstructionsAction
    data object OnDismissDialog : ViewStandingInstructionsAction
}
