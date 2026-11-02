/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.feature.document.list

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.data.document.DocumentRepository
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.model.shared.GenericResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DocumentListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val docsState = MutableStateFlow<ScreenState<List<Document>>>(ScreenState.Loading)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    private val requested = mutableListOf<Pair<String, Int>>()
    private val downloaded = mutableListOf<Triple<String, Int, Int>>()
    private var downloadBytes: ByteArray? = byteArrayOf(1, 2, 3)

    private val repository = object : DocumentRepository {
        override fun documentsStream(
            entityType: String,
            entityId: Int,
            scope: CoroutineScope,
        ): ScreenDataStream<List<Document>> {
            requested += entityType to entityId
            return screenDataStreamForTesting(docsState, refreshTrigger = refreshes)
        }

        override suspend fun downloadDocument(
            entityType: String,
            entityId: Int,
            documentId: Int,
        ): ByteArray? {
            downloaded += Triple(entityType, entityId, documentId)
            return downloadBytes
        }

        override suspend fun fetchDocuments(entityType: String, entityId: Int) =
            error("fetchDocuments is not used by the document list")
    }

    private class FakeCommands(
        private val gate: CompletableDeferred<Unit>? = null,
        private val result: () -> MutationResult<GenericResponse> =
            { MutationResult.Applied(GenericResponse(), synced = true) },
    ) : DocumentCommandRepository {
        val removed = mutableListOf<Triple<String, Int, Int>>()

        override suspend fun removeDocument(
            entityType: String,
            entityId: Int,
            documentId: Int,
        ): MutationResult<GenericResponse> {
            removed += Triple(entityType, entityId, documentId)
            gate?.await()
            return result()
        }

        override suspend fun createDocument(
            entityType: String,
            entityId: Int,
            upload: DocumentUpload,
        ) = error("not used here")

        override suspend fun updateDocument(
            entityType: String,
            entityId: Int,
            documentId: Int,
            upload: DocumentUpload,
        ) = error("not used here")
    }

    private fun viewModel(
        commands: FakeCommands = FakeCommands(),
        entityType: String = "groups",
        entityId: Int = 7,
    ) = DocumentListViewModel(
        repository,
        repository,
        commands,
        SavedStateHandle(mapOf("entityType" to entityType, "entityId" to entityId)),
    )

    private fun doc(id: Int, name: String? = "Deed", desc: String? = "signed") =
        Document(id = id, name = name, description = desc)

    @Test
    fun theReadIsScopedToTheENTITYTypeAndIdFromTheRoute() = runTest(dispatcher) {
        viewModel(entityType = "groups", entityId = 7)
        testScheduler.advanceUntilIdle()
        // This list is generic across Fineract entity types — the scoping IS the route.
        assertEquals(listOf("groups" to 7), requested.toList())
    }

    @Test
    fun theDocumentsComeFromTheStream() = runTest(dispatcher) {
        val vm = viewModel()
        docsState.value = ScreenState.Content(listOf(doc(1), doc(2)))

        vm.documentList.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertEquals(2, assertIs<ScreenState.Content<List<Document>>>(awaitItem()).data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aSuccessfulRemoveReReadsTheListSoTheRowDisappears() = runTest(dispatcher) {
        val commands = FakeCommands()
        val vm = viewModel(commands)
        docsState.value = ScreenState.Content(listOf(doc(1)))
        testScheduler.advanceUntilIdle()

        refreshes.test {
            vm.actionChannel.trySend(DocumentListAction.Remove(1))
            testScheduler.advanceUntilIdle()

            assertEquals(listOf(Triple("groups", 7, 1)), commands.removed.toList())
            // The command repository is cache-free, so without this the removed row stays on
            // screen — the defect the note list shipped with.
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(DocumentNotice.Removed, vm.stateFlow.value.notice)
    }

    @Test
    fun removingTheLastDocumentLeavesAnEmptyListNotTheRemovedRow() = runTest(dispatcher) {
        // The stream reports "no documents" as an empty Content (no `isEmpty` predicate); the
        // screen draws its AppEmptyUi from it. With the predicate the removed row stayed on screen.
        val vm = viewModel()
        vm.documentList.test {
            docsState.value = ScreenState.Content(listOf(doc(1)))
            testScheduler.advanceUntilIdle()
            docsState.value = ScreenState.Content(emptyList())
            testScheduler.advanceUntilIdle()

            val last = assertIs<ScreenState.Content<List<Document>>>(expectMostRecentItem())
            assertTrue(last.data.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aSecondRemoveInsideTheInFlightWindowIsDropped() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeCommands(gate = gate)
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(DocumentListAction.Remove(1))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(DocumentListAction.Remove(1))
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.removed.size)
    }

    @Test
    fun anOfflineRemoveSaysSo() = runTest(dispatcher) {
        val commands = FakeCommands(result = { MutationResult.Blocked(BlockReason.OFFLINE) })
        val vm = viewModel(commands)

        vm.actionChannel.trySend(DocumentListAction.Remove(1))
        testScheduler.advanceUntilIdle()

        assertEquals(DocumentNotice.Offline, vm.stateFlow.value.notice)
        assertTrue(!vm.stateFlow.value.isWorking)
    }

    @Test
    fun aDownloadReportsWhatActuallyHappened() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(DocumentListAction.Download(5))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(Triple("groups", 7, 5)), downloaded.toList())
        assertEquals(DocumentNotice.Downloaded, vm.stateFlow.value.notice)
    }

    @Test
    fun aFailedDownloadDoesNotReportSuccess() = runTest(dispatcher) {
        // THE REGRESSION. The original set its `downloadDocumentState` flag on success only but
        // reported "Download successful" from a boolean that was never reset, so a failure could
        // show the success message. The notice now carries the actual outcome.
        downloadBytes = null
        val vm = viewModel()
        vm.actionChannel.trySend(DocumentListAction.Download(5))
        testScheduler.advanceUntilIdle()

        assertEquals(DocumentNotice.DownloadFailed, vm.stateFlow.value.notice)
    }

    @Test
    fun aNoticeClearsSoTheSameMessageCanFireTwice() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(DocumentListAction.Download(5))
        testScheduler.advanceUntilIdle()
        assertEquals(DocumentNotice.Downloaded, vm.stateFlow.value.notice)

        // The original's two booleans latched true, so each snackbar fired at most once per
        // screen visit.
        vm.actionChannel.trySend(DocumentListAction.DismissNotice)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.notice)

        vm.actionChannel.trySend(DocumentListAction.Download(5))
        testScheduler.advanceUntilIdle()
        assertEquals(DocumentNotice.Downloaded, vm.stateFlow.value.notice)
    }

    @Test
    fun theUploadEventCarriesTheEntityTheListIsScopedTo() = runTest(dispatcher) {
        val vm = viewModel(entityType = "groups", entityId = 7)
        vm.eventFlow.test {
            vm.actionChannel.trySend(DocumentListAction.UploadClicked)
            assertEquals(DocumentListEvent.Upload("groups", 7), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
