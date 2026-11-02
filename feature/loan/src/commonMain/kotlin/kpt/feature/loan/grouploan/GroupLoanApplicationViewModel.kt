/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.grouploan

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
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.objects.payloads.GroupLoanPayload
import kpt.core.model.objects.template.loan.GroupLoanTemplate
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Apply for a GROUP loan — ported from
 * `6b66e8a43:feature/loan/groupLoanAccount/{GroupLoanAccountViewModel,Screen}.kt`.
 *
 * Reached from group detail's "Add Loan Account". The product list is read first; choosing a
 * product fetches that product's template, which supplies every dropdown's options AND the
 * defaults for every numeric field.
 *
 * ## FIXED: a GROUP loan application declared `loanType = "individual"`
 *
 * Hardcoded, on the group-loan screen:
 *
 * ```kotlin
 * loanType = "individual"
 * ```
 *
 * Fineract uses `loanType` to decide what the loan is attached to. A group application sent as
 * individual is either rejected or — worse — accepted against the wrong entity. `"group"` now,
 * named as a constant beside the group id it travels with.
 *
 * ## FIXED: the date/format mismatch, FIFTH occurrence — and the first on a CREATE
 *
 * Both dates went out as `DateHelper.getDateAsStringFromLong(...)` ("14-03-2026") with
 * `dateFormat = ApiDateFormatter.DATE_FORMAT` ("dd MMMM yyyy"). Same defect as `clientClosure`,
 * `clientTransfer`, `loanApproval` and `loanDisbursement`; the first time it was on a request that
 * CREATES something rather than acting on an existing record.
 *
 * ## FIXED: five arbitrary hardcoded defaults, when the template carries the real ones
 *
 * ```kotlin
 * var principalAmount by rememberSaveable { mutableStateOf("10000.0") }
 * var numberOfRepayment by rememberSaveable { mutableStateOf("10") }
 * var nominal by rememberSaveable { mutableStateOf("5.0") }
 * var repaidEvery by rememberSaveable { mutableStateOf("2") }
 * var loanTerms by rememberSaveable { mutableStateOf("20") }
 * ```
 *
 * A principal of 10,000 and 5% interest over 20 terms are not this product's terms — they are
 * placeholders, and an officer who submitted without editing every field created a loan on them.
 * `GroupLoanTemplate` carries `principal`, `numberOfRepayments`, `interestRatePerPeriod`,
 * `repaymentEvery` and `termFrequency`; the form prefills from those, and re-prefills when the
 * product changes (because they are that PRODUCT's terms).
 *
 * ## FIXED: three payload fields fed from the wrong dropdown
 *
 * ```kotlin
 * repaymentFrequencyType = loanTermsTypeFrequency              // the LOAN TERM's frequency
 * repaymentFrequencyDayOfWeekType = repaidEveryTypeFrequency
 * repaymentFrequencyNthDayType = repaidEveryTypeFrequency      // same value, different field
 * ```
 *
 * The repayment frequency took the loan-term dropdown's value, and the day-of-week and nth-day
 * fields both took the day-of-week dropdown's. Each field now comes from its own selection, and the
 * two that have no control on this form are omitted rather than filled with a neighbour's value.
 *
 * ## FIXED: the product could be submitted as id 0
 *
 * The screen auto-loaded the template for `loanProducts[0]` but never SET `selectedLoanProductId`,
 * which started at `0` — so submitting without touching the product dropdown sent `productId = 0`
 * while the form showed the first product's terms. Selecting a product is what loads its template
 * here, so the two cannot disagree.
 */
internal class GroupLoanApplicationViewModel(
    private val repository: LoanRepository,
    private val commands: LoanCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<GroupLoanApplicationState, GroupLoanApplicationEvent, GroupLoanApplicationAction>(
    GroupLoanApplicationState(groupId = savedStateHandle.get<Int>(GROUP_ID_KEY) ?: -1),
) {

    /** This group's application that did not land — the officer's input; the template is re-fetched. */
    private val formDraft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.GROUP_LOAN.key,
        ownerId = state.groupId.toLong(),
        GroupLoanFormSnapshot.serializer(),
        GroupLoanPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered

    private var lastPayload: GroupLoanPayload? = null

    private fun keepDraft(payload: GroupLoanPayload, failure: String?) =
        formDraft.keep(state.product?.name.orEmpty(), GroupLoanFormSnapshot.of(state), payload, failure)

    private val productsStream = repository.loanProductsStream(viewModelScope)

    /** The tenant's loan products. Choosing one is what starts the form. */
    val products: StateFlow<ScreenState<List<LoanProductOption>>> = productsStream.state
        .mapContent { products, _ ->
            products.mapNotNull { product ->
                LoanProductOption(
                    // A product with no id cannot be applied for, so it is not offered.
                    id = product.id ?: return@mapNotNull null,
                    name = product.name.orEmpty(),
                )
            }
        }
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: GroupLoanApplicationAction) {
        when (action) {
            GroupLoanApplicationAction.NavigateBack ->
                sendEvent(GroupLoanApplicationEvent.NavigateBack)

            GroupLoanApplicationAction.OnRetryProducts -> productsStream.retry()
            GroupLoanApplicationAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            GroupLoanApplicationAction.ResumeDraft,
            GroupLoanApplicationAction.DiscardDraft,
            GroupLoanApplicationAction.SaveDraft,
            -> handleDraftAction(action)

            is GroupLoanApplicationAction.OnProductSelected -> {
                mutableStateFlow.value = state.copy(product = action.product)
                loadTemplate(action.product.id)
            }

            GroupLoanApplicationAction.OnRetryTemplate ->
                state.product?.let { loadTemplate(it.id) }

            is GroupLoanApplicationAction.OnOptionSelected,
            is GroupLoanApplicationAction.OnNumberChanged,
            is GroupLoanApplicationAction.OnExactDaysToggled,
            is GroupLoanApplicationAction.OnDateChanged,
            is GroupLoanApplicationAction.OpenDatePicker,
            GroupLoanApplicationAction.CloseDatePicker,
            -> handleFormEdit(action)

            GroupLoanApplicationAction.OnSubmit -> validateThenSubmit()
        }
    }

    private fun handleDraftAction(action: GroupLoanApplicationAction) {
        when (action) {
            GroupLoanApplicationAction.ResumeDraft -> formDraft.resume()?.let { restored ->
                val product = restored.product ?: return@let
                mutableStateFlow.value = state.copy(product = product)
                loadTemplate(product.id, restore = restored)
            }
            GroupLoanApplicationAction.DiscardDraft -> formDraft.discard()
            GroupLoanApplicationAction.SaveDraft -> lastPayload?.let { payload ->
                val failed = state.dialogState as? GroupLoanApplicationState.DialogState.Failed
                keepDraft(payload, failure = failed?.msg ?: FAILED_MSG)
                mutableStateFlow.value = state.copy(dialogState = failed?.copy(draftSaved = true))
            }
            else -> Unit
        }
    }

    private fun handleFormEdit(action: GroupLoanApplicationAction) {
        when (action) {
            is GroupLoanApplicationAction.OnOptionSelected -> mutableStateFlow.value = state.copy(
                selections = state.selections + (action.field to action.option),
                errors = state.errors - action.field,
            )

            is GroupLoanApplicationAction.OnNumberChanged -> mutableStateFlow.value = state.copy(
                numbers = state.numbers + (action.field to action.value),
                errors = state.errors - action.field,
            )

            is GroupLoanApplicationAction.OnExactDaysToggled ->
                mutableStateFlow.value = state.copy(calculateExactDays = action.selected)

            is GroupLoanApplicationAction.OnDateChanged -> mutableStateFlow.value = when (action.field) {
                GroupLoanDate.SUBMITTED_ON -> state.copy(submittedOn = action.millis)
                GroupLoanDate.EXPECTED_DISBURSEMENT -> state.copy(expectedDisbursement = action.millis)
            }

            is GroupLoanApplicationAction.OpenDatePicker ->
                mutableStateFlow.value = state.copy(activeDatePicker = action.field)

            GroupLoanApplicationAction.CloseDatePicker ->
                mutableStateFlow.value = state.copy(activeDatePicker = null)

            else -> Unit
        }
    }

    /** [restore] = a resumed draft's input, which wins over the template's defaults. */
    private fun loadTemplate(productId: Int, restore: GroupLoanFormSnapshot? = null) {
        mutableStateFlow.value = state.copy(isLoadingTemplate = true, templateError = false)

        viewModelScope.launch {
            val template = repository.groupLoanTemplate(state.groupId, productId)

            mutableStateFlow.value = if (template == null) {
                state.copy(isLoadingTemplate = false, templateError = true)
            } else {
                state.copy(
                    isLoadingTemplate = false,
                    templateError = false,
                    template = template,
                    // Prefilled from THIS product's terms, and re-prefilled whenever the product
                    // changes — see the header for the five placeholders this replaces. Any
                    // selection the officer had made is dropped with them, because the options
                    // themselves came from the previous product's template.
                    numbers = template.defaultNumbers() + restore?.numbers.orEmpty(),
                    selections = restore?.selections.orEmpty(),
                    errors = emptySet(),
                    calculateExactDays = restore?.calculateExactDays ?: state.calculateExactDays,
                    submittedOn = restore?.submittedOn ?: state.submittedOn,
                    expectedDisbursement = restore?.expectedDisbursement ?: state.expectedDisbursement,
                )
            }
        }
    }

    private fun validateThenSubmit() {
        if (state.isSubmitting) return

        val errors = validationErrors()
        mutableStateFlow.value = state.copy(errors = errors)
        if (errors.isEmpty()) buildDraft()?.let(::submit)
    }

    private fun validationErrors(): Set<GroupLoanField> = buildSet {
        if (state.product == null) add(GroupLoanField.PRODUCT)
        // Every numeric field must parse and be positive. The original called `.toInt()` and
        // `.toDouble()` on them unguarded, so a non-numeric crashed the submit.
        GroupLoanField.numeric.forEach { field ->
            if (!state.number(field).isPositiveNumber()) add(field)
        }
        // The four dropdowns Fineract requires on a loan application.
        GroupLoanField.requiredSelections.forEach { field ->
            if (state.selections[field] == null) add(field)
        }
    }

    /** The four required dropdown ids, or null when any is missing. */
    private fun requiredSelectionIds(): List<Int>? = listOfNotNull(
        state.selectionId(GroupLoanField.LOAN_TERM_TYPE),
        state.selectionId(GroupLoanField.REPAYMENT_FREQUENCY_TYPE),
        state.selectionId(GroupLoanField.AMORTIZATION),
        state.selectionId(GroupLoanField.INTEREST_TYPE),
    ).takeIf { it.size == REQUIRED_SELECTION_COUNT }

    private fun buildDraft(): GroupLoanDraft? {
        val productId = state.product?.id
        val rate = state.number(GroupLoanField.NOMINAL_INTEREST).trim().toDoubleOrNull()
        val term = state.number(GroupLoanField.LOAN_TERM).trim().toIntOrNull()
        val ids = requiredSelectionIds()
        // Rate and term travel together, keeping the null check to three conditions (ComplexCondition).
        val terms = rate?.let { r -> term?.let { t -> r to t } }
        if (productId == null || terms == null || ids == null) return null
        return GroupLoanDraft(
            productId = productId,
            submittedOn = state.submittedOn,
            expectedDisbursement = state.expectedDisbursement,
            principal = state.number(GroupLoanField.PRINCIPAL).trim(),
            numberOfRepayments = state.number(GroupLoanField.NUMBER_OF_REPAYMENTS).trim(),
            interestRatePerPeriod = terms.first,
            repaymentEvery = state.number(GroupLoanField.REPAID_EVERY).trim(),
            loanTermFrequency = terms.second,
            loanTermFrequencyType = ids[0],
            repaymentFrequencyType = ids[1],
            amortizationType = ids[2],
            interestType = ids[3],
            interestCalculationPeriodType = state.selectionId(GroupLoanField.INTEREST_CALCULATION_PERIOD),
            transactionProcessingStrategyId = state.selectionId(GroupLoanField.REPAYMENT_STRATEGY),
            loanPurposeId = state.selectionId(GroupLoanField.LOAN_PURPOSE),
            fundId = state.selectionId(GroupLoanField.FUND),
            loanOfficerId = state.selectionId(GroupLoanField.LOAN_OFFICER),
            calculateExactDays = state.calculateExactDays,
        )
    }

    private fun submit(draft: GroupLoanDraft) {
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            val payload = GroupLoanPayload(
                groupId = state.groupId,
                productId = draft.productId,
                // `"group"`, not the original's hardcoded `"individual"` — see the header.
                loanType = GROUP_LOAN_TYPE,
                // Both dates from the helper that OWNS the two format fields below.
                submittedOnDate = ApiDateFormatter.formatForApi(draft.submittedOn),
                expectedDisbursementDate = ApiDateFormatter.formatForApi(draft.expectedDisbursement),
                dateFormat = ApiDateFormatter.DATE_FORMAT,
                locale = ApiDateFormatter.LOCALE,
                principal = draft.principal,
                numberOfRepayments = draft.numberOfRepayments,
                interestRatePerPeriod = draft.interestRatePerPeriod,
                repaymentEvery = draft.repaymentEvery,
                loanTermFrequency = draft.loanTermFrequency,
                loanTermFrequencyType = draft.loanTermFrequencyType,
                // Its OWN dropdown, not the loan term's.
                repaymentFrequencyType = draft.repaymentFrequencyType,
                amortizationType = draft.amortizationType,
                interestType = draft.interestType,
                interestCalculationPeriodType = draft.interestCalculationPeriodType,
                transactionProcessingStrategyId = draft.transactionProcessingStrategyId,
                loanPurposeId = draft.loanPurposeId,
                isAllowPartialPeriodInterestCalculation = draft.calculateExactDays,
                // `repaymentFrequencyDayOfWeekType` and `repaymentFrequencyNthDayType` are
                // deliberately OMITTED: this form has no control for either, and the original
                // filled both from the day-of-week dropdown — so an nth-day field received a
                // day-of-week value.
            )

            lastPayload = payload
            when (val result = commands.createGroupLoanAccount(payload).offlineWhenUnsent()) {
                is MutationResult.Applied -> {
                    formDraft.clear()
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = GroupLoanApplicationState.DialogState.Created(
                            loanId = result.value.id,
                        ),
                    )
                }

                // Offline: kept automatically, sent on reconnect.
                is MutationResult.Blocked -> {
                    if (result.isOffline) keepDraft(payload, failure = null)
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = GroupLoanApplicationState.DialogState.Failed(
                            if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                            offline = result.isOffline,
                            draftSaved = result.isOffline,
                        ),
                    )
                }

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create group loan failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = GroupLoanApplicationState.DialogState.Failed(
                            result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = GroupLoanApplicationState.DialogState.Failed(FAILED_MSG),
                )
            }
        }
    }

    private companion object {
        const val TAG = "GroupLoanApplication"
        const val GROUP_ID_KEY = "groupId"

        /** Fineract's `loanType` for a loan attached to a GROUP. */
        const val GROUP_LOAN_TYPE = "group"
        const val STATE_TIMEOUT_MS = 5_000L
        const val REQUIRED_SELECTION_COUNT = 4

        const val OFFLINE_MSG = "No connection — the application was not submitted."
        const val FAILED_MSG = "Could not submit this loan application."
    }
}

