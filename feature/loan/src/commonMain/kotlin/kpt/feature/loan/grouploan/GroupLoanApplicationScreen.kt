/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.grouploan

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
import androidx.compose.material3.SelectableDates
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
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.generated.resources.draft_save
import kpt.core.base.ui.generated.resources.draft_saved
import kpt.core.base.ui.generated.resources.draft_saved_offline
import kpt.core.base.ui.screen.DefaultEmptyContent
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.base.ui.screen.ScreenStateCta
import kpt.core.base.ui.screen.ScreenStateEmpty
import kpt.core.base.ui.submit.FormDraftBanner
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppSweetError
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.objects.template.loan.GroupLoanTemplate
import kpt.core.ui.components.AppCheckBox
import kpt.core.ui.components.AppProgressIndicatorMini
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_amortization
import kpt.feature.loan.generated.resources.feature_loan_application
import kpt.feature.loan.generated.resources.feature_loan_application_created_message
import kpt.feature.loan.generated.resources.feature_loan_application_created_title
import kpt.feature.loan.generated.resources.feature_loan_application_failure_title
import kpt.feature.loan.generated.resources.feature_loan_application_no_products
import kpt.feature.loan.generated.resources.feature_loan_application_no_products_title
import kpt.feature.loan.generated.resources.feature_loan_calculate_interest_for_exact_days_in
import kpt.feature.loan.generated.resources.feature_loan_cancel
import kpt.feature.loan.generated.resources.feature_loan_dialog_continue
import kpt.feature.loan.generated.resources.feature_loan_disbursed_date
import kpt.feature.loan.generated.resources.feature_loan_error_pick_an_option
import kpt.feature.loan.generated.resources.feature_loan_error_positive_number
import kpt.feature.loan.generated.resources.feature_loan_fund
import kpt.feature.loan.generated.resources.feature_loan_interest_calculation_period
import kpt.feature.loan.generated.resources.feature_loan_interest_type_method
import kpt.feature.loan.generated.resources.feature_loan_loan_terms
import kpt.feature.loan.generated.resources.feature_loan_nominal
import kpt.feature.loan.generated.resources.feature_loan_number_of_repayments
import kpt.feature.loan.generated.resources.feature_loan_officer
import kpt.feature.loan.generated.resources.feature_loan_principal
import kpt.feature.loan.generated.resources.feature_loan_product
import kpt.feature.loan.generated.resources.feature_loan_purpose
import kpt.feature.loan.generated.resources.feature_loan_repaid_every
import kpt.feature.loan.generated.resources.feature_loan_repayment_frequency
import kpt.feature.loan.generated.resources.feature_loan_repayment_strategy
import kpt.feature.loan.generated.resources.feature_loan_retry
import kpt.feature.loan.generated.resources.feature_loan_select_date
import kpt.feature.loan.generated.resources.feature_loan_submission_date
import kpt.feature.loan.generated.resources.feature_loan_submit
import kpt.feature.loan.generated.resources.feature_loan_template_failed
import kpt.feature.loan.generated.resources.feature_loan_term
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kpt.core.base.ui.generated.resources.Res as BaseRes

/**
 * Carried over from `6b66e8a43:feature/loan/groupLoanAccount/GroupLoanAccountScreen.kt` — the
 * product dropdown, the four optional reference dropdowns, two dates, external id, five numeric
 * terms, the four required type dropdowns, the exact-days checkbox and submit.
 *
 * ## §6a adaptations
 *
 * The four-case `GroupLoanAccountUiState` splits into the products [ScreenState], the template's
 * own loading/error flags, and a dialog for the write's outcome — the original's single state meant
 * a create in flight replaced the filled-in form with a progress bar, and on success it did this
 * from the composition body:
 *
 * ```kotlin
 * is GroupLoanAccountCreatedSuccessfully -> { scope.launch { snackbar.showSnackbar(msg) }; onBackPressed() }
 * ```
 *
 * The sixteen `rememberSaveable` locals become keyed maps in state, so the defaults, the screen and
 * the validation cannot disagree about a field's name — and the five numeric defaults come from the
 * PRODUCT's template rather than from hardcoded literals (see the ViewModel's header).
 *
 * ## The submission date cannot be in the future
 *
 * The original restricted BOTH pickers to `>= now`, future-only. "Submitted on" is when the
 * application is made — today — so a future value is wrong and today was not selectable either;
 * same inverted rule as the four other screens. Expected disbursement is left unrestricted: a
 * future disbursement is the whole point of the field.
 */
