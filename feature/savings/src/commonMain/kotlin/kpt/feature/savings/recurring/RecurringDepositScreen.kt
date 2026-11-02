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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.base.ui.submit.FormDraftBanner
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppCheckBox
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.AppTwoButtonRow
import kpt.core.ui.components.ResultStatus
import kpt.feature.savings.generated.resources.Res
import kpt.feature.savings.generated.resources.feature_savings_back
import kpt.feature.savings.generated.resources.feature_savings_cancel
import kpt.feature.savings.generated.resources.feature_savings_charge_add
import kpt.feature.savings.generated.resources.feature_savings_charge_amount
import kpt.feature.savings.generated.resources.feature_savings_charge_remove
import kpt.feature.savings.generated.resources.feature_savings_charge_select
import kpt.feature.savings.generated.resources.feature_savings_close
import kpt.feature.savings.generated.resources.feature_savings_compounding_period
import kpt.feature.savings.generated.resources.feature_savings_days_in_year
import kpt.feature.savings.generated.resources.feature_savings_error_non_negative
import kpt.feature.savings.generated.resources.feature_savings_error_pick_an_option
import kpt.feature.savings.generated.resources.feature_savings_error_positive_number
import kpt.feature.savings.generated.resources.feature_savings_external_id
import kpt.feature.savings.generated.resources.feature_savings_field_officer
import kpt.feature.savings.generated.resources.feature_savings_interest_calculation_type
import kpt.feature.savings.generated.resources.feature_savings_lockin_period
import kpt.feature.savings.generated.resources.feature_savings_lockin_period_type
import kpt.feature.savings.generated.resources.feature_savings_next
import kpt.feature.savings.generated.resources.feature_savings_no_charges_added
import kpt.feature.savings.generated.resources.feature_savings_offline_application
import kpt.feature.savings.generated.resources.feature_savings_posting_period
import kpt.feature.savings.generated.resources.feature_savings_product
import kpt.feature.savings.generated.resources.feature_savings_rd_adjust_advance
import kpt.feature.savings.generated.resources.feature_savings_rd_allow_withdrawal
import kpt.feature.savings.generated.resources.feature_savings_rd_created
import kpt.feature.savings.generated.resources.feature_savings_rd_deposit_amount
import kpt.feature.savings.generated.resources.feature_savings_rd_deposit_period
import kpt.feature.savings.generated.resources.feature_savings_rd_deposit_period_type
import kpt.feature.savings.generated.resources.feature_savings_rd_failed
import kpt.feature.savings.generated.resources.feature_savings_rd_mandatory
import kpt.feature.savings.generated.resources.feature_savings_rd_penalty_interest
import kpt.feature.savings.generated.resources.feature_savings_rd_penalty_interest_on
import kpt.feature.savings.generated.resources.feature_savings_rd_pre_closure_penalty
import kpt.feature.savings.generated.resources.feature_savings_rd_recurring_frequency
import kpt.feature.savings.generated.resources.feature_savings_rd_recurring_frequency_type
import kpt.feature.savings.generated.resources.feature_savings_rd_step_interest
import kpt.feature.savings.generated.resources.feature_savings_rd_title
import kpt.feature.savings.generated.resources.feature_savings_recurring_no_products
import kpt.feature.savings.generated.resources.feature_savings_select_date
import kpt.feature.savings.generated.resources.feature_savings_step_charges
import kpt.feature.savings.generated.resources.feature_savings_step_details
import kpt.feature.savings.generated.resources.feature_savings_step_of
import kpt.feature.savings.generated.resources.feature_savings_step_preview
import kpt.feature.savings.generated.resources.feature_savings_step_terms
import kpt.feature.savings.generated.resources.feature_savings_submit
import kpt.feature.savings.generated.resources.feature_savings_submitted_on
import kpt.feature.savings.generated.resources.feature_savings_succeeded
import kpt.feature.savings.generated.resources.feature_savings_summary_charges
import kpt.feature.savings.generated.resources.feature_savings_summary_product
import kpt.feature.savings.generated.resources.feature_savings_summary_submitted
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kpt.core.base.ui.generated.resources.Res as BaseRes