/** Positive and parseable. A principal, term count or rate of zero is not an application. */
private fun String.isPositiveNumber(): Boolean = trim().toDoubleOrNull()?.let { it > 0 } == true

/**
 * This PRODUCT's terms, as the form's starting values.
 *
 * Every one of these replaces an arbitrary literal the original hardcoded — see the ViewModel's
 * header. A field the template leaves null starts blank rather than at someone's guess.
 */
private fun GroupLoanTemplate.defaultNumbers(): Map<GroupLoanField, String> = buildMap {
    principal?.let { put(GroupLoanField.PRINCIPAL, it.toString()) }
    numberOfRepayments?.let { put(GroupLoanField.NUMBER_OF_REPAYMENTS, it.toString()) }
    interestRatePerPeriod?.let { put(GroupLoanField.NOMINAL_INTEREST, it.toString()) }
    repaymentEvery?.let { put(GroupLoanField.REPAID_EVERY, it.toString()) }
    termFrequency?.let { put(GroupLoanField.LOAN_TERM, it.toString()) }
}

/** A selectable loan product. */
@Serializable
internal data class LoanProductOption(val id: Int, val name: String)

/** One dropdown choice: the id the payload needs, and the label the dropdown shows. */
@Serializable
internal data class GroupLoanOption(val id: Int, val label: String)

