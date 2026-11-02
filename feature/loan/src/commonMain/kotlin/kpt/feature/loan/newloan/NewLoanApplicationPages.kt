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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppSweetError
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTypography
import kpt.core.ui.components.AppCheckBox
import kpt.core.ui.components.AppProgressIndicatorMini
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_amortization
import kpt.feature.loan.generated.resources.feature_loan_disbursed_date
import kpt.feature.loan.generated.resources.feature_loan_equal_amortization
import kpt.feature.loan.generated.resources.feature_loan_error_pick_an_option
import kpt.feature.loan.generated.resources.feature_loan_error_positive_number
import kpt.feature.loan.generated.resources.feature_loan_external_id
import kpt.feature.loan.generated.resources.feature_loan_fund
import kpt.feature.loan.generated.resources.feature_loan_interest_calculation_period
import kpt.feature.loan.generated.resources.feature_loan_interest_type_method
import kpt.feature.loan.generated.resources.feature_loan_link_savings
import kpt.feature.loan.generated.resources.feature_loan_nominal
import kpt.feature.loan.generated.resources.feature_loan_nominal_frequency
import kpt.feature.loan.generated.resources.feature_loan_number_of_repayments
import kpt.feature.loan.generated.resources.feature_loan_officer
import kpt.feature.loan.generated.resources.feature_loan_partial_period_interest
import kpt.feature.loan.generated.resources.feature_loan_principal
import kpt.feature.loan.generated.resources.feature_loan_product
import kpt.feature.loan.generated.resources.feature_loan_purpose
import kpt.feature.loan.generated.resources.feature_loan_repaid_every
import kpt.feature.loan.generated.resources.feature_loan_repayment_day_of_week
import kpt.feature.loan.generated.resources.feature_loan_repayment_frequency
import kpt.feature.loan.generated.resources.feature_loan_repayment_nth_day
import kpt.feature.loan.generated.resources.feature_loan_repayment_strategy
import kpt.feature.loan.generated.resources.feature_loan_schedule_offline
import kpt.feature.loan.generated.resources.feature_loan_schedule_rejected
import kpt.feature.loan.generated.resources.feature_loan_schedule_unavailable
import kpt.feature.loan.generated.resources.feature_loan_submission_date
import kpt.feature.loan.generated.resources.feature_loan_summary_disbursement
import kpt.feature.loan.generated.resources.feature_loan_summary_installments
import kpt.feature.loan.generated.resources.feature_loan_summary_interest
import kpt.feature.loan.generated.resources.feature_loan_summary_principal
import kpt.feature.loan.generated.resources.feature_loan_summary_total
import kpt.feature.loan.generated.resources.feature_loan_template_failed
import kpt.feature.loan.generated.resources.feature_loan_term
import kpt.feature.loan.schedule.ScheduleTable
import kpt.feature.loan.schedule.toScheduleTable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The wizard's four pages — ported from `6b66e8a43:.../newLoanAccount/pages/` (DetailsPage 292,
 * TermsPage 701, SchedulePage 109 and PreviewPage 292 lines; **ChargesPage 126 lines is ABSENT**,
 * because `LoansPayload` carries no charges and the page's values were discarded on submit — see
 * [NewLoanApplicationViewModel]).
 *
 * Each page is a plain scrolling Column. The original's pages were rendered inside a
 * `HorizontalPager`, which is what let a swipe reach a step whose predecessor had not validated.
 */

