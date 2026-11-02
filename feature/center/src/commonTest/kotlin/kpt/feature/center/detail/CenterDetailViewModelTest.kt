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

package kpt.feature.center.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeRecentRecordsRepository
import kpt.core.model.center.Center
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.group.Group
import kpt.core.model.objects.groups.CenterInfo
import kpt.feature.center.testing.FakeCenterRepository
import kpt.feature.center.testing.center
import kpt.feature.center.testing.group
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * The detail screen merges what the original split across `centerDetails/` and `centerGroupList/`.
 * These tests pin the two properties that merge has to preserve: the screen is about ONE center,
 * and the two halves fail independently.
 */
class CenterDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        centers: FakeCenterRepository,
        centerId: Int = 7,
    ) = CenterDetailViewModel(centers, SavedStateHandle(mapOf("centerId" to centerId)), FakeRecentRecordsRepository())

    /** The centre's own read, carrying [groups] as its `groupMembers`. */
    private fun FakeCenterRepository.emitMembers(vararg groups: Group) {
        meetingState.value = ScreenState.Content(CenterWithAssociations(id = 7, groupMembers = groups.toList()))
    }

    @Test
    fun theScreenShowsTheOneCenterItWasOpenedFor() = runTest(dispatcher) {
        val centers = FakeCenterRepository()
        val vm = viewModel(centers)
        centers.emit(center(1), center(7, name = "Target"), center(9))

        vm.center.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<Center>>(awaitItem())
            assertEquals("Target", content.data.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aCenterOutsideTheCaseloadIsEmptyNotBlankFields() = runTest(dispatcher) {
        // A Content holding null would render a detail page of empty rows, which reads as "this
        // center has no data" rather than "this center is not yours".
        val centers = FakeCenterRepository()
        val vm = viewModel(centers)
        centers.emit(center(1), center(2))

        vm.center.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Empty>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theGroupsAreTheCentresOwnMembers() = runTest(dispatcher) {
        // From `centers/{id}?associations=groupMembers` — the `groups` list never carries
        // `centerId`, so filtering it matched nothing for every centre (device, 2026-10-25).
        val centers = FakeCenterRepository()
        val vm = viewModel(centers)
        centers.emitMembers(group(1, centerId = 7), group(3, centerId = 7))

        vm.groups.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<List<Group>>>(awaitItem())
            assertEquals(listOf(1, 3), content.data.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aCenterWithNoGroupsIsEmptyRatherThanAnEmptyList() = runTest(dispatcher) {
        // Empty renders the empty state; a Content of zero rows renders a blank area that looks
        // like a section which failed to draw.
        val centers = FakeCenterRepository()
        val vm = viewModel(centers)
        centers.emitMembers()

        vm.groups.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Empty>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailedGroupsReadDoesNotBlankTheCenterDetails() = runTest(dispatcher) {
        // THE property the merge depends on. The original had these as two screens, so one failing
        // could not affect the other; folding them into one screen only stays honest if their
        // ScreenStates remain independent. The center's fields are already on screen and still
        // true, so a groups failure must not replace them.
        val centers = FakeCenterRepository()
        val vm = viewModel(centers)
        centers.emit(center(7, name = "Still here"))

        vm.center.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Content<Center>>(awaitItem())

            centers.meetingState.value = ScreenState.Error(RuntimeException("groups boom"))
            testScheduler.advanceUntilIdle()

            // The center flow must NOT re-emit because groups failed. Any emission here is the
            // coupling this test exists to forbid.
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailedCenterReadDoesNotDiscardAlreadyLoadedGroups() = runTest(dispatcher) {
        // The same independence in the other direction — asserted separately because a shared
        // `combine` would pass the first test and fail this one.
        val centers = FakeCenterRepository()
        val vm = viewModel(centers)
        centers.emitMembers(group(1, centerId = 7))

        vm.groups.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Content<List<Group>>>(awaitItem())

            centers.state.value = ScreenState.Error(RuntimeException("center boom"))
            testScheduler.advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theSummaryIsTheReportRowAndAnEmptyReportIsEmptyNotZeros() = runTest(dispatcher) {
        val centers = FakeCenterRepository()
        val vm = viewModel(centers)
        centers.summaryState.value = ScreenState.Content(listOf(CenterInfo(activeClients = 12, overdueClientLoans = 2)))
        vm.summary.test {
            var item = awaitItem()
            while (item is ScreenState.Loading) item = awaitItem()
            val info = assertIs<ScreenState.Content<CenterInfo>>(item).data
            assertEquals(12, info.activeClients)
            assertEquals(2, info.overdueClientLoans)
            centers.summaryState.value = ScreenState.Content(emptyList())
            assertIs<ScreenState.Empty>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addSavingsAccountOpensTheApplicationForThisCentre() = runTest(dispatcher) {
        val vm = viewModel(FakeCenterRepository())
        vm.eventFlow.test {
            vm.trySendAction(CenterDetailAction.AddSavingsAccountClicked)
            assertEquals(CenterDetailEvent.OpenAddSavingsAccount(7), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
