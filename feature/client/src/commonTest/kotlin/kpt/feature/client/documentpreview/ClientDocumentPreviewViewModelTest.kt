/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.documentpreview

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.data.document.DocumentRepository
import kpt.core.model.objects.noncoreobjects.Document
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ClientDocumentPreviewViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    /** What the server returns for the entity's document list; `null` = the read failed. */
    private var serverList: List<Document>? = emptyList()
    private val downloads = mutableListOf<Triple<String, Int, Int>>()
    private var file: ByteArray? = PDF

    private val repository = object : DocumentRepository {
        // The regression this guards: the lookup once read the cache-first STREAM, which an upload
        // does not invalidate, so a scan uploaded seconds earlier was reported missing on device.
        // Any stream read here fails the test.
        override fun documentsStream(
            entityType: String,
            entityId: Int,
            scope: CoroutineScope,
        ): ScreenDataStream<List<Document>> = error("the preview must not read the cached stream")

        override suspend fun fetchDocuments(entityType: String, entityId: Int) = serverList

        override suspend fun downloadDocument(entityType: String, entityId: Int, documentId: Int): ByteArray? {
            downloads += Triple(entityType, entityId, documentId)
            return file
        }
    }

    private fun viewModel(vararg args: Pair<String, Any>) = ClientDocumentPreviewViewModel(
        repository,
        SavedStateHandle(mapOf("entityType" to "clients", "entityId" to 42, "title" to "Deed", *args)),
    )

    // ---- by id: the documents list ----

    @Test
    fun byIdDownloadsThatDocumentOfThatEntity() = runTest(dispatcher) {
        val vm = viewModel("documentId" to 7)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(Triple("clients", 42, 7)), downloads)
        val loaded = assertIs<PreviewContent.Loaded>(vm.stateFlow.value.content)
        assertContentEquals(PDF, loaded.bytes)
        assertEquals("Deed", vm.stateFlow.value.title)
    }

    @Test
    fun aFailedDownloadIsRetryableAndRetryRecovers() = runTest(dispatcher) {
        file = null
        val vm = viewModel("documentId" to 7)
        testScheduler.advanceUntilIdle()
        assertIs<PreviewContent.Failed>(vm.stateFlow.value.content)

        file = PDF
        vm.trySendAction(ClientDocumentPreviewAction.Retry)
        testScheduler.advanceUntilIdle()
        assertIs<PreviewContent.Loaded>(vm.stateFlow.value.content)
        assertEquals(2, downloads.size)
    }

    // ---- by key: the identifiers list ----

    @Test
    fun byKeyDownloadsTheExactMatch() = runTest(dispatcher) {
        serverList = listOf(
            Document(id = 1, description = "PassportA1Inactive"),
            Document(id = 2, description = "PassportA1Active", fileName = "a1.pdf", type = "application/pdf"),
        )
        val vm = viewModel("matchKey" to "PassportA1Active", "matchPrefix" to "PassportA1")
        testScheduler.advanceUntilIdle()

        // The exact key wins over an earlier prefix match.
        assertEquals(2, downloads.single().third)
        val loaded = assertIs<PreviewContent.Loaded>(vm.stateFlow.value.content)
        assertEquals("a1.pdf", loaded.fileName)
        assertEquals("application/pdf", loaded.mimeType)
    }

    @Test
    fun aStatusChangeNoLongerHidesTheScan() {
        // Uploaded while Active; the identifier is Inactive now. The original's exact match missed
        // it forever; the status-free prefix still names the one identifier.
        val docs = listOf(Document(id = 9, description = "PassportA1Active"))
        assertEquals(9, findIdentifierDocument(docs, "PassportA1Inactive", "PassportA1")?.id)
    }

    @Test
    fun emptyKeysNeverMatchEverything() {
        // An empty prefix would `startsWith`-match every document — the wrong scan, silently.
        val docs = listOf(Document(id = 9, description = "PassportA1Active"))
        assertNull(findIdentifierDocument(docs, "", ""))
    }

    @Test
    fun anIdentifierWithoutAScanSaysSo() = runTest(dispatcher) {
        serverList = emptyList()
        val vm = viewModel("matchKey" to "PassportA1Active", "matchPrefix" to "PassportA1")
        testScheduler.advanceUntilIdle()

        assertIs<PreviewContent.NoDocument>(vm.stateFlow.value.content)
        assertEquals(emptyList(), downloads)
    }

    @Test
    fun anUnreadableListIsAFailureNotAMissingScan() = runTest(dispatcher) {
        // Offline with nothing cached must not tell the officer the identifier has no document.
        serverList = null
        val vm = viewModel("matchKey" to "PassportA1Active", "matchPrefix" to "PassportA1")
        testScheduler.advanceUntilIdle()

        assertIs<PreviewContent.Failed>(vm.stateFlow.value.content)
    }

    private companion object {
        val PDF = "%PDF-1.7".encodeToByteArray()
    }
}
