/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.newloan

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
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.data.loan.LoanRepository
import kpt.core.model.draft.CreateForm
import kpt.core.model.loan.LoanTemplate
import kpt.core.model.objects.account.loan.LoanApplicationInput
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * The new INDIVIDUAL loan application — carried over from
 * `6b66e8a43:feature/loan/newLoanAccount/` (a 1,208-line ViewModel, a 538-line screen and five
 * pages totalling 1,520 lines).
 *
 * Reached from the client's loan-accounts list ("add account"), whose whole chain was already
 * ported and correctly `null`-gated at `ClientNavigation.kt` — wiring it is one line, per spec §6a.
 *
 * ## Four steps, not five — the Charges page carried nothing
 *
 * `LoansPayload` has **no `charges` and no `collateral` member** — not in the fork, not at
 * `6b66e8a43`, and `CreateLoanAccountUseCase` is a straight passthrough that adds neither. Both of
 * the original's payload builders were checked: neither reads `state.addedCharges` or
 * `state.addedCollaterals`. So the entire Charges page and the add/edit/delete collateral dialog
 * cluster were **dead inputs** — an officer attached charges and collateral to an application and
 * every one was discarded on submit, with a successful-looking result.
 *
 * Absent, not inert, the same rule the unported nav affordances follow: this wizard has four steps
 * (Details · Terms · Schedule · Preview) and no collateral management. Tracked in CAPABILITY_GAPS
 * as `new-loan-charges-and-collateral-were-discarded` — restoring them needs the payload members
 * first, which is a `core/model` change plus proof of the server's shape, not a UI change.
 *
 * ## One payload builder, because the original's two had DRIFTED
 *
 * `submitLoanApplication()` and `buildLoansPayloadForSchedulePreview()` were near-duplicates, and
 * the difference was not cosmetic:
 *
 * | | preview builder | submit builder |
 * |---|---|---|
 * | option lookup | `?.getOrNull(index)?.id` — safe | `?.options[index]?.id` — **throws on -1** |
 * | interest rate | `.toDoubleOrNull()` — safe | `.toDouble()` — **throws on non-numeric** |
 *
 * Someone hardened the preview copy and never backported it, so the crashes survived on exactly
 * the path that matters: preview the schedule fine, then crash on submit. [payload] is the single
 * builder, which is what makes that divergence unexpressible.
 *
 * ## Twelve parallel indices became one keyed map
 *
 * The original held `loanOfficerIndex`, `fundIndex`, `termFrequencyIndex` … — twelve
 * `Int = -1` fields holding dropdown POSITIONS, with its own comment conceding "it is not actual
 * value for the field". Two consequences, both real:
 *
 *  * **-1 reaches the payload.** Six of the submit builder's lookups had no `-1` guard, so an
 *    officer who never opened (say) the amortization dropdown crashed on submit. `indexOfFirst`
 *    in the template prefill also yields -1 when it finds no match, so the crash was reachable
 *    without touching anything.
 *  * **Stale indices survive a product change.** `loadLoanAccountTemplate` reset six indices and
 *    left the other six, so position 3 of the previous product's fund list was submitted against
 *    the new product's — a different fund, or out of bounds.
 *
 * [selections] is `Map<LoanField, LoanOption>` holding the id (and, for the repayment strategy, the
 * CODE the payload actually wants). An unchosen field is absent from the map, so "unchosen" and
 * "position -1" cannot be confused, and the whole map is cleared when the product changes.
 *
 * ## Every step validates — the original's "Next" did nothing
 *
 * `moveToNextStep()` incremented `currentStep` with no checks whatsoever, so the wizard walked
 * from an empty form to submit. Each [LoanStep] now declares its own required fields and Next
 * validates only those, so an error is reported on the step that owns it rather than at the end.
 */
