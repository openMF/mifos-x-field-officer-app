/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.note.NoteCommandRepository
import kpt.core.data.note.NoteRepository
import kpt.core.model.note.NoteDraft
import kpt.core.model.objects.note.CreateNoteResponseDto
import kpt.core.model.objects.note.DeleteNoteResponseDto
import kpt.core.model.objects.note.Note
import kpt.core.model.objects.note.NoteRequestDto
import kpt.core.model.objects.note.UpdateNoteResponseDto

/**
 * In-memory [NoteRepository] for ViewModel- and screen-level tests.
 *
 * Hands back REAL [ScreenDataStream]s via the sanctioned `screenDataStreamForTesting` factory
 * rather than a parallel fake of the state machine — so a test exercises the same Loading /
 * Content / Empty / Error transitions production does.
 */
@ExperimentalDataTestingApi
class FakeNoteRepository(
    initial: ScreenState<List<Note>> = ScreenState.Loading,
) : NoteRepository {

    /** What the notes stream emits; set it to drive the screen. */
    val state = MutableStateFlow(initial)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    /**
     * The stream's force-fresh channel, exposed so a test can assert whether the screen re-read
     * after a write.
     *
     * A write now lands in the table the read observes, so the ViewModel must NOT force one —
     * asserted as no emission here.
     */
    val forceFreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    /** Every resourceId the ViewModel asked for — the read key is worth asserting. */
    val requestedIds = mutableListOf<Long>()

    /** Convenience: put the stream into Content with these rows. */
    fun emit(vararg notes: Note) {
        // An empty list is Content, as the real stream emits it (see ChargeRepositoryImpl).
        state.value = ScreenState.Content(notes.toList())
    }

    /** Every resourceType the ViewModel asked for, in order. */
    val requestedTypes = mutableListOf<String>()

    override fun notesStream(
        resourceType: String,
        resourceId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Note>> {
        requestedTypes += resourceType
        requestedIds += resourceId
        return screenDataStreamForTesting(
            state,
            refreshTrigger = refreshes,
            forceFreshTrigger = forceFreshes,
        )
    }
}

/**
 * In-memory [NoteCommandRepository] that RECORDS every write and replays a scripted outcome.
 *
 * Recording the `resourceType` matters: it is no longer a route parameter (see `NoteNavigation`)
 * but it still has to reach the wire, and nothing else would catch it going missing.
 */
@ExperimentalDataTestingApi
class FakeNoteCommandRepository(
    private val outcome: () -> MutationResult<Unit> = { MutationResult.Applied(Unit, synced = true) },
) : NoteCommandRepository {

    /** Notes added, in order. */
    val added = mutableListOf<Written>()

    /** Notes updated, in order. */
    val updated = mutableListOf<Written>()

    /** Notes deleted, in order. */
    val deleted = mutableListOf<Written>()

    /**
     * One recorded write.
     *
     * @property resourceType the owner's resource type (`clients`, `groups`)
     * @property resourceId the owner's id
     * @property noteId the note's id; null for an add
     * @property text the note text; null for a delete
     */
    data class Written(
        val resourceType: String,
        val resourceId: Long,
        val noteId: Long? = null,
        val text: String? = null,
    )

    /** Idempotency keys seen by create, in order — a retry must present the SAME one. */
    val createKeys = mutableListOf<String>()

    override suspend fun addNewNote(
        resourceType: String,
        resourceId: Long,
        noteRequestDto: NoteRequestDto,
        idempotencyKey: String,
    ): MutationResult<CreateNoteResponseDto> {
        added += Written(resourceType, resourceId, text = noteRequestDto.note)
        createKeys += idempotencyKey
        return outcome().map { CreateNoteResponseDto() }
    }

    override suspend fun updateNote(
        resourceType: String,
        resourceId: Long,
        noteId: Long,
        noteRequestDto: NoteRequestDto,
    ): MutationResult<UpdateNoteResponseDto> {
        updated += Written(resourceType, resourceId, noteId, noteRequestDto.note)
        return outcome().map { UpdateNoteResponseDto() }
    }

    override suspend fun deleteNote(
        resourceType: String,
        resourceId: Long,
        noteId: Long,
    ): MutationResult<DeleteNoteResponseDto> {
        deleted += Written(resourceType, resourceId, noteId)
        return outcome().map { DeleteNoteResponseDto() }
    }

    /** Drafts actually sent, in order — the outbox's and the syncer's shared view. */
    val submitted = mutableListOf<NoteDraft>()

    /**
     * Set to make the next [submitDraft] THROW.
     *
     * `submitDraft` throws to fail by design (the framework's submit machinery treats a returning
     * block as success), so a fake that returned an error value would not exercise the path that
     * matters.
     */
    var failWith: Throwable? = null

    /**
     * When set, awaited BEFORE the submit completes — holding the mutation IN FLIGHT.
     *
     * Needed because the framework's double-submit guard is "no-op while Submitting". Under
     * `Dispatchers.Unconfined` the first submit finishes before the second call is even made, so
     * without a gate the guard is never exercised and the test would pass against a ViewModel with
     * no protection at all.
     */
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun submitDraft(draft: NoteDraft) {
        failWith?.let { throw it }
        gate?.await()
        submitted += draft
    }
}

/**
 * Re-type a scripted outcome onto the DTO the contract demands.
 *
 * The scripted `outcome` is written once per test as `MutationResult<Unit>` because the DTO is
 * never what a test is asserting — the OUTCOME CLASS is. This keeps each fake method from
 * re-stating the same four-branch `when`.
 */
private fun <T> MutationResult<Unit>.map(value: () -> T): MutationResult<T> = when (this) {
    is MutationResult.Applied -> MutationResult.Applied(value(), synced)
    is MutationResult.Blocked -> MutationResult.Blocked(reason)
    is MutationResult.Failed -> MutationResult.Failed(cause, rolledBack)
    is MutationResult.Conflicted -> MutationResult.Conflicted(conflictId, value())
}
