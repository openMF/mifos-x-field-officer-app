/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.recurring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.FormDraftSlot
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.store.submit.draft.isOffline
import kpt.core.base.store.submit.draft.offlineWhenUnsent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.recurringdeposit.RecurringDepositCommandRepository
import kpt.core.data.recurringdeposit.RecurringDepositRepository
import kpt.core.model.draft.CreateForm
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.recurringdeposit.RecurringDepositAccountTemplate
import kpt.core.model.recurringdeposit.RecurringDepositApplicationInput
import kpt.core.model.recurringdeposit.RecurringDepositChargeInput
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * The new recurring-deposit application — ported from
 * `6b66e8a43:feature/recurringDeposit/newRecurringDepositAccount/` (a 1,118-line ViewModel, a
 * 220-line screen and six pages totalling ~2,100 lines). The largest single item in the migration.
 *
 * ## It lives in `feature/savings`, not its own module
 *
 * A deliberate deviation from the original's `feature/recurringDeposit`. A recurring deposit IS a
 * savings-family account — `SavingAccountDepositTypeEntity.ServerTypes.RECURRING` — and the savings
 * summary already serves all three deposit types by `(type, id)`, so this wizard shares that
 * module's strings, components and account vocabulary. A whole module whose only content is one
 * wizard is the duplicate-scaffolding shape `G-IMPL-DUP` guards against.
 *
 * ## The product index could crash the submit
 *
 * ```kotlin
 * productId = state.template.productOptions?.get(state.recurringDepositAccountDetail.loanProductSelected)?.id
 * //                                        └─ get, not getOrNull …
 * val loanProductSelected: Int = -1        // ← … against this default
 * ```
 *
 * `get(-1)` throws, so submitting before choosing a product crashed. The surrounding fields were
 * more careful — five other option reads are guarded by an explicit `!= -1` and two use
 * `getOrNull` — which is what makes the unguarded one easy to miss: the file looks defensive.
 *
 * Six option reads additionally used `get(index)` *inside* an `!= -1` guard, which protects against
 * the sentinel but NOT against an index left over from a previous template. [selections] is keyed,
 * so neither failure is expressible.
 *
 * ## The date pairing was CORRECT here
 *
 * `dateFormat = DateHelper.SHORT_MONTH` alongside a date from
 * `DateHelper.getDateAsStringFromLong` — both `dd-MM-yyyy`. Worth saying, because eight other
 * screens in this migration got the same pairing wrong and two sent raw epoch millis: this one was
 * right by both halves happening to come from the same object. The port keeps it right by
 * construction instead, via [RecurringDepositApplicationInput]'s required parameters.
 *
 * ## `isCalendarInherited` is NOT sent
 *
 * The original passed `settings.depositPeriod.depositFrequencySameAsGroupCenterMeeting` — whether
 * the deposit frequency follows the GROUP or CENTRE meeting schedule. This screen is reached only
 * for a CLIENT (its route carries `clientId`, and the original's own caller is the client-side
 * chooser), so a client has no meeting schedule to inherit and the field had no control: it was a
 * constant `false` dressed as a setting. Omitted rather than sent, and recorded in CAPABILITY_GAPS.
 */
