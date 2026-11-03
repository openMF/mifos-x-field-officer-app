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
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [AddEditNoteContent] (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 *
 * Renders the stateless half directly — no Koin, no ViewModel. The state machine is covered by
 * [AddEditNoteViewModelTest]; these pin what the user can SEE and TOUCH in each state.
 */
@OptIn(ExperimentalTestApi::class)
class AddEditNoteScreenUiTest {

    @Test
    fun aBlankNoteCannotBeSubmitted() = runComposeUiTest {
        setContent {
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
        // Disabled, not hidden. The user must be able to see what the screen is FOR before they
        // have typed anything.
        onNodeWithTag(TestTags.AddEditNote.SUBMIT).assertIsNotEnabled()
        onNodeWithTag(TestTags.AddEditNote.FIELD).assertIsDisplayed()
    }

    @Test
    fun typingEnablesSubmit() = runComposeUiTest {
        setContent {
            KptTheme {
                AddEditNoteContent(
                    // i18n:skip — test fixture
                    form = NoteFormState(text = "visited the shop"),
                    isSubmitting = false,
                    onTextChange = {},
                    onSubmit = {},
                    onBackClick = {},
                    onDiscardDismissed = {},
                    onDiscardConfirmed = {},
                )
            }
        }
        onNodeWithTag(TestTags.AddEditNote.SUBMIT).assertIsEnabled()
    }

    @Test
    fun submitIsDisabledWhileAWriteIsInFlight() = runComposeUiTest {
        setContent {
            KptTheme {
                AddEditNoteContent(
                    // i18n:skip — test fixture
                    form = NoteFormState(text = "sending"),
                    isSubmitting = true,
                    onTextChange = {},
                    onSubmit = {},
                    onBackClick = {},
                    onDiscardDismissed = {},
                    onDiscardConfirmed = {},
                )
            }
        }
        // A double-tap must not send the note twice.
        onNodeWithTag(TestTags.AddEditNote.SUBMIT).assertIsNotEnabled()
    }

    /*
     * REMOVED: the offline-write-error test.
     *
     * It asserted that a failed submit kept the user's text on screen beneath an inline error —
     * the best this screen could do when a refusal left the note nowhere else. There is no such
     * state now: a refused submit becomes a durable draft, so the guarantee moved from "the text
     * is still in the field" to "the note is in the outbox", which is asserted in
     * AddEditNoteViewModelTest where it belongs.
     */
    @Test
    fun theDiscardWarningRendersWhenThereIsSomethingToLose() = runComposeUiTest {
        setContent {
            KptTheme {
                AddEditNoteContent(
                    form = NoteFormState(
                        // i18n:skip — test fixture
                        text = "half a thought",
                        loadedText = "",
                        showDiscardConfirmation = true,
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
        onNodeWithTag(TestTags.AddEditNote.DISCARD_DIALOG).assertIsDisplayed()
    }
}
