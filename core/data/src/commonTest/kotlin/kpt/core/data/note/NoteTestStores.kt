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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.base.store.infra.StoreFactory
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.data.infra.testMutationGateway
import kpt.core.data.note.impl.NoteCommandRepositoryImpl
import kpt.core.model.objects.note.Note
import kpt.core.network.mifos.note.api.NoteApi
import kpt.core.network.mifos.note.mapper.toModel
import kpt.core.store.note.NOTE_CLIENT_RESOURCE
import kpt.core.store.note.NoteItemKey
import kpt.core.store.note.NoteKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder

/**
 * The note stores wired the way `NoteStore.kt` wires them — REAL Store5 stores over one in-memory
 * table of `Note` rows that emit `Note` models — so a write through the item store is visible to the
 * list store with nothing refreshed in between.
 */
internal class NoteTestStores(api: NoteApi) {

    val table = MutableStateFlow<List<Note>>(emptyList())

    /** The owner type of each row (by note id) — the real table's `resourceType` column. */
    private val typeOf = mutableMapOf<Long, String>()

    private fun Note.ownedBy(
        key: NoteKey,
    ) = clientId == key.resourceId && (typeOf[id] ?: NOTE_CLIENT_RESOURCE) == key.resourceType

    fun rowsOf(clientId: Long, resourceType: String = NOTE_CLIENT_RESOURCE) =
        table.value.filter { it.ownedBy(NoteKey(resourceType, clientId)) }

    val notesStore = StoreBuilder.from(
        fetcher = Fetcher.of { key: NoteKey ->
            api.retrieveListNotes(key.resourceType, key.resourceId).map { it.toModel() }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: NoteKey ->
                table.map { rows -> rows.filter { it.ownedBy(key) } }
            },
            // Mirrors NoteDao.replaceFor — scoped to (resourceType, owner id).
            writer = { key: NoteKey, notes: List<Note> ->
                notes.forEach { note -> note.id?.let { typeOf[it] = key.resourceType } }
                table.value = table.value.filterNot { it.ownedBy(key) } +
                    notes.map { it.copy(clientId = key.resourceId) }
            },
        ),
    ).build()

    val itemStore = StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: NoteItemKey -> table.map { rows -> rows.firstOrNull { it.id == key.noteId } } },
            writer = { key: NoteItemKey, note: Note ->
                table.value = table.value.filterNot { it.id == key.noteId } +
                    note.copy(id = key.noteId, clientId = key.resourceId)
            },
            delete = { key: NoteItemKey -> table.value = table.value.filterNot { it.id == key.noteId } },
        ),
    )

    fun commands(api: NoteApi, gateway: MutationGateway = testMutationGateway()) =
        NoteCommandRepositoryImpl(api, gateway, notesStore, itemStore)
}