/**
 * The new recurring-deposit application wizard — see [RecurringDepositViewModel] for the unguarded
 * `get(-1)` on the product, why this lives in `feature/savings`, and the `isCalendarInherited`
 * field that was a constant dressed as a setting.
 *
 * ## §6a adaptations
 *
 * The original's six `*Page` composables were driven by a `HorizontalPager` synced to
 * `state.currentStep`; the step is state and the content is a `when`. Its deeply nested state
 * (`state.recurringDepositAccountSettings.depositPeriod.periodType` and friends — four levels for
 * one dropdown index) flattens into one keyed map, which is what makes a stale index unexpressible.
 */
@Composable
internal fun RecurringDepositScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecurringDepositViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val template by viewModel.template.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            RecurringDepositEvent.NavigateBack -> onBackPressed()
            RecurringDepositEvent.Done -> onBackPressed()
        }
    }

    RecurringDepositContent(
        state = state,
        template = template,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
        offeredDraft = offeredDraft,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun RecurringDepositContent(
    state: RecurringDepositState,
    template: ScreenState<RecurringDepositForm>,
    onAction: (RecurringDepositAction) -> Unit,
    modifier: Modifier = Modifier,
    offeredDraft: SavedFormDraft? = null,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_savings_rd_title),
        onBackPressed = { onAction(RecurringDepositAction.NavigateBack) },
        modifier = modifier.testTag(RecurringDepositTestTags.SCREEN),
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            StepHeader(state.step)
            offeredDraft?.let {
                FormDraftBanner(
                    failureReason = it.errorMessage,
                    onResume = { onAction(RecurringDepositAction.ResumeDraft) },
                    onDiscard = { onAction(RecurringDepositAction.DiscardDraft) },
                    modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
                )
            }

            ScreenContent(
                state = template,
                onRetry = { onAction(RecurringDepositAction.OnRetry) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = KptTheme.spacing.md)
                    .testTag(RecurringDepositTestTags.CONTENT),
            ) { form, _ ->
                when (state.step) {
                    RecurringStep.DETAILS -> DetailsPage(state, form, onAction)
                    RecurringStep.TERMS -> TermsPage(state, form, onAction)
                    RecurringStep.INTEREST -> InterestPage(state, form, onAction)
                    RecurringStep.CHARGES -> ChargesPage(state, form, onAction)
                    RecurringStep.PREVIEW -> PreviewPage(state)
                }
            }

            StepFooter(state, onAction)
        }
    }

    if (state.isDatePickerOpen) {
        ApplicationDatePicker(state, onAction)
    }

    RecurringStatusDialog(state, onAction)
}

