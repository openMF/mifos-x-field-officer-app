/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppSweetError
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.objects.commonfiles.InterestType
import kpt.core.ui.components.AppCheckBox
import kpt.core.ui.components.AppProgressIndicatorMini
import kpt.feature.savings.generated.resources.Res
import kpt.feature.savings.generated.resources.feature_savings_allow_overdraft
import kpt.feature.savings.generated.resources.feature_savings_application_template_failed
import kpt.feature.savings.generated.resources.feature_savings_charge_add
import kpt.feature.savings.generated.resources.feature_savings_charge_amount
import kpt.feature.savings.generated.resources.feature_savings_charge_remove
import kpt.feature.savings.generated.resources.feature_savings_charge_select
import kpt.feature.savings.generated.resources.feature_savings_compounding_period
import kpt.feature.savings.generated.resources.feature_savings_days_in_year
import kpt.feature.savings.generated.resources.feature_savings_enforce_min_balance
import kpt.feature.savings.generated.resources.feature_savings_error_non_negative
import kpt.feature.savings.generated.resources.feature_savings_error_pick_an_option
import kpt.feature.savings.generated.resources.feature_savings_external_id
import kpt.feature.savings.generated.resources.feature_savings_field_officer
import kpt.feature.savings.generated.resources.feature_savings_interest_calculation_type
import kpt.feature.savings.generated.resources.feature_savings_lockin_period
import kpt.feature.savings.generated.resources.feature_savings_lockin_period_type
import kpt.feature.savings.generated.resources.feature_savings_min_balance
import kpt.feature.savings.generated.resources.feature_savings_no_charges_added
import kpt.feature.savings.generated.resources.feature_savings_opening_balance
import kpt.feature.savings.generated.resources.feature_savings_posting_period
import kpt.feature.savings.generated.resources.feature_savings_product
import kpt.feature.savings.generated.resources.feature_savings_submitted_on
import kpt.feature.savings.generated.resources.feature_savings_summary_charges
import kpt.feature.savings.generated.resources.feature_savings_summary_officer
import kpt.feature.savings.generated.resources.feature_savings_summary_owner_client
import kpt.feature.savings.generated.resources.feature_savings_summary_owner_group
import kpt.feature.savings.generated.resources.feature_savings_summary_product
import kpt.feature.savings.generated.resources.feature_savings_summary_submitted
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The wizard's four pages — ported from `6b66e8a43:.../savingsAccountv2/pages/` (DetailsPage 174,
 * TermsPage 309, ChargesPage 114, PreviewPage 198 lines).
 *
 * The **Charges page stays**, unlike the loan wizard's: `SavingsPayload` carries `charges` and the
 * original's builder mapped them, so the values reach the server. Worth stating because the two
 * pages look identical and the loan one had to be removed as a dead input.
 */

