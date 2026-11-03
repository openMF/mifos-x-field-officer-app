/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.signout

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeAuthCommandRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The sign-out CHOICE (Settings, bottom row). What each choice removes is `AuthCommandRepository.signOut`'s
 * job, asserted in core/data's `AuthCommandRepositoryTest`; this pins that the ViewModel asks for the
 * option the officer picked, defaults to clearing data, and keeps the officer signed in when the purge fails.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalDataTestingApi::class)
class SignOutViewModelTest {

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val auth = FakeAuthCommandRepository()

    @Test
    fun signOutIsConfirmedBeforeItHappens() = runTest {
        val vm = SignOutViewModel(auth)
        vm.trySendAction(SignOutAction.Requested)
        assertTrue(vm.stateFlow.value.confirming)
        vm.trySendAction(SignOutAction.Cancelled)
        assertFalse(vm.stateFlow.value.confirming)
        assertEquals(emptyList(), auth.signOuts)
    }

    @Test
    fun theDefaultClearsTheData() = runTest {
        val vm = SignOutViewModel(auth)
        vm.trySendAction(SignOutAction.Requested)
        assertEquals(SignOutMode.SessionAndData, vm.stateFlow.value.mode)
        vm.trySendAction(SignOutAction.Confirmed)
        assertEquals(SignOutEvent.SignedOut, vm.eventFlow.first())
        assertEquals(listOf(true), auth.signOuts)
    }

    @Test
    fun choosingSignOutOnlyKeepsTheData() = runTest {
        val vm = SignOutViewModel(auth)
        vm.trySendAction(SignOutAction.Requested)
        vm.trySendAction(SignOutAction.ModeSelected(SignOutMode.SessionOnly))
        vm.trySendAction(SignOutAction.Confirmed)
        assertEquals(SignOutEvent.SignedOut, vm.eventFlow.first())
        assertEquals(listOf(false), auth.signOuts)
    }

    @Test
    fun reopeningTheDialogStartsOnTheDefaultAgain() = runTest {
        val vm = SignOutViewModel(auth)
        vm.trySendAction(SignOutAction.Requested)
        vm.trySendAction(SignOutAction.ModeSelected(SignOutMode.SessionOnly))
        vm.trySendAction(SignOutAction.Cancelled)
        vm.trySendAction(SignOutAction.Requested)
        assertEquals(SignOutMode.SessionAndData, vm.stateFlow.value.mode)
    }

    @Test
    fun aFailedPurgeKeepsTheOfficerSignedInAndSaysSo() = runTest {
        auth.signOutResult = MutationResult.Failed(RuntimeException("disk"), rolledBack = false)
        val vm = SignOutViewModel(auth)
        vm.trySendAction(SignOutAction.Requested)
        vm.trySendAction(SignOutAction.Confirmed)
        assertTrue(vm.stateFlow.value.failed)
        assertTrue(vm.stateFlow.value.confirming)
    }
}
