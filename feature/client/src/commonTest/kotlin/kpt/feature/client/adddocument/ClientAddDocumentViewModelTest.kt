/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.adddocument

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.shared.GenericResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The client add-document form.
 *
 * The file PICKER itself is not driven here — it suspends on a platform dialog that `commonTest`
 * cannot open, which is the same limit the `editprofile` and `document/upload` siblings hit. What
 * IS testable is everything around it: the route argument reaching the write and the submit guards.
 *
 * NOT covered, deliberately rather than by omission: the `MutationResult` fan-out that tells
 * Offline from Failed. Reaching `submit()`'s write needs a chosen `PlatformFile`, and the only way
 * to get one here is the picker. A test that asserted `AddDocumentError.Offline ==
 * AddDocumentError.Offline` would pass without exercising the mapping at all — the shape of
 * fixture this port has already been bitten by. The branch is covered in `feature/document`'s
 * `DocumentUploadViewModelTest`, which runs the same four-way fan-out against the same repository
 * contract.
 */
class ClientAddDocumentViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private class FakeCommands(
        private val outcome: () -> MutationResult<GenericResponse> =
            { MutationResult.Applied(GenericResponse(), synced = true) },
    ) : DocumentCommandRepository {
        /** (entityType, entityId) per create — proves WHAT was targeted, not just that it fired. */
        val created = mutableListOf<Pair<String, Int>>()

        override suspend fun createDocument(
            entityType: String,
            entityId: Int,
            upload: DocumentUpload,
        ): MutationResult<SaveResponse> {
            created += entityType to entityId
            return outcome().asSaveResponse()
        }

        override suspend fun updateDocument(
            entityType: String,
            entityId: Int,
            documentId: Int,
            upload: DocumentUpload,
        ) = error("the client add form never updates")

        override suspend fun removeDocument(entityType: String, entityId: Int, documentId: Int) =
            error("the client add form never removes")
    }

    private fun viewModel(
        commands: DocumentCommandRepository = FakeCommands(),
        clientId: Int = 42,
    ) = ClientAddDocumentViewModel(
        commands,
        SavedStateHandle(mapOf(ClientAddDocumentViewModel.CLIENT_ID_KEY to clientId)),
    )

    /** The route argument must reach the state the write reads — not a SavedStateHandle default. */
    @Test
    fun theRouteArgumentReachesTheState() = runTest(dispatcher) {
        assertEquals(42, viewModel().stateFlow.value.clientId)
        assertEquals(7, viewModel(clientId = 7).stateFlow.value.clientId)
    }

    /**
     * Submit is refused without a file.
     *
     * `canSubmit` gates the button, but the guard is in the ViewModel too — a disabled button is a
     * presentation detail, not a guarantee, and the original relied on exactly that distinction
     * (its progress dialog merely covered the button).
     */
    @Test
    fun submitDoesNothingWithoutAFile() = runTest(dispatcher) {
        val commands = FakeCommands()
        val vm = viewModel(commands)
        vm.trySendAction(ClientAddDocumentAction.UpdateName("Passport"))
        vm.trySendAction(ClientAddDocumentAction.Submit)
        testScheduler.advanceUntilIdle()
        assertTrue(commands.created.isEmpty(), "no file chosen, so nothing may be uploaded")
    }

    /** A blank name is a FIELD error, raised before anything is sent. */
    @Test
    fun aBlankNameIsRefusedBeforeTheWrite() = runTest(dispatcher) {
        val commands = FakeCommands()
        val vm = viewModel(commands)
        vm.trySendAction(ClientAddDocumentAction.Submit)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.nameError, "the name field must report the problem")
        assertTrue(commands.created.isEmpty())
    }

    /** Typing in the name clears its error — the message must not outlive the cause. */
    @Test
    fun typingClearsTheNameError() = runTest(dispatcher) {
        val vm = viewModel()
        vm.trySendAction(ClientAddDocumentAction.Submit)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.nameError)

        vm.trySendAction(ClientAddDocumentAction.UpdateName("P"))
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.nameError)
    }

    /** `canSubmit` is the original's `enabled = step == VIEW`, expressed as "a file is chosen". */
    @Test
    fun canSubmitRequiresAFile() = runTest(dispatcher) {
        val vm = viewModel()
        assertTrue(!vm.stateFlow.value.canSubmit, "no file chosen")
        assertTrue(!vm.stateFlow.value.copy(isSubmitting = true).canSubmit, "in flight")
    }

    /** The sheet opens and closes through state, so rotation cannot strand it open. */
    @Test
    fun theFileSourceSheetIsState() = runTest(dispatcher) {
        val vm = viewModel()
        vm.trySendAction(ClientAddDocumentAction.OpenFileSource)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.isSourceSheetOpen)

        vm.trySendAction(ClientAddDocumentAction.DismissFileSource)
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.isSourceSheetOpen)
    }

    /** An error is dismissable, so a recoverable mistake does not end the screen. */
    @Test
    fun anErrorCanBeDismissedWithoutLeavingTheForm() = runTest(dispatcher) {
        val vm = viewModel()
        vm.trySendAction(ClientAddDocumentAction.UpdateName("Passport"))
        vm.trySendAction(ClientAddDocumentAction.UpdateDescription("Scan"))
        testScheduler.advanceUntilIdle()

        vm.trySendAction(ClientAddDocumentAction.DismissError)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.error)
        // The typed values survive — the original's only error button left the screen.
        assertEquals("Passport", vm.stateFlow.value.name)
        assertEquals("Scan", vm.stateFlow.value.description)
    }

    /** The 1 MB ceiling is the original's, to the byte. */
    @Test
    fun theSizeCeilingIsTheOriginals() {
        assertEquals(1_048_576L, ClientAddDocumentViewModel.MAX_DOCUMENT_BYTES)
    }

    /** Fineract's entity segment for a client document. A wrong one attaches to the wrong thing. */
    @Test
    fun theEntityTypeIsClients() {
        assertEquals("clients", ClientAddDocumentViewModel.ENTITY_TYPE)
    }

    /** NavigateBack is dispatched, not called directly — the ViewModel owns the event. */
    @Test
    fun navigateBackIsAnEvent() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.trySendAction(ClientAddDocumentAction.NavigateBack)
            assertEquals(ClientAddDocumentEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

/** The create endpoint answers [SaveResponse] (it carries the new id); every other arm passes through. */
private fun MutationResult<GenericResponse>.asSaveResponse(): MutationResult<SaveResponse> = when (this) {
    is MutationResult.Applied -> MutationResult.Applied(SaveResponse(), synced)
    is MutationResult.Conflicted -> MutationResult.Conflicted(conflictId, SaveResponse())
    is MutationResult.Blocked -> this
    is MutationResult.Failed -> this
}
