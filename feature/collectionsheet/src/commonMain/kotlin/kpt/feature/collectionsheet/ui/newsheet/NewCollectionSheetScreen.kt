/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.collectionsheet.ui.newsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.model.office.Office
import kpt.core.model.staff.Staff
import kpt.feature.collectionsheet.generated.resources.Res
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_cancel
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_clear
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_fill_collection_sheet_message
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_generate
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_generate_new
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_individual_collection_sheet
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_office
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_repayment_date
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_select
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_staff_optional
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Ported from `6b66e8a43:feature/collectionSheet/.../NewIndividualCollectionSheetScreen.kt` (and its
 * `IndividualCollectionSheetScreen` tab host, whose only other tab was a fake — see the ViewModel).
 * Same header, office / repayment date / staff fields, and Generate + Clear buttons.
 */
@Composable
internal fun NewCollectionSheetScreen(
    onNavigateBack: () -> Unit,
    onOpenSheet: (officeId: Int, staffId: Int?, transactionDate: String) -> Unit,
    viewModel: NewCollectionSheetViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offices by viewModel.offices.collectAsStateWithLifecycle()
    val staff by viewModel.staffOptions.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            NewCollectionSheetEvent.NavigateBack -> onNavigateBack()
            is NewCollectionSheetEvent.OpenSheet -> onOpenSheet(event.officeId, event.staffId, event.transactionDate)
        }
    }

    NewCollectionSheetContent(
        state = state,
        offices = offices,
        staff = staff,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        onRetry = viewModel::onRetry,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewCollectionSheetContent(
    state: NewCollectionSheetState,
    offices: ScreenState<List<Office>>,
    staff: List<Staff>,
    onAction: (NewCollectionSheetAction) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    AppScaffold(
        title = stringResource(Res.string.feature_collection_sheet_individual_collection_sheet),
        onBackPressed = { onAction(NewCollectionSheetAction.NavigateBack) },
        modifier = modifier,
    ) { padding ->
        ScreenContent(
            state = offices,
            onRetry = onRetry,
            modifier = Modifier.fillMaxSize().padding(padding).testTag(NewCollectionSheetTestTags.SCREEN),
        ) { officeList, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(KptTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.feature_collection_sheet_generate_new),
                    style = KptTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(Res.string.feature_collection_sheet_fill_collection_sheet_message),
                    style = KptTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(KptTheme.spacing.sm))

                AppTextFieldDropdown(
                    value = state.office?.label.orEmpty(),
                    // The OPTION is dispatched, resolved from the same list that supplied the labels.
                    onOptionSelected = { index, label ->
                        officeList.getOrNull(index)?.let {
                            onAction(NewCollectionSheetAction.SelectOffice(Choice(it.id, label)))
                        }
                    },
                    options = officeList.map { it.name.orEmpty() },
                    label = stringResource(Res.string.feature_collection_sheet_office),
                    modifier = Modifier.fillMaxWidth().testTag(NewCollectionSheetTestTags.OFFICE),
                )
                AppDatePickerTextField(
                    value = DateHelper.getDateAsStringFromLong(state.dateMillis),
                    label = stringResource(Res.string.feature_collection_sheet_repayment_date),
                    openDatePicker = { onAction(NewCollectionSheetAction.OpenDatePicker) },
                    modifier = Modifier.fillMaxWidth().testTag(NewCollectionSheetTestTags.DATE),
                )
                AppTextFieldDropdown(
                    value = state.staff?.label.orEmpty(),
                    onOptionSelected = { index, label ->
                        staff.getOrNull(index)?.id?.let {
                            onAction(NewCollectionSheetAction.SelectStaff(Choice(it, label)))
                        }
                    },
                    options = staff.map { it.displayName.orEmpty() },
                    label = stringResource(Res.string.feature_collection_sheet_staff_optional),
                    // Nothing to pick until an office is chosen — and its staff list arrives.
                    enabled = state.office != null && staff.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().testTag(NewCollectionSheetTestTags.STAFF),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md)) {
                    AppButton(
                        onClick = { onAction(NewCollectionSheetAction.Generate) },
                        enabled = state.canGenerate,
                        modifier = Modifier.weight(1f).testTag(NewCollectionSheetTestTags.GENERATE),
                    ) { Text(stringResource(Res.string.feature_collection_sheet_generate)) }
                    AppButton(
                        onClick = { onAction(NewCollectionSheetAction.Clear) },
                        modifier = Modifier.weight(1f).testTag(NewCollectionSheetTestTags.CLEAR),
                    ) { Text(stringResource(Res.string.feature_collection_sheet_clear)) }
                }
            }
        }

        if (state.datePickerOpen) {
            val picker = rememberDatePickerState(
                initialSelectedDateMillis = state.dateMillis,
                selectableDates = NotInTheFuture,
            )
            DatePickerDialog(
                onDismissRequest = { onAction(NewCollectionSheetAction.CloseDatePicker) },
                confirmButton = {
                    TextButton(onClick = {
                        picker.selectedDateMillis?.let { onAction(NewCollectionSheetAction.DatePicked(it)) }
                            ?: onAction(NewCollectionSheetAction.CloseDatePicker)
                    }) { Text(stringResource(Res.string.feature_collection_sheet_select)) }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(NewCollectionSheetAction.CloseDatePicker) }) {
                        Text(stringResource(Res.string.feature_collection_sheet_cancel))
                    }
                },
            ) { DatePicker(state = picker) }
        }
    }
}

/** A repayment records collected cash, so it cannot be dated in the future (the original allowed ONLY the future). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
private object NotInTheFuture : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

/** Stable handles for UI tests and device flows. */
internal object NewCollectionSheetTestTags {
    const val SCREEN = "newCollectionSheet:screen"
    const val OFFICE = "newCollectionSheet:office"
    const val DATE = "newCollectionSheet:date"
    const val STAFF = "newCollectionSheet:staff"
    const val GENERATE = "newCollectionSheet:generate"
    const val CLEAR = "newCollectionSheet:clear"
}