/** The two dates the application collects. */
internal enum class GroupLoanDate { SUBMITTED_ON, EXPECTED_DISBURSEMENT }

/**
 * Every field that can carry a value or an error, keyed so the screen, the defaults and the
 * validation all agree on the name.
 */
internal enum class GroupLoanField {
    PRODUCT,
    LOAN_PURPOSE,
    LOAN_OFFICER,
    FUND,
    PRINCIPAL,
    NUMBER_OF_REPAYMENTS,
    NOMINAL_INTEREST,
    REPAID_EVERY,
    REPAYMENT_FREQUENCY_TYPE,
    LOAN_TERM,
    LOAN_TERM_TYPE,
    AMORTIZATION,
    INTEREST_CALCULATION_PERIOD,
    REPAYMENT_STRATEGY,
    INTEREST_TYPE,
    ;

    companion object {
        /** The five numeric inputs, each prefilled from the template and each validated. */
        val numeric = listOf(
            PRINCIPAL,
            NUMBER_OF_REPAYMENTS,
            NOMINAL_INTEREST,
            REPAID_EVERY,
            LOAN_TERM,
        )

        /**
         * The dropdowns Fineract REQUIRES on a loan application.
         *
         * Purpose, officer, fund and the two optional type fields are not here: Fineract accepts an
         * application without them, and the original sent `0` for each when unchosen — an id that
         * belongs to no row.
         */
        val requiredSelections = listOf(
            LOAN_TERM_TYPE,
            REPAYMENT_FREQUENCY_TYPE,
            AMORTIZATION,
            INTEREST_TYPE,
        )
    }
}

