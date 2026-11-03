/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.document.upload

import androidx.lifecycle.SavedStateHandle
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
import kpt.feature.document.navigation.DocumentUploadRoute
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The validation and the create-vs-replace decision. Both were in the SCREEN in the original —
 * three fields and three error booleans in `remember`, validated inside the submit lambda — so
 * neither survived rotation nor could be tested at all.
 */
class DocumentUploadViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private class FakeCommands : DocumentCommandRepository {
        val created = mutableListOf<Pair<String, Int>>()
        val updated = mutableListOf<Triple<String, Int, Int>>()

        override suspend fun createDocument(
            entityType: String,
            entityId: Int,
            upload: DocumentUpload,
        ): MutationResult<SaveResponse> {
            created += entityType to entityId
            return MutationResult.Applied(SaveResponse(), synced = true)
        }

        override suspend fun updateDocument(
            entityType: String,
            entityId: Int,
            documentId: Int,
            upload: DocumentUpload,
        ): MutationResult<GenericResponse> {
            updated += Triple(entityType, entityId, documentId)
            return MutationResult.Applied(GenericResponse(), synced = true)
        }

        override suspend fun removeDocument(entityType: String, entityId: Int, documentId: Int) =
            error("not used here")
    }

    private fun viewModel(
        commands: FakeCommands = FakeCommands(),
        documentId: Int = DocumentUploadRoute.CREATE_NEW,
    ) = DocumentUploadViewModel(
        commands,
        SavedStateHandle(
            mapOf("entityType" to "groups", "entityId" to 7, "documentId" to documentId),
        ),
    )

    // ---- create vs replace ----

    @Test
    fun theSentinelDocumentIdMeansCREATE() = runTest(dispatcher) {
        val vm = viewModel(documentId = DocumentUploadRoute.CREATE_NEW)
        testScheduler.advanceUntilIdle()
        // A nav argument cannot be absent, so -1 is how "no document yet" reaches the screen. The
        // ViewModel narrows it to null so `isEditing` is a real question.
        assertNull(vm.stateFlow.value.documentId)
        assertFalse(vm.stateFlow.value.isEditing)
    }

    @Test
    fun aRealDocumentIdMeansREPLACE() = runTest(dispatcher) {
        val vm = viewModel(documentId = 42)
        testScheduler.advanceUntilIdle()
        assertEquals(42, vm.stateFlow.value.documentId)
        assertTrue(vm.stateFlow.value.isEditing)
    }

    // ---- validation ----

    @Test
    fun allThreeFieldsAreRequiredAndSubmitIsBlockedUntilTheyAreGiven() = runTest(dispatcher) {
        val commands = FakeCommands()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(DocumentUploadAction.Submit)
        testScheduler.advanceUntilIdle()

        // The original's rule: name, description AND a file. (Fineract itself needs only the file
        // and a name; the extra requirement is the original's and is kept.)
        val state = vm.stateFlow.value
        assertTrue(state.nameError)
        assertTrue(state.descriptionError)
        assertTrue(state.fileError)
        assertTrue(commands.created.isEmpty(), "nothing must be posted while invalid")
    }

    @Test
    fun aFieldsErrorClearsAsSoonAsItIsEdited() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(DocumentUploadAction.Submit)
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.nameError)

        vm.actionChannel.trySend(DocumentUploadAction.NameChanged("Deed"))
        testScheduler.advanceUntilIdle()
        assertFalse(vm.stateFlow.value.nameError)
        // The others are untouched — editing one field must not clear another's error.
        assertTrue(vm.stateFlow.value.descriptionError)
    }

    @Test
    fun submitStaysBlockedWhileOnlyTheFileIsMissing() = runTest(dispatcher) {
        val commands = FakeCommands()
        val vm = viewModel(commands)

        vm.actionChannel.trySend(DocumentUploadAction.NameChanged("Deed"))
        vm.actionChannel.trySend(DocumentUploadAction.DescriptionChanged("signed copy"))
        vm.actionChannel.trySend(DocumentUploadAction.Submit)
        testScheduler.advanceUntilIdle()

        // A file cannot be faked here — `PlatformFile` is a platform handle, and `readBytes()`
        // would touch the filesystem. What matters is that the guard refuses to post without one:
        // the original validated in the screen's submit lambda, where nothing could check it.
        assertTrue(vm.stateFlow.value.fileError)
        assertFalse(vm.stateFlow.value.nameError)
        assertFalse(vm.stateFlow.value.descriptionError)
        assertTrue(commands.created.isEmpty())
        assertTrue(commands.updated.isEmpty())
    }

    @Test
    fun theValidatorIsPureAndChecksAllThreeAtOnce() {
        // `validated()` is the whole rule in one place, so it can be checked without a ViewModel.
        val empty = DocumentUploadState().validated()
        assertTrue(empty.nameError && empty.descriptionError && empty.fileError)
        assertFalse(empty.isValid)

        val named = DocumentUploadState(name = "Deed", description = "signed").validated()
        assertFalse(named.nameError)
        assertFalse(named.descriptionError)
        // Still invalid: no file.
        assertTrue(named.fileError)
        assertFalse(named.isValid)
    }

    @Test
    fun blankIsTreatedAsMissingNotJustEmpty() {
        val spaces = DocumentUploadState(name = "   ", description = "\t").validated()
        assertTrue(spaces.nameError)
        assertTrue(spaces.descriptionError)
    }

    @Test
    fun navigateBackIsAnEventNotAStateChange() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(DocumentUploadAction.NavigateBack)
        testScheduler.advanceUntilIdle()
        // Nothing in the form changed — leaving is the caller's business.
        assertEquals("", vm.stateFlow.value.name)
    }
}
