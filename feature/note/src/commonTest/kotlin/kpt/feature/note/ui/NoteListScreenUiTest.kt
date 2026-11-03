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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.note.Note
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [NoteListContent] (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 *
 * Renders the STATELESS half directly with a constructed state — no Koin, no ViewModel, no
 * repository. That is what makes each render a single unambiguous assertion about one state;
 * the state machine itself is covered by [NoteListViewModelTest].
 *
 * Per-screen tests live in the screen's OWN module `commonTest`, so `internal` composables in
 * `kpt.feature.note.ui` are reachable — no visibility change needed.
 */
@OptIn(ExperimentalTestApi::class)
class NoteListScreenUiTest {

    private fun note(id: Long, text: String) = Note(id = id, clientId = 42, noteContent = text)

    @Test
    fun theAddAffordanceIsAlwaysReachable() = runComposeUiTest {
        setContent {
            KptTheme {
                NoteListContent(
                    notes = ScreenState.Loading,
                    ui = NoteListState(),
                    onAction = {},
                )
            }
        }
        // The FAB lives in `Scaffold.floatingActionButton` and is present regardless of repository
        // state — including while the list is still loading, which is exactly when a user who
        // knows what they want to write should not have to wait.
        onNodeWithTag(TestTags.NoteList.FAB).assertIsDisplayed()
        onNodeWithTag(TestTags.NoteList.SCREEN).assertIsDisplayed()
    }

    @Test
    fun aRowRevealsItsActionsOnlyWhenExpanded() = runComposeUiTest {
        setContent {
            KptTheme {
                NoteListContent(
                    notes = ScreenState.Content(listOf(note(1, "first"))),
                    ui = NoteListState(),
                    onAction = {},
                )
            }
        }
        // Collapsed: no edit, no delete. Destructive controls must not sit one mis-tap away in a
        // scrolling list.
        onNodeWithTag(TestTags.NoteList.ROW_ACTIONS, useUnmergedTree = true).assertDoesNotExist()
        onNodeWithTag(TestTags.NoteList.row(1)).assertIsDisplayed()
    }

    @Test
    fun theExpandedRowShowsEditAndDelete() = runComposeUiTest {
        setContent {
            KptTheme {
                NoteListContent(
                    notes = ScreenState.Content(listOf(note(1, "first"))),
                    ui = NoteListState(expandedNoteId = 1),
                    onAction = {},
                )
            }
        }
        // Unmerged: the row is a clickable Card, so it merges its descendants' semantics and the
        // inner action tags are not addressable in the merged tree.
        onNodeWithTag(TestTags.NoteList.ROW_ACTIONS, useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag(TestTags.NoteList.ROW_EDIT, useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag(TestTags.NoteList.ROW_DELETE, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun deleteIsConfirmedBeforeItHappens() = runComposeUiTest {
        setContent {
            KptTheme {
                NoteListContent(
                    notes = ScreenState.Content(listOf(note(1, "first"))),
                    ui = NoteListState(expandedNoteId = 1, showDeleteConfirmation = true),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(TestTags.NoteList.DELETE_DIALOG).assertIsDisplayed()
    }

    @Test
    fun aFailedWriteRendersAboveTheNotesRatherThanReplacingThem() = runComposeUiTest {
        setContent {
            KptTheme {
                NoteListContent(
                    notes = ScreenState.Content(listOf(note(1, "keep me"))),
                    ui = NoteListState(writeError = NoteWriteError.Offline),
                    onAction = {},
                )
            }
        }
        // THE REGRESSION THIS EXISTS FOR, as a RENDER rather than a state assertion: the banner
        // and the list must both be on screen. A unit test can prove the state still holds the
        // notes; only a render proves they were not covered by an error screen.
        onNodeWithTag(TestTags.NoteList.WRITE_ERROR).assertIsDisplayed()
        onNodeWithTag(TestTags.NoteList.row(1)).assertIsDisplayed()
    }
}
