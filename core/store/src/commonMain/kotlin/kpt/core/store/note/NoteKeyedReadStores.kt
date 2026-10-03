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
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.network.mifos.note.api.NoteApi
import kpt.core.network.mifos.note.dto.NoteDto
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for note with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

/** Addresses one `retrieveNote` read. */
data class RetrieveNoteKey(
    val resourceType: String,
    val resourceId: Long,
    val noteId: Long,
)

@StoreProvider(id = "noteRetrieveNote", ttl = "12h")
@CacheKey(fn = "forKey", key = "noteRetrieveNote:{key}", params = ["key:String"])
fun provideRetrieveNoteStore(
    noteApi: NoteApi,
    cache: ApiResponseCacheDao,
): Store<RetrieveNoteKey, NoteDto> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.NoteRetrieveNote.forKey("${key.resourceType}:${key.resourceId}:${key.noteId}") },
    fetch = { key -> noteApi.retrieveNote(resourceType = key.resourceType, resourceId = key.resourceId, noteId = key.noteId) },
)
