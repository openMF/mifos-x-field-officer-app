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

/**
 * Append-only test-tag registry for the note feature.
 *
 * Consumed by:
 *  - Compose UI tests in `feature/note/src/commonTest/`
 *    (`NoteListScreenUiTest`, `AddEditNoteScreenUiTest`).
 *  - Maestro on-device flows — Maestro resolves these string constants via
 *    `assertVisible: { id: "..." }` matchers on Android's `Modifier.testTag(...)`
 *    semantics.
 *
 * **APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).** Removing or renaming any
 * string breaks the build (test-side compile reference) and the on-device Maestro flows in
 * the same commit. Add new tags at the bottom. If a tag genuinely must be retired, add a
 * sibling `// uitest-tag-retire:` annotation and land the source deletion in the same commit
 * as the matching Maestro / UI-test callers.
 */
object TestTags {

    /** Tags for [NoteListScreen]. */
    object NoteList {
        const val SCREEN: String = "note_list_screen"
        const val LIST: String = "note_list"
        const val BACK: String = "note_list_back"

        /** FAB that opens the "Add note" screen. Always present regardless of repository state. */
        const val FAB: String = "note_list_fab"

        /** The expanded action strip on a row — edit + delete. Absent until the row is expanded. */
        const val ROW_ACTIONS: String = "note_list_row_actions"
        const val ROW_EDIT: String = "note_list_row_edit"
        const val ROW_DELETE: String = "note_list_row_delete"

        /** Delete confirmation. Destructive and irreversible, so it is always confirmed. */
        const val DELETE_DIALOG: String = "note_list_delete_dialog"

        /**
         * Write-failure banner. Distinct from the list's own error state: a failed WRITE leaves
         * the notes intact, so it must not be rendered as a screen-level error that replaces them.
         */
        const val WRITE_ERROR: String = "note_list_write_error"

        /** One row, suffixed with the note id so a test can address a specific note. */
        fun row(id: Long?): String = "note_list_row_$id"
    }

    /** Tags for [AddEditNoteScreen]. */
    object AddEditNote {
        const val SCREEN: String = "note_addedit_screen"
        const val FIELD: String = "note_addedit_field"
        const val SUBMIT: String = "note_addedit_submit"
        const val BACK: String = "note_addedit_back"

        /** Unsaved-changes warning, shown only when the text actually differs from what loaded. */
        const val DISCARD_DIALOG: String = "note_addedit_discard_dialog"

        /** Inline write-failure message. The user's text stays on screen above it. */
        const val WRITE_ERROR: String = "note_addedit_write_error"
    }
}
