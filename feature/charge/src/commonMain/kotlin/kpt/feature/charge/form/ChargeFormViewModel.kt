/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.DateHelper
import kpt.core.data.charge.ChargeCommandRepository
import kpt.core.data.charge.ChargeRepository
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.objects.payloads.ChargesPayload
import kpt.core.model.objects.template.client.ChargeOptions
import kpt.feature.charge.ui.CHARGE_ID_KEY
import kpt.feature.charge.ui.chargeResource
import kpt.feature.charge.ui.chargeResourceId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Add or edit a charge on any resource — ported from the add/update half of
 * `6b66e8a43:feature/client/charges/{ChargesScreen,ChargesViewModel}.kt` (`ChargeAddFields`,
 * `createCharge`, `updateCharge`, `getCharge`) and the loan's
 * `loanChargeForm/{LoanChargeFormSheet,LoanChargeSheetViewModel}.kt`, which were the same form twice.
 *
 * ## What is SELECTABLE depends on the resource, and on what the form can collect
 *
 * An option needs an id, a name and an amount to be sent at all (the loan form's `mapNotNull`,
 * kept). Beyond that:
 *  - LOAN keeps the loan form's filter: disbursement / installment-fee / specified-due-date, FLAT
 *    only — the original dropped everything else because the form has nothing to ask for it.
 *  - CLIENT and SAVINGS drop only the kinds that need inputs this form does not collect: the
 *    annual / monthly / weekly fees (`feeOnMonthDay`, `feeInterval`) and the overdue-installment
 *    fee, which Fineract applies itself.
 *
 * ## The due date follows the charge
 *
 * Shown and sent only for a specified-due-date charge. The original CLIENT form always sent one,
 * and a disbursement or installment charge that carries a due date is rejected outright.
 *
 * ## Editing
 *
 * The charge being edited is read from the table the list fills (offline-safe) and matched to its
 * template option by the DEFINITION id — the original matched by NAME, so two charge definitions
 * sharing a name prefilled the wrong one. The definition cannot change on an update, so the picker
 * is read-only and the payload carries only amount + due date: Fineract's PUT does not accept
 * `chargeId`.
 */
