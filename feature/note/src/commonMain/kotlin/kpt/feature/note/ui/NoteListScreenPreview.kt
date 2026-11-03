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
import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.note.Note
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * The `*Screen` entry composables are not previewed: they resolve their ViewModel through Koin.
 * `NoteListContent` is, because it is the stateless half and takes its state as a parameter.
 *
 * Literals below are PREVIEW FIXTURE DATA — never reachable from the running app, which is why
 * they carry `// i18n:skip` rather than string-resource keys. A fixture is note CONTENT (what a
 * field officer typed), not app COPY; translating it would be meaningless.
 */

internal fun previewNote(
    id: Long = 1,
    // i18n:skip — preview fixture, never rendered in the app
    content: String = "Visited the shop. Stock is low but takings are steady; follow up next cycle.",
    author: String? = "fieldofficer",
) = Note(
    id = id,
    clientId = 42,
    noteContent = content,
    createdByUsername = author,
)

@Preview
@Composable
internal fun NoteListContentPreview() {
    KptTheme {
        NoteListContent(
            notes = ScreenState.Content(
                listOf(
                    previewNote(),
                    // i18n:skip — preview fixture
                    previewNote(id = 2, content = "Called about the missed repayment."),
                ),
            ),
            ui = NoteListState(),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun NoteListContentEmptyPreview() {
    // The first-visit case, and the one a fixture full of rows can never show. A client with no
    // notes is normal, not an error, so it must read as an invitation rather than a failure.
    KptTheme {
        NoteListContent(
            notes = ScreenState.Content(emptyList()),
            ui = NoteListState(),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun NoteListContentLoadingPreview() {
    KptTheme {
        NoteListContent(notes = ScreenState.Loading, ui = NoteListState(), onAction = {})
    }
}

@Preview
@Composable
internal fun NoteListContentWriteErrorKeepsTheNotesPreview() {
    // THE ONE WORTH LOOKING AT. A failed delete renders ABOVE the list, not instead of it — the
    // notes are still correct and must stay on screen. Rendering it as a screen-level error would
    // hide good data behind an unrelated failure, and only a render shows that clearly.
    KptTheme {
        NoteListContent(
            notes = ScreenState.Content(listOf(previewNote())),
            ui = NoteListState(writeError = NoteWriteError.Offline),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun NoteRowCollapsedAndExpandedPreview() {
    // Tapping a row reveals its actions rather than navigating. Both halves of that toggle are
    // shown together because the difference between them IS the interaction.
    KptTheme {
        Column {
            NoteRow(
                note = previewNote(),
                expanded = false,
                onToggle = {},
                onEdit = {},
                onDelete = {},
            )
            NoteRow(
                // i18n:skip — preview fixture; `author = null` covers the unattributed row
                note = previewNote(id = 2, content = "Short note.", author = null),
                expanded = true,
                onToggle = {},
                onEdit = {},
                onDelete = {},
            )
        }
    }
}