@Composable
private fun RecurringStatusDialog(
    state: RecurringDepositState,
    onAction: (RecurringDepositAction) -> Unit,
) {
    when (val dialog = state.dialogState) {
        null -> Unit
        RecurringDialog.Created -> AppStatusDialog(
            status = ResultStatus.SUCCESS,
            btnText = stringResource(Res.string.feature_savings_close),
            onConfirm = { onAction(RecurringDepositAction.Done) },
            onDismissRequest = { onAction(RecurringDepositAction.Done) },
            successTitle = stringResource(Res.string.feature_savings_succeeded),
            successMessage = stringResource(Res.string.feature_savings_rd_created),
            failureTitle = stringResource(Res.string.feature_savings_rd_failed),
            failureMessage = "",
            modifier = Modifier.fillMaxSize().testTag(RecurringDepositTestTags.STATUS_DIALOG),
        )

        is RecurringDialog.Failed -> AppStatusDialog(
            status = ResultStatus.FAILURE,
            btnText = stringResource(Res.string.feature_savings_close),
            // Dismiss, keeping five pages of values.
            onConfirm = { onAction(RecurringDepositAction.CloseDialog) },
            onDismissRequest = { onAction(RecurringDepositAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_savings_succeeded),
            successMessage = "",
            failureTitle = stringResource(Res.string.feature_savings_rd_failed),
            failureMessage = when {
                dialog.offline -> stringResource(Res.string.feature_savings_offline_application)
                else -> dialog.msg ?: stringResource(Res.string.feature_savings_rd_failed)
            },
            modifier = Modifier.fillMaxSize().testTag(RecurringDepositTestTags.STATUS_DIALOG),
            secondaryBtnText = stringResource(BaseRes.string.draft_save).takeIf { !dialog.draftSaved },
            onSecondary = { onAction(RecurringDepositAction.SaveDraft) },
            note = when {
                !dialog.draftSaved -> null
                dialog.offline -> stringResource(BaseRes.string.draft_saved_offline)
                else -> stringResource(BaseRes.string.draft_saved)
            },
        )
    }
}

/** Step 1 — the product, the officer, the date, an optional external id. */
@Composable
private fun DetailsPage(
    state: RecurringDepositState,
    form: RecurringDepositForm,
    onAction: (RecurringDepositAction) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Gap()
        // The product is REQUIRED, so its absence is said, not hidden: a tenant with no recurring
        // deposit products drew no field at all, and Next silently did nothing (gateway, 2026-10-26).
        if (form.products.isEmpty()) {
            Text(
                text = stringResource(Res.string.feature_savings_recurring_no_products),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.NO_PRODUCTS),
            )
        }
        Dropdown(RecurringField.PRODUCT, Res.string.feature_savings_product, form.products, state, onAction)
        Dropdown(
            RecurringField.FIELD_OFFICER,
            Res.string.feature_savings_field_officer,
            form.fieldOfficers,
            state,
            onAction,
        )

        Gap()
        AppDatePickerTextField(
            value = DateHelper.getDateAsStringFromLong(state.submittedOn),
            label = stringResource(Res.string.feature_savings_submitted_on),
            openDatePicker = { onAction(RecurringDepositAction.OpenDatePicker) },
            modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.SUBMITTED_ON),
        )

        Gap()
        AppOutlinedTextField(
            value = state.externalId,
            onValueChange = { onAction(RecurringDepositAction.OnExternalIdChanged(it)) },
            label = stringResource(Res.string.feature_savings_external_id),
            modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.EXTERNAL_ID),
        )
        Gap()
    }
}

/** Step 2 — how much, how often, for how long. */
@Composable
private fun TermsPage(
    state: RecurringDepositState,
    form: RecurringDepositForm,
    onAction: (RecurringDepositAction) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        NumberField(RecurringField.DEPOSIT_AMOUNT, Res.string.feature_savings_rd_deposit_amount, state, onAction)
        NumberField(
            RecurringField.RECURRING_FREQUENCY,
            Res.string.feature_savings_rd_recurring_frequency,
            state,
            onAction,
        )
        Dropdown(
            RecurringField.RECURRING_FREQUENCY_TYPE,
            Res.string.feature_savings_rd_recurring_frequency_type,
            form.periodFrequencies,
            state,
            onAction,
        )
        NumberField(RecurringField.DEPOSIT_PERIOD, Res.string.feature_savings_rd_deposit_period, state, onAction)
        // Its OWN dropdown, from the same option list as the recurring frequency but a separate
        // selection — the original kept them in two different nested state objects, which is how
        // one could end up holding an index from the other's list.
        Dropdown(
            RecurringField.DEPOSIT_PERIOD_TYPE,
            Res.string.feature_savings_rd_deposit_period_type,
            form.periodFrequencies,
            state,
            onAction,
        )

        Gap()
        Toggle(RecurringToggle.MANDATORY_DEPOSIT, Res.string.feature_savings_rd_mandatory, state, onAction)
        Toggle(RecurringToggle.ADJUST_ADVANCE_PAYMENTS, Res.string.feature_savings_rd_adjust_advance, state, onAction)
        Toggle(RecurringToggle.ALLOW_WITHDRAWAL, Res.string.feature_savings_rd_allow_withdrawal, state, onAction)
        Gap()
    }
}

