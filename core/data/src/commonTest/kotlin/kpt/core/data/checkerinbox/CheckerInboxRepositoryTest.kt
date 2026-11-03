/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.checkerinbox

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.infra.StoreFactory
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.checkerinbox.impl.CheckerInboxCommandRepositoryImpl
import kpt.core.data.infra.testMutationGateway
import kpt.core.model.objects.checkerinboxtask.CheckerInboxSearchTemplate
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.model.objects.checkerinboxtask.RescheduleLoansTask
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.checkerinbox.api.CheckerInboxApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Checker decisions over REAL Store5 stores wired as `CheckerInboxStore.kt` wires them: the inbox
 * read store and the per-task local store share one in-memory `checker_tasks` table.
 */
class CheckerInboxRepositoryTest {

    private fun task(id: Int) = CheckerTask(id, 0, "awaiting.approval", "maker", "CREATE", "CLIENT", "1")

    /** Fineract's pending queue. */
    private val pending = mutableListOf(task(1), task(2))
    private var fetches = 0

    private val table = MutableStateFlow<List<CheckerTask>>(emptyList())

    private val inboxStore = StoreBuilder.from(
        fetcher = Fetcher.of { _: Unit ->
            fetches++
            pending.toList()
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit -> table },
            // Mirrors CheckerInboxDao.replaceAll.
            writer = { _: Unit, tasks: List<CheckerTask> -> table.value = tasks },
        ),
    ).build()

    private val taskStore = StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: Int -> table.map { rows -> rows.firstOrNull { it.id == id } } },
            writer = { id: Int, task: CheckerTask -> table.value = table.value.filterNot { it.id == id } + task },
            delete = { id: Int -> table.value = table.value.filterNot { it.id == id } },
        ),
    )

    private val api = object : CheckerInboxApi {
        override suspend fun getCheckerList(actionName: String?, entityName: String?, resourceId: Int?) =
            error("reads go through the store")

        override suspend fun approveCheckerEntry(auditId: Int) = decided(auditId)
        override suspend fun rejectCheckerEntry(auditId: Int) = decided(auditId)
        override suspend fun deleteCheckerEntry(auditId: Int) = decided(auditId)

        private fun decided(auditId: Int): GenericResponse {
            pending.removeAll { it.id == auditId }
            return GenericResponse()
        }

        override suspend fun getRescheduleLoansTaskList(): List<RescheduleLoansTask> = error("not used")
        override suspend fun getCheckerInboxSearchTemplate(): CheckerInboxSearchTemplate = error("not used")
        override suspend fun getCheckerTasksFromResourceId(actionName: String?, entityName: String?, resourceId: Int?) =
            error("not used")
    }

    private val commands = CheckerInboxCommandRepositoryImpl(api, testMutationGateway(), taskStore)

    @Test
    fun aTaskDecidedElsewhereLeavesTheTableOnTheNextRead() = runTest {
        inboxStore.fresh(Unit)
        pending.removeAll { it.id == 1 }

        inboxStore.fresh(Unit)

        // The old upsert-only writer kept it pending here forever.
        assertEquals(listOf(2), table.value.map { it.id })
    }

    @Test
    fun approveRejectAndDeleteEachTakeTheTaskOutOfTheInboxWithoutARefetch() = runTest {
        pending += task(3)
        inboxStore.fresh(Unit)
        val before = fetches

        commands.approveCheckerEntry(1)
        commands.rejectCheckerEntry(2)
        assertIs<MutationResult.Applied<GenericResponse>>(commands.deleteCheckerEntry(3))

        assertEquals(emptyList(), table.value)
        assertEquals(before, fetches)
    }

    @Test
    fun anOfflineDecisionIsBlockedAndLeavesTheTaskPending() = runTest {
        inboxStore.fresh(Unit)
        val offline = CheckerInboxCommandRepositoryImpl(api, testMutationGateway(isOnline = false), taskStore)

        assertIs<MutationResult.Blocked>(offline.approveCheckerEntry(1))
        assertEquals(listOf(1, 2), table.value.map { it.id })
        assertEquals(2, pending.size)
    }
}
