/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.cache.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.cache.entity.ApiResponseCacheEntity

@DbDao
@Dao
interface ApiResponseCacheDao {

    @Query("SELECT * FROM api_response_cache WHERE cacheKey = :cacheKey LIMIT 1")
    fun observe(cacheKey: String): Flow<ApiResponseCacheEntity?>

    /**
     * Every cached response under one prefix, re-emitting on any write to the table — the read side
     * of [deleteByPrefix]. A search over "everything this device has cached for clients" (addresses,
     * identifiers) is a scan of these rows, because each client's list is its own row.
     */
    @Query("SELECT * FROM api_response_cache WHERE cacheKey LIKE :prefix || '%'")
    fun observeByPrefix(prefix: String): Flow<List<ApiResponseCacheEntity>>

    @Upsert
    suspend fun upsert(row: ApiResponseCacheEntity)

    @Query("DELETE FROM api_response_cache WHERE cacheKey = :cacheKey")
    suspend fun delete(cacheKey: String)

    /**
     * Drop every cached response under one prefix — the seam a feature uses to invalidate its own
     * reads after a write, without knowing which keys exist.
     */
    @Query("DELETE FROM api_response_cache WHERE cacheKey LIKE :prefix || '%'")
    suspend fun deleteByPrefix(prefix: String)

    @Query("DELETE FROM api_response_cache")
    suspend fun deleteAll()
}