@OptIn(ExperimentalTime::class)
internal class NewLoanApplicationViewModel(
    private val repository: LoanRepository,
    private val commands: LoanCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<NewLoanApplicationState, NewLoanApplicationEvent, NewLoanApplicationAction>(
    NewLoanApplicationState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    /**
     * This client's application that did not land. The draft keeps the officer's INPUT only; the
     * product template is fetched again on resume (it is the server's, and may have changed).
     */
    private val draft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.LOAN.key,
        ownerId = state.clientId.toLong(),
        LoanFormSnapshot.serializer(),
        LoanApplicationInput.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = draft.offered

    private var lastPayload: LoanApplicationInput? = null

    private fun keepDraft(payload: LoanApplicationInput, failure: String?) = draft.keep(
        title = state.selections[LoanField.PRODUCT]?.label.orEmpty(),
        state = LoanFormSnapshot.of(state),
        payload = payload,
        failure = failure,
    )

    private val productsStream = repository.loanProductsStream(viewModelScope)

    /** The tenant's loan products. Choosing one is what starts the form. */
    val products: StateFlow<ScreenState<List<LoanProductOption>>> = productsStream.state
        .mapContent { products, _ ->
            products.mapNotNull { product ->
                // A product with no id cannot be applied for, so it is not offered.
                LoanProductOption(id = product.id ?: return@mapNotNull null, name = product.name.orEmpty())
            }
        }
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: NewLoanApplicationAction) {
        when (action) {
            NewLoanApplicationAction.NavigateBack -> sendEvent(NewLoanApplicationEvent.NavigateBack)
            NewLoanApplicationAction.OnRetryProducts -> productsStream.retry()
            NewLoanApplicationAction.CloseDialog -> mutableStateFlow.value = state.copy(dialogState = null)

            NewLoanApplicationAction.OnRetryTemplate ->
                state.selectionId(LoanField.PRODUCT)?.let(::loadTemplate)

            NewLoanApplicationAction.ResumeDraft,
            NewLoanApplicationAction.DiscardDraft,
            NewLoanApplicationAction.SaveDraft,
            -> handleDraftAction(action)

            is NewLoanApplicationAction.OnProductSelected,
            is NewLoanApplicationAction.OnOptionSelected,
            is NewLoanApplicationAction.OnNumberChanged,
            is NewLoanApplicationAction.OnExternalIdChanged,
            is NewLoanApplicationAction.OnToggled,
            is NewLoanApplicationAction.OnDateChanged,
            is NewLoanApplicationAction.OpenDatePicker,
            NewLoanApplicationAction.CloseDatePicker,
            -> handleFormEdit(action)

            NewLoanApplicationAction.OnNext -> advance()
            NewLoanApplicationAction.OnBack -> retreat()
            NewLoanApplicationAction.OnRetrySchedule -> previewSchedule()
            NewLoanApplicationAction.OnSubmit -> submit()
        }
    }

    private fun handleDraftAction(action: NewLoanApplicationAction) {
        when (action) {
            NewLoanApplicationAction.ResumeDraft -> draft.resume()?.let { restored ->
                val productId = restored.selections[LoanField.PRODUCT]?.id ?: return@let
                mutableStateFlow.value = state.copy(
                    selections = restored.selections,
                    template = null,
                    schedule = null,
                    step = LoanStep.DETAILS,
                )
                loadTemplate(productId, restore = restored)
            }
            NewLoanApplicationAction.DiscardDraft -> draft.discard()
            NewLoanApplicationAction.SaveDraft -> lastPayload?.let { payload ->
                val failed = state.dialogState as? NewLoanApplicationState.DialogState.Failed
                keepDraft(payload, failure = failed?.msg ?: FAILED_MSG)
                mutableStateFlow.value = state.copy(dialogState = failed?.copy(draftSaved = true))
            }
            else -> Unit
        }
    }

    private fun handleFormEdit(action: NewLoanApplicationAction) {
        when (action) {
            is NewLoanApplicationAction.OnProductSelected -> {
                // Clearing the WHOLE map, not the six the original reset: every option list in it
                // came from the previous product's template.
                mutableStateFlow.value = state.copy(
                    selections = mapOf(LoanField.PRODUCT to action.product.asOption()),
                    numbers = emptyMap(),
                    errors = emptySet(),
                    template = null,
                    schedule = null,
                )
                loadTemplate(action.product.id)
            }

            is NewLoanApplicationAction.OnOptionSelected -> mutableStateFlow.value = state.copy(
                selections = state.selections + (action.field to action.option),
                errors = state.errors - action.field,
                // Any change to the terms invalidates a schedule already previewed.
                schedule = if (action.field.affectsSchedule) null else state.schedule,
            )

            is NewLoanApplicationAction.OnNumberChanged -> mutableStateFlow.value = state.copy(
                numbers = state.numbers + (action.field to action.value),
                errors = state.errors - action.field,
                schedule = if (action.field.affectsSchedule) null else state.schedule,
            )

            is NewLoanApplicationAction.OnExternalIdChanged ->
                mutableStateFlow.value = state.copy(externalId = action.value)

            is NewLoanApplicationAction.OnToggled -> mutableStateFlow.value = state.copy(
                toggles = state.toggles + (action.toggle to action.selected),
                schedule = null,
            )

            is NewLoanApplicationAction.OnDateChanged -> mutableStateFlow.value = state.copy(
                dates = state.dates + (action.field to action.millis),
                schedule = null,
            )

            is NewLoanApplicationAction.OpenDatePicker ->
                mutableStateFlow.value = state.copy(activeDatePicker = action.field)

            NewLoanApplicationAction.CloseDatePicker ->
                mutableStateFlow.value = state.copy(activeDatePicker = null)

            else -> Unit
        }
    }

    /** [restore] = a resumed draft's input, which wins over the template's defaults. */
    private fun loadTemplate(productId: Int, restore: LoanFormSnapshot? = null) {
        mutableStateFlow.value = state.copy(isLoadingTemplate = true, templateError = false)

        viewModelScope.launch {
            val template = repository.loanTemplate(state.clientId, productId)

            mutableStateFlow.value = if (template == null) {
                state.copy(isLoadingTemplate = false, templateError = true)
            } else {
                state.copy(
                    isLoadingTemplate = false,
                    templateError = false,
                    template = template,
                    numbers = template.defaultNumbers() + restore?.numbers.orEmpty(),
                    // Prefilled by matching the template's OWN chosen value against its option
                    // list. `firstOrNull`, never `indexOfFirst` — a miss leaves the field unchosen
                    // instead of storing -1, which is what made the original's crash reachable
                    // without the officer touching anything.
                    selections = state.selections + template.defaultSelections() + restore?.selections.orEmpty(),
                    dates = state.dates + restore?.dates.orEmpty(),
                    toggles = state.toggles + restore?.toggles.orEmpty(),
                    externalId = restore?.externalId ?: state.externalId,
                )
            }
        }
    }

    /** Validate THIS step, then move on — or preview the schedule when entering that step. */
    private fun advance() {
        val missing = state.step.missing(state)
        mutableStateFlow.value = state.copy(errors = missing)
        if (missing.isNotEmpty()) return

        val next = LoanStep.entries.getOrNull(state.step.ordinal + 1) ?: return
        mutableStateFlow.value = state.copy(step = next)
        if (next == LoanStep.SCHEDULE) previewSchedule()
    }

    private fun retreat() {
        // Floored at the first step. The original computed `currentStep - 1` with no guard.
        LoanStep.entries.getOrNull(state.step.ordinal - 1)
            ?.let { mutableStateFlow.value = state.copy(step = it) }
            ?: sendEvent(NewLoanApplicationEvent.NavigateBack)
    }

    private fun previewSchedule() {
        val payload = payload() ?: return
        mutableStateFlow.value = state.copy(isLoadingSchedule = true, scheduleError = null)

        viewModelScope.launch {
            when (val result = commands.calculateLoanSchedule(payload)) {
                is MutationResult.Applied -> mutableStateFlow.value = state.copy(
                    isLoadingSchedule = false,
                    scheduleError = null,
                    schedule = result.value,
                )

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isLoadingSchedule = false,
                    // A preview needs the server to compute it; offline is a distinct message
                    // rather than "could not calculate".
                    scheduleError = if (result.reason == BlockReason.OFFLINE) {
                        ScheduleError.Offline
                    } else {
                        ScheduleError.Failed
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "schedule preview failed" }
                    mutableStateFlow.value = state.copy(
                        isLoadingSchedule = false,
                        // The server REJECTED these terms, which is information the officer needs
                        // on the Terms step — not a replaced screen. The original swapped the
                        // whole wizard for an error page, losing a form four pages deep.
                        scheduleError = ScheduleError.Rejected(result.cause.serverErrorMessage()),
                    )
                }

                is MutationResult.Conflicted ->
                    mutableStateFlow.value =
                        state.copy(isLoadingSchedule = false, scheduleError = ScheduleError.Failed)
            }
        }
    }

    private fun submit() {
        if (state.isSubmitting) return

        // Every step re-validated, not only the last: the officer can walk BACK and empty a field.
        val missing = LoanStep.entries.flatMap { it.missing(state) }.toSet()
        if (missing.isNotEmpty()) {
            mutableStateFlow.value = state.copy(
                errors = missing,
                // Land on the earliest step that owns a missing field, so the error is visible.
                step = LoanStep.entries.first { step -> step.missing(state).isNotEmpty() },
            )
        } else {
            payload()?.let(::sendApplication)
        }
    }

    private fun sendApplication(payload: LoanApplicationInput) {
        lastPayload = payload
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            when (val result = commands.createLoanAccount(payload).offlineWhenUnsent()) {
                is MutationResult.Applied -> {
                    draft.clear()
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = NewLoanApplicationState.DialogState.Created,
                    )
                }

                // Offline: kept automatically, sent on reconnect.
                is MutationResult.Blocked -> {
                    if (result.isOffline) keepDraft(payload, failure = null)
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = NewLoanApplicationState.DialogState.Failed(
                            if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                            offline = result.isOffline,
                            draftSaved = result.isOffline,
                        ),
                    )
                }

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create loan failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = NewLoanApplicationState.DialogState.Failed(
                            result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = NewLoanApplicationState.DialogState.Failed(FAILED_MSG),
                )
            }
        }
    }

    /**
     * The ONE payload builder, for both the schedule preview and the submit.
     *
     * Returns null only when the product is unchosen, which the step-1 validation already refuses —
     * so the null is a type-level guard, not a silent exit. Every option read goes through
     * [NewLoanApplicationState.selectionId], which returns null for unchosen rather than indexing a
     * list, and every number through `toDoubleOrNull` / `toIntOrNull`.
     */
    private fun payload(): LoanApplicationInput? {
        val productId = state.selectionId(LoanField.PRODUCT) ?: return null
        val repaymentEvery = state.int(LoanField.REPAID_EVERY)
        val numberOfRepayments = state.int(LoanField.NUMBER_OF_REPAYMENTS)

        return LoanApplicationInput(
            clientId = state.clientId,
            productId = productId,
            // `"individual"` is correct here — this IS the individual application. (The GROUP
            // screen is where the original hardcoded it wrongly; see `grouploan`.)
            loanType = INDIVIDUAL_LOAN_TYPE,
            principal = state.double(LoanField.PRINCIPAL),
            numberOfRepayments = numberOfRepayments,
            interestRatePerPeriod = state.double(LoanField.NOMINAL_INTEREST),
            repaymentEvery = repaymentEvery,
            // The original's own derivation, kept: the term is as long as the repayments take.
            loanTermFrequency = (numberOfRepayments ?: 0) * (repaymentEvery ?: 0),
            loanTermFrequencyType = state.selectionId(LoanField.TERM_FREQUENCY_TYPE),
            // Its OWN dropdown. The original fed this from the TERM frequency's — the identical
            // defect the group loan screen had, and `repaymentFrequencyTypeOptions` exists on the
            // template for exactly this field.
            repaymentFrequencyType = state.selectionId(LoanField.REPAYMENT_FREQUENCY_TYPE),
            repaymentFrequencyDayOfWeekType = state.selectionId(LoanField.REPAYMENT_DAY_OF_WEEK),
            repaymentFrequencyNthDayType = state.selectionId(LoanField.REPAYMENT_NTH_DAY),
            amortizationType = state.selectionId(LoanField.AMORTIZATION),
            interestType = state.selectionId(LoanField.INTEREST_METHOD),
            interestCalculationPeriodType = state.selectionId(LoanField.INTEREST_CALCULATION_PERIOD),
            // A CODE, not an id — the only option field that is, which is why [LoanOption] carries
            // both and the payload reads `selectionCode` here.
            transactionProcessingStrategyCode = state.selectionCode(LoanField.REPAYMENT_STRATEGY),
            allowPartialPeriodInterestCalculation = state.toggle(LoanToggle.INTEREST_PARTIAL_PERIOD),
            loanPurposeId = state.selectionId(LoanField.LOAN_PURPOSE),
            fundId = state.selectionId(LoanField.FUND),
            loanOfficerId = state.selectionId(LoanField.LOAN_OFFICER),
            linkAccountId = state.selectionId(LoanField.LINK_SAVINGS),
            submittedOnDate = ApiDateFormatter.formatForApi(state.date(LoanDate.SUBMITTED_ON)),
            expectedDisbursementDate =
            ApiDateFormatter.formatForApi(state.date(LoanDate.EXPECTED_DISBURSEMENT)),
            // Paired with the formatter above. The original used `DateHelper.SHORT_MONTH`, which
            // happens to agree — but only because BOTH sides were `DateHelper`; pairing them here
            // is what keeps that true when either changes.
            dateFormat = ApiDateFormatter.DATE_FORMAT,
            // `ApiDateFormatter.LOCALE`, not the original's hardcoded `"en"`.
            locale = ApiDateFormatter.LOCALE,
            // Blank is omitted. The original sent `""`, which Fineract stores as an external id of
            // empty string rather than as "no external id".
            externalId = state.externalId.trim().takeIf { it.isNotBlank() },
        )
    }

    private companion object {
        const val TAG = "NewLoanApplication"
        const val CLIENT_ID_KEY = "clientId"

        /** Fineract's `loanType` for a loan attached to a CLIENT. */
        const val INDIVIDUAL_LOAN_TYPE = "individual"

        const val STATE_TIMEOUT_MS = 5_000L
        const val OFFLINE_MSG = "No connection — the application was not submitted."
        const val FAILED_MSG = "Could not submit this loan application."
    }
}

