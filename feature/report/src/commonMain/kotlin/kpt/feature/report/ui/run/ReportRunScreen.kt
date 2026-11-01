/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.report.ui.run

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.report.ReportOption
import kpt.core.model.report.ReportParameter
import kpt.core.model.report.ReportParameterType
import kpt.core.ui.components.AppEmptyCard
import kpt.feature.report.generated.resources.Res
import kpt.feature.report.generated.resources.feature_report_all_option
import kpt.feature.report.generated.resources.feature_report_back_to_parameters
import kpt.feature.report.generated.resources.feature_report_cancel
import kpt.feature.report.generated.resources.feature_report_export_csv
import kpt.feature.report.generated.resources.feature_report_exported_successfully
import kpt.feature.report.generated.resources.feature_report_failed
import kpt.feature.report.generated.resources.feature_report_no_parameters
import kpt.feature.report.generated.resources.feature_report_no_rows
import kpt.feature.report.generated.resources.feature_report_ok
import kpt.feature.report.generated.resources.feature_report_required
import kpt.feature.report.generated.resources.feature_report_rows
import kpt.feature.report.generated.resources.feature_report_run_report
import kpt.feature.report.generated.resources.feature_report_select
import kpt.feature.report.generated.resources.feature_report_unable_to_export
import kpt.feature.report.ui.saveReportCsv
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

/** See [ReportRunViewModel]. */
@Composable
internal fun ReportRunScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportRunViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val parameters by viewModel.parameters.collectAsStateWithLifecycle()
    val options by viewModel.options.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ReportRunEvent.NavigateBack -> onNavigateBack()
            // A save dialog on Android / desktop / iOS (no storage permission), a browser download on the
            // web — see saveReportCsv. The original wrote to a hardcoded Android path and was TODO() elsewhere.
            is ReportRunEvent.ExportCsv -> {
                val saved = runCatching { saveReportCsv(event.fileName, event.csv.encodeToByteArray()) }
                    .getOrDefault(false)
                viewModel.trySendAction(ReportRunAction.ExportFinished(saved))
            }
            is ReportRunEvent.ShowMessage -> snackbar.showSnackbar(
                getString(
                    if (event.outcome == ExportOutcome.SAVED) {
                        Res.string.feature_report_exported_successfully
                    } else {
                        Res.string.feature_report_unable_to_export
                    },
                ),
            )
        }
    }

    ReportRunContent(
        state = state,
        parameters = parameters,
        options = options,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        snackbarHostState = snackbar,
        onRetry = viewModel::onRetry,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ReportRunContent(
    state: ReportRunState,
    parameters: ScreenState<List<ReportParameter>>,
    options: Map<String, ParameterOptions>,
    onAction: (ReportRunAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRetry: () -> Unit = {},
) {
    val result = state.result
    AppScaffold(
        title = state.reportName,
        onBackPressed = { onAction(ReportRunAction.NavigateBack) },
        snackbarHostState = snackbarHostState,
        actions = {
            if (result != null) {
                TextButton(
                    onClick = { onAction(ReportRunAction.Export) },
                    modifier = Modifier.testTag(ReportRunTestTags.EXPORT),
                ) {
                    Text(stringResource(Res.string.feature_report_export_csv))
                }
            }
        },
        modifier = modifier,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (result != null) {
                ResultTable(result, onBack = { onAction(ReportRunAction.BackToParameters) })
            } else {
                ScreenContent(state = parameters, onRetry = onRetry, modifier = Modifier.fillMaxSize()) { params, _ ->
                    ParameterForm(state, params, options, onAction)
                }
            }
        }
    }
    state.datePickerFor?.let { name -> DateDialog(name, state.values[name], onAction) }
    state.failure?.let { failure ->
        AlertDialog(
            onDismissRequest = { onAction(ReportRunAction.DismissFailure) },
            confirmButton = {
                TextButton(onClick = { onAction(ReportRunAction.DismissFailure) }) {
                    Text(stringResource(Res.string.feature_report_ok))
                }
            },
            text = {
                val generic = stringResource(Res.string.feature_report_failed)
                Text(failure.serverMessage?.let { "$generic\n$it" } ?: generic)
            },
            modifier = Modifier.testTag(ReportRunTestTags.FAILURE),
        )
    }
}

@Composable
private fun ParameterForm(
    state: ReportRunState,
    params: List<ReportParameter>,
    options: Map<String, ParameterOptions>,
    onAction: (ReportRunAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(KptTheme.spacing.md)
            .testTag(ReportRunTestTags.FORM),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        val visible = params.filter { it.displayType != ReportParameterType.HIDDEN }
        if (visible.isEmpty()) Text(stringResource(Res.string.feature_report_no_parameters))
        for (parameter in visible) {
            val error = if (parameter.name in state.missing) {
                stringResource(Res.string.feature_report_required)
            } else {
                null
            }
            val value = effectiveValue(parameter, state.values)
            val tag = Modifier.fillMaxWidth().testTag(ReportRunTestTags.parameter(parameter.name))
            when (parameter.displayType) {
                ReportParameterType.SELECT -> {
                    val loaded = (options[parameter.name] as? ParameterOptions.Loaded)?.options.orEmpty()
                    // "All" first where the report offers it — sent as -1, Fineract's convention.
                    val all = stringResource(Res.string.feature_report_all_option)
                    val choices = if (parameter.allowAll) listOf(ReportOption(ALL, all)) + loaded else loaded
                    AppTextFieldDropdown(
                        value = choices.firstOrNull { it.id == value }?.label.orEmpty(),
                        onOptionSelected = { index, _ ->
                            choices.getOrNull(index)?.let {
                                onAction(ReportRunAction.ValueChanged(parameter.name, it.id))
                            }
                        },
                        options = choices.map { it.label },
                        label = parameter.label,
                        errorMessage = error,
                        enabled = choices.isNotEmpty(),
                        modifier = tag,
                    )
                }
                ReportParameterType.DATE -> AppDatePickerTextField(
                    value = value.orEmpty(),
                    label = parameter.label,
                    errorMessage = error,
                    openDatePicker = { onAction(ReportRunAction.OpenDatePicker(parameter.name)) },
                    modifier = tag,
                )
                else -> AppOutlinedTextField(
                    value = state.values[parameter.name] ?: parameter.defaultValue.orEmpty(),
                    onValueChange = { onAction(ReportRunAction.ValueChanged(parameter.name, it)) },
                    label = parameter.label,
                    isError = error != null,
                    errorText = error,
                    modifier = tag,
                )
            }
        }
        AppButton(
            onClick = { onAction(ReportRunAction.Run) },
            enabled = !state.isRunning,
            modifier = Modifier.fillMaxWidth().testTag(ReportRunTestTags.RUN),
        ) {
            if (state.isRunning) {
                CircularProgressIndicator(strokeWidth = KptTheme.spacing.xs / 2)
            } else {
                Text(stringResource(Res.string.feature_report_run_report))
            }
        }
    }
}

/**
 * The result as a real table: ROWS of cells, one per header, so every value stays in its row. The
 * original drew each COLUMN as its own list and skipped null cells, so one empty value shifted every
 * later value in that column up a row — misattributing amounts to the wrong client.
 */
@Composable
private fun ResultTable(
    result: FullParameterListResponse,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.feature_report_rows, result.data.size),
                style = KptTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onBack) { Text(stringResource(Res.string.feature_report_back_to_parameters)) }
        }
        if (result.data.isEmpty()) {
            AppEmptyCard(
                msg = stringResource(Res.string.feature_report_no_rows),
                modifier = Modifier.padding(KptTheme.spacing.md),
            )
        } else {
            ResultRows(result)
        }
    }
}

