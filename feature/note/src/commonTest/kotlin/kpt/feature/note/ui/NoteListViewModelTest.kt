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
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeNoteCommandRepository
import kpt.core.data.testing.FakeNoteRepository
import kpt.core.model.objects.note.Note
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks what the port of `6b66e8a43:.../notes/NoteViewModel.kt` changed.
 *
 * The original had no tests, and could not easily have had any: four overlapping encodings of
 * "what is happening" (DataState + dialogState + isRefreshing + networkConnection) with no single
 * value to assert against.
 */
@OptIn(
    ExperimentalScreenDataStreamTestingApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalDataTestingApi::class,
)
class NoteListViewModelTest {

    private val repository = FakeNoteRepository()

    private fun viewModel(
        clientId: Int = 42,
        resourceType: String = "clients",
        outcome: () -> MutationResult<Unit> = { MutationResult.Applied(Unit, synced = true) },
    ): Pair<NoteListViewModel, FakeNoteCommandRepository> {
        val commands = FakeNoteCommandRepository(outcome)
        return NoteListViewModel(
            repository,
            commands,
            SavedStateHandle(mapOf("resourceType" to resourceType, "resourceId" to clientId)),
        ) to commands
    }

    private fun note(id: Long, text: String) = Note(id = id, clientId = 42, noteContent = text)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(Dispatchers.Unconfined)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theStreamIsKeyedOnTheClientFromTheRoute() = runTest {
        viewModel(clientId = 7)
        // The original read `resourceId` + `resourceType` from the route and passed BOTH to the
        // repository. The migrated read path is keyed on the client alone — faithful, because
        // every original caller passed "clients" (see NoteNavigation).
        assertEquals(listOf(7L), repository.requestedIds)
    }

