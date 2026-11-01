/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.fixeddepositapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.base.ui.submit.FormDraftBanner
import kpt.core.base.ui.submit.FormDraftFailure
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.savings.FixedDepositTemplate
import kpt.core.ui.components.AppTwoButtonRow
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import kpt.feature.client.generated.resources.feature_client_fdapp_deposit_amount
import kpt.feature.client.generated.resources.feature_client_fdapp_deposit_period
import kpt.feature.client.generated.resources.feature_client_fdapp_deposit_period_type
import kpt.feature.client.generated.resources.feature_client_fdapp_error_amount_required
import kpt.feature.client.generated.resources.feature_client_fdapp_error_failed
import kpt.feature.client.generated.resources.feature_client_fdapp_error_offline
import kpt.feature.client.generated.resources.feature_client_fdapp_error_period_required
import kpt.feature.client.generated.resources.feature_client_fdapp_error_period_type_required
import kpt.feature.client.generated.resources.feature_client_fdapp_error_product_required
import kpt.feature.client.generated.resources.feature_client_fdapp_external_id
import kpt.feature.client.generated.resources.feature_client_fdapp_field_officer
import kpt.feature.client.generated.resources.feature_client_fdapp_interest_calculation_type
import kpt.feature.client.generated.resources.feature_client_fdapp_interest_compounding
import kpt.feature.client.generated.resources.feature_client_fdapp_interest_days_in_year
import kpt.feature.client.generated.resources.feature_client_fdapp_interest_posting
import kpt.feature.client.generated.resources.feature_client_fdapp_lock_in_period
import kpt.feature.client.generated.resources.feature_client_fdapp_next
import kpt.feature.client.generated.resources.feature_client_fdapp_no_products
import kpt.feature.client.generated.resources.feature_client_fdapp_penal_applicable
import kpt.feature.client.generated.resources.feature_client_fdapp_penal_interest
import kpt.feature.client.generated.resources.feature_client_fdapp_product
import kpt.feature.client.generated.resources.feature_client_fdapp_step_of
import kpt.feature.client.generated.resources.feature_client_fdapp_title
import kpt.feature.client.generated.resources.feature_client_fdapp_transfer_interest
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Open a fixed-deposit account. See [FixedDepositApplicationViewModel] for the porting decisions,
 * including the eleven option-list indexes it removes.
 *
 * Carried over from `6b66e8a43:feature/client/newFixedDepositAccount/` — six pages (Details,
 * Terms, Settings, Interest, Charges, Preview), the step header with its progress bar, and the
 * Back/Next footer that becomes Back/Submit on the last page.
 *
 * ## §6a adaptations
 *
 * The template read becomes [ScreenState] through [ScreenContent]. Every dropdown reports the
 * CHOSEN option rather than an index into a list held elsewhere.
 *
 * The CHARGES page is read-only, for the same reason the share wizard's is: the original's
 * add-charge dialog is a separate composable this port does not have, and an add button that
 * could not work would be an inert affordance. Recorded alongside
 * `share-application-charges-not-editable`.
 */
