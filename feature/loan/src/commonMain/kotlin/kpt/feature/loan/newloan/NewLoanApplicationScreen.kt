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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_application
import kpt.feature.loan.generated.resources.feature_loan_application_created_message
import kpt.feature.loan.generated.resources.feature_loan_application_created_title
import kpt.feature.loan.generated.resources.feature_loan_application_failure_title
import kpt.feature.loan.generated.resources.feature_loan_application_no_products
import kpt.feature.loan.generated.resources.feature_loan_application_no_products_title
import kpt.feature.loan.generated.resources.feature_loan_back
import kpt.feature.loan.generated.resources.feature_loan_cancel
import kpt.feature.loan.generated.resources.feature_loan_dialog_continue
import kpt.feature.loan.generated.resources.feature_loan_next
import kpt.feature.loan.generated.resources.feature_loan_retry
import kpt.feature.loan.generated.resources.feature_loan_select_date
import kpt.feature.loan.generated.resources.feature_loan_step_details
import kpt.feature.loan.generated.resources.feature_loan_step_of
import kpt.feature.loan.generated.resources.feature_loan_step_preview
import kpt.feature.loan.generated.resources.feature_loan_step_schedule
import kpt.feature.loan.generated.resources.feature_loan_step_terms
import kpt.feature.loan.generated.resources.feature_loan_submit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kpt.core.base.ui.generated.resources.Res as BaseRes

/**
 * The new INDIVIDUAL loan application wizard — ported from
 * `6b66e8a43:feature/loan/newLoanAccount/NewLoanAccountScreen.kt` (538 lines) plus its pages.
 *
 * See [NewLoanApplicationViewModel] for why this is FOUR steps rather than five, why there is one
 * payload builder rather than two, and why the twelve parallel dropdown indices became one keyed
 * map.
 *
 * ## §6a adaptations
 *
 * The original drove paging with a `HorizontalPager` + `rememberPagerState` kept in sync with
 * `state.currentStep` by two `LaunchedEffect`s in both directions, which let the two disagree
 * mid-swipe. The step is state, and the content is a `when` over it — a swipe cannot skip the
 * validation that Next runs.
 *
 * The original's `launchEffectKey = Random.nextInt()` existed to re-fire a `LaunchedEffect` that
 * showed a snackbar from the composition. The outcome is a dialog in state instead.
 */