    @Test
    fun theListCarriesExactlyTheNotesTheScreenCounts() = runTest {
        // The derived `count` moved out of the state holder when the stream became a StateFlow —
        // the screen now counts the same `notes` state it renders. The property is unchanged: ONE
        // source, so a header cannot disagree with the rows beneath it.
        val (vm, _) = viewModel()
        repository.state.value = ScreenState.Content(listOf(note(1, "first"), note(2, "second")))
        runCurrent()

        vm.notes.test {
            val content = assertIs<ScreenState.Content<List<Note>>>(expectMostRecentItem())
            assertEquals(2, content.data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deletingTheLastNoteLeavesAnEmptyListNotTheDeletedRow() = runTest {
        // The stream now reports "no notes" as an empty Content (no `isEmpty` predicate), which
        // the screen draws as EmptyNotes. With the predicate the deleted row stayed on screen.
        val (vm, _) = viewModel()
        repository.emit(note(1, "only"))
        runCurrent()
        repository.emit()
        runCurrent()

        vm.notes.test {
            val content = assertIs<ScreenState.Content<List<Note>>>(expectMostRecentItem())
            assertTrue(content.data.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun tappingTheSameRowTwiceCollapsesIt() = runTest {
        val (vm, _) = viewModel()
        vm.trySendAction(NoteListAction.ToggleExpanded(5))
        runCurrent()
        assertEquals(5L, vm.stateFlow.value.expandedNoteId)

        vm.trySendAction(NoteListAction.ToggleExpanded(5))
        runCurrent()
        // Collapses rather than re-opening — the action strip is a toggle, not a selection.
        assertNull(vm.stateFlow.value.expandedNoteId)
    }

    @Test
    fun deleteAlwaysConfirmsBeforeItDeletes() = runTest {
        val (vm, commands) = viewModel()
        vm.trySendAction(NoteListAction.ToggleExpanded(9))
        vm.trySendAction(NoteListAction.DeleteClicked)
        runCurrent()

        // Tapping Delete opens the dialog and sends NOTHING. Delete is irreversible, so the tap
        // itself must never be the commit.
        assertTrue(vm.stateFlow.value.showDeleteConfirmation)
        assertEquals(emptyList<FakeNoteCommandRepository.Written>(), commands.deleted)
    }

    @Test
    fun confirmingDeleteSendsTheClientResourceType() = runTest {
        val (vm, commands) = viewModel(clientId = 42)
        vm.trySendAction(NoteListAction.ToggleExpanded(9))
        vm.trySendAction(NoteListAction.DeleteClicked)
        vm.trySendAction(NoteListAction.ConfirmDelete)
        runCurrent()

        // The endpoint is /{resourceType}/{resourceId}/notes, so the type still reaches the wire
        // even though it is no longer a route parameter.
        assertEquals(
            listOf(FakeNoteCommandRepository.Written("clients", 42L, noteId = 9L)),
            commands.deleted,
        )
    }

    @Test
    fun aFailedDeleteLeavesTheNotesOnScreen() = runTest {
        val (vm, _) = viewModel(
            outcome = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        repository.state.value = ScreenState.Content(listOf(note(1, "keep me")))
        vm.trySendAction(NoteListAction.ToggleExpanded(1))
        vm.trySendAction(NoteListAction.DeleteClicked)
        vm.trySendAction(NoteListAction.ConfirmDelete)
        runCurrent()

        // THE REGRESSION THIS EXISTS FOR: a write failure must not blank the read. The notes are
        // still there and still correct; folding the failure into ScreenState.Error would replace
        // good content with an error screen because an unrelated mutation failed.
        assertEquals(NoteWriteError.Offline, vm.stateFlow.value.writeError)
        // …and the notes are STILL THERE. That is the half the regression is about.
        vm.notes.test {
            val content = assertIs<ScreenState.Content<List<Note>>>(expectMostRecentItem())
            assertEquals(1, content.data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun offlineIsReportedAsOfflineNotAsAnUnexpectedError() = runTest {
        val (vm, _) = viewModel(outcome = { MutationResult.Blocked(BlockReason.OFFLINE) })
        vm.trySendAction(NoteListAction.ToggleExpanded(1))
        vm.trySendAction(NoteListAction.ConfirmDelete)
        runCurrent()
        // The original rendered every non-success as "Unexpected error", which tells a field
        // officer in a dead spot that the app is broken rather than that they have no signal.
        assertEquals(NoteWriteError.Offline, vm.stateFlow.value.writeError)
    }

    @Test
    fun editActsOnTheExpandedRow() = runTest {
        val (vm, _) = viewModel(clientId = 42)
        vm.trySendAction(NoteListAction.ToggleExpanded(3))
        runCurrent()

        vm.eventFlow.test {
            vm.trySendAction(NoteListAction.EditClicked)
            // Carries the EXPANDED row's id, not an id threaded through the action. "Which note"
            // lives in exactly one place.
            assertEquals(NoteListEvent.NavigateToEdit("clients", 42, 3L), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addCarriesTheClientFromTheRoute() = runTest {
        val (vm, _) = viewModel(clientId = 42)
        vm.eventFlow.test {
            vm.trySendAction(NoteListAction.AddClicked)
            assertEquals(NoteListEvent.NavigateToAdd("clients", 42), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun editWithNoExpandedRowDoesNothing() = runTest {
        val (vm, _) = viewModel()
        vm.trySendAction(NoteListAction.EditClicked)
        runCurrent()
        // No row open means no note to edit. Emitting a navigation event with a null id would
        // open the editor on nothing.
        assertNull(vm.stateFlow.value.expandedNoteId)
    }

    @Test
    fun aSuccessfulDeleteNeedsNoReRead() = runTest {
        // Device report "delete note is not working" was a cache-free command leaving the row on
        // screen; this ViewModel used to force a re-read to hide it. The command now clears the row
        // through the note store (NoteRepositoryTest), so a forced re-read would be a second fetch
        // for nothing.
        val (vm, _) = viewModel()
        repository.state.value = ScreenState.Content(listOf(note(1, "doomed")))
        runCurrent()

        repository.forceFreshes.test {
            vm.trySendAction(NoteListAction.ToggleExpanded(1L))
            vm.trySendAction(NoteListAction.DeleteClicked)
            vm.trySendAction(NoteListAction.ConfirmDelete)
            runCurrent()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.stateFlow.value.expandedNoteId)
    }
}