@Composable
internal fun FixedDepositApplicationScreen(
    onNavigateBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FixedDepositApplicationViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val template by viewModel.template.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            FixedDepositApplicationEvent.NavigateBack -> onNavigateBack()
            FixedDepositApplicationEvent.Created -> onCreated()
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_fdapp_title),
        onBackPressed = { viewModel.trySendAction(FixedDepositApplicationAction.NavigateBack) },
    ) { scaffoldPadding ->
        FixedDepositApplicationContent(
            state = state,
            template = template,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
            offeredDraft = offeredDraft,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun FixedDepositApplicationContent(
    state: FixedDepositApplicationState,
    template: ScreenState<FixedDepositTemplate>,
    onAction: (FixedDepositApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    offeredDraft: SavedFormDraft? = null,
) {
    Column(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .testTag(FixedDepositApplicationTestTags.SCREEN),
    ) {
        StepHeader(state.step)

        ScreenContent(
            state = template,
            onRetry = { },
            modifier = Modifier.weight(1f),
        ) { options, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = KptTheme.spacing.md),
            ) {
                offeredDraft?.let {
                    FormDraftBanner(
                        failureReason = it.errorMessage,
                        onResume = { onAction(FixedDepositApplicationAction.ResumeDraft) },
                        onDiscard = { onAction(FixedDepositApplicationAction.DiscardDraft) },
                    )
                }
                when (state.step) {
                    FixedDepositStep.DETAILS -> DetailsPage(state, options, onAction)
                    FixedDepositStep.TERMS -> TermsPage(state, options, onAction)
                    FixedDepositStep.SETTINGS -> SettingsPage(state, options, onAction)
                    FixedDepositStep.INTEREST -> InterestPage(state, options, onAction)
                    FixedDepositStep.CHARGES -> ChargesPage(options)
                    FixedDepositStep.PREVIEW -> PreviewPage(state)
                }

                state.error?.let { error ->
                    Spacer(Modifier.height(KptTheme.spacing.sm))
                    FormDraftFailure(
                        message = stringResource(error.message),
                        serverReason = state.serverMessage,
                        draftSaved = state.draftSaved,
                        offline = error == FixedDepositError.Offline,
                        onSaveDraft = { onAction(FixedDepositApplicationAction.SaveDraft) },
                        modifier = Modifier.testTag(FixedDepositApplicationTestTags.ERROR),
                    )
                }

                Spacer(Modifier.height(KptTheme.spacing.lg))
            }
        }

        StepFooter(state, onAction)
    }
}

@Composable
private fun DetailsPage(
    state: FixedDepositApplicationState,
    template: FixedDepositTemplate,
    onAction: (FixedDepositApplicationAction) -> Unit,
) {
    Column {
        val products = template.productOptions.orEmpty()
        // Required: say so when the tenant has none, rather than an empty picker and a bare
        // "required" error the officer can do nothing about (gateway, 2026-10-26).
        if (products.isEmpty()) {
            Text(
                text = stringResource(Res.string.feature_client_fdapp_no_products),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().testTag(FixedDepositApplicationTestTags.NO_PRODUCTS),
            )
        }
        OptionDropdown(
            value = state.product?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_product),
            labels = products.mapNotNull { it.name },
            error = state.productError.errorOr(Res.string.feature_client_fdapp_error_product_required),
            onSelected = { index, label ->
                products.getOrNull(index)?.id?.let {
                    onAction(FixedDepositApplicationAction.UpdateProduct(FixedDepositOption(it, label)))
                }
            },
            tag = FixedDepositApplicationTestTags.PRODUCT,
        )

        val officers = template.fieldOfficerOptions.orEmpty()
        OptionDropdown(
            value = state.fieldOfficer?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_field_officer),
            labels = officers.mapNotNull { it.displayName },
            error = null,
            onSelected = { index, label ->
                officers.getOrNull(index)?.id?.let {
                    onAction(
                        FixedDepositApplicationAction.UpdateFieldOfficer(FixedDepositOption(it, label)),
                    )
                }
            },
            tag = FixedDepositApplicationTestTags.FIELD_OFFICER,
        )

        AppOutlinedTextField(
            value = state.externalId,
            onValueChange = { onAction(FixedDepositApplicationAction.UpdateExternalId(it)) },
            label = stringResource(Res.string.feature_client_fdapp_external_id),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TermsPage(
    state: FixedDepositApplicationState,
    template: FixedDepositTemplate,
    onAction: (FixedDepositApplicationAction) -> Unit,
) {
    Column {
        AppOutlinedTextField(
            value = state.depositAmount,
            onValueChange = { onAction(FixedDepositApplicationAction.UpdateDepositAmount(it)) },
            label = stringResource(Res.string.feature_client_fdapp_deposit_amount),
            isError = state.depositAmountError,
            errorText = state.depositAmountError
                .errorOr(Res.string.feature_client_fdapp_error_amount_required),
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.fillMaxWidth().testTag(FixedDepositApplicationTestTags.DEPOSIT_AMOUNT),
        )

        AppOutlinedTextField(
            value = state.depositPeriod,
            onValueChange = { onAction(FixedDepositApplicationAction.UpdateDepositPeriod(it)) },
            label = stringResource(Res.string.feature_client_fdapp_deposit_period),
            isError = state.depositPeriodError,
            errorText = state.depositPeriodError
                .errorOr(Res.string.feature_client_fdapp_error_period_required),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth().testTag(FixedDepositApplicationTestTags.DEPOSIT_PERIOD),
        )

        val periods = template.periodFrequencyTypeOptions.orEmpty()
        OptionDropdown(
            value = state.depositPeriodType?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_deposit_period_type),
            labels = periods.mapNotNull { it.value },
            error = state.depositPeriodTypeError
                .errorOr(Res.string.feature_client_fdapp_error_period_type_required),
            onSelected = { index, label ->
                periods.getOrNull(index)?.id?.let {
                    onAction(
                        FixedDepositApplicationAction.UpdateDepositPeriodType(
                            FixedDepositOption(it, label),
                        ),
                    )
                }
            },
            tag = FixedDepositApplicationTestTags.DEPOSIT_PERIOD_TYPE,
        )
    }
}

@Composable
private fun SettingsPage(
    state: FixedDepositApplicationState,
    template: FixedDepositTemplate,
    onAction: (FixedDepositApplicationAction) -> Unit,
) {
    Column {
        AppOutlinedTextField(
            value = state.lockInPeriod,
            onValueChange = { onAction(FixedDepositApplicationAction.UpdateLockInPeriod(it)) },
            label = stringResource(Res.string.feature_client_fdapp_lock_in_period),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth(),
        )

        val lockTypes = template.periodFrequencyTypeOptions.orEmpty()
        OptionDropdown(
            value = state.lockInPeriodType?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_deposit_period_type),
            labels = lockTypes.mapNotNull { it.value },
            error = null,
            onSelected = { index, label ->
                lockTypes.getOrNull(index)?.id?.let {
                    onAction(
                        FixedDepositApplicationAction.UpdateLockInPeriodType(
                            FixedDepositOption(it, label),
                        ),
                    )
                }
            },
            tag = FixedDepositApplicationTestTags.LOCK_IN_TYPE,
        )

        CheckboxRow(
            checked = state.transferInterestToSavings,
            label = stringResource(Res.string.feature_client_fdapp_transfer_interest),
            onChange = {
                onAction(FixedDepositApplicationAction.UpdateTransferInterestToSavings(it))
            },
            tag = FixedDepositApplicationTestTags.TRANSFER_INTEREST,
        )

        CheckboxRow(
            checked = state.penalInterestApplicable,
            label = stringResource(Res.string.feature_client_fdapp_penal_applicable),
            onChange = { onAction(FixedDepositApplicationAction.UpdatePenalInterestApplicable(it)) },
            tag = FixedDepositApplicationTestTags.PENAL_APPLICABLE,
        )

        // The rate is ABSENT until the penalty applies — a field whose value the payload would
        // discard is a field that should not be asked for.
        if (state.penalInterestApplicable) {
            AppOutlinedTextField(
                value = state.penalInterest,
                onValueChange = { onAction(FixedDepositApplicationAction.UpdatePenalInterest(it)) },
                label = stringResource(Res.string.feature_client_fdapp_penal_interest),
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth().testTag(FixedDepositApplicationTestTags.PENAL_RATE),
            )
        }
    }
}