/** Step 3 — interest, lock-in, and the pre-closure penalty. */
@Composable
private fun InterestPage(
    state: RecurringDepositState,
    form: RecurringDepositForm,
    onAction: (RecurringDepositAction) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Dropdown(
            RecurringField.COMPOUNDING_PERIOD,
            Res.string.feature_savings_compounding_period,
            form.compoundingPeriods,
            state,
            onAction,
        )
        Dropdown(
            RecurringField.POSTING_PERIOD,
            Res.string.feature_savings_posting_period,
            form.postingPeriods,
            state,
            onAction,
        )
        Dropdown(
            RecurringField.CALCULATION_TYPE,
            Res.string.feature_savings_interest_calculation_type,
            form.calculationTypes,
            state,
            onAction,
        )
        Dropdown(
            RecurringField.DAYS_IN_YEAR,
            Res.string.feature_savings_days_in_year,
            form.daysInYearTypes,
            state,
            onAction,
        )

        NumberField(RecurringField.LOCKIN_PERIOD, Res.string.feature_savings_lockin_period, state, onAction)
        Dropdown(
            RecurringField.LOCKIN_PERIOD_TYPE,
            Res.string.feature_savings_lockin_period_type,
            form.lockinFrequencies,
            state,
            onAction,
        )

        Gap()
        Toggle(RecurringToggle.PRE_CLOSURE_PENALTY, Res.string.feature_savings_rd_pre_closure_penalty, state, onAction)

        // The penalty's own fields appear ONLY when the penalty applies — and are sent only then.
        // The original rendered them always and sent whatever had been typed even with the
        // checkbox clear, so a penalty the officer had unchecked still reached the server.
        if (state.toggle(RecurringToggle.PRE_CLOSURE_PENALTY)) {
            NumberField(
                RecurringField.PENALTY_INTEREST,
                Res.string.feature_savings_rd_penalty_interest,
                state,
                onAction,
            )
            Dropdown(
                RecurringField.PENALTY_INTEREST_ON,
                Res.string.feature_savings_rd_penalty_interest_on,
                form.penaltyInterestOnTypes,
                state,
                onAction,
            )
        }
        Gap()
    }
}

/** Step 4 — charges, which DO reach the payload. */
@Composable
private fun ChargesPage(
    state: RecurringDepositState,
    form: RecurringDepositForm,
    onAction: (RecurringDepositAction) -> Unit,
) {
    var picked by remember { mutableStateOf<RecurringOption?>(null) }
    var amount by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Gap()

        if (form.charges.isNotEmpty()) {
            AppTextFieldDropdown(
                value = picked?.label.orEmpty(),
                onOptionSelected = { index, _ -> picked = form.charges.getOrNull(index) },
                options = form.charges.map { it.label },
                label = stringResource(Res.string.feature_savings_charge_select),
                readOnly = true,
                modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.CHARGE_PICKER),
            )

            Gap()
            AppOutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = stringResource(Res.string.feature_savings_charge_amount),
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.CHARGE_AMOUNT),
            )

            Gap()
            TextButton(
                // Both, and a positive amount: a charge row with no amount is not a charge.
                enabled = picked != null && amount.trim().toDoubleOrNull()?.let { it > 0 } == true,
                onClick = {
                    val chosen = picked ?: return@TextButton
                    onAction(
                        RecurringDepositAction.OnChargeAdded(
                            RecurringChargeRow(chosen.id, chosen.label, amount.trim()),
                        ),
                    )
                    picked = null
                    amount = ""
                },
                modifier = Modifier.testTag(RecurringDepositTestTags.CHARGE_ADD),
            ) { Text(stringResource(Res.string.feature_savings_charge_add)) }
        }

        Gap()

        if (state.charges.isEmpty()) {
            Text(
                text = stringResource(Res.string.feature_savings_no_charges_added),
                style = AppTypography.bodyMedium,
                modifier = Modifier.testTag(RecurringDepositTestTags.NO_CHARGES),
            )
            return@Column
        }

        Column(
            modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.CHARGE_LIST),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            state.charges.forEachIndexed { index, charge ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .testTag(RecurringDepositTestTags.chargeRow(index)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(charge.label, style = AppTypography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(charge.amount, style = AppTypography.labelLarge)
                    IconButton(onClick = { onAction(RecurringDepositAction.OnChargeRemoved(index)) }) {
                        Icon(
                            AppIcons.Delete,
                            contentDescription = stringResource(Res.string.feature_savings_charge_remove),
                        )
                    }
                }
            }
        }
        Gap()
    }
}