/** Positive and parseable. A principal, term count or rate of zero is not an application. */
private fun String.isPositiveNumber(): Boolean = trim().toDoubleOrNull()?.let { it > 0 } == true

/** Parseable and not negative — a grace period or tolerance of zero is meaningful. */
private fun String.isNonNegativeNumber(): Boolean = trim().toDoubleOrNull()?.let { it >= 0 } == true

/**
 * This PRODUCT's terms, as the form's starting values — the original's prefill, kept.
 *
 * A field the template leaves null starts BLANK rather than at `"0"`. The original defaulted
 * `principalAmount` and `nominalInterestRate` to the string `"0"`, which reads as a deliberate
 * zero and is submittable as one.
 */
private fun LoanTemplate.defaultNumbers(): Map<LoanField, String> = buildMap {
    principal?.let { put(LoanField.PRINCIPAL, it.toString()) }
    numberOfRepayments?.let { put(LoanField.NUMBER_OF_REPAYMENTS, it.toString()) }
    interestRatePerPeriod?.let { put(LoanField.NOMINAL_INTEREST, it.toString()) }
    repaymentEvery?.let { put(LoanField.REPAID_EVERY, it.toString()) }
}

/**
 * The template's own chosen option for each type field, matched against its option list.
 *
 * `firstOrNull` on a value match, so a miss omits the field. The original used `indexOfFirst`,
 * which returns -1 on a miss and stored it as if it were a position.
 */