@OptIn(ExperimentalTime::class)
internal class RecurringDepositViewModel(
    repository: RecurringDepositRepository,
    private val commands: RecurringDepositCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<RecurringDepositState, RecurringDepositEvent, RecurringDepositAction>(
    RecurringDepositState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    /** This client's application that did not land — resumable here, listed in Sync & Drafts. */
    private val formDraft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.RECURRING_DEPOSIT.key,
        ownerId = state.clientId.toLong(),
        RecurringDepositState.serializer(),
        RecurringDepositApplicationInput.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered

    private var lastInput: RecurringDepositApplicationInput? = null

    private fun keepDraft(input: RecurringDepositApplicationInput, failure: String?) = formDraft.keep(
        title = state.selections[RecurringField.PRODUCT]?.label.orEmpty(),
        state = state.copy(isSubmitting = false, step = RecurringStep.DETAILS),
        payload = input,
        failure = failure,
    )

    private val stream = repository.templateStream(state.clientId, viewModelScope)

    /**
     * The application template — ONE read per client, which carries both the product list and
     * every option list.
     *
     * Unlike the loan and savings wizards there is no per-product cascade: the recurring-deposit
     * template endpoint is keyed on the client alone, so choosing a product does not re-ask.
     */
    val template: StateFlow<ScreenState<RecurringDepositForm>> = stream.state
        .mapContent { template, _ -> template.toForm() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: RecurringDepositAction) {
        when (action) {
            RecurringDepositAction.ResumeDraft,
            RecurringDepositAction.DiscardDraft,
            RecurringDepositAction.SaveDraft,
            -> handleDraftAction(action)

            RecurringDepositAction.NavigateBack -> sendEvent(RecurringDepositEvent.NavigateBack)
            RecurringDepositAction.Done -> sendEvent(RecurringDepositEvent.Done)
            RecurringDepositAction.OnRetry -> stream.retry()
            RecurringDepositAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            is RecurringDepositAction.OnOptionSelected,
            is RecurringDepositAction.OnNumberChanged,
            is RecurringDepositAction.OnExternalIdChanged,
            is RecurringDepositAction.OnToggled,
            is RecurringDepositAction.OnDateChanged,
            RecurringDepositAction.OpenDatePicker,
            RecurringDepositAction.CloseDatePicker,
            is RecurringDepositAction.OnChargeAdded,
            is RecurringDepositAction.OnChargeRemoved,
            -> handleFormEdit(action)

            RecurringDepositAction.OnNext -> advance()
            RecurringDepositAction.OnBack -> retreat()
            RecurringDepositAction.OnSubmit -> submit()
        }
    }

    private fun handleDraftAction(action: RecurringDepositAction) {
        when (action) {
            RecurringDepositAction.ResumeDraft -> formDraft.resume()?.let { mutableStateFlow.value = it }
            RecurringDepositAction.DiscardDraft -> formDraft.discard()
            RecurringDepositAction.SaveDraft -> lastInput?.let { input ->
                val failed = state.dialogState as? RecurringDialog.Failed
                keepDraft(input, failure = failed?.msg ?: "Not accepted by the server.")
                mutableStateFlow.value = state.copy(dialogState = failed?.copy(draftSaved = true))
            }
            else -> Unit
        }
    }

    private fun handleFormEdit(action: RecurringDepositAction) {
        when (action) {
            is RecurringDepositAction.OnOptionSelected -> mutableStateFlow.value = state.copy(
                selections = state.selections + (action.field to action.option),
                errors = state.errors - action.field,
            )

            is RecurringDepositAction.OnNumberChanged -> mutableStateFlow.value = state.copy(
                numbers = state.numbers + (action.field to action.value),
                errors = state.errors - action.field,
            )

            is RecurringDepositAction.OnExternalIdChanged ->
                mutableStateFlow.value = state.copy(externalId = action.value)

            is RecurringDepositAction.OnToggled ->
                mutableStateFlow.value = state.copy(toggles = state.toggles + (action.toggle to action.selected))

            is RecurringDepositAction.OnDateChanged ->
                mutableStateFlow.value = state.copy(submittedOn = action.millis)

            RecurringDepositAction.OpenDatePicker ->
                mutableStateFlow.value = state.copy(isDatePickerOpen = true)

            RecurringDepositAction.CloseDatePicker ->
                mutableStateFlow.value = state.copy(isDatePickerOpen = false)

            is RecurringDepositAction.OnChargeAdded ->
                mutableStateFlow.value = state.copy(charges = state.charges + action.charge)

            is RecurringDepositAction.OnChargeRemoved ->
                mutableStateFlow.value =
                    state.copy(charges = state.charges.filterIndexed { i, _ -> i != action.index })

            else -> Unit
        }
    }

    /** Validate THIS step, then move on. The original's step movement validated nothing. */
    private fun advance() {
        val missing = state.step.missing(state)
        mutableStateFlow.value = state.copy(errors = missing)
        if (missing.isNotEmpty()) return

        RecurringStep.entries.getOrNull(state.step.ordinal + 1)
            ?.let { mutableStateFlow.value = state.copy(step = it) }
    }

    private fun retreat() {
        RecurringStep.entries.getOrNull(state.step.ordinal - 1)
            ?.let { mutableStateFlow.value = state.copy(step = it) }
            ?: sendEvent(RecurringDepositEvent.NavigateBack)
    }

    private fun submit() {
        if (state.isSubmitting) return

        // Every step re-validated: the officer can walk back and empty a field.
        val missing = RecurringStep.entries.flatMap { it.missing(state) }.toSet()
        if (missing.isNotEmpty()) {
            mutableStateFlow.value = state.copy(
                errors = missing,
                step = RecurringStep.entries.first { it.missing(state).isNotEmpty() },
            )
        } else {
            // A VALUE, not an index into a list that may have changed — the read that used to be
            // `productOptions?.get(-1)`.
            state.selectionId(RecurringField.PRODUCT)?.let(::sendApplication)
        }
    }

    private fun sendApplication(productId: Int) {
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            val input = RecurringDepositApplicationInput(
                clientId = state.clientId,
                productId = productId,
                // The date and the format from one helper, so the pair cannot drift apart.
                submittedOnDate = ApiDateFormatter.formatForApi(state.submittedOn),
                locale = ApiDateFormatter.LOCALE,
                dateFormat = ApiDateFormatter.DATE_FORMAT,
                mandatoryRecommendedDepositAmount = state.double(RecurringField.DEPOSIT_AMOUNT),
                recurringFrequency = state.int(RecurringField.RECURRING_FREQUENCY),
                recurringFrequencyType = state.selectionId(RecurringField.RECURRING_FREQUENCY_TYPE),
                depositPeriod = state.int(RecurringField.DEPOSIT_PERIOD),
                depositPeriodFrequencyId = state.selectionId(RecurringField.DEPOSIT_PERIOD_TYPE),
                fieldOfficerId = state.selectionId(RecurringField.FIELD_OFFICER),
                externalId = state.externalId.trim().takeIf { it.isNotBlank() },
                isMandatoryDeposit = state.toggle(RecurringToggle.MANDATORY_DEPOSIT),
                adjustAdvanceTowardsFuturePayments =
                state.toggle(RecurringToggle.ADJUST_ADVANCE_PAYMENTS),
                allowWithdrawal = state.toggle(RecurringToggle.ALLOW_WITHDRAWAL),
                interestCompoundingPeriodType = state.selectionId(RecurringField.COMPOUNDING_PERIOD),
                interestPostingPeriodType = state.selectionId(RecurringField.POSTING_PERIOD),
                interestCalculationType = state.selectionId(RecurringField.CALCULATION_TYPE),
                interestCalculationDaysInYearType = state.selectionId(RecurringField.DAYS_IN_YEAR),
                lockinPeriodFrequency = state.int(RecurringField.LOCKIN_PERIOD),
                // Sent only alongside a frequency — a unit with no number names nothing.
                lockinPeriodFrequencyType = state.selectionId(RecurringField.LOCKIN_PERIOD_TYPE)
                    .takeIf { state.int(RecurringField.LOCKIN_PERIOD) != null },
                preClosurePenalApplicable = state.toggle(RecurringToggle.PRE_CLOSURE_PENALTY),
                // Both penalty fields are sent ONLY when the penalty applies. The original sent
                // whatever had been typed even with the checkbox clear, so a penalty the officer
                // had unchecked still reached the server.
                preClosurePenalInterest = state.double(RecurringField.PENALTY_INTEREST)
                    .takeIf { state.toggle(RecurringToggle.PRE_CLOSURE_PENALTY) },
                preClosurePenalInterestOnTypeId =
                state.selectionId(RecurringField.PENALTY_INTEREST_ON)
                    .takeIf { state.toggle(RecurringToggle.PRE_CLOSURE_PENALTY) },
                charges = state.charges.map {
                    RecurringDepositChargeInput(chargeId = it.chargeId, amount = it.amount)
                },
            )

            lastInput = input
            val result = commands.createRecurringDepositAccount(input).offlineWhenUnsent()
            // Offline: kept automatically and sent on reconnect.
            if (result.isOffline) keepDraft(input, failure = null)
            if (result is MutationResult.Applied) formDraft.clear()
            mutableStateFlow.value = when (result) {
                is MutationResult.Applied ->
                    state.copy(isSubmitting = false, dialogState = RecurringDialog.Created)

                is MutationResult.Blocked -> state.copy(
                    isSubmitting = false,
                    dialogState = RecurringDialog.Failed(
                        offline = result.reason == BlockReason.OFFLINE,
                        msg = null,
                        draftSaved = result.isOffline,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create recurring deposit failed" }
                    state.copy(
                        isSubmitting = false,
                        dialogState = RecurringDialog.Failed(offline = false, msg = result.cause.serverErrorMessage()),
                    )
                }

                is MutationResult.Conflicted -> state.copy(
                    isSubmitting = false,
                    dialogState = RecurringDialog.Failed(offline = false, msg = null),
                )
            }
        }
    }

    private companion object {
        const val TAG = "RecurringDeposit"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/** Positive and parseable. A deposit amount or period of zero is not an application. */
private fun String.isPositiveNumber(): Boolean = trim().toDoubleOrNull()?.let { it > 0 } == true

/** Parses and is not negative when filled, or is blank. */
private fun String.isNonNegativeOrBlank(): Boolean {
    val t = trim()
    return t.isEmpty() || (t.toDoubleOrNull()?.let { it >= 0 } == true)
}

/**
 * The template's option lists, projected once.
 *
 * Five option types in the template share `{id, code, value}` but are five distinct Kotlin types,
 * so each needs its own projection — there is no common supertype to write one against.
 */
private fun RecurringDepositAccountTemplate.toForm() = RecurringDepositForm(
    products = productOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.name.orEmpty())
    },
    fieldOfficers = fieldOfficerOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.displayName.orEmpty())
    },
    periodFrequencies = periodFrequencyTypeOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.value.orEmpty())
    },
    lockinFrequencies = lockinPeriodFrequencyTypeOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.value.orEmpty())
    },
    compoundingPeriods = interestCompoundingPeriodTypeOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.value.orEmpty())
    },
    postingPeriods = interestPostingPeriodTypeOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.value.orEmpty())
    },
    calculationTypes = interestCalculationTypeOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.value.orEmpty())
    },
    daysInYearTypes = interestCalculationDaysInYearTypeOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.value.orEmpty())
    },
    penaltyInterestOnTypes = preClosurePenalInterestOnTypeOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.value.orEmpty())
    },
    charges = chargeOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.name.orEmpty())
    },
)

