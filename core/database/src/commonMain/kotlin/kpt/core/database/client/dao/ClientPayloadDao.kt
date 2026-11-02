/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.dao

import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import kpt.core.database.client.entity.ClientPayloadEntity
import kpt.core.database.datatable.entity.DataTablePayload

/** Offline client payload queue. */
interface ClientPayloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientPayloadEntity::class)
    suspend fun insertClientPayload(clientPayload: ClientPayloadEntity)

    @Insert(entity = DataTablePayload::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataTablePayload(dataTablePayloads: DataTablePayload)

    @Query("SELECT * FROM ClientPayload")
    fun getAllClientPayload(): Flow<List<ClientPayloadEntity>>

    @Query("SELECT * FROM DataTablePayload WHERE clientCreationTime = :clientCreationTime")
    fun getDataTablePayloadByCreationTime(clientCreationTime: Long): Flow<List<DataTablePayload>>

    @Query("DELETE FROM ClientPayload WHERE id = :id")
    suspend fun deleteClientPayloadById(id: Int)

    @Query("DELETE FROM DataTablePayload WHERE clientCreationTime = :clientCreationTime")
    suspend fun deleteDataTablePayloadByCreationTime(clientCreationTime: Long)

    @Update(entity = ClientPayloadEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateDatabaseClientPayload(clientPayload: ClientPayloadEntity)

    /**
     * Drop an offline client payload together with the datatable rows queued alongside it.
     * Was `ClientDaoHelper.deleteAndUpdatePayloads` — now atomic, so a half-deleted payload cannot
     * be replayed on the next sync.
     */
    @Transaction
    suspend fun deleteClientPayloadWithData(id: Int, clientCreationTime: Long) {
        deleteClientPayloadById(id)
        deleteDataTablePayloadByCreationTime(clientCreationTime)
    }

    /** One queued offline client, for the write store's source of truth. */
    @Query("SELECT * FROM ClientPayload WHERE id = :id LIMIT 1")
    fun observeClientPayload(id: Int): Flow<ClientPayloadEntity?>

    /** The logout purge for the offline client queue — a capture must not outlive its officer. */
    @Query("DELETE FROM ClientPayload")
    suspend fun deleteAllClientPayloads()
}