private fun LoanTemplate.defaultSelections(): Map<LoanField, LoanOption> = buildMap {
    amortizationTypeOptions.firstOrNull { it.value == amortizationType?.value }
        ?.let { o -> o.id?.let { put(LoanField.AMORTIZATION, LoanOption(it, o.value.orEmpty())) } }
    termFrequencyTypeOptions.firstOrNull { it.value == termPeriodFrequencyType?.value }
        ?.let { o -> o.id?.let { put(LoanField.TERM_FREQUENCY_TYPE, LoanOption(it, o.value.orEmpty())) } }
    repaymentFrequencyTypeOptions.firstOrNull { it.value == repaymentFrequencyType?.value }
        ?.let { o -> o.id?.let { put(LoanField.REPAYMENT_FREQUENCY_TYPE, LoanOption(it, o.value.orEmpty())) } }
    interestRateFrequencyTypeOptions.firstOrNull { it.value == interestRateFrequencyType?.value }
        ?.let { o -> o.id?.let { put(LoanField.NOMINAL_FREQUENCY, LoanOption(it, o.value.orEmpty())) } }
    interestTypeOptions.firstOrNull { it.value == interestType?.value }
        ?.let { o -> o.id?.let { put(LoanField.INTEREST_METHOD, LoanOption(it, o.value.orEmpty())) } }
    interestCalculationPeriodTypeOptions.firstOrNull { it.value == interestCalculationPeriodType?.value }
        ?.let { o -> o.id?.let { put(LoanField.INTEREST_CALCULATION_PERIOD, LoanOption(it, o.value.orEmpty())) } }
    strategyOptions().firstOrNull { it.code == transactionProcessingStrategyCode }
        ?.let { put(LoanField.REPAYMENT_STRATEGY, it) }
}