/** Step 1 — the product, the owner's officer, the date, and an optional external id. */
@Composable
internal fun SavingsDetailsPage(
    state: SavingsApplicationState,
    productOptions: List<SavingsProductOption>,
    onAction: (SavingsApplicationAction) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SavingsFieldGap()

        // Choosing a product loads ITS template and prefills the interest fields, so the product
        // on screen and the terms on the next step cannot disagree.
        AppTextFieldDropdown(
            value = state.selections[SavingsField.PRODUCT]?.label.orEmpty(),
            onOptionSelected = { index, _ ->
                productOptions.getOrNull(index)
                    ?.let { onAction(SavingsApplicationAction.OnProductSelected(it)) }
            },
            options = productOptions.map { it.name },
            label = stringResource(Res.string.feature_savings_product),
            readOnly = true,
            enabled = !state.isLoadingTemplate,
            errorMessage = state.errorFor(SavingsField.PRODUCT),
            modifier = Modifier.fillMaxWidth().testTag(SavingsApplicationTestTags.PRODUCT),
        )

        if (state.isLoadingTemplate) {
            SavingsFieldGap()
            AppProgressIndicatorMini(
                modifier = Modifier.testTag(SavingsApplicationTestTags.TEMPLATE_LOADING),
            )
        }

        if (state.templateError) {
            SavingsFieldGap()
            AppSweetError(
                message = stringResource(Res.string.feature_savings_application_template_failed),
                onclick = { onAction(SavingsApplicationAction.OnRetryTemplate) },
                modifier = Modifier.fillMaxWidth()
                    .testTag(SavingsApplicationTestTags.TEMPLATE_ERROR),
            )
        }

        // The rest exists only once a product's template has landed.
        val template = state.template ?: return@Column

        SavingsFieldGap()
        AppDatePickerTextField(
            value = DateHelper.getDateAsStringFromLong(state.submittedOn),
            label = stringResource(Res.string.feature_savings_submitted_on),
            openDatePicker = { onAction(SavingsApplicationAction.OpenDatePicker) },
            modifier = Modifier.fillMaxWidth()
                .testTag(SavingsApplicationTestTags.SUBMITTED_ON),
        )

        SavingsDropdown(
            field = SavingsField.FIELD_OFFICER,
            label = Res.string.feature_savings_field_officer,
            options = template.fieldOfficerOptions.orEmpty().mapNotNull { o ->
                SavingsOption(id = o.id ?: return@mapNotNull null, label = o.displayName.orEmpty())
            },
            state = state,
            onAction = onAction,
        )

        SavingsFieldGap()
        AppOutlinedTextField(
            value = state.externalId,
            onValueChange = { onAction(SavingsApplicationAction.OnExternalIdChanged(it)) },
            label = stringResource(Res.string.feature_savings_external_id),
            modifier = Modifier.fillMaxWidth()
                .testTag(SavingsApplicationTestTags.EXTERNAL_ID),
        )

        SavingsFieldGap()
    }
}

/** Step 2 — balances, lock-in, and the four interest fields Fineract requires. */
@Composable
internal fun SavingsTermsPage(
    state: SavingsApplicationState,
    onAction: (SavingsApplicationAction) -> Unit,
) {
    val template = state.template ?: return

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SavingsNumberField(SavingsField.OPENING_BALANCE, Res.string.feature_savings_opening_balance, state, onAction)
        SavingsNumberField(SavingsField.MINIMUM_BALANCE, Res.string.feature_savings_min_balance, state, onAction)
        SavingsNumberField(SavingsField.LOCKIN_PERIOD, Res.string.feature_savings_lockin_period, state, onAction)

        SavingsDropdown(
            field = SavingsField.LOCKIN_PERIOD_TYPE,
            label = Res.string.feature_savings_lockin_period_type,
            options = template.lockinPeriodFrequencyTypeOptions.orEmpty().toOptions(),
            state = state,
            onAction = onAction,
        )

        SavingsDropdown(
            SavingsField.COMPOUNDING_PERIOD,
            Res.string.feature_savings_compounding_period,
            template.interestCompoundingPeriodTypeOptions.orEmpty().toOptions(),
            state,
            onAction,
        )
        SavingsDropdown(
            SavingsField.POSTING_PERIOD,
            Res.string.feature_savings_posting_period,
            template.interestPostingPeriodTypeOptions.orEmpty().toOptions(),
            state,
            onAction,
        )
        SavingsDropdown(
            SavingsField.CALCULATION_TYPE,
            Res.string.feature_savings_interest_calculation_type,
            template.interestCalculationTypeOptions.orEmpty().toOptions(),
            state,
            onAction,
        )
        SavingsDropdown(
            SavingsField.DAYS_IN_YEAR,
            Res.string.feature_savings_days_in_year,
            template.interestCalculationDaysInYearTypeOptions.orEmpty().toOptions(),
            state,
            onAction,
        )

        SavingsFieldGap()
        SavingsToggleRow(SavingsToggle.ALLOW_OVERDRAFT, Res.string.feature_savings_allow_overdraft, state, onAction)
        SavingsToggleRow(
            SavingsToggle.ENFORCE_MIN_BALANCE,
            Res.string.feature_savings_enforce_min_balance,
            state,
            onAction,
        )

        SavingsFieldGap()
    }
}

