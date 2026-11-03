/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(
    kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class,
    kpt.core.data.testing.ExperimentalDataTestingApi::class,
)

package kpt.feature.client.details

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeClientCommandRepository
import kpt.core.data.testing.FakeClientRepository
import kpt.core.model.client.Client
import kpt.core.ui.components.ResultStatus
import kpt.feature.client.details.components.ClientDetailsActionItem
import kpt.feature.client.details.components.clientDetailsActionItems
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The AssignStaff row's two behaviours are the only real logic on this screen, and they are what
 * these tests mostly pin.
 */
class ClientDetailsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val clientState = MutableStateFlow<ScreenState<Client>>(ScreenState.Loading)

    private fun repository(image: String? = null) =
        FakeClientRepository(client = clientState, image = image)

    private fun viewModel(
        repo: FakeClientRepository = repository(),
        commands: FakeClientCommandRepository = FakeClientCommandRepository(),
        clientId: Int = 42,
    ) = ClientDetailsViewModel(repo, commands, SavedStateHandle(mapOf("clientId" to clientId)))

    @Test
    fun theRouteArgumentReachesBothReads() = runTest(dispatcher) {
        val repo = repository()
        viewModel(repo, clientId = 7)
        testScheduler.advanceUntilIdle()
        assertEquals(listOf<Any>(7), repo.requestedKeys["client"]?.toList())
        assertEquals(listOf(7), repo.imageRequests.toList())
    }

    @Test
    fun activateOpensTheActivateScreenForThisClient() = runTest(dispatcher) {
        // The original's pending-client bottom bar; the activate route had no caller before this.
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientDetailsAction.OnActivateClick)
            assertEquals(ClientDetailsEvent.NavigateToActivate(42), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun tappingAssignStaffOnAnUnstaffedClientNavigates() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(
                ClientDetailsAction.OnActionClick(ClientDetailsActionItem.AssignStaff, hasStaff = false),
            )
            val event = assertIs<ClientDetailsEvent.OnActionClick>(awaitItem())
            assertEquals(ClientDetailsActionItem.AssignStaff, event.action)
            assertEquals(42, event.clientId)
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun tappingAssignStaffOnAnAlreadyStaffedClientRaisesTheUnassignConfirmationInstead() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(
            ClientDetailsAction.OnActionClick(
                ClientDetailsActionItem.AssignStaff,
                hasStaff = true,
                staffId = 5,
            ),
        )
        testScheduler.advanceUntilIdle()

        // The original's behaviour: the SAME row either navigates to the assign form or asks to
        // unassign, depending on whether a staff member is already set. Fineract needs a few
        // minutes between unassign and re-assign, which is what the dialog's copy warns about.
        val dialog = assertIs<ClientDetailsState.DialogState.UnAssignStaff>(vm.stateFlow.value.dialogState)
        assertEquals(5, dialog.staffId)
    }

    @Test
    fun everyOtherActionNavigatesRegardlessOfStaff() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(
                ClientDetailsAction.OnActionClick(
                    ClientDetailsActionItem.ClosureApplication,
                    hasStaff = true,
                    staffId = 5,
                ),
            )
            assertIs<ClientDetailsEvent.OnActionClick>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun confirmingTheUnassignSendsTheStaffIdFromTheDialogNotFromAReReadClient() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository()
        val vm = viewModel(commands = commands)

        vm.actionChannel.trySend(ClientDetailsAction.ConfirmUnAssignStaff(staffId = 5))
        testScheduler.advanceUntilIdle()

        // The original did `state.client!!.staffId` — a `!!` on a nullable field of a record the
        // ViewModel does not own, read again at confirm time.
        assertEquals(listOf(42 to 5), commands.unassignedStaff)
        val dialog = assertIs<ClientDetailsState.DialogState.ShowStatusDialog>(vm.stateFlow.value.dialogState)
        assertEquals(ResultStatus.SUCCESS, dialog.status)
    }

    @Test
    fun aSecondUnassignInsideTheInFlightWindowIsDropped() = runTest(dispatcher) {
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val commands = FakeClientCommandRepository(gate = gate)
        val vm = viewModel(commands = commands)

        vm.actionChannel.trySend(ClientDetailsAction.ConfirmUnAssignStaff(5))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(ClientDetailsAction.ConfirmUnAssignStaff(5))
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.unassignedStaff.size)
    }

    @Test
    fun acknowledgingSuccessReReadsTheClientSoTheStaffRowUpdates() = runTest(dispatcher) {
        val repo = repository()
        val vm = ClientDetailsViewModel(
            repo,
            FakeClientCommandRepository(),
            SavedStateHandle(mapOf("clientId" to 42)),
        )
        testScheduler.advanceUntilIdle()

        repo.refreshesFor("client").test {
            vm.actionChannel.trySend(ClientDetailsAction.OnNext)
            testScheduler.advanceUntilIdle()
            // Without this the Staff detail row and the AssignStaff row's behaviour both stay on
            // the pre-unassign value from cache.
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun acknowledgingAFailureOnlyDismisses() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository(
            unitResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands = commands)

        vm.actionChannel.trySend(ClientDetailsAction.ConfirmUnAssignStaff(5))
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<ClientDetailsState.DialogState.ShowStatusDialog>(vm.stateFlow.value.dialogState)
        assertEquals(ResultStatus.FAILURE, dialog.status)
        assertTrue(dialog.msg.contains("No connection"), dialog.msg)

        vm.actionChannel.trySend(ClientDetailsAction.DismissDialog)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun theUpdatePhotoAndSignatureEventsCarryTheNameAndAccountNoTheScreenHad() = runTest(dispatcher) {
        val vm = viewModel()
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientDetailsAction.OnUpdatePhotoClick("Asha", "000042"))
            val photo = assertIs<ClientDetailsEvent.NavigateToUpdatePhoto>(awaitItem())
            // The original read `state.client?.displayName ?: ""` at the call site, so a tap during
            // load navigated with an empty name and client -1.
            assertEquals(Triple(42, "Asha", "000042"), Triple(photo.clientId, photo.name, photo.accountNo))

            vm.actionChannel.trySend(ClientDetailsAction.OnUpdateSignatureClick("Asha", "000042"))
            val sig = assertIs<ClientDetailsEvent.NavigateToUpdateSignature>(awaitItem())
            assertEquals(42, sig.clientId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun retryReReadsBothTheClientAndThePhoto() = runTest(dispatcher) {
        val repo = repository()
        val vm = viewModel(repo)
        testScheduler.advanceUntilIdle()
        assertEquals(1, repo.imageRequests.size)

        vm.actionChannel.trySend(ClientDetailsAction.OnRetry)
        testScheduler.advanceUntilIdle()
        assertEquals(2, repo.imageRequests.size)
    }

    @Test
    fun eightOfTheElevenDeclaredActionsRender() {
        // The original commented three out of the list and left their event branches empty. Kept
        // declared-but-unlisted so there is no dead row on screen.
        assertEquals(8, clientDetailsActionItems.size)
        listOf(
            ClientDetailsActionItem.ClientScreenReports,
            ClientDetailsActionItem.ViewStandingInstructions,
            ClientDetailsActionItem.CreateSelfServiceUsers,
        ).forEach { assertTrue(it !in clientDetailsActionItems, "$it must not render") }

        assertEquals(ClientDetailsActionItem.ApplyNewApplication, clientDetailsActionItems.first())
        assertEquals(ClientDetailsActionItem.CreateStandingInstructions, clientDetailsActionItems.last())
    }
}
