/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.infra.testMutationGateway
import kpt.core.data.notification.impl.NotificationRepositoryImpl
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.cache.entity.ApiResponseCacheEntity
import kpt.core.model.notification.AppNotification
import kpt.core.network.mifos.notification.api.NotificationApi
import kpt.core.network.mifos.notification.dto.NotificationDto
import kpt.core.network.mifos.notification.dto.NotificationPageDto
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.notification.provideNotificationsLocalStore
import kpt.core.store.notification.provideNotificationsStore
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * "Mark all read" over the REAL notification stores and one in-memory cache table: the command's
 * local effect lands in the row the list reads, without a second fetch.
 */
class NotificationRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- the server ----

    private var server = listOf(
        NotificationDto(id = 1, objectType = "loan", objectId = 10, content = "Loan approved"),
        NotificationDto(id = 2, objectType = "client", objectId = 20, content = "Client activated"),
        NotificationDto(id = 3, objectType = "loan", objectId = 11, content = "Repayment", isRead = true),
    )
    private var markRefused: Throwable? = null
    private var marked = 0

    private val api = object : NotificationApi {
        override suspend fun getNotifications(
            isRead: Boolean,
            offset: Int,
            limit: Int,
            orderBy: String,
            sortOrder: String,
        ) = NotificationPageDto(pageItems = server.filter { it.isRead == isRead })

        override suspend fun markAllRead() {
            markRefused?.let { throw it }
            marked++
            server = server.map { it.copy(isRead = true) }
        }
    }

    // ---- core/database ----

    private val cacheRows = MutableStateFlow<Map<String, ApiResponseCacheEntity>>(emptyMap())
    private val cache = object : ApiResponseCacheDao {
        override fun observe(cacheKey: String) = cacheRows.map { it[cacheKey] }
        override fun observeByPrefix(
            prefix: String,
        ) = cacheRows.map { rows -> rows.filterKeys { it.startsWith(prefix) }.values.toList() }
        override suspend fun upsert(row: ApiResponseCacheEntity) {
            cacheRows.value += row.cacheKey to row
        }
        override suspend fun delete(cacheKey: String) {
            cacheRows.value -= cacheKey
        }
        override suspend fun deleteByPrefix(prefix: String) {
            cacheRows.value = cacheRows.value.filterKeys { !it.startsWith(prefix) }
        }
        override suspend fun deleteAll() {
            cacheRows.value = emptyMap()
        }
    }

    private val store = provideNotificationsStore(api, cache)
    private val repository = NotificationRepositoryImpl(
        notificationApi = api,
        gateway = testMutationGateway(),
        store = store,
        localStore = provideNotificationsLocalStore(cache),
    )

    private fun cachedRow(): List<AppNotification>? = cacheRows.value[AppCacheKeys.Notifications.LIST]
        ?.let { json.decodeFromString<List<AppNotification>>(it.payload) }

    @Test
    fun markingAllReadRecordsItInTheCachedListTheBellReads() = runTest {
        store.fresh(Unit)
        assertEquals(2, cachedRow()!!.count { !it.isRead })
        // Only the write itself reaches the server — the list is NOT re-fetched to learn the result.
        server = emptyList()

        assertIs<MutationResult.Applied<Unit>>(repository.markAllRead())

        assertEquals(1, marked)
        val row = cachedRow()!!
        assertEquals(listOf(1L, 2L, 3L), row.map { it.id })
        assertTrue(row.all { it.isRead })
    }

    @Test
    fun withNothingCachedThereIsNothingToMark() = runTest {
        assertIs<MutationResult.Applied<Unit>>(repository.markAllRead())

        assertEquals(1, marked)
        assertNull(cachedRow(), "no list was ever fetched, so none is invented")
    }

    @Test
    fun aRefusedMarkLeavesTheCachedListAsItWas() = runTest {
        store.fresh(Unit)
        markRefused = IllegalStateException("403")

        assertIs<MutationResult.Failed>(repository.markAllRead())

        assertEquals(2, cachedRow()!!.count { !it.isRead })
    }
}