/** Step 5 — what will be submitted. */
@Composable
private fun PreviewPage(state: RecurringDepositState) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Gap()
        Column(
            modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.SUMMARY),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            SummaryRow(
                stringResource(Res.string.feature_savings_summary_product),
                state.selections[RecurringField.PRODUCT]?.label.orEmpty(),
            )
            SummaryRow(
                stringResource(Res.string.feature_savings_rd_deposit_amount),
                state.number(RecurringField.DEPOSIT_AMOUNT),
            )
            SummaryRow(
                stringResource(Res.string.feature_savings_rd_recurring_frequency),
                "${state.number(RecurringField.RECURRING_FREQUENCY)} " +
                    state.selections[RecurringField.RECURRING_FREQUENCY_TYPE]?.label.orEmpty(),
            )
            SummaryRow(
                stringResource(Res.string.feature_savings_rd_deposit_period),
                "${state.number(RecurringField.DEPOSIT_PERIOD)} " +
                    state.selections[RecurringField.DEPOSIT_PERIOD_TYPE]?.label.orEmpty(),
            )
            SummaryRow(
                stringResource(Res.string.feature_savings_summary_submitted),
                DateHelper.getDateAsStringFromLong(state.submittedOn),
            )
            SummaryRow(
                stringResource(Res.string.feature_savings_summary_charges),
                state.charges.size.toString(),
            )
        }
        Gap()
    }
}

