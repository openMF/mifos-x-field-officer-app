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

package kpt.feature.client.identifiers

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
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
import kpt.core.model.objects.noncoreobjects.DocumentType
import kpt.core.model.objects.noncoreobjects.Identifier
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class ClientIdentifiersViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val identifiers = MutableStateFlow<ScreenState<List<Identifier>>>(ScreenState.Loading)

    /**
     * The shared fake from `core/data`, supplied ONLY the identifiers stream.
     *
     * Every other stream throws if read — which is the diagnostic this test wants: a future
     * regression where this ViewModel reads the wrong stream fails loudly instead of rendering
     * "no identifiers". The per-test anonymous fake it replaced had to re-declare every method,
     * so adding a stream to the interface broke tests that have nothing to do with it.
     */
    private val repository = FakeClientRepository(identifiers = identifiers)

    private val refreshes get() = repository.refreshes

    /** Every clientId the repository was asked for — proves the route argument arrived, not `0`. */
    private val requestedIds: List<Long>
        get() = repository.requestedKeys["identifiers"].orEmpty().map { it as Long }

    private fun viewModel(
        commands: FakeClientCommandRepository = FakeClientCommandRepository(),
        clientId: Int = 42,
    ) = ClientIdentifiersViewModel(
        repository,
        commands,
        SavedStateHandle(mapOf("clientId" to clientId)),
    )

    private fun identifier(id: Int) = Identifier(id = id, clientId = 42, documentKey = "K$id")

    @Test
    fun theRouteArgumentReachesTheRead() = runTest(dispatcher) {
        viewModel(clientId = 7)
        testScheduler.advanceUntilIdle()
        // A `0` here is the classic SavedStateHandle-default bug: the screen loads SOMEBODY's
        // identifiers (or none) and looks merely empty.
        assertEquals(listOf(7L), requestedIds)
    }

    @Test
    fun theStreamStateReachesTheScreen() = runTest(dispatcher) {
        val vm = viewModel()
        identifiers.value = ScreenState.Content(listOf(identifier(1), identifier(2)))

        vm.identifiers.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<List<Identifier>>>(awaitItem())
            assertEquals(2, content.data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun noNetworkArrivesAsAStateNotADialog() = runTest(dispatcher) {
        // The original wrapped every load in `networkMonitor.isOnline` and raised
        // DialogState.NoInternet. The substrate decides this for every screen, so it must arrive
        // as a content state with NO dialog raised.
        val vm = viewModel()
        identifiers.value = ScreenState.NoNetwork()

        vm.identifiers.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.NoNetwork>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // …and NO dialog was raised. That is the half the original got wrong.
        assertEquals(null, vm.stateFlow.value.dialogState)
    }

    @Test
    fun deleteIsConfirmedBeforeItIsSent() = runTest(dispatcher) {
        // Asking for confirmation must NOT send anything — a destructive action that fires on the
        // first tap is the defect this dialog exists to prevent.
        val commands = FakeClientCommandRepository()
        val vm = viewModel(commands)

        vm.trySendAction(ClientIdentifiersAction.ShowDeleteConfirmation(9L))
        testScheduler.advanceUntilIdle()
        assertIs<ClientIdentifiersState.DialogState.DeleteConfirmation>(vm.stateFlow.value.dialogState)
        assertEquals(emptyList(), commands.deletedIdentifiers)

        vm.trySendAction(ClientIdentifiersAction.ConfirmDelete(9L))
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(42L to 9L), commands.deletedIdentifiers)
        assertEquals(null, vm.stateFlow.value.dialogState)
    }

    @Test
    fun aDoubleConfirmSendsOneDelete() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeClientCommandRepository(gate = gate)
        val vm = viewModel(commands)

        vm.trySendAction(ClientIdentifiersAction.ConfirmDelete(9L))
        testScheduler.advanceUntilIdle()
        vm.trySendAction(ClientIdentifiersAction.ConfirmDelete(9L))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(42L to 9L), commands.deletedIdentifiers)
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun anOfflineDeleteIsReportedAndStaysRetryable() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository {
            MutationResult.Blocked(BlockReason.OFFLINE)
        }
        val vm = viewModel(commands)

        vm.trySendAction(ClientIdentifiersAction.ConfirmDelete(9L))
        testScheduler.advanceUntilIdle()

        assertEquals(IdentifierWriteError.Offline, vm.stateFlow.value.writeError)
        // `isDeleting` must clear, or the row stays un-deletable forever with no indication why.
        assertFalse(vm.stateFlow.value.isDeleting)
    }

    // ---- View document ----

    @Test
    fun viewDocumentNamesTheScanByTheKeyTheUploadWrote() = runTest(dispatcher) {
        val vm = viewModel(clientId = 7)
        val row = Identifier(
            id = 3,
            documentKey = "A1234",
            documentType = DocumentType(name = "Passport"),
            // Fineract's status code — mapped through getClientIdentifierStatus, as the list does.
            status = "clientIdentifierStatusType.active",
        )

        vm.eventFlow.test {
            vm.trySendAction(ClientIdentifiersAction.ViewDocument(row))
            assertEquals(
                ClientIdentifiersEvent.ViewDocument(
                    // The scan is keyed by the CLIENT, not the identifier — the original's convention.
                    clientId = 7,
                    matchKey = "PassportA1234Active",
                    matchPrefix = "PassportA1234",
                    title = "A1234",
                ),
                awaitItem(),
            )
        }
    }
}
