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

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 *
 * `AddEditNoteContent` is the stateless half; the `*Screen` entry composable resolves its
 * ViewModel through Koin and is therefore not previewed.
 *
 * Every literal here is PREVIEW FIXTURE DATA carrying `// i18n:skip`. `AddEditNoteState.text`
 * holds what a field officer TYPED — note content, not app copy — so a fixture for it is sample
 * user input, and translating sample input would be meaningless.
 *
 * These five cover the states the screen can actually be in, and three are states a single
 * "happy" fixture would never render.
 */

@Preview
@Composable
internal fun AddEditNoteContentAddPreview() {
    // Add mode with an empty field: submit must be DISABLED. The original let a blank note through
    // and the server rejected it — a round trip to be told nothing was typed.
    KptTheme {
        AddEditNoteContent(
            form = NoteFormState(),
            isSubmitting = false,
            onTextChange = {},
            onSubmit = {},
            onBackClick = {},
            onDiscardDismissed = {},
            onDiscardConfirmed = {},
        )
    }
}

@Preview
@Composable
internal fun AddEditNoteContentEditPreview() {
    // Edit mode: title, label and button text all change off `editEnabled`. The original carried
    // three StringResource fields in its STATE to do this; here the screen picks its own copy.
    KptTheme {
        AddEditNoteContent(
            form = NoteFormState(
                // i18n:skip — preview fixture
                text = "Follow up on the missed repayment.",
                // i18n:skip — preview fixture
                loadedText = "Follow up on the missed repayment.",
                editEnabled = true,
            ),
            isSubmitting = false,
            onTextChange = {},
            onSubmit = {},
            onBackClick = {},
            onDiscardDismissed = {},
            onDiscardConfirmed = {},
        )
    }
}

@Preview
@Composable
internal fun AddEditNoteContentSubmittingPreview() {
    // In flight: submit disabled so a double-tap cannot send the note twice.
    KptTheme {
        AddEditNoteContent(
            form = NoteFormState(
                // i18n:skip — preview fixture
                text = "Saving this one.",
            ),
            isSubmitting = true,
            onTextChange = {},
            onSubmit = {},
            onBackClick = {},
            onDiscardDismissed = {},
            onDiscardConfirmed = {},
        )
    }
}

/*
 * REMOVED: `AddEditNoteContentOfflineKeepsTheTextPreview`.
 *
 * It rendered the inline write-error with the user's text still above it — the best this screen
 * could do when a refused submit left the note nowhere but in the text field.
 *
 * That state no longer exists. A refused submit now writes the note to `framework_submit_drafts`
 * and `OfflineSubmitSyncer` replays it on reconnect, so there is no "offline, text at risk" render
 * to pin. The durability it used to approximate is covered where it actually lives:
 * `AddEditNoteViewModelTest` asserts the draft is persisted under its unique key, and Settings →
 * Sync & Drafts renders the queued note.
 */

/*
 * NOT PREVIEWED: the discard-confirmation dialog.
 *
 * An `AlertDialog` composes into its own window, so the render tier sees TWO roots and
 * `onRoot()` in `NotePreviewScreenshotTest` is ambiguous — it fails with "Expected exactly 1
 * node but found 2". This is a property of the device-free tier, not of this screen: the
 * template has zero dialog previews across its 38 preview files for the same reason.
 *
 * The state is NOT uncovered. `AddEditNoteScreenUiTest.theDiscardWarningRendersWhenThereIsSomethingToLose`
 * asserts it through `runComposeUiTest`, which addresses nodes across roots happily. Dialogs
 * belong to the UI-test tier; the screenshot tier covers the screen body.
 */
