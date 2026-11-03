/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.note.impl

import kotlinx.coroutines.CancellationException
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.note.NoteCommandRepository
import kpt.core.data.note.NoteSubmitException
import kpt.core.model.note.NoteDraft
import kpt.core.model.objects.note.Note
import kpt.core.model.objects.note.NoteRequestDto
import kpt.core.network.mifos.note.api.NoteApi
import kpt.core.store.config.AppStoreIds
import kpt.core.store.note.NoteItemKey
import kpt.core.store.note.NoteKey
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/**
 * Note writes, through the single write door ([MutationGateway.command]).
 *
 * Network → store → database: [CommandSpec.endpoint] is the Fineract call, and
 * [CommandSpec.localApply] records the server's acceptance in the `Note` table THROUGH the note
 * stores, so the notes list re-emits by itself. Until now every command ended at the endpoint with
 * `payload = Unit` and no local side, which is why the list needed a forced re-read after each
 * write and still showed a deleted note when that re-read was missing (device, 2026-10-20).
 *
 * - **delete** is fully known from the request: the row is cleared through [itemStore].
 * - **create / update** ingest the server's record through [notesStore] (`fresh` — network → SoT
 *   writer → database). The row the list renders carries `createdOn` / `createdByUsername` (its
 *   order and attribution), which only the server assigns; neither response carries them, so a
 *   locally composed row would sort last and show no author.
 *
 * Every owner (`clients`, `groups`) shares the `Note` table, keyed (resourceType, owner id), so a
 * write is recorded locally whichever owner it is on.
 *
 * `OnlineRequired`, as before: `localApply` runs only with the server's result.
 */
@RepositoryBinding(binds = NoteCommandRepository::class)
internal class NoteCommandRepositoryImpl(
    private val noteApi: NoteApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Notes) private val notesStore: Store<NoteKey, List<Note>>,
    @FromStore(AppStoreIds.NoteItem) private val itemStore: MutableStore<NoteItemKey, Note>,
) : NoteCommandRepository {

    override suspend fun addNewNote(
        resourceType: String,
        resourceId: Long,
        noteRequestDto: NoteRequestDto,
        idempotencyKey: String,
    ) = gateway.command(
        CommandSpec(
            payload = noteRequestDto,
            endpoint = {
                noteApi.addNewNote(
                    resourceType = resourceType,
                    resourceId = resourceId,
                    noteRequestDto = it,
                    idempotencyKey = idempotencyKey,
                )
            },
            localApply = { response -> if (response != null) ingest(resourceType, resourceId) },
        ),
        policy = MutationPolicy.OnlineRequired,
    )

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteNote(resourceType: String, resourceId: Long, noteId: Long) =
        gateway.command(
            CommandSpec(
                payload = noteId,
                endpoint = { noteApi.deleteNote(resourceType = resourceType, resourceId = resourceId, noteId = it) },
                localApply = { response ->
                    if (response != null) {
                        itemStore.clear(
                            NoteItemKey(
                                resourceType = resourceType,
                                resourceId = resourceId,
                                noteId = noteId,
                            ),
                        )
                    }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    override suspend fun updateNote(
        resourceType: String,
        resourceId: Long,
        noteId: Long,
        noteRequestDto: NoteRequestDto,
    ) =
        gateway.command(
            CommandSpec(
                payload = noteRequestDto,
                endpoint = {
                    noteApi.updateNote(
                        resourceType = resourceType,
                        resourceId = resourceId,
                        noteId = noteId,
                        noteRequestDto = it,
                    )
                },
                localApply = { response -> if (response != null) ingest(resourceType, resourceId) },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    override suspend fun submitDraft(draft: NoteDraft) {
        // A REPLAY of a create may be re-sending something the server already accepted — the first
        // attempt's response simply never reached the client (timeout, process death, auth race).
        // `Idempotency-Key` is sent for this, but `apis.mifos.community` ignores it (measured
        // 2026-10-21, see NoteApi), so without a client-side check the replay writes a SECOND note.
        //
        // Only CREATES need this. An update and a delete name the note they act on, so replaying
        // either is naturally idempotent.
        if (draft.noteId == null && alreadyOnServer(draft)) return

        val dto = NoteRequestDto(note = draft.text)
        // Bound to a local: `noteId` is a nullable property of a class in another module, so the
        // compiler cannot smart-cast it inside the branch.
        val editing = draft.noteId
        val result = if (editing == null) {
            addNewNote(draft.resourceType, draft.resourceId, dto, draft.idempotencyKey)
        } else {
            updateNote(draft.resourceType, draft.resourceId, editing, dto)
        }

        // Unwrap to throw-to-fail. `OfflineSubmitSyncer` marks a draft SUBMITTED when the block
        // RETURNS, so swallowing a Blocked here would delete a note that never reached Fineract —
        // the exact data loss this draft exists to prevent.
        when (result) {
            is MutationResult.Applied -> Unit
            is MutationResult.Blocked -> throw NoteSubmitException.Offline(result.reason.toString())
            is MutationResult.Failed -> throw NoteSubmitException.Rejected(result.cause)
            is MutationResult.Conflicted ->
                throw NoteSubmitException.Rejected(IllegalStateException("conflict ${'$'}{result.conflictId}"))
        }
    }

    /**
     * Has this exact draft already landed?
     *
     * Read through the notes store (`fresh`), not `NoteApi` directly: the server's list is written
     * to the `Note` table on the way, so a replay that turns out to be a duplicate still leaves the
     * note it duplicates on screen.
     *
     * Matches on TEXT within the client, which is the only identity a create has before the server
     * assigns one. Deliberately conservative in the one direction that matters: on any doubt — the
     * read fails or the list is unreachable — it returns false and
     * the note is sent. A duplicate note is recoverable by deleting one; a silently dropped note is
     * not, and dropping it would defeat the outbox this whole path exists to serve.
     *
     * The narrow false-positive is an officer deliberately writing the SAME text twice on one
     * client while the first is still queued. Judged acceptable against losing work: notes are
     * free-text observations, and an exact repeat of a queued one is far rarer than a replay.
     */
    private suspend fun alreadyOnServer(draft: NoteDraft): Boolean {
        return orFalse {
            notesStore.fresh(
                NoteKey(
                    draft.resourceType,
                    draft.resourceId,
                ),
            ).any { it.noteContent?.trim() == draft.text.trim() }
        }
    }

    /**
     * Record an accepted create/update by reading the server's list through [notesStore] into the
     * `Note` table.
     *
     * A failure here is swallowed, not thrown: the server HAS the write, and a throw would turn it
     * into `Failed` — the officer (or the outbox) would then send it again and write a duplicate.
     * The row arrives with the list's next read instead.
     */
    private suspend fun ingest(resourceType: String, resourceId: Long) {
        orFalse {
            notesStore.fresh(NoteKey(resourceType, resourceId))
            true
        }
    }

    private inline fun orFalse(block: () -> Boolean): Boolean = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        false
    }
}
