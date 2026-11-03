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

package kpt.feature.activate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.AppAlertDialog
import kpt.feature.activate.generated.resources.Res
import kpt.feature.activate.generated.resources.feature_activate
import kpt.feature.activate.generated.resources.feature_activate_activation_date
import kpt.feature.activate.generated.resources.feature_activate_cancel
import kpt.feature.activate.generated.resources.feature_activate_center
import kpt.feature.activate.generated.resources.feature_activate_client
import kpt.feature.activate.generated.resources.feature_activate_failed_to_activate_center
import kpt.feature.activate.generated.resources.feature_activate_failed_to_activate_client
import kpt.feature.activate.generated.resources.feature_activate_failed_to_activate_group
import kpt.feature.activate.generated.resources.feature_activate_group
import kpt.feature.activate.generated.resources.feature_activate_select
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * Carried over from `6b66e8a43:feature/activate/ActivateScreen.kt` — the date field, its
 * `DatePickerDialog`, and the full-width submit button, in the original's order and with its
 * tokens. Serves client, centre and group.
 *
 * §6a's three adaptations: the `Loading` state no longer replaces the form (see the ViewModel's
 * header — it used to discard the picked date); the success/failure dialog is driven by one
 * `result` field instead of three UI states; and the activation payload is built here, from the
 * picked date, exactly as the original built it.
 *
 * `AppScaffold` keeps the original's title + back affordance, so this screen has its own top bar
 * where the client drill-downs do not — that is the original's shape and it is kept.
 */
@Composable
internal fun ActivateScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivateViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ActivateEvent.NavigateBack -> onBackPressed()
        }
    }

    ActivateContent(
        state = state,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        onBackPressed = onBackPressed,
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ActivateContent(
    state: ActivateState,
    onAction: (ActivateAction) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * The date the picker starts on — today in the app. A parameter so a preview can pin it: read
     * from the clock here, the screen's golden would change every day.
     */
    initialDateMillis: Long = Clock.System.now().toEpochMilliseconds(),
) {
    AppScaffold(
        title = stringResource(Res.string.feature_activate),
        onBackPressed = onBackPressed,
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .testTag(ActivateTestTags.SCREEN),
        ) {
            var showDatePicker by rememberSaveable { mutableStateOf(false) }
            var activateDate by rememberSaveable { mutableLongStateOf(initialDateMillis) }
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = activateDate,
                selectableDates = remember {
                    object : SelectableDates {
                        // The original's rule, kept: an activation is dated TODAY OR LATER. (The
                        // inverse of closure, which is past-only.)
                        override fun isSelectableDate(utcTimeMillis: Long) =
                            utcTimeMillis >= Clock.System.now().toEpochMilliseconds()
                    }
                },
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                                datePickerState.selectedDateMillis?.let { activateDate = it }
                            },
                        ) { Text(stringResource(Res.string.feature_activate_select)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDatePicker = false },
                        ) { Text(stringResource(Res.string.feature_activate_cancel)) }
                    },
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            AppDatePickerTextField(
                value = DateHelper.getDateAsStringFromLong(activateDate),
                label = stringResource(Res.string.feature_activate_activation_date),
                openDatePicker = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().testTag(ActivateTestTags.DATE),
            )

            Spacer(modifier = Modifier.height(KptTheme.spacing.md))

            AppButton(
                onClick = {
                    onAction(
                        ActivateAction.Submit(
                            // The original's payload, field for field — and it pairs the date with
                            // the formatter that owns the format constant, which closure and
                            // transfer did NOT (see their ViewModels).
                            ActivatePayload(
                                activationDate = ApiDateFormatter.formatForApi(activateDate),
                                dateFormat = ApiDateFormatter.DATE_FORMAT,
                                locale = ApiDateFormatter.LOCALE,
                            ),
                        ),
                    )
                },
                enabled = !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(DesignToken.spacing.dp44)
                    .padding(horizontal = KptTheme.spacing.md)
                    .testTag(ActivateTestTags.SUBMIT),
                contentPadding = PaddingValues(),
            ) {
                Text(
                    text = stringResource(Res.string.feature_activate),
                    style = KptTheme.typography.bodySmall,
                )
            }
        }
    }

    Result(state = state, onAction = onAction)
}

@Composable
private fun Result(
    state: ActivateState,
    onAction: (ActivateAction) -> Unit,
) {
    val result = state.result ?: return

    val text = when (result) {
        ActivateResult.Succeeded -> stringResource(
            when (state.type) {
                ActivateType.Client -> Res.string.feature_activate_client
                ActivateType.Center -> Res.string.feature_activate_center
                ActivateType.Group -> Res.string.feature_activate_group
            },
        )

        ActivateResult.Failed -> listOfNotNull(
            stringResource(
                when (state.type) {
                    ActivateType.Client -> Res.string.feature_activate_failed_to_activate_client
                    ActivateType.Center -> Res.string.feature_activate_failed_to_activate_center
                    ActivateType.Group -> Res.string.feature_activate_failed_to_activate_group
                },
            ),
            state.failureReason,
        ).joinToString("\n")

        // No string for this in the original — it had no offline case, because its hand-rolled
        // error path could not tell "rejected" from "never sent". The gateway can.
        ActivateResult.Offline -> OFFLINE_MESSAGE
    }

    AppAlertDialog(
        dialogText = text,
        // Success leaves the screen — the entity is active, so there is nothing left to do here,
        // and the caller re-reads it. A failure only dismisses.
        onConfirmation = {
            onAction(
                if (result == ActivateResult.Succeeded) {
                    ActivateAction.NavigateBack
                } else {
                    ActivateAction.DismissDialog
                },
            )
        },
        onDismissRequest = { onAction(ActivateAction.DismissDialog) },
        dismissText = null,
        modifier = Modifier.testTag(ActivateTestTags.RESULT_DIALOG),
    )
}

private const val OFFLINE_MESSAGE = "No connection — the activation was not sent."
