/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.client.ClientRepository
import kpt.core.database.client.entity.ClientAccounts
import kpt.core.database.client.entity.ClientEntity
import kpt.core.database.loan.entity.LoanAccountEntity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the four defects the port fixed (see [ClientDetailViewModel]'s header).
 *
 * The id-read and the independent-state tests are the two that the original arrangement could not
 * have passed: it read `clientId` before SavedStateHandle carried it, and it shared one
 * `showLoading` flag across two concurrent collectors.
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class, ExperimentalCoroutinesApi::class)
class ClientDetailViewModelTest {

    private val clientState = MutableStateFlow<ScreenState<ClientEntity>>(ScreenState.Loading)
    private val accountsState = MutableStateFlow<ScreenState<ClientAccounts>>(ScreenState.Loading)
    private val clientRefreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    private val accountRefreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    /** Ids the repository was asked for — proves the route argument reached it, not a `0` default. */
    private val requestedIds = mutableListOf<Int>()

    private val repository = object : ClientRepository {
        override fun clientsStream(scope: CoroutineScope) = error("not used by the detail screen")

        override fun clientStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<ClientEntity> {
            requestedIds += clientId
            return screenDataStreamForTesting(clientState, refreshTrigger = clientRefreshes)
        }

        override fun clientAccountsStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<ClientAccounts> {
            requestedIds += clientId
            return screenDataStreamForTesting(accountsState, refreshTrigger = accountRefreshes)
        }
    }

    private fun viewModel(clientId: Int = 42) =
        ClientDetailViewModel(repository, SavedStateHandle(mapOf("clientId" to clientId)))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(Dispatchers.Unconfined)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun bothStreamsOpenOnTheRouteClientIdNotZero() = runTest {
        viewModel(clientId = 42)
        // The original read `clientId.value` from a SavedStateHandle flow whose initialValue was 0,
        // so the FIRST request went out for client 0 before the real id arrived.
        assertEquals(listOf(42, 42), requestedIds)
    }

    @Test
    fun identityRendersWhileAccountsAreStillLoading() = runTest {
        val vm = viewModel()
        clientState.value = ScreenState.Content(ClientEntity(id = 42, displayName = "Asha"))

        val state = vm.stateFlow.value
        // One shared `showLoading` meant a slow accounts call hid an already-cached name.
        assertTrue(state.client is ScreenState.Content)
        assertTrue(state.accounts is ScreenState.Loading)
        assertEquals("Asha", (state.client as ScreenState.Content).data.displayName)
    }

    @Test
    fun aFailedAccountsReadLeavesIdentityIntact() = runTest {
        val vm = viewModel()
        clientState.value = ScreenState.Content(ClientEntity(id = 42, displayName = "Asha"))
        accountsState.value = ScreenState.Error(RuntimeException("boom"))

        assertTrue(vm.stateFlow.value.client is ScreenState.Content)
        assertTrue(vm.stateFlow.value.accounts is ScreenState.Error)
    }

    @Test
    fun retryReachesTheStreamThatFailed() = runTest {
        val vm = viewModel()
        accountRefreshes.test {
            vm.actionChannel.trySend(ClientDetailAction.RetryAccounts)
            // A Retry wired to `{}` would leave this expectation unmet — that is the whole point of
            // holding the streams rather than collecting them inline.
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun accountTapsCarryTheAccountIdNotTheClientId() = runTest {
        val vm = viewModel(clientId = 42)
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientDetailAction.LoanClicked(loanId = 7))
            assertEquals(ClientDetailEvent.OpenLoan(7), awaitItem())

            vm.actionChannel.trySend(ClientDetailAction.SavingsClicked(savingsId = 9))
            assertEquals(ClientDetailEvent.OpenSavings(9), awaitItem())

            // Notes and documents hang off the CLIENT, so they carry the client id.
            vm.actionChannel.trySend(ClientDetailAction.NotesClicked)
            assertEquals(ClientDetailEvent.OpenNotes(42), awaitItem())
        }
    }

    @Test
    fun anAccountsPayloadWithRowsIsContentNotEmpty() = runTest {
        val vm = viewModel()
        accountsState.value = ScreenState.Content(
            ClientAccounts(loanAccounts = listOf(LoanAccountEntity(id = 7, productName = "Group loan"))),
        )
        val accounts = vm.stateFlow.value.accounts
        assertTrue(accounts is ScreenState.Content)
        assertEquals(1, accounts.data.loanAccounts.size)
    }
}
