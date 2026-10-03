/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
