/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.note

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.note.dao.NoteDao
import kpt.core.database.note.entity.NoteEntity
import kpt.core.network.mifos.note.api.NoteApi
import kpt.core.network.mifos.note.mapper.toEntity
import org.mobilenativefoundation.store.store5.Fetcher
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
@StoreProvider(id = "clientNotes", ttl = "15m")
@CacheKey(fn = "forClient", key = "clientNotes:{clientId}", params = ["clientId:Long"])
fun provideNoteStore(
    service: NoteApi,
    dao: NoteDao,
): Store<Long, List<NoteEntity>> = StoreFactory.createStore(
    fetcher = Fetcher.of { clientId: Long ->
        service.retrieveListNotes(resourceType = CLIENT_RESOURCE, resourceId = clientId)
            .map { it.toEntity() }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { clientId: Long -> dao.getNotesForClient(clientId) },
        writer = { _: Long, notes: List<NoteEntity> -> dao.insertNotes(notes) },
        delete = { clientId: Long -> dao.deleteNotesForClient(clientId) },
    ),
)

private const val CLIENT_RESOURCE = "clients"