/**
 * The repayment strategies, keyed by CODE — the only thing the payload sends. Current Fineract sends
 * these with no `id`, and requiring one dropped every option: the field never drew, yet stayed
 * required, so Terms' Next silently did nothing (gateway, device 2026-10-26). The position stands in
 * for the id, which only identifies the row in the picker.
 */
internal fun LoanTemplate.strategyOptions(): List<LoanOption> =
    transactionProcessingStrategyOptions.mapIndexedNotNull { index, o ->
        val code = o.code ?: return@mapIndexedNotNull null
        LoanOption(id = o.id ?: index, label = o.name ?: code, code = code)
    }

/** A selectable loan product. */
internal data class LoanProductOption(val id: Int, val name: String)

internal fun LoanProductOption.asOption() = LoanOption(id = id, label = name)

/**
 * One dropdown choice.
 *
 * [code] is populated only for the repayment strategy, which is the one option the payload
 * identifies by code rather than id — carrying both is what lets every dropdown share one type.
 */
@Serializable
internal data class LoanOption(val id: Int, val label: String, val code: String? = null)

/** The dates the application collects. */
internal enum class LoanDate { SUBMITTED_ON, EXPECTED_DISBURSEMENT }

/** The checkboxes. */
internal enum class LoanToggle { EQUAL_AMORTIZATION, INTEREST_PARTIAL_PERIOD }