@Composable
private fun InterestPage(
    state: FixedDepositApplicationState,
    template: FixedDepositTemplate,
    onAction: (FixedDepositApplicationAction) -> Unit,
) {
    Column {
        val compounding = template.interestCompoundingPeriodTypeOptions.orEmpty()
        OptionDropdown(
            value = state.interestCompoundingPeriod?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_interest_compounding),
            labels = compounding.mapNotNull { it.value },
            error = null,
            onSelected = { index, label ->
                compounding.getOrNull(index)?.id?.let {
                    onAction(
                        FixedDepositApplicationAction.UpdateInterestCompoundingPeriod(
                            FixedDepositOption(it, label),
                        ),
                    )
                }
            },
            tag = FixedDepositApplicationTestTags.INTEREST_COMPOUNDING,
        )

        val posting = template.interestPostingPeriodTypeOptions.orEmpty()
        OptionDropdown(
            value = state.interestPostingPeriod?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_interest_posting),
            labels = posting.mapNotNull { it.value },
            error = null,
            onSelected = { index, label ->
                posting.getOrNull(index)?.id?.let {
                    onAction(
                        FixedDepositApplicationAction.UpdateInterestPostingPeriod(
                            FixedDepositOption(it, label),
                        ),
                    )
                }
            },
            tag = FixedDepositApplicationTestTags.INTEREST_POSTING,
        )

        val calcType = template.interestCalculationTypeOptions.orEmpty()
        OptionDropdown(
            value = state.interestCalculationType?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_interest_calculation_type),
            labels = calcType.mapNotNull { it.value },
            error = null,
            onSelected = { index, label ->
                calcType.getOrNull(index)?.id?.let {
                    onAction(
                        FixedDepositApplicationAction.UpdateInterestCalculationType(
                            FixedDepositOption(it, label),
                        ),
                    )
                }
            },
            tag = FixedDepositApplicationTestTags.INTEREST_CALC_TYPE,
        )

        val daysInYear = template.interestCalculationDaysInYearTypeOptions.orEmpty()
        OptionDropdown(
            value = state.interestCalculationDaysInYear?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_interest_days_in_year),
            labels = daysInYear.mapNotNull { it.value },
            error = null,
            onSelected = { index, label ->
                daysInYear.getOrNull(index)?.id?.let {
                    onAction(
                        FixedDepositApplicationAction.UpdateInterestCalculationDaysInYear(
                            FixedDepositOption(it, label),
                        ),
                    )
                }
            },
            tag = FixedDepositApplicationTestTags.INTEREST_DAYS_IN_YEAR,
        )
    }
}

