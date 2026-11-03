/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.document

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.infra.StoreFactory
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.document.impl.DocumentCommandRepositoryImpl
import kpt.core.data.document.impl.DocumentRepositoryImpl
import kpt.core.data.infra.testMutationGateway
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.document.api.DocumentApi
import kpt.core.store.document.DocumentContentKey
import kpt.core.store.document.DocumentItemKey
import kpt.core.store.document.DocumentKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The document repositories over REAL Store5 stores wired the way `core/store` wires them: a read
 * store and a local write store over the SAME table (an in-memory list of rows here), so a write
 * through one is visible through the other with nothing refreshed in between.
 */
class DocumentRepositoryTest {

    private data class Row(val entityType: String, val entityId: Int, val doc: Document)

    /** The server. `online = false` makes every read throw, as a dropped connection does. */
    private var online = true
    private val serverDocs = mutableMapOf(DocumentKey("clients", 1) to listOf(Document(id = 1, name = "a")))
    private val serverFiles = mutableMapOf(DocumentContentKey("clients", 1, 1) to byteArrayOf(1, 2, 3))
    private var fileFetches = 0
    private var nextServerId = 10

    /** The two tables: `documents` and `document_contents`. */
    private val documents = MutableStateFlow<List<Row>>(emptyList())
    private val contents = MutableStateFlow<Map<DocumentContentKey, ByteArray>>(emptyMap())

    private fun rowsOf(key: DocumentKey) = documents.value
        .filter { it.entityType == key.entityType && it.entityId == key.entityId }.map { it.doc }

    // ---- core/store, as provided in DocumentStore.kt ----

    private val readStore = StoreBuilder.from(
        fetcher = Fetcher.of { key: DocumentKey ->
            check(online) { "offline" }
            serverDocs[key].orEmpty()
        },
        sourceOfTruth = SourceOfTruth.of(
            // Mirrors DocumentStore: an empty table is an empty list (see NullReaderStreamTest).
            reader = { key: DocumentKey -> documents.map { rowsOf(key) } },
            writer = { key: DocumentKey, docs: List<Document> ->
                documents.value = documents.value
                    .filterNot { it.entityType == key.entityType && it.entityId == key.entityId } +
                    docs.map { Row(key.entityType, key.entityId, it) }
            },
        ),
    ).build()

    private val itemStore = StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: DocumentItemKey ->
                documents.map { rows -> rows.firstOrNull { it.doc.id == key.documentId }?.doc }
            },
            writer = { key: DocumentItemKey, doc: Document ->
                documents.value = documents.value.filterNot { it.doc.id == key.documentId } +
                    Row(key.entityType, key.entityId, doc)
            },
            delete = { key: DocumentItemKey ->
                documents.value = documents.value.filterNot { it.doc.id == key.documentId }
            },
        ),
    )

    private val contentStore = StoreBuilder.from(
        fetcher = Fetcher.of { key: DocumentContentKey ->
            check(online) { "offline" }
            fileFetches++
            serverFiles.getValue(key)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: DocumentContentKey -> contents.map { it[key] } },
            writer = { key: DocumentContentKey, bytes: ByteArray -> contents.value += key to bytes },
        ),
    ).build()

    private val contentLocalStore = StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: DocumentContentKey -> contents.map { it[key] } },
            writer = { key: DocumentContentKey, bytes: ByteArray -> contents.value += key to bytes },
            delete = { key: DocumentContentKey -> contents.value -= key },
        ),
    )

    // ---- core/network ----

    private val api = object : DocumentApi {
        override suspend fun getDocuments(entityType: String, entityId: Int) = error("reads go through the store")
        override suspend fun downloadDocument(entityType: String, entityId: Int, documentId: Int): HttpResponse =
            error("reads go through the store")

        override suspend fun createDocument(entityType: String, entityId: Int, request: MultiPartFormDataContent) =
            SaveResponse(resourceId = nextServerId++)

        override suspend fun removeDocument(entityType: String, entityId: Int, documentId: Int) = GenericResponse()

        override suspend fun updateDocument(
            entityType: String,
            entityId: Int,
            documentId: Int,
            request: MultiPartFormDataContent,
        ) = GenericResponse()
    }

    private val repository = DocumentRepositoryImpl(readStore, contentStore)
    private val commands = DocumentCommandRepositoryImpl(api, testMutationGateway(), itemStore, contentLocalStore)

    private val scan = DocumentUpload(
        name = "Passport",
        fileName = "passport.pdf",
        mimeType = "application/pdf",
        bytes = "%PDF-1.7".encodeToByteArray(),
        description = "PassportA1Active",
    )

    // ---- reads ----

    @Test
    fun theLookupWritesTheServersListThroughTheSourceOfTruth() = runTest {
        assertEquals(listOf(1), repository.fetchDocuments("clients", 1)?.map { it.id })
        assertEquals(listOf(1), rowsOf(DocumentKey("clients", 1)).map { it.id })
    }

    @Test
    fun offlineTheLookupAnswersFromTheDatabase() = runTest {
        repository.fetchDocuments("clients", 1)
        online = false
        // Device finding 2026-10-23: a fresh-only lookup failed here and stranded a cached scan.
        assertEquals(listOf(1), repository.fetchDocuments("clients", 1)?.map { it.id })
    }

    @Test
    fun offlineWithNothingCachedIsNull() = runTest {
        online = false
        assertNull(repository.fetchDocuments("clients", 1))
    }

    @Test
    fun aFileViewedOnceOpensOfflineWithoutRefetching() = runTest {
        assertContentEquals(byteArrayOf(1, 2, 3), repository.downloadDocument("clients", 1, 1))
        online = false
        assertContentEquals(byteArrayOf(1, 2, 3), repository.downloadDocument("clients", 1, 1))
        assertEquals(1, fileFetches)
    }

    // ---- writes go through the stores ----

    @Test
    fun anUploadLandsInTheTableTheReadsObserve() = runTest {
        repository.fetchDocuments("clients", 1)

        val result = commands.createDocument("clients", 1, scan)

        assertEquals(10, assertIs<MutationResult.Applied<SaveResponse>>(result).value.resourceId)
        // Through the local write store into the shared table — no refetch of the list.
        val uploaded = rowsOf(DocumentKey("clients", 1)).single { it.id == 10 }
        assertEquals("PassportA1Active", uploaded.description)
        assertEquals("application/pdf", uploaded.type)
    }

    @Test
    fun anUploadedFileOpensOfflineWithoutEverBeingDownloaded() = runTest {
        commands.createDocument("clients", 1, scan)
        online = false

        assertContentEquals(scan.bytes, repository.downloadDocument("clients", 1, 10))
        assertEquals(0, fileFetches)
    }

    @Test
    fun aDeleteRemovesTheRowAndItsFile() = runTest {
        repository.fetchDocuments("clients", 1)
        repository.downloadDocument("clients", 1, 1)

        commands.removeDocument("clients", 1, 1)

        assertTrue(rowsOf(DocumentKey("clients", 1)).none { it.id == 1 })
        assertNull(contents.value[DocumentContentKey("clients", 1, 1)])
    }

    @Test
    fun anOfflineUploadIsBlockedAndWritesNothing() = runTest {
        val offlineCommands = DocumentCommandRepositoryImpl(
            api,
            testMutationGateway(isOnline = false),
            itemStore,
            contentLocalStore,
        )

        assertIs<MutationResult.Blocked>(offlineCommands.createDocument("clients", 1, scan))
        assertEquals(emptyList(), documents.value)
    }
}