/** Every option list the form renders, projected to one type. */
internal data class RecurringDepositForm(
    val products: List<RecurringOption>,
    val fieldOfficers: List<RecurringOption>,
    val periodFrequencies: List<RecurringOption>,
    val lockinFrequencies: List<RecurringOption>,
    val compoundingPeriods: List<RecurringOption>,
    val postingPeriods: List<RecurringOption>,
    val calculationTypes: List<RecurringOption>,
    val daysInYearTypes: List<RecurringOption>,
    val penaltyInterestOnTypes: List<RecurringOption>,
    val charges: List<RecurringOption>,
)

/** One dropdown choice. */
@Serializable
internal data class RecurringOption(val id: Int, val label: String)

/** A charge the officer attached, before it becomes a payload row. */
@Serializable
internal data class RecurringChargeRow(val chargeId: Int, val label: String, val amount: String)

/** The checkboxes. */
internal enum class RecurringToggle {
    MANDATORY_DEPOSIT,
    ADJUST_ADVANCE_PAYMENTS,
    ALLOW_WITHDRAWAL,
    PRE_CLOSURE_PENALTY,
}

/** Every field that can carry a value or an error. */
internal enum class RecurringField {
    PRODUCT,
    FIELD_OFFICER,
    DEPOSIT_AMOUNT,
    RECURRING_FREQUENCY,
    RECURRING_FREQUENCY_TYPE,
    DEPOSIT_PERIOD,
    DEPOSIT_PERIOD_TYPE,
    COMPOUNDING_PERIOD,
    POSTING_PERIOD,
    CALCULATION_TYPE,
    DAYS_IN_YEAR,
    LOCKIN_PERIOD,
    LOCKIN_PERIOD_TYPE,
    PENALTY_INTEREST,
    PENALTY_INTEREST_ON,
    ;