/** An application proven complete, so the submit path never re-reads nullable state. */
internal data class GroupLoanDraft(
    val productId: Int,
    val submittedOn: Long,
    val expectedDisbursement: Long,
    val principal: String,
    val numberOfRepayments: String,
    val interestRatePerPeriod: Double,
    val repaymentEvery: String,
    val loanTermFrequency: Int,
    val loanTermFrequencyType: Int,
    val repaymentFrequencyType: Int,
    val amortizationType: Int,
    val interestType: Int,
    val interestCalculationPeriodType: Int?,
    val transactionProcessingStrategyId: Int?,
    val loanPurposeId: Int?,
    val fundId: Int?,
    val loanOfficerId: Int?,
    val calculateExactDays: Boolean,
)

@OptIn(ExperimentalTime::class)
internal data class GroupLoanApplicationState(
    val groupId: Int = -1,
    val product: LoanProductOption? = null,
    val template: GroupLoanTemplate? = null,

    val numbers: Map<GroupLoanField, String> = emptyMap(),
    val selections: Map<GroupLoanField, GroupLoanOption> = emptyMap(),
    val errors: Set<GroupLoanField> = emptySet(),

    val calculateExactDays: Boolean = false,
    val submittedOn: Long = Clock.System.now().toEpochMilliseconds(),
    val expectedDisbursement: Long = Clock.System.now().toEpochMilliseconds(),
    val activeDatePicker: GroupLoanDate? = null,

    val isLoadingTemplate: Boolean = false,
    val templateError: Boolean = false,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    fun number(field: GroupLoanField): String = numbers[field].orEmpty()

    fun selectionId(field: GroupLoanField): Int? = selections[field]?.id

    sealed interface DialogState {
        /** [loanId] is Fineract's new loan id, shown so the officer can find it. */
        data class Created(val loanId: Int?) : DialogState

        /** [draftSaved] = kept (offline automatically, or "Save as draft"), in Sync & Drafts. */
        data class Failed(val msg: String, val offline: Boolean = false, val draftSaved: Boolean = false) : DialogState
    }
}