@Composable
private fun StepHeader(step: RecurringStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm)
            .testTag(RecurringDepositTestTags.STEP_HEADER),
    ) {
        Text(
            text = stringResource(
                Res.string.feature_savings_step_of,
                step.ordinal + 1,
                RecurringStep.entries.size,
                stringResource(step.label),
            ),
            style = AppTypography.labelLarge,
        )
        LinearProgressIndicator(
            progress = { (step.ordinal + 1).toFloat() / RecurringStep.entries.size },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepFooter(
    state: RecurringDepositState,
    onAction: (RecurringDepositAction) -> Unit,
) {
    val isLast = state.step == RecurringStep.entries.last()

    AppTwoButtonRow(
        firstBtnText = stringResource(Res.string.feature_savings_back),
        secondBtnText = stringResource(
            if (isLast) Res.string.feature_savings_submit else Res.string.feature_savings_next,
        ),
        onFirstBtnClick = { onAction(RecurringDepositAction.OnBack) },
        onSecondBtnClick = {
            onAction(
                if (isLast) RecurringDepositAction.OnSubmit else RecurringDepositAction.OnNext,
            )
        },
        isButtonIconVisible = false,
        // Enabled even with fields missing — tapping it is how the officer learns which.
        isSecondButtonEnabled = state.canAdvance,
        firstBtnTestTag = RecurringDepositTestTags.BACK,
        secondBtnTestTag = RecurringDepositTestTags.NEXT,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(DesignToken.spacing.dp44)
            .padding(KptTheme.spacing.md),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationDatePicker(
    state: RecurringDepositState,
    onAction: (RecurringDepositAction) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.submittedOn,
        // An application is submitted when it is made. The original allowed FUTURE dates only, so
        // today was unselectable — the ninth screen in this migration with that inverted rule.
        selectableDates = remember { NotInTheFuture },
    )

    DatePickerDialog(
        onDismissRequest = { onAction(RecurringDepositAction.CloseDatePicker) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis
                        ?.let { onAction(RecurringDepositAction.OnDateChanged(it)) }
                    onAction(RecurringDepositAction.CloseDatePicker)
                },
            ) { Text(stringResource(Res.string.feature_savings_select_date)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(RecurringDepositAction.CloseDatePicker) }) {
                Text(stringResource(Res.string.feature_savings_cancel))
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

@Composable
private fun Dropdown(
    field: RecurringField,
    label: StringResource,
    options: List<RecurringOption>,
    state: RecurringDepositState,
    onAction: (RecurringDepositAction) -> Unit,
) {
    // Absent when the tenant configures no options for it.
    if (options.isEmpty()) return

    Gap()
    AppTextFieldDropdown(
        value = state.selections[field]?.label.orEmpty(),
        onOptionSelected = { index, _ ->
            options.getOrNull(index)
                ?.let { onAction(RecurringDepositAction.OnOptionSelected(field, it)) }
        },
        options = options.map { it.label },
        label = stringResource(label),
        readOnly = true,
        errorMessage = state.errorFor(field),
        modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.dropdown(field)),
    )
}

@Composable
private fun NumberField(
    field: RecurringField,
    label: StringResource,
    state: RecurringDepositState,
    onAction: (RecurringDepositAction) -> Unit,
) {
    Gap()
    AppOutlinedTextField(
        value = state.number(field),
        onValueChange = { onAction(RecurringDepositAction.OnNumberChanged(field, it)) },
        label = stringResource(label),
        keyboardType = KeyboardType.Decimal,
        isError = field in state.errors,
        errorText = state.errorFor(field),
        modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.number(field)),
    )
}

@Composable
private fun Toggle(
    toggle: RecurringToggle,
    label: StringResource,
    state: RecurringDepositState,
    onAction: (RecurringDepositAction) -> Unit,
) {
    AppCheckBox(
        text = stringResource(label),
        checked = state.toggle(toggle),
        onCheckChanged = { onAction(RecurringDepositAction.OnToggled(toggle, it)) },
        modifier = Modifier.fillMaxWidth().testTag(RecurringDepositTestTags.toggle(toggle)),
    )
}

@Composable
private fun Gap() = Spacer(Modifier.height(KptTheme.spacing.md))

private val RecurringStep.label get() = when (this) {
    RecurringStep.DETAILS -> Res.string.feature_savings_step_details
    RecurringStep.TERMS -> Res.string.feature_savings_step_terms
    RecurringStep.INTEREST -> Res.string.feature_savings_rd_step_interest
    RecurringStep.CHARGES -> Res.string.feature_savings_step_charges
    RecurringStep.PREVIEW -> Res.string.feature_savings_step_preview
}

/** The error message for one field, or null. */
@Composable
internal fun RecurringDepositState.errorFor(field: RecurringField): String? = when {
    field !in errors -> null
    field in RecurringField.requiredPositive || field == RecurringField.PENALTY_INTEREST ->
        stringResource(Res.string.feature_savings_error_positive_number)
    field in RecurringField.optionalNumbers ->
        stringResource(Res.string.feature_savings_error_non_negative)
    else -> stringResource(Res.string.feature_savings_error_pick_an_option)
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object RecurringDepositTestTags {
    const val NO_PRODUCTS = "recurring-deposit:no-products"
    const val SCREEN = "recurringDeposit:screen"
    const val CONTENT = "recurringDeposit:content"
    const val STEP_HEADER = "recurringDeposit:stepHeader"
    const val BACK = "recurringDeposit:back"
    const val NEXT = "recurringDeposit:next"
    const val STATUS_DIALOG = "recurringDeposit:statusDialog"
    const val SUBMITTED_ON = "recurringDeposit:submittedOn"
    const val EXTERNAL_ID = "recurringDeposit:externalId"
    const val CHARGE_PICKER = "recurringDeposit:chargePicker"
    const val CHARGE_AMOUNT = "recurringDeposit:chargeAmount"
    const val CHARGE_ADD = "recurringDeposit:chargeAdd"
    const val CHARGE_LIST = "recurringDeposit:chargeList"
    const val NO_CHARGES = "recurringDeposit:noCharges"
    const val SUMMARY = "recurringDeposit:summary"
    fun dropdown(field: RecurringField): String = "recurringDeposit:dropdown:${field.name}"
    fun number(field: RecurringField): String = "recurringDeposit:number:${field.name}"
    fun toggle(toggle: RecurringToggle): String = "recurringDeposit:toggle:${toggle.name}"
    fun chargeRow(index: Int): String = "recurringDeposit:charge:$index"
}