    companion object {
        /** Must parse and be > 0 — the deposit's size and cadence. */
        val requiredPositive = listOf(DEPOSIT_AMOUNT, RECURRING_FREQUENCY, DEPOSIT_PERIOD)

        /** Optional, but must parse and be >= 0 when filled. */
        val optionalNumbers = listOf(LOCKIN_PERIOD, PENALTY_INTEREST)

        /** The units the counts above are meaningless without. */
        val requiredTermSelections = listOf(RECURRING_FREQUENCY_TYPE, DEPOSIT_PERIOD_TYPE)

        /** The four interest fields Fineract requires. */
        val requiredInterestSelections = listOf(
            COMPOUNDING_PERIOD,
            POSTING_PERIOD,
            CALCULATION_TYPE,
            DAYS_IN_YEAR,
        )
    }
}

/** The wizard's steps, each owning the fields it validates. */
internal enum class RecurringStep {
    DETAILS {
        override fun missing(state: RecurringDepositState) = buildSet {
            if (state.selections[RecurringField.PRODUCT] == null) add(RecurringField.PRODUCT)
        }
    },
    TERMS {
        override fun missing(state: RecurringDepositState) = buildSet {
            RecurringField.requiredPositive.forEach {
                if (!state.number(it).isPositiveNumber()) add(it)
            }
            RecurringField.requiredTermSelections.forEach {
                if (state.selections[it] == null) add(it)
            }
        }
    },
    INTEREST {
        override fun missing(state: RecurringDepositState) = buildSet {
            RecurringField.optionalNumbers.forEach {
                if (!state.number(it).isNonNegativeOrBlank()) add(it)
            }
            RecurringField.requiredInterestSelections.forEach {
                if (state.selections[it] == null) add(it)
            }
            // The penalty's own fields matter only when the penalty applies.
            if (state.toggle(RecurringToggle.PRE_CLOSURE_PENALTY)) {
                if (!state.number(RecurringField.PENALTY_INTEREST).isPositiveNumber()) {
                    add(RecurringField.PENALTY_INTEREST)
                }
                if (state.selections[RecurringField.PENALTY_INTEREST_ON] == null) {
                    add(RecurringField.PENALTY_INTEREST_ON)
                }
            }
        }
    },