/** Step 1 — pick the product, then the reference fields its template offers. */
@Composable
internal fun DetailsPage(
    state: NewLoanApplicationState,
    productOptions: List<LoanProductOption>,
    onAction: (NewLoanApplicationAction) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FieldGap()

        // Choosing a product is what loads its template and prefills the terms, so the product on
        // screen and the terms on the next step can never disagree.
        AppTextFieldDropdown(
            value = state.selections[LoanField.PRODUCT]?.label.orEmpty(),
            onOptionSelected = { index, _ ->
                productOptions.getOrNull(index)
                    ?.let { onAction(NewLoanApplicationAction.OnProductSelected(it)) }
            },
            options = productOptions.map { it.name },
            label = stringResource(Res.string.feature_loan_product),
            readOnly = true,
            enabled = !state.isLoadingTemplate,
            errorMessage = state.errorFor(LoanField.PRODUCT),
            modifier = Modifier.fillMaxWidth().testTag(NewLoanApplicationTestTags.PRODUCT),
        )

        if (state.isLoadingTemplate) {
            FieldGap()
            AppProgressIndicatorMini(
                modifier = Modifier.testTag(NewLoanApplicationTestTags.TEMPLATE_LOADING),
            )
        }

        if (state.templateError) {
            FieldGap()
            AppSweetError(
                message = stringResource(Res.string.feature_loan_template_failed),
                onclick = { onAction(NewLoanApplicationAction.OnRetryTemplate) },
                modifier = Modifier.fillMaxWidth()
                    .testTag(NewLoanApplicationTestTags.TEMPLATE_ERROR),
            )
        }

        // The rest of the page exists only once a product's template has landed.
        val template = state.template ?: return@Column

        DateField(
            LoanDate.SUBMITTED_ON,
            Res.string.feature_loan_submission_date,
            state,
            onAction,
            NewLoanApplicationTestTags.SUBMITTED_ON,
        )
        DateField(
            LoanDate.EXPECTED_DISBURSEMENT,
            Res.string.feature_loan_disbursed_date,
            state,
            onAction,
            NewLoanApplicationTestTags.DISBURSEMENT_DATE,
        )

        FieldGap()
        AppOutlinedTextField(
            value = state.externalId,
            onValueChange = { onAction(NewLoanApplicationAction.OnExternalIdChanged(it)) },
            label = stringResource(Res.string.feature_loan_external_id),
            modifier = Modifier.fillMaxWidth()
                .testTag(NewLoanApplicationTestTags.EXTERNAL_ID),
        )

        // Each drawn only when the tenant configures options for it — the original's
        // `template.xOptions?.let { }`, kept. An unconfigured field is absent, not an empty list.
        Dropdown(
            LoanField.LOAN_PURPOSE,
            Res.string.feature_loan_purpose,
            template.loanPurposeOptions.options { it.id to it.name },
            state,
            onAction,
        )
        Dropdown(
            LoanField.LOAN_OFFICER,
            Res.string.feature_loan_officer,
            template.loanOfficerOptions.options { it.id to it.displayName },
            state,
            onAction,
        )
        Dropdown(
            LoanField.FUND,
            Res.string.feature_loan_fund,
            template.fundOptions.options { it.id to it.name },
            state,
            onAction,
        )
        Dropdown(
            LoanField.LINK_SAVINGS,
            Res.string.feature_loan_link_savings,
            template.accountLinkingOptions.options { it.id to it.accountNo },
            state,
            onAction,
        )

        FieldGap()
    }
}

/** Step 2 — the terms. Every field here is prefilled from the product and validated on Next. */
@Composable
internal fun TermsPage(
    state: NewLoanApplicationState,
    onAction: (NewLoanApplicationAction) -> Unit,
) {
    val template = state.template ?: return

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        NumberField(LoanField.PRINCIPAL, Res.string.feature_loan_principal, state, onAction)
        NumberField(LoanField.NUMBER_OF_REPAYMENTS, Res.string.feature_loan_number_of_repayments, state, onAction)
        NumberField(LoanField.REPAID_EVERY, Res.string.feature_loan_repaid_every, state, onAction)

        // Its OWN dropdown. The original fed the repaymentFrequencyType PAYLOAD field from the
        // loan TERM's dropdown, so a weekly repayment on a monthly term was unexpressible.
        Dropdown(
            LoanField.REPAYMENT_FREQUENCY_TYPE,
            Res.string.feature_loan_repayment_frequency,
            template.repaymentFrequencyTypeOptions.options { it.id to it.value },
            state,
            onAction,
        )
        Dropdown(
            LoanField.TERM_FREQUENCY_TYPE,
            Res.string.feature_loan_term,
            template.termFrequencyTypeOptions.options { it.id to it.value },
            state,
            onAction,
        )

        // Each from its own control. The original filled BOTH payload fields from the day-of-week
        // dropdown, so the nth-day field received a day-of-week value.
        Dropdown(
            LoanField.REPAYMENT_DAY_OF_WEEK,
            Res.string.feature_loan_repayment_day_of_week,
            template.repaymentFrequencyDaysOfWeekTypeOptions.options { it.id to it.value },
            state,
            onAction,
        )
        Dropdown(
            LoanField.REPAYMENT_NTH_DAY,
            Res.string.feature_loan_repayment_nth_day,
            template.repaymentFrequencyNthDayTypeOptions.options { it.id to it.value },
            state,
            onAction,
        )

        NumberField(LoanField.NOMINAL_INTEREST, Res.string.feature_loan_nominal, state, onAction)
        Dropdown(
            LoanField.NOMINAL_FREQUENCY,
            Res.string.feature_loan_nominal_frequency,
            template.interestRateFrequencyTypeOptions.options { it.id to it.value },
            state,
            onAction,
        )
        Dropdown(
            LoanField.INTEREST_METHOD,
            Res.string.feature_loan_interest_type_method,
            template.interestTypeOptions.options { it.id to it.value },
            state,
            onAction,
        )
        Dropdown(
            LoanField.AMORTIZATION,
            Res.string.feature_loan_amortization,
            template.amortizationTypeOptions.options { it.id to it.value },
            state,
            onAction,
        )

        FieldGap()
        Toggle(LoanToggle.EQUAL_AMORTIZATION, Res.string.feature_loan_equal_amortization, state, onAction)

        Dropdown(
            LoanField.INTEREST_CALCULATION_PERIOD,
            Res.string.feature_loan_interest_calculation_period,
            template.interestCalculationPeriodTypeOptions.options { it.id to it.value },
            state,
            onAction,
        )

        FieldGap()
        Toggle(LoanToggle.INTEREST_PARTIAL_PERIOD, Res.string.feature_loan_partial_period_interest, state, onAction)

        // The one option the payload identifies by CODE, so the projection carries it.
        Dropdown(
            field = LoanField.REPAYMENT_STRATEGY,
            label = Res.string.feature_loan_repayment_strategy,
            options = template.strategyOptions(),
            state = state,
            onAction = onAction,
        )

        FieldGap()
    }
}

