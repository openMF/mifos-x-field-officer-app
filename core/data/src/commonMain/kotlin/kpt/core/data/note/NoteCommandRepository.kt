/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.note

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.note.NoteDraft
import kpt.core.model.objects.note.CreateNoteResponseDto
import kpt.core.model.objects.note.DeleteNoteResponseDto
import kpt.core.model.objects.note.NoteRequestDto
import kpt.core.model.objects.note.UpdateNoteResponseDto

/**
 * Every note write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * Writes go the same way reads do — network → store → database: the command reaches Fineract, and
 * the server's acceptance is recorded through the note stores, so every note stream already shows it
 * when the call returns. Offline CAPTURE is a different operation with its own explicit queue
 * ([kpt.core.data.sync.OfflineQueueRepository]); it is never a silent fallback from here.
 */
interface NoteCommandRepository {

    suspend fun addNewNote(
        resourceType: String,
        resourceId: Long,
        noteRequestDto: NoteRequestDto,
        idempotencyKey: String,
    ): MutationResult<CreateNoteResponseDto>

    suspend fun deleteNote(resourceType: String, resourceId: Long, noteId: Long): MutationResult<DeleteNoteResponseDto>

    suspend fun updateNote(
        resourceType: String,
        resourceId: Long,
        noteId: Long,
        noteRequestDto: NoteRequestDto,
    ): MutationResult<UpdateNoteResponseDto>

    /**
     * Send one [NoteDraft] — create or update, decided by `noteId`.
     *
     * The single entry point the OUTBOX replays against. `OfflineSubmitSyncer` hands back a payload
     * it persisted days ago with no idea which of the two calls produced it, so the choice has to
     * live here, derived from the draft itself.
     *
     * THROWS on failure rather than returning [MutationResult]. That is deliberate and is the one
     * place in this layer that does: `SubmitHandler`/`OfflineSubmitSyncer` are built on
     * throw-to-fail, and a `MutationResult.Blocked` quietly returned to the syncer would be read as
     * success and the draft marked SUBMITTED — losing the note. See [NoteSubmitException].
     */
    suspend fun submitDraft(draft: NoteDraft)
}
