/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.document.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.document.entity.DocumentEntity

@DbDao
@Dao
interface DocumentDao {

    /** Observe the documents attached to one resource — the scoping every read uses. */
    @Query(
        "SELECT * FROM documents WHERE parentEntityType = :entityType AND parentEntityId = :entityId ORDER BY name ASC",
    )
    fun observeForEntity(entityType: String, entityId: Int): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): DocumentEntity?

    @Upsert
    suspend fun upsertAll(documents: List<DocumentEntity>)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM documents WHERE parentEntityType = :entityType AND parentEntityId = :entityId")
    suspend fun deleteForEntity(entityType: String, entityId: Int)

    @Query("DELETE FROM documents")
    suspend fun deleteAll()
}
