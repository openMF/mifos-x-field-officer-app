/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.conflict.ConflictEntry
import kpt.core.base.store.mutation.conflict.ConflictInbox
import kpt.core.base.store.mutation.conflict.ConflictResolution
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Locks the [ConflictInboxViewModel] contract.
 *
 * The per-row actions are the point: `MutationResult.Conflicted` is a distinct arm precisely so a
 * write conflict reaches the user as a CHOICE rather than being silently resolved, and this screen
 * is where that choice is made. A regression that collapsed AcceptServer and RetryLocal — or
 * dropped the id — would silently discard one side of a conflicted write, so both are asserted to
 * reach the inbox with the right [ConflictResolution].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConflictInboxViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeConflictInbox(initial: List<ConflictEntry> = emptyList()) : ConflictInbox {
        val pending = MutableStateFlow(initial)
        val resolved = mutableListOf<Pair<String, ConflictResolution>>()

        override suspend fun record(
            entity: String,
            key: String,
            localPayloadJson: String,
            serverPayloadJson: String,
            formRoute: String?,
        ): String = "c-new"

        override fun observePending(): Flow<List<ConflictEntry>> = pending

        override suspend fun resolve(conflictId: String, resolution: ConflictResolution) {
            resolved += conflictId to resolution
            pending.value = pending.value.filterNot { it.id == conflictId }
        }
    }

    private fun entry(id: String) = ConflictEntry(
        id = id,
        entity = "CloudTodo",
        key = "1",
        localPayloadJson = """{"completed":true}""",
        serverPayloadJson = """{"completed":false}""",
        formRoute = null,
        recordedAtMs = 1_700_000_000_000L,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun startsLoadingThenPublishesThePendingFeed() = runTest {
        val e = entry("c1")
        val vm = ConflictInboxViewModel(FakeConflictInbox(listOf(e)))
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.stateFlow.first { it is ConflictInboxUiState.Success }
        assertEquals(listOf(e), assertIs<ConflictInboxUiState.Success>(state).conflicts)
    }

    @Test
    fun acceptServerResolvesWithAcceptServer() = runTest {
        val inbox = FakeConflictInbox(listOf(entry("c1")))
        val vm = ConflictInboxViewModel(inbox)

        vm.acceptServer("c1")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("c1" to ConflictResolution.ACCEPT_SERVER), inbox.resolved)
    }

    @Test
    fun retryLocalResolvesWithRetryLocalNotAcceptServer() = runTest {
        // The two arms must stay distinct — collapsing them would discard the user's local payload
        // while reporting the conflict handled.
        val inbox = FakeConflictInbox(listOf(entry("c1")))
        val vm = ConflictInboxViewModel(inbox)

        vm.retryLocal("c1")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("c1" to ConflictResolution.RETRY_LOCAL), inbox.resolved)
    }
}
