/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.data.testing.ExperimentalDataTestingApi::class)

package kpt.feature.client.editprofile

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.testing.FakeClientCommandRepository
import kpt.core.data.testing.FakeClientRepository
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.feature.client.imageCodecAvailable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the client PHOTO screen — what client details' "Update Photo" opens.
 *
 * The gallery, camera and crop paths are not exercised: `FileKit.openFilePicker`,
 * `FileKitUtil.takePhoto` and `ImageCropper.crop` all suspend on a platform dialog, which a
 * common-source test cannot drive. What IS pinned is everything around them — the route argument,
 * the photo read, the delete command, the in-flight guard and the outcome-to-copy mapping that the
 * original's `try` / `catch` could not express.
 */
class ClientEditProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    /**
     * The two image commands, over [FakeClientCommandRepository].
     *
     * Both are `error("… is not faked")` in the shared fake, so they are overridden here by
     * delegation rather than reimplementing the other seventeen methods. The right home for them
     * is the shared fake itself — in `core/data`, which owns the contract — and that is flagged in
     * the port report, since this port may not edit outside its own package.
     */
    private class ImageCommands(
        private val result: MutationResult<Unit> = MutationResult.Applied(Unit, synced = true),
        private val gate: CompletableDeferred<Unit>? = null,
        delegate: ClientCommandRepository = FakeClientCommandRepository(),
    ) : ClientCommandRepository by delegate {

        val deleted = mutableListOf<Int>()
        val uploaded = mutableListOf<Int>()

        override suspend fun deleteClientImage(clientId: Int): MutationResult<Unit> {
            deleted += clientId
            gate?.await()
            return result
        }

        override suspend fun uploadClientImage(
            clientId: Int,
            image: DocumentUpload,
        ): MutationResult<Unit> {
            uploaded += clientId
            gate?.await()
            return result
        }
    }

    private fun viewModel(
        repo: FakeClientRepository = FakeClientRepository(),
        commands: ClientCommandRepository = ImageCommands(),
        clientId: Int = 42,
    ) = ClientEditProfileViewModel(
        repo,
        commands,
        SavedStateHandle(mapOf("clientId" to clientId)),
    )

    @Test
    fun theRouteArgumentReachesTheRead() = runTest(dispatcher) {
        // A `0` or a `-1` here is the SavedStateHandle default leaking, and it would show — then
        // DELETE — somebody else's photo.
        val repo = FakeClientRepository(image = null)
        viewModel(repo, clientId = 7)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(7), repo.imageRequests.toList())
    }

    @Test
    fun aMissingPhotoIsNotAnErrorState() = runTest(dispatcher) {
        // Fineract 404s when no photo was ever uploaded — the common case. The avatar placeholder
        // IS the empty state, so nothing on the screen may turn red and both buttons stay usable.
        val vm = viewModel(FakeClientRepository(image = null))
        testScheduler.advanceUntilIdle()

        assertNull(vm.stateFlow.value.profileImage)
        assertNull(vm.stateFlow.value.writeError)
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun theStoredPhotoIsDecodedIntoState() = runTest(dispatcher) {
        if (!imageCodecAvailable()) return@runTest
        // A real 1x1 PNG, because `imageToByteArray` also runs the image codec: arbitrary bytes
        // base64-decode fine and then come back EMPTY. Same trap the profile port's test records.
        val vm = viewModel(FakeClientRepository(image = "data:image/png;base64,$ONE_PIXEL_PNG"))
        testScheduler.advanceUntilIdle()

        val bytes = vm.stateFlow.value.profileImage
        assertTrue(bytes != null && bytes.isNotEmpty(), "expected decoded photo bytes, got $bytes")
    }

    @Test
    fun deleteSendsTheClientIdAndClearsThePhoto() = runTest(dispatcher) {
        if (!imageCodecAvailable()) return@runTest
        val commands = ImageCommands()
        val vm = viewModel(
            repo = FakeClientRepository(image = "data:image/png;base64,$ONE_PIXEL_PNG"),
            commands = commands,
            clientId = 7,
        )
        testScheduler.advanceUntilIdle()
        assertNotNull(vm.stateFlow.value.profileImage)

        vm.trySendAction(ClientEditProfileAction.OnConfirmDeleteClick)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(7), commands.deleted.toList())
        // The avatar returning to its placeholder is the only confirmation a delete gets.
        assertNull(vm.stateFlow.value.profileImage)
        assertFalse(vm.stateFlow.value.isSubmitting)
    }

    @Test
    fun anOfflineDeleteIsDistinguishableFromAFailedOne() = runTest(dispatcher) {
        // The point of routing through MutationResult. The original caught `Exception` and showed
        // `e.message`, so a dropped connection and a server refusal read the same to the officer —
        // and the photo is still there in the first case.
        val vm = viewModel(
            commands = ImageCommands(MutationResult.Blocked(BlockReason.OFFLINE)),
        )
        testScheduler.advanceUntilIdle()

        vm.trySendAction(ClientEditProfileAction.OnConfirmDeleteClick)
        testScheduler.advanceUntilIdle()

        assertEquals(PhotoWriteError.Offline, vm.stateFlow.value.writeError)
    }

    @Test
    fun aFailedDeleteReportsFailedAndLeavesTheScreen() = runTest(dispatcher) {
        if (!imageCodecAvailable()) return@runTest
        val vm = viewModel(
            repo = FakeClientRepository(image = "data:image/png;base64,$ONE_PIXEL_PNG"),
            commands = ImageCommands(MutationResult.Failed(RuntimeException("boom"), rolledBack = false)),
        )
        testScheduler.advanceUntilIdle()

        vm.trySendAction(ClientEditProfileAction.OnConfirmDeleteClick)
        testScheduler.advanceUntilIdle()

        assertEquals(PhotoWriteError.Failed, vm.stateFlow.value.writeError)
        // The photo was NOT cleared: the server still has it, so showing it gone would lie.
        assertNotNull(vm.stateFlow.value.profileImage)
    }

    @Test
    fun aSecondDeleteIsRefusedWhileTheFirstIsStillInFlight() = runTest(dispatcher) {
        // The original had no in-flight guard on either write, and its confirm dialog's button
        // stayed live — two taps sent two DELETEs.
        val gate = CompletableDeferred<Unit>()
        val commands = ImageCommands(gate = gate)
        val vm = viewModel(commands = commands, clientId = 7)
        testScheduler.advanceUntilIdle()

        vm.trySendAction(ClientEditProfileAction.OnConfirmDeleteClick)
        testScheduler.advanceUntilIdle()
        vm.trySendAction(ClientEditProfileAction.OnConfirmDeleteClick)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(7), commands.deleted.toList())

        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertFalse(vm.stateFlow.value.isSubmitting)
    }

    @Test
    fun confirmingDeleteClosesItsDialogBeforeTheWrite() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val vm = viewModel(commands = ImageCommands(gate = gate))
        testScheduler.advanceUntilIdle()

        vm.trySendAction(ClientEditProfileAction.ShowDeleteDialog)
        testScheduler.advanceUntilIdle()
        assertEquals(ClientEditProfileState.DialogState.ShowDeleteDialog, vm.stateFlow.value.dialogState)

        vm.trySendAction(ClientEditProfileAction.OnConfirmDeleteClick)
        testScheduler.advanceUntilIdle()
        // Otherwise the confirm dialog sits over the screen for the whole round trip.
        assertNull(vm.stateFlow.value.dialogState)
        assertTrue(vm.stateFlow.value.isSubmitting)

        gate.complete(Unit)
    }

    @Test
    fun dismissingClearsBothTheDialogAndTheError() = runTest(dispatcher) {
        // One action for both, because the error alert and the sheet are never up together and a
        // dismiss that cleared only one would leave the other stuck.
        val vm = viewModel(commands = ImageCommands(MutationResult.Blocked(BlockReason.OFFLINE)))
        testScheduler.advanceUntilIdle()

        vm.trySendAction(ClientEditProfileAction.OnConfirmDeleteClick)
        testScheduler.advanceUntilIdle()
        assertNotNull(vm.stateFlow.value.writeError)

        vm.trySendAction(ClientEditProfileAction.OnDismissDialog)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.writeError)
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun theBackArrowAndTheSuccessDialogBothLeaveTheScreen() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.eventFlow.test {
            // `NavigateBack` had NO dispatcher in the original — the screen drew no top bar, so the
            // action it declared was unreachable.
            vm.trySendAction(ClientEditProfileAction.NavigateBack)
            assertEquals(ClientEditProfileEvent.NavigateBack, awaitItem())

            // The original's `OnNext`, sent by the success dialog's 1500ms timer.
            vm.trySendAction(ClientEditProfileAction.OnSuccessContinue)
            assertEquals(ClientEditProfileEvent.NavigateBack, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun stateComparesTheImageBytesByContent() = runTest(dispatcher) {
        // A ByteArray in a data class compares by IDENTITY, so without the explicit equals every
        // state copy looks like a change and the avatar recomposes on each one.
        val bytes = byteArrayOf(1, 2, 3)
        assertEquals(
            ClientEditProfileState(profileImage = bytes),
            ClientEditProfileState(profileImage = byteArrayOf(1, 2, 3)),
        )
        assertEquals(
            ClientEditProfileState(profileImage = bytes).hashCode(),
            ClientEditProfileState(profileImage = byteArrayOf(1, 2, 3)).hashCode(),
        )
    }

    private companion object {
        const val ONE_PIXEL_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC"
    }
}