/** The product's charges, read-only — see the screen KDoc. */
@Composable
private fun ChargesPage(template: FixedDepositTemplate) {
    template.chargeOptions.orEmpty().forEach { charge ->
        Text(
            text = charge.name.orEmpty(),
            style = AppTypography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(vertical = KptTheme.spacing.xs),
        )
    }
}

/** What will be sent, read from the SAME state the payload is built from. */
@Composable
private fun PreviewPage(state: FixedDepositApplicationState) {
    Column {
        PreviewRow(stringResource(Res.string.feature_client_fdapp_product), state.product?.label.orEmpty())
        PreviewRow(stringResource(Res.string.feature_client_fdapp_deposit_amount), state.depositAmount)
        PreviewRow(stringResource(Res.string.feature_client_fdapp_deposit_period), state.depositPeriod)
        PreviewRow(
            stringResource(Res.string.feature_client_fdapp_deposit_period_type),
            state.depositPeriodType?.label.orEmpty(),
        )
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = AppTypography.bodyMedium,
        modifier = Modifier.fillMaxWidth().padding(vertical = KptTheme.spacing.xs),
    )
}

@Composable
private fun CheckboxRow(
    checked: Boolean,
    label: String,
    onChange: (Boolean) -> Unit,
    tag: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChange, modifier = Modifier.testTag(tag))
        Text(text = label, style = AppTypography.bodyMedium)
    }
}

