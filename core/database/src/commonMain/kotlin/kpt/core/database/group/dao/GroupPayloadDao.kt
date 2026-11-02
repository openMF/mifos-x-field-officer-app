/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.group.dao

import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import kpt.core.database.group.entity.GroupPayloadEntity

/** Offline group payload queue. */
interface GroupPayloadDao {
    @Insert(entity = GroupPayloadEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupPayload(groupPayload: GroupPayloadEntity)

    @Update(entity = GroupPayloadEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateGroupPayload(payload: GroupPayloadEntity)

    @Query("DELETE FROM GroupPayload where id = :groupId")
    suspend fun deleteGroupPayloadById(groupId: Int)

    @Query("SELECT * FROM GroupPayload")
    fun getAllGroupPayloads(): Flow<List<GroupPayloadEntity>>

    /** One queued offline group, for the write store's source of truth. */
    @Query("SELECT * FROM GroupPayload WHERE id = :id LIMIT 1")
    fun observeGroupPayload(id: Int): Flow<GroupPayloadEntity?>

    /** The logout purge for the offline group queue. */
    @Query("DELETE FROM GroupPayload")
    suspend fun deleteAllGroupPayloads()
}
