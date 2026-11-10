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

package kpt.feature.offlinesync.ui

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeCenterRepository
import kpt.core.data.testing.FakeClientRepository
import kpt.core.data.testing.FakeGroupRepository
import kpt.core.data.testing.FakeNetworkMonitor
import kpt.core.data.testing.FakeOfflineSyncRepository
import kpt.core.model.center.Center
import kpt.core.model.offline.DownloadFailure
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.RefreshMode
import kpt.core.model.offline.RefreshSettings
import kpt.core.model.offline.SyncJob
import kpt.core.model.offline.SyncJobStatus
import kpt.core.model.offline.SyncTarget
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineSyncViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val centre = OfflineRecord(
        RecordKind.CENTRE,
        10,
        "Centro Arin",
        chosen = true,
        groups = 1,
        clients = 2,
        status = RecordSyncStatus.SYNCED,
        refreshedAtMs = NOW - HOUR,
    )
    private val group = OfflineRecord(
        RecordKind.GROUP,
        84,
        "MARIPOSAS MONARCAS",
        parentKind = RecordKind.CENTRE,
        parentId = 10,
        parentName = "Centro Arin",
        clients = 2,
        status = RecordSyncStatus.SYNCED,
        refreshedAtMs = NOW - HOUR,
    )
    private val asha = OfflineRecord(
        RecordKind.CLIENT,
        192,
        "FOTest Asha",
        parentKind = RecordKind.GROUP,
        parentId = 84,
        parentName = "MARIPOSAS MONARCAS",
        status = RecordSyncStatus.SYNCED,
        refreshedAtMs = NOW - HOUR,
    )
    private val irving = OfflineRecord(
        RecordKind.CLIENT,
        205,
        "Irving Zamora",
        chosen = true,
        status = RecordSyncStatus.SYNCED,
        refreshedAtMs = NOW - 2 * DAY,
    )
    private val waiting = OfflineRecord(
        RecordKind.CENTRE,
        18,
        "Arin Savings",
        chosen = true,
        status = RecordSyncStatus.WAITING,
    )

    private fun vm(
        repo: FakeOfflineSyncRepository = FakeOfflineSyncRepository(listOf(centre, group, asha, irving, waiting)),
        network: FakeNetworkMonitor = FakeNetworkMonitor(),
        centers: FakeCenterRepository = FakeCenterRepository(
            listOf(Center(id = 10, name = "Centro Arin"), Center(id = 27, name = "Centro Las Flores")),
        ),
    ) = OfflineSyncViewModel(repo, centers, FakeGroupRepository(), FakeClientRepository(), network, nowMs = { NOW })

    @Test
    fun theDashboardCountsWhatIsOnTheDeviceAtThreeLevelsAndWhatNeedsSync() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()
        val state = vm.stateFlow.value

        assertEquals(OfflineScreen.CONTENT, state.screen)
        assertEquals(2, state.count(RecordKind.CENTRE))
        assertEquals(1, state.count(RecordKind.GROUP))
        assertEquals(2, state.count(RecordKind.CLIENT))
        // Arin Savings is waiting; Irving was synced two days ago on Manual (a day is the limit).
        assertEquals(2, state.toSync)
        // Needs-sync first on a level, then by name.
        assertEquals(listOf("Arin Savings", "Centro Arin"), state.levelRows.map { it.name })
    }

    @Test
    fun levelsShowEveryLocalRecordAndCentresExpandToTheirGroups() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()

        vm.trySendAction(OfflineSyncAction.Level(RecordKind.CLIENT))
        advanceUntilIdle()
        // The client under the group is listed at its level, not only the chosen one.
        assertEquals(listOf("Irving Zamora", "FOTest Asha"), vm.stateFlow.value.levelRows.map { it.name })

        vm.trySendAction(OfflineSyncAction.Search("asha"))
        advanceUntilIdle()
        assertEquals(listOf("FOTest Asha"), vm.stateFlow.value.levelRows.map { it.name })

        vm.trySendAction(OfflineSyncAction.ToggleExpand(centre))
        advanceUntilIdle()
        assertEquals(listOf(group), vm.stateFlow.value.childrenOf(centre))
        assertTrue((RecordKind.CENTRE to 10) in vm.stateFlow.value.expanded)
    }

    @Test
    fun aRunningJobIsTheSyncingStateAndDisablesSyncNow() = runTest(dispatcher) {
        val job = SyncJob(1, SyncJobStatus.RUNNING, listOf(centre.target), reads = 312, startedAtMs = NOW)
        val repo = FakeOfflineSyncRepository(listOf(centre), job = job)
        val vm = vm(repo)
        advanceUntilIdle()

        assertEquals(OfflineScreen.SYNCING, vm.stateFlow.value.screen)
        assertFalse(vm.stateFlow.value.canSync)
        vm.trySendAction(OfflineSyncAction.SyncNow)
        vm.trySendAction(OfflineSyncAction.CancelJob)
        advanceUntilIdle()
        assertEquals(listOf("cancel"), repo.calls, "Sync now is ignored while a job runs; Cancel reaches the job")
    }

    @Test
    fun aFinishedJobWithFailuresIsThePartialFailureStateAndRetriesOnlyFailures() = runTest(dispatcher) {
        val failures = listOf(
            DownloadFailure(
                "Loan 000001475 · documents",
                "Access denied (403): User has no authority to: READ_DOCUMENT",
            ),
        )
        val job = SyncJob(
            1,
            SyncJobStatus.FINISHED,
            listOf(centre.target),
            done = 1,
            reads = 1037,
            failures = failures,
            startedAtMs = NOW,
        )
        val repo = FakeOfflineSyncRepository(listOf(centre), job = job)
        val vm = vm(repo)
        advanceUntilIdle()

        assertEquals(OfflineScreen.PARTIAL_FAILURE, vm.stateFlow.value.screen)
        vm.trySendAction(OfflineSyncAction.RetryFailed)
        advanceUntilIdle()
        assertEquals(listOf("retryFailed"), repo.calls)
    }

    @Test
    fun nothingChosenIsTheEmptyState() = runTest(dispatcher) {
        val vm = vm(FakeOfflineSyncRepository())
        advanceUntilIdle()
        assertEquals(OfflineScreen.EMPTY, vm.stateFlow.value.screen)
    }

    @Test
    fun offlineKeepsTheRecordsButBlocksEverythingThatNeedsTheServer() = runTest(dispatcher) {
        val repo = FakeOfflineSyncRepository(listOf(centre, irving))
        val network = FakeNetworkMonitor(online = false)
        val vm = vm(repo, network)
        advanceUntilIdle()

        assertFalse(vm.stateFlow.value.online)
        assertEquals(OfflineScreen.CONTENT, vm.stateFlow.value.screen, "offline is an overlay, never a blank screen")
        vm.trySendAction(OfflineSyncAction.SyncNow)
        vm.trySendAction(OfflineSyncAction.RetryFailed)
        advanceUntilIdle()
        assertTrue(repo.calls.isEmpty())

        network.online.value = true
        advanceUntilIdle()
        vm.trySendAction(OfflineSyncAction.SyncNow)
        advanceUntilIdle()
        assertEquals(listOf("syncNow"), repo.calls)
    }

    @Test
    fun thePickerListsTheOfficersCentresAndDownloadsTheSelection() = runTest(dispatcher) {
        val repo = FakeOfflineSyncRepository(listOf(centre))
        val vm = vm(repo)
        vm.pickerRows.test {
            assertEquals(ScreenState.Loading, awaitItem())
            vm.trySendAction(OfflineSyncAction.AddRecords)
            val rows = expectMostRecentContent()
            assertEquals(listOf("Centro Arin", "Centro Las Flores"), rows.map { it.target.name })
            cancelAndIgnoreRemainingEvents()
        }
        // Centro Arin is already on the device — the picker says so.
        assertTrue(vm.stateFlow.value.isOnDevice(SyncTarget(RecordKind.CENTRE, 10, "Centro Arin")))

        vm.trySendAction(OfflineSyncAction.PickerSelect(SyncTarget(RecordKind.CENTRE, 27, "Centro Las Flores")))
        vm.trySendAction(OfflineSyncAction.PickerDownload)
        advanceUntilIdle()

        assertEquals(listOf("download:CENTRE:27"), repo.calls)
        assertEquals(null, vm.stateFlow.value.picker, "the sheet closes")
    }

    @Test
    fun thePickerShapesTheListStreamsContentAndDropsRowsTheServerGaveNoId() = runTest(dispatcher) {
        val centers = FakeCenterRepository(
            listOf(Center(id = null, name = "Draft centre"), Center(id = 27, name = null)),
        )
        val vm = vm(centers = centers)
        vm.pickerRows.test {
            assertEquals(ScreenState.Loading, awaitItem())
            vm.trySendAction(OfflineSyncAction.AddRecords)
            val rows = expectMostRecentContent()
            // No id, no target to download; a missing name falls back to the id.
            assertEquals(listOf(SyncTarget(RecordKind.CENTRE, 27, "#27")), rows.map { it.target })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removingAsksFirstAndAutomaticSavesTheSchedule() = runTest(dispatcher) {
        val repo = FakeOfflineSyncRepository(listOf(centre, irving))
        val vm = vm(repo)
        advanceUntilIdle()

        vm.trySendAction(OfflineSyncAction.AskRemove(irving))
        advanceUntilIdle()
        assertTrue(repo.calls.isEmpty(), "nothing is removed before the officer confirms")
        vm.trySendAction(OfflineSyncAction.ConfirmRemove)
        vm.trySendAction(OfflineSyncAction.SetMode(RefreshMode.AUTOMATIC))
        advanceUntilIdle()

        assertEquals(listOf("remove:CLIENT:205", "setRefresh:AUTOMATIC:6:true:false"), repo.calls)
        assertEquals(RefreshSettings(mode = RefreshMode.AUTOMATIC), vm.stateFlow.value.refresh)
    }

    @Test
    fun openingARecordAndTheDraftsLinkNavigate() = runTest(dispatcher) {
        val vm = vm()
        vm.eventFlow.test {
            vm.trySendAction(OfflineSyncAction.OpenRecord(asha))
            assertEquals(OfflineSyncEvent.OpenRecord(RecordKind.CLIENT, 192), awaitItem())
            vm.trySendAction(OfflineSyncAction.OpenDrafts)
            assertEquals(OfflineSyncEvent.OpenDrafts, awaitItem())
        }
    }

    private suspend fun ReceiveTurbine<ScreenState<List<PickerRow>>>.expectMostRecentContent(): List<PickerRow> {
        while (true) {
            val item = awaitItem()
            if (item is ScreenState.Content) return item.data
        }
    }

    private companion object {
        const val NOW = 1_800_000_000_000L
        const val HOUR = 3_600_000L
        const val DAY = 24 * HOUR
    }
}
