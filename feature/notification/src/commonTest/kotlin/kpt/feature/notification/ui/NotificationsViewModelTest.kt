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

package kpt.feature.notification.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeNotificationRepository
import kpt.core.model.notification.AppNotification
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun n(id: Long, type: String, objectId: Long?, read: Boolean) =
        AppNotification(id, type, objectId, "x", "row $id", read, "2026-10-27T09:00:00")

    @Test
    fun markAllReadClearsEveryUnreadRow() = runTest(dispatcher) {
        val repo = FakeNotificationRepository(listOf(n(1, "loan", 9, read = false), n(2, "client", 3, read = true)))
        val vm = NotificationsViewModel(repo)
        vm.trySendAction(NotificationsAction.MarkAllRead)
        advanceUntilIdle()
        assertEquals(1, repo.markedRead)
        val rows = (vm.notifications.first { it is ScreenState.Content } as ScreenState.Content).data
        assertTrue(rows.all { it.isRead })
    }

    @Test
    fun aRefusedMarkAllReadSaysSo() = runTest(dispatcher) {
        val repo = FakeNotificationRepository(
            listOf(n(1, "loan", 9, read = false)),
            markAllReadResult = MutationResult.Failed(IllegalStateException("offline"), rolledBack = false),
        )
        val vm = NotificationsViewModel(repo)
        vm.eventFlow.test {
            vm.trySendAction(NotificationsAction.MarkAllRead)
            assertEquals(NotificationsEvent.MarkAllReadFailed, awaitItem())
        }
    }

    @Test
    fun onlyARowThatNamesAKnownRecordOpensIt() = runTest(dispatcher) {
        val vm = NotificationsViewModel(FakeNotificationRepository())
        val loan = n(1, "loan", 9, read = false)
        vm.eventFlow.test {
            vm.trySendAction(NotificationsAction.Open(n(2, "report", 4, read = true)))
            vm.trySendAction(NotificationsAction.Open(n(3, "loan", null, read = true)))
            vm.trySendAction(NotificationsAction.Open(loan))
            assertEquals(NotificationsEvent.Open(loan), awaitItem())
        }
    }

    @Test
    fun savingsAndCentreSpellingsBothResolve() {
        assertEquals(NotificationTarget.SAVINGS, NotificationTarget.of("savingsAccount"))
        assertEquals(NotificationTarget.CENTER, NotificationTarget.of("centre"))
        assertEquals(null, NotificationTarget.of("report"))
    }
}
