/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)

package kpt.feature.client.closure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
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
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextButton
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.client.Narration
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import kpt.feature.client.generated.resources.feature_client_charge_cancel
import kpt.feature.client.generated.resources.feature_client_charge_select
import kpt.feature.client.generated.resources.feature_client_closure_expected_date
import kpt.feature.client.generated.resources.feature_client_closure_failure_title
import kpt.feature.client.generated.resources.feature_client_closure_no_reasons
import kpt.feature.client.generated.resources.feature_client_closure_reason
import kpt.feature.client.generated.resources.feature_client_closure_success_message
import kpt.feature.client.generated.resources.feature_client_closure_success_title
import kpt.feature.client.generated.resources.feature_client_closure_title
import kpt.feature.client.generated.resources.feature_client_dialog_continue
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * Carried over from `6b66e8a43:feature/client/clientClosure/ClientClosureScreen.kt` under §6a —
 * layout, the date-picker affordance, the dropdown, the button row and the copy are the original's.
 *
 * The three permitted adaptations are the same as `clientStaff`'s: the read's Loading/Error dialogs
 * become [ScreenState] through [ScreenContent]; actions dispatch unchanged except that `OnSubmit`
 * carries the chosen reason id; and `AppBreadcrumbNavBar` is dropped for the same
 * NavController-coupling reason every other ported screen drops it.
 *
 * The DISPLAYED date keeps `DateHelper.getDateAsStringFromLong` — the original's format. Only the
 * date put on the WIRE changed, and that is in the ViewModel; see its header.
 */
@Composable
internal fun ClientClosureScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientClosureViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val reasons by viewModel.reasons.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientClosureEvent.NavigateBack -> onNavigateBack()
            ClientClosureEvent.NavigateNext -> onNavigateNext()
        }
    }

    // Wrapped in AppScaffold 2026-10-23. This screen had NO app bar at all — no title and no
    // back arrow — and every one of its empty branches returns before the Back/Submit row, so an
    // officer who reached it on a tenant with nothing configured had no way out but the system
    // back gesture. Found on device: the client Closure screen rendered as the single line
    // "No closure reasons found" with zero clickable nodes in the whole hierarchy.
    //
    // Not a §6a deviation. The originals were Fragments and took their app bar from the hosting
    // Activity's toolbar; AppScaffold IS that toolbar in the ported world, which is why every
    // other ported screen has one. These five simply missed it.
    AppScaffold(
        title = stringResource(Res.string.feature_client_closure_title),
        onBackPressed = { viewModel.trySendAction(ClientClosureAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientClosureContent(
            state = state,
            reasons = reasons,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier.padding(scaffoldPadding),
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientClosureContent(
    state: ClientClosureState,
    reasons: ScreenState<List<Narration>>,
    onAction: (ClientClosureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(
        state = reasons,
        onRetry = { onAction(ClientClosureAction.OnRetry) },
        modifier = modifier.fillMaxSize().testTag(ClientClosureTestTags.SCREEN),
    ) { narrations, _ ->
        if (narrations.isEmpty()) {
            // A tenant with no configured closure reasons. The original drew this too, and the
            // submit button is what `isEnabled` gated — there is nothing to submit.
            Text(
                text = stringResource(Res.string.feature_client_closure_no_reasons),
                modifier = Modifier.testTag(ClientClosureTestTags.NO_REASONS),
            )
            return@ScreenContent
        }

        val selected = narrations[state.currentSelectedIndex.coerceIn(narrations.indices)]
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date,
            selectableDates = remember {
                object : SelectableDates {
                    // The original's rule, kept: a closure cannot be dated in the future.
                    override fun isSelectableDate(utcTimeMillis: Long) =
                        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
                }
            },
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_client_closure_title),
                style = AppTypography.labelLargeEmphasized,
            )
            Spacer(Modifier.height(DesignToken.padding.largeIncreased))

            AppDatePickerTextField(
                value = DateHelper.getDateAsStringFromLong(state.date),
                modifier = Modifier.fillMaxWidth().testTag(ClientClosureTestTags.DATE),
                label = stringResource(Res.string.feature_client_closure_expected_date),
                openDatePicker = { onAction(ClientClosureAction.UpdateDatePicker(true)) },
            )

            if (state.showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { onAction(ClientClosureAction.UpdateDatePicker(false)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onAction(ClientClosureAction.UpdateDatePicker(false))
                                datePickerState.selectedDateMillis?.let {
                                    onAction(ClientClosureAction.UpdateDate(it))
                                }
                            },
                        ) { Text(stringResource(Res.string.feature_client_charge_select)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { onAction(ClientClosureAction.UpdateDatePicker(false)) },
                        ) { Text(stringResource(Res.string.feature_client_charge_cancel)) }
                    },
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(Modifier.height(DesignToken.padding.largeIncreased))

            AppTextFieldDropdown(
                value = selected.name,
                onOptionSelected = { index, _ -> onAction(ClientClosureAction.OptionChanged(index)) },
                options = narrations.map { it.name },
                label = stringResource(Res.string.feature_client_closure_reason),
                modifier = Modifier.fillMaxWidth().testTag(ClientClosureTestTags.REASON),
                readOnly = true,
            )

            Spacer(Modifier.height(DesignToken.padding.largeIncreased))

            Row(modifier = Modifier.fillMaxWidth()) {
                AppOutlinedButton(
                    onClick = { onAction(ClientClosureAction.NavigateBack) },
                    leadingIcon = {
                        Icon(
                            imageVector = AppIcons.ChevronLeft,
                            contentDescription = null,
                            modifier = Modifier.size(DesignToken.sizes.iconAverage),
                            tint = KptTheme.colorScheme.primary,
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(Res.string.feature_client_btn_back),
                            color = KptTheme.colorScheme.primary,
                            style = AppTypography.labelLarge,
                        )
                    },
                    modifier = Modifier.weight(1f).testTag(ClientClosureTestTags.BACK),
                )
                Spacer(Modifier.padding(KptTheme.spacing.sm))
                AppTextButton(
                    onClick = { onAction(ClientClosureAction.OnSubmit(selected.id)) },
                    enabled = !state.isSubmitting,
                    leadingIcon = {
                        Icon(
                            imageVector = AppIcons.Check,
                            contentDescription = null,
                            modifier = Modifier.size(DesignToken.sizes.iconAverage),
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(Res.string.feature_client_btn_submit),
                            style = AppTypography.labelLarge,
                        )
                    },
                    modifier = Modifier.weight(1f).testTag(ClientClosureTestTags.SUBMIT),
                )
            }
        }
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        is ClientClosureState.DialogState.ShowStatusDialog -> AppStatusDialog(
            status = dialog.status,
            btnText = stringResource(Res.string.feature_client_dialog_continue),
            // Per-status, like clientStaff: forward on success (the client is closed, this screen
            // has no subject left), dismiss on failure. The original navigated on BOTH.
            onConfirm = {
                onAction(
                    if (dialog.status == ResultStatus.SUCCESS) {
                        ClientClosureAction.OnNext
                    } else {
                        ClientClosureAction.CloseDialog
                    },
                )
            },
            onDismissRequest = { onAction(ClientClosureAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_client_closure_success_title),
            successMessage = stringResource(Res.string.feature_client_closure_success_message),
            failureTitle = stringResource(Res.string.feature_client_closure_failure_title),
            failureMessage = dialog.msg,
            modifier = Modifier.fillMaxSize().testTag(ClientClosureTestTags.STATUS_DIALOG),
        )
    }
}
