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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.objects.note.Note
import kpt.feature.note.generated.resources.Res
import kpt.feature.note.generated.resources.feature_note_add_item
import kpt.feature.note.generated.resources.feature_note_button_cancel
import kpt.feature.note.generated.resources.feature_note_button_dismiss
import kpt.feature.note.generated.resources.feature_note_cd_add
import kpt.feature.note.generated.resources.feature_note_cd_back
import kpt.feature.note.generated.resources.feature_note_delete
import kpt.feature.note.generated.resources.feature_note_delete_note
import kpt.feature.note.generated.resources.feature_note_delete_note_confirmation
import kpt.feature.note.generated.resources.feature_note_edit_note
import kpt.feature.note.generated.resources.feature_note_error_delete_offline
import kpt.feature.note.generated.resources.feature_note_no_item_found
import kpt.feature.note.generated.resources.feature_note_notes
import kpt.feature.note.generated.resources.feature_note_unexpected_error
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NoteListScreen(
    onBackClick: () -> Unit,
    onAddNote: (resourceType: String, resourceId: Int) -> Unit,
    onEditNote: (resourceType: String, resourceId: Int, noteId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteListViewModel = koinViewModel(),
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val ui by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                NoteListEvent.NavigateBack -> onBackClick()
                is NoteListEvent.NavigateToAdd -> onAddNote(event.resourceType, event.resourceId)
                is NoteListEvent.NavigateToEdit -> onEditNote(event.resourceType, event.resourceId, event.noteId)
            }
        }
    }

    NoteListContent(
        notes = notes,
        ui = ui,
        onAction = { viewModel.actionChannel.trySend(it) },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests without Koin or a signed-in session. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteListContent(
    notes: ScreenState<List<Note>>,
    ui: NoteListState,
    onAction: (NoteListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(TestTags.NoteList.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feature_note_notes)) },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(NoteListAction.BackClicked) },
                        modifier = Modifier.testTag(TestTags.NoteList.BACK),
                    ) {
                        Icon(
                            AppIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.feature_note_cd_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(NoteListAction.AddClicked) },
                modifier = Modifier.testTag(TestTags.NoteList.FAB),
            ) {
                Icon(
                    AppIcons.Add,
                    contentDescription = stringResource(Res.string.feature_note_cd_add),
                )
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // A failed WRITE is shown ABOVE the list, never instead of it. The notes are still
            // correct; replacing them with an error screen because a delete failed would hide
            // good data behind an unrelated failure.
            ui.writeError?.let { error ->
                WriteErrorBanner(
                    error = error,
                    onDismiss = { onAction(NoteListAction.DismissDialog) },
                )
            }

            ScreenContent(
                state = notes,
                onRetry = { onAction(NoteListAction.Retry) },
                modifier = Modifier.fillMaxSize().testTag(TestTags.NoteList.LIST),
            ) { notes, _ ->
                if (notes.isEmpty()) {
                    EmptyNotes()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(notes, key = { it.id ?: 0L }) { note ->
                            NoteRow(
                                note = note,
                                expanded = ui.expandedNoteId == note.id,
                                onToggle = { onAction(NoteListAction.ToggleExpanded(note.id)) },
                                onEdit = { onAction(NoteListAction.EditClicked) },
                                onDelete = { onAction(NoteListAction.DeleteClicked) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (ui.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onConfirm = { onAction(NoteListAction.ConfirmDelete) },
            onDismiss = { onAction(NoteListAction.DismissDialog) },
        )
    }
}

/**
 * One note. Tapping it reveals the actions rather than navigating.
 *
 * That is the original's interaction and it is kept deliberately: edit and delete on a row the user
 * has not opened would put a destructive control one mis-tap away in a scrolling list.
 */
@Composable
internal fun NoteRow(
    note: Note,
    expanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag(TestTags.NoteList.row(note.id)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = note.noteContent.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
            )

            // Attribution, when the server supplied it. The original rendered the raw `createdOn`
            // string straight from the API; it is omitted rather than shown unparsed, because a
            // half-formatted timestamp reads as a bug.
            note.createdByUsername?.let { author ->
                Text(
                    text = author,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag(TestTags.NoteList.ROW_ACTIONS),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag(TestTags.NoteList.ROW_EDIT),
                    ) { Text(stringResource(Res.string.feature_note_edit_note)) }

                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag(TestTags.NoteList.ROW_DELETE),
                    ) { Text(stringResource(Res.string.feature_note_delete_note)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotes() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            AppIcons.Add,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(Res.string.feature_note_no_item_found),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = stringResource(Res.string.feature_note_add_item),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(TestTags.NoteList.DELETE_DIALOG),
        title = { Text(stringResource(Res.string.feature_note_delete_note)) },
        text = { Text(stringResource(Res.string.feature_note_delete_note_confirmation)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.feature_note_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.feature_note_button_cancel))
            }
        },
    )
}

@Composable
internal fun WriteErrorBanner(error: NoteWriteError, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(TestTags.NoteList.WRITE_ERROR),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                when (error) {
                    NoteWriteError.Offline -> Res.string.feature_note_error_delete_offline
                    NoteWriteError.Failed -> Res.string.feature_note_unexpected_error
                },
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onDismiss) {
            Text(stringResource(Res.string.feature_note_button_dismiss))
        }
    }
}