@Composable
internal fun NewLoanApplicationScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewLoanApplicationViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            NewLoanApplicationEvent.NavigateBack -> onBackPressed()
        }
    }

    NewLoanApplicationContent(
        state = state,
        products = products,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
        offeredDraft = offeredDraft,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun NewLoanApplicationContent(
    state: NewLoanApplicationState,
    products: ScreenState<List<LoanProductOption>>,
    onAction: (NewLoanApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
    offeredDraft: SavedFormDraft? = null,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_application),
        // Back on the top bar leaves the WIZARD; the footer's Back walks a step. The original wired
        // both to the same callback, so the top bar abandoned a four-page form without warning on
        // what looked like a step-back.
        onBackPressed = { onAction(NewLoanApplicationAction.NavigateBack) },
        modifier = modifier.testTag(NewLoanApplicationTestTags.SCREEN),
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            StepHeader(state.step)
            offeredDraft?.let {
                FormDraftBanner(
                    failureReason = it.errorMessage,
                    onResume = { onAction(NewLoanApplicationAction.ResumeDraft) },
                    onDiscard = { onAction(NewLoanApplicationAction.DiscardDraft) },
                    modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
                )
            }

            ScreenContent(
                state = products,
                onRetry = { onAction(NewLoanApplicationAction.OnRetryProducts) },
                // An office with no product catalogue hits this BEFORE any field is drawn, so the
                // shared default ("Nothing here yet") would leave an officer staring at a blank
                // form with no stated cause and no next step. Name the cause, offer the recourse.
                empty = {
                    DefaultEmptyContent(
                        config = ScreenStateEmpty(
                            title = stringResource(Res.string.feature_loan_application_no_products_title),
                            message = stringResource(Res.string.feature_loan_application_no_products),
                            cta = ScreenStateCta(
                                label = stringResource(Res.string.feature_loan_retry),
                                onClick = { onAction(NewLoanApplicationAction.OnRetryProducts) },
                            ),
                        ),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = KptTheme.spacing.md)
                    .testTag(NewLoanApplicationTestTags.CONTENT),
            ) { productOptions, _ ->
                when (state.step) {
                    LoanStep.DETAILS -> DetailsPage(state, productOptions, onAction)
                    LoanStep.TERMS -> TermsPage(state, onAction)
                    LoanStep.SCHEDULE -> SchedulePage(state, onAction)
                    LoanStep.PREVIEW -> PreviewPage(state, onAction)
                }
            }

            StepFooter(state, onAction)
        }
    }

    state.activeDatePicker?.let { which -> ApplicationDatePicker(which, state, onAction) }

    when (val dialog = state.dialogState) {
        null -> Unit
        NewLoanApplicationState.DialogState.Created -> AppStatusDialog(
            status = ResultStatus.SUCCESS,
            btnText = stringResource(Res.string.feature_loan_dialog_continue),
            onConfirm = { onAction(NewLoanApplicationAction.NavigateBack) },
            onDismissRequest = { onAction(NewLoanApplicationAction.NavigateBack) },
            successTitle = stringResource(Res.string.feature_loan_application_created_title),
            successMessage = stringResource(Res.string.feature_loan_application_created_message),
            failureTitle = stringResource(Res.string.feature_loan_application_failure_title),
            failureMessage = "",
            modifier = Modifier.fillMaxSize().testTag(NewLoanApplicationTestTags.STATUS_DIALOG),
        )

        is NewLoanApplicationState.DialogState.Failed -> AppStatusDialog(
            status = ResultStatus.FAILURE,
            btnText = stringResource(Res.string.feature_loan_dialog_continue),
            // Dismiss, keeping every value. A rejected application is four pages to retype, and
            // the original replaced the whole wizard with an error screen.
            onConfirm = { onAction(NewLoanApplicationAction.CloseDialog) },
            onDismissRequest = { onAction(NewLoanApplicationAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_loan_application_created_title),
            successMessage = stringResource(Res.string.feature_loan_application_created_message),
            failureTitle = stringResource(Res.string.feature_loan_application_failure_title),
            failureMessage = dialog.msg,
            modifier = Modifier.fillMaxSize().testTag(NewLoanApplicationTestTags.STATUS_DIALOG),
            secondaryBtnText = stringResource(BaseRes.string.draft_save).takeIf { !dialog.draftSaved },
            onSecondary = { onAction(NewLoanApplicationAction.SaveDraft) },
            note = when {
                !dialog.draftSaved -> null
                dialog.offline -> stringResource(BaseRes.string.draft_saved_offline)
                else -> stringResource(BaseRes.string.draft_saved)
            },
        )
    }
}

/** Which of the four steps this is, named — a bare progress bar does not say where you are. */
@Composable
private fun StepHeader(step: LoanStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm)
            .testTag(NewLoanApplicationTestTags.STEP_HEADER),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
    ) {
        Text(
            text = stringResource(
                Res.string.feature_loan_step_of,
                step.ordinal + 1,
                LoanStep.entries.size,
                stringResource(step.label),
            ),
            style = AppTypography.labelLarge,
        )
        LinearProgressIndicator(
            progress = { (step.ordinal + 1).toFloat() / LoanStep.entries.size },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Back / Next, with Next becoming Submit on the last step.
 *
 * Next stays ENABLED when fields are missing — tapping it is how the officer learns WHICH, and the
 * ViewModel reports them on the step that owns them. It is disabled only while a write or a
 * template read is actually in flight.
 */
@Composable
private fun StepFooter(
    state: NewLoanApplicationState,
    onAction: (NewLoanApplicationAction) -> Unit,
) {
    val isLast = state.step == LoanStep.entries.last()

    AppTwoButtonRow(
        firstBtnText = stringResource(Res.string.feature_loan_back),
        secondBtnText = stringResource(
            if (isLast) Res.string.feature_loan_submit else Res.string.feature_loan_next,
        ),
        onFirstBtnClick = { onAction(NewLoanApplicationAction.OnBack) },
        onSecondBtnClick = {
            onAction(
                if (isLast) NewLoanApplicationAction.OnSubmit else NewLoanApplicationAction.OnNext,
            )
        },
        isButtonIconVisible = false,
        isSecondButtonEnabled = state.canAdvance,
        firstBtnTestTag = NewLoanApplicationTestTags.BACK,
        secondBtnTestTag = NewLoanApplicationTestTags.NEXT,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(DesignToken.spacing.dp44)
            .padding(KptTheme.spacing.md),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationDatePicker(
    which: LoanDate,
    state: NewLoanApplicationState,
    onAction: (NewLoanApplicationAction) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.date(which),
        selectableDates = remember(which) {
            when (which) {
                // "Submitted on" is when the application is made. The original restricted BOTH
                // pickers to future dates only, so today was unselectable here — the same inverted
                // rule as four other screens in this migration.
                LoanDate.SUBMITTED_ON -> NotInTheFuture
                // A future disbursement is the whole point of the field.
                LoanDate.EXPECTED_DISBURSEMENT -> AnyDate
            }
        },
    )

    DatePickerDialog(
        onDismissRequest = { onAction(NewLoanApplicationAction.CloseDatePicker) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis
                        ?.let { onAction(NewLoanApplicationAction.OnDateChanged(which, it)) }
                    onAction(NewLoanApplicationAction.CloseDatePicker)
                },
            ) { Text(stringResource(Res.string.feature_loan_select_date)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(NewLoanApplicationAction.CloseDatePicker) }) {
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

/** Each step's own heading. */
private val LoanStep.label get() = when (this) {
    LoanStep.DETAILS -> Res.string.feature_loan_step_details
    LoanStep.TERMS -> Res.string.feature_loan_step_terms
    LoanStep.SCHEDULE -> Res.string.feature_loan_step_schedule
    LoanStep.PREVIEW -> Res.string.feature_loan_step_preview
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object NewLoanApplicationTestTags {
    const val SCREEN = "newLoan:screen"
    const val CONTENT = "newLoan:content"
    const val STEP_HEADER = "newLoan:stepHeader"
    const val BACK = "newLoan:back"
    const val NEXT = "newLoan:next"
    const val STATUS_DIALOG = "newLoan:statusDialog"
    const val PRODUCT = "newLoan:product"
    const val EXTERNAL_ID = "newLoan:externalId"
    const val SUBMITTED_ON = "newLoan:submittedOn"
    const val DISBURSEMENT_DATE = "newLoan:disbursementDate"
    const val TEMPLATE_LOADING = "newLoan:templateLoading"
    const val TEMPLATE_ERROR = "newLoan:templateError"
    const val SCHEDULE_LOADING = "newLoan:scheduleLoading"
    const val SCHEDULE_ERROR = "newLoan:scheduleError"
    const val SCHEDULE_TABLE = "newLoan:scheduleTable"
    const val SUMMARY = "newLoan:summary"
    fun dropdown(field: LoanField): String = "newLoan:dropdown:${field.name}"
    fun number(field: LoanField): String = "newLoan:number:${field.name}"
    fun toggle(toggle: LoanToggle): String = "newLoan:toggle:${toggle.name}"
}

/** Spacer the pages share, so every field sits on the same rhythm. */
@Composable
internal fun FieldGap() = Spacer(Modifier.height(KptTheme.spacing.md))
