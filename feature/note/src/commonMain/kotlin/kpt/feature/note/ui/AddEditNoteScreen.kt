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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.ui.submit.MutationScreenContent
import kpt.core.designsystem.icon.AppIcons
import kpt.feature.note.generated.resources.Res
import kpt.feature.note.generated.resources.feature_note_add_note
import kpt.feature.note.generated.resources.feature_note_button_add
import kpt.feature.note.generated.resources.feature_note_button_cancel
import kpt.feature.note.generated.resources.feature_note_button_confirm
import kpt.feature.note.generated.resources.feature_note_button_update
import kpt.feature.note.generated.resources.feature_note_cd_back
import kpt.feature.note.generated.resources.feature_note_dialog_warning
import kpt.feature.note.generated.resources.feature_note_dialog_warning_message
import kpt.feature.note.generated.resources.feature_note_edit_note_label
import kpt.feature.note.generated.resources.feature_note_update_note
import kpt.feature.note.generated.resources.feature_note_write_note_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AddEditNoteScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditNoteViewModel = koinViewModel(),
) {
    // TWO flows: the submit/draft lifecycle from BaseMutationViewModel, and the editable body.
    val mutation by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()

    MutationScreenContent(
        state = mutation,
        onRetry = viewModel::onRetry,
        // Reached on a real server ACK and on a draft saved offline alike — in both cases the note
        // is safe (sent, or durable and queued), so leaving the editor is correct either way.
        onSubmitted = { onBackClick() },
        modifier = modifier,
    ) { _, _ ->
        AddEditNoteContent(
            form = form,
            isSubmitting = mutation.isSubmitting,
            onTextChange = viewModel::onTextChange,
            onSubmit = viewModel::submitForm,
            onBackClick = {
                if (form.isDirty) viewModel.onDiscardRequested() else onBackClick()
            },
            onDiscardDismissed = viewModel::onDiscardDismissed,
            onDiscardConfirmed = onBackClick,
        )
    }
}

/**
 * Stateless half — rendered in tests without Koin.
 *
 * The title / label / button text all derive from `editEnabled`. The original carried three
 * `StringResource` fields in its STATE and reassigned them in `init`, which put presentation in the
 * state machine and made the state un-testable without a Compose resource runtime.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditNoteContent(
    form: NoteFormState,
    isSubmitting: Boolean,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    onDiscardDismissed: () -> Unit,
    onDiscardConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(TestTags.AddEditNote.SCREEN),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (form.editEnabled) {
                                Res.string.feature_note_update_note
                            } else {
                                Res.string.feature_note_add_note
                            },
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(
                        // Routed through the ViewModel, NOT straight to onBackClick: it is the
                        // ViewModel that knows whether there are unsaved changes worth asking about.
                        onClick = onBackClick,
                        modifier = Modifier.testTag(TestTags.AddEditNote.BACK),
                    ) {
                        Icon(
                            AppIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.feature_note_cd_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        ) {
            OutlinedTextField(
                value = form.text,
                onValueChange = onTextChange,
                label = {
                    Text(
                        stringResource(
                            if (form.editEnabled) {
                                Res.string.feature_note_edit_note_label
                            } else {
                                Res.string.feature_note_write_note_label
                            },
                        ),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .testTag(TestTags.AddEditNote.FIELD),
            )

            // The write error is GONE from this screen, and that is the change.
            //
            // It used to render inline under the field because a failure left the note nowhere but
            // on screen. Now a refused submit becomes a durable draft: `MutationScreenContent`
            // shows the submit status, and the note itself is in `framework_submit_drafts`,
            // replayed on reconnect and listed in Settings → Sync & Drafts. There is no longer a
            // failure state in which the text exists only in this text field.

            Button(
                onClick = onSubmit,
                // Disabled on a blank note and while in flight. The original allowed both: an
                // empty note round-tripped to the server to be rejected, and a double-tap sent two.
                // The in-flight half is now also guarded by the framework (`SubmitHandler.submit`
                // no-ops while Submitting); this keeps the BUTTON from looking tappable meanwhile.
                enabled = form.canSubmit && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .testTag(TestTags.AddEditNote.SUBMIT),
            ) {
                Text(
                    stringResource(
                        if (form.editEnabled) {
                            Res.string.feature_note_button_update
                        } else {
                            Res.string.feature_note_button_add
                        },
                    ),
                )
            }
        }
    }

    if (form.showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = onDiscardDismissed,
            modifier = Modifier.testTag(TestTags.AddEditNote.DISCARD_DIALOG),
            title = { Text(stringResource(Res.string.feature_note_dialog_warning)) },
            text = { Text(stringResource(Res.string.feature_note_dialog_warning_message)) },
            confirmButton = {
                TextButton(onClick = onDiscardConfirmed) {
                    Text(stringResource(Res.string.feature_note_button_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDiscardDismissed) {
                    Text(stringResource(Res.string.feature_note_button_cancel))
                }
            },
        )
    }
}
