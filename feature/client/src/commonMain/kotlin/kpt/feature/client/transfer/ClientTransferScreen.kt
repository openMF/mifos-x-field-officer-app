/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)

package kpt.feature.client.transfer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextButton
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.office.Office
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import kpt.feature.client.generated.resources.feature_client_charge_cancel
import kpt.feature.client.generated.resources.feature_client_charge_select
import kpt.feature.client.generated.resources.feature_client_dialog_continue
import kpt.feature.client.generated.resources.feature_client_transfer_add_note_here
import kpt.feature.client.generated.resources.feature_client_transfer_add_notes
import kpt.feature.client.generated.resources.feature_client_transfer_choose_office
import kpt.feature.client.generated.resources.feature_client_transfer_expected_date
import kpt.feature.client.generated.resources.feature_client_transfer_failure_title
import kpt.feature.client.generated.resources.feature_client_transfer_no_offices
import kpt.feature.client.generated.resources.feature_client_transfer_success_message
import kpt.feature.client.generated.resources.feature_client_transfer_success_title
import kpt.feature.client.generated.resources.feature_client_transfer_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * Carried over from `6b66e8a43:feature/client/clientTransfer/ClientTransferScreen.kt` under §6a —
 * the office dropdown, the date field, the note field, the button row and the copy are the
 * original's, in the original's order, including `verticalScroll`.
 *
 * Adaptations are §6a's three only: read states through [ScreenContent]; `OnSubmit` carries the
 * chosen office id; `AppBreadcrumbNavBar` dropped for its NavController coupling.
 */
@Composable
internal fun ClientTransferScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientTransferViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offices by viewModel.offices.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientTransferEvent.NavigateBack -> onNavigateBack()
            ClientTransferEvent.NavigateNext -> onNavigateNext()
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
        title = stringResource(Res.string.feature_client_transfer_title),
        onBackPressed = { viewModel.trySendAction(ClientTransferAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientTransferContent(
            state = state,
            offices = offices,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier.padding(scaffoldPadding),
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientTransferContent(
    state: ClientTransferState,
    offices: ScreenState<List<Office>>,
    onAction: (ClientTransferAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(
        state = offices,
        onRetry = { onAction(ClientTransferAction.OnRetry) },
        modifier = modifier.fillMaxSize().testTag(ClientTransferTestTags.SCREEN),
    ) { officeList, _ ->
        if (officeList.isEmpty()) {
            Text(
                text = stringResource(Res.string.feature_client_transfer_no_offices),
                modifier = Modifier.testTag(ClientTransferTestTags.NO_OFFICES),
            )
            return@ScreenContent
        }

        val selected = officeList[state.currentSelectedIndex.coerceIn(officeList.indices)]
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date,
            selectableDates = remember {
                object : SelectableDates {
                    // The original's rule, kept verbatim including the day-start normalisation: a
                    // transfer is dated TODAY OR LATER (the inverse of closure, which is past-only).
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val now = Clock.System.now().toEpochMilliseconds()
                        val currentDayStart = now - (now % DAY_MS)
                        val selectedDayStart = utcTimeMillis - (utcTimeMillis % DAY_MS)
                        return selectedDayStart >= currentDayStart
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_client_transfer_title),
                style = AppTypography.labelLargeEmphasized,
            )
            Spacer(Modifier.height(DesignToken.padding.largeIncreased))

            AppTextFieldDropdown(
                value = selected.name ?: "",
                onOptionSelected = { index, _ -> onAction(ClientTransferAction.OptionChanged(index)) },
                options = officeList.map { it.name ?: "" },
                label = stringResource(Res.string.feature_client_transfer_choose_office),
                modifier = Modifier.fillMaxWidth().testTag(ClientTransferTestTags.OFFICE),
            )

            Spacer(Modifier.height(DesignToken.padding.largeIncreased))

            AppDatePickerTextField(
                value = DateHelper.getDateAsStringFromLong(state.date),
                modifier = Modifier.fillMaxWidth().testTag(ClientTransferTestTags.DATE),
                label = stringResource(Res.string.feature_client_transfer_expected_date),
                openDatePicker = { onAction(ClientTransferAction.UpdateDatePicker(true)) },
            )
            if (state.showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { onAction(ClientTransferAction.UpdateDatePicker(false)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onAction(ClientTransferAction.UpdateDatePicker(false))
                                datePickerState.selectedDateMillis?.let {
                                    onAction(ClientTransferAction.UpdateDate(it))
                                }
                            },
                        ) { Text(stringResource(Res.string.feature_client_charge_select)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { onAction(ClientTransferAction.UpdateDatePicker(false)) },
                        ) { Text(stringResource(Res.string.feature_client_charge_cancel)) }
                    },
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(Modifier.height(DesignToken.padding.largeIncreased))
            AppOutlinedTextField(
                value = state.note,
                onValueChange = { onAction(ClientTransferAction.NoteChanged(it)) },
                label = stringResource(Res.string.feature_client_transfer_add_notes),
                modifier = Modifier.fillMaxWidth().testTag(ClientTransferTestTags.NOTE),
                singleLine = true,
                placeholder = stringResource(Res.string.feature_client_transfer_add_note_here),
            )

            Spacer(Modifier.height(DesignToken.padding.largeIncreased))

            Row(modifier = Modifier.fillMaxWidth()) {
                AppOutlinedButton(
                    onClick = { onAction(ClientTransferAction.NavigateBack) },
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
                    modifier = Modifier.weight(1f).testTag(ClientTransferTestTags.BACK),
                )
                Spacer(Modifier.padding(KptTheme.spacing.sm))
                AppTextButton(
                    onClick = { onAction(ClientTransferAction.OnSubmit(selected.id)) },
                    // The original's gate: a transfer needs a note. `isSubmitting` is folded in
                    // so the button also cannot be tapped twice.
                    enabled = state.isEnabled,
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
                    modifier = Modifier.weight(1f).testTag(ClientTransferTestTags.SUBMIT),
                )
            }
        }
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        is ClientTransferState.DialogState.ShowStatusDialog -> AppStatusDialog(
            status = dialog.status,
            btnText = stringResource(Res.string.feature_client_dialog_continue),
            onConfirm = {
                onAction(
                    if (dialog.status == ResultStatus.SUCCESS) {
                        ClientTransferAction.OnNext
                    } else {
                        ClientTransferAction.CloseDialog
                    },
                )
            },
            onDismissRequest = { onAction(ClientTransferAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_client_transfer_success_title),
            successMessage = stringResource(Res.string.feature_client_transfer_success_message),
            failureTitle = stringResource(Res.string.feature_client_transfer_failure_title),
            failureMessage = dialog.msg,
            modifier = Modifier.fillMaxSize().testTag(ClientTransferTestTags.STATUS_DIALOG),
        )
    }
}

/** 24h in millis — the original's literal `86400000`, named. */
private const val DAY_MS = 86_400_000L
