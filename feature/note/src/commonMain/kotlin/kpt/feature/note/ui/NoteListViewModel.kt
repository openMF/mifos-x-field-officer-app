/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.note.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.Constants
import kpt.core.data.note.NoteCommandRepository
import kpt.core.data.note.NoteRepository
import kpt.core.model.objects.note.Note

/**
 * Notes attached to one client — ported from
 * `6b66e8a43:feature/note/src/commonMain/kotlin/com/mifos/feature/note/notes/NoteViewModel.kt`.
 *
 * ## What changed from the original, and why
 *
 * The original carried FOUR separate encodings of "what is happening": a `DataState` collected from
 * the repository, a nullable `dialogState` (Loading / Error / ShowDialog), an `isRefreshing` flag,
 * and a `networkConnection` boolean fed by its own `NetworkMonitor` collector. They could disagree
 * — and did: `loadNote()` wrote `dialogState = Loading` only `if (!state.isRefreshing)`, so a
 * pull-to-refresh that failed left the screen with no loading indicator AND no error.
 *
 * `ScreenState` expresses all four once, decided by the Store: Loading / Content / Empty /
 * NoNetwork / Error. The screen renders whatever it says. `NetworkMonitor` is gone from this layer
 * because `NoNetwork` already IS the connectivity answer, scoped to the read that needs it.
 *
 * The DIALOG state is genuinely separate and stays separate — a delete confirmation is a user
 * decision, not a data state, and conflating the two is what made the original's `dialogState` do
 * three unrelated jobs.
 */
internal class NoteListViewModel(
    repository: NoteRepository,
    private val commands: NoteCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<NoteListState, NoteListEvent, NoteListAction>(NoteListState()) {

    /**
     * Read once from the route argument.
     *
     * The owner — Fineract's `/{resourceType}/{resourceId}/notes` (clients, groups). See `NoteNavigation`.
     */
    private val resourceType: String = savedStateHandle.get<String>(RESOURCE_TYPE_KEY) ?: Constants.ENTITY_TYPE_CLIENTS
    private val resourceId: Int = savedStateHandle.get<Int>(RESOURCE_ID_KEY) ?: 0

    /**
     * Held, not collected inline — the error state's Retry has to reach the stream that failed.
     * A Retry wired to `{}` is the dead-clickable defect in its most misleading form.
     */
    private val notesStream = repository.notesStream(resourceType, resourceId.toLong(), viewModelScope)

    /**
     * The client's notes. `stateIn(… WhileSubscribed …)` rather than an `init{}` hand-lift — the
     * latter kept collecting for the ViewModel's whole lifetime, so a notes screen left on the back
     * stack went on refreshing. See `ClientListViewModel.screenState`.
     */
    val notes: StateFlow<ScreenState<List<Note>>> = notesStream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: NoteListAction) {
        when (action) {
            NoteListAction.Retry -> notesStream.retry()
            NoteListAction.Refresh -> notesStream.refresh()
            NoteListAction.BackClicked -> sendEvent(NoteListEvent.NavigateBack)
            NoteListAction.AddClicked -> sendEvent(NoteListEvent.NavigateToAdd(resourceType, resourceId))

            // Edit acts on the EXPANDED row rather than on an id carried in the action. That
            // mirrors the original, where the action strip belongs to whichever row is open, and it
            // keeps "which note" in exactly one place.
            NoteListAction.EditClicked ->
                state.expandedNoteId?.let { sendEvent(NoteListEvent.NavigateToEdit(resourceType, resourceId, it)) }

            is NoteListAction.ToggleExpanded ->
                mutableStateFlow.value = state.copy(
                    expandedNoteId = if (state.expandedNoteId == action.id) null else action.id,
                )

            NoteListAction.DeleteClicked ->
                mutableStateFlow.value = state.copy(showDeleteConfirmation = true)

            NoteListAction.DismissDialog ->
                mutableStateFlow.value = state.copy(showDeleteConfirmation = false, writeError = null)

            NoteListAction.ConfirmDelete -> confirmDelete()
        }
    }

    /**
     * Delete is the only destructive action on this screen, so it is the only one that confirms.
     *
     * The original called `getNoteOptionsAndObserveNetwork()` after a successful delete — a full
     * re-fetch AND a second network-observer coroutine every time, so the collectors accumulated
     * for the life of the screen. Only the second half of that was wrong: the re-subscribe.
     *
     * ## No re-read after a delete
     *
     * An earlier version force-refreshed the stream here, because `NoteCommandRepository` was
     * cache-free: the server deleted the note, the client was told `Applied`, and the row stayed
     * on screen — reported as "delete note is not working". The command now clears the row through
     * the note store, in the `Note` table this stream observes, so the list drops it by itself.
     */
    private fun confirmDelete() {
        val id = state.expandedNoteId ?: return
        mutableStateFlow.value = state.copy(isDeleting = true, showDeleteConfirmation = false)
        viewModelScope.launch {
            val result = commands.deleteNote(
                resourceType = resourceType,
                resourceId = resourceId.toLong(),
                noteId = id,
            )
            mutableStateFlow.value = when (result) {
                is MutationResult.Applied ->
                    state.copy(isDeleting = false, expandedNoteId = null, writeError = null)

                // Offline is NOT a failure the user caused, and it is recoverable by moving. The
                // original rendered every non-success as "Unexpected error", which told a field
                // officer in a dead spot that the app was broken.
                is MutationResult.Blocked ->
                    state.copy(
                        isDeleting = false,
                        writeError = if (result.reason == BlockReason.OFFLINE) {
                            NoteWriteError.Offline
                        } else {
                            NoteWriteError.Failed
                        },
                    )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "deleteNote failed" }
                    state.copy(isDeleting = false, writeError = NoteWriteError.Failed)
                }

                is MutationResult.Conflicted ->
                    state.copy(isDeleting = false, writeError = NoteWriteError.Failed)
            }
        }
    }

    private companion object {
        /** Matches the route argument name in `NoteNavigation.NoteListRoute`. */
        const val RESOURCE_TYPE_KEY = "resourceType"
        const val RESOURCE_ID_KEY = "resourceId"
        const val TAG = "NoteListViewModel"

        /** Template default — long enough to survive a rotation or a brief tab switch. */
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * One state, one source.
 *
 * `count` is DERIVED from the same stream the list renders, so a header cannot disagree with the
 * list below it.
 */
internal data class NoteListState(
    val expandedNoteId: Long? = null,
    val showDeleteConfirmation: Boolean = false,
    val isDeleting: Boolean = false,
    val writeError: NoteWriteError? = null,
)

/**
 * Why a WRITE failure is modelled apart from the read's [ScreenState].
 *
 * A failed delete must not blank the list — the notes are still there and still correct. Folding it
 * into `ScreenState.Error` would replace good content with an error screen because an unrelated
 * mutation failed.
 */
internal enum class NoteWriteError { Offline, Failed }

internal sealed interface NoteListEvent {
    data object NavigateBack : NoteListEvent
    data class NavigateToAdd(val resourceType: String, val resourceId: Int) : NoteListEvent
    data class NavigateToEdit(val resourceType: String, val resourceId: Int, val noteId: Long) : NoteListEvent
}

internal sealed interface NoteListAction {
    data object Retry : NoteListAction
    data object Refresh : NoteListAction
    data object BackClicked : NoteListAction
    data object AddClicked : NoteListAction
    data object EditClicked : NoteListAction
    data object DeleteClicked : NoteListAction
    data object ConfirmDelete : NoteListAction
    data object DismissDialog : NoteListAction
    data class ToggleExpanded(val id: Long?) : NoteListAction
}
