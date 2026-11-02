/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.note.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
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

    @Query("SELECT * FROM Note WHERE resourceType = :resourceType AND clientId = :resourceId ORDER BY createdOn DESC")
    fun getNotesFor(resourceType: String, resourceId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM Note WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<NoteEntity?>

    @Query("DELETE FROM Note WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM Note WHERE resourceType = :resourceType AND clientId = :resourceId")
    suspend fun deleteNotesFor(resourceType: String, resourceId: Long)

    @Query("DELETE FROM Note")
    suspend fun deleteAll()

    /**
     * Make the client's rows EXACTLY the server's list. The store's writer used [insertNotes]
     * alone, so a note deleted on the server (or on another device) stayed in the table forever.
     */
    @Transaction
    suspend fun replaceFor(resourceType: String, resourceId: Long, notes: List<NoteEntity>) {
        deleteNotesFor(resourceType, resourceId)
        insertNotes(notes)
    }
}
