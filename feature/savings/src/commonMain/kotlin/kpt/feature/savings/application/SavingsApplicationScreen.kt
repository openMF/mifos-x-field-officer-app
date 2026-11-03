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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.AppTwoButtonRow
import kpt.core.ui.components.ResultStatus
import kpt.feature.savings.generated.resources.Res
import kpt.feature.savings.generated.resources.feature_savings_application_created
import kpt.feature.savings.generated.resources.feature_savings_application_failed
import kpt.feature.savings.generated.resources.feature_savings_application_group_title
import kpt.feature.savings.generated.resources.feature_savings_application_no_products
import kpt.feature.savings.generated.resources.feature_savings_application_no_products_title
import kpt.feature.savings.generated.resources.feature_savings_application_retry
import kpt.feature.savings.generated.resources.feature_savings_application_title
import kpt.feature.savings.generated.resources.feature_savings_back
import kpt.feature.savings.generated.resources.feature_savings_cancel
import kpt.feature.savings.generated.resources.feature_savings_close
import kpt.feature.savings.generated.resources.feature_savings_next
import kpt.feature.savings.generated.resources.feature_savings_offline_application
import kpt.feature.savings.generated.resources.feature_savings_select_date
import kpt.feature.savings.generated.resources.feature_savings_step_charges
import kpt.feature.savings.generated.resources.feature_savings_step_details
import kpt.feature.savings.generated.resources.feature_savings_step_of
import kpt.feature.savings.generated.resources.feature_savings_step_preview
import kpt.feature.savings.generated.resources.feature_savings_step_terms
import kpt.feature.savings.generated.resources.feature_savings_submit
import kpt.feature.savings.generated.resources.feature_savings_succeeded
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kpt.core.base.ui.generated.resources.Res as BaseRes

/**
 * The new savings application wizard — ported from
 * `6b66e8a43:feature/savings/savingsAccountv2/SavingsAccountScreen.kt` (374 lines).
 *
 * See [SavingsApplicationViewModel] for the dropped group path, the date/format mismatch, and why
 * the Charges page survives here when the loan wizard's identical-looking one had to be removed.
 *
 * ## §6a adaptations
 *
 * Paging was a `HorizontalPager` kept in sync with `state.currentStep`; the step is state and the
 * content is a `when` over it, so a swipe cannot bypass the validation Next runs. The outcome is a
 * dialog in state rather than a snackbar fired from the composition body.
 */
