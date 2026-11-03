/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.application

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.store.screen.mapContent
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.FormDraftSlot
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.store.submit.draft.isOffline
import kpt.core.base.store.submit.draft.offlineWhenUnsent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.savings.SavingsAccountRepository
import kpt.core.data.savings.SavingsCommandRepository
import kpt.core.model.draft.CreateForm
import kpt.core.model.objects.account.saving.SavingsApplicationInput
import kpt.core.model.objects.account.saving.SavingsChargeInput
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.savings.SavingProductsTemplate
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * The new savings application — ported from `6b66e8a43:feature/savings/savingsAccountv2/`
 * (a 736-line ViewModel, a 374-line screen and four pages totalling 795 lines).
 *
 * The original ships TWO savings create screens, `savingsAccount/` and `savingsAccountv2/`, the
 * same duplicate-create shape the loan vertical had. `v2` is the live one.
 *
 * ## The group path was accepted by the route and dropped by the ViewModel
 *
 * `SavingsAccountRoute(groupId: Int, clientId: Int, isGroupAccount: Boolean)` — and the ViewModel
 * read exactly one of the three:
 *
 * ```kotlin
 * SavingsAccountState(clientId = savedStateHandle.toRoute<SavingsAccountRoute>().clientId)
 * ```
 *
 * `groupId` and `isGroupAccount` appear nowhere else in the file. `SavingsPayload` HAS a `groupId`
 * member and the builder never set it, so a group savings application posted with a client id and
 * no group — the group path could not work at all. Same accepted-then-discarded class as the group
 * loan's External Id and the loan wizard's charges, but on the IDENTITY of the account.
 *
 * Fixed by collapsing to [ownerId] + [isGroup]: one id and one discriminator, so there is no second
 * id to forget. The discriminator also picks which Fineract TEMPLATE endpoint the form reads — the
 * original read neither per-owner template, though both exist and are cached.
 *
 * ## The date and its declared format disagreed
 *
 * ```kotlin
 * dateFormat = ApiDateFormatter.DATE_FORMAT                            // "dd MMMM yyyy"
 * submittedOnDate = state.submissionDate                               // "dd-MM-yyyy"
 * //                └─ DateHelper.getDateAsStringFromLong(...)
 * ```
 *
 * Occurrence **eight** of this class in the migration, and back to the ordinary variant: a real
 * date in the wrong format, rather than the raw epoch millis the approval and activation screens
 * sent. Both halves now come from [ApiDateFormatter].
 *
 * ## Unlike the loan wizard, the Charges page is REAL
 *
 * `SavingsPayload` does carry `charges`, and the original's builder did map them
 * (`charges = state.addedCharges.map { ChargesPayload(chargeId, amount) }`). So this page stays —
 * which is worth stating, because the loan wizard's identical-looking page was discarded on submit
 * and had to be removed. The difference is in the payload, not the UI.
 *
 * What the original did NOT do is give each charge the date/format pair it sets on the application
 * itself; the mapper supplies both from the same helper.
 *
 * ## Indices became a keyed map, as everywhere else
 *
 * Eight parallel `Int = -1` dropdown positions (`savingsProductSelected`, `fieldOfficerIndex`,
 * `interestCompPeriodIndex`, …). The original read them with `getOrNull`, so unlike the loan
 * wizard it did not CRASH — but a stale index still survived a product change, selecting a
 * different option from the new product's list. [selections] is keyed and cleared with the product.
 */
