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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.common.utils.Constants
import kpt.core.data.note.NOTE_FORM_KEY
import kpt.core.data.note.NoteSubmitException
import kpt.core.model.objects.note.Note
import kpt.feature.note.testing.FakeNoteCommandRepository
import kpt.feature.note.testing.FakeNoteRepository
import kpt.feature.note.testing.FakeSubmitOutbox
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The app's first offline-durable form.
 *
 * The tests that matter are the DURABILITY ones: before this change a refused submit left the
 * note in a text field and nowhere else, so navigating away lost it. The guarantee is now "the
 * note is in the outbox", and these assert exactly that rather than taking it on trust.
 */
class AddEditNoteViewModelTest {

    private val repository = FakeNoteRepository()
    private val commands = FakeNoteCommandRepository()
    private val outbox = FakeSubmitOutbox<kpt.core.model.note.NoteDraft>()

    @BeforeTest fun setUp() = Dispatchers.setMain(Dispatchers.Unconfined)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun note(id: Long, text: String) = Note(id = id, clientId = 7, noteContent = text)

    private fun viewModel(clientId: Int = 7, noteId: Long? = null): AddEditNoteViewModel {
        val handle = SavedStateHandle(
            buildMap<String, Any> {
                put("resourceType", "clients")
                put("resourceId", clientId)
                noteId?.let { put("noteId", it) }
            },
        )
        return AddEditNoteViewModel(repository, commands, outbox, handle)
    }

    @Test
    fun aMissingNoteIdMeansAdd() = runTest {
        val vm = viewModel()
        assertFalse(vm.formState.value.editEnabled)
    }

    @Test
    fun theEditorSeedsItselfFromTheNoteAlreadyInTheStore() = runTest {
        repository.emit(note(1, "first"), note(2, "second"))
        val vm = viewModel(noteId = 2)
        runCurrent()

        // Seeded from the CACHE — no second per-note read.
        assertEquals("second", vm.formState.value.text)
        assertFalse(vm.formState.value.isDirty)
    }

    @Test
    fun aBlankNoteCannotBeSubmitted() = runTest {
        val vm = viewModel()
        assertFalse(vm.formState.value.canSubmit)

        vm.onTextChange("   ")
        assertFalse(vm.formState.value.canSubmit, "whitespace is not a note")

        vm.onTextChange("real")
        assertTrue(vm.formState.value.canSubmit)
    }

    @Test
    fun addSendsTheTrimmedTextAndTheClientResourceType() = runTest {
        val vm = viewModel(clientId = 7)
        vm.onTextChange("  visited the shop  ")
        vm.submitForm()
        runCurrent()

        val sent = commands.submitted.single()
        assertEquals("visited the shop", sent.text)
        assertEquals(7L, sent.resourceId)
        assertNull(sent.noteId, "an add must not carry a note id")
        assertEquals(Constants.ENTITY_TYPE_CLIENTS, sent.resourceType)
    }

    @Test
    fun editSendsAnUpdateRatherThanAnAdd() = runTest {
        repository.emit(note(5, "original"))
        val vm = viewModel(noteId = 5)
        runCurrent()
        vm.onTextChange("corrected")
        vm.submitForm()
        runCurrent()

        // The id is what makes `submitDraft` choose update over create — on a live submit AND on
        // an outbox replay days later, which is why it travels on the payload.
        assertEquals(5L, commands.submitted.single().noteId)
    }

    @Test
    fun anOfflineSubmitPERSISTSTheNoteRatherThanLosingIt() = runTest {
        // THE regression this whole change exists for. Before it, this path showed "you are
        // offline", kept the text on screen, and lost it the moment the user navigated away.
        commands.failWith = NoteSubmitException.Offline("OFFLINE")
        val vm = viewModel(clientId = 7)
        vm.onTextChange("client was not at the shop; will revisit Thursday")
        vm.submitForm()
        runCurrent()

        val draft = outbox.entries.singleOrNull()
        assertNotNull(draft, "an offline note must survive as a draft, not only on screen")
        assertEquals(SubmitOutboxStatus.PENDING, draft.status)
        assertEquals("client was not at the shop; will revisit Thursday", draft.payload.text)
        assertEquals(NOTE_FORM_KEY, draft.formKey)
    }