@Composable
internal fun SavingsApplicationScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavingsApplicationViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            SavingsApplicationEvent.NavigateBack -> onBackPressed()
            // Leaves AFTER the write, so the caller behind re-reads.
            SavingsApplicationEvent.Done -> onBackPressed()
        }
    }

    SavingsApplicationContent(
        state = state,
        products = products,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
        offeredDraft = offeredDraft,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun SavingsApplicationContent(
    state: SavingsApplicationState,
    products: ScreenState<List<SavingsProductOption>>,
    onAction: (SavingsApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
    offeredDraft: SavedFormDraft? = null,
) {
    AppScaffold(
        // Named for the OWNER, so an officer who opened this from group detail can see that it is
        // a group application. The original's title said "Savings Account" either way, which is
        // also the screen whose group path silently posted a client id.
        title = stringResource(
            if (state.isGroup) {
                Res.string.feature_savings_application_group_title
            } else {
                Res.string.feature_savings_application_title
            },
        ),
        // The top bar leaves the WIZARD; the footer's Back walks a step.
        onBackPressed = { onAction(SavingsApplicationAction.NavigateBack) },
        modifier = modifier.testTag(SavingsApplicationTestTags.SCREEN),
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            StepHeader(state.step)
            offeredDraft?.let {
                FormDraftBanner(
                    failureReason = it.errorMessage,
                    onResume = { onAction(SavingsApplicationAction.ResumeDraft) },
                    onDiscard = { onAction(SavingsApplicationAction.DiscardDraft) },
                    modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
                )
            }

            ScreenContent(
                state = products,
                onRetry = { onAction(SavingsApplicationAction.OnRetryProducts) },
                // An office with no product catalogue hits this BEFORE any field is drawn, so the
                // shared default ("Nothing here yet") would leave an officer staring at a blank
                // form with no stated cause and no next step. Name the cause, offer the recourse.
                empty = {
                    DefaultEmptyContent(
                        config = ScreenStateEmpty(
                            title = stringResource(Res.string.feature_savings_application_no_products_title),
                            message = stringResource(Res.string.feature_savings_application_no_products),
                            cta = ScreenStateCta(
                                label = stringResource(Res.string.feature_savings_application_retry),
                                onClick = { onAction(SavingsApplicationAction.OnRetryProducts) },
                            ),
                        ),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = KptTheme.spacing.md)
                    .testTag(SavingsApplicationTestTags.CONTENT),
            ) { productOptions, _ ->
                when (state.step) {
                    SavingsStep.DETAILS -> SavingsDetailsPage(state, productOptions, onAction)
                    SavingsStep.TERMS -> SavingsTermsPage(state, onAction)
                    SavingsStep.CHARGES -> SavingsChargesPage(state, onAction)
                    SavingsStep.PREVIEW -> SavingsPreviewPage(state)
                }
            }

            StepFooter(state, onAction)
        }
    }

    if (state.isDatePickerOpen) {
        ApplicationDatePicker(state, onAction)
    }

    ApplicationStatusDialog(state, onAction)
}

@Composable
private fun ApplicationStatusDialog(
    state: SavingsApplicationState,
    onAction: (SavingsApplicationAction) -> Unit,
) {
    when (val dialog = state.dialogState) {
        null -> Unit
        ApplicationDialog.Created -> AppStatusDialog(
            status = ResultStatus.SUCCESS,
            btnText = stringResource(Res.string.feature_savings_close),
            onConfirm = { onAction(SavingsApplicationAction.Done) },
            onDismissRequest = { onAction(SavingsApplicationAction.Done) },
            successTitle = stringResource(Res.string.feature_savings_succeeded),
            successMessage = stringResource(Res.string.feature_savings_application_created),
            failureTitle = stringResource(Res.string.feature_savings_application_failed),
            failureMessage = "",
            modifier = Modifier.fillMaxSize()
                .testTag(SavingsApplicationTestTags.STATUS_DIALOG),
        )

        is ApplicationDialog.Failed -> AppStatusDialog(
            status = ResultStatus.FAILURE,
            btnText = stringResource(Res.string.feature_savings_close),
            // Dismiss, keeping four pages of values.
            onConfirm = { onAction(SavingsApplicationAction.CloseDialog) },
            onDismissRequest = { onAction(SavingsApplicationAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_savings_succeeded),
            successMessage = "",
            failureTitle = stringResource(Res.string.feature_savings_application_failed),
            failureMessage = when {
                dialog.offline -> stringResource(Res.string.feature_savings_offline_application)
                else ->
                    dialog.msg
                        ?: stringResource(Res.string.feature_savings_application_failed)
            },
            modifier = Modifier.fillMaxSize()
                .testTag(SavingsApplicationTestTags.STATUS_DIALOG),
            secondaryBtnText = stringResource(BaseRes.string.draft_save).takeIf { !dialog.draftSaved },
            onSecondary = { onAction(SavingsApplicationAction.SaveDraft) },
            note = when {
                !dialog.draftSaved -> null
                dialog.offline -> stringResource(BaseRes.string.draft_saved_offline)
                else -> stringResource(BaseRes.string.draft_saved)
            },
        )
    }
}

/** Which of the four steps this is, named — a bare bar does not say where you are. */
@Composable
private fun StepHeader(step: SavingsStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm)
            .testTag(SavingsApplicationTestTags.STEP_HEADER),
    ) {
        Text(
            text = stringResource(
                Res.string.feature_savings_step_of,
                step.ordinal + 1,
                SavingsStep.entries.size,
                stringResource(step.label),
            ),
            style = AppTypography.labelLarge,
        )
        LinearProgressIndicator(
            progress = { (step.ordinal + 1).toFloat() / SavingsStep.entries.size },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Back / Next, with Next becoming Submit on the last step.
 *
 * Next stays ENABLED when fields are missing — tapping it is how the officer learns which, and the
 * ViewModel reports them on the step that owns them.
 */
@Composable
private fun StepFooter(
    state: SavingsApplicationState,
    onAction: (SavingsApplicationAction) -> Unit,
) {
    val isLast = state.step == SavingsStep.entries.last()

    AppTwoButtonRow(
        firstBtnText = stringResource(Res.string.feature_savings_back),
        secondBtnText = stringResource(
            if (isLast) Res.string.feature_savings_submit else Res.string.feature_savings_next,
        ),
        onFirstBtnClick = { onAction(SavingsApplicationAction.OnBack) },
        onSecondBtnClick = {
            onAction(
                if (isLast) {
                    SavingsApplicationAction.OnSubmit
                } else {
                    SavingsApplicationAction.OnNext
                },
            )
        },
        isButtonIconVisible = false,
        isSecondButtonEnabled = state.canAdvance,
        firstBtnTestTag = SavingsApplicationTestTags.BACK,
        secondBtnTestTag = SavingsApplicationTestTags.NEXT,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(DesignToken.spacing.dp44)
            .padding(KptTheme.spacing.md),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationDatePicker(
    state: SavingsApplicationState,
    onAction: (SavingsApplicationAction) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.submittedOn,
        // An application is submitted when it is made — today. The original allowed FUTURE dates
        // only, so today was unselectable: the same inverted rule as seven other screens.
        selectableDates = remember { NotInTheFuture },
    )

    DatePickerDialog(
        onDismissRequest = { onAction(SavingsApplicationAction.CloseDatePicker) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis
                        ?.let { onAction(SavingsApplicationAction.OnDateChanged(it)) }
                    onAction(SavingsApplicationAction.CloseDatePicker)
                },
            ) { Text(stringResource(Res.string.feature_savings_select_date)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(SavingsApplicationAction.CloseDatePicker) }) {
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

private val SavingsStep.label get() = when (this) {
    SavingsStep.DETAILS -> Res.string.feature_savings_step_details
    SavingsStep.TERMS -> Res.string.feature_savings_step_terms
    SavingsStep.CHARGES -> Res.string.feature_savings_step_charges
    SavingsStep.PREVIEW -> Res.string.feature_savings_step_preview
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object SavingsApplicationTestTags {
    const val SCREEN = "savingsApplication:screen"
    const val CONTENT = "savingsApplication:content"
    const val STEP_HEADER = "savingsApplication:stepHeader"
    const val BACK = "savingsApplication:back"
    const val NEXT = "savingsApplication:next"
    const val STATUS_DIALOG = "savingsApplication:statusDialog"
    const val PRODUCT = "savingsApplication:product"
    const val EXTERNAL_ID = "savingsApplication:externalId"
    const val SUBMITTED_ON = "savingsApplication:submittedOn"
    const val TEMPLATE_LOADING = "savingsApplication:templateLoading"
    const val TEMPLATE_ERROR = "savingsApplication:templateError"
    const val CHARGE_PICKER = "savingsApplication:chargePicker"
    const val CHARGE_AMOUNT = "savingsApplication:chargeAmount"
    const val CHARGE_ADD = "savingsApplication:chargeAdd"
    const val CHARGE_LIST = "savingsApplication:chargeList"
    const val NO_CHARGES = "savingsApplication:noCharges"
    const val SUMMARY = "savingsApplication:summary"
    fun dropdown(field: SavingsField): String = "savingsApplication:dropdown:${field.name}"
    fun number(field: SavingsField): String = "savingsApplication:number:${field.name}"
    fun toggle(toggle: SavingsToggle): String = "savingsApplication:toggle:${toggle.name}"
    fun chargeRow(index: Int): String = "savingsApplication:charge:$index"
}