/**
 * Every field that can carry a value or an error, keyed so the prefill, the screen and the
 * validation all agree on the name.
 *
 * [affectsSchedule] marks the fields the server's schedule calculation reads: changing one
 * invalidates a preview already fetched, which the original did not do — it kept showing the
 * schedule for the terms you had before.
 */
internal enum class LoanField(val affectsSchedule: Boolean = true) {
    PRODUCT,
    FUND(affectsSchedule = false),
    LOAN_OFFICER(affectsSchedule = false),
    LOAN_PURPOSE(affectsSchedule = false),
    LINK_SAVINGS(affectsSchedule = false),

    PRINCIPAL,
    NUMBER_OF_REPAYMENTS,
    NOMINAL_INTEREST,
    REPAID_EVERY,
    TERM_FREQUENCY_TYPE,
    REPAYMENT_FREQUENCY_TYPE,
    REPAYMENT_DAY_OF_WEEK,
    REPAYMENT_NTH_DAY,
    NOMINAL_FREQUENCY,
    INTEREST_METHOD,
    AMORTIZATION,
    INTEREST_CALCULATION_PERIOD,
    REPAYMENT_STRATEGY,
    ;

    companion object {
        /** Must parse and be > 0. */
        val requiredPositive = listOf(PRINCIPAL, NUMBER_OF_REPAYMENTS, REPAID_EVERY)

        /** Must parse and be >= 0 — a 0% product is legitimate. */
        val requiredNonNegative = listOf(NOMINAL_INTEREST)
    }
}