@Composable
internal fun GroupLoanApplicationScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupLoanApplicationViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            GroupLoanApplicationEvent.NavigateBack -> onBackPressed()
        }
    }

    GroupLoanApplicationContent(
        state = state,
        products = products,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
        offeredDraft = offeredDraft,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun GroupLoanApplicationContent(
    state: GroupLoanApplicationState,
    products: ScreenState<List<LoanProductOption>>,
    onAction: (GroupLoanApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
    offeredDraft: SavedFormDraft? = null,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_application),
        onBackPressed = { onAction(GroupLoanApplicationAction.NavigateBack) },
        modifier = modifier.testTag(GroupLoanApplicationTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = products,
            onRetry = { onAction(GroupLoanApplicationAction.OnRetryProducts) },
            // Same reasoning as NewLoanApplicationScreen: the wizard's first page is gated on
            // the tenant's product catalogue, so an office with none configured gets the EMPTY
            // state before any field is drawn. The shared default ("Nothing here yet") names no
            // cause and offers no next step on a form the officer just opened.
            empty = {
                DefaultEmptyContent(
                    config = ScreenStateEmpty(
                        title = stringResource(Res.string.feature_loan_application_no_products_title),
                        message = stringResource(Res.string.feature_loan_application_no_products),
                        cta = ScreenStateCta(
                            label = stringResource(Res.string.feature_loan_retry),
                            onClick = { onAction(GroupLoanApplicationAction.OnRetryProducts) },
                        ),
                    ),
                )
            },
            modifier = Modifier.fillMaxSize().padding(padding)
                .testTag(GroupLoanApplicationTestTags.CONTENT),
        ) { productOptions, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = KptTheme.spacing.md)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(KptTheme.spacing.md))
                offeredDraft?.let {
                    FormDraftBanner(
                        failureReason = it.errorMessage,
                        onResume = { onAction(GroupLoanApplicationAction.ResumeDraft) },
                        onDiscard = { onAction(GroupLoanApplicationAction.DiscardDraft) },
                    )
                }

                // Choosing a product is what loads its template and prefills the form, so the
                // product on screen and the terms on screen can never disagree — the original
                // auto-loaded the first product's template while leaving `productId` at 0.
                AppTextFieldDropdown(
                    value = state.product?.name.orEmpty(),
                    onOptionSelected = { index, _ ->
                        productOptions.getOrNull(index)
                            ?.let { onAction(GroupLoanApplicationAction.OnProductSelected(it)) }
                    },
                    options = productOptions.map { it.name },
                    label = stringResource(Res.string.feature_loan_product),
                    readOnly = true,
                    enabled = !state.isLoadingTemplate,
                    errorMessage = state.errorFor(GroupLoanField.PRODUCT),
                    modifier = Modifier.fillMaxWidth()
                        .testTag(GroupLoanApplicationTestTags.PRODUCT),
                )

                if (state.isLoadingTemplate) {
                    Spacer(Modifier.height(KptTheme.spacing.sm))
                    AppProgressIndicatorMini(
                        modifier = Modifier.testTag(GroupLoanApplicationTestTags.LOADING),
                    )
                }

                when {
                    state.templateError -> {
                        Spacer(Modifier.height(KptTheme.spacing.md))
                        AppSweetError(
                            message = stringResource(Res.string.feature_loan_template_failed),
                            onclick = { onAction(GroupLoanApplicationAction.OnRetryTemplate) },
                            modifier = Modifier.fillMaxWidth()
                                .testTag(GroupLoanApplicationTestTags.TEMPLATE_ERROR),
                        )
                    }

                    // The form exists only once a product's template has landed: every dropdown's
                    // options and every numeric default come from it.
                    state.template != null -> ApplicationForm(
                        state = state,
                        template = state.template,
                        onAction = onAction,
                    )
                }

                Spacer(Modifier.height(KptTheme.spacing.md))
            }
        }
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        is GroupLoanApplicationState.DialogState.Created -> AppStatusDialog(
            status = ResultStatus.SUCCESS,
            btnText = stringResource(Res.string.feature_loan_dialog_continue),
            onConfirm = { onAction(GroupLoanApplicationAction.NavigateBack) },
            onDismissRequest = { onAction(GroupLoanApplicationAction.NavigateBack) },
            successTitle = stringResource(Res.string.feature_loan_application_created_title),
            successMessage = stringResource(Res.string.feature_loan_application_created_message),
            failureTitle = stringResource(Res.string.feature_loan_application_failure_title),
            failureMessage = "",
            modifier = Modifier.fillMaxSize()
                .testTag(GroupLoanApplicationTestTags.STATUS_DIALOG),
        )

        is GroupLoanApplicationState.DialogState.Failed -> AppStatusDialog(
            status = ResultStatus.FAILURE,
            btnText = stringResource(Res.string.feature_loan_dialog_continue),
            // Dismiss, keeping every value — a rejected application is a long form to retype.
            onConfirm = { onAction(GroupLoanApplicationAction.CloseDialog) },
            onDismissRequest = { onAction(GroupLoanApplicationAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_loan_application_created_title),
            successMessage = stringResource(Res.string.feature_loan_application_created_message),
            failureTitle = stringResource(Res.string.feature_loan_application_failure_title),
            failureMessage = dialog.msg,
            modifier = Modifier.fillMaxSize()
                .testTag(GroupLoanApplicationTestTags.STATUS_DIALOG),
            secondaryBtnText = stringResource(BaseRes.string.draft_save).takeIf { !dialog.draftSaved },
            onSecondary = { onAction(GroupLoanApplicationAction.SaveDraft) },
            note = when {
                !dialog.draftSaved -> null
                dialog.offline -> stringResource(BaseRes.string.draft_saved_offline)
                else -> stringResource(BaseRes.string.draft_saved)
            },
        )
    }
}