@Composable
private fun ResultRows(result: FullParameterListResponse) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize().horizontalScroll(scroll).testTag(ReportRunTestTags.TABLE)) {
        Row(Modifier.background(KptTheme.colorScheme.surfaceContainerHigh)) {
            result.columnHeaders.forEach { Cell(it.columnName, bold = true) }
        }
        LazyColumn {
            itemsIndexed(result.data) { index, dataRow ->
                val rowColor = if (index % 2 == 0) {
                    KptTheme.colorScheme.surface
                } else {
                    KptTheme.colorScheme.surfaceContainerLow
                }
                Row(Modifier.background(rowColor)) {
                    result.columnHeaders.indices.forEach { Cell(dataRow.row.getOrNull(it).orEmpty()) }
                }
                HorizontalDivider(color = KptTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun Cell(text: String, bold: Boolean = false) {
    Text(
        text = text,
        style = KptTheme.typography.bodySmall,
        fontWeight = if (bold) FontWeight.SemiBold else null,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(CELL_WIDTH).padding(KptTheme.spacing.sm),
    )
}

@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
private fun DateDialog(parameter: String, value: String?, onAction: (ReportRunAction) -> Unit) {
    // The picker speaks UTC-midnight millis; the value is an ISO date. Convert both ways in UTC so a
    // device west of Greenwich does not pick the day before.
    val initial = value?.let {
        runCatching { LocalDate.parse(it).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() }.getOrNull()
    }
    val picker = rememberDatePickerState(initialSelectedDateMillis = initial)
    DatePickerDialog(
        onDismissRequest = { onAction(ReportRunAction.CloseDatePicker) },
        confirmButton = {
            TextButton(onClick = {
                picker.selectedDateMillis?.let { millis ->
                    val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                    onAction(ReportRunAction.ValueChanged(parameter, date.toString()))
                }
                onAction(ReportRunAction.CloseDatePicker)
            }) { Text(stringResource(Res.string.feature_report_select)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(ReportRunAction.CloseDatePicker) }) {
                Text(stringResource(Res.string.feature_report_cancel))
            }
        },
    ) { DatePicker(state = picker) }
}

private val CELL_WIDTH = 140.dp

internal object ReportRunTestTags {
    const val FORM = "reportRun:form"
    const val RUN = "reportRun:run"
    const val TABLE = "reportRun:table"
    const val EXPORT = "reportRun:export"
    const val FAILURE = "reportRun:failure"

    fun parameter(name: String) = "reportRun:parameter:$name"
}
