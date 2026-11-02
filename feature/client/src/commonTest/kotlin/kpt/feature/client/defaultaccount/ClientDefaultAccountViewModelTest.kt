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

package kpt.feature.client.defaultaccount

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
import kpt.core.data.testing.fineractRejection
import kpt.core.model.client.Client
import kpt.core.model.client.ClientTemplateOptions
import kpt.core.model.client.SavingAccountOption
import kpt.core.ui.components.ResultStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks what the `clientUpdateDefaultAccount` port changed — above all that the account WRITTEN is
 * the account the officer chose, which the original's index-based selection could not guarantee.
 */
class ClientDefaultAccountViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val template = MutableStateFlow<ScreenState<ClientTemplateOptions>>(ScreenState.Loading)
    private val client = MutableStateFlow<ScreenState<Client>>(ScreenState.Loading)

    private val repository = FakeClientRepository(template = template, client = client)

    private fun viewModel(
        commands: FakeClientCommandRepository = FakeClientCommandRepository(),
        clientId: Int = 42,
    ) = ClientDefaultAccountViewModel(
        repository,
        commands,
        SavedStateHandle(mapOf("clientId" to clientId)),
        kpt.core.data.testing.FakeFormDraftRepository(),
    )

    private fun accounts(vararg ids: Long) = ClientTemplateOptions(
        savingAccountOptions = ids.map { SavingAccountOption(id = it, accountNo = "SA-$it") },
    )

    @Test
    fun theRouteArgumentReachesBothReads() = runTest(dispatcher) {
        viewModel(clientId = 7)
        testScheduler.advanceUntilIdle()
        // The original's route defaulted `clientId` to -1, so a missing key asked Fineract for
        // client -1 — twice, once per read.
        assertEquals(listOf<Any>(7), repository.requestedKeys["template"]?.toList())
        assertEquals(listOf<Any>(7), repository.requestedKeys["client"]?.toList())
    }

    @Test
    fun theAccountListIsProjectedFromTheTemplateStream() = runTest(dispatcher) {
        val vm = viewModel()
        template.value = ScreenState.Content(accounts(11L, 22L))

        vm.accounts.test {
            // `awaitItem`, not `expectMostRecentItem`: WhileSubscribed means nothing flows until
            // this collector attaches, so the first item is still Loading.
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<List<SavingAccountOption>>>(awaitItem())
            // The original issued its own uncached `getClientTemplate` for this one field.
            assertEquals(listOf("SA-11", "SA-22"), content.data.map { it.accountNo })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theClientsCurrentDefaultIsPreSelected() = runTest(dispatcher) {
        val vm = viewModel()
        client.value = ScreenState.Content(Client(id = 42, savingsAccountId = 22L))

        vm.defaultAccountId.test {
            assertNull(awaitItem())
            assertEquals(22L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailedClientReadCostsThePreSelectionAndNothingElse() = runTest(dispatcher) {
        val vm = viewModel()
        template.value = ScreenState.Content(accounts(11L))
        client.value = ScreenState.Error(RuntimeException("boom"))
        testScheduler.advanceUntilIdle()

        // The original wrapped the client fetch in its own try/catch that only logged, so this
        // failure left the form usable. Combining the two streams would have promoted it to a
        // full-screen error on a screen that does not need the client to function.
        vm.accounts.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Content<List<SavingAccountOption>>>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.defaultAccountId.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailedAccountReadStaysAReadStateRatherThanBecomingADialog() = runTest(dispatcher) {
        val vm = viewModel()
        template.value = ScreenState.Error(RuntimeException("boom"))
        testScheduler.advanceUntilIdle()

        vm.accounts.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Error>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // The original raised `DialogState.Error` and hid the whole form behind it. Only the submit
        // RESULT is still a dialog.
        assertNull(vm.stateFlow.value.dialogState)
    }

    @Test
    fun retryReachesBothStreamsAndNothingElse() = runTest(dispatcher) {
        val vm = viewModel()
        repository.refreshesFor("template").test {
            repository.refreshesFor("client").test {
                vm.actionChannel.trySend(ClientDefaultAccountAction.OnRetry)
                testScheduler.advanceUntilIdle()
                expectMostRecentItem()
                cancelAndIgnoreRemainingEvents()
            }
            // The original's retry also launched a fresh, never-cancelled `networkMonitor.isOnline`
            // collector each time; there is no observer to leak now.
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theSelectionIsAddressedByAccountIdSoAReorderedListCannotShiftIt() {
        val before = listOf(
            SavingAccountOption(id = 11L, accountNo = "SA-11"),
            SavingAccountOption(id = 22L, accountNo = "SA-22"),
        )
        // The officer picks the SECOND row — index 1 under the original's scheme.
        assertEquals(22L, before.selectionFor(chosenId = 22L, defaultId = 11L)?.id)

        // The store re-emits the list reordered. An index of 1 now points at account 11 and the
        // original would have submitted THAT as the client's default; the id still resolves to 22.
        val after = before.reversed()
        assertEquals(22L, after.selectionFor(chosenId = 22L, defaultId = 11L)?.id)

        // Untouched form: the client's existing default wins, which is what the original's
        // `getClientSavingsAccountId()` pre-selection did.
        assertEquals(11L, after.selectionFor(chosenId = null, defaultId = 11L)?.id)
        // No default on the client, or one that is not in the list: first row, the original's
        // `currentSelectedIndex = 0` fallback.
        assertEquals(22L, after.selectionFor(chosenId = null, defaultId = null)?.id)
        assertEquals(22L, after.selectionFor(chosenId = 99L, defaultId = 99L)?.id)
        // An empty list has no selection — the screen renders the "no savings accounts" message
        // rather than indexing into nothing, which is what the original would have done.
        assertNull(emptyList<SavingAccountOption>().selectionFor(chosenId = 1L, defaultId = 1L))
    }

    @Test
    fun submitWritesTheChosenAccountAndReReadsTheClient() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository()
        val vm = viewModel(commands)
        template.value = ScreenState.Content(accounts(11L, 22L))
        testScheduler.advanceUntilIdle()

        repository.refreshesFor("client").test {
            vm.actionChannel.trySend(ClientDefaultAccountAction.OnSave(accountId = 22L))
            testScheduler.advanceUntilIdle()

            // Client id in the path argument, ACCOUNT id in the payload — the account the officer
            // chose, carried on the action rather than re-indexed at submit time.
            assertEquals(1, commands.updatedSavingsAccounts.size)
            val (clientId, payload) = commands.updatedSavingsAccounts.single()
            assertEquals(42, clientId)
            assertEquals(22L, payload.savingsAccountId)

            // `savingsAccountId` is a field on the CLIENT, so it is re-read; otherwise coming back
            // would pre-select the previous default from cache and contradict the success dialog.
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }

        val dialog = assertIs<ClientDefaultAccountState.DialogState.ShowStatusDialog>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals(ResultStatus.SUCCESS, dialog.status)
    }

    @Test
    fun aSecondSubmitInsideTheInFlightWindowIsDropped() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeClientCommandRepository(gate = gate)
        val vm = viewModel(commands)
        template.value = ScreenState.Content(accounts(11L))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientDefaultAccountAction.OnSave(accountId = 11L))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(ClientDefaultAccountAction.OnSave(accountId = 11L))
        testScheduler.advanceUntilIdle()

        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        // The original launched a coroutine per tap; its Loading dialog happened to hide the
        // button, which is not a guard.
        assertEquals(1, commands.updatedSavingsAccounts.size)
    }

    @Test
    fun anOfflineSubmitSaysSoRatherThanReportingAGenericFailure() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository(
            unitResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        vm.actionChannel.trySend(ClientDefaultAccountAction.OnSave(accountId = 11L))
        testScheduler.advanceUntilIdle()

        val dialog = assertIs<ClientDefaultAccountState.DialogState.ShowStatusDialog>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals(ResultStatus.FAILURE, dialog.status)
        assertTrue(dialog.msg.contains("No connection"), "offline should be named: ${dialog.msg}")
    }

    @Test
    fun aRejectionKeepsTheServersReason() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository(
            unitResult = {
                MutationResult.Failed(
                    fineractRejection("savings account is not active"),
                    rolledBack = false,
                )
            },
        )
        val vm = viewModel(commands)
        vm.actionChannel.trySend(ClientDefaultAccountAction.OnSave(accountId = 11L))
        testScheduler.advanceUntilIdle()

        // The original's repository extracted the Fineract error body and the dialog showed it.
        val dialog = assertIs<ClientDefaultAccountState.DialogState.ShowStatusDialog>(
            vm.stateFlow.value.dialogState,
        )
        assertEquals("savings account is not active", dialog.msg)
    }

    @Test
    fun dismissingTheFailureDialogReturnsToTheFormAndDoesNotNavigate() = runTest(dispatcher) {
        val commands = FakeClientCommandRepository(
            unitResult = { MutationResult.Blocked(BlockReason.OFFLINE) },
        )
        val vm = viewModel(commands)
        vm.actionChannel.trySend(ClientDefaultAccountAction.OnSave(accountId = 11L))
        testScheduler.advanceUntilIdle()

        vm.actionChannel.trySend(ClientDefaultAccountAction.Dismiss)
        testScheduler.advanceUntilIdle()

        assertNull(vm.stateFlow.value.dialogState)
        // And the write is still submittable — `isSubmitting` was cleared with the result, not
        // left latched by the failure.
        assertEquals(false, vm.stateFlow.value.isSubmitting)
    }
}
