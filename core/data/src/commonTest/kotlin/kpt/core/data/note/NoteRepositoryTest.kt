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
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.infra.testMutationGateway
import kpt.core.model.note.NoteDraft
import kpt.core.model.objects.note.CreateNoteResponseDto
import kpt.core.model.objects.note.DeleteNoteResponseDto
import kpt.core.model.objects.note.NoteRequestDto
import kpt.core.model.objects.note.UpdateNoteResponseDto
import kpt.core.network.mifos.note.api.NoteApi
import kpt.core.network.mifos.note.dto.NoteDto
import kpt.core.store.note.NOTE_CLIENT_RESOURCE
import kpt.core.store.note.NoteKey
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Note writes land in the `Note` table the list store observes — through the stores, with nothing
 * refreshed afterwards. Before, every command was `payload = Unit` with no local side and the list
 * screen forced a re-read to hide a deleted note (device, 2026-10-20).
 */
class NoteRepositoryTest {

    /** Fineract: assigns the id and the attribution (`createdOn` / `createdByUsername`). */
    private val server = mutableListOf(NoteDto(id = 1, clientId = 7, note = "Visited", createdByUsername = "mifos"))
    private var nextId = 10L
    private var listFetches = 0

    /** Each note's owner type on the server (note 1 is a client note). */
    private val serverType = mutableMapOf(1L to "clients")

    private val api = object : NoteApi {
        override suspend fun retrieveListNotes(resourceType: String, resourceId: Long): List<NoteDto> {
            listFetches++
            return server.filter { it.clientId == resourceId && serverType[it.id] == resourceType }
        }

        override suspend fun addNewNote(
            resourceType: String,
            resourceId: Long,
            noteRequestDto: NoteRequestDto,
            idempotencyKey: String,
        ): CreateNoteResponseDto {
            val id = nextId++
            serverType[id] = resourceType
            server += NoteDto(
                id = id,
                clientId = resourceId,
                note = noteRequestDto.note,
                createdByUsername = "mifos",
                createdOn = "2026-10-23",
            )
            return CreateNoteResponseDto(clientId = resourceId, resourceId = id)
        }

        override suspend fun updateNote(
            resourceType: String,
            resourceId: Long,
            noteId: Long,
            noteRequestDto: NoteRequestDto,
        ): UpdateNoteResponseDto {
            val i = server.indexOfFirst { it.id == noteId }
            server[i] = server[i].copy(note = noteRequestDto.note, updatedByUsername = "mifos")
            return UpdateNoteResponseDto(resourceId = noteId)
        }

        override suspend fun deleteNote(resourceType: String, resourceId: Long, noteId: Long): DeleteNoteResponseDto {
            server.removeAll { it.id == noteId }
            return DeleteNoteResponseDto(resourceId = noteId)
        }

        override suspend fun retrieveNote(resourceType: String, resourceId: Long, noteId: Long) =
            error("not used")
    }

    private val stores = NoteTestStores(api)
    private val commands = stores.commands(api)

    @Test
    fun theListReadReplacesTheClientsRows() = runTest {
        stores.notesStore.fresh(NoteKey(NOTE_CLIENT_RESOURCE, 7L))
        server.clear()

        stores.notesStore.fresh(NoteKey(NOTE_CLIENT_RESOURCE, 7L))

        // The old insert-only writer kept a note deleted on the server in the table forever.
        assertEquals(emptyList(), stores.rowsOf(7L))
    }

    @Test
    fun aCreateLandsWithTheServersAttribution() = runTest {
        val result = commands.addNewNote("clients", 7L, NoteRequestDto(note = "Collected 500"), "k-1")

        assertIs<MutationResult.Applied<CreateNoteResponseDto>>(result)
        val created = stores.rowsOf(7L).single { it.id == 10L }
        assertEquals("Collected 500", created.noteContent)
        // Server-assigned — why create ingests the record rather than composing a row locally.
        assertEquals("mifos", created.createdByUsername)
        assertEquals("2026-10-23", created.createdOn)
    }

    @Test
    fun anUpdateLandsInTheTable() = runTest {
        stores.notesStore.fresh(NoteKey(NOTE_CLIENT_RESOURCE, 7L))

        commands.updateNote("clients", 7L, 1L, NoteRequestDto(note = "Visited twice"))

        assertEquals("Visited twice", stores.rowsOf(7L).single { it.id == 1L }.noteContent)
    }

    @Test
    fun aDeleteRemovesTheRowWithoutARefetch() = runTest {
        stores.notesStore.fresh(NoteKey(NOTE_CLIENT_RESOURCE, 7L))
        val fetchesBefore = listFetches

        commands.deleteNote("clients", 7L, 1L)

        assertTrue(stores.rowsOf(7L).none { it.id == 1L })
        assertEquals(fetchesBefore, listFetches)
    }

    @Test
    fun anOfflineWriteIsBlockedAndWritesNothing() = runTest {
        val offline = stores.commands(api, testMutationGateway(isOnline = false))

        assertIs<MutationResult.Blocked>(offline.addNewNote("clients", 7L, NoteRequestDto(note = "x"), "k-1"))
        assertEquals(emptyList(), stores.table.value)
        assertEquals(1, server.size)
    }

    @Test
    fun aReplayThatProvesADuplicateStillShowsTheNote() = runTest {
        // The first attempt reached the server unseen; the replay is skipped — and the duplicate
        // check, reading through the store, has put the accepted note in the table on the way.
        val draft = NoteDraft(
            resourceId = 7L,
            text = "Visited",
            noteId = null,
            resourceType = "clients",
            idempotencyKey = "k-1",
        )

        commands.submitDraft(draft)

        assertEquals(1, server.size)
        assertEquals(listOf("Visited"), stores.rowsOf(7L).map { it.noteContent })
    }

    @Test
    fun aGroupNoteIsRecordedUnderTheGroupNotTheClientWithTheSameId() = runTest {
        // Every owner shares the Note table, keyed (resourceType, owner id): group 7 is not client 7.
        commands.addNewNote("groups", 7L, NoteRequestDto(note = "Group met"), "k-1")

        assertEquals(2, server.size)
        assertEquals(listOf("Group met"), stores.rowsOf(7L, resourceType = "groups").map { it.noteContent })
        assertTrue(stores.rowsOf(7L).none { it.noteContent == "Group met" }, "not in client 7's notes")
    }
}
