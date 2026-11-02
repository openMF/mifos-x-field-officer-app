/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.feature.collectionsheet.ui

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.office.OfficeRepository
import kpt.core.data.staff.StaffRepository
import kpt.core.model.office.Office
import kpt.core.model.staff.Staff
import kpt.core.model.utils.ApiDateFormatter
import kpt.feature.collectionsheet.ui.newsheet.Choice
import kpt.feature.collectionsheet.ui.newsheet.NewCollectionSheetAction
import kpt.feature.collectionsheet.ui.newsheet.NewCollectionSheetEvent
import kpt.feature.collectionsheet.ui.newsheet.NewCollectionSheetViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NewCollectionSheetViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val offices = object : OfficeRepository {
        override fun officesStream(scope: CoroutineScope) =
            screenDataStreamForTesting(
                flowOf<ScreenState<List<Office>>>(ScreenState.Content(listOf(Office(id = 1, name = "Head Office")))),
            )
    }

    private val staffAsked = mutableListOf<Int>()
    private val staff = object : StaffRepository {
        override fun staffStream(officeId: Int, scope: CoroutineScope) = screenDataStreamForTesting(
            flowOf<ScreenState<List<Staff>>>(
                ScreenState.Content(listOf(Staff(id = officeId * 10, displayName = "Officer $officeId"))),
            ),
        ).also { staffAsked += officeId }
    }

    private fun vm() = NewCollectionSheetViewModel(offices, staff)

    @Test
    fun anOfficeIsRequiredStaffIsNot() = runTest(dispatcher) {
        val vm = vm()
        assertFalse(vm.stateFlow.value.canGenerate)
        vm.trySendAction(NewCollectionSheetAction.SelectOffice(Choice(1, "Head Office")))
        runCurrent()
        assertTrue(vm.stateFlow.value.canGenerate)
    }

    @Test
    fun anUnsetStaffIsAbsentNotZero() = runTest(dispatcher) {
        val vm = vm()
        vm.trySendAction(NewCollectionSheetAction.SelectOffice(Choice(1, "Head Office")))
        vm.trySendAction(NewCollectionSheetAction.DatePicked(DAY))
        runCurrent()
        vm.eventFlow.test {
            vm.trySendAction(NewCollectionSheetAction.Generate)
            runCurrent()
            // The original sent staffId = 0 when none was chosen.
            assertEquals(NewCollectionSheetEvent.OpenSheet(1, null, ApiDateFormatter.formatForApi(DAY)), awaitItem())
        }
    }

    @Test
    fun changingOfficeClearsTheStaffAndLoadsThatOfficesStaff() = runTest(dispatcher) {
        val vm = vm()
        backgroundScope.launch { vm.staffOptions.collect {} }
        vm.trySendAction(NewCollectionSheetAction.SelectOffice(Choice(1, "Head Office")))
        runCurrent()
        assertEquals(listOf(10), vm.staffOptions.value.map { it.id })
        vm.trySendAction(NewCollectionSheetAction.SelectStaff(Choice(10, "Officer 1")))
        vm.trySendAction(NewCollectionSheetAction.SelectOffice(Choice(2, "Branch")))
        runCurrent()
        assertNull(vm.stateFlow.value.staff)
        assertEquals(listOf(20), vm.staffOptions.value.map { it.id })
        assertEquals(listOf(1, 2), staffAsked)
    }

    @Test
    fun clearResetsTheForm() = runTest(dispatcher) {
        val vm = vm()
        vm.trySendAction(NewCollectionSheetAction.SelectOffice(Choice(1, "Head Office")))
        vm.trySendAction(NewCollectionSheetAction.Clear)
        runCurrent()
        assertNull(vm.stateFlow.value.office)
    }

    private companion object {
        /** 2026-10-23T12:00Z. */
        const val DAY = 1_792_756_800_000L
    }
}