@Composable
private fun ApplicationForm(
    state: GroupLoanApplicationState,
    template: GroupLoanTemplate,
    onAction: (GroupLoanApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Each dropdown is drawn only when the template OFFERS its options — the original's
        // `template.xOptions?.let { }`, kept. A field the tenant does not configure is absent rather
        // than an empty dropdown.
        Dropdown(
            GroupLoanField.LOAN_PURPOSE,
            Res.string.feature_loan_purpose,
            template.loanPurposeOptions?.toOptions { it.id to it.name },
            state,
            onAction,
        )
        Dropdown(
            GroupLoanField.LOAN_OFFICER,
            Res.string.feature_loan_officer,
            template.loanOfficerOptions?.toOptions { it.id to it.displayName },
            state,
            onAction,
        )
        Dropdown(
            GroupLoanField.FUND,
            Res.string.feature_loan_fund,
            template.fundOptions?.toOptions { it.id to it.name },
            state,
            onAction,
        )

        DateField(
            GroupLoanDate.SUBMITTED_ON,
            Res.string.feature_loan_submission_date,
            state.submittedOn,
            onAction,
            GroupLoanApplicationTestTags.SUBMITTED_ON,
        )
        DateField(
            GroupLoanDate.EXPECTED_DISBURSEMENT,
            Res.string.feature_loan_disbursed_date,
            state.expectedDisbursement,
            onAction,
            GroupLoanApplicationTestTags.DISBURSEMENT_DATE,
        )

        // The original's External Id field is ABSENT, not inert. It wrote to a `rememberSaveable`
        // that nothing read — `GroupLoanPayload` has no `externalId` member, in the fork OR at
        // `6b66e8a43` — so an officer typed an identifier their back office needed and it was
        // discarded on submit with no error. Same "absent, never inert" rule the unported nav
        // affordances follow; restoring it means adding the field to the payload and proving Fineract
        // accepts it (it enforces uniqueness, so a wrong guess fails the whole application).
        // Tracked in CAPABILITY_GAPS as `group-loan-external-id-was-discarded`.

        NumberField(GroupLoanField.PRINCIPAL, Res.string.feature_loan_principal, state, onAction)
        NumberField(GroupLoanField.NUMBER_OF_REPAYMENTS, Res.string.feature_loan_number_of_repayments, state, onAction)
        NumberField(GroupLoanField.NOMINAL_INTEREST, Res.string.feature_loan_nominal, state, onAction)
        NumberField(GroupLoanField.REPAID_EVERY, Res.string.feature_loan_repaid_every, state, onAction)

        // Its OWN dropdown. The original fed this payload field from the LOAN TERM's frequency.
        Dropdown(
            GroupLoanField.REPAYMENT_FREQUENCY_TYPE,
            Res.string.feature_loan_repayment_frequency,
            template.repaymentFrequencyTypeOptions?.toOptions { it.id to it.value },
            state,
            onAction,
        )

        NumberField(GroupLoanField.LOAN_TERM, Res.string.feature_loan_loan_terms, state, onAction)
        Dropdown(
            GroupLoanField.LOAN_TERM_TYPE,
            Res.string.feature_loan_term,
            template.termFrequencyTypeOptions?.toOptions { it.id to it.value },
            state,
            onAction,
        )

        Dropdown(
            GroupLoanField.AMORTIZATION,
            Res.string.feature_loan_amortization,
            template.amortizationTypeOptions?.toOptions { it.id to it.value },
            state,
            onAction,
        )
        Dropdown(
            GroupLoanField.INTEREST_CALCULATION_PERIOD,
            Res.string.feature_loan_interest_calculation_period,
            template.interestCalculationPeriodTypeOptions?.toOptions { it.id to it.value },
            state,
            onAction,
        )
        Dropdown(
            GroupLoanField.REPAYMENT_STRATEGY,
            Res.string.feature_loan_repayment_strategy,
            template.transactionProcessingStrategyOptions?.toOptions { it.id to it.name },
            state,
            onAction,
        )

        Spacer(Modifier.height(KptTheme.spacing.sm))

        AppCheckBox(
            text = stringResource(Res.string.feature_loan_calculate_interest_for_exact_days_in),
            checked = state.calculateExactDays,
            onCheckChanged = { onAction(GroupLoanApplicationAction.OnExactDaysToggled(it)) },
            modifier = Modifier.fillMaxWidth().testTag(GroupLoanApplicationTestTags.EXACT_DAYS),
        )

        Dropdown(
            GroupLoanField.INTEREST_TYPE,
            Res.string.feature_loan_interest_type_method,
            template.interestTypeOptions?.toOptions { it.id to it.value },
            state,
            onAction,
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppButton(
            onClick = { onAction(GroupLoanApplicationAction.OnSubmit) },
            // Enabled, so tapping it REPORTS what is missing across a form this long.
            enabled = !state.isSubmitting && !state.isLoadingTemplate,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(DesignToken.spacing.dp44)
                .testTag(GroupLoanApplicationTestTags.SUBMIT),
        ) {
            Text(text = stringResource(Res.string.feature_loan_submit), style = AppTypography.bodyLarge)
        }

        state.activeDatePicker?.let { which -> ApplicationDatePicker(which, state, onAction) }
    }
}

/** Projects a template option list to (id, label), dropping entries with no id. */
private fun <T> List<T>.toOptions(select: (T) -> Pair<Int?, String?>): List<GroupLoanOption> =
    mapNotNull { item ->
        val (id, label) = select(item)
        // No id means unsendable, so not selectable — the original mapped `it.value.toString()`,
        // which rendered "null" for a nameless option and still let it be chosen.
        GroupLoanOption(id = id ?: return@mapNotNull null, label = label.orEmpty())
    }

@Composable
private fun Dropdown(
    field: GroupLoanField,
    label: StringResource,
    options: List<GroupLoanOption>?,
    state: GroupLoanApplicationState,
    onAction: (GroupLoanApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Absent when the tenant configures no options for it.
    options ?: return
    if (options.isEmpty()) return

    Column(modifier = modifier) {
        Spacer(Modifier.height(KptTheme.spacing.md))
        AppTextFieldDropdown(
            value = state.selections[field]?.label.orEmpty(),
            onOptionSelected = { index, _ ->
                options.getOrNull(index)
                    ?.let { onAction(GroupLoanApplicationAction.OnOptionSelected(field, it)) }
            },
            options = options.map { it.label },
            label = stringResource(label),
            readOnly = true,
            errorMessage = state.errorFor(field),
            modifier = Modifier.fillMaxWidth().testTag(GroupLoanApplicationTestTags.dropdown(field)),
        )
    }
}

@Composable
private fun NumberField(
    field: GroupLoanField,
    label: StringResource,
    state: GroupLoanApplicationState,
    onAction: (GroupLoanApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(Modifier.height(KptTheme.spacing.md))
        AppOutlinedTextField(
            value = state.number(field),
            onValueChange = { onAction(GroupLoanApplicationAction.OnNumberChanged(field, it)) },
            label = stringResource(label),
            keyboardType = KeyboardType.Number,
            isError = field in state.errors,
            errorText = state.errorFor(field),
            modifier = Modifier.fillMaxWidth().testTag(GroupLoanApplicationTestTags.number(field)),
        )
    }
}

@Composable
private fun DateField(
    field: GroupLoanDate,
    label: StringResource,
    millis: Long,
    onAction: (GroupLoanApplicationAction) -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(Modifier.height(KptTheme.spacing.md))
        AppDatePickerTextField(
            value = DateHelper.getDateAsStringFromLong(millis),
            label = stringResource(label),
            openDatePicker = { onAction(GroupLoanApplicationAction.OpenDatePicker(field)) },
            modifier = Modifier.fillMaxWidth().testTag(tag),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationDatePicker(
    which: GroupLoanDate,
    state: GroupLoanApplicationState,
    onAction: (GroupLoanApplicationAction) -> Unit,
) {
    val initial = when (which) {
        GroupLoanDate.SUBMITTED_ON -> state.submittedOn
        GroupLoanDate.EXPECTED_DISBURSEMENT -> state.expectedDisbursement
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial,
        selectableDates = remember(which) {
            when (which) {
                // "Submitted on" is when the application is made. The original allowed FUTURE
                // dates only on both pickers, so today was unselectable here.
                GroupLoanDate.SUBMITTED_ON -> NotInTheFuture
                // A future disbursement is the whole point of this field.
                GroupLoanDate.EXPECTED_DISBURSEMENT -> AnyDate
            }
        },
    )

    DatePickerDialog(
        onDismissRequest = { onAction(GroupLoanApplicationAction.CloseDatePicker) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onAction(GroupLoanApplicationAction.OnDateChanged(which, it))
                    }
                    onAction(GroupLoanApplicationAction.CloseDatePicker)
                },
            ) { Text(stringResource(Res.string.feature_loan_select_date)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(GroupLoanApplicationAction.CloseDatePicker) }) {
                Text(stringResource(Res.string.feature_loan_cancel))
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
private object NotInTheFuture : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

@OptIn(ExperimentalMaterial3Api::class)
private object AnyDate : SelectableDates

/**
 * The error message for one field, or null.
 *
 * The ViewModel names WHICH field failed; the screen owns how it reads — the same split as the
 * transfer and reschedule-form screens.
 */
@Composable
internal fun GroupLoanApplicationState.errorFor(field: GroupLoanField): String? = when {
    field !in errors -> null
    field in GroupLoanField.numeric -> stringResource(Res.string.feature_loan_error_positive_number)
    else -> stringResource(Res.string.feature_loan_error_pick_an_option)
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object GroupLoanApplicationTestTags {
    const val SCREEN = "groupLoan:screen"
    const val CONTENT = "groupLoan:content"
    const val PRODUCT = "groupLoan:product"
    const val LOADING = "groupLoan:loading"
    const val TEMPLATE_ERROR = "groupLoan:templateError"
    const val SUBMITTED_ON = "groupLoan:submittedOn"
    const val DISBURSEMENT_DATE = "groupLoan:disbursementDate"
    const val EXACT_DAYS = "groupLoan:exactDays"
    const val SUBMIT = "groupLoan:submit"
    const val STATUS_DIALOG = "groupLoan:statusDialog"
    fun dropdown(field: GroupLoanField): String = "groupLoan:dropdown:${field.name}"
    fun number(field: GroupLoanField): String = "groupLoan:number:${field.name}"
}
