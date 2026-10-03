/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database.infra.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kpt.core.base.database.infra.entity.BookkeeperEntity

/**
 * DAO for [BookkeeperEntity]. Provides persistent sync-failure tracking
 * for [org.mobilenativefoundation.store.store5.Bookkeeper] implementations.
 */
@Dao
interface BookkeeperDao {

    @Query("SELECT lastFailedSync FROM store_bookkeeper WHERE `key` = :key")
    suspend fun getLastFailedSync(key: String): Long?

    @Upsert
    suspend fun upsert(entity: BookkeeperEntity)

    @Query("DELETE FROM store_bookkeeper WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM store_bookkeeper")
    suspend fun deleteAll()

    /**
     * Every key with a recorded sync failure, oldest failure first.
     *
     * A [org.mobilenativefoundation.store.store5.Bookkeeper] can only answer "did THIS key
     * fail?" — `getLastFailedSync(key)` needs the key you are already holding. That is enough
     * to gate a re-read, but it makes the recorded failures undrainable: nothing can enumerate
     * what is outstanding, so a write that failed while offline stays recorded forever and is
     * never retried. This query is the enumeration a sync orchestrator needs to drain the
     * backlog when connectivity returns.
     */
    @Query("SELECT `key` FROM store_bookkeeper ORDER BY lastFailedSync ASC")
    suspend fun pendingKeys(): List<String>
}
