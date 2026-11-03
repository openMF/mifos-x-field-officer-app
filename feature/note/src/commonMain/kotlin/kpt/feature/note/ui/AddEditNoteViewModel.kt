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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.IdempotencyKey
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.base.ui.viewmodel.BaseMutationViewModel
import kpt.core.base.ui.viewmodel.MutationMode
import kpt.core.common.utils.Constants
import kpt.core.data.note.NOTE_FORM_KEY
import kpt.core.data.note.NoteCommandRepository
import kpt.core.data.note.NoteRepository
import kpt.core.model.note.NoteDraft

/**
 * Write or edit one note — the app's first OFFLINE-DURABLE form.
 *
 * Ported from `6b66e8a43:feature/note/addEditNotes/AddEditNoteViewModel.kt`, then re-based onto
 * [BaseMutationViewModel] with [MutationMode.Draft].
 *
 * ## What changed, and why it matters here more than anywhere else
 *
 * The first migration hand-rolled the submit: `viewModelScope.launch`, an exhaustive
 * `when (MutationResult)`, and three bespoke flags (`isSubmitting` / `submitted` / `writeError`).
 * It worked, and it lost data. On `Blocked(OFFLINE)` it showed "you are offline" and kept the text
 * ON SCREEN — navigate away, or let the process die, and a note typed in a village with no signal
 * was gone.
 *
 * `MutationMode.Draft` persists the payload to `framework_submit_drafts` the moment the network
 * refuses it, and `OfflineSubmitSyncer` (registered eagerly in `NoteDataProviders`) replays it on
 * the next reconnect — with no screen open. The draft is visible and actionable meanwhile in
 * **Settings → Sync & Drafts**, which already renders PENDING / RETRYING / FAILED and was waiting
 * for a producer.
 *
 * `OnlineRequired` on the note write is UNCHANGED and still right: it stops the app claiming a note
 * reached Fineract when it did not. It never implied throwing the officer's typing away.
 *
 * ## Keys
 *
 * `formKey = "note"` groups every note draft for the outbox; `uniqueKey` partitions inside it so
 * concurrent drafts coexist — a new note on client 7 and an edit of note 12 are different slots and
 * neither overwrites the other. `autoSaveDraft = true` because the officer has already decided to
 * save by tapping Submit; prompting again after a failure they did not cause is noise.
 */