/**
 * Step 3 — charges, which DO reach the payload.
 *
 * Pick a charge, give it an amount, add it. The original kept the picked charge and the typed
 * amount in two `rememberSaveable` locals and appended on a button; the pair lives in local state
 * here for the same reason, but the LIST is in the ViewModel because it is what gets submitted.
 */
@Composable
internal fun SavingsChargesPage(
    state: SavingsApplicationState,
    onAction: (SavingsApplicationAction) -> Unit,
) {
    val template = state.template ?: return
    val options = template.chargeOptions.orEmpty().mapNotNull { o ->
        SavingsOption(id = o.id ?: return@mapNotNull null, label = o.name.orEmpty())
    }

    var picked by remember { mutableStateOf<SavingsOption?>(null) }
    var amount by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SavingsFieldGap()

        // Absent when the tenant configures no charges for this product.
        if (options.isNotEmpty()) {
            AppTextFieldDropdown(
                value = picked?.label.orEmpty(),
                onOptionSelected = { index, _ -> picked = options.getOrNull(index) },
                options = options.map { it.label },
                label = stringResource(Res.string.feature_savings_charge_select),
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
                    .testTag(SavingsApplicationTestTags.CHARGE_PICKER),
            )

            SavingsFieldGap()
            AppOutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = stringResource(Res.string.feature_savings_charge_amount),
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth()
                    .testTag(SavingsApplicationTestTags.CHARGE_AMOUNT),
            )

            SavingsFieldGap()
            TextButton(
                // Enabled only with BOTH a charge and a positive amount: a charge row with no
                // amount is not a charge, and the original appended whatever was typed.
                enabled = picked != null && amount.trim().toDoubleOrNull()?.let { it > 0 } == true,
                onClick = {
                    val chosen = picked ?: return@TextButton
                    onAction(
                        SavingsApplicationAction.OnChargeAdded(
                            SavingsChargeRow(
                                chargeId = chosen.id,
                                label = chosen.label,
                                amount = amount.trim(),
                            ),
                        ),
                    )
                    picked = null
                    amount = ""
                },
                modifier = Modifier.testTag(SavingsApplicationTestTags.CHARGE_ADD),
            ) { Text(stringResource(Res.string.feature_savings_charge_add)) }
        }

        SavingsFieldGap()

        if (state.charges.isEmpty()) {
            // Charges are OPTIONAL — said plainly, so a blank page does not read as a failure.
            Text(
                text = stringResource(Res.string.feature_savings_no_charges_added),
                style = AppTypography.bodyMedium,
                modifier = Modifier.testTag(SavingsApplicationTestTags.NO_CHARGES),
            )
            return@Column
        }

        Column(
            modifier = Modifier.fillMaxWidth().testTag(SavingsApplicationTestTags.CHARGE_LIST),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            state.charges.forEachIndexed { index, charge ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .testTag(SavingsApplicationTestTags.chargeRow(index)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = charge.label, style = AppTypography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(text = charge.amount, style = AppTypography.labelLarge)
                    IconButton(onClick = { onAction(SavingsApplicationAction.OnChargeRemoved(index)) }) {
                        Icon(
                            AppIcons.Delete,
                            contentDescription = stringResource(Res.string.feature_savings_charge_remove),
                        )
                    }
                }
            }
        }

        SavingsFieldGap()
    }
}

