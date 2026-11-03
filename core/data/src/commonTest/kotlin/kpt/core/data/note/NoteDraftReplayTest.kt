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

import kotlinx.coroutines.test.runTest
import kpt.core.data.note.impl.NoteCommandRepositoryImpl
import kpt.core.model.note.NoteDraft
import kpt.core.model.objects.note.CreateNoteResponseDto
import kpt.core.model.objects.note.DeleteNoteResponseDto
import kpt.core.model.objects.note.NoteRequestDto
import kpt.core.model.objects.note.UpdateNoteResponseDto
import kpt.core.network.mifos.note.api.NoteApi
import kpt.core.network.mifos.note.dto.NoteDto
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Replaying a queued CREATE must not write a second note.
 *
 * ## Why this is a real path, not a hypothetical
 *
 * `OfflineSubmitSyncer` replays a draft whenever it sees the network return. A draft stays PENDING
 * when the client never SAW the result — the request timed out, the process died, the replay went
 * out before the auth header was installed. The server may well have applied it. `Idempotency-Key`
 * exists for exactly this and is sent, but `apis.mifos.community` IGNORES it: measured 2026-10-21
 * by pinning the key to one constant across two creates — both notes were created.
 *
 * So the protection has to be client-side, and it is in [NoteCommandRepositoryImpl.submitDraft].
 */
class NoteDraftReplayTest {

    private class FakeNoteApi(existing: List<String> = emptyList()) : NoteApi {
        var created: Int = 0
        var updated: Int = 0
        val notes = existing.mapIndexed { i, t -> NoteDto(id = i.toLong(), note = t) }.toMutableList()

        override suspend fun retrieveListNotes(resourceType: String, resourceId: Long) = notes.toList()

        override suspend fun addNewNote(
            resourceType: String,
            resourceId: Long,
            noteRequestDto: NoteRequestDto,
            idempotencyKey: String,
        ): CreateNoteResponseDto {
            created++
            notes += NoteDto(id = 99L, note = noteRequestDto.note)
            return CreateNoteResponseDto()
        }

        override suspend fun updateNote(
            resourceType: String,
            resourceId: Long,
            noteId: Long,
            noteRequestDto: NoteRequestDto,
        ): UpdateNoteResponseDto {
            updated++
            return UpdateNoteResponseDto()
        }

        override suspend fun deleteNote(resourceType: String, resourceId: Long, noteId: Long) =
            DeleteNoteResponseDto()

        override suspend fun retrieveNote(resourceType: String, resourceId: Long, noteId: Long) =
            NoteDto()
    }

    // The duplicate check reads through the notes store now, so the repo runs over real stores.
    private fun repo(api: NoteApi) = NoteTestStores(api).commands(api)

    private fun draft(text: String, noteId: Long? = null) = NoteDraft(
        resourceId = 7L,
        text = text,
        noteId = noteId,
        resourceType = "clients",
        idempotencyKey = "k-1",
    )

    @Test
    fun aFirstSubmitSendsTheNote() = runTest {
        val api = FakeNoteApi()
        repo(api).submitDraft(draft("Visited the shop"))
        assertEquals(1, api.created)
    }

    @Test
    fun aReplayOfAnAlreadyAppliedCreateDoesNotWriteASecondNote() = runTest {
        // THE REGRESSION. The first attempt reached the server; its response did not reach us, so
        // the draft is still PENDING and the syncer replays it. Without the guard this is note #2.
        val api = FakeNoteApi(existing = listOf("Visited the shop"))
        repo(api).submitDraft(draft("Visited the shop"))
        assertEquals(0, api.created)
    }

    @Test
    fun aDifferentNoteOnTheSameClientIsStillSent() = runTest {
        // The guard must not swallow genuinely new work just because the client has notes.
        val api = FakeNoteApi(existing = listOf("Visited the shop"))
        repo(api).submitDraft(draft("Collected 500"))
        assertEquals(1, api.created)
    }

    @Test
    fun anUnreadableListSendsTheNoteRatherThanDroppingIt() = runTest {
        // Conservative in the one direction that matters: a duplicate is recoverable, a dropped
        // note is not. If the pre-check cannot answer, the note goes.
        val api = object : NoteApi by FakeNoteApi() {
            var created = 0
            override suspend fun retrieveListNotes(resourceType: String, resourceId: Long): List<NoteDto> =
                error("list unreachable")

            override suspend fun addNewNote(
                resourceType: String,
                resourceId: Long,
                noteRequestDto: NoteRequestDto,
                idempotencyKey: String,
            ): CreateNoteResponseDto {
                created++
                return CreateNoteResponseDto()
            }
        }
        repo(api).submitDraft(draft("Collected 500"))
        assertEquals(1, api.created)
    }

    @Test
    fun anEditIsReplayedWithoutThePreCheck() = runTest {
        // An update NAMES the note it changes, so replaying it is already idempotent — and the
        // pre-check would be wrong here: the text IS on the server, that is the point of an edit.
        val api = FakeNoteApi(existing = listOf("Visited the shop"))
        repo(api).submitDraft(draft("Visited the shop", noteId = 0L))
        assertEquals(1, api.updated)
        assertEquals(0, api.created)
    }
}
