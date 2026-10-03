/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.note.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.note.entity.NoteEntity

/**
 * Notes attached to a client (or other Fineract resource).
 *
 * `NoteEntity` shipped without a DAO, so notes had no read path out of Room and the feature was
 * network-only. Scoped by `clientId` because that is how the wire endpoint is scoped
 * (`{resourceType}/{resourceId}/notes`) — a global read would mix one client's notes into another's.
 */
@DbDao
@Dao
interface NoteDao {

    @Insert(entity = NoteEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Query("SELECT * FROM Note WHERE clientId = :clientId ORDER BY createdOn DESC")
    fun getNotesForClient(clientId: Long): Flow<List<NoteEntity>>

    @Query("DELETE FROM Note WHERE clientId = :clientId")
    suspend fun deleteNotesForClient(clientId: Long)
}