/**
 * The wizard's steps, each owning the fields it validates.
 *
 * The validation lives HERE rather than in one submit-time check, so Next reports an error on the
 * step that owns the field. The original's `moveToNextStep()` validated nothing at all.
 */
internal enum class LoanStep {
    DETAILS {
        override fun missing(state: NewLoanApplicationState) = buildSet {
            if (state.selections[LoanField.PRODUCT] == null) add(LoanField.PRODUCT)
        }
    },
    TERMS {
        override fun missing(state: NewLoanApplicationState) = buildSet {
            LoanField.requiredPositive.forEach {
                if (!state.number(it).isPositiveNumber()) add(it)
            }
            LoanField.requiredNonNegative.forEach {
                if (!state.number(it).isNonNegativeNumber()) add(it)
            }
            // The four type fields Fineract requires on a loan application. Each was an unguarded
            // index read in the original's submit builder.
            REQUIRED_TERM_SELECTIONS.forEach {
                if (state.selections[it] == null) add(it)
            }
        }
    },

    /** Server-calculated; nothing to validate, and it cannot be reached with Terms incomplete. */
    SCHEDULE {
        override fun missing(state: NewLoanApplicationState) = emptySet<LoanField>()
    },
    PREVIEW {
        override fun missing(state: NewLoanApplicationState) = emptySet<LoanField>()
    },
    ;

    abstract fun missing(state: NewLoanApplicationState): Set<LoanField>

    companion object {
        val REQUIRED_TERM_SELECTIONS = listOf(
            LoanField.TERM_FREQUENCY_TYPE,
            LoanField.REPAYMENT_FREQUENCY_TYPE,
            LoanField.AMORTIZATION,
            LoanField.INTEREST_METHOD,
            LoanField.INTEREST_CALCULATION_PERIOD,
            LoanField.REPAYMENT_STRATEGY,
        )
    }
}

/** Why a schedule preview could not be shown. Distinct from a SUBMIT failure. */
internal sealed interface ScheduleError {
    data object Offline : ScheduleError
    data object Failed : ScheduleError

    /** The server computed and REJECTED these terms; [msg] is its reason. */
    data class Rejected(val msg: String?) : ScheduleError
}