internal class AddEditNoteViewModel(
    private val repository: NoteRepository,
    private val commands: NoteCommandRepository,
    outbox: SubmitOutbox<NoteDraft>,
    savedStateHandle: SavedStateHandle,
) : BaseMutationViewModel<NoteDraft, Unit>(
    MutationMode.Draft(
        outbox = outbox,
        formKey = NOTE_FORM_KEY,
        uniqueKey = savedStateHandle.uniqueKey(),
        autoSaveDraft = true,
    ),
) {

    private val resourceType: String = savedStateHandle.get<String>(RESOURCE_TYPE_KEY) ?: Constants.ENTITY_TYPE_CLIENTS
    private val resourceId: Int = savedStateHandle.get<Int>(RESOURCE_ID_KEY) ?: 0

    /** `null` means ADD. Read once; the original branched on it in four separate places. */
    private val noteId: Long? = savedStateHandle.get<Long>(NOTE_ID_KEY)

    /**
     * Minted ONCE, when this editor opens — not per submit and never per retry.
     *
     * Regenerating it on retry would defeat the whole mechanism: the server would see each attempt
     * as a new intent and the duplicate it exists to prevent would reappear. Held here (not in
     * `draftPayload()`) so every payload this screen produces — first submit, retry, and the copy
     * persisted to the outbox — carries the same token.
     */
    private val idempotencyKey: String = IdempotencyKey.generate()

    private val _formState = MutableStateFlow(NoteFormState(editEnabled = noteId != null))

    /** The form as it currently stands — the editable body the screen renders. */
    val formState: StateFlow<NoteFormState> = _formState.asStateFlow()

    init {
        // `mutableScreenState` gates "form ready", not display data: Loading while an existing note
        // hydrates, then Content. Without it `MutationScreenContent` sits at Loading forever,
        // because it renders the READ gate before the form.
        if (noteId == null) {
            mutableScreenState.value = ScreenState.Content(draftPayload())
        } else {
            loadExistingNote(noteId)
        }
    }

    /**
     * Seed the field from the note already in the Store.
     *
     * `first { it !is Loading }` waits for the stream to SETTLE rather than sampling it — opening
     * the editor directly (deep link, process death) must not show an empty field just because the
     * cache had not emitted yet.
     */
    private fun loadExistingNote(id: Long) {
        viewModelScope.launch {
            runCatching {
                repository.notesStream(resourceType, resourceId.toLong(), viewModelScope).state
                    .first { it !is ScreenState.Loading }
            }.onSuccess { screenState ->
                val existing = (screenState as? ScreenState.Content)
                    ?.data
                    ?.firstOrNull { it.id == id }
                    ?.noteContent
                    .orEmpty()
                _formState.update { it.copy(text = existing, loadedText = existing) }
            }.onFailure {
                Logger.e(tag = TAG, throwable = it) { "could not seed note $id" }
            }
            mutableScreenState.value = ScreenState.Content(draftPayload())
        }
    }

    /**
     * The one network call, reached by BOTH paths — a live submit and an outbox replay days later.
     *
     * It THROWS on failure (see `NoteCommandRepository.submitDraft`): the framework's submit
     * machinery treats a returning block as success, so a swallowed `Blocked` would mark the draft
     * SUBMITTED and delete a note that never left the device.
     */
    override suspend fun performSubmit(payload: NoteDraft) = commands.submitDraft(payload)

    fun onTextChange(value: String) = _formState.update { it.copy(text = value) }

    /** Submit the form. No-op while already submitting — the framework guards it. */
    fun submitForm() = onSubmit(draftPayload())

    fun onDiscardRequested() = _formState.update { it.copy(showDiscardConfirmation = true) }

    fun onDiscardDismissed() = _formState.update { it.copy(showDiscardConfirmation = false) }

    private fun draftPayload() = NoteDraft(
        resourceId = resourceId.toLong(),
        text = _formState.value.text.trim(),
        noteId = noteId,
        // The type travels ON the draft so a replay does not have to re-derive it from UI context.
        resourceType = resourceType,
        idempotencyKey = idempotencyKey,
    )

    private companion object {
        const val RESOURCE_TYPE_KEY = "resourceType"
        const val RESOURCE_ID_KEY = "resourceId"
        const val NOTE_ID_KEY = "noteId"
        const val TAG = "AddEditNoteViewModel"
    }
}

/**
 * The draft's slot in the outbox.
 *
 * Read from the handle rather than from the ViewModel's own fields because
 * [BaseMutationViewModel]'s constructor runs FIRST — the mode is a super-constructor argument, so
 * `resourceId`/`noteId` do not exist yet.
 *
 * An edit keys on the note; a new note keys on its owner. So "new note on client 7" and "edit of
 * note 12" are separate slots, and two officers' worth of offline work never collapses into one.
 * A client keeps the pre-v18 `client:<id>:new` form, so a note queued before the change still finds
 * its slot; other owners are `<resourceType>:<id>:new`.
 */
private fun SavedStateHandle.uniqueKey(): String {
    val type = get<String>("resourceType") ?: Constants.ENTITY_TYPE_CLIENTS
    val owner = get<Int>("resourceId") ?: 0
    return get<Long>("noteId")?.let { "note:$it" }
        ?: if (type == Constants.ENTITY_TYPE_CLIENTS) "client:$owner:new" else "$type:$owner:new"
}

/** The editable body. Submit lifecycle lives in [BaseMutationViewModel]'s `uiState`, not here. */
internal data class NoteFormState(
    val text: String = "",
    val loadedText: String = "",
    val editEnabled: Boolean = false,
    val showDiscardConfirmation: Boolean = false,
) {
    val isDirty: Boolean get() = text.trim() != loadedText.trim()
    val canSubmit: Boolean get() = text.isNotBlank()
}
