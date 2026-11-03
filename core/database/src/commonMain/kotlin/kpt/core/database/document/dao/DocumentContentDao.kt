/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.document.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.document.entity.DocumentContentEntity

/** Room queries for document content rows. */
@DbDao
@Dao
interface DocumentContentDao {

    /** Observes the cached content of one document. */
    @Query(
        "SELECT * FROM document_contents WHERE entityType = :entityType AND entityId = :entityId " +
            "AND documentId = :documentId LIMIT 1",
    )
    fun observe(entityType: String, entityId: Int, documentId: Int): Flow<DocumentContentEntity?>

    /** Inserts `content`, or replaces the row with the same key. */
    @Upsert
    suspend fun upsert(content: DocumentContentEntity)

    /** Deletes the cached content of one document. */
    @Query(
        "DELETE FROM document_contents WHERE entityType = :entityType AND entityId = :entityId " +
            "AND documentId = :documentId",
    )
    suspend fun delete(entityType: String, entityId: Int, documentId: Int)

    /** Deletes every `document_contents` row. */
    @Query("DELETE FROM document_contents")
    suspend fun deleteAll()
}
