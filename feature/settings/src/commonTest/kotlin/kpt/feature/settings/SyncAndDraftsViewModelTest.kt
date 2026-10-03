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
import kpt.core.base.store.infra.DraftInventory
import kpt.core.base.store.infra.DraftRecord
import kpt.core.base.store.submit.SubmitOutboxStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Locks the [SyncAndDraftsViewModel] contract.
 *
 * The load-bearing behaviour is the STATUS SPLIT: one flat `observeAll()` feed is partitioned into
 * drafts (PENDING) / syncing (RETRYING) / failed (FAILED). Getting that wrong is not cosmetic — a
 * FAILED draft shown as "syncing" tells the user their write is still in flight when it has
 * stopped, which is the offline-write equivalent of reporting an unsynced write as saved. Each
 * bucket is asserted separately so a mis-mapped status cannot pass.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncAndDraftsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeDraftInventory(initial: List<DraftRecord> = emptyList()) : DraftInventory {
        val rows = MutableStateFlow(initial)
        val discarded = mutableListOf<Long>()
        val retried = mutableListOf<Long>()
        var pruneCount = 0
            private set

        override fun observeAll(): Flow<List<DraftRecord>> = rows
        override suspend fun discard(id: Long) {
            discarded += id
        }
        override suspend fun retry(id: Long) {
            retried += id
        }
        override suspend fun pruneExpired() {
            pruneCount++
        }
    }

    private fun draft(id: Long, status: SubmitOutboxStatus) = DraftRecord(
        id = id,
        formKey = "bill-reminder-create",
        uniqueKey = "u$id",
        status = status,
        createdAtMs = 1_700_000_000_000L,
        updatedAtMs = 1_700_000_000_000L,
        errorMessage = if (status == SubmitOutboxStatus.FAILED) "boom" else null,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun partitionsTheFlatFeedByStatus() = runTest {
        val inv = FakeDraftInventory(
            listOf(
                draft(1, SubmitOutboxStatus.PENDING),
                draft(2, SubmitOutboxStatus.RETRYING),
                draft(3, SubmitOutboxStatus.FAILED),
                draft(4, SubmitOutboxStatus.PENDING),
            ),
        )
        val vm = SyncAndDraftsViewModel(inv)
        dispatcher.scheduler.advanceUntilIdle()

        val s = assertIs<SyncAndDraftsUiState.Success>(
            vm.stateFlow.first { it is SyncAndDraftsUiState.Success },
        )
        assertEquals(listOf(1L, 4L), s.drafts.map { it.id })
        assertEquals(listOf(2L), s.syncing.map { it.id })
        assertEquals(listOf(3L), s.failed.map { it.id })
    }

    @Test
    fun retryAndDiscardRouteToTheirOwnInventoryCalls() = runTest {
        // Distinct arms: discarding a draft the user asked to RETRY destroys their unsaved write.
        val inv = FakeDraftInventory(listOf(draft(7, SubmitOutboxStatus.FAILED)))
        val vm = SyncAndDraftsViewModel(inv)

        vm.retry(7L)
        vm.discard(7L)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(7L), inv.retried)
        assertEquals(listOf(7L), inv.discarded)
    }

    @Test
    fun pruneExpiredReachesTheInventory() = runTest {
        val inv = FakeDraftInventory()
        val vm = SyncAndDraftsViewModel(inv)

        vm.pruneExpired()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, inv.pruneCount)
    }
}