internal class ChargeFormViewModel(
    private val repository: ChargeRepository,
    private val commands: ChargeCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ChargeFormState, ChargeFormEvent, ChargeFormAction>(
    ChargeFormState(
        resource = savedStateHandle.chargeResource(),
        resourceId = savedStateHandle.chargeResourceId(),
        // The route's -1 sentinel narrowed to "no charge yet".
        editingId = savedStateHandle.get<Int>(CHARGE_ID_KEY)?.takeIf { it >= 0 },
    ),
) {

    init {
        load()
    }

    override fun handleAction(action: ChargeFormAction) {
        when (action) {
            ChargeFormAction.NavigateBack -> sendEvent(ChargeFormEvent.NavigateBack)
            ChargeFormAction.OnRetry -> load()

            is ChargeFormAction.OnChargeSelected -> if (!state.isEdit) {
                mutableStateFlow.value = state.copy(
                    selectedCharge = action.charge,
                    // The chosen charge's DEFAULT amount, which the officer may then edit.
                    amount = formatAmount(action.charge.amount),
                    chargeError = false,
                    amountError = false,
                )
            }

            is ChargeFormAction.OnAmountChanged ->
                mutableStateFlow.value = state.copy(amount = action.amount, amountError = false)

            is ChargeFormAction.OnDueDateChanged -> mutableStateFlow.value = state.copy(dueDate = action.millis)

            is ChargeFormAction.UpdateDatePicker -> mutableStateFlow.value = state.copy(showDatePicker = action.show)

            ChargeFormAction.CloseDialog -> mutableStateFlow.value = state.copy(dialogState = null)

            ChargeFormAction.OnSubmit -> submit()
        }
    }

    private fun load() {
        mutableStateFlow.value = state.copy(isLoadingTypes = true, typesError = false)

        viewModelScope.launch {
            val template = repository.chargeTemplate(state.resource, state.resourceId)
            val types = template?.chargeOptions.orEmpty().mapNotNull { it.toChargeType(state.resource) }
            val editing = state.editingId?.let { repository.charge(state.resource, state.resourceId, it) }

            mutableStateFlow.value = when {
                template == null -> state.copy(isLoadingTypes = false, typesError = true)
                // Editing a charge this device has never loaded — nothing to prefill from.
                state.isEdit && editing == null -> state.copy(isLoadingTypes = false, typesError = true)
                editing != null -> state.copy(
                    isLoadingTypes = false,
                    chargeTypes = types,
                    // The definition, even if the template no longer offers it as NEW.
                    selectedCharge = types.firstOrNull { it.id == editing.chargeId }
                        ?: ChargeType(
                            id = editing.chargeId ?: -1,
                            name = editing.name.orEmpty(),
                            amount = editing.amount ?: 0.0,
                            time = editing.chargeTimeType?.value.orEmpty(),
                            calculation = editing.chargeCalculationType?.value.orEmpty(),
                            needsDueDate = editing.chargeTimeType?.id == SPECIFIED_DUE_DATE,
                        ),
                    amount = editing.amount?.let { formatAmount(it) }.orEmpty(),
                    dueDate = DateHelper.getDateAsLongFromList(editing.dueDate) ?: state.dueDate,
                )

                else -> state.copy(isLoadingTypes = false, chargeTypes = types)
            }
        }
    }

    private fun submit() {
        if (state.isSubmitting) return
        val charge = state.selectedCharge
        // Validated at SUBMIT, not only while typing: the original checked the amount as a side
        // effect of a keystroke, so an untouched prefilled value went out unvalidated.
        val amount = state.amount.trim()
        when {
            charge == null -> mutableStateFlow.value = state.copy(chargeError = true)
            (amount.toDoubleOrNull() ?: 0.0) <= 0.0 -> mutableStateFlow.value = state.copy(amountError = true)
            else -> send(charge, amount)
        }
    }

    private fun send(charge: ChargeType, amount: String) {
        mutableStateFlow.value = state.copy(isSubmitting = true, chargeError = false, dialogState = null)

        // `getDateAsStringFromLong` emits "dd MMMM yyyy" and `SHORT_MONTH` declares the same — the
        // pairing the original already had, kept.
        val dueDate = DateHelper.getDateAsStringFromLong(state.dueDate).takeIf { charge.needsDueDate }
        val editingId = state.editingId

        viewModelScope.launch {
            val result = if (editingId != null) {
                commands.updateCharge(
                    state.resource,
                    state.resourceId,
                    editingId,
                    ChargesPayload(
                        amount = amount,
                        dueDate = dueDate,
                        locale = LOCALE,
                        dateFormat = DateHelper.SHORT_MONTH,
                    ),
                )
            } else {
                commands.createCharge(
                    state.resource,
                    state.resourceId,
                    ChargesPayload(
                        chargeId = charge.id,
                        amount = amount,
                        dueDate = dueDate,
                        locale = LOCALE,
                        dateFormat = DateHelper.SHORT_MONTH,
                    ),
                )
            }

            mutableStateFlow.value = state.copy(
                isSubmitting = false,
                dialogState = when (result) {
                    is MutationResult.Applied -> ChargeFormState.DialogState.Submitted
                    is MutationResult.Blocked -> ChargeFormState.DialogState.Failed(
                        offline = result.reason == BlockReason.OFFLINE,
                    )

                    is MutationResult.Failed -> {
                        Logger.e(tag = TAG, throwable = result.cause) { "save charge failed" }
                        // Fineract's own reason ("charge already paid", "due date before
                        // disbursement") is what the officer can act on.
                        ChargeFormState.DialogState.Failed(serverMessage = result.cause.serverErrorMessage())
                    }

                    is MutationResult.Conflicted -> ChargeFormState.DialogState.Failed()
                },
            )
        }
    }

    private companion object {
        const val TAG = "ChargeForm"
        const val LOCALE = "en"
    }
}

/** A selectable charge definition, with everything the payload and the form both need. */
internal data class ChargeType(
    val id: Int,
    val name: String,
    val amount: Double,
    /** Server-supplied display value ("Specified due date", "Disbursement", …). */
    val time: String,
    val calculation: String,
    val needsDueDate: Boolean,
)

// Fineract's ChargeTimeType / ChargeCalculationType ids — stable API enums, unlike their `value`
// strings, which the server localises.
private const val DISBURSEMENT = 1
private const val SPECIFIED_DUE_DATE = 2
private const val ANNUAL_FEE = 6
private const val MONTHLY_FEE = 7
private const val INSTALLMENT_FEE = 8
private const val OVERDUE_INSTALLMENT = 9
private const val WEEKLY_FEE = 11
private const val FLAT = 1

private val LOAN_TIMES = setOf(DISBURSEMENT, SPECIFIED_DUE_DATE, INSTALLMENT_FEE)
private val NEEDS_UNCOLLECTED_INPUT = setOf(ANNUAL_FEE, MONTHLY_FEE, WEEKLY_FEE, OVERDUE_INSTALLMENT)

internal fun ChargeOptions.toChargeType(resource: ChargeResource): ChargeType? {
    val time = chargeTimeType?.id
    val selectable = when (resource) {
        ChargeResource.LOAN -> time in LOAN_TIMES && chargeCalculationType?.id == FLAT
        ChargeResource.CLIENT, ChargeResource.SAVINGS -> time != null && time !in NEEDS_UNCOLLECTED_INPUT
    }
    if (!selectable) return null
    return id?.let { chargeId ->
        name?.let { chargeName ->
            amount?.let { chargeAmount ->
                ChargeType(
                    id = chargeId,
                    name = chargeName,
                    amount = chargeAmount,
                    time = chargeTimeType?.value.orEmpty(),
                    calculation = chargeCalculationType?.value.orEmpty(),
                    needsDueDate = time == SPECIFIED_DUE_DATE,
                )
            }
        }
    }
}

/** "150.0" -> "150" — a prefilled whole amount should not read as a decimal the officer typed. */
private fun formatAmount(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

@OptIn(ExperimentalTime::class)
internal data class ChargeFormState(
    val resource: ChargeResource = ChargeResource.CLIENT,
    val resourceId: Int = -1,
    /** The levied charge being edited; `null` when adding. */
    val editingId: Int? = null,
    val chargeTypes: List<ChargeType> = emptyList(),
    val selectedCharge: ChargeType? = null,
    val amount: String = "",
    val dueDate: Long = Clock.System.now().toEpochMilliseconds(),
    val showDatePicker: Boolean = false,
    val isLoadingTypes: Boolean = false,
    val typesError: Boolean = false,
    val chargeError: Boolean = false,
    val amountError: Boolean = false,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    val isEdit: Boolean get() = editingId != null

    /** A due date is collected only for the one charge kind that has one. */
    val needsDueDate: Boolean get() = selectedCharge?.needsDueDate == true

    sealed interface DialogState {
        data object Submitted : DialogState

        /** [offline] and [serverMessage] pick the message; neither set is the generic failure. */
        data class Failed(val offline: Boolean = false, val serverMessage: String? = null) : DialogState
    }
}

internal sealed interface ChargeFormEvent {
    data object NavigateBack : ChargeFormEvent
}

internal sealed interface ChargeFormAction {
    data object NavigateBack : ChargeFormAction
    data object OnRetry : ChargeFormAction
    data object OnSubmit : ChargeFormAction
    data object CloseDialog : ChargeFormAction
    data class OnChargeSelected(val charge: ChargeType) : ChargeFormAction
    data class OnAmountChanged(val amount: String) : ChargeFormAction
    data class OnDueDateChanged(val millis: Long) : ChargeFormAction
    data class UpdateDatePicker(val show: Boolean) : ChargeFormAction
}