internal sealed interface GroupLoanApplicationEvent {
    data object NavigateBack : GroupLoanApplicationEvent
}

internal sealed interface GroupLoanApplicationAction {
    data object ResumeDraft : GroupLoanApplicationAction
    data object DiscardDraft : GroupLoanApplicationAction
    data object SaveDraft : GroupLoanApplicationAction
    data object NavigateBack : GroupLoanApplicationAction
    data object OnRetryProducts : GroupLoanApplicationAction
    data object OnRetryTemplate : GroupLoanApplicationAction
    data object OnSubmit : GroupLoanApplicationAction
    data object CloseDialog : GroupLoanApplicationAction
    data object CloseDatePicker : GroupLoanApplicationAction
    data class OnProductSelected(val product: LoanProductOption) : GroupLoanApplicationAction
    data class OnOptionSelected(
        val field: GroupLoanField,
        val option: GroupLoanOption,
    ) : GroupLoanApplicationAction
    data class OnNumberChanged(val field: GroupLoanField, val value: String) : GroupLoanApplicationAction
    data class OnExactDaysToggled(val selected: Boolean) : GroupLoanApplicationAction
    data class OnDateChanged(val field: GroupLoanDate, val millis: Long) : GroupLoanApplicationAction
    data class OpenDatePicker(val field: GroupLoanDate) : GroupLoanApplicationAction
}

/** What a group loan draft keeps: the officer's input — not the server's template. */
@Serializable
internal data class GroupLoanFormSnapshot(
    val product: LoanProductOption? = null,
    val numbers: Map<GroupLoanField, String> = emptyMap(),
    val selections: Map<GroupLoanField, GroupLoanOption> = emptyMap(),
    val calculateExactDays: Boolean = false,
    val submittedOn: Long? = null,
    val expectedDisbursement: Long? = null,
) {
    companion object {
        fun of(state: GroupLoanApplicationState) = GroupLoanFormSnapshot(
            state.product,
            state.numbers,
            state.selections,
            state.calculateExactDays,
            state.submittedOn,
            state.expectedDisbursement,
        )
    }
}