/** Step 4 — what will be submitted. */
@Composable
internal fun SavingsPreviewPage(state: SavingsApplicationState) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SavingsFieldGap()

        Column(
            modifier = Modifier.fillMaxWidth().testTag(SavingsApplicationTestTags.SUMMARY),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            // States the OWNER, because this wizard serves both and the original's group path
            // silently submitted a client id.
            SummaryRow(
                stringResource(
                    if (state.isGroup) {
                        Res.string.feature_savings_summary_owner_group
                    } else {
                        Res.string.feature_savings_summary_owner_client
                    },
                ),
                state.ownerId.toString(),
            )
            SummaryRow(
                stringResource(Res.string.feature_savings_summary_product),
                state.selections[SavingsField.PRODUCT]?.label.orEmpty(),
            )
            SummaryRow(
                stringResource(Res.string.feature_savings_summary_submitted),
                DateHelper.getDateAsStringFromLong(state.submittedOn),
            )
            state.selections[SavingsField.FIELD_OFFICER]?.let {
                SummaryRow(stringResource(Res.string.feature_savings_summary_officer), it.label)
            }
            SummaryRow(
                stringResource(Res.string.feature_savings_summary_charges),
                state.charges.size.toString(),
            )
        }

        SavingsFieldGap()
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

/**
 * `{id, value}` option lists → selectable options, dropping entries with no id.
 *
 * No id means unsendable, so not selectable.
 */
private fun List<InterestType>.toOptions(): List<SavingsOption> =
    mapNotNull { o -> SavingsOption(id = o.id ?: return@mapNotNull null, label = o.value.orEmpty()) }

@Composable
private fun SavingsDropdown(
    field: SavingsField,
    label: StringResource,
    options: List<SavingsOption>,
    state: SavingsApplicationState,
    onAction: (SavingsApplicationAction) -> Unit,
) {
    // Absent when the tenant configures no options for it.
    if (options.isEmpty()) return

    SavingsFieldGap()
    AppTextFieldDropdown(
        value = state.selections[field]?.label.orEmpty(),
        onOptionSelected = { index, _ ->
            options.getOrNull(index)
                ?.let { onAction(SavingsApplicationAction.OnOptionSelected(field, it)) }
        },
        options = options.map { it.label },
        label = stringResource(label),
        readOnly = true,
        errorMessage = state.errorFor(field),
        modifier = Modifier.fillMaxWidth()
            .testTag(SavingsApplicationTestTags.dropdown(field)),
    )
}

@Composable
private fun SavingsNumberField(
    field: SavingsField,
    label: StringResource,
    state: SavingsApplicationState,
    onAction: (SavingsApplicationAction) -> Unit,
) {
    SavingsFieldGap()
    AppOutlinedTextField(
        value = state.number(field),
        onValueChange = { onAction(SavingsApplicationAction.OnNumberChanged(field, it)) },
        label = stringResource(label),
        keyboardType = KeyboardType.Decimal,
        isError = field in state.errors,
        errorText = state.errorFor(field),
        modifier = Modifier.fillMaxWidth()
            .testTag(SavingsApplicationTestTags.number(field)),
    )
}

@Composable
private fun SavingsToggleRow(
    toggle: SavingsToggle,
    label: StringResource,
    state: SavingsApplicationState,
    onAction: (SavingsApplicationAction) -> Unit,
) {
    AppCheckBox(
        text = stringResource(label),
        checked = state.toggle(toggle),
        onCheckChanged = { onAction(SavingsApplicationAction.OnToggled(toggle, it)) },
        modifier = Modifier.fillMaxWidth()
            .testTag(SavingsApplicationTestTags.toggle(toggle)),
    )
}

/**
 * The error message for one field, or null.
 *
 * The ViewModel names WHICH field failed; the pages own how it reads — the same split as every
 * other form in this port.
 */
@Composable
internal fun SavingsApplicationState.errorFor(field: SavingsField): String? = when {
    field !in errors -> null
    field in SavingsField.optionalNumbers -> stringResource(Res.string.feature_savings_error_non_negative)
    else -> stringResource(Res.string.feature_savings_error_pick_an_option)
}

/** Spacer the pages share, so every field sits on the same rhythm. */
@Composable
internal fun SavingsFieldGap() = Spacer(Modifier.height(KptTheme.spacing.md))
