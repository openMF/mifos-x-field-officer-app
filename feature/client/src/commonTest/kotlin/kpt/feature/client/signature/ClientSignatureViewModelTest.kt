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

package kpt.feature.client.signature

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
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.shared.GenericResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A signature is a DOCUMENT named `clientSignature`. These tests pin that indirection and the
 * derived-id behaviour that replaced the original's two-step load.
 */
class ClientSignatureViewModelTest {

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
            error("fetchDocuments is not used by the signature screen")
    }

    private class FakeCommands(
        private val gate: CompletableDeferred<Unit>? = null,
        private val result: () -> MutationResult<GenericResponse> =
            { MutationResult.Applied(GenericResponse(), synced = true) },
    ) : DocumentCommandRepository {
        val created = mutableListOf<Pair<String, Int>>()
        val updated = mutableListOf<Triple<String, Int, Int>>()
        val removed = mutableListOf<Triple<String, Int, Int>>()

        override suspend fun createDocument(
            entityType: String,
            entityId: Int,
            upload: DocumentUpload,
        ): MutationResult<SaveResponse> {
            created += entityType to entityId
            gate?.await()
            return result().asSaveResponse()
        }

        override suspend fun updateDocument(
            entityType: String,
            entityId: Int,
            documentId: Int,
            upload: DocumentUpload,
        ): MutationResult<GenericResponse> {
            updated += Triple(entityType, entityId, documentId)
            gate?.await()
            return result()
        }

        override suspend fun removeDocument(
            entityType: String,
            entityId: Int,
            documentId: Int,
        ): MutationResult<GenericResponse> {
            removed += Triple(entityType, entityId, documentId)
            gate?.await()
            return result()
        }
    }

    private fun viewModel(commands: FakeCommands = FakeCommands(), clientId: Int = 42) =
        ClientSignatureViewModel(
            repository,
            repository,
            commands,
            SavedStateHandle(mapOf("clientId" to clientId)),
        )

    private fun doc(id: Int, name: String) = Document(id = id, name = name, fileName = "$name.png")

    @Test
    fun theSignatureIsFoundInTheClientsDOCUMENTListByItsReservedName() = runTest(dispatcher) {
        val vm = viewModel()
        docsState.value = ScreenState.Content(
            listOf(doc(1, "passport"), doc(7, "clientSignature"), doc(9, "deed")),
        )

        vm.signatureId.test {
            assertIs<ScreenState.Loading>(awaitItem())
            // There is no signature endpoint — it is a document under a reserved name. Derived
            // from the list, so "is there one" and "which one" are a single answer.
            assertEquals(7, assertIs<ScreenState.Content<Int?>>(awaitItem()).data)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf("clients" to 42), requested.toList())
    }

    @Test
    fun aClientWithNoSignatureYieldsNullRatherThanAnError() = runTest(dispatcher) {
        val vm = viewModel()
        docsState.value = ScreenState.Content(listOf(doc(1, "passport")))

        vm.signatureId.test {
            assertIs<ScreenState.Loading>(awaitItem())
            // The common case. The screen shows "No Signature Found!" and the upload nudge.
            assertNull(assertIs<ScreenState.Content<Int?>>(awaitItem()).data)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadingTheImageDownloadsTheDocumentTheScreenNamed() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(ClientSignatureAction.LoadSignature(signatureId = 7))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(Triple("clients", 42, 7)), downloaded.toList())
        assertEquals(listOf<Byte>(1, 2, 3), vm.stateFlow.value.clientSignatureImage?.toList())
    }

    @Test
    fun anUndownloadableSignatureIsNotAnErrorState() = runTest(dispatcher) {
        downloadBytes = null
        val vm = viewModel()
        vm.actionChannel.trySend(ClientSignatureAction.LoadSignature(7))
        testScheduler.advanceUntilIdle()

        // The empty-image message IS the empty state; a failed attachment fetch must not turn the
        // screen into an error.
        assertNull(vm.stateFlow.value.clientSignatureImage)
        assertNull(vm.stateFlow.value.writeError)
    }

    @Test
    fun deletingSendsTheIdFromTheActionAndReReadsTheDocumentList() = runTest(dispatcher) {
        val commands = FakeCommands()
        val vm = viewModel(commands)
        docsState.value = ScreenState.Content(listOf(doc(7, "clientSignature")))
        testScheduler.advanceUntilIdle()

        refreshes.test {
            vm.actionChannel.trySend(ClientSignatureAction.DeleteSignature(7))
            testScheduler.advanceUntilIdle()

            assertEquals(listOf(Triple("clients", 42, 7)), commands.removed.toList())
            // The derived id must follow the delete, or the screen keeps offering Delete for a
            // document that is gone.
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
        // The cached bytes are dropped so the screen does not keep drawing the deleted signature.
        assertNull(vm.stateFlow.value.clientSignatureImage)
    }

    @Test
    fun aSecondDeleteInsideTheInFlightWindowIsDropped() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeCommands(gate = gate)
        val vm = viewModel(commands)

        vm.actionChannel.trySend(ClientSignatureAction.DeleteSignature(7))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(ClientSignatureAction.DeleteSignature(7))
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.removed.size)
    }

    @Test
    fun anOfflineDeleteIsReportedAndClearsTheInFlightFlag() = runTest(dispatcher) {
        val commands = FakeCommands(result = { MutationResult.Blocked(BlockReason.OFFLINE) })
        val vm = viewModel(commands)

        vm.actionChannel.trySend(ClientSignatureAction.DeleteSignature(7))
        testScheduler.advanceUntilIdle()

        assertEquals(SignatureWriteError.Offline, vm.stateFlow.value.writeError)
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }

    @Test
    fun theUploadSheetAndDeleteConfirmationAreDialogStatesNotReadStates() = runTest(dispatcher) {
        val vm = viewModel()

        vm.actionChannel.trySend(ClientSignatureAction.ShowUploadOptionsBottomSheet)
        testScheduler.advanceUntilIdle()
        assertEquals(ClientSignatureState.DialogState.ShowUploadOptions, vm.stateFlow.value.dialogState)

        vm.actionChannel.trySend(ClientSignatureAction.ShowDeleteDialog)
        testScheduler.advanceUntilIdle()
        assertEquals(ClientSignatureState.DialogState.ShowDeleteDialog, vm.stateFlow.value.dialogState)

        vm.actionChannel.trySend(ClientSignatureAction.OpenSignatureDraw)
        testScheduler.advanceUntilIdle()
        assertEquals(ClientSignatureState.DialogState.ShowSignatureDraw, vm.stateFlow.value.dialogState)

        vm.actionChannel.trySend(ClientSignatureAction.OnDismissDialog)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun retryReachesTheDocumentStream() = runTest(dispatcher) {
        val vm = viewModel()
        refreshes.test {
            vm.actionChannel.trySend(ClientSignatureAction.OnRetry)
            testScheduler.advanceUntilIdle()
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theStateComparesTheImageByContentNotByIdentity() {
        val a = ClientSignatureState(clientSignatureImage = byteArrayOf(1, 2))
        val b = ClientSignatureState(clientSignatureImage = byteArrayOf(1, 2))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}

/** The create endpoint answers [SaveResponse] (it carries the new id); every other arm passes through. */
private fun MutationResult<GenericResponse>.asSaveResponse(): MutationResult<SaveResponse> = when (this) {
    is MutationResult.Applied -> MutationResult.Applied(SaveResponse(), synced)
    is MutationResult.Conflicted -> MutationResult.Conflicted(conflictId, SaveResponse())
    is MutationResult.Blocked -> this
    is MutationResult.Failed -> this
}