    /** Charges are optional; each row was validated as it was added. */
    CHARGES {
        override fun missing(state: RecurringDepositState) = emptySet<RecurringField>()
    },
    PREVIEW {
        override fun missing(state: RecurringDepositState) = emptySet<RecurringField>()
    },
    ;

    abstract fun missing(state: RecurringDepositState): Set<RecurringField>
}

internal sealed interface RecurringDialog {
    data object Created : RecurringDialog

    /** [draftSaved] = kept (offline automatically, or "Save as draft"), in Sync & Drafts. */
    data class Failed(val offline: Boolean, val msg: String?, val draftSaved: Boolean = false) : RecurringDialog
}

@OptIn(ExperimentalTime::class)
/** Serializable so a draft restores the wizard as left; the transient fields are per-visit. */
@Serializable
internal data class RecurringDepositState(
    val clientId: Int = -1,
    val step: RecurringStep = RecurringStep.DETAILS,

    val numbers: Map<RecurringField, String> = emptyMap(),
    val selections: Map<RecurringField, RecurringOption> = emptyMap(),
    val toggles: Map<RecurringToggle, Boolean> = emptyMap(),
    @Transient val errors: Set<RecurringField> = emptySet(),
    val charges: List<RecurringChargeRow> = emptyList(),

    val externalId: String = "",
    val submittedOn: Long = Clock.System.now().toEpochMilliseconds(),
    @Transient val isDatePickerOpen: Boolean = false,

    @Transient val isSubmitting: Boolean = false,
    @Transient val dialogState: RecurringDialog? = null,
) {
    fun number(field: RecurringField): String = numbers[field].orEmpty()

    fun selectionId(field: RecurringField): Int? = selections[field]?.id

    fun toggle(toggle: RecurringToggle): Boolean = toggles[toggle] == true

    fun int(field: RecurringField): Int? = number(field).trim().toIntOrNull()

    fun double(field: RecurringField): Double? = number(field).trim().toDoubleOrNull()

    val canAdvance: Boolean get() = !isSubmitting
}

internal sealed interface RecurringDepositEvent {
    data object NavigateBack : RecurringDepositEvent

    /** The application landed — leave, so the caller behind re-reads. */
    data object Done : RecurringDepositEvent
}

internal sealed interface RecurringDepositAction {
    data object ResumeDraft : RecurringDepositAction
    data object DiscardDraft : RecurringDepositAction
    data object SaveDraft : RecurringDepositAction
    data object NavigateBack : RecurringDepositAction
    data object Done : RecurringDepositAction
    data object OnRetry : RecurringDepositAction
    data object OnNext : RecurringDepositAction
    data object OnBack : RecurringDepositAction
    data object OnSubmit : RecurringDepositAction
    data object CloseDialog : RecurringDepositAction
    data object OpenDatePicker : RecurringDepositAction
    data object CloseDatePicker : RecurringDepositAction
    data class OnOptionSelected(
        val field: RecurringField,
        val option: RecurringOption,
    ) : RecurringDepositAction
    data class OnNumberChanged(val field: RecurringField, val value: String) : RecurringDepositAction
    data class OnExternalIdChanged(val value: String) : RecurringDepositAction
    data class OnToggled(val toggle: RecurringToggle, val selected: Boolean) : RecurringDepositAction
    data class OnDateChanged(val millis: Long) : RecurringDepositAction
    data class OnChargeAdded(val charge: RecurringChargeRow) : RecurringDepositAction
    data class OnChargeRemoved(val index: Int) : RecurringDepositAction
}