@OptIn(ExperimentalTime::class)
internal data class NewLoanApplicationState(
    val clientId: Int = -1,
    val step: LoanStep = LoanStep.DETAILS,
    val template: LoanTemplate? = null,
    val schedule: RepaymentSchedule? = null,

    val numbers: Map<LoanField, String> = emptyMap(),
    val selections: Map<LoanField, LoanOption> = emptyMap(),
    val dates: Map<LoanDate, Long> = emptyMap(),
    val toggles: Map<LoanToggle, Boolean> = emptyMap(),
    val errors: Set<LoanField> = emptySet(),

    val externalId: String = "",
    val activeDatePicker: LoanDate? = null,

    val isLoadingTemplate: Boolean = false,
    val templateError: Boolean = false,
    val isLoadingSchedule: Boolean = false,
    val scheduleError: ScheduleError? = null,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    fun number(field: LoanField): String = numbers[field].orEmpty()

    fun selectionId(field: LoanField): Int? = selections[field]?.id

    fun selectionCode(field: LoanField): String? = selections[field]?.code

    fun toggle(toggle: LoanToggle): Boolean = toggles[toggle] == true

    /** Defaults to today, which is what both dates mean when untouched. */
    fun date(field: LoanDate): Long = dates[field] ?: Clock.System.now().toEpochMilliseconds()

    fun double(field: LoanField): Double? = number(field).trim().toDoubleOrNull()

    fun int(field: LoanField): Int? = number(field).trim().toIntOrNull()

    /** The product's terms are the whole form; without them there is nothing past step 1. */
    val canAdvance: Boolean get() = !isLoadingTemplate && !isSubmitting

    sealed interface DialogState {
        data object Created : DialogState

        /** [draftSaved] = kept (offline automatically, or "Save as draft"), in Sync & Drafts. */
        data class Failed(val msg: String, val offline: Boolean = false, val draftSaved: Boolean = false) : DialogState
    }
}

internal sealed interface NewLoanApplicationEvent {
    data object NavigateBack : NewLoanApplicationEvent
}

internal sealed interface NewLoanApplicationAction {
    data object ResumeDraft : NewLoanApplicationAction
    data object DiscardDraft : NewLoanApplicationAction
    data object SaveDraft : NewLoanApplicationAction
    data object NavigateBack : NewLoanApplicationAction
    data object OnRetryProducts : NewLoanApplicationAction
    data object OnRetryTemplate : NewLoanApplicationAction
    data object OnRetrySchedule : NewLoanApplicationAction
    data object OnNext : NewLoanApplicationAction
    data object OnBack : NewLoanApplicationAction
    data object OnSubmit : NewLoanApplicationAction
    data object CloseDialog : NewLoanApplicationAction
    data object CloseDatePicker : NewLoanApplicationAction
    data class OnProductSelected(val product: LoanProductOption) : NewLoanApplicationAction
    data class OnOptionSelected(val field: LoanField, val option: LoanOption) : NewLoanApplicationAction
    data class OnNumberChanged(val field: LoanField, val value: String) : NewLoanApplicationAction
    data class OnExternalIdChanged(val value: String) : NewLoanApplicationAction
    data class OnToggled(val toggle: LoanToggle, val selected: Boolean) : NewLoanApplicationAction
    data class OnDateChanged(val field: LoanDate, val millis: Long) : NewLoanApplicationAction
    data class OpenDatePicker(val field: LoanDate) : NewLoanApplicationAction
}

/** What a loan draft keeps: the officer's input across all four steps — not the server's template. */
@Serializable
internal data class LoanFormSnapshot(
    val numbers: Map<LoanField, String> = emptyMap(),
    val selections: Map<LoanField, LoanOption> = emptyMap(),
    val dates: Map<LoanDate, Long> = emptyMap(),
    val toggles: Map<LoanToggle, Boolean> = emptyMap(),
    val externalId: String = "",
) {
    companion object {
        fun of(state: NewLoanApplicationState) =
            LoanFormSnapshot(state.numbers, state.selections, state.dates, state.toggles, state.externalId)
    }
}