/**
 * Step 3 — the schedule the SERVER calculates for terms that have not been submitted.
 *
 * Renders the same table as the existing loan-schedule screen, through the shared
 * `toScheduleTable` mapper, so the two cannot drift.
 */
@Composable
internal fun SchedulePage(
    state: NewLoanApplicationState,
    onAction: (NewLoanApplicationAction) -> Unit,
) {
    // NOT verticalScroll: the schedule table is a LazyColumn that takes the remaining height, and an
    // unbounded parent collapsed it to zero — the step showed only its summary bar (device, 2026-10-26).
    Column(Modifier.fillMaxSize()) {
        FieldGap()

        when {
            state.isLoadingSchedule -> AppProgressIndicatorMini(
                modifier = Modifier.testTag(NewLoanApplicationTestTags.SCHEDULE_LOADING),
            )

            state.scheduleError != null -> AppSweetError(
                message = when (val e = state.scheduleError) {
                    ScheduleError.Offline -> stringResource(Res.string.feature_loan_schedule_offline)
                    ScheduleError.Failed -> stringResource(Res.string.feature_loan_schedule_unavailable)
                    // The SERVER's own reason for refusing these terms — the officer needs it to
                    // know which term to change. The original replaced the wizard with a generic
                    // error page, discarding both the reason and the form.
                    is ScheduleError.Rejected ->
                        e.msg
                            ?: stringResource(Res.string.feature_loan_schedule_rejected)
                },
                onclick = { onAction(NewLoanApplicationAction.OnRetrySchedule) },
                modifier = Modifier.fillMaxWidth()
                    .testTag(NewLoanApplicationTestTags.SCHEDULE_ERROR),
            )

            state.schedule != null -> {
                val currency = state.schedule.currency
                ScheduleTable(
                    state.schedule.toScheduleTable(
                        currencyCode = currency?.code.orEmpty(),
                        maxDigits = currency?.decimalPlaces ?: DEFAULT_DECIMALS,
                    ),
                )
            }
        }

        FieldGap()
    }
}

/** Step 4 — what will be submitted, from the schedule the server returned. */
@Composable
internal fun PreviewPage(
    state: NewLoanApplicationState,
    onAction: (NewLoanApplicationAction) -> Unit,
) {
    // Same as the schedule step: the table scrolls itself and needs a bounded height to fill.
    Column(Modifier.fillMaxSize()) {
        FieldGap()

        val schedule = state.schedule
        if (schedule == null) {
            // Reachable by walking back, changing a term and forward again: the change clears the
            // preview, so there is nothing to summarise until it is recalculated.
            AppSweetError(
                message = stringResource(Res.string.feature_loan_schedule_unavailable),
                onclick = { onAction(NewLoanApplicationAction.OnRetrySchedule) },
                modifier = Modifier.fillMaxWidth()
                    .testTag(NewLoanApplicationTestTags.SCHEDULE_ERROR),
            )
            return@Column
        }

        val code = schedule.currency?.code.orEmpty()
        val digits = schedule.currency?.decimalPlaces ?: DEFAULT_DECIMALS

        Column(
            modifier = Modifier.fillMaxWidth().testTag(NewLoanApplicationTestTags.SUMMARY),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            SummaryRow(
                stringResource(Res.string.feature_loan_summary_disbursement),
                DateHelper.getDateAsStringFromLong(state.date(LoanDate.EXPECTED_DISBURSEMENT)),
            )
            SummaryRow(
                stringResource(Res.string.feature_loan_summary_installments),
                // From the SCHEDULE the server returned, not from the form's own count — they can
                // differ when the server adjusts for a grace period or a holiday calendar.
                schedule.getListOfActualPeriods().size.toString(),
            )
            // Every figure from `schedule`, the response in hand. The original's summary read
            // `state.repaymentSchedule` — the PREVIOUS calculation — while the new one sat in the
            // response, so the numbers shown belonged to the terms before the last edit.
            SummaryRow(
                stringResource(Res.string.feature_loan_summary_principal),
                amount(schedule.totalPrincipalExpected, code, digits),
            )
            SummaryRow(
                stringResource(Res.string.feature_loan_summary_interest),
                amount(schedule.totalInterestCharged, code, digits),
            )
            SummaryRow(
                stringResource(Res.string.feature_loan_summary_total),
                amount(schedule.totalRepaymentExpected, code, digits),
            )
        }

        FieldGap()
        ScheduleTable(schedule.toScheduleTable(code, digits))
        FieldGap()
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = AppTypography.bodyMedium)
        Text(text = value, style = AppTypography.labelLarge)
    }
}