@OptIn(ExperimentalTime::class)
internal class SavingsApplicationViewModel(
    private val repository: SavingsAccountRepository,
    private val commands: SavingsCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<SavingsApplicationState, SavingsApplicationEvent, SavingsApplicationAction>(
    SavingsApplicationState(
        ownerId = savedStateHandle.get<Int>(OWNER_ID_KEY) ?: -1,
        isGroup = savedStateHandle.get<Boolean>(IS_GROUP_KEY) == true,
    ),
) {

    /**
     * This owner's application that did not land — the officer's input (the template is re-fetched).
     * The owner KIND is part of the slot: client 7's and group 7's applications are different drafts.
     */
    private val formDraft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.SAVINGS.key,
        ownerId = state.ownerId.toLong(),
        SavingsFormSnapshot.serializer(),
        SavingsApplicationInput.serializer(),
        ownerKind = if (state.isGroup) "group" else "client",
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered

    private var lastInput: SavingsApplicationInput? = null

    private fun keepDraft(input: SavingsApplicationInput, failure: String?) = formDraft.keep(
        title = state.selections[SavingsField.PRODUCT]?.label.orEmpty(),
        state = SavingsFormSnapshot.of(state),
        payload = input,
        failure = failure,
    )

    private val productsStream = repository.savingsProductsStream(viewModelScope)

    /** The tenant's savings products. Choosing one is what starts the form. */
    val products: StateFlow<ScreenState<List<SavingsProductOption>>> = productsStream.state
        .mapContent { products, _ ->
            products.mapNotNull { product ->
                // A product with no id cannot be applied for, so it is not offered.
                SavingsProductOption(
                    id = product.id ?: return@mapNotNull null,
                    name = product.name.orEmpty(),
                )
            }
        }
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: SavingsApplicationAction) {
        when (action) {
            SavingsApplicationAction.ResumeDraft,
            SavingsApplicationAction.DiscardDraft,
            SavingsApplicationAction.SaveDraft,
            -> handleDraftAction(action)

            SavingsApplicationAction.NavigateBack ->
                sendEvent(SavingsApplicationEvent.NavigateBack)

            SavingsApplicationAction.Done -> sendEvent(SavingsApplicationEvent.Done)
            SavingsApplicationAction.OnRetryProducts -> productsStream.refresh(forceFresh = true)
            SavingsApplicationAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            SavingsApplicationAction.OnRetryTemplate ->
                state.selectionId(SavingsField.PRODUCT)?.let(::loadTemplate)

            is SavingsApplicationAction.OnProductSelected,
            is SavingsApplicationAction.OnOptionSelected,
            is SavingsApplicationAction.OnNumberChanged,
            is SavingsApplicationAction.OnExternalIdChanged,
            is SavingsApplicationAction.OnToggled,
            is SavingsApplicationAction.OnDateChanged,
            SavingsApplicationAction.OpenDatePicker,
            SavingsApplicationAction.CloseDatePicker,
            is SavingsApplicationAction.OnChargeAdded,
            is SavingsApplicationAction.OnChargeRemoved,
            -> handleFormEdit(action)

            SavingsApplicationAction.OnNext -> advance()
            SavingsApplicationAction.OnBack -> retreat()
            SavingsApplicationAction.OnSubmit -> submit()
        }
    }

    private fun handleDraftAction(action: SavingsApplicationAction) {
        when (action) {
            SavingsApplicationAction.ResumeDraft -> formDraft.resume()?.let { restored ->
                val productId = restored.selections[SavingsField.PRODUCT]?.id ?: return@let
                mutableStateFlow.value = state.copy(
                    selections = restored.selections,
                    template = null,
                    step = SavingsStep.DETAILS,
                )
                loadTemplate(productId, restore = restored)
            }
            SavingsApplicationAction.DiscardDraft -> formDraft.discard()
            SavingsApplicationAction.SaveDraft -> lastInput?.let { input ->
                val failed = state.dialogState as? ApplicationDialog.Failed
                keepDraft(input, failure = failed?.msg ?: "Not accepted by the server.")
                mutableStateFlow.value = state.copy(dialogState = failed?.copy(draftSaved = true))
            }
            else -> Unit
        }
    }

    private fun handleFormEdit(action: SavingsApplicationAction) {
        when (action) {
            is SavingsApplicationAction.OnProductSelected -> {
                // The WHOLE selection map clears: every option list in it came from the previous
                // product's template.
                mutableStateFlow.value = state.copy(
                    selections = mapOf(SavingsField.PRODUCT to action.product.asOption()),
                    numbers = emptyMap(),
                    charges = emptyList(),
                    errors = emptySet(),
                    template = null,
                )
                loadTemplate(action.product.id)
            }

            is SavingsApplicationAction.OnOptionSelected -> mutableStateFlow.value = state.copy(
                selections = state.selections + (action.field to action.option),
                errors = state.errors - action.field,
            )

            is SavingsApplicationAction.OnNumberChanged -> mutableStateFlow.value = state.copy(
                numbers = state.numbers + (action.field to action.value),
                errors = state.errors - action.field,
            )

            is SavingsApplicationAction.OnExternalIdChanged ->
                mutableStateFlow.value = state.copy(externalId = action.value)

            is SavingsApplicationAction.OnToggled ->
                mutableStateFlow.value =
                    state.copy(toggles = state.toggles + (action.toggle to action.selected))

            is SavingsApplicationAction.OnDateChanged ->
                mutableStateFlow.value = state.copy(submittedOn = action.millis)

            SavingsApplicationAction.OpenDatePicker ->
                mutableStateFlow.value = state.copy(isDatePickerOpen = true)

            SavingsApplicationAction.CloseDatePicker ->
                mutableStateFlow.value = state.copy(isDatePickerOpen = false)

            is SavingsApplicationAction.OnChargeAdded ->
                mutableStateFlow.value =
                    state.copy(charges = state.charges + action.charge)

            is SavingsApplicationAction.OnChargeRemoved ->
                mutableStateFlow.value =
                    state.copy(charges = state.charges.filterIndexed { i, _ -> i != action.index })

            else -> Unit
        }
    }

    /** [restore] = a resumed draft's input, which wins over the template's defaults. */
    private fun loadTemplate(productId: Int, restore: SavingsFormSnapshot? = null) {
        mutableStateFlow.value = state.copy(isLoadingTemplate = true, templateError = false)

        viewModelScope.launch {
            // The OWNER decides the endpoint, not just a payload field.
            val template = repository.savingsApplicationTemplate(
                ownerId = state.ownerId,
                productId = productId,
                isGroup = state.isGroup,
            )

            mutableStateFlow.value = if (template == null) {
                state.copy(isLoadingTemplate = false, templateError = true)
            } else {
                state.copy(
                    isLoadingTemplate = false,
                    templateError = false,
                    template = template,
                    selections = state.selections + template.defaultSelections() + restore?.selections.orEmpty(),
                    numbers = state.numbers + restore?.numbers.orEmpty(),
                    toggles = state.toggles + restore?.toggles.orEmpty(),
                    charges = restore?.charges ?: state.charges,
                    externalId = restore?.externalId ?: state.externalId,
                    submittedOn = restore?.submittedOn ?: state.submittedOn,
                )
            }
        }
    }

    /** Validate THIS step, then move on. The original's `moveToNextStep` validated nothing. */
    private fun advance() {
        val missing = state.step.missing(state)
        mutableStateFlow.value = state.copy(errors = missing)
        if (missing.isNotEmpty()) return

        SavingsStep.entries.getOrNull(state.step.ordinal + 1)
            ?.let { mutableStateFlow.value = state.copy(step = it) }
    }

    private fun retreat() {
        // Floored at the first step; the original computed `currentStep - 1` with no guard.
        SavingsStep.entries.getOrNull(state.step.ordinal - 1)
            ?.let { mutableStateFlow.value = state.copy(step = it) }
            ?: sendEvent(SavingsApplicationEvent.NavigateBack)
    }

    private fun submit() {
        if (state.isSubmitting) return

        // Every step re-validated: the officer can walk BACK and empty a field.
        val missing = SavingsStep.entries.flatMap { it.missing(state) }.toSet()
        if (missing.isNotEmpty()) {
            mutableStateFlow.value = state.copy(
                errors = missing,
                step = SavingsStep.entries.first { it.missing(state).isNotEmpty() },
            )
        } else {
            state.selectionId(SavingsField.PRODUCT)?.let(::sendApplication)
        }
    }

    private fun sendApplication(productId: Int) {
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            val input = SavingsApplicationInput(
                // ONE owner and ONE discriminator — see the class KDoc for the path this replaces.
                ownerId = state.ownerId,
                isGroup = state.isGroup,
                productId = productId,
                // The date and the format from the same helper. The original paired
                // `DateHelper.getDateAsStringFromLong` (dd-MM-yyyy) with
                // `ApiDateFormatter.DATE_FORMAT` (dd MMMM yyyy).
                submittedOnDate = ApiDateFormatter.formatForApi(state.submittedOn),
                locale = ApiDateFormatter.LOCALE,
                dateFormat = ApiDateFormatter.DATE_FORMAT,
                fieldOfficerId = state.selectionId(SavingsField.FIELD_OFFICER),
                externalId = state.externalId.trim().takeIf { it.isNotBlank() },
                minRequiredOpeningBalance = state.number(SavingsField.OPENING_BALANCE)
                    .trim().takeIf { it.isNotBlank() },
                minRequiredBalance = state.number(SavingsField.MINIMUM_BALANCE)
                    .trim().takeIf { it.isNotBlank() },
                lockinPeriodFrequency = state.int(SavingsField.LOCKIN_PERIOD),
                // Sent only alongside a frequency — a unit with no number names nothing. The
                // original's own `takeIf`, kept.
                lockinPeriodFrequencyType = state.selectionId(SavingsField.LOCKIN_PERIOD_TYPE)
                    .takeIf { state.int(SavingsField.LOCKIN_PERIOD) != null },
                interestCompoundingPeriodType = state.selectionId(SavingsField.COMPOUNDING_PERIOD),
                interestPostingPeriodType = state.selectionId(SavingsField.POSTING_PERIOD),
                interestCalculationType = state.selectionId(SavingsField.CALCULATION_TYPE),
                interestCalculationDaysInYearType = state.selectionId(SavingsField.DAYS_IN_YEAR),
                allowOverdraft = state.toggle(SavingsToggle.ALLOW_OVERDRAFT),
                enforceMinRequiredBalance = state.toggle(SavingsToggle.ENFORCE_MIN_BALANCE),
                charges = state.charges.map {
                    SavingsChargeInput(chargeId = it.chargeId, amount = it.amount)
                },
            )

            lastInput = input
            val result = commands.createSavingsAccount(input).offlineWhenUnsent()
            // Offline: kept automatically and sent on reconnect.
            if (result.isOffline) keepDraft(input, failure = null)
            if (result is MutationResult.Applied) formDraft.clear()
            mutableStateFlow.value = when (result) {
                is MutationResult.Applied ->
                    state.copy(isSubmitting = false, dialogState = ApplicationDialog.Created)

                is MutationResult.Blocked -> state.copy(
                    isSubmitting = false,
                    dialogState = ApplicationDialog.Failed(
                        offline = result.reason == BlockReason.OFFLINE,
                        msg = null,
                        draftSaved = result.isOffline,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create savings failed" }
                    state.copy(
                        isSubmitting = false,
                        dialogState = ApplicationDialog.Failed(
                            offline = false,
                            msg = result.cause.serverErrorMessage(),
                        ),
                    )
                }

                is MutationResult.Conflicted -> state.copy(
                    isSubmitting = false,
                    dialogState = ApplicationDialog.Failed(offline = false, msg = null),
                )
            }
        }
    }

    private companion object {
        const val TAG = "SavingsApplication"
        const val OWNER_ID_KEY = "ownerId"
        const val IS_GROUP_KEY = "isGroup"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/** Parses and is not negative — a zero opening balance or lock-in is meaningful. */
private fun String.isNonNegativeOrBlank(): Boolean {
    val t = trim()
    return t.isEmpty() || (t.toDoubleOrNull()?.let { it >= 0 } == true)
}

/**
 * The product's own chosen option for each interest field, matched against its option list.
 *
 * `firstOrNull` on an id match, so a miss omits the field rather than storing a position. The
 * original held indices and read them with `getOrNull` — safe from crashing, but a stale index
 * survived a product change and selected a different option from the new list.
 */
private fun SavingProductsTemplate.defaultSelections(): Map<SavingsField, SavingsOption> = buildMap {
    interestCompoundingPeriodTypeOptions
        .orEmpty()
        .firstOrNull { it.id == interestCompoundingPeriodType?.id }
        ?.let { o -> o.id?.let { put(SavingsField.COMPOUNDING_PERIOD, SavingsOption(it, o.value.orEmpty())) } }
    interestPostingPeriodTypeOptions
        .orEmpty()
        .firstOrNull { it.id == interestPostingPeriodType?.id }
        ?.let { o -> o.id?.let { put(SavingsField.POSTING_PERIOD, SavingsOption(it, o.value.orEmpty())) } }
    interestCalculationTypeOptions
        .orEmpty()
        .firstOrNull { it.id == interestCalculationType?.id }
        ?.let { o -> o.id?.let { put(SavingsField.CALCULATION_TYPE, SavingsOption(it, o.value.orEmpty())) } }
    interestCalculationDaysInYearTypeOptions
        .orEmpty()
        .firstOrNull { it.id == interestCalculationDaysInYearType?.id }
        ?.let { o -> o.id?.let { put(SavingsField.DAYS_IN_YEAR, SavingsOption(it, o.value.orEmpty())) } }
}

/** A selectable savings product. */
internal data class SavingsProductOption(val id: Int, val name: String)

internal fun SavingsProductOption.asOption() = SavingsOption(id = id, label = name)

/** One dropdown choice. */
@Serializable
internal data class SavingsOption(val id: Int, val label: String)

/** A charge the officer attached, before it becomes a payload row. */
@Serializable
internal data class SavingsChargeRow(val chargeId: Int, val label: String, val amount: String)

/** The checkboxes. */
internal enum class SavingsToggle { ALLOW_OVERDRAFT, ENFORCE_MIN_BALANCE }

/** Every field that can carry a value or an error, keyed so prefill/screen/validation agree. */
internal enum class SavingsField {
    PRODUCT,
    FIELD_OFFICER,
    OPENING_BALANCE,
    MINIMUM_BALANCE,
    LOCKIN_PERIOD,
    LOCKIN_PERIOD_TYPE,
    COMPOUNDING_PERIOD,
    POSTING_PERIOD,
    CALCULATION_TYPE,
    DAYS_IN_YEAR,
    ;

    companion object {
        /** Optional, but must parse and be >= 0 when filled. */
        val optionalNumbers = listOf(OPENING_BALANCE, MINIMUM_BALANCE, LOCKIN_PERIOD)

        /** The four interest fields Fineract requires on a savings application. */
        val requiredSelections = listOf(
            COMPOUNDING_PERIOD,
            POSTING_PERIOD,
            CALCULATION_TYPE,
            DAYS_IN_YEAR,
        )
    }
}

/** The wizard's steps, each owning the fields it validates. */
internal enum class SavingsStep {
    DETAILS {
        override fun missing(state: SavingsApplicationState) = buildSet {
            if (state.selections[SavingsField.PRODUCT] == null) add(SavingsField.PRODUCT)
        }
    },
    TERMS {
        override fun missing(state: SavingsApplicationState) = buildSet {
            SavingsField.optionalNumbers.forEach {
                if (!state.number(it).isNonNegativeOrBlank()) add(it)
            }
            SavingsField.requiredSelections.forEach {
                if (state.selections[it] == null) add(it)
            }
        }
    },

    /** Charges are optional; each row was validated as it was added. */
    CHARGES {
        override fun missing(state: SavingsApplicationState) = emptySet<SavingsField>()
    },
    PREVIEW {
        override fun missing(state: SavingsApplicationState) = emptySet<SavingsField>()
    },
    ;

    abstract fun missing(state: SavingsApplicationState): Set<SavingsField>
}

internal sealed interface ApplicationDialog {
    data object Created : ApplicationDialog

    /** [draftSaved] = kept (offline automatically, or "Save as draft"), in Sync & Drafts. */
    data class Failed(val offline: Boolean, val msg: String?, val draftSaved: Boolean = false) : ApplicationDialog
}

@OptIn(ExperimentalTime::class)
internal data class SavingsApplicationState(
    val ownerId: Int = -1,
    /** A GROUP application, not a client one — picks the owner field AND the template endpoint. */
    val isGroup: Boolean = false,
    val step: SavingsStep = SavingsStep.DETAILS,
    val template: SavingProductsTemplate? = null,

    val numbers: Map<SavingsField, String> = emptyMap(),
    val selections: Map<SavingsField, SavingsOption> = emptyMap(),
    val toggles: Map<SavingsToggle, Boolean> = emptyMap(),
    val errors: Set<SavingsField> = emptySet(),
    val charges: List<SavingsChargeRow> = emptyList(),

    val externalId: String = "",
    val submittedOn: Long = Clock.System.now().toEpochMilliseconds(),
    val isDatePickerOpen: Boolean = false,

    val isLoadingTemplate: Boolean = false,
    val templateError: Boolean = false,
    val isSubmitting: Boolean = false,
    val dialogState: ApplicationDialog? = null,
) {
    fun number(field: SavingsField): String = numbers[field].orEmpty()

    fun selectionId(field: SavingsField): Int? = selections[field]?.id

    fun toggle(toggle: SavingsToggle): Boolean = toggles[toggle] == true

    fun int(field: SavingsField): Int? = number(field).trim().toIntOrNull()

    val canAdvance: Boolean get() = !isLoadingTemplate && !isSubmitting
}

internal sealed interface SavingsApplicationEvent {
    data object NavigateBack : SavingsApplicationEvent

    /** The application landed — leave, so the caller behind re-reads. */
    data object Done : SavingsApplicationEvent
}

internal sealed interface SavingsApplicationAction {
    data object ResumeDraft : SavingsApplicationAction
    data object DiscardDraft : SavingsApplicationAction
    data object SaveDraft : SavingsApplicationAction
    data object NavigateBack : SavingsApplicationAction
    data object Done : SavingsApplicationAction
    data object OnRetryProducts : SavingsApplicationAction
    data object OnRetryTemplate : SavingsApplicationAction
    data object OnNext : SavingsApplicationAction
    data object OnBack : SavingsApplicationAction
    data object OnSubmit : SavingsApplicationAction
    data object CloseDialog : SavingsApplicationAction
    data object OpenDatePicker : SavingsApplicationAction
    data object CloseDatePicker : SavingsApplicationAction
    data class OnProductSelected(val product: SavingsProductOption) : SavingsApplicationAction
    data class OnOptionSelected(
        val field: SavingsField,
        val option: SavingsOption,
    ) : SavingsApplicationAction
    data class OnNumberChanged(val field: SavingsField, val value: String) : SavingsApplicationAction
    data class OnExternalIdChanged(val value: String) : SavingsApplicationAction
    data class OnToggled(val toggle: SavingsToggle, val selected: Boolean) : SavingsApplicationAction
    data class OnDateChanged(val millis: Long) : SavingsApplicationAction
    data class OnChargeAdded(val charge: SavingsChargeRow) : SavingsApplicationAction
    data class OnChargeRemoved(val index: Int) : SavingsApplicationAction
}

/** What a savings draft keeps: the officer's input across the steps — not the server's template. */
@Serializable
internal data class SavingsFormSnapshot(
    val numbers: Map<SavingsField, String> = emptyMap(),
    val selections: Map<SavingsField, SavingsOption> = emptyMap(),
    val toggles: Map<SavingsToggle, Boolean> = emptyMap(),
    val charges: List<SavingsChargeRow> = emptyList(),
    val externalId: String = "",
    val submittedOn: Long? = null,
) {
    companion object {
        fun of(state: SavingsApplicationState) = SavingsFormSnapshot(
            state.numbers,
            state.selections,
            state.toggles,
            state.charges,
            state.externalId,
            state.submittedOn,
        )
    }
}
