/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.note

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.note.dao.NoteDao
import kpt.core.model.objects.note.Note
import kpt.core.network.mifos.note.api.NoteApi
import kpt.core.network.mifos.note.mapper.toModel
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Client notes — READ-CACHE (`createStore`), keyed by clientId.
 *
 * Notes are what a field officer reads to recall a client's history mid-visit, which is exactly
 * when connectivity is least likely — so this is a case where the cache is the point, not an
 * optimisation. The DTO→entity map lives in the SourceOfTruth writer per the template's read-path
 * contract, so the store emits the Room type the UI observes.
 */
/** One owner's notes — Fineract's `{resourceType}/{resourceId}/notes`. */
data class NoteKey(val resourceType: String, val resourceId: Long)

@StoreProvider(id = "notes", ttl = "15m")
@CacheKey(
    fn = "forOwner",
    key = "notes:{resourceType}:{resourceId}",
    params = ["resourceType:String", "resourceId:Long"],
)
fun provideNoteStore(
    service: NoteApi,
    dao: NoteDao,
): Store<NoteKey, List<Note>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: NoteKey ->
        service.retrieveListNotes(resourceType = key.resourceType, resourceId = key.resourceId)
            .map { it.toModel() }
    },
    sourceOfTruth = SourceOfTruth.of(
        // `[]` for no rows, NOT DocumentStore's null-for-empty: Store5 emits NOTHING when a reader
        // goes from rows to null (measured 2026-10-23), so deleting an owner's last note through
        // `noteItem` would leave it on screen. The cost is "no notes" shown for a never-loaded
        // owner while offline.
        reader = { key: NoteKey ->
            dao.getNotesFor(key.resourceType, key.resourceId).map { rows -> rows.map { it.toModel() } }
        },
        // REPLACE, not insert: a fetch is the server's whole list for the owner, so a row it no
        // longer contains was deleted and must leave the table. The owner is stamped onto each row
        // because it is what the reader filters on.
        writer = { key: NoteKey, notes: List<Note> ->
            dao.replaceFor(
                key.resourceType,
                key.resourceId,
                notes.map { it.toEntity().copy(clientId = key.resourceId, resourceType = key.resourceType) },
            )
        },
        delete = { key: NoteKey -> dao.deleteNotesFor(key.resourceType, key.resourceId) },
        // What the logout purge (`Store.clear()`) calls. Without it the purge cleared the in-memory
        // cache and left every owner's notes for the next officer.
        deleteAll = { dao.deleteAll() },
    ),
)

/** One note ROW — the owner it hangs off plus the note's id. */
data class NoteItemKey(val resourceType: String, val resourceId: Long, val noteId: Long)

/**
 * The WRITE half. Once the server accepts a delete, `NoteCommandRepository` records it by clearing
 * the row THROUGH this store — never a DAO call in `core/data` (S5-1). It shares the `Note` table
 * with [provideNoteStore], so the list re-emits with no refresh.
 *
 * Local-only (`createOfflineMutableStore`): the network leg already ran in the command.
 * `logout = false`: the rows are purged by the paired read store's `deleteAll`.
 */
// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "noteItem", logout = false)
@CacheKey(
    fn = "item",
    key = "noteItem:{resourceType}:{resourceId}:{noteId}",
    params = ["resourceType:String", "resourceId:Long", "noteId:Long"],
)
fun provideNoteItemStore(
    dao: NoteDao,
): MutableStore<NoteItemKey, Note> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: NoteItemKey -> dao.observeById(key.noteId).map { it?.toModel() } },
        writer = { key: NoteItemKey, note: Note ->
            val entity = note.toEntity()
                .copy(id = key.noteId, clientId = key.resourceId, resourceType = key.resourceType)
            dao.insertNotes(listOf(entity))
        },
        delete = { key: NoteItemKey -> dao.deleteById(key.noteId) },
    ),
)

/** Fineract's owner path for client notes — the default, and every caller before groups. */
const val NOTE_CLIENT_RESOURCE: String = "clients"

/** Fineract's owner path for group notes (the group detail's Notes entry). */
const val NOTE_GROUP_RESOURCE: String = "groups"