/** A null total renders EMPTY, not "0.00" — the same distinction the schedule table makes. */
private fun amount(value: Double?, code: String, digits: Int): String {
    value ?: return ""
    return CurrencyFormatter.format(value, code, digits)
}

/**
 * Projects a template option list to (id, label), dropping entries with no id.
 *
 * No id means unsendable, so not selectable. The original rendered `it.value.toString()`, which
 * printed "null" for a nameless option and still let it be chosen.
 */
private inline fun <T> List<T>.options(select: (T) -> Pair<Int?, String?>): List<LoanOption> =
    mapNotNull { item ->
        val (id, label) = select(item)
        LoanOption(id = id ?: return@mapNotNull null, label = label.orEmpty())
    }

@Composable
private fun Dropdown(
    field: LoanField,
    label: StringResource,
    options: List<LoanOption>,
    state: NewLoanApplicationState,
    onAction: (NewLoanApplicationAction) -> Unit,
) {
    // Absent when the tenant configures no options for it.
    if (options.isEmpty()) return

    FieldGap()
    AppTextFieldDropdown(
        value = state.selections[field]?.label.orEmpty(),
        onOptionSelected = { index, _ ->
            options.getOrNull(index)
                ?.let { onAction(NewLoanApplicationAction.OnOptionSelected(field, it)) }
        },
        options = options.map { it.label },
        label = stringResource(label),
        readOnly = true,
        errorMessage = state.errorFor(field),
        modifier = Modifier.fillMaxWidth()
            .testTag(NewLoanApplicationTestTags.dropdown(field)),
    )
}

@Composable
private fun NumberField(
    field: LoanField,
    label: StringResource,
    state: NewLoanApplicationState,
    onAction: (NewLoanApplicationAction) -> Unit,
) {
    FieldGap()
    AppOutlinedTextField(
        value = state.number(field),
        onValueChange = { onAction(NewLoanApplicationAction.OnNumberChanged(field, it)) },
        label = stringResource(label),
        keyboardType = KeyboardType.Number,
        isError = field in state.errors,
        errorText = state.errorFor(field),
        modifier = Modifier.fillMaxWidth().testTag(NewLoanApplicationTestTags.number(field)),
    )
}

@Composable
private fun Toggle(
    toggle: LoanToggle,
    label: StringResource,
    state: NewLoanApplicationState,
    onAction: (NewLoanApplicationAction) -> Unit,
) {
    AppCheckBox(
        text = stringResource(label),
        checked = state.toggle(toggle),
        onCheckChanged = { onAction(NewLoanApplicationAction.OnToggled(toggle, it)) },
        modifier = Modifier.fillMaxWidth().testTag(NewLoanApplicationTestTags.toggle(toggle)),
    )
}

@Composable
private fun DateField(
    field: LoanDate,
    label: StringResource,
    state: NewLoanApplicationState,
    onAction: (NewLoanApplicationAction) -> Unit,
    tag: String,
) {
    FieldGap()
    AppDatePickerTextField(
        value = DateHelper.getDateAsStringFromLong(state.date(field)),
        label = stringResource(label),
        openDatePicker = { onAction(NewLoanApplicationAction.OpenDatePicker(field)) },
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}

/**
 * The error message for one field, or null.
 *
 * The ViewModel names WHICH field failed; the pages own how it reads — the same split as the
 * transfer, reschedule-form and group-loan screens.
 */
@Composable
internal fun NewLoanApplicationState.errorFor(field: LoanField): String? = when {
    field !in errors -> null
    field in LoanField.requiredPositive || field in LoanField.requiredNonNegative ->
        stringResource(Res.string.feature_loan_error_positive_number)
    else -> stringResource(Res.string.feature_loan_error_pick_an_option)
}

/** Fineract's own default when a currency declares no precision. */
private const val DEFAULT_DECIMALS = 2
