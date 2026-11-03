/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.document

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kpt.core.database.AppDatabase
import kpt.core.database.di.testPlatformModule
import kpt.core.database.document.entity.DocumentContentEntity
import kpt.core.database.document.entity.DocumentEntity
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The two document tables behind `DocumentStore` — see `FetchedAtDaoTest` for the harness. */
class DocumentDaoTest {

    private lateinit var database: AppDatabase

    @BeforeTest
    fun setup() {
        startKoin { modules(testPlatformModule) }
        database = KoinPlatform.getKoin().get()
    }

    @AfterTest
    fun teardown() {
        runCatching { database.close() }
        stopKoin()
    }

    private fun row(id: Int, entityId: Int = 1) =
        DocumentEntity(id = id, parentEntityType = "clients", parentEntityId = entityId, name = "doc$id")

    @Test
    fun replaceDropsARowTheServerNoLongerHas() = runTest {
        val dao = database.documentDao
        dao.replaceForEntity("clients", 1, listOf(row(1), row(2)))
        // Document 2 was deleted on the server: the next fetch carries only 1. The upsert-only
        // writer this replaces left 2 in the table, so it kept showing after its own delete.
        dao.replaceForEntity("clients", 1, listOf(row(1)))

        assertEquals(listOf(1), dao.observeForEntity("clients", 1).first().map { it.id })
    }

    @Test
    fun replaceLeavesOtherEntitiesAlone() = runTest {
        val dao = database.documentDao
        dao.replaceForEntity("clients", 1, listOf(row(1, entityId = 1)))
        dao.replaceForEntity("clients", 2, listOf(row(2, entityId = 2)))
        dao.replaceForEntity("clients", 1, emptyList())

        assertEquals(emptyList(), dao.observeForEntity("clients", 1).first())
        assertEquals(listOf(2), dao.observeForEntity("clients", 2).first().map { it.id })
    }

    @Test
    fun contentRoundTripsItsBytes() = runTest {
        val dao = database.documentContentDao
        val pdf = "%PDF-1.7".encodeToByteArray()
        dao.upsert(DocumentContentEntity("client_identifiers", 1, 3, pdf))

        assertContentEquals(pdf, dao.observe("client_identifiers", 1, 3).first()?.bytes)
        // The key is the whole triple — the same document id under another entity is another file.
        assertNull(dao.observe("clients", 1, 3).first())
    }

    @Test
    fun deleteAllIsWhatTheLogoutPurgeRuns() = runTest {
        val dao = database.documentContentDao
        dao.upsert(DocumentContentEntity("clients", 1, 1, byteArrayOf(1)))
        dao.upsert(DocumentContentEntity("clients", 2, 2, byteArrayOf(2)))
        dao.deleteAll()

        assertNull(dao.observe("clients", 1, 1).first())
        assertNull(dao.observe("clients", 2, 2).first())
    }
}