/** One dropdown. The id is resolved from the SAME list that supplied the label. */
@Composable
private fun OptionDropdown(
    value: String,
    label: String,
    labels: List<String>,
    error: String?,
    onSelected: (index: Int, label: String) -> Unit,
    tag: String,
) {
    AppTextFieldDropdown(
        value = value,
        onOptionSelected = onSelected,
        options = labels,
        label = label,
        errorMessage = error,
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}

@Composable
private fun StepHeader(step: FixedDepositStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm)
            .testTag(FixedDepositApplicationTestTags.STEP_HEADER),
    ) {
        Text(
            text = stringResource(
                Res.string.feature_client_fdapp_step_of,
                step.ordinal + 1,
                FixedDepositStep.entries.size,
            ),
            style = AppTypography.labelLarge,
        )
        LinearProgressIndicator(
            progress = { (step.ordinal + 1).toFloat() / FixedDepositStep.entries.size },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepFooter(
    state: FixedDepositApplicationState,
    onAction: (FixedDepositApplicationAction) -> Unit,
) {
    val isLast = state.step == FixedDepositStep.entries.last()
    AppTwoButtonRow(
        firstBtnText = stringResource(Res.string.feature_client_btn_back),
        secondBtnText = stringResource(
            if (isLast) Res.string.feature_client_btn_submit else Res.string.feature_client_fdapp_next,
        ),
        onFirstBtnClick = {
            onAction(
                if (state.step == FixedDepositStep.DETAILS) {
                    FixedDepositApplicationAction.NavigateBack
                } else {
                    FixedDepositApplicationAction.PreviousStep
                },
            )
        },
        onSecondBtnClick = {
            onAction(
                if (isLast) {
                    FixedDepositApplicationAction.Submit
                } else {
                    FixedDepositApplicationAction.NextStep
                },
            )
        },
        isButtonIconVisible = false,
        modifier = Modifier.testTag(FixedDepositApplicationTestTags.STEP_FOOTER),
    )
}

@Composable
private fun Boolean.errorOr(resource: StringResource): String? =
    if (this) stringResource(resource) else null

private val FixedDepositError.message: StringResource
    get() = when (this) {
        FixedDepositError.Offline -> Res.string.feature_client_fdapp_error_offline
        FixedDepositError.Failed -> Res.string.feature_client_fdapp_error_failed
    }

internal object FixedDepositApplicationTestTags {
    const val NO_PRODUCTS = "fd-application:no-products"
    const val SCREEN = "FixedDepositApplicationScreen"
    const val STEP_HEADER = "FixedDepositApplicationStepHeader"
    const val STEP_FOOTER = "FixedDepositApplicationStepFooter"
    const val PRODUCT = "FixedDepositApplicationProduct"
    const val FIELD_OFFICER = "FixedDepositApplicationFieldOfficer"
    const val DEPOSIT_AMOUNT = "FixedDepositApplicationDepositAmount"
    const val DEPOSIT_PERIOD = "FixedDepositApplicationDepositPeriod"
    const val DEPOSIT_PERIOD_TYPE = "FixedDepositApplicationDepositPeriodType"
    const val LOCK_IN_TYPE = "FixedDepositApplicationLockInType"
    const val TRANSFER_INTEREST = "FixedDepositApplicationTransferInterest"
    const val PENAL_APPLICABLE = "FixedDepositApplicationPenalApplicable"
    const val PENAL_RATE = "FixedDepositApplicationPenalRate"
    const val INTEREST_COMPOUNDING = "FixedDepositApplicationInterestCompounding"
    const val INTEREST_POSTING = "FixedDepositApplicationInterestPosting"
    const val INTEREST_CALC_TYPE = "FixedDepositApplicationInterestCalcType"
    const val INTEREST_DAYS_IN_YEAR = "FixedDepositApplicationInterestDaysInYear"
    const val ERROR = "FixedDepositApplicationError"
}
