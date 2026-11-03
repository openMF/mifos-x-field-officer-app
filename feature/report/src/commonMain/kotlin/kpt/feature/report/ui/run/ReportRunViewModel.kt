/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.report.ui.run

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.dataOrNull
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.report.ReportRepository
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.report.ReportOption
import kpt.core.model.report.ReportParameter
import kpt.core.model.report.ReportParameterType
import kpt.feature.report.ui.toCsv
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Ported from `6b66e8a43:feature/report/reportDetail/ReportDetailViewModel.kt` + `ReportDetailScreen.kt`
 * (+ `report/ReportScreen.kt`, the result).
 *
 * The original's form knew TEN parameter names, hardcoded, and nothing else — no dates — so every report
 * taking a start/end date (most transaction reports) could not be run. Here the form is the report's
 * own `FullParameterList`: every parameter it declares, by type ([ReportParameterType]).
 *
 * Options load per SELECT parameter and narrow by their parent's value (loan officers by office), as
 * the original did for its two hardcoded parents; picking a parent clears its children's values.
 * Defaults are derived, never written into state: "All" (-1) where the report offers it, today for a
 * date, the server's default for a hidden parameter.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class ReportRunViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ReportRepository,
) : BaseViewModel<ReportRunState, ReportRunEvent, ReportRunAction>(
    ReportRunState(reportName = requireNotNull(savedStateHandle.get<String>("reportName"))),
) {

    private val parametersStream = repository.reportParametersStream(state.reportName, viewModelScope)

    val parameters: StateFlow<ScreenState<List<ReportParameter>>> = parametersStream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** Fetched option lists, by (parameter, parent values) — a pick must not re-fetch every list. */
    private val optionCache = mutableMapOf<Pair<String, Map<String, String>>, List<ReportOption>?>()

    /**
     * Each SELECT parameter's options. Recomputed when the parameters or a VALUE changes; only a list
     * whose parent value changed is fetched again.
     */
    val options: StateFlow<Map<String, ParameterOptions>> =
        combine(
            parametersStream.state.map { it.dataOrNull.orEmpty() },
            stateFlow.map { it.values }.distinctUntilChanged(),
        ) { params, values ->
            params.filter { it.displayType == ReportParameterType.SELECT }
                .associate { it.name to parentQuery(it, params, values) }
        }
            .distinctUntilChanged()
            .mapLatest { requests ->
                requests.mapValues { (name, parents) ->
                    when (parents) {
                        null -> ParameterOptions.WaitingForParent
                        else -> optionCache.getOrPut(name to parents) { repository.parameterOptions(name, parents) }
                            ?.let { ParameterOptions.Loaded(it) } ?: ParameterOptions.Failed
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), emptyMap())

    fun onRetry() = parametersStream.retry()

    override fun handleAction(action: ReportRunAction) {
        when (action) {
            is ReportRunAction.ValueChanged -> {
                val params = parameters.value.dataOrNull.orEmpty()
                // A child's value belonged to the PREVIOUS parent (an officer of the old office).
                val children = params.filter { it.parentName == action.parameter }.map { it.name }.toSet()
                mutableStateFlow.value = state.copy(
                    values = state.values - children + (action.parameter to action.value),
                    missing = state.missing - action.parameter,
                )
            }
            is ReportRunAction.OpenDatePicker -> mutableStateFlow.value = state.copy(datePickerFor = action.parameter)
            ReportRunAction.CloseDatePicker -> mutableStateFlow.value = state.copy(datePickerFor = null)
            ReportRunAction.Run -> run()
            ReportRunAction.BackToParameters -> mutableStateFlow.value = state.copy(result = null, failure = null)
            ReportRunAction.DismissFailure -> mutableStateFlow.value = state.copy(failure = null)
            ReportRunAction.Export -> state.result?.let {
                sendEvent(ReportRunEvent.ExportCsv(fileName = state.reportName.toFileName(), csv = it.toCsv()))
            }
            is ReportRunAction.ExportFinished ->
                sendEvent(ReportRunEvent.ShowMessage(if (action.saved) ExportOutcome.SAVED else ExportOutcome.FAILED))
            ReportRunAction.NavigateBack ->
                // Back from a result returns to the form, as the original's result screen did.
                if (state.result != null) {
                    handleAction(ReportRunAction.BackToParameters)
                } else {
                    sendEvent(ReportRunEvent.NavigateBack)
                }
        }
    }

    private fun run() {
        if (state.isRunning) return
        val params = parameters.value.dataOrNull ?: return
        val values = params.associate { it.name to effectiveValue(it, state.values) }
        val missing = params
            .filter { it.displayType != ReportParameterType.HIDDEN && values[it.name].isNullOrBlank() && it.isRequired }
            .map { it.name }.toSet()
        if (missing.isNotEmpty()) {
            mutableStateFlow.value = state.copy(missing = missing)
        } else {
            execute(params, values)
        }
    }

    private fun execute(params: List<ReportParameter>, values: Map<String, String?>) {
        val query = params.mapNotNull { p -> values[p.name]?.let { p.queryKey to it } }.toMap()
        mutableStateFlow.value = state.copy(isRunning = true, failure = null)
        viewModelScope.launch {
            val result = repository.runReport(state.reportName, query)
            mutableStateFlow.value = state.copy(
                isRunning = false,
                result = result.getOrNull(),
                failure = result.exceptionOrNull()?.let { ReportFailure(it.serverErrorMessage()) },
            )
        }
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/** A parameter's value as it will be sent: the officer's, else its derived default. */
@OptIn(ExperimentalTime::class)
internal fun effectiveValue(parameter: ReportParameter, values: Map<String, String>): String? =
    values[parameter.name]?.takeIf { it.isNotBlank() } ?: when (parameter.displayType) {
        ReportParameterType.SELECT -> ALL.takeIf { parameter.allowAll }
        // Fineract substitutes a date into SQL as-is, so it goes as ISO `yyyy-MM-dd`.
        ReportParameterType.DATE -> Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        ReportParameterType.TEXT -> parameter.defaultValue
        ReportParameterType.HIDDEN -> parameter.defaultValue
    }

/** Free text may be blank; every other visible input must have a value. */
private val ReportParameter.isRequired get() = displayType != ReportParameterType.TEXT

/** Fineract's "All" for a `selectAll` parameter. */
internal const val ALL = "-1"

/**
 * The parent values [parameter]'s options are read with, or null while its parent has none yet (there
 * is nothing to narrow by — e.g. no office chosen, so no officer list).
 */
internal fun parentQuery(
    parameter: ReportParameter,
    params: List<ReportParameter>,
    values: Map<String, String>,
): Map<String, String>? {
    val parent = parameter.parentName?.let { name -> params.firstOrNull { it.name == name } } ?: return emptyMap()
    return effectiveValue(parent, values)?.let { mapOf(parent.queryKey to it) }
}

private fun String.toFileName() = lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifEmpty { "report" }

internal sealed interface ParameterOptions {
    data object WaitingForParent : ParameterOptions
    data object Failed : ParameterOptions
    data class Loaded(val options: List<ReportOption>) : ParameterOptions
}

internal data class ReportFailure(val serverMessage: String?)

internal enum class ExportOutcome { SAVED, FAILED }

internal data class ReportRunState(
    val reportName: String,
    /** The officer's entries, by parameter NAME: an option id, an ISO date, or text. */
    val values: Map<String, String> = emptyMap(),
    /** Parameters Run refused for want of a value. */
    val missing: Set<String> = emptySet(),
    val datePickerFor: String? = null,
    val isRunning: Boolean = false,
    val result: FullParameterListResponse? = null,
    val failure: ReportFailure? = null,
)

internal sealed interface ReportRunEvent {
    data object NavigateBack : ReportRunEvent
    data class ExportCsv(val fileName: String, val csv: String) : ReportRunEvent
    data class ShowMessage(val outcome: ExportOutcome) : ReportRunEvent
}

internal sealed interface ReportRunAction {
    data class ValueChanged(val parameter: String, val value: String) : ReportRunAction
    data class OpenDatePicker(val parameter: String) : ReportRunAction
    data object CloseDatePicker : ReportRunAction
    data object Run : ReportRunAction
    data object BackToParameters : ReportRunAction
    data object DismissFailure : ReportRunAction
    data object Export : ReportRunAction
    data class ExportFinished(val saved: Boolean) : ReportRunAction
    data object NavigateBack : ReportRunAction
}