    @Test
    fun concurrentDraftsDoNotOverwriteEachOther() = runTest {
        // A new note on one client and an edit of another note are different slots. Sharing a
        // unique key would mean the second offline note silently replaced the first — the failure
        // mode the outbox's uniqueKey exists to prevent.
        commands.failWith = NoteSubmitException.Offline("OFFLINE")

        viewModel(clientId = 7).apply {
            onTextChange("new note on 7")
            submitForm()
        }
        runCurrent()
        repository.emit(note(12, "existing"))
        viewModel(clientId = 7, noteId = 12).apply {
            onTextChange("edit of 12")
            submitForm()
        }
        runCurrent()

        assertEquals(2, outbox.entries.size, "two drafts collapsed into one slot")
        assertEquals(
            setOf("client:7:new", "note:12"),
            outbox.entries.mapNotNull { it.uniqueKey }.toSet(),
        )
    }

    @Test
    fun aSuccessfulSubmitLeavesNoDraftBehind() = runTest {
        val vm = viewModel()
        vm.onTextChange("sent fine")
        vm.submitForm()
        runCurrent()

        assertEquals(1, commands.submitted.size)
        assertTrue(
            outbox.entries.none { it.status == SubmitOutboxStatus.PENDING },
            "a note that reached the server must not linger as a pending draft",
        )
    }

    @Test
    fun aRetryPresentsTheSAMEIdempotencyKeyAsTheFirstAttempt() = runTest {
        // The whole point of the token. The template makes a replay idempotent by minting the
        // row's own id client-side; Fineract assigns note ids, so the equivalent is a stable
        // de-duplication key — and "stable" is the entire contract. Regenerating it per attempt
        // would make the server treat each retry as a new intent and recreate the duplicate this
        // exists to prevent.
        val vm = viewModel()
        vm.onTextChange("retry me")

        commands.failWith = NoteSubmitException.Offline("OFFLINE")
        vm.submitForm()
        runCurrent()

        commands.failWith = null
        vm.onRetry()
        runCurrent()

        val draftKey = outbox.entries.single().payload.idempotencyKey
        assertEquals(draftKey, commands.submitted.single().idempotencyKey)
        assertTrue(draftKey.isNotBlank())
    }

    @Test
    fun twoSeparateNotesGetDifferentKeys() = runTest {
        // The inverse: distinct intents must NOT share a token, or the server would dedupe a
        // genuinely new note away as a repeat of the previous one.
        commands.failWith = NoteSubmitException.Offline("OFFLINE")
        viewModel(clientId = 7).apply {
            onTextChange("first")
            submitForm()
        }
        runCurrent()
        repository.emit(note(12, "existing"))
        viewModel(clientId = 7, noteId = 12).apply {
            onTextChange("second")
            submitForm()
        }
        runCurrent()

        val keys = outbox.entries.map { it.payload.idempotencyKey }
        assertEquals(keys.size, keys.distinct().size, "two intents shared one idempotency key")
    }

    @Test
    fun aDoubleTapSendsOneWrite() = runTest {
        // Still guarded — but by the FRAMEWORK now (`SubmitHandler.submit` no-ops while
        // Submitting) rather than by a hand-rolled `submitted` latch. Kept because the guarantee
        // is what matters, not which layer provides it.
        val gate = CompletableDeferred<Unit>()
        commands.gate = gate
        val vm = viewModel()
        vm.onTextChange("once")

        vm.submitForm()
        runCurrent()
        vm.submitForm() // lands while the first is still in flight
        runCurrent()

        gate.complete(Unit)
        runCurrent()
        assertEquals(1, commands.submitted.size, "the second tap sent a second note")
    }

    @Test
    fun theFormIsReadyOnlyOnceAnExistingNoteHasHydrated() = runTest {
        // `MutationScreenContent` renders the READ gate before the form, so a screen whose
        // screenState never reaches Content sits at a spinner forever.
        repository.state.value = ScreenState.Loading
        val vm = viewModel(noteId = 5)
        runCurrent()
        repository.emit(note(5, "arrived"))
        runCurrent()

        assertEquals("arrived", vm.formState.value.text)
    }
}
