/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.collectionsheet.ui.newsheet

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.dataOrNull
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.office.OfficeRepository
import kpt.core.data.staff.StaffRepository
import kpt.core.model.office.Office
import kpt.core.model.staff.Staff
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Ported from `6b66e8a43:feature/collectionSheet/.../NewIndividualCollectionSheetViewModel.kt` + the
 * form state its screen held in `rememberSaveable`s.
 *
 * What changed, and why:
 *  - **The sheet is not fetched here.** The original fetched it on this screen, showed a "Found
 *    Collection Sheet" bottom sheet, then serialised the WHOLE sheet into the next route as JSON. Here
 *    [NewCollectionSheetEvent.OpenSheet] carries only the request (office, staff, date) and the
 *    details screen streams the sheet itself — store-backed, so offline it shows the last sheet
 *    generated for that request. Its header carries the date and member count the bottom sheet did.
 *  - **Staff is optional and absent when unset.** The original sent `staffId = 0` when none was
 *    chosen; 0 is not a staff member.
 *  - **The date picker allows today and the past.** The original allowed only `>= now` — which
 *    excluded today's meeting itself and every past one, while a repayment dated in the future is the
 *    one thing Fineract rejects.
 *  - **The "Saved" tab is gone.** It rendered a fixed "No Saved Collection Sheet" with a TODO to
 *    integrate an API that never existed — a control that looked functional and was not.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class NewCollectionSheetViewModel(
    offices: OfficeRepository,
    private val staff: StaffRepository,
) : BaseViewModel<NewCollectionSheetState, NewCollectionSheetEvent, NewCollectionSheetAction>(
    NewCollectionSheetState(dateMillis = Clock.System.now().toEpochMilliseconds()),
) {

    private val officesStream = offices.officesStream(viewModelScope)

    val offices: StateFlow<ScreenState<List<Office>>> = officesStream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** One stream per office, kept: switching back to an office reuses its stream rather than re-opening one. */
    private val staffStreams = mutableMapOf<Int, ScreenDataStream<List<Staff>>>()

    private fun staffStreamFor(officeId: Int) =
        staffStreams.getOrPut(officeId) { staff.staffStream(officeId, viewModelScope) }

    /** The chosen office's staff; empty until an office is chosen (or while it loads, or if it fails). */
    val staffOptions: StateFlow<List<Staff>> = stateFlow
        .map { it.office?.id }
        .distinctUntilChanged()
        .flatMapLatest { officeId ->
            if (officeId == null) {
                flowOf(emptyList())
            } else {
                staffStreamFor(officeId).state.map { it.dataOrNull.orEmpty() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), emptyList())

    fun onRetry() = officesStream.retry()

    override fun handleAction(action: NewCollectionSheetAction) {
        when (action) {
            // A staff member belongs to one office — a choice made under the previous office is cleared.
            is NewCollectionSheetAction.SelectOffice ->
                mutableStateFlow.value = state.copy(office = action.office, staff = null)
            is NewCollectionSheetAction.SelectStaff -> mutableStateFlow.value = state.copy(staff = action.staff)
            NewCollectionSheetAction.OpenDatePicker -> mutableStateFlow.value = state.copy(datePickerOpen = true)
            NewCollectionSheetAction.CloseDatePicker -> mutableStateFlow.value = state.copy(datePickerOpen = false)
            is NewCollectionSheetAction.DatePicked ->
                mutableStateFlow.value = state.copy(dateMillis = action.millis, datePickerOpen = false)
            NewCollectionSheetAction.Clear ->
                mutableStateFlow.value =
                    NewCollectionSheetState(dateMillis = Clock.System.now().toEpochMilliseconds())
            NewCollectionSheetAction.Generate -> state.office?.let { office ->
                sendEvent(
                    NewCollectionSheetEvent.OpenSheet(
                        officeId = office.id,
                        staffId = state.staff?.id,
                        transactionDate = ApiDateFormatter.formatForApi(state.dateMillis),
                    ),
                )
            }
            NewCollectionSheetAction.NavigateBack -> sendEvent(NewCollectionSheetEvent.NavigateBack)
        }
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/** A chosen option: its id and the label the dropdown shows. */
internal data class Choice(val id: Int, val label: String)

internal data class NewCollectionSheetState(
    val office: Choice? = null,
    val staff: Choice? = null,
    val dateMillis: Long,
    val datePickerOpen: Boolean = false,
) {
    /** Generate needs an office; staff is optional, exactly as Fineract's request treats it. */
    val canGenerate: Boolean get() = office != null
}

internal sealed interface NewCollectionSheetEvent {
    data object NavigateBack : NewCollectionSheetEvent

    /** The request that selects a sheet — the details screen streams the sheet for it. */
    data class OpenSheet(val officeId: Int, val staffId: Int?, val transactionDate: String) : NewCollectionSheetEvent
}

internal sealed interface NewCollectionSheetAction {
    data class SelectOffice(val office: Choice) : NewCollectionSheetAction
    data class SelectStaff(val staff: Choice) : NewCollectionSheetAction
    data object OpenDatePicker : NewCollectionSheetAction
    data object CloseDatePicker : NewCollectionSheetAction
    data class DatePicked(val millis: Long) : NewCollectionSheetAction
    data object Generate : NewCollectionSheetAction
    data object Clear : NewCollectionSheetAction
    data object NavigateBack : NewCollectionSheetAction
}
